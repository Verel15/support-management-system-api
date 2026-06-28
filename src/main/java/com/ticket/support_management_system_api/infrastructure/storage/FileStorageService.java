package com.ticket.support_management_system_api.infrastructure.storage;

import com.ticket.support_management_system_api.common.exception.StorageException;
import com.ticket.support_management_system_api.config.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService implements StorageService {

    private final FileUploadProperties uploadProperties;

    @Override
    public String store(MultipartFile file, String folder) {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String storedFilename = UUID.randomUUID() + ext;
        String relativePath = folder + "/" + storedFilename;

        Path targetPath = resolveStoragePath(relativePath);
        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("ไม่สามารถบันทึกไฟล์ได้: " + e.getMessage());
        }
        return relativePath;
    }

    @Override
    public void delete(String relativePath) {
        Path filePath = resolveStoragePath(relativePath);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new StorageException("ไม่สามารถลบไฟล์ได้: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String relativePath) {
        Path filePath = resolveStoragePath(relativePath);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new StorageException("ไม่พบไฟล์หรือไม่สามารถอ่านได้: " + relativePath);
        } catch (MalformedURLException e) {
            throw new StorageException("เส้นทางไฟล์ไม่ถูกต้อง: " + relativePath);
        }
    }

    private Path resolveStoragePath(String relativePath) {
        return Paths.get(uploadProperties.getLocalBasePath()).resolve(relativePath).normalize();
    }
}
