package com.example.backendprojet.controller;


import com.example.backendprojet.services.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private KeycloakService keycloakService;

    // ────── Records inline (pas besoin de classe séparée) ──────
    record RegisterRequest(
            String username,
            String email,
            String firstName,
            String lastName,
            String password,
            List<String> roles
    ) {}

    record ForgotPasswordRequest(String email) {}

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            String userId = keycloakService.registerUser(
                    req.username(),
                    req.email(),
                    req.firstName(),
                    req.lastName(),
                    req.password(),
                    req.roles()
            );
            return ResponseEntity.ok(Map.of(
                    "message", "Compte créé avec succès",
                    "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // ✅ FORGOT PASSWORD
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest req) {
        try {
            keycloakService.sendResetPasswordEmail(req.email());
        } catch (Exception ignored) {
            // Ne pas révéler si l'email existe
        }
        return ResponseEntity.ok(Map.of(
                "message", "Si cet email existe, un lien vous a été envoyé"
        ));
    }

    // ✅ GET ALL USERS
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(keycloakService.getAllUsersWithRoles());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ GET USER BY USERNAME
    @GetMapping("/users/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        try {
            return ResponseEntity.ok(keycloakService.getUserByUsername(username));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ UPDATE USER
    @PutMapping("/users/{username}")
    public ResponseEntity<?> updateUser(
            @PathVariable String username,
            @RequestBody Map<String, String> body) {
        try {
            keycloakService.updateUser(
                    username,
                    body.get("email"),
                    body.get("firstName"),
                    body.get("lastName")
            );
            return ResponseEntity.ok(Map.of("message", "Utilisateur mis à jour"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ UPDATE ROLES
    @PutMapping("/users/{username}/roles")
    public ResponseEntity<?> updateRoles(
            @PathVariable String username,
            @RequestBody Map<String, List<String>> body) {
        try {
            keycloakService.updateUserRoles(username, body.get("roles"));
            return ResponseEntity.ok(Map.of("message", "Rôles mis à jour"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ TOGGLE ACTIVE/INACTIVE
    @PatchMapping("/users/{username}/status")
    public ResponseEntity<?> toggleStatus(
            @PathVariable String username,
            @RequestBody Map<String, Boolean> body) {
        try {
            keycloakService.toggleUserStatus(username, body.get("enabled"));
            return ResponseEntity.ok(Map.of("message", "Statut mis à jour"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ DELETE USER
    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            keycloakService.deleteUser(username);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}