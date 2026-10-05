package com.syscrafters.salestorm.controller;

import com.syscrafters.salestorm.dto.ApiResponse;
import com.syscrafters.salestorm.dto.ProductDetailDTO;
import com.syscrafters.salestorm.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Product Service", description = "Endpoints for browsing products and flash-sale items")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products with real-time stock levels")
    public ApiResponse<List<ProductDetailDTO>> getAllProducts() {
        return ApiResponse.ok(productService.getAllProducts());
    }

    @GetMapping("/flash-sale")
    @Operation(summary = "Get active flash-sale products")
    public ApiResponse<List<ProductDetailDTO>> getFlashSaleProducts() {
        return ApiResponse.ok(productService.getFlashSaleProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details by ID")
    public ApiResponse<ProductDetailDTO> getProductById(@PathVariable("id") Long id) {
        return ApiResponse.ok(productService.getProductById(id));
    }
}
