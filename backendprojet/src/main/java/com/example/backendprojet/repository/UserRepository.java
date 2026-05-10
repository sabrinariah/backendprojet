package com.example.backendprojet.repository;




import com.example.backendprojet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 🔹 Repository pour gérer les utilisateurs
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Récupérer un utilisateur par son username


    // Vérifier si un utilisateur existe par son username
    boolean existsByUsername(String username);

    // Vérifier si un utilisateur existe par son email
    boolean existsByEmail(String email);


    Optional<User> findByUsername(String username);
    void deleteByUsername(String username);

}

