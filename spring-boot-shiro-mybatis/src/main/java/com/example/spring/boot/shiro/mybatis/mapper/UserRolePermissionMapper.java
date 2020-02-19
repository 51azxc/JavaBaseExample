package com.example.spring.boot.shiro.mybatis.mapper;

import com.example.spring.boot.shiro.mybatis.entity.Permission;
import com.example.spring.boot.shiro.mybatis.entity.Role;
import com.example.spring.boot.shiro.mybatis.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRolePermissionMapper {

    @Insert("INSERT INTO user (username, password) VALUES(#{user.username}, #{user.password})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int addUser(@Param("user") User user);

    @InsertProvider(type = UserRolePermissionProvider.class, method = "insertUserRoles")
    int addUserRoles(@Param("uid")Long uid, @Param("rids") List<Long> rids);

    @Delete("DELETE FROM user where id = #{id}")
    int deleteUser(@Param("id") Long id);

    @Delete("DELETE FROM user_role where user_id = #{id}")
    int deleteUserRoles(@Param("id") Long id);

    @SelectProvider(type = UserRolePermissionProvider.class, method = "findPermissionsByRoleId")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "permission", column = "permission")
    })
    List<Permission> findPermissionsByRoleId(@Param("roleId")Long roleId);

    @SelectProvider(type = UserRolePermissionProvider.class, method = "findUserRolesByUserId")
    @Results({
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "roleName", column = "role_name"),
            @Result(property = "permissions", column = "id", javaType = List.class,
                    many = @Many(select = "findPermissionsByRoleId"))
    })
    List<Role> findRolesByUserId(@Param("uid")Long userId);

    @Select("SELECT id, username, password FROM user WHERE username = #{username}")
    @Results(id = "userMap", value = {
            @Result(id = true, property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "roles", column = "id", javaType = List.class,
                    many = @Many(select = "findRolesByUserId"))
    })
    Optional<User> findUserByUsername(@Param("username")String username);

    @Select("SELECT id, username FROM user")
    @ResultMap("userMap")
    List<User> findUsers();
}
