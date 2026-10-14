package com.studiolynk.model.dto.admin;

import java.time.Instant;

/**
 * DTO for Studio identity/document submission status for Admin monitoring (ADM-005).
 */
public class AdminStudioIdentityDto {

    private Long id;
    private Long studioId;
    private String studioName;
    private String ownerName;
    private String studioEmail;
    private String phone;
    private String documentType;
    private String documentUrl;
    private String declarationText;
    private String status;
    private Instant submittedAt;

    public AdminStudioIdentityDto() {
    }

    public AdminStudioIdentityDto(Long id, Long studioId, String studioName, String ownerName,
                                  String studioEmail, String phone, String documentType,
                                  String documentUrl, String declarationText, String status,
                                  Instant submittedAt) {
        this.id = id;
        this.studioId = studioId;
        this.studioName = studioName;
        this.ownerName = ownerName;
        this.studioEmail = studioEmail;
        this.phone = phone;
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

    public Long getStudioId() {
        return studioId;
    }

    public void setStudioId(Long studioId) {
        this.studioId = studioId;
    }

    public String getStudioName() {
        return studioName;
    }

    public void setStudioName(String studioName) {
        this.studioName = studioName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStudioEmail() {
        return studioEmail;
    }

    public void setStudioEmail(String studioEmail) {
        this.studioEmail = studioEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
