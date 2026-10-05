package com.syscrafters.salestorm.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean flashSaleActive;

    @Column(nullable = false)
    private int reservationDurationSeconds;

    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Product() {
    }

    public Product(String name, String description, BigDecimal originalPrice, BigDecimal salePrice,
                   String imageUrl, boolean flashSaleActive, int reservationDurationSeconds) {
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.salePrice = salePrice;
        this.imageUrl = imageUrl;
        this.flashSaleActive = flashSaleActive;
        this.reservationDurationSeconds = reservationDurationSeconds;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.reservationDurationSeconds <= 0) {
            this.reservationDurationSeconds = 300; // 5 minutes
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isFlashSaleActive() {
        return flashSaleActive;
    }

    public void setFlashSaleActive(boolean flashSaleActive) {
        this.flashSaleActive = flashSaleActive;
    }

    public int getReservationDurationSeconds() {
        return reservationDurationSeconds;
    }

    public void setReservationDurationSeconds(int reservationDurationSeconds) {
        this.reservationDurationSeconds = reservationDurationSeconds;
    }

    public LocalDateTime getSaleStartTime() {
        return saleStartTime;
    }

    public void setSaleStartTime(LocalDateTime saleStartTime) {
        this.saleStartTime = saleStartTime;
    }

    public LocalDateTime getSaleEndTime() {
        return saleEndTime;
    }

    public void setSaleEndTime(LocalDateTime saleEndTime) {
        this.saleEndTime = saleEndTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
