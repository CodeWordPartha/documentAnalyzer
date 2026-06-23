package com.partha.document_analyzer.repositories;

import com.partha.document_analyzer.entities.User;
import com.partha.document_analyzer.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    void deleteByUsername(String username);
    
    boolean existsByUsername(String lowerCase);

    boolean existsByEmail(String lowerCase);
}
