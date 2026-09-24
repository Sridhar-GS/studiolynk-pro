-- ==============================================================================
-- StudioLynk Database Schema Migration (V1)
-- Final-Year Academic Prototype
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. Identity & Authentication
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDIO', 'FREELANCER', 'ADMIN') NOT NULL,
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS password_reset_otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. Studio Domain
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS studios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    studio_name VARCHAR(150) NOT NULL,
    owner_name VARCHAR(150) NOT NULL,
    logo_url VARCHAR(512),
    phone VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    years_of_operation INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_studios_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_studios_name (studio_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS studio_social_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    studio_id BIGINT NOT NULL,
    platform_name VARCHAR(50) NOT NULL,
    url VARCHAR(512) NOT NULL,
    CONSTRAINT fk_studio_links_studio FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS studio_identity_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    studio_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_url VARCHAR(512),
    declaration_text TEXT,
    status ENUM('SUBMITTED', 'VERIFIED') NOT NULL DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_studio_identity_studio FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. Freelancer Domain
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS freelancers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    profile_photo_url VARCHAR(512),
    phone VARCHAR(50) NOT NULL,
    address TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    experience_years INT NOT NULL DEFAULT 0,
    bio TEXT,
    full_day_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    half_day_rate DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_freelancers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_freelancers_rates (full_day_rate, half_day_rate),
    INDEX idx_freelancers_exp (experience_years)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Skills Master & Mapping
CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id BIGINT,
    CONSTRAINT fk_skills_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_skills_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS freelancer_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    freelancer_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    CONSTRAINT fk_fs_freelancer FOREIGN KEY (freelancer_id) REFERENCES freelancers(id) ON DELETE CASCADE,
    CONSTRAINT fk_fs_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY uq_freelancer_skill (freelancer_id, skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Services Master & Mapping
CREATE TABLE IF NOT EXISTS services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id BIGINT,
    CONSTRAINT fk_services_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_services_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS freelancer_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    freelancer_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    CONSTRAINT fk_fserv_freelancer FOREIGN KEY (freelancer_id) REFERENCES freelancers(id) ON DELETE CASCADE,
    CONSTRAINT fk_fserv_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    UNIQUE KEY uq_freelancer_service (freelancer_id, service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Equipment Categories & Mapping
CREATE TABLE IF NOT EXISTS equipment_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    is_custom BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id BIGINT,
    CONSTRAINT fk_eq_category FOREIGN KEY (category_id) REFERENCES equipment_categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_eq_creator FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_eq_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS freelancer_equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    freelancer_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    CONSTRAINT fk_fe_freelancer FOREIGN KEY (freelancer_id) REFERENCES freelancers(id) ON DELETE CASCADE,
    CONSTRAINT fk_fe_equipment FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    UNIQUE KEY uq_freelancer_equipment (freelancer_id, equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 4. Portfolio Domain
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    freelancer_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_portfolio_freelancer FOREIGN KEY (freelancer_id) REFERENCES freelancers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS portfolio_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portcat_portfolio FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE,
    INDEX idx_portcat_order (portfolio_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS portfolio_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    s3_key VARCHAR(512) NOT NULL,
    image_url VARCHAR(1024) NOT NULL,
    original_filename VARCHAR(255),
    content_type VARCHAR(100),
    file_size BIGINT,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_portimg_category FOREIGN KEY (category_id) REFERENCES portfolio_categories(id) ON DELETE CASCADE,
    INDEX idx_portimg_order (category_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 5. Availability Domain (Rolling 10-Day Window)
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS freelancer_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    freelancer_id BIGINT NOT NULL,
    available_date DATE NOT NULL,
    status ENUM('AVAILABLE', 'BUSY', 'NOT_SET') NOT NULL DEFAULT 'NOT_SET',
    start_time TIME,
    end_time TIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_avail_freelancer FOREIGN KEY (freelancer_id) REFERENCES freelancers(id) ON DELETE CASCADE,
    UNIQUE KEY uq_freelancer_date (freelancer_id, available_date),
    INDEX idx_avail_lookup (available_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 6. Work Requirements & Requests Domain
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    studio_id BIGINT NOT NULL,
    event_name VARCHAR(150) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    location TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    day_type ENUM('FULL_DAY', 'HALF_DAY') NOT NULL DEFAULT 'FULL_DAY',
    budget DECIMAL(10, 2) NOT NULL,
    description TEXT,
    status ENUM('DRAFT', 'OPEN', 'REQUESTED', 'ACCEPTED', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'OPEN',
    -- Protected private event contact details (revealed strictly upon studio confirmation)
    event_contact_name VARCHAR(150),
    event_contact_phone VARCHAR(50),
    confirmed_freelancer_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_req_studio FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    CONSTRAINT fk_req_confirmed_fl FOREIGN KEY (confirmed_freelancer_id) REFERENCES freelancers(id) ON DELETE SET NULL,
    INDEX idx_req_date_status (event_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS work_requirement_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    CONSTRAINT fk_reqsk_req FOREIGN KEY (requirement_id) REFERENCES work_requirements(id) ON DELETE CASCADE,
    CONSTRAINT fk_reqsk_skill FOREIGN KEY (skill_id) REFERENCES skills(id) ON DELETE CASCADE,
    UNIQUE KEY uq_req_skill (requirement_id, skill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS work_requirement_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    CONSTRAINT fk_reqserv_req FOREIGN KEY (requirement_id) REFERENCES work_requirements(id) ON DELETE CASCADE,
    CONSTRAINT fk_reqserv_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    UNIQUE KEY uq_req_service (requirement_id, service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS work_requirement_equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    CONSTRAINT fk_reqeq_req FOREIGN KEY (requirement_id) REFERENCES work_requirements(id) ON DELETE CASCADE,
    CONSTRAINT fk_reqeq_eq FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE CASCADE,
    UNIQUE KEY uq_req_equipment (requirement_id, equipment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS work_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    freelancer_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    agreed_price DECIMAL(10, 2),
    cancellation_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wreq_req FOREIGN KEY (requirement_id) REFERENCES work_requirements(id) ON DELETE CASCADE,
    CONSTRAINT fk_wreq_freelancer FOREIGN KEY (freelancer_id) REFERENCES freelancers(id) ON DELETE CASCADE,
    UNIQUE KEY uq_req_freelancer (requirement_id, freelancer_id),
    INDEX idx_wreq_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 7. Messaging Domain
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    studio_id BIGINT NOT NULL,
    freelancer_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conv_req FOREIGN KEY (requirement_id) REFERENCES work_requirements(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_studio FOREIGN KEY (studio_id) REFERENCES studios(id) ON DELETE CASCADE,
    CONSTRAINT fk_conv_freelancer FOREIGN KEY (freelancer_id) REFERENCES freelancers(id) ON DELETE CASCADE,
    UNIQUE KEY uq_conv_req_fl (requirement_id, freelancer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_msg_conv FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_msg_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_msg_conv_time (conversation_id, sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 8. Ratings & Reviews Domain
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    target_type ENUM('STUDIO', 'FREELANCER') NOT NULL,
    score INT NOT NULL,
    review_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rating_req FOREIGN KEY (requirement_id) REFERENCES work_requirements(id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_from FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_to FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_rating_score CHECK (score >= 1 AND score <= 5),
    UNIQUE KEY uq_rating_req_user (requirement_id, from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 9. Notifications Domain
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    related_entity_id BIGINT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 10. Initial Seed Data (Predefined Catalogues)
-- ------------------------------------------------------------------------------
-- Predefined Skills
INSERT INTO skills (name, is_custom) VALUES
('Portrait Photography', FALSE),
('Wedding Photography', FALSE),
('Candid Photography', FALSE),
('Event Cinematography', FALSE),
('Drone Cinematography', FALSE),
('Color Grading', FALSE),
('Photo Retouching & Editing', FALSE),
('Studio Lighting Setup', FALSE),
('Gimbal & Camera Movement', FALSE),
('Sound Recording & Sync', FALSE)
ON DUPLICATE KEY UPDATE name=name;

-- Predefined Services
INSERT INTO services (name, is_custom) VALUES
('Wedding Photography & Film', FALSE),
('Pre-Wedding & Engagement Shoot', FALSE),
('Traditional Video Coverage', FALSE),
('Candid Wedding Photography', FALSE),
('Corporate & Commercial Shoot', FALSE),
('Fashion & Model Portfolio', FALSE),
('Maternity & Baby Shoot', FALSE),
('Product Photography', FALSE),
('Post-Production Video Editing', FALSE)
ON DUPLICATE KEY UPDATE name=name;

-- Predefined Equipment Categories
INSERT INTO equipment_categories (name) VALUES
('Camera Body'),
('Lenses'),
('Lighting & Modifiers'),
('Audio & Mics'),
('Stabilization & Rigging'),
('Drones & Aerial'),
('Editing Hardware & Software'),
('Other Accessories')
ON DUPLICATE KEY UPDATE name=name;

-- Predefined Equipment
INSERT INTO equipment (category_id, name, is_custom) VALUES
(1, 'Sony Alpha A7 IV', FALSE),
(1, 'Sony FX3 Cinema Line', FALSE),
(1, 'Canon EOS R5', FALSE),
(1, 'Canon EOS R6 Mark II', FALSE),
(1, 'Nikon Z8', FALSE),
(2, 'Sony FE 24-70mm f/2.8 GM II', FALSE),
(2, 'Sony FE 70-200mm f/2.8 GM OSS II', FALSE),
(2, 'Canon RF 50mm f/1.2 L USM', FALSE),
(2, 'Sigma 85mm f/1.4 DG DN Art', FALSE),
(3, 'Godox AD600 Pro Strobe', FALSE),
(3, 'Godox V1 Flash', FALSE),
(3, 'Aputure Light Storm 300d II', FALSE),
(4, 'Rode Wireless PRO Mic', FALSE),
(4, 'Sennheiser MKE 600 Shotgun', FALSE),
(5, 'DJI RS 3 Pro Gimbal', FALSE),
(6, 'DJI Mavic 3 Pro Drone', FALSE),
(7, 'Apple MacBook Pro M3 Max (DaVinci/Premiere/Lightroom)', FALSE)
ON DUPLICATE KEY UPDATE name=name;
