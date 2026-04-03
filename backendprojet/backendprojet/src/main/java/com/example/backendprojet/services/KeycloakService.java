package com.example.backendprojet.services;

import com.example.backendprojet.repository.UserRepository;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakService {
    @Autowired
    private UserRepository userRepository;
    private final String serverUrl = "http://localhost:8080";
    private final String realmTarget = "projet";
    private final String clientId = "projet-client";
    private final String clientSecret = "3oJi7ahw4rDQtAi4p0jHCyAuOX58JpGH";

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realmTarget)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    // 🔹 Créer utilisateur Keycloak
    public String createUser(String username, String email, String firstName, String lastName, String roleName) {
        Keycloak keycloak = getKeycloakInstance();
        try {
            RealmResource realm = keycloak.realm(realmTarget);
            UsersResource users = realm.users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue("Default123!");
            credential.setTemporary(false);
            user.setCredentials(Collections.singletonList(credential));

            Response response = users.create(user);

            // ⚡ Gestion spécifique du code 409
            if (response.getStatus() == 409) {
                System.out.println("Utilisateur déjà existant dans Keycloak : " + username);
                return getUserIdByUsername(username, realm); // récupère l’ID existant
            }

            if (response.getStatus() != 201) {
                String error = response.readEntity(String.class);
                throw new RuntimeException("Erreur Keycloak création utilisateur: " + response.getStatus() + " -> " + error);
            }

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            // 🔹 Ajout du rôle si fourni
            if (roleName != null && !roleName.isEmpty()) {
                RoleRepresentation role = realm.roles().get(roleName).toRepresentation();
                realm.users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
            }

            System.out.println("Utilisateur Keycloak créé : " + username);
            return userId;

        } finally {
            keycloak.close();
        }
    }

    // Méthode utilitaire pour récupérer l'ID d'un utilisateur existant par username
    private String getUserIdByUsername(String username, RealmResource realm) {
        List<UserRepresentation> users = realm.users().search(username, true);
        if (!users.isEmpty()) {
            return users.get(0).getId();
        }
        return null;
    }
    // 🔹 Mettre à jour utilisateur Keycloak
    public void updateUser(String username, String email, String firstName, String lastName) {
        Keycloak keycloak = getKeycloakInstance();
        try {
            RealmResource realm = keycloak.realm(realmTarget);
            UsersResource users = realm.users();

            // Chercher l'utilisateur
            List<UserRepresentation> existingUsers = users.search(username);
            if (existingUsers.isEmpty()) {
                throw new RuntimeException("Utilisateur introuvable dans Keycloak : " + username);
            }

            UserRepresentation user = existingUsers.get(0);

            // Mettre à jour les champs si fournis
            if (email != null && !email.isEmpty()) user.setEmail(email);
            if (firstName != null && !firstName.isEmpty()) user.setFirstName(firstName);
            if (lastName != null && !lastName.isEmpty()) user.setLastName(lastName);

            users.get(user.getId()).update(user);
            System.out.println("Utilisateur Keycloak mis à jour : " + username);

        } finally {
            keycloak.close();
        }
    }

    // 🔹 Supprimer utilisateur Keycloak
    // 🔹 Supprimer utilisateur Keycloak ET BD

    public void deleteUser(String username) {
        Keycloak keycloak = getKeycloakInstance();
        try {
            RealmResource realm = keycloak.realm(realmTarget);
            List<UserRepresentation> users = realm.users().search(username, true);

            if (users.isEmpty()) {
                System.out.println("Utilisateur déjà supprimé dans Keycloak : " + username);
            } else {
                String userId = users.get(0).getId();
                realm.users().delete(userId);
                System.out.println("Utilisateur Keycloak supprimé : " + username);
            }

            // 🔹 Supprimer l'utilisateur de la base de données via l'instance userRepository
            Optional<com.example.backendprojet.model.User> userEntity = userRepository.findByUsername(username);
            if (userEntity.isPresent()) {
                userRepository.delete(userEntity.get());
                System.out.println("Utilisateur supprimé de la BD : " + username);
            } else {
                System.out.println("Utilisateur non trouvé dans la BD : " + username);
            }

        } finally {
            keycloak.close();
        }
    }
    // 🔹 Récupérer tous les utilisateurs Keycloak avec leurs rôles
    public List<Map<String, Object>> getAllUsersWithRoles() {
        Keycloak keycloak = getKeycloakInstance();
        try {
            RealmResource realm = keycloak.realm(realmTarget);
            UsersResource usersResource = realm.users();

            List<UserRepresentation> users = usersResource.list();
            List<Map<String, Object>> result = new ArrayList<>();

            for (UserRepresentation user : users) {
                Map<String, Object> map = new HashMap<>();
                map.put("username", user.getUsername());
                map.put("firstName", user.getFirstName());
                map.put("lastName", user.getLastName());
                map.put("email", user.getEmail());
                map.put("active", user.isEnabled());

                List<RoleRepresentation> roles = usersResource.get(user.getId())
                        .roles()
                        .realmLevel()
                        .listAll();

                List<String> roleNames = roles.stream()
                        .map(RoleRepresentation::getName)
                        .collect(Collectors.toList());

                map.put("roles", roleNames);
                result.add(map);
            }

            return result;

        } finally {
            keycloak.close();
        }
    }
    // 🔹 Activer / Désactiver utilisateur Keycloak
    public void toggleUserStatus(String username, boolean enabled) {
        Keycloak keycloak = getKeycloakInstance();
        try {
            RealmResource realm = keycloak.realm(realmTarget);
            UsersResource users = realm.users();

            // 🔍 Chercher utilisateur
            List<UserRepresentation> existingUsers = users.search(username);

            if (existingUsers.isEmpty()) {
                throw new RuntimeException("Utilisateur introuvable dans Keycloak : " + username);
            }

            UserRepresentation user = existingUsers.get(0);

            // 🔥 IMPORTANT : changer le statut
            user.setEnabled(enabled);

            // 🔄 Mise à jour
            users.get(user.getId()).update(user);

            System.out.println("Statut utilisateur modifié : " + username + " -> " + enabled);

        } finally {
            keycloak.close();
        }
    }
}