package com.example.backendprojet.controller;

import com.example.backendprojet.dto.CreateUserRequest;
import com.example.backendprojet.services.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private KeycloakService keycloakService;

    // ───────── Créer utilisateur ─────────
    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        try {
            keycloakService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getRoles() // ✅ CORRIGÉ
            );
            return ResponseEntity.ok("Utilisateur créé avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    // ───────── Récupérer tous les utilisateurs ─────────
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(keycloakService.getAllUsersWithRoles());
    }

    // ───────── Récupérer utilisateur ─────────
    @GetMapping("/{username}")
    public ResponseEntity<?> getUser(@PathVariable String username) {
        try {
            return ResponseEntity.ok(keycloakService.getUserByUsername(username));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable : " + username);
        }
    }

    // ───────── UPDATE USER + ROLES ─────────
    @PutMapping("/update/{username}")
    public ResponseEntity<String> updateUser(
            @PathVariable String username,
            @RequestBody CreateUserRequest request) {

        try {
            // 🔹 update infos
            keycloakService.updateUser(
                    username,
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );

            // 🔹 update roles
            keycloakService.updateUserRoles(
                    username,
                    request.getRoles() // ✅ CORRIGÉ
            );

            return ResponseEntity.ok("Utilisateur modifié avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable : " + username);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur : " + e.getMessage());
        }
    }

    // ───────── DELETE USER ─────────
    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        try {
            keycloakService.deleteUser(username);
            return ResponseEntity.ok("Utilisateur supprimé !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable : " + username);
        }
    }

    // ───────── ACTIVER / DÉSACTIVER ─────────
    @PutMapping("/status/{username}")
    public ResponseEntity<String> toggleUserStatus(
            @PathVariable String username,
            @RequestParam boolean enabled) {

        try {
            keycloakService.toggleUserStatus(username, enabled);
            return ResponseEntity.ok("Statut mis à jour !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable : " + username);
        }
    }
}