package com.kfyty.core.utils;

import com.kfyty.core.exception.SupportException;
import com.kfyty.core.support.FilePart;
import com.kfyty.core.support.FilePartDescription;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

/**
 * 描述: io 工具
 *
 * @author kfyty725
 * @date 2022/7/2 11:13
 * @email kfyty725@hotmail.com
 */
@Slf4j
public abstract class IOUtil {
    /**
     * 默认的缓冲区大小
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * 临时文件夹位置
     */
    public static final String TEMP_PATH = System.getProperty("java.io.tmpdir");

    /**
     * 获取路径
     *
     * @param path 路径
     * @return 路径
     */
    public static Path getPath(String path) {
        try {
            return Paths.get(path);
        } catch (Exception e) {
            log.error("get path failed: {}, error message: {}", path, e.getMessage());
            return null;
        }
    }

    /**
     * 将输入流复制到输出流
     *
     * @param in  输入流
     * @param out 输出流
     * @return 输出流
     */
    public static <T extends OutputStream> T copy(InputStream in, T out) {
        return copy(in, out, DEFAULT_BUFFER_SIZE);
    }

    /**
     * 将输入流复制到输出流
     *
     * @param in     输入流
     * @param out    输出流
     * @param buffer 复制时使用的缓冲区大小
     * @return 输出流
     */
    public static <T extends OutputStream> T copy(InputStream in, T out, int buffer) {
        try {
            int n = -1;
            byte[] bytes = new byte[buffer];
            while ((n = in.read(bytes)) != -1) {
                write(out, bytes, 0, n);
            }
            out.flush();
            return out;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 将字节数组写入到输出流
     *
     * @param out   输出流
     * @param bytes 字节数组
     * @return 输出流
     */
    public static <T extends OutputStream> T write(T out, byte[] bytes) {
        return write(out, bytes, 0, bytes.length);
    }

    /**
     * 将字节数组写入到输出流
     *
     * @param out   输出流
     * @param bytes 字节数组
     * @param start 数组开始位置
     * @param limit 读取的长度
     * @return 输出流
     */
    public static <T extends OutputStream> T write(T out, byte[] bytes, int start, int limit) {
        try {
            out.write(bytes, start, limit);
            return out;
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 将输入流转换为可重复读取的输入流
     *
     * @param inputStream 输入流
     * @return 字节数组输入流
     */
    public static InputStream repeatable(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        }
        return new ByteArrayInputStream(copy(inputStream, new ByteArrayOutputStream()).toByteArray());
    }

    /**
     * 将输入流转换为字符串
     *
     * @param inputStream 输入流
     * @return 字符串
     */
    public static String toString(InputStream inputStream) {
        if (inputStream == null) {
            return CommonUtil.EMPTY_STRING;
        }
        return copy(inputStream, new ByteArrayOutputStream()).toString();
    }

    /**
     * 将输入流写入临时文件，并设置程序退出时删除
     *
     * @param inputStream 输入流
     * @return 临时文件
     */
    public static File writeToTemp(InputStream inputStream) {
        File file = new File(TEMP_PATH + UUID.randomUUID());
        file.deleteOnExit();
        return writeTo(inputStream, file);
    }

    /**
     * 将输入流写入文件
     *
     * @param inputStream 输入流
     * @param file        文件
     * @return 文件
     */
    public static File writeTo(InputStream inputStream, File file) {
        if (inputStream != null) {
            copy(inputStream, newOutputStream(requireNonNull(file)));
        }
        return file;
    }

    /**
     * 从文件返回一个输入流
     *
     * @param file 文件
     * @return 输入流
     */
    public static InputStream newInputStream(File file) {
        try {
            return Files.newInputStream(file.toPath());
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 从文件返回一个输出流
     *
     * @param file 文件
     * @return 输出流
     */
    public static OutputStream newOutputStream(File file) {
        try {
            return Files.newOutputStream(file.toPath());
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 扫描路径下的文件
     *
     * @param path 路径
     * @return 文件列表
     */
    public static List<File> scanFiles(String path) {
        return scanFiles(path, e -> true);
    }

    /**
     * 根据断言扫描路径下的文件
     *
     * @param path          路径
     * @param filePredicate 文件断言
     * @return 文件列表
     */
    public static List<File> scanFiles(String path, Predicate<File> filePredicate) {
        return scanFiles(path, filePredicate, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 根据断言扫描路径下的文件
     *
     * @param path          路径
     * @param filePredicate 文件断言
     * @param classLoader   ClassLoader
     * @return 文件列表
     */
    public static List<File> scanFiles(String path, Predicate<File> filePredicate, ClassLoader classLoader) {
        try {
            URL root = Objects.requireNonNull(classLoader.getResource(""));
            File file = new File(root.getPath() + path);
            if (file.isFile()) {
                return filePredicate.test(file) ? singletonList(file) : emptyList();
            }
            File[] files = file.listFiles();
            return files == null ? emptyList() : Arrays.stream(files).filter(File::isFile).filter(filePredicate).collect(Collectors.toList());
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    /**
     * 确保文件夹已存在
     *
     * @param path 路径
     */
    public static void ensureFolderExists(String path) {
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new SupportException("ensure folder exists failed !");
        }
    }

    /**
     * 下载到指定目录
     *
     * @param url     url
     * @param dirName 目录
     * @return 文件
     */
    public static File download(String url, String dirName) {
        return download(url, dirName, UUID.randomUUID().toString().replace("-", ""));
    }

    /**
     * 下载到指定目录
     *
     * @param url      url
     * @param dirName  目录
     * @param fileName 文件名称
     * @return 文件
     */
    public static File download(String url, String dirName, String fileName) {
        try {
            long start = System.currentTimeMillis();
            ensureFolderExists(dirName);

            File file = new File(dirName + "/" + fileName);
            URL httpUrl = new URL(url.replace(" ", "%20"));
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.connect();

            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

            IOUtil.copy(bis, bos);

            bos.close();
            bis.close();
            conn.disconnect();
            log.info("下载文件耗时: {} -> {} ms", url, System.currentTimeMillis() - start);

            return file;
        } catch (IOException e) {
            throw new SupportException(e);
        }
    }

    /**
     * 分割文件
     *
     * @param srcFile     文件
     * @param splitSizeMB 分片大小
     * @return 分片文件描述
     */
    public static List<FilePart> split(File srcFile, int splitSizeMB) {
        return split(srcFile, splitSizeMB, true);
    }

    /**
     * 分割文件
     *
     * @param srcFile         文件
     * @param splitSizeMB     分片大小
     * @param onlyDescription 是否仅保存分割描述文件
     * @return 分片文件描述
     */
    public static List<FilePart> split(File srcFile, int splitSizeMB, boolean onlyDescription) {
        try {
            List<FilePart> fileParts = new ArrayList<>();
            String splitDir = UUID.randomUUID().toString().replace("-", "");
            ensureFolderExists(splitDir);

            long totalSize = srcFile.length();                                                          // 文件
            long size = splitSizeMB * 1024L * 1024L;                                                    // 每个分片大小

            if (size >= totalSize) {
                return Collections.singletonList(new FilePart(srcFile));
            }

            long splitSize = 0L;
            int fileCount = (int) (totalSize / size);                                                   // 计算分片数量
            boolean isOdd = totalSize % size != 0;                                                      // 是否不能整除
            try (RandomAccessFile rf = new RandomAccessFile(srcFile, "r")) {
                for (int i = 1; i <= fileCount; i++) {
                    int length = (int) size;
                    if (i == fileCount && isOdd) {
                        length = (int) (totalSize - (fileCount - 1) * size);
                    }
                    if (onlyDescription) {
                        FilePartDescription fpd = new FilePartDescription(i, (int) splitSize, length, i + "_" + srcFile.getName(), new RandomAccessFile(srcFile, "r"));
                        splitSize += fpd.getLength();
                        fileParts.add(fpd);
                        log.info("after split file name: {}, file size: {}", fpd.getName(), fpd.getLength());
                        continue;
                    }
                    File filePart = new File(splitDir, i + "_" + srcFile.getName());
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filePart))) {
                        byte[] bs = new byte[length];
                        rf.read(bs);
                        out.write(bs);
                        out.flush();
                        splitSize += filePart.length();
                    }
                    fileParts.add(new FilePart(i, filePart));
                    log.info("after split file name: {}, file size: {}", filePart.getName(), filePart.length());
                }
            }
            log.info("split ok, total: {}, split: {}....", totalSize, splitSize);
            return fileParts;
        } catch (IOException e) {
            throw new SupportException(e);
        }
    }

    /**
     * 刷新并关闭一个对象
     *
     * @param obj 对象
     */
    public static void close(Object obj) {
        if (obj == null) {
            return;
        }
        if (!(obj instanceof AutoCloseable)) {
            throw new SupportException("can't close !");
        }
        try {
            if (obj instanceof Flushable) {
                ((Flushable) obj).flush();
            }
            ((AutoCloseable) obj).close();
        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }
}
