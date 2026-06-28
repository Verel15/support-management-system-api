package com.ticket.support_management_system_api.features.upload.service;

import com.ticket.support_management_system_api.common.exception.FileValidationException;
import com.ticket.support_management_system_api.config.FileUploadProperties;
import com.ticket.support_management_system_api.features.upload.dto.FileUploadResponse;
import com.ticket.support_management_system_api.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final Pattern SAFE_FOLDER = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
            "image/jpeg",      new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "image/png",       new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
            "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
    );
    private static final byte[] ZIP_MAGIC  = {0x50, 0x4B, 0x03, 0x04};
    private static final byte[] OLE2_MAGIC = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0};

    private final StorageService storageService;
    private final FileUploadProperties uploadProperties;

    public FileUploadResponse upload(MultipartFile file, String folder) {
        validateFolder(folder);
        validateFile(file);
        String relativePath = storageService.store(file, folder);
        return FileUploadResponse.builder()
                .fileName(file.getOriginalFilename())
                .fileUrl("/api/v1/files/" + relativePath)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();
    }

    public void delete(String fileUrl) {
        String relativePath = fileUrl.replaceFirst("^/api/v1/files/", "");
        storageService.delete(relativePath);
    }

    public Resource loadAsResource(String folder, String filename) {
        return storageService.load(folder + "/" + filename);
    }

    private void validateFolder(String folder) {
        if (!SAFE_FOLDER.matcher(folder).matches()) {
            throw new FileValidationException("ชื่อ folder ไม่ถูกต้อง: ใช้ได้เฉพาะตัวอักษร ตัวเลข - และ _");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("ไม่สามารถอัปโหลดไฟล์ว่างได้");
        }
        if (file.getSize() > uploadProperties.getMaxFileSizeBytes()) {
            long limitMB = uploadProperties.getMaxFileSizeBytes() / 1_048_576;
            throw new FileValidationException("ไฟล์มีขนาดใหญ่เกิน " + limitMB + " MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !uploadProperties.getAllowedContentTypes().contains(contentType)) {
            throw new FileValidationException("ประเภทไฟล์ไม่รองรับ: " + contentType);
        }
        validateMagicBytes(file, contentType);
    }

    private void validateMagicBytes(MultipartFile file, String contentType) {
        try {
            byte[] header = file.getInputStream().readNBytes(16);
            byte[] expected = MAGIC_BYTES.get(contentType);
            if (expected != null) {
                if (!startsWith(header, expected)) {
                    throw new FileValidationException("เนื้อหาไฟล์ไม่ตรงกับประเภทที่ระบุ");
                }
            } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
                if (!startsWith(header, ZIP_MAGIC)) {
                    throw new FileValidationException("เนื้อหาไฟล์ไม่ตรงกับประเภทที่ระบุ");
                }
            } else if ("application/msword".equals(contentType)) {
                if (!startsWith(header, OLE2_MAGIC)) {
                    throw new FileValidationException("เนื้อหาไฟล์ไม่ตรงกับประเภทที่ระบุ");
                }
            }
        } catch (FileValidationException e) {
            throw e;
        } catch (IOException e) {
            throw new FileValidationException("ไม่สามารถอ่านไฟล์ได้");
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        return Arrays.equals(Arrays.copyOf(data, prefix.length), prefix);
    }
}
