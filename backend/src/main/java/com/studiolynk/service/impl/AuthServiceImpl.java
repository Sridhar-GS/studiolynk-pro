package com.studiolynk.service.impl;

import com.studiolynk.exception.BadRequestException;
import com.studiolynk.exception.DuplicateResourceException;
import com.studiolynk.exception.ResourceNotFoundException;
import com.studiolynk.model.dto.AuthResponseDto;
import com.studiolynk.model.dto.LoginRequestDto;
import com.studiolynk.model.dto.RegisterRequestDto;
import com.studiolynk.model.dto.UserSummaryDto;
import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.UserRepository;
import com.studiolynk.security.JwtTokenProvider;
import com.studiolynk.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user registration, authentication, JWT issuance, and session state.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with email '" + email + "' already exists.");
        }

        if (request.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Public registration for ADMIN role is not permitted.");
        }

        User user = new User(
                email,
                passwordEncoder.encode(request.getPassword()),
                request.getRole()
        );
        user.setOnboardingCompleted(false);

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser);

        UserSummaryDto userSummary = mapToSummary(savedUser);
        return new AuthResponseDto(token, userSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = tokenProvider.generateToken(user);
        UserSummaryDto userSummary = mapToSummary(user);

        return new AuthResponseDto(token, userSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToSummary(user);
    }

    private UserSummaryDto mapToSummary(User user) {
        return new UserSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isOnboardingCompleted()
        );
    }
}
