package com.example.spring.boot.shiro.mybatis.config;

import com.example.spring.boot.shiro.mybatis.service.CustomShiroRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilterFacotryBean() {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(securityManager());
        factoryBean.setLoginUrl("/login");
        factoryBean.setSuccessUrl("/me");
        factoryBean.setUnauthorizedUrl("/403");
        /* 部分默认过滤器
         * anon: 允许匿名访问
         * authc: 需要认证才能访问 @RequiresAuthentication
         * authcBasic: 需要HttpBasic认证才能访问
         * user: 已登录/记住我的用户才能访问 @RequiresUser
         * rest: 根据方法请求类型来确认访问权限
         * roles: 根据指定角色来访问 @RequiresRoles eg: roles[ROLE_ADMIN]
         * perms: 根据指定权限来访问 @RequiresPermissions eg: perms[user:edit]
         * port: 根据指定端口来访问
         * ssl: 只https请求访问
         */
        Map<String, String> map = new LinkedHashMap<>();
        map.put("/login", "anon");
        map.put("/", "anon");

        map.put("/admin", "authc, roles[ROLE_ADMIN]");
        map.put("/users", "authc, perms[user:search]");
        map.put("/**", "authc");
        factoryBean.setFilterChainDefinitionMap(map);
        return factoryBean;
    }

    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher();
        matcher.setHashAlgorithmName("md5");
        //加密1次
        matcher.setHashIterations(1);
        return matcher;
    }
    @Bean
    public Realm realm() {
        CustomShiroRealm realm = new CustomShiroRealm();
        realm.setCredentialsMatcher(hashedCredentialsMatcher());
        return realm;
    }

    @Bean
    public SessionsSecurityManager securityManager() {
       return new DefaultWebSecurityManager(realm());
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        //不设置这个属性又引入了aop的话，如果使用shiro的Requires系列注解，回返回404
        creator.setUsePrefix(true);
        return creator;
    }
}
