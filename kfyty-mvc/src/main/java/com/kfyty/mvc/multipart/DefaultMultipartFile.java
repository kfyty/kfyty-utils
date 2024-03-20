package com.kfyty.mvc.multipart;

import com.kfyty.core.lang.Lazy;
import com.kfyty.core.utils.CommonUtil;
import com.kfyty.core.utils.IOUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 描述:
 *
 * @author kfyty725
 * @date 2021/6/4 14:02
 * @email kfyty725@hotmail.com
 */
@RequiredArgsConstructor
public class DefaultMultipartFile implements MultipartFile {
    private final String formName;
    private final String fileName;
    private final String contentType;
    private final boolean isFile;
    private final long size;
    private final Lazy<InputStream> inputStreamLazy;

    @Override
    public boolean isFile() {
        return this.isFile;
    }

    @Override
    public String getName() {
        return this.formName;
    }

    @Override
    public String getOriginalFilename() {
        return this.fileName;
    }

    @Override
    public String getContentType() {
        return this.contentType;
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
        try (InputStream in = this.getInputStream()) {
            return IOUtil.read(in);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.inputStreamLazy.get();
    }

    @Override
    public void transferTo(File dest) throws Exception {
        if (dest.exists() && !dest.delete()) {
            throw new IOException("Destination file [" + dest.getAbsolutePath() + "] already exists and could not be deleted !");
        }
        try (InputStream in = this.getInputStream()) {
            IOUtil.copy(in, IOUtil.newOutputStream(dest));
        }
    }

    public static List<MultipartFile> from(HttpServletRequest request) {
        try {
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            fileItemFactory.setDefaultCharset(request.getCharacterEncoding());
            FileUpload fileUpload = new FileUpload();
            fileUpload.setHeaderEncoding(StandardCharsets.UTF_8.name());
            fileUpload.setFileItemFactory(fileItemFactory);
            List<FileItem> fileItems = fileUpload.parseRequest(new ServletRequestContext(request));
            List<MultipartFile> multipartFiles = new ArrayList<>(fileItems.size());
            for (FileItem fileItem : fileItems) {
                Lazy<InputStream> inputStream = new Lazy<>(() -> {
                    try {
                        return fileItem.getInputStream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                multipartFiles.add(new DefaultMultipartFile(fileItem.getFieldName(), fileItem.getName(), fileItem.getContentType(), !fileItem.isFormField(), fileItem.getSize(), inputStream));
            }
            if (multipartFiles.isEmpty() && CommonUtil.notEmpty(request.getParts())) {
                for (Part part : request.getParts()) {
                    Lazy<InputStream> inputStream = new Lazy<>(() -> {
                        try {
                            return part.getInputStream();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    multipartFiles.add(new DefaultMultipartFile(part.getName(), part.getSubmittedFileName(), part.getContentType(), part.getSubmittedFileName() != null, part.getSize(), inputStream));
                }
            }
            return multipartFiles;
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
