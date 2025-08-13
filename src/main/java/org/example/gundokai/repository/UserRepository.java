package org.example.gundokai.repository;

import org.example.gundokai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    @Query("SELECT a FROM User a WHERE a.email LIKE %:keyword%")
    List<User> searchByEmail(String keyword);

}
