package org.example.gundokai.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.example.gundokai.exception.AppException;
import org.example.gundokai.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {
    private final Cloudinary cloudinary;

    @Autowired
    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB limit

    public String uploadFile(MultipartFile file) {
        validateFile(file);

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";

            // Get extension (like .jpg, .png)
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Generate a clean filename without any extensions
            String fileName = UUID.randomUUID().toString();

            // Upload lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("public_id", fileName, "folder", "gundokai"));

            String url = uploadResult.get("url").toString();
            log.info("File uploaded successfully to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Upload failed: {}", e.getMessage());
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            // Lấy public_id từ URL Cloudinary
            String publicId = extractPublicId(fileUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted successfully from Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }

    private String extractPublicId(String fileUrl) {
        // Lấy public_id từ URL (bỏ phần domain và version)
        String[] parts = fileUrl.split("/upload/")[1].split("/");
        StringBuilder publicId = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            publicId.append(parts[i]);
            if (i < parts.length - 1) {
                publicId.append("/");
            }
        }
        return publicId.toString().substring(0, publicId.lastIndexOf("."));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }
    }
}
