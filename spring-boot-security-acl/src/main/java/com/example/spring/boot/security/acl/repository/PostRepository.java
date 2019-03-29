package com.example.spring.boot.security.acl.repository;

import com.example.spring.boot.security.acl.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    //@Secured({"ROLE_USER", "AFTER_ACL_READ"})
    @PostAuthorize("hasRole('ADMIN') or hasPermission(returnObject.get(), 'READ')")
    Optional<Post> findById(Long aLong);

    //@Secured({"ROLE_USER", "AFTER_ACL_COLLECTION_READ"})
    @PostFilter("hasRole('ADMIN') or hasPermission(filterObject, 'READ')")
    List<Post> findAll();

    @Query("select p from Post p where p.author = ?#{principal?.username} or 1 = ?#{hasRole('ADMIN') ? 1 : 0}")
    Page<Post> findAll(Pageable pageable);

    //@Secured({"ROLE_ADMIN", "VOTE_ACL_POST_DELETE"})
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#post, 'DELETE')")
    void delete(@Param("post") Post post);

    @PreAuthorize("hasRole('ADMIN') or hasPermission(#id, 'com.example.spring.boot.security.acl.domain.Post', 'DELETE')")
    void deleteById(@P("id") Long id);
}
