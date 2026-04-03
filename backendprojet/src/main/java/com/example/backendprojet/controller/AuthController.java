package com.example.backendprojet.controller;


import com.example.backendprojet.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        // Ici tu peux intégrer Keycloak login ou JWT
        return ResponseEntity.ok("Login successful for user: " + request.getUsername());
    }
}