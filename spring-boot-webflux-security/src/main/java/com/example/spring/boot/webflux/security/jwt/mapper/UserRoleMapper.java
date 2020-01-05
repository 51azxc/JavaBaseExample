package com.example.spring.boot.webflux.security.jwt.mapper;

import com.example.spring.boot.webflux.security.jwt.entity.SystemRole;
import com.example.spring.boot.webflux.security.jwt.entity.SystemUser;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface UserRoleMapper {

    @Insert("INSERT INTO user (username, password) VALUES(#{user.username}, #{user.password})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int addUser(@Param("user")SystemUser user);

    @InsertProvider(type = UserRoleProvider.class, method = "insertRoles")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int addRoles(@Param("roles") List<SystemRole> roles);

    @InsertProvider(type = UserRoleProvider.class, method = "insertUserRoles")
    int addUserRoles(@Param("uid")Long uid, @Param("rids")List<Long> rids);

    @SelectProvider(type = UserRoleProvider.class, method = "findUserRolesByUserId")
    @Results({
            @Result(id = true, property = "id", column = "id", javaType = Long.class),
            @Result(property = "roleType", column = "role_type", javaType = String.class)
    })
    Set<SystemRole> findRolesByUserId(@Param("uid")Long uid);

    @Select("SELECT id, username, password FROM user where username = #{username}")
    @Results(id = "userMap", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "roles", column = "id", many = @Many(select = "findRolesByUserId"))
    })
    Optional<SystemUser> findUserByUsername(@Param("username")String username);
}
