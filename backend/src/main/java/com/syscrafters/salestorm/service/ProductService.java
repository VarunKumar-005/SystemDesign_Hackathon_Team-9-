package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.ProductDetailDTO;
import com.syscrafters.salestorm.entity.Inventory;
import com.syscrafters.salestorm.entity.Product;
import com.syscrafters.salestorm.exception.BusinessException;
import com.syscrafters.salestorm.repository.InventoryRepository;
import com.syscrafters.salestorm.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ProductService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDetailDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDetailDTO> getFlashSaleProducts() {
        return productRepository.findByFlashSaleActiveTrue().stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found with ID: " + productId));
        return toDetailDTO(product);
    }

    private ProductDetailDTO toDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setFlashSaleActive(product.isFlashSaleActive());
        dto.setReservationDurationSeconds(product.getReservationDurationSeconds());
        dto.setSaleStartTime(product.getSaleStartTime());
        dto.setSaleEndTime(product.getSaleEndTime());

        inventoryRepository.findByProductId(product.getId()).ifPresentOrElse(inv -> {
            dto.setAvailableStock(inv.getAvailableQuantity());
            dto.setTotalStock(inv.getTotalQuantity());
            dto.setReservedStock(inv.getReservedQuantity());
            dto.setSoldStock(inv.getSoldQuantity());
        }, () -> {
            dto.setAvailableStock(0);
            dto.setTotalStock(0);
            dto.setReservedStock(0);
            dto.setSoldStock(0);
        });

        return dto;
    }
}
