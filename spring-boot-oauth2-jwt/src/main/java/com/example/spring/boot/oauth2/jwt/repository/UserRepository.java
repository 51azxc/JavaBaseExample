package com.example.spring.boot.oauth2.jwt.repository;

import com.example.spring.boot.oauth2.jwt.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    @Query("select u.username from User u where u.id = :id")
    Optional<String> findUsernameById(@Param("id") Long id);
}
