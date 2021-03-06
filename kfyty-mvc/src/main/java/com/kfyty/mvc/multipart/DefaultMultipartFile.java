package com.kfyty.mvc.multipart;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 14:02
 * @email kfyty725@hotmail.com
 */
public class DefaultMultipartFile implements MultipartFile {
    private final FileItem fileItem;
    private final long size;

    public DefaultMultipartFile(FileItem fileItem) {
        this.fileItem = fileItem;
        this.size = fileItem.getSize();
    }

    @Override
    public boolean isFile() {
        return !this.fileItem.isFormField();
    }

    @Override
    public String getName() {
        return fileItem.getFieldName();
    }

    @Override
    public String getOriginalFilename() {
        return fileItem.getName();
    }

    @Override
    public String getContentType() {
        return fileItem.getContentType();
    }

    @Override
    public boolean isEmpty() {
        return getSize() < 1;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return fileItem.get();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileItem.getInputStream();
    }

    @Override
    public void transferTo(File dest) throws Exception {
        if (dest.exists() && !dest.delete()) {
            throw new IOException("Destination file [" + dest.getAbsolutePath() + "] already exists and could not be deleted !");
        }
        this.fileItem.write(dest);
    }

    public static List<MultipartFile> from(HttpServletRequest request) {
        try {
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            ServletFileUpload fileUpload = new ServletFileUpload();
            fileItemFactory.setDefaultCharset("UTF-8");
            fileUpload.setHeaderEncoding("UTF-8");
            fileUpload.setFileItemFactory(fileItemFactory);
            List<FileItem> fileItems = fileUpload.parseRequest(new ServletRequestContext(request));
            List<MultipartFile> multipartFiles = new ArrayList<>(fileItems.size());
            for (FileItem fileItem : fileItems) {
                multipartFiles.add(new DefaultMultipartFile(fileItem));
            }
            return multipartFiles;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
