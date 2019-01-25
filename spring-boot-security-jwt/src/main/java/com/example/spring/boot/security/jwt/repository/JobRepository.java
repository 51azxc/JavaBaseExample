package com.example.spring.boot.security.jwt.repository;

import com.example.spring.boot.security.jwt.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

}
