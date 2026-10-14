package com.studiolynk.model.dto.admin;

import java.util.List;

/**
 * Aggregated platform metrics and health indicators for Admin Dashboard (ADM-001, ADM-003, ADM-005).
 */
public class AdminDashboardMetricsDto {

    private long totalStudios;
    private long totalFreelancers;
    private long totalUsers;
    private long activeUsers;
    private long totalRequirements;
    private long openRequirements;
    private long confirmedRequirements;
    private long completedRequirements;
    private long totalRequests;
    private long pendingRequests;
    private long acceptedRequests;
    private long confirmedRequests;
    private long totalRatings;
    private double averagePlatformRating;
    private long totalIdentitySubmissions;
    private List<AdminStudioIdentityDto> recentIdentitySubmissions;
    private List<AdminActivityItemDto> recentActivity;

    public AdminDashboardMetricsDto() {
    }

    public long getTotalStudios() {
        return totalStudios;
    }

    public void setTotalStudios(long totalStudios) {
        this.totalStudios = totalStudios;
    }

    public long getTotalFreelancers() {
        return totalFreelancers;
    }

    public void setTotalFreelancers(long totalFreelancers) {
        this.totalFreelancers = totalFreelancers;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getTotalRequirements() {
        return totalRequirements;
    }

    public void setTotalRequirements(long totalRequirements) {
        this.totalRequirements = totalRequirements;
    }

    public long getOpenRequirements() {
        return openRequirements;
    }

    public void setOpenRequirements(long openRequirements) {
        this.openRequirements = openRequirements;
    }

    public long getConfirmedRequirements() {
        return confirmedRequirements;
    }

    public void setConfirmedRequirements(long confirmedRequirements) {
        this.confirmedRequirements = confirmedRequirements;
    }

    public long getCompletedRequirements() {
        return completedRequirements;
    }

    public void setCompletedRequirements(long completedRequirements) {
        this.completedRequirements = completedRequirements;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getConfirmedRequests() {
        return confirmedRequests;
    }

    public void setConfirmedRequests(long confirmedRequests) {
        this.confirmedRequests = confirmedRequests;
    }

    public long getAcceptedRequests() {
        return acceptedRequests;
    }

    public void setAcceptedRequests(long acceptedRequests) {
        this.acceptedRequests = acceptedRequests;
    }

    public long getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(long totalRatings) {
        this.totalRatings = totalRatings;
    }

    public double getAveragePlatformRating() {
        return averagePlatformRating;
    }

    public void setAveragePlatformRating(double averagePlatformRating) {
        this.averagePlatformRating = averagePlatformRating;
    }

    public long getTotalIdentitySubmissions() {
        return totalIdentitySubmissions;
    }

    public void setTotalIdentitySubmissions(long totalIdentitySubmissions) {
        this.totalIdentitySubmissions = totalIdentitySubmissions;
    }

    public List<AdminStudioIdentityDto> getRecentIdentitySubmissions() {
        return recentIdentitySubmissions;
    }

    public void setRecentIdentitySubmissions(List<AdminStudioIdentityDto> recentIdentitySubmissions) {
        this.recentIdentitySubmissions = recentIdentitySubmissions;
    }

    public List<AdminActivityItemDto> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<AdminActivityItemDto> recentActivity) {
        this.recentActivity = recentActivity;
    }
}
