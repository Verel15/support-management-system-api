package com.ticket.support_management_system_api.features.upload.controller;

import com.ticket.support_management_system_api.common.exception.TooManyRequestsException;
import com.ticket.support_management_system_api.common.response.ApiResponse;
import com.ticket.support_management_system_api.features.auth.model.JwtPrincipal;
import com.ticket.support_management_system_api.features.upload.dto.FileUploadResponse;
import com.ticket.support_management_system_api.features.upload.service.FileUploadService;
import com.ticket.support_management_system_api.features.upload.service.UploadRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UploadRateLimitService uploadRateLimitService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            @AuthenticationPrincipal JwtPrincipal user) {

        if (!uploadRateLimitService.tryConsumeUpload(user.userId().toString())) {
            throw new TooManyRequestsException("อัปโหลดไฟล์บ่อยเกินไป กรุณาลองใหม่ในภายหลัง");
        }
        FileUploadResponse response = fileUploadService.upload(file, folder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("อัปโหลดไฟล์สำเร็จ", response));
    }

    @GetMapping("/{folder}/{filename}")
    public ResponseEntity<Resource> download(
            @PathVariable String folder,
            @PathVariable String filename,
            @AuthenticationPrincipal JwtPrincipal user) {

        Resource resource = fileUploadService.loadAsResource(folder, filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam("url") String fileUrl,
            @AuthenticationPrincipal JwtPrincipal user) {

        fileUploadService.delete(fileUrl);
        return ResponseEntity.ok(ApiResponse.success("ลบไฟล์สำเร็จ", null));
    }
}
