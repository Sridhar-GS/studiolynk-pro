package com.studiolynk.service.impl;

import com.studiolynk.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for dispatching transactional emails such as Password Reset OTPs via SMTP.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        log.info("Preparing Password Reset OTP for [{}]. OTP: [{}] (Logged for local dev/testing verification)", toEmail, otp);

        if (fromEmail == null || fromEmail.isBlank()) {
            log.info("SMTP username is not configured. Email dispatch skipped in local dev mode. OTP is [{}]", otp);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("StudioLynk — Password Reset OTP");
            message.setText("Hello,\n\nYour 6-digit password reset code is: " + otp +
                    "\n\nThis code will expire in 5 minutes. If you did not request this, please ignore this email.\n\nBest regards,\nStudioLynk Team");

            mailSender.send(message);
            log.info("Successfully dispatched Password Reset OTP to [{}]", toEmail);
        } catch (Exception e) {
            log.warn("Failed to send email via SMTP to [{}]: {}. Dev fallback OTP is [{}]", toEmail, e.getMessage(), otp);
        }
    }
}
