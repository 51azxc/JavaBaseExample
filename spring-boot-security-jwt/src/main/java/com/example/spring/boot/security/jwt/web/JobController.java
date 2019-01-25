package com.example.spring.boot.security.jwt.web;

import com.example.spring.boot.security.jwt.domain.Job;
import com.example.spring.boot.security.jwt.service.JobService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Optional;

@Api(value = "Job Controller")
@RestController
@RequestMapping("jobs")
public class JobController {

    @Autowired
    JobService jobService;

    @ApiOperation(value = "create job", notes = "job id is not required", produces = "application/json")
    @ApiImplicitParam(paramType = "body", required = true)
    @PostMapping()
    public HttpEntity<?> createJob(@Valid @RequestBody Job job) {
        jobService.saveJob(job);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "update job by job id", notes = "job id is required", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "Long", name = "id", value = "job id", required = true),
            @ApiImplicitParam(paramType = "body", name = "job", required = true)
    })
    @PutMapping("{id}")
    public HttpEntity<?> updateJob(@Valid @RequestBody Job job) {
        jobService.saveJob(job);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("delete job by job id")
    @ApiImplicitParam(paramType = "path", dataType = "Long", name = "id", value = "job id", required = true)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public HttpEntity<?> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.ok().build();
    }

    @ApiOperation("get all jobs")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "page",
                    value = "page number", example = "0"),
            @ApiImplicitParam(paramType = "query", dataType = "Integer", name = "size",
                    value = "page size", example = "6"),
            @ApiImplicitParam(paramType = "query", dataType = "String", name = "sort",
                    value = "sort parameter", example = "title,asc")
    })
    @GetMapping()
    public HttpEntity<?> getJobPage(@ApiIgnore  @PageableDefault(size = 6, sort = "title") Pageable pageable) {
        Page<Job> page = jobService.getJobs(pageable);
        return ResponseEntity.ok(page);
    }

    @ApiOperation("get job by id")
    @ApiImplicitParam(paramType = "path", dataType = "Long", name = "id", value = "job id", required = true)
    @GetMapping("{id}")
    public HttpEntity<?> getJob(@PathVariable Long id) {
        Optional<Job> jobOptional = jobService.getJob(id);
        if (jobOptional.isPresent()) {
            return ResponseEntity.ok(jobOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
