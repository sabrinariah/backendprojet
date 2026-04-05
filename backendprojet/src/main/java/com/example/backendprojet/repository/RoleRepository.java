package com.example.backendprojet.repository;




import com.example.backendprojet.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Cette méthode permet de récupérer un rôle par son nom

    Optional<Role> findByName(String name);
}