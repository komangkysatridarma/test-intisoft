package com.example.test_intisoft.model;

public record UserDTO(Long id, String fullname, String username, String email, String password, Role role) {
}