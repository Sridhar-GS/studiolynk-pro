package com.studiolynk.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolios")
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false, unique = true)
    private Freelancer freelancer;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<PortfolioCategory> categories = new ArrayList<>();

    public Portfolio() {
    }

    public Portfolio(Freelancer freelancer) {
        this.freelancer = freelancer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Freelancer getFreelancer() {
        return freelancer;
    }

    public void setFreelancer(Freelancer freelancer) {
        this.freelancer = freelancer;
    }

    public List<PortfolioCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<PortfolioCategory> categories) {
        this.categories = categories;
    }

    public void addCategory(PortfolioCategory category) {
        categories.add(category);
        category.setPortfolio(this);
    }

    public void removeCategory(PortfolioCategory category) {
        categories.remove(category);
        category.setPortfolio(null);
    }
}
