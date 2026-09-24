package com.studiolynk.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.studiolynk.model.enums.SubmissionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entity for Studio owner prototype identity verification declaration and document metadata.
 * In accordance with STU-002 and STU-003, this stores prototype identity declarations without
 * external Aadhaar APIs or Aadhaar number persistence.
 */
@Entity
@Table(name = "studio_identity_submissions")
public class StudioIdentitySubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studio_id", nullable = false)
    @JsonBackReference
    private Studio studio;

    @Column(name = "document_type", nullable = false, length = 50)
    private String documentType;

    @Column(name = "document_url", length = 512)
    private String documentUrl;

    @Column(name = "declaration_text", columnDefinition = "TEXT")
    private String declarationText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    public StudioIdentitySubmission() {
    }

    public StudioIdentitySubmission(Studio studio, String documentType, String documentUrl, String declarationText, SubmissionStatus status) {
        this.studio = studio;
        this.documentType = documentType;
        this.documentUrl = documentUrl;
        this.declarationText = declarationText;
        this.status = status != null ? status : SubmissionStatus.SUBMITTED;
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.submittedAt == null) {
            this.submittedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Studio getStudio() {
        return studio;
    }

    public void setStudio(Studio studio) {
        this.studio = studio;
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
