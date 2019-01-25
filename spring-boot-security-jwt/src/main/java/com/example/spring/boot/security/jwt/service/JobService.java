package com.example.spring.boot.security.jwt.service;

import com.example.spring.boot.security.jwt.domain.Job;
import com.example.spring.boot.security.jwt.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class JobService {

    @Autowired JobRepository jobRepository;

    @Transactional
    public void saveJob(Job job) {
        jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }

    public Optional<Job> getJob(Long id) {
        return jobRepository.findById(id);
    }

    public Page<Job> getJobs(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }
}
