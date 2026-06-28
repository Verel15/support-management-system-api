package com.ticket.support_management_system_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.upload")
@Getter
@Setter
public class FileUploadProperties {
    private long maxFileSizeBytes = 10_485_760L;
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private String localBasePath = System.getProperty("user.home") + "/uploads";
}
