package com.example.backendprojet.services;

import com.example.backendprojet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakService {

    @Autowired
    private UserRepository userRepository;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realmTarget;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─────────────── Obtenir token admin ───────────────
    private String getAdminToken() {
        String url = serverUrl + "/realms/" + realmTarget
                + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Impossible d'obtenir le token Keycloak");
        }

        return (String) response.getBody().get("access_token");
    }

    // ─────────────── Headers avec Bearer ───────────────
    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    // ─────────────── Créer utilisateur dans Keycloak + DB ───────────────
    // ─────────────── Créer utilisateur dans Keycloak + DB ───────────────
// ─────────────── Créer utilisateur dans Keycloak + DB ───────────────
    public String createUser(String username, String email,
                             String firstName, String lastName,
                             String roleName) {

        // Vérifie si l'utilisateur existe déjà dans la DB
        if (userRepository.findByUsername(username).isPresent()) {
            return getUserIdByUsername(username);
        }

        // URL Keycloak pour créer un utilisateur
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users";

        // Création du mot de passe temporaire pour Keycloak
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", "Default123!");  // mot de passe temporaire
        credential.put("temporary", false);

        // Corps de la requête pour Keycloak
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("enabled", true);
        body.put("emailVerified", true);
        body.put("credentials", Collections.singletonList(credential));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, authHeaders());

        try {
            // Création de l'utilisateur dans Keycloak
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Erreur création utilisateur Keycloak : " + response.getStatusCode());
            }

            // Récupération de l'ID Keycloak
            String location = response.getHeaders().getLocation().toString();
            String userId = location.substring(location.lastIndexOf("/") + 1);

            // Assignation du rôle si fourni
            if (roleName != null && !roleName.isEmpty()) {
                assignRoleById(userId, roleName);
            }

            // Sauvegarde dans DB avec mot de passe fictif
            com.example.backendprojet.model.User user = new com.example.backendprojet.model.User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setActive(true);
            user.setPassword(UUID.randomUUID().toString());  // mot de passe fictif pour respecter NOT NULL
            userRepository.save(user);

            return userId;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Si conflit (utilisateur déjà dans Keycloak)
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String userId = getUserIdByUsername(username);
                if (roleName != null && !roleName.isEmpty()) {
                    assignRoleById(userId, roleName);
                }
                return userId;
            }
            throw e;
        }
    }
    // ─────────────── Récupérer tous les utilisateurs avec rôles ───────────────
    public List<Map<String, Object>> getAllUsersWithRoles() {
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users?max=1000";
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody();
        if (users == null) return new ArrayList<>();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> user : users) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.get("id"));
            map.put("username", user.get("username"));
            map.put("firstName", user.get("firstName"));
            map.put("lastName", user.get("lastName"));
            map.put("email", user.get("email"));
            map.put("active", user.get("enabled"));
            map.put("createdAt", user.get("createdTimestamp"));

            String userId = (String) user.get("id");
            List<String> roles = getUserRoles(userId);
            map.put("roles", roles);

            result.add(map);
        }
        return result;
    }

    private List<String> getUserRoles(String userId) {
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId + "/role-mappings/realm";
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
        List<Map<String, Object>> roles = (List<Map<String, Object>>) response.getBody();
        if (roles == null) return new ArrayList<>();

        List<String> excluded = Arrays.asList(
                "default-roles-" + realmTarget,
                "offline_access",
                "uma_authorization"
        );

        return roles.stream()
                .map(r -> (String) r.get("name"))
                .filter(name -> !excluded.contains(name))
                .collect(Collectors.toList());
    }

    // ─────────────── Mettre à jour utilisateur ───────────────
    // ─────────────── Mettre à jour un utilisateur ───────────────
    public void updateUser(String username, String email,
                           String firstName, String lastName) {
        String userId = getUserIdByUsername(username); // récupère l'ID Keycloak

        // Préparer le corps de la requête
        Map<String, Object> body = new LinkedHashMap<>();
        if (email     != null) body.put("email", email);
        if (firstName != null) body.put("firstName", firstName);
        if (lastName  != null) body.put("lastName", lastName);

        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId;
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, authHeaders());

        // Mise à jour dans Keycloak
        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

        // Mise à jour dans la DB si présent
        userRepository.findByUsername(username).ifPresent(user -> {
            if (email     != null) user.setEmail(email);
            if (firstName != null) user.setFirstName(firstName);
            if (lastName  != null) user.setLastName(lastName);
            userRepository.save(user);
        });
    }
    // ─────────────── Supprimer utilisateur ───────────────
    public void deleteUser(String username) {
        String userId = getUserIdByUsername(username);

        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId;
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

        // Supprimer dans DB
        userRepository.findByUsername(username)
                .ifPresent(userRepository::delete);
    }

    // ─────────────── Helpers privés ───────────────
    private String getUserIdByUsername(String username) {
        String url = serverUrl + "/admin/realms/" + realmTarget
                + "/users?username=" + username + "&exact=true";

        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody();

        if (users == null || users.isEmpty()) {
            throw new RuntimeException("Utilisateur introuvable : " + username);
        }
        return (String) users.get(0).get("id");
    }

    private void assignRoleById(String userId, String roleName) {
        String roleUrl = serverUrl + "/admin/realms/" + realmTarget + "/roles/" + roleName;
        HttpEntity<Void> roleRequest = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> roleResponse = restTemplate.exchange(roleUrl, HttpMethod.GET, roleRequest, Map.class);
        Map<String, Object> role = roleResponse.getBody();
        if (role == null) throw new RuntimeException("Rôle introuvable : " + roleName);

        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId + "/role-mappings/realm";
        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(Collections.singletonList(role), authHeaders());
        restTemplate.exchange(url, HttpMethod.POST, request, Void.class);
    }
    // ─────────────── Activer / Désactiver utilisateur ───────────────
    public void toggleUserStatus(String username, boolean enabled) {
        String userId = getUserIdByUsername(username);

        String url = serverUrl + "/admin/realms/" + realmTarget
                + "/users/" + userId;

        Map<String, Object> body = new HashMap<>();
        body.put("enabled", enabled);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, authHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

        // Mise à jour dans la DB
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setActive(enabled);
            userRepository.save(user);
        });
    }
}