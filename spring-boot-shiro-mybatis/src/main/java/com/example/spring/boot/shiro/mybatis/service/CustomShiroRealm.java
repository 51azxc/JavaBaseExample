package com.example.spring.boot.shiro.mybatis.service;

import com.example.spring.boot.shiro.mybatis.entity.Permission;
import com.example.spring.boot.shiro.mybatis.entity.Role;
import com.example.spring.boot.shiro.mybatis.entity.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomShiroRealm extends AuthorizingRealm {

    @Autowired
    @Lazy   //shiro realm的创建顺序早于TransactionManagement，会导致事务失效，因此要加上懒加载注解，这样事务才能生效
    private UserService userService;

    //检查鉴权
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String username = (String)token.getPrincipal();
        Optional<User> userOptional = userService.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
        }
        return null;
    }

    //检查授权
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        User user = (User)principals.getPrimaryPrincipal();
        //List<String> permissions = user.getRoles().stream().map(Role::getPermissions)
        //        .flatMap(Collection::stream).map(Permission::getPermission).collect(Collectors.toList());
        for (Role role : user.getRoles()) {
            info.addRole(role.getRoleName());
            List<String> permissions = role.getPermissions().stream()
                    .map(Permission::getPermission).collect(Collectors.toList());
            info.addStringPermissions(permissions);
        }
        return info;
    }
}
