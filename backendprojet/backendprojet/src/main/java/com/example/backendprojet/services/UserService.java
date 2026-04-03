package com.example.backendprojet.services;

import com.example.backendprojet.dto.CreateUserRequest;
import com.example.backendprojet.model.User;
import com.example.backendprojet.model.Role;
import com.example.backendprojet.repository.UserRepository;
import com.example.backendprojet.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private KeycloakService keycloakService;

    // 🔹 Créer un utilisateur
    public User createUser(CreateUserRequest request) {
        keycloakService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getRole()
        );

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword()); // encoder pour prod
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        Role role = roleRepository.findByName(request.getRole());
        if (role == null) {
            role = new Role();
            role.setName(request.getRole());
            roleRepository.save(role);
        }
        user.setRole(role);

        return userRepository.save(user);
    }

    // 🔹 Récupérer tous les utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 🔹 Modifier un utilisateur
    // 🔹 Modifier un utilisateur
// 🔹 Mettre à jour un utilisateur uniquement dans Keycloak
    public void updateUserKeycloak(String username, CreateUserRequest request) {
        try {
            keycloakService.updateUser(
                    username,
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );
            System.out.println("Utilisateur Keycloak mis à jour : " + username);
        } catch (RuntimeException e) {
            // Gestion claire si l'utilisateur n'existe pas dans Keycloak
            System.out.println("Utilisateur introuvable dans Keycloak : " + username);
            throw new RuntimeException("Utilisateur introuvable dans Keycloak : " + username);
        }
    }
    // 🔹 Supprimer un utilisateur
    public void deleteUser(String username) {
        // 1️⃣ Supprimer de Keycloak
        try {
            keycloakService.deleteUser(username);
            System.out.println("Utilisateur supprimé Keycloak : " + username);
        } catch (RuntimeException e) {
            System.out.println("Utilisateur non trouvé dans Keycloak (ignoré) : " + username);
        }

        // 2️⃣ Supprimer de la DB
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            userRepository.delete(userOpt.get());
            System.out.println("Utilisateur supprimé DB : " + username);
        } else {
            System.out.println("Utilisateur non trouvé dans DB (ignoré) : " + username);
        }
    }
    // 🔹 Activer / Désactiver utilisateur (DB + Keycloak)
    public void updateUserStatus(String username, boolean active) {

        // 1️⃣ Keycloak (IMPORTANT 🔐)
        keycloakService.toggleUserStatus(username, active);

        // 2️⃣ DB (optionnel mais recommandé)
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(active); // ⚠️ suppose que tu as ce champ dans ton entity
            userRepository.save(user);
            System.out.println("Statut utilisateur mis à jour DB : " + username);
        } else {
            System.out.println("Utilisateur non trouvé dans DB (ignoré) : " + username);
        }
    }
}