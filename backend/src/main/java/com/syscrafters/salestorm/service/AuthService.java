package com.syscrafters.salestorm.service;

import com.syscrafters.salestorm.dto.AuthDTOs.*;
import com.syscrafters.salestorm.entity.Customer;
import com.syscrafters.salestorm.entity.Customer.CustomerRole;
import com.syscrafters.salestorm.exception.BusinessException;
import com.syscrafters.salestorm.repository.CustomerRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final SecretKey secretKey;
    private final long expirationMs;

    public AuthService(CustomerRepository customerRepository,
                       @Value("${salestorm.jwt.secret}") String secret,
                       @Value("${salestorm.jwt.expiration-ms}") long expirationMs) {
        this.customerRepository = customerRepository;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public AuthResponse login(LoginRequest req) {
        Customer customer = customerRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        // Prototype password check (prefixed or simple match for demo)
        if (!customer.getPassword().equals(req.getPassword())) {
            throw new BusinessException("Invalid email or password");
        }

        String token = generateToken(customer);
        return new AuthResponse(token, customer.getId(), customer.getEmail(), customer.getFullName(), customer.getRole());
    }

    public AuthResponse register(RegisterRequest req) {
        if (customerRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already registered: " + req.getEmail());
        }

        Customer customer = new Customer(
                req.getEmail(),
                req.getPassword(),
                req.getFullName(),
                req.getRole() != null ? req.getRole() : CustomerRole.CUSTOMER
        );
        customer = customerRepository.save(customer);

        String token = generateToken(customer);
        return new AuthResponse(token, customer.getId(), customer.getEmail(), customer.getFullName(), customer.getRole());
    }

    public String generateToken(Customer customer) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(customer.getEmail())
                .claim("id", customer.getId())
                .claim("role", customer.getRole().name())
                .claim("name", customer.getFullName())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
