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
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private KeycloakService keycloakService;

    // 🔹 Créer utilisateur
    public User createUser(CreateUserRequest request) {

        keycloakService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getRoles()
        );

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // 🔥 Gestion MULTI-ROLES
        List<Role> roles = request.getRoles().stream().map(roleName -> {

            return roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName(roleName);
                        return roleRepository.save(newRole);
                    });

        }).collect(Collectors.toList());

        user.setRoles(roles); // ⚠️ nécessite List<Role> dans User

        return userRepository.save(user);
    }

    // 🔹 Récupérer tous les users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 🔹 UPDATE KEYCLOAK
    public void updateUserKeycloak(String username, CreateUserRequest request) {
        try {
            keycloakService.updateUser(
                    username,
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName()
            );
        } catch (RuntimeException e) {
            throw new RuntimeException("Utilisateur introuvable dans Keycloak");
        }
    }

    // 🔹 DELETE
    public void deleteUser(String username) {
        try {
            keycloakService.deleteUser(username);
        } catch (RuntimeException ignored) {}

        userRepository.findByUsername(username).ifPresent(userRepository::delete);
    }

    // 🔹 STATUS
    public void updateUserStatus(String username, boolean active) {

        keycloakService.toggleUserStatus(username, active);

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setActive(active);
            userRepository.save(user);
        });
    }

    // 🔹 GET USER
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔹 UPDATE ROLES
    public void updateUserRoles(String username, List<String> roles) {
        try {
            keycloakService.updateUserRoles(username, roles);
        } catch (Exception e) {
            throw new RuntimeException("Erreur mise à jour rôles Keycloak");
        }
    }
}