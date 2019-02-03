package com.example.spring.boot.security.jwt.web;

import com.example.spring.boot.security.jwt.domain.Job;
import com.example.spring.boot.security.jwt.dto.JobPageResource;
import com.example.spring.boot.security.jwt.service.JobService;
import com.example.spring.boot.security.jwt.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Api(value = "Job Controller")
@RestController
@RequestMapping("jobs")
public class JobController {

    @Autowired
    JobService jobService;
    @Autowired
    UserService userService;

    @Value("${spring.data.web.pageable.page-parameter:page}")
    private String pageNumber;
    @Value("${spring.data.web.pageable.size-parameter:size}")
    private String pageSize;

    @ApiOperation(value = "create job", notes = "job id is not required", produces = "application/json")
    @ApiImplicitParam(paramType = "body", required = true)
    @PostMapping()
    public HttpEntity<?> createJob(@Valid @RequestBody Job job) {
        Job j = jobService.saveJob(job);
        Resource<Job> resource = getJobResource(j);
        return ResponseEntity.ok(resource);
    }

    @ApiOperation(value = "update job by job id", notes = "job id is required", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "Long", name = "id", value = "job id", required = true),
            @ApiImplicitParam(paramType = "body", name = "job", required = true)
    })
    @PutMapping("{id}")
    public HttpEntity<?> updateJob(@PathVariable Long jobId, @Valid @RequestBody Job job) {
        if (job.getId() == null) {
            job.setId(jobId);
        }
        Job j = jobService.saveJob(job);
        Resource<Job> resource = getJobResource(j);
        return ResponseEntity.ok(resource);
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
        Page<Resource> page1 = page.map(job -> {
            userService.getUsernameById(job.getCreateBy()).ifPresent(job::setCreateByUserName);
            return getJobResource(job);
        });
        //page.forEach(job -> userService.getUsernameById(job.getCreateBy()).ifPresent(job::setCreateByUserName));
        JobPageResource resource = new JobPageResource(page1, pageNumber, pageSize);
        return ResponseEntity.ok(resource);
    }

    @ApiOperation("get job by id")
    @ApiImplicitParam(paramType = "path", dataType = "Long", name = "id", value = "job id", required = true)
    @GetMapping("{id}")
    public HttpEntity<?> getJob(@PathVariable Long id) {
        Optional<Job> jobOptional = jobService.getJob(id);
        if (jobOptional.isPresent()) {
            return ResponseEntity.ok(getJobResource(jobOptional.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // hateoas resource, 主要是返回的数据中包含了links属性，其中可以定义各种url给前端调用，进一步解耦
    private Resource<Job> getJobResource(Job job) {
        Resource<Job> resource = new Resource<>(job);
        resource.add(linkTo(methodOn(JobController.class).getJob(job.getId()))
                .withSelfRel().withType("GET"));
        resource.add(linkTo(methodOn(JobController.class).updateJob(job.getId(), job))
                .withRel("edit").withType("PUT"));
        resource.add(linkTo(methodOn(JobController.class).deleteJob(job.getId()))
                .withRel("del").withType("DELETE"));
        resource.add(linkTo(methodOn(JobController.class).getJobPage(null))
                .withRel("collections").withType("GET"));
        return resource;
    }

}
