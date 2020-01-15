package com.example.spring.boot.webflux.security.jwt.repository;

import com.example.spring.boot.webflux.security.jwt.domain.Role;
import com.example.spring.boot.webflux.security.jwt.domain.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleType(RoleType roleType);
    @Query(value = "select * from role r join user_roles ur on r.id = ur.role_id " +
            "join user u on ur.user_id = u.id where u.username = ?", nativeQuery = true)
    Set<Role> findByUsers_username(String username);

    Set<Role> findByRoleTypeIn(List<RoleType> roleTypes);
}
