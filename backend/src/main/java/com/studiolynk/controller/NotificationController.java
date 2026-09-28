package com.studiolynk.controller;

import com.studiolynk.model.dto.ApiResponse;
import com.studiolynk.model.dto.NotificationDto;
import com.studiolynk.model.dto.UnreadCountDto;
import com.studiolynk.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notifications management (NOT-001 - NOT-004)")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieve list of in-app notifications with optional unread filter")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly
    ) {
        List<NotificationDto> notifications = notificationService.getUserNotifications(userDetails.getUsername(), unreadOnly);
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread count", description = "Get number of unread notifications for badge counters")
    public ResponseEntity<ApiResponse<UnreadCountDto>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        long count = notificationService.getUnreadCount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Unread count retrieved", new UnreadCountDto(count)));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read (POST)", description = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsReadPost(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        NotificationDto dto = notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", dto));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read (PATCH)", description = "Mark a single notification as read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsReadPatch(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id
    ) {
        NotificationDto dto = notificationService.markAsRead(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", dto));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all as read (POST)", description = "Mark all user notifications as read (NOT-004)")
    public ResponseEntity<ApiResponse<Void>> markAllAsReadPost(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all as read (PATCH)", description = "Mark all user notifications as read (NOT-004)")
    public ResponseEntity<ApiResponse<Void>> markAllAsReadPatch(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        notificationService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
