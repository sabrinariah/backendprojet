package com.example.backendprojet.services;

import com.example.backendprojet.model.User;
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

    // ───────── Obtenir token admin ─────────
    private String getAdminToken() {
        String url = serverUrl + "/realms/" + realmTarget + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Impossible d'obtenir le token Keycloak");
        }
        return (String) response.getBody().get("access_token");
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public String createUser(String username,
                             String email,
                             String firstName,
                             String lastName,
                             List<String> roles) {

        // Vérifie si l'utilisateur existe déjà
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            return getUserIdByUsername(username);
        }

        String url = serverUrl + "/admin/realms/" + realmTarget + "/users";

        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", "Default123!");
        credential.put("temporary", false);

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
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

            if (response.getStatusCode() != HttpStatus.CREATED) {
                throw new RuntimeException("Erreur création utilisateur Keycloak : " + response.getStatusCode());
            }

            String location = response.getHeaders().getLocation().toString();
            String userId = location.substring(location.lastIndexOf("/") + 1);

            // 🔥 AJOUT DES RÔLES (MULTI-ROLES)
            if (roles != null && !roles.isEmpty()) {
                for (String role : roles) {
                    assignRoleById(userId, role);
                }
            }

            // Sauvegarde dans DB
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setActive(true);
            user.setPassword(UUID.randomUUID().toString());
            userRepository.save(user);

            return userId;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String userId = getUserIdByUsername(username);

                // 🔥 AJOUT DES RÔLES SI USER EXISTE DÉJÀ
                if (roles != null && !roles.isEmpty()) {
                    for (String role : roles) {
                        assignRoleById(userId, role);
                    }
                }

                return userId;
            }
            throw e;
        }
    }
    // ───────── Récupérer tous les utilisateurs ─────────
    public List<Map<String, Object>> getAllUsersWithRoles() {
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users?max=1000";
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody();
        if (users == null) return new ArrayList<>();

        return users.stream().map(user -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", user.get("id"));
            map.put("username", user.get("username"));
            map.put("firstName", user.get("firstName"));
            map.put("lastName", user.get("lastName"));
            map.put("email", user.get("email"));
            map.put("active", user.get("enabled"));
            map.put("createdAt", user.get("createdTimestamp"));

            String userId = (String) user.get("id");
            map.put("roles", getUserRoles(userId));

            return map;
        }).collect(Collectors.toList());
    }

    private List<String> getUserRoles(String userId) {
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId + "/role-mappings/realm";
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

        List<Map<String, Object>> roles = (List<Map<String, Object>>) response.getBody();
        if (roles == null) return new ArrayList<>();

        List<String> excluded = Arrays.asList("default-roles-" + realmTarget, "offline_access", "uma_authorization");

        return roles.stream()
                .map(r -> (String) r.get("name"))
                .filter(name -> !excluded.contains(name))
                .collect(Collectors.toList());
    }

    // ───────── Récupérer un utilisateur par username ─────────
    public Map<String, Object> getUserByUsername(String username) {
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users?username=" + username + "&exact=true";
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);

        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody();
        if (users == null || users.isEmpty()) throw new RuntimeException("Utilisateur introuvable : " + username);

        Map<String, Object> user = users.get(0);
        String userId = (String) user.get("id");

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.get("id"));
        result.put("username", user.get("username"));
        result.put("firstName", user.get("firstName"));
        result.put("lastName", user.get("lastName"));
        result.put("email", user.get("email"));
        result.put("active", user.get("enabled"));
        result.put("roles", getUserRoles(userId));

        return result;
    }

    // ───────── Mise à jour utilisateur ─────────
    public void updateUser(String username, String email, String firstName, String lastName) {
        String userId = getUserIdByUsername(username);

        Map<String, Object> body = new LinkedHashMap<>();
        if (email != null) body.put("email", email);
        if (firstName != null) body.put("firstName", firstName);
        if (lastName != null) body.put("lastName", lastName);

        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId;
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, authHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

        userRepository.findByUsername(username).ifPresent(user -> {
            if (email != null) user.setEmail(email);
            if (firstName != null) user.setFirstName(firstName);
            if (lastName != null) user.setLastName(lastName);
            userRepository.save(user);
        });
    }

    // ───────── Supprimer utilisateur ─────────
    public void deleteUser(String username) {
        String userId = getUserIdByUsername(username);
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId;
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);

        userRepository.findByUsername(username).ifPresent(userRepository::delete);
    }

    // ───────── Activer / Désactiver utilisateur ─────────
    public void toggleUserStatus(String username, boolean enabled) {
        String userId = getUserIdByUsername(username);

        String url = serverUrl + "/admin/realms/" + realmTarget + "/users/" + userId;
        Map<String, Object> body = new HashMap<>();
        body.put("enabled", enabled);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, authHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);

        userRepository.findByUsername(username).ifPresent(user -> {
            user.setActive(enabled);
            userRepository.save(user);
        });
    }

    // ───────── Helpers ─────────
    private String getUserIdByUsername(String username) {
        Map<String, Object> user = getUserByUsername(username);
        return (String) user.get("id");
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
    public void updateUserRoles(String username, List<String> newRoles) {

        String userId = getUserIdByUsername(username);

        // 🔥 1. Récupérer les rôles actuels
        String getRolesUrl = serverUrl + "/admin/realms/" + realmTarget
                + "/users/" + userId + "/role-mappings/realm";

        HttpEntity<Void> request = new HttpEntity<>(authHeaders());

        ResponseEntity<List> response = restTemplate.exchange(
                getRolesUrl,
                HttpMethod.GET,
                request,
                List.class
        );

        List<Map<String, Object>> currentRoles = response.getBody();

        // 🔥 2. Supprimer les anciens rôles
        if (currentRoles != null && !currentRoles.isEmpty()) {
            String deleteUrl = serverUrl + "/admin/realms/" + realmTarget
                    + "/users/" + userId + "/role-mappings/realm";

            HttpEntity<List<Map<String, Object>>> deleteRequest =
                    new HttpEntity<>(currentRoles, authHeaders());

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, deleteRequest, Void.class);
        }

        // 🔥 3. Ajouter les nouveaux rôles
        if (newRoles != null && !newRoles.isEmpty()) {

            List<Map<String, Object>> rolesToAdd = new ArrayList<>();

            for (String roleName : newRoles) {

                String roleUrl = serverUrl + "/admin/realms/" + realmTarget + "/roles/" + roleName;

                HttpEntity<Void> roleRequest = new HttpEntity<>(authHeaders());

                ResponseEntity<Map> roleResponse = restTemplate.exchange(
                        roleUrl,
                        HttpMethod.GET,
                        roleRequest,
                        Map.class
                );

                Map<String, Object> role = roleResponse.getBody();

                if (role != null) {
                    rolesToAdd.add(role);
                } else {
                    System.out.println("⚠️ Rôle introuvable : " + roleName);
                }
            }

            if (!rolesToAdd.isEmpty()) {
                String addUrl = serverUrl + "/admin/realms/" + realmTarget
                        + "/users/" + userId + "/role-mappings/realm";

                HttpEntity<List<Map<String, Object>>> addRequest =
                        new HttpEntity<>(rolesToAdd, authHeaders());

                restTemplate.exchange(addUrl, HttpMethod.POST, addRequest, Void.class);
            }
        }

        System.out.println("✅ Rôles mis à jour pour : " + username);
    }
    public String registerUser(String username,
                               String email,
                               String firstName,
                               String lastName,
                               String password,
                               List<String> roles) {

        // URL Keycloak admin
        String url = serverUrl + "/admin/realms/" + realmTarget + "/users";

        // credentials (mot de passe dynamique)
        Map<String, Object> credential = new HashMap<>();
        credential.put("type", "password");
        credential.put("value", password); // ✅ mot de passe venant Angular
        credential.put("temporary", false);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("enabled", true);
        body.put("emailVerified", true);
        body.put("credentials", Collections.singletonList(credential));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, authHeaders());

        ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Erreur création utilisateur Keycloak");
        }

        // récupérer ID user
        String location = response.getHeaders().getLocation().toString();
        String userId = location.substring(location.lastIndexOf("/") + 1);

        // assign roles
        if (roles != null) {
            for (String role : roles) {
                assignRoleById(userId, role);
            }
        }

        return userId;
    }
    // ✅ FORGOT PASSWORD — envoie l'action UPDATE_PASSWORD par email
    public void sendResetPasswordEmail(String email) {
        // 1. Chercher l'utilisateur par email
        String searchUrl = serverUrl + "/admin/realms/" + realmTarget
                + "/users?email=" + email + "&exact=true";

        HttpEntity<Void> request = new HttpEntity<>(authHeaders());
        ResponseEntity<List> response = restTemplate.exchange(
                searchUrl, HttpMethod.GET, request, List.class
        );

        List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody();
        if (users == null || users.isEmpty()) return; // email inconnu → silencieux

        String userId = (String) users.get(0).get("id");

        // 2. Déclencher l'action email UPDATE_PASSWORD
        String actionUrl = serverUrl + "/admin/realms/" + realmTarget
                + "/users/" + userId + "/execute-actions-email";

        HttpEntity<List<String>> actionRequest = new HttpEntity<>(
                List.of("UPDATE_PASSWORD"), authHeaders()
        );
        restTemplate.exchange(actionUrl, HttpMethod.PUT, actionRequest, Void.class);
    }
}