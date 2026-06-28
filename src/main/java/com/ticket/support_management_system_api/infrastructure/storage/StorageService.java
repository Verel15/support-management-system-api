package com.ticket.support_management_system_api.infrastructure.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String folder);
    void delete(String relativePath);
    Resource load(String relativePath);
}
