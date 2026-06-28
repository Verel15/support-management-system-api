package com.ticket.support_management_system_api.features.project.service;

import com.ticket.support_management_system_api.common.exception.FileValidationException;
import com.ticket.support_management_system_api.common.exception.ResourceNotFoundException;
import com.ticket.support_management_system_api.features.project.dto.ProjectDocumentResponse;
import com.ticket.support_management_system_api.features.project.entities.Project;
import com.ticket.support_management_system_api.features.project.entities.ProjectDocument;
import com.ticket.support_management_system_api.features.project.repository.ProjectDocumentRepository;
import com.ticket.support_management_system_api.features.project.repository.ProjectRepository;
import com.ticket.support_management_system_api.features.upload.dto.FileUploadResponse;
import com.ticket.support_management_system_api.features.upload.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectDocumentService {

    private static final int MAX_DOCUMENTS_PER_PROJECT = 50;
    private static final String UPLOAD_FOLDER = "project-documents";

    private final ProjectDocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final FileUploadService fileUploadService;

    @Transactional(readOnly = true)
    public List<ProjectDocumentResponse> findAllByProject(UUID projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("ไม่พบโปรเจค id: " + projectId);
        }
        return documentRepository.findAllByProjectIdAndArchivedAtIsNull(projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectDocumentResponse uploadDocument(UUID projectId, MultipartFile file) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบโปรเจค id: " + projectId));

        long docCount = documentRepository.countByProjectIdAndArchivedAtIsNull(projectId);
        if (docCount >= MAX_DOCUMENTS_PER_PROJECT) {
            throw new FileValidationException("โปรเจคนี้มีเอกสารครบ " + MAX_DOCUMENTS_PER_PROJECT + " ไฟล์แล้ว");
        }

        FileUploadResponse uploaded = fileUploadService.upload(file, UPLOAD_FOLDER);

        ProjectDocument document = ProjectDocument.builder()
                .project(project)
                .fileName(uploaded.getFileName())
                .fileUrl(uploaded.getFileUrl())
                .contentType(uploaded.getContentType())
                .fileSize(uploaded.getFileSize())
                .build();

        return toResponse(documentRepository.save(document));
    }

    public void deleteDocument(UUID projectId, UUID documentId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("ไม่พบโปรเจค id: " + projectId);
        }
        ProjectDocument document = documentRepository.findByIdAndProjectId(documentId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("ไม่พบเอกสาร id: " + documentId));

        fileUploadService.delete(document.getFileUrl());

        document.setArchivedAt(LocalDateTime.now());
        documentRepository.save(document);
    }

    private ProjectDocumentResponse toResponse(ProjectDocument doc) {
        return ProjectDocumentResponse.builder()
                .id(doc.getId())
                .fileName(doc.getFileName())
                .fileUrl(doc.getFileUrl())
                .contentType(doc.getContentType())
                .fileSize(doc.getFileSize())
                .createdAt(doc.getCreatedAt())
                .build();
    }
}
