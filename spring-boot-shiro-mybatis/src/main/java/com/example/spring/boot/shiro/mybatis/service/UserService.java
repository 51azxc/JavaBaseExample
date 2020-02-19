package com.example.spring.boot.shiro.mybatis.service;

import com.example.spring.boot.shiro.mybatis.entity.User;
import org.springframework.cache.annotation.*;

import java.util.List;
import java.util.Optional;

@CacheConfig(cacheNames = {"userCache"})
public interface UserService {
    @Caching(
            put = {@CachePut(value = "userCache", key = "#user.username")},
            evict = {@CacheEvict(value = "userCache", allEntries = true)}
    )
    User addUser(User user);

    @Cacheable(value = "userCache", key = "'user.' + #username", unless = "#result == null")
    Optional<User> getUserByUsername(String username);

    @Cacheable(value = "userCache", key = "#root.methodName", unless = "#result.size() == 0")
    List<User> getAllUsers();

    @CacheEvict(value = "userCache", allEntries = true)
    boolean deleteUser(Long userId);
}
