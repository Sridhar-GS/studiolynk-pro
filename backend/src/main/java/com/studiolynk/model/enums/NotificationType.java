package com.studiolynk.model.enums;

/**
 * Defines notification event categories per NOT-002 and docs/11-NOTIFICATION-SPECIFICATION.md
 */
public enum NotificationType {
    REQUEST_RECEIVED,     // Studio sent a work request to freelancer
    REQUEST_ACCEPTED,     // Freelancer accepted studio's request
    REQUEST_REJECTED,     // Freelancer declined studio's request
    STUDIO_CONFIRMATION,  // Studio confirmed freelancer booking
    NEW_MESSAGE,          // New message received in conversation
    WORK_STARTED,         // Work requirement marked IN_PROGRESS
    WORK_COMPLETED,       // Work requirement marked COMPLETED
    WORK_CANCELLED,       // Confirmed work cancelled by either party
    RATING_REMINDER       // Shoot finished; prompt to leave public star rating and review
}
