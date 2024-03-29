package com.kfyty.core.utils;

import com.kfyty.core.lang.util.concurrent.WeakConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.stream.Collectors;

import static com.kfyty.core.utils.CommonUtil.EMPTY_STRING;
import static com.kfyty.core.utils.ReflectUtil.isAbstract;

/**
 * 功能描述: 解析 package 工具
 *
 * @author kfyty725@hotmail.com
 * @date 2019/8/22 14:17
 * @since JDK 1.8
 */
@Slf4j
public abstract class PackageUtil {
    private static final Map<String, Set<String>> SCAN_PACKAGE_CACHE = new WeakConcurrentHashMap<>(4);

    public static Set<String> scanClassName(Class<?> mainClass) {
        return scanClassName(mainClass.getPackage().getName());
    }

    public static Set<Class<?>> scanClass(Class<?> mainClass) {
        return scanClass(mainClass.getPackage().getName());
    }

    public static <T> List<T> scanInstance(Class<T> mainClass) {
        return scanInstance(mainClass, clazz -> !isAbstract(clazz) && mainClass.isAssignableFrom(clazz));
    }

    public static <T> List<T> scanInstance(Class<T> mainClass, Predicate<Class<?>> scanFilter) {
        return scanInstance(mainClass.getPackage().getName(), scanFilter);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> scanInstance(String basePackage, Predicate<Class<?>> scanFilter) {
        Set<Class<?>> classes = PackageUtil.scanClass(basePackage);
        return (List<T>) classes.stream().filter(scanFilter).map(ReflectUtil::newInstance).collect(Collectors.toList());
    }

    public static Set<String> scanClassName(String basePackage) {
        try {
            Set<String> cache = SCAN_PACKAGE_CACHE.get(basePackage);
            if (cache != null) {
                return cache;
            }
            Set<String> classes = new HashSet<>();
            Enumeration<URL> urls = ClassLoaderUtil.classLoader(PackageUtil.class).getResources(basePackage.replace('.', '/'));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if ("jar".equalsIgnoreCase(url.getProtocol())) {
                    classes.addAll(scanClassNameByJar(url));
                    continue;
                }
                classes.addAll(scanClassNameByFile(url));
            }
            return SCAN_PACKAGE_CACHE.computeIfAbsent(basePackage, k -> classes);
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Set<Class<?>> scanClass(String basePackage) {
        Set<Class<?>> result = new HashSet<>();
        Set<String> classes = scanClassName(basePackage);
        if (CommonUtil.empty(classes)) {
            return result;
        }
        for (String clazz : classes) {
            Optional.ofNullable(ReflectUtil.load(clazz, false, false)).ifPresent(result::add);
        }
        return result;
    }

    private static Set<String> scanClassNameByJar(URL url) {
        try {
            Set<String> classes = new HashSet<>();
            String path = !url.getPath().contains("!") ? url.getPath() : url.getPath().split("!")[1];
            Enumeration<JarEntry> entries = ((JarURLConnection) url.openConnection()).getJarFile().entries();
            while (entries.hasMoreElements()) {
                String classPath = entries.nextElement().getName();
                if (!("/" + classPath).startsWith(path) || !classPath.endsWith(".class")) {
                    continue;
                }
                classes.add(classPath.replace('/', '.').replace(".class", EMPTY_STRING));
            }
            return classes;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    private static Set<String> scanClassNameByFile(URL url) {
        try {
            Set<String> classes = new HashSet<>();
            File[] files = new File(url.getPath()).listFiles();
            if (CommonUtil.empty(files)) {
                return classes;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(scanClassNameByFile(file.toURI().toURL()));
                    continue;
                }
                if (!file.getPath().endsWith(".class")) {
                    continue;
                }
                String classPath = file.getPath();
                classPath = classPath.substring(classPath.indexOf("classes" + File.separator) + 8, classPath.lastIndexOf("."));
                classes.add(classPath.replace(File.separator, "."));
            }
            return classes;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
