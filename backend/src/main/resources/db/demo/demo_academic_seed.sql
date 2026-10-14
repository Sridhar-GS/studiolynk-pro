-- ==============================================================================
-- StudioLynk Academic Prototype Demo Data Seeding Script (Phase 19)
-- Passwords for all demo accounts: Demo@2026!
-- BCrypt Hash: $2a$10$m39Useehdgy2en6uFtazle0d.LBhNdG9u5OOb3E4nkanNbDMIoixW
-- ==============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Create Demo Users
-- Admin is already seeded (admin@studiolynk.com / Admin@2026!)
INSERT INTO users (id, email, password_hash, role, onboarding_completed, created_at, updated_at) VALUES
(101, 'studio.lumiere@studiolynk.com', '$2a$10$m39Useehdgy2en6uFtazle0d.LBhNdG9u5OOb3E4nkanNbDMIoixW', 'STUDIO', TRUE, NOW(), NOW()),
(102, 'studio.monochrome@studiolynk.com', '$2a$10$m39Useehdgy2en6uFtazle0d.LBhNdG9u5OOb3E4nkanNbDMIoixW', 'STUDIO', TRUE, NOW(), NOW()),
(201, 'karthik.lens@studiolynk.com', '$2a$10$m39Useehdgy2en6uFtazle0d.LBhNdG9u5OOb3E4nkanNbDMIoixW', 'FREELANCER', TRUE, NOW(), NOW()),
(202, 'ananya.visuals@studiolynk.com', '$2a$10$m39Useehdgy2en6uFtazle0d.LBhNdG9u5OOb3E4nkanNbDMIoixW', 'FREELANCER', TRUE, NOW(), NOW()),
(203, 'vignesh.cinema@studiolynk.com', '$2a$10$m39Useehdgy2en6uFtazle0d.LBhNdG9u5OOb3E4nkanNbDMIoixW', 'FREELANCER', TRUE, NOW(), NOW()),
(204, 'deepak.shoots@studiolynk.com', '$2a$10$m39Useehdgy2en6uFtazle0d.LBhNdG9u5OOb3E4nkanNbDMIoixW', 'FREELANCER', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 2. Create Demo Studios
INSERT INTO studios (id, user_id, studio_name, owner_name, logo_url, phone, address, latitude, longitude, years_of_operation, created_at, updated_at) VALUES
(1, 101, 'Lumière Wedding Studios', 'Rajesh Kannan', 'https://images.unsplash.com/photo-1542038784456-1ea8e935640e?w=500&auto=format&fit=crop&q=80', '+91 98401 23456', 'No. 42, G.N. Chetty Road, T. Nagar, Chennai, Tamil Nadu 600017', 13.04180000, 80.23410000, 8, NOW(), NOW()),
(2, 102, 'Monochrome Creative Hub', 'Priya Sundaram', 'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500&auto=format&fit=crop&q=80', '+91 98801 87654', '100 Feet Road, HAL 2nd Stage, Indiranagar, Bangalore, Karnataka 560038', 12.97840000, 77.64080000, 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO studio_identity_submissions (studio_id, document_type, document_url, declaration_text, status, submitted_at) VALUES
(1, 'STUDIO_REGISTRATION_CERTIFICATE', 'https://studiolynk-bucket-2026.s3.ap-south-1.amazonaws.com/studios/1/documents/lumiere_reg_cert.pdf', 'I hereby declare that Lumière Wedding Studios is a lawfully operating enterprise.', 'VERIFIED', NOW()),
(2, 'BUSINESS_GST_CERTIFICATE', 'https://studiolynk-bucket-2026.s3.ap-south-1.amazonaws.com/studios/2/documents/monochrome_gst.pdf', 'I hereby declare that Monochrome Creative Hub is a registered photography business.', 'VERIFIED', NOW());

INSERT INTO studio_social_links (studio_id, platform_name, url) VALUES
(1, 'Instagram', 'https://instagram.com/lumiere_weddings_chennai'),
(1, 'Website', 'https://lumierestudios.example.com'),
(2, 'Instagram', 'https://instagram.com/monochrome_blr'),
(2, 'Vimeo', 'https://vimeo.com/monochromehub');

-- 3. Create Demo Freelancers
INSERT INTO freelancers (id, user_id, full_name, profile_photo_url, phone, address, latitude, longitude, experience_years, bio, full_day_rate, half_day_rate, created_at, updated_at) VALUES
(1, 201, 'Karthik Subramanian', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&auto=format&fit=crop&q=80', '+91 97909 11223', 'Mylapore, Chennai, Tamil Nadu 600004', 13.03340000, 80.26780000, 7, 'Award-winning candid wedding and drone photographer with 7+ years capturing South Indian luxury weddings, beachside pre-wedding stories, and cultural ceremonies.', 18000.00, 10000.00, NOW(), NOW()),
(2, 202, 'Ananya Sharma', 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=80', '+91 98412 33445', 'Besant Nagar, Chennai, Tamil Nadu 600090', 13.00020000, 80.26670000, 4, 'Specialist in high-fashion editorial portraits, baby & maternity photography, and creative outdoor portfolio shoots using natural light and medium-format optics.', 14000.00, 8000.00, NOW(), NOW()),
(3, 203, 'Vignesh Raj', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&auto=format&fit=crop&q=80', '+91 99620 55667', 'Anna Nagar, Chennai, Tamil Nadu 600040', 13.08500000, 80.21000000, 6, 'Cinematographer and colorist specialized in Sony FX3/FX6 rigs, Ronin gimbal stabilization, 4K HDR wedding teasers, and commercial brand documentaries.', 22000.00, 12000.00, NOW(), NOW()),
(4, 204, 'Deepak Nambiar', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500&auto=format&fit=crop&q=80', '+91 98860 77889', 'Koramangala, Bangalore, Karnataka 560034', 12.93520000, 77.62450000, 5, 'Destination wedding and architectural photographer with expertise in wide-angle perspectives, luxury venues, and dynamic sunset couple shoots.', 20000.00, 11000.00, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Skills Mapping
INSERT IGNORE INTO freelancer_skills (freelancer_id, skill_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(2, 1), (2, 5), (2, 7),
(3, 2), (3, 4), (3, 6),
(4, 1), (4, 2), (4, 8);

-- Services Mapping
INSERT IGNORE INTO freelancer_services (freelancer_id, service_id) VALUES
(1, 1), (1, 2), (1, 4),
(2, 1), (2, 2),
(3, 3), (3, 4), (3, 8),
(4, 1), (4, 2);

-- Equipment Mapping
INSERT IGNORE INTO freelancer_equipment (freelancer_id, equipment_id) VALUES
(1, 1), (1, 5), (1, 6), (1, 12),
(2, 2), (2, 7), (2, 14),
(3, 4), (3, 5), (3, 10), (3, 13),
(4, 3), (4, 6), (4, 8);

-- 4. Portfolios & Images
INSERT INTO portfolios (id, freelancer_id, created_at, updated_at) VALUES
(1, 1, NOW(), NOW()),
(2, 2, NOW(), NOW()),
(3, 3, NOW(), NOW()),
(4, 4, NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

INSERT INTO portfolio_categories (id, portfolio_id, name, sort_order, created_at) VALUES
(1, 1, 'Traditional Weddings', 0, NOW()),
(2, 1, 'Candid Portraits', 1, NOW()),
(3, 1, 'Drone Highlights', 2, NOW()),
(4, 2, 'Fashion Editorials', 0, NOW()),
(5, 2, 'Baby & Maternity', 1, NOW()),
(6, 3, 'Wedding Cinema Teasers', 0, NOW()),
(7, 3, 'Brand Documentaries', 1, NOW()),
(8, 4, 'Destination Pre-Weddings', 0, NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO portfolio_images (category_id, s3_key, image_url, original_filename, content_type, file_size, sort_order, created_at) VALUES
(1, 'freelancers/1/portfolio/1_traditional_weddings/wedding_muhurtham.jpg', 'https://images.unsplash.com/photo-1519741497674-611481863552?w=800&auto=format&fit=crop&q=80', 'wedding_muhurtham.jpg', 'image/jpeg', 2450000, 0, NOW()),
(1, 'freelancers/1/portfolio/1_traditional_weddings/mandap_rituals.jpg', 'https://images.unsplash.com/photo-1606800052052-a08af7148866?w=800&auto=format&fit=crop&q=80', 'mandap_rituals.jpg', 'image/jpeg', 3120000, 1, NOW()),
(2, 'freelancers/1/portfolio/2_candid_portraits/bride_golden_hour.jpg', 'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?w=800&auto=format&fit=crop&q=80', 'bride_golden_hour.jpg', 'image/jpeg', 1980000, 0, NOW()),
(3, 'freelancers/1/portfolio/3_drone_highlights/temple_aerial.jpg', 'https://images.unsplash.com/photo-1473968512647-3e447244af8f?w=800&auto=format&fit=crop&q=80', 'temple_aerial.jpg', 'image/jpeg', 4200000, 0, NOW()),
(4, 'freelancers/2/portfolio/4_fashion_editorials/runway_look.jpg', 'https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=800&auto=format&fit=crop&q=80', 'runway_look.jpg', 'image/jpeg', 2800000, 0, NOW()),
(6, 'freelancers/3/portfolio/6_wedding_cinema_teasers/teaser_frame.jpg', 'https://images.unsplash.com/photo-1492691527719-9d1e07e534b4?w=800&auto=format&fit=crop&q=80', 'teaser_frame.jpg', 'image/jpeg', 3500000, 0, NOW()),
(8, 'freelancers/4/portfolio/8_destination_pre_weddings/lake_palace_sunset.jpg', 'https://images.unsplash.com/photo-1465495976277-4387d4b0b4c6?w=800&auto=format&fit=crop&q=80', 'lake_palace_sunset.jpg', 'image/jpeg', 2900000, 0, NOW());

-- 5. Rolling 10-Day Availability (from CURDATE() to CURDATE() + 9 days)
DELETE FROM freelancer_availability WHERE freelancer_id IN (1, 2, 3, 4);

-- Freelancer 1 (Karthik): Available most days, Busy on Day 2 (Confirmed Shoot)
INSERT INTO freelancer_availability (freelancer_id, available_date, status, start_time, end_time) VALUES
(1, CURDATE() + INTERVAL 0 DAY, 'AVAILABLE', '08:00:00', '21:00:00'),
(1, CURDATE() + INTERVAL 1 DAY, 'AVAILABLE', '08:00:00', '21:00:00'),
(1, CURDATE() + INTERVAL 2 DAY, 'BUSY', '08:00:00', '22:00:00'), -- Shoot Confirmed
(1, CURDATE() + INTERVAL 3 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(1, CURDATE() + INTERVAL 4 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(1, CURDATE() + INTERVAL 5 DAY, 'AVAILABLE', '09:00:00', '19:00:00'),
(1, CURDATE() + INTERVAL 6 DAY, 'AVAILABLE', '08:00:00', '21:00:00'),
(1, CURDATE() + INTERVAL 7 DAY, 'NOT_SET', NULL, NULL),
(1, CURDATE() + INTERVAL 8 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(1, CURDATE() + INTERVAL 9 DAY, 'AVAILABLE', '08:00:00', '20:00:00');

-- Freelancer 2 (Ananya): Available on Days 0, 1, 3, 4, 6
INSERT INTO freelancer_availability (freelancer_id, available_date, status, start_time, end_time) VALUES
(2, CURDATE() + INTERVAL 0 DAY, 'AVAILABLE', '09:00:00', '18:00:00'),
(2, CURDATE() + INTERVAL 1 DAY, 'AVAILABLE', '09:00:00', '18:00:00'),
(2, CURDATE() + INTERVAL 2 DAY, 'AVAILABLE', '09:00:00', '18:00:00'),
(2, CURDATE() + INTERVAL 3 DAY, 'AVAILABLE', '09:00:00', '18:00:00'),
(2, CURDATE() + INTERVAL 4 DAY, 'BUSY', '10:00:00', '18:00:00'),
(2, CURDATE() + INTERVAL 5 DAY, 'NOT_SET', NULL, NULL),
(2, CURDATE() + INTERVAL 6 DAY, 'AVAILABLE', '10:00:00', '19:00:00'),
(2, CURDATE() + INTERVAL 7 DAY, 'AVAILABLE', '10:00:00', '19:00:00'),
(2, CURDATE() + INTERVAL 8 DAY, 'AVAILABLE', '10:00:00', '19:00:00'),
(2, CURDATE() + INTERVAL 9 DAY, 'NOT_SET', NULL, NULL);

-- Freelancer 3 (Vignesh): Available on Days 1, 2, 4, 5, 8
INSERT INTO freelancer_availability (freelancer_id, available_date, status, start_time, end_time) VALUES
(3, CURDATE() + INTERVAL 0 DAY, 'BUSY', '08:00:00', '20:00:00'),
(3, CURDATE() + INTERVAL 1 DAY, 'AVAILABLE', '07:00:00', '22:00:00'),
(3, CURDATE() + INTERVAL 2 DAY, 'AVAILABLE', '07:00:00', '22:00:00'),
(3, CURDATE() + INTERVAL 3 DAY, 'BUSY', '08:00:00', '20:00:00'),
(3, CURDATE() + INTERVAL 4 DAY, 'AVAILABLE', '06:00:00', '20:00:00'),
(3, CURDATE() + INTERVAL 5 DAY, 'AVAILABLE', '07:00:00', '21:00:00'),
(3, CURDATE() + INTERVAL 6 DAY, 'NOT_SET', NULL, NULL),
(3, CURDATE() + INTERVAL 7 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(3, CURDATE() + INTERVAL 8 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(3, CURDATE() + INTERVAL 9 DAY, 'AVAILABLE', '08:00:00', '20:00:00');

-- Freelancer 4 (Deepak): Available Days 0..8 in Bangalore
INSERT INTO freelancer_availability (freelancer_id, available_date, status, start_time, end_time) VALUES
(4, CURDATE() + INTERVAL 0 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 1 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 2 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 3 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 4 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 5 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 6 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 7 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 8 DAY, 'AVAILABLE', '08:00:00', '20:00:00'),
(4, CURDATE() + INTERVAL 9 DAY, 'NOT_SET', NULL, NULL);

-- 6. Work Requirements
INSERT INTO work_requirements (id, studio_id, event_name, event_type, event_date, start_time, end_time, location, latitude, longitude, day_type, budget, description, status, event_contact_name, event_contact_phone, confirmed_freelancer_id, created_at, updated_at) VALUES
(1, 1, 'Grand Royal Wedding & Reception', 'Wedding Photography', CURDATE() + INTERVAL 2 DAY, '08:00:00', '22:00:00', 'Mayor Ramanathan Chettiar Hall, MRC Nagar, Chennai', 13.02340000, 80.27450000, 'FULL_DAY', 25000.00, 'Lead candid photographer needed for morning muhurtham and evening luxury reception. Requires full-frame body and fast zoom optics.', 'CONFIRMED', 'Sneha & Vikram (Parents: Dr. Swaminathan)', '+91 99401 11222', 1, NOW(), NOW()),
(2, 1, 'Beachside Pre-Wedding Cinematic Teaser', 'Pre-Wedding Shoot', CURDATE() + INTERVAL 4 DAY, '06:00:00', '18:00:00', 'InterContinental Chennai Mahabalipuram Resort, ECR', 12.82340000, 80.24120000, 'FULL_DAY', 20000.00, 'Creative pre-wedding teaser shoot on sunrise beach. Requires gimbal stabilizer and 4K 60fps slow-motion capabilities.', 'REQUESTED', 'Pooja & Arvind', '+91 98409 33221', NULL, NOW(), NOW()),
(3, 2, 'Autumn Luxury Fashion Lookbook', 'Fashion Photography', CURDATE() + INTERVAL 6 DAY, '10:00:00', '18:00:00', 'UB City Loft Studios, Vittal Mallya Road, Bangalore', 12.97190000, 77.59580000, 'FULL_DAY', 15000.00, 'Studio lookbook catalog shoot for indie handloom label. 6 model changes with studio strobes and beauty dish.', 'OPEN', 'Kavita Menon (Label Director)', '+91 98802 44332', NULL, NOW(), NOW()),
(4, 1, 'South Indian Temple Wedding Highlights', 'Wedding Photography', CURDATE() - INTERVAL 10 DAY, '07:00:00', '16:00:00', 'Kapaleeshwarar Temple Hall, Mylapore, Chennai', 13.03340000, 80.26780000, 'FULL_DAY', 20000.00, 'Traditional temple wedding highlights and candid family coverage.', 'COMPLETED', 'Meera & Raghu', '+91 98400 99887', 1, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY)
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- Requirement Skills & Services
INSERT IGNORE INTO work_requirement_skills (requirement_id, skill_id) VALUES
(1, 2), (1, 3), (1, 4),
(2, 2), (2, 4), (2, 6),
(3, 1), (3, 5), (3, 7),
(4, 2), (4, 3);

INSERT IGNORE INTO work_requirement_services (requirement_id, service_id) VALUES
(1, 1), (1, 4),
(2, 3), (2, 4),
(3, 1),
(4, 1);

-- 7. Work Requests
INSERT INTO work_requests (id, requirement_id, freelancer_id, status, message, agreed_price, cancellation_reason, created_at, updated_at) VALUES
(1, 1, 1, 'CONFIRMED', 'We loved your portfolio and would like you to lead wedding candid photography for this royal wedding.', 22000.00, NULL, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(2, 1, 3, 'CANCELLED', 'Hi Vignesh, we have a wedding requirement on this date.', NULL, 'Studio confirmed another candidate for this shoot.', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 1 DAY),
(3, 2, 3, 'PENDING', 'Hi Vignesh, looking for cinema teasers with your FX3 gimbal setup on ECR beach.', 20000.00, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(4, 4, 1, 'CONFIRMED', 'Lead traditional coverage at Kapaleeshwarar Temple.', 20000.00, NULL, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 11 DAY)
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 8. Messaging Conversation & Real-Time Chat
INSERT INTO conversations (id, requirement_id, studio_id, freelancer_id, created_at) VALUES
(1, 1, 1, 1, NOW() - INTERVAL 2 DAY)
ON DUPLICATE KEY UPDATE id = id;

INSERT INTO messages (conversation_id, sender_id, content, is_read, sent_at) VALUES
(1, 101, 'Hello Karthik! We saw your candid muhurtham portfolio. Are you comfortable leading the 8:00 AM call time at MRC Nagar?', TRUE, NOW() - INTERVAL 40 HOUR),
(1, 201, 'Hi Rajesh Sir! Yes absolutely. I am fully available on that date. I will bring two Sony A7 IV bodies and 24-70 GM II + 70-200 GM II lenses.', TRUE, NOW() - INTERVAL 36 HOUR),
(1, 101, 'Fantastic. Our client is requesting drone aerials of the baraat entrance as well. Can we include that for ₹22,000?', TRUE, NOW() - INTERVAL 30 HOUR),
(1, 201, 'Yes, agreed! I will bring my DJI Mini 3 Pro for the outdoor entrance. Excited to work with Lumière Studios.', TRUE, NOW() - INTERVAL 26 HOUR),
(1, 101, 'Great! I have confirmed your booking in StudioLynk. You can now view the direct client contacts on your dashboard.', TRUE, NOW() - INTERVAL 24 HOUR);

-- 9. In-App Notifications
INSERT INTO notifications (user_id, type, title, message, related_entity_id, is_read, created_at) VALUES
(201, 'STUDIO_CONFIRMATION', 'Shoot Booking Confirmed!', 'Lumière Wedding Studios confirmed you for "Grand Royal Wedding & Reception". Client contact details are now unlocked.', 1, TRUE, NOW() - INTERVAL 24 HOUR),
(101, 'REQUEST_ACCEPTED', 'Request Accepted', 'Karthik Subramanian accepted your request for "Grand Royal Wedding & Reception".', 1, TRUE, NOW() - INTERVAL 26 HOUR),
(203, 'REQUEST_RECEIVED', 'New Shoot Booking Request', 'Lumière Wedding Studios requested your availability for "Beachside Pre-Wedding Cinematic Teaser".', 2, FALSE, NOW() - INTERVAL 1 DAY);

-- 10. Completed Shoot Rating & Reviews
INSERT INTO ratings (requirement_id, from_user_id, to_user_id, target_type, score, review_text, created_at) VALUES
(4, 101, 201, 'FREELANCER', 5, 'Outstanding candid coverage! Karthik arrived punctually at the temple, managed family portraits seamlessly, and delivered breathtaking emotional shots of the muhurtham rituals. Highly recommended for lead wedding shoots.', NOW() - INTERVAL 8 DAY),
(4, 201, 101, 'STUDIO', 5, 'Working with Lumière Wedding Studios was a wonderful professional experience. Clear brief, prompt payment, and excellent on-site event coordination by Rajesh Kannan.', NOW() - INTERVAL 8 DAY)
ON DUPLICATE KEY UPDATE score = VALUES(score);

SET FOREIGN_KEY_CHECKS = 1;
