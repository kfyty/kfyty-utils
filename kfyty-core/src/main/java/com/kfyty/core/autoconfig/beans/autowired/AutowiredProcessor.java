package com.kfyty.core.autoconfig.beans.autowired;

import com.kfyty.core.autoconfig.ApplicationContext;
import com.kfyty.core.autoconfig.LaziedObject;
import com.kfyty.core.autoconfig.annotation.Autowired;
import com.kfyty.core.autoconfig.beans.BeanDefinition;
import com.kfyty.core.exception.BeansException;
import com.kfyty.core.generic.ActualGeneric;
import com.kfyty.core.generic.Generic;
import com.kfyty.core.generic.SimpleGeneric;
import com.kfyty.core.lang.Lazy;
import com.kfyty.core.utils.AopUtil;
import com.kfyty.core.utils.BeanUtil;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.ReflectUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription.isLazied;
import static com.kfyty.core.autoconfig.beans.autowired.AutowiredDescription.isRequired;
import static com.kfyty.core.utils.AnnotationUtil.findAnnotation;
import static com.kfyty.core.utils.AnnotationUtil.hasAnnotation;
import static com.kfyty.core.utils.AopUtil.getTargetClass;
import static com.kfyty.core.utils.AopUtil.isJdkProxy;
import static java.util.Optional.ofNullable;

/**
 * 描述: 自动注入处理器
 *
 * @author kfyty725
 * @date 2021/6/12 11:28
 * @email kfyty725@hotmail.com
 */
@Slf4j
public class AutowiredProcessor {
    /**
     * 正在解析中的 bean name
     */
    private final Set<String> resolving;

    /**
     * 应用上下文
     */
    @Getter
    private final ApplicationContext context;

    public AutowiredProcessor(ApplicationContext context) {
        this.context = context;
        this.resolving = new LinkedHashSet<>();
    }

    public void doAutowired(Object bean, Field field) {
        AutowiredDescription description = AutowiredDescription.from(field);
        if (description != null) {
            this.doAutowired(bean, field, description.markLazied(hasAnnotation(field, com.kfyty.core.autoconfig.annotation.Lazy.class)));
        }
    }

    public void doAutowired(Object bean, Method method) {
        AutowiredDescription description = AutowiredDescription.from(method);
        if (description != null) {
            this.doAutowired(bean, method, description.markLazied(hasAnnotation(method, com.kfyty.core.autoconfig.annotation.Lazy.class)));
        }
    }

    public void doAutowired(Object bean, Field field, AutowiredDescription description) {
        if (ReflectUtil.getFieldValue(bean, field) != null) {
            return;
        }
        ActualGeneric actualGeneric = ActualGeneric.from(bean.getClass(), field);
        Object targetBean = this.doResolveBean(actualGeneric, description, field.getType());
        if (targetBean != null) {
            ReflectUtil.setFieldValue(bean, field, targetBean);
            if (log.isDebugEnabled()) {
                log.debug("autowired bean: {} -> {} !", targetBean, bean);
            }
        }
    }

    public void doAutowired(Object bean, Method method, AutowiredDescription description) {
        int index = 0;
        Object[] parameters = new Object[method.getParameterCount()];
        for (Parameter parameter : method.getParameters()) {
            ActualGeneric actualGeneric = ActualGeneric.from(bean.getClass(), parameter);
            AutowiredDescription paramDescription = ofNullable(AutowiredDescription.from(findAnnotation(parameter, Autowired.class))).orElse(description);
            Object targetBean = this.doResolveBean(actualGeneric, paramDescription, parameter.getType());
            parameters[index++] = targetBean;
        }
        ReflectUtil.invokeMethod(bean, method, parameters);
        if (log.isDebugEnabled()) {
            log.debug("autowired bean: {} -> {} !", parameters, bean);
        }
    }

    /**
     * 解析 bean 依赖
     * 仅解析自动装配的候选者
     *
     * @param actualGeneric 实际泛型
     * @param description   自动注入描述
     * @param requiredType  实际请求类型
     * @return bean
     */
    public Object doResolveBean(ActualGeneric actualGeneric, AutowiredDescription description, Class<?> requiredType) {
        String beanName = BeanUtil.getBeanName(actualGeneric.getSimpleActualType(), description == null ? null : description.value());
        Supplier<Object> targetBeanProvider = () -> this.doResolveBean(beanName, actualGeneric, description);
        Object targetBean = LaziedObject.class.isAssignableFrom(actualGeneric.getSourceType()) ? new Lazy<>(targetBeanProvider) : targetBeanProvider.get();
        if (targetBean != null && isJdkProxy(targetBean) && requiredType.equals(getTargetClass(targetBean))) {
            targetBean = AopUtil.getTarget(targetBean);
        }
        return targetBean;
    }

    /**
     * 解析 bean 依赖
     * 仅解析自动装配的候选者
     *
     * @param targetBeanName 目标 bean name，如果是泛型则忽略
     * @param returnType     目标 bean 类型
     * @return bean
     */
    public Object doResolveBean(String targetBeanName, ActualGeneric returnType, AutowiredDescription autowired) {
        Object resolveBean = null;
        Map<String, Object> beans = this.doGetBean(targetBeanName, returnType.getSimpleActualType(), returnType.isSimpleGeneric(), autowired);
        if (List.class.isAssignableFrom(returnType.getSourceType())) {
            resolveBean = new ArrayList<>(beans.values());
        }
        if (Set.class.isAssignableFrom(returnType.getSourceType())) {
            resolveBean = new HashSet<>(beans.values());
        }
        if (returnType.isMapGeneric()) {
            resolveBean = beans;
        }
        if (returnType.isSimpleArray()) {
            resolveBean = beans.values().toArray((Object[]) Array.newInstance(returnType.getSimpleActualType(), 0));
        }
        if (resolveBean == null) {
            resolveBean = beans.size() == 1 ? beans.values().iterator().next() : this.matchBeanIfNecessary(beans, targetBeanName, returnType);
        }
        return resolveBean;
    }

    private synchronized void checkResolving(String targetBeanName) {
        if (this.resolving.contains(targetBeanName)) {
            throw new BeansException("bean circular dependency: \r\n" + this.buildCircularDependency());
        }
    }

    private synchronized void prepareResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if (!isGeneric) {
            this.checkResolving(targetBeanName);
            if (!this.context.containsReference(targetBeanName)) {
                this.resolving.add(targetBeanName);
            }
            return;
        }
        for (BeanDefinition beanDefinition : this.context.getBeanDefinitions(targetType).values()) {
            this.checkResolving(beanDefinition.getBeanName());
            if (!this.context.containsReference(beanDefinition.getBeanName())) {
                this.resolving.add(beanDefinition.getBeanName());
            }
        }
    }

    private synchronized void removeResolving(String targetBeanName, Class<?> targetType, boolean isGeneric) {
        if (!isGeneric) {
            this.resolving.remove(targetBeanName);
        } else {
            this.context.getBeanDefinitions(targetType).values().forEach(e -> this.resolving.remove(e.getBeanName()));
        }
    }

    private Map<String, Object> doGetBean(String targetBeanName, Class<?> targetType, boolean isGeneric, AutowiredDescription autowired) {
        Map<String, Object> beanOfType = new LinkedHashMap<>(2);
        Map<String, BeanDefinition> targetBeanDefinitions = new LinkedHashMap<>();
        if (this.context.containsBeanDefinition(targetBeanName)) {
            Optional.of(this.context.getBeanDefinition(targetBeanName)).ifPresent(bd -> targetBeanDefinitions.put(bd.getBeanName(), bd));
        } else {
            targetBeanDefinitions.putAll(this.context.getBeanDefinitions(targetType));
        }
        for (Iterator<Map.Entry<String, BeanDefinition>> i = targetBeanDefinitions.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry<String, BeanDefinition> entry = i.next();
            if (!entry.getValue().isAutowireCandidate()) {
                i.remove();
                continue;
            }
            if (this.context.contains(entry.getKey())) {
                beanOfType.put(entry.getKey(), this.context.getBean(entry.getKey()));
            } else if (isGeneric) {
                try {
                    this.prepareResolving(targetBeanName, targetType, true);
                    this.context.registerBeanReference(entry.getValue());
                } finally {
                    this.removeResolving(targetBeanName, targetType, true);
                }
            }
        }
        if (beanOfType.size() < targetBeanDefinitions.size()) {
            try {
                this.prepareResolving(targetBeanName, targetType, isGeneric);
                if (isGeneric) {
                    for (Map.Entry<String, BeanDefinition> entry : targetBeanDefinitions.entrySet()) {
                        if (!beanOfType.containsKey(entry.getKey())) {
                            beanOfType.put(entry.getKey(), this.context.registerBean(entry.getValue(), isLazied(autowired)));
                        }
                    }
                } else {
                    BeanDefinition beanDefinition = targetBeanDefinitions.size() == 1 ? targetBeanDefinitions.values().iterator().next() : targetBeanDefinitions.get(targetBeanName);
                    if (beanDefinition == null) {
                        if (!isRequired(autowired)) {
                            return beanOfType;
                        }
                        throw new BeansException(CommonUtil.format("resolve target bean failed, more than one bean definition of type {}, but no bean definition found of name: {}", targetType, targetBeanName));
                    }
                    beanOfType.put(beanDefinition.getBeanName(), this.context.registerBean(beanDefinition, isLazied(autowired)));
                }
            } finally {
                this.removeResolving(targetBeanName, targetType, isGeneric);
            }
        }
        if (isRequired(autowired) && beanOfType.isEmpty() || isRequired(autowired) && !isGeneric && beanOfType.size() > 1 && !beanOfType.containsKey(targetBeanName)) {
            throw new BeansException("resolve target bean failed, the bean does not exists of name: " + targetBeanName);
        }
        return beanOfType;
    }

    private Object matchBeanIfNecessary(Map<String, ?> beans, String beanName, ActualGeneric actualGeneric) {
        Object bean = beans.get(beanName);
        if (bean != null) {
            return bean;
        }
        List<Generic> targetGenerics = new ArrayList<>(actualGeneric.getGenericInfo().keySet());
        for (Object value : beans.values()) {
            SimpleGeneric generic = SimpleGeneric.from(getTargetClass(value));
            if (generic.size() != targetGenerics.size()) {
                continue;
            }
            boolean matched = true;
            List<Generic> generics = new ArrayList<>(generic.getGenericInfo().keySet());
            for (int i = 0; i < generics.size(); i++) {
                if (!Objects.equals(targetGenerics.get(i).get(), generics.get(i).get())) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                if (bean != null) {
                    throw new BeansException("resolve target bean failed, more than one generic bean found of name: " + beanName);
                }
                bean = value;
            }
        }
        return bean;
    }

    private String buildCircularDependency() {
        StringBuilder builder = new StringBuilder("┌─────┐\r\n");
        Object[] beanNames = this.resolving.toArray();
        for (int i = 0; i < beanNames.length; i++) {
            builder.append(beanNames[i]).append(" -> ").append(this.context.getBeanDefinition(beanNames[i].toString())).append("\r\n");
            if (i < beanNames.length - 1) {
                builder.append("↑     ↓\r\n");
            }
        }
        return builder.append("└─────┘").toString();
    }
}
