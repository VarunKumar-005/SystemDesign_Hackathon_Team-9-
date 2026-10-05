package com.syscrafters.salestorm.dto;

import com.syscrafters.salestorm.entity.Customer.CustomerRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDTOs {

    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        @NotBlank(message = "Full name is required")
        private String fullName;

        private CustomerRole role = CustomerRole.CUSTOMER;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public CustomerRole getRole() {
            return role;
        }

        public void setRole(CustomerRole role) {
            this.role = role;
        }
    }

    public static class AuthResponse {
        private String token;
        private Long customerId;
        private String email;
        private String fullName;
        private CustomerRole role;

        public AuthResponse() {}

        public AuthResponse(String token, Long customerId, String email, String fullName, CustomerRole role) {
            this.token = token;
            this.customerId = customerId;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public CustomerRole getRole() {
            return role;
        }

        public void setRole(CustomerRole role) {
            this.role = role;
        }
    }
}
