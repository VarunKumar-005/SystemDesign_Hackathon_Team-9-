package com.syscrafters.salestorm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDetailDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal salePrice;
    private String imageUrl;
    private boolean flashSaleActive;
    private int reservationDurationSeconds;
    private int availableStock;
    private int totalStock;
    private int reservedStock;
    private int soldStock;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;

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

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public int getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(int totalStock) {
        this.totalStock = totalStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }

    public void setReservedStock(int reservedStock) {
        this.reservedStock = reservedStock;
    }

    public int getSoldStock() {
        return soldStock;
    }

    public void setSoldStock(int soldStock) {
        this.soldStock = soldStock;
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
}
