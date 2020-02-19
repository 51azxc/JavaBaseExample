package com.example.spring.boot.shiro.mybatis.service;

import com.example.spring.boot.shiro.mybatis.entity.Role;
import com.example.spring.boot.shiro.mybatis.entity.User;
import com.example.spring.boot.shiro.mybatis.mapper.UserRolePermissionMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRolePermissionMapper userMapper;

    public UserServiceImpl(UserRolePermissionMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User addUser(User user) {
        user.setPassword(hashMD5String(user.getPassword()));
        userMapper.addUser(user);
        assert user.getId() > 0;
        List<Long> rids = user.getRoles().stream().map(Role::getId).collect(Collectors.toList());
        int result = userMapper.addUserRoles(user.getId(), rids);
        assert result == rids.size();
        return user;
    }

    public List<User> getAllUsers() {
        return userMapper.findUsers();
    }

    public Optional<User> getUserByUsername(String username) {
        return userMapper.findUserByUsername(username);
    }

    private String hashMD5String(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            //md5 hash值为8位字符串，通过BigInteger来转换成16位hex值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteUser(Long userId) {
        int deleteUser = userMapper.deleteUser(userId);
        int deleteUserRoles = userMapper.deleteUserRoles(userId);
        return deleteUser == 1 && deleteUserRoles > 0;
    }

}
