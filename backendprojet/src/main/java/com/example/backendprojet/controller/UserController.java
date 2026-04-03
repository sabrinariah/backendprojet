package com.example.backendprojet.controller;

import com.example.backendprojet.dto.CreateUserRequest;
import com.example.backendprojet.model.User;
import com.example.backendprojet.services.KeycloakService;
import com.example.backendprojet.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.ws.rs.BadRequestException; // ✅ Jakarta WS
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {

    private final KeycloakService keycloakService;
    private final UserService userService;

    @Autowired
    public UserController(KeycloakService keycloakService, UserService userService) {
        this.keycloakService = keycloakService;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addUser(@RequestBody UserRequest userRequest) {
        boolean keycloakCreated = false;
        try {
            keycloakService.createUser(
                    userRequest.getUsername(),
                    userRequest.getEmail(),
                    userRequest.getFirstName(),
                    userRequest.getLastName(),
                    userRequest.getRole()
            );
            keycloakCreated = true;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("409")) {
                System.out.println("Utilisateur déjà existant dans Keycloak : " + userRequest.getUsername());
            } else {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur Keycloak création utilisateur : " + e.getMessage());
            }
        }

        try {
            CreateUserRequest createUserRequest = new CreateUserRequest();
            createUserRequest.setUsername(userRequest.getUsername());
            createUserRequest.setPassword(userRequest.getPassword());
            createUserRequest.setEmail(userRequest.getEmail());
            createUserRequest.setFirstName(userRequest.getFirstName());
            createUserRequest.setLastName(userRequest.getLastName());
            createUserRequest.setRole(userRequest.getRole());

            userService.createUser(createUserRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur DB création utilisateur : " + e.getMessage());
        }

        return keycloakCreated
                ? ResponseEntity.ok("Utilisateur créé avec succès dans Keycloak et DB !")
                : ResponseEntity.ok("Utilisateur déjà existant dans Keycloak mais créé dans DB avec succès !");
    }

    @GetMapping("/keycloak")
    public ResponseEntity<?> getKeycloakUsers() {
        try {
            List<Map<String, Object>> users = keycloakService.getAllUsersWithRoles();
            return ResponseEntity.ok(users);
        } catch (BadRequestException bre) {
            String msg = bre.getCause() != null ? bre.getCause().getMessage() : bre.toString();
            System.err.println("Erreur Keycloak 400 Bad Request : " + msg);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur Keycloak 400 Bad Request : " + msg);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur récupération utilisateurs Keycloak : " + e.getMessage());
        }
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/update/{username}")
    public ResponseEntity<String> updateUser(@PathVariable String username, @RequestBody CreateUserRequest request) {
        try {
            keycloakService.updateUser(
                    username,
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );
            userService.updateUserKeycloak(username, request);
            return ResponseEntity.ok("Utilisateur modifié avec succès !");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur introuvable : " + username);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable String username) {
        try {
            keycloakService.deleteUser(username);
            userService.deleteUser(username);
            return ResponseEntity.ok("Utilisateur supprimé dans Keycloak et DB !");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur suppression : " + e.getMessage());
        }
    }

    public static class UserRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    @GetMapping("/check-user/{username}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable String username) {
        try {
            List<Map<String, Object>> users = keycloakService.getAllUsersWithRoles();
            boolean exists = users.stream().anyMatch(u -> username.equals(u.get("username")));
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @GetMapping("/existing-users")
    public ResponseEntity<List<String>> getExistingUsers() {
        try {
            List<Map<String, Object>> users = keycloakService.getAllUsersWithRoles();
            List<String> existingUsernames = users.stream()
                    .map(u -> (String) u.get("username"))
                    .toList();
            System.out.println("Utilisateurs existants dans Keycloak : " + existingUsernames);
            return ResponseEntity.ok(existingUsernames);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    @PatchMapping("/toggle/{username}")
    public ResponseEntity<Map<String, Object>> toggleUser(@PathVariable String username, @RequestBody Map<String, Boolean> body) {
        boolean active = body.get("active");
        keycloakService.toggleUserStatus(username, active);
        userService.updateUserStatus(username, active);

        Map<String, Object> response = Map.of(
                "username", username,
                "active", active,
                "message", "Statut utilisateur mis à jour !"
        );
        return ResponseEntity.ok(response);
    }
}