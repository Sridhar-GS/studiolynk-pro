package com.studiolynk.model.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FreelancerSearchResponseDto {

    private int totalResults;
    private LocalDate searchedDate;
    private String searchedTime;
    private String searchedLocation;
    private List<FreelancerCardDto> freelancers = new ArrayList<>();

    public FreelancerSearchResponseDto() {
    }

    public FreelancerSearchResponseDto(int totalResults, LocalDate searchedDate, String searchedTime,
                                       String searchedLocation, List<FreelancerCardDto> freelancers) {
        this.totalResults = totalResults;
        this.searchedDate = searchedDate;
        this.searchedTime = searchedTime;
        this.searchedLocation = searchedLocation;
        this.freelancers = freelancers != null ? freelancers : new ArrayList<>();
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public LocalDate getSearchedDate() {
        return searchedDate;
    }

    public void setSearchedDate(LocalDate searchedDate) {
        this.searchedDate = searchedDate;
    }

    public String getSearchedTime() {
        return searchedTime;
    }

    public void setSearchedTime(String searchedTime) {
        this.searchedTime = searchedTime;
    }

    public String getSearchedLocation() {
        return searchedLocation;
    }

    public void setSearchedLocation(String searchedLocation) {
        this.searchedLocation = searchedLocation;
    }

    public List<FreelancerCardDto> getFreelancers() {
        return freelancers;
    }

    public void setFreelancers(List<FreelancerCardDto> freelancers) {
        this.freelancers = freelancers;
    }
}
