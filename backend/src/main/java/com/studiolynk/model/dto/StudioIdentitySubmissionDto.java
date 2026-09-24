package com.studiolynk.model.dto;

import com.studiolynk.model.enums.SubmissionStatus;

import java.time.Instant;

public class StudioIdentitySubmissionDto {
    private Long id;
    private String documentType;
    private String documentUrl;
    private String declarationText;
    private SubmissionStatus status;
    private Instant submittedAt;

    public StudioIdentitySubmissionDto() {
    }

    public StudioIdentitySubmissionDto(Long id, String documentType, String documentUrl, String declarationText, SubmissionStatus status, Instant submittedAt) {
        this.id = id;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.declarationText = declarationText;
        this.status = status;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentUrl() {
        return documentUrl;
    }

    public void setDocumentUrl(String documentUrl) {
        this.documentUrl = documentUrl;
    }

    public String getDeclarationText() {
        return declarationText;
    }

    public void setDeclarationText(String declarationText) {
        this.declarationText = declarationText;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
