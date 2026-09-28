-- Flyway Migration V2: Add message column to work_requests table (Phase 10 - REQ-001)
ALTER TABLE work_requests ADD COLUMN message TEXT NULL AFTER status;
