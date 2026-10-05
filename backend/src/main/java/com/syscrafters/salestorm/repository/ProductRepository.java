package com.syscrafters.salestorm.repository;

import com.syscrafters.salestorm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByFlashSaleActiveTrue();
}
