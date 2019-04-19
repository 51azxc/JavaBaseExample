package com.example.spring.boot.oauth2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
@EnableAuthorizationServer      //配置授权服务器
public class AuthorizationConfig extends AuthorizationServerConfigurerAdapter {
    private final AuthenticationManager authenticationManagerBean;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthorizationConfig(AuthenticationManager authenticationManagerBean,
                               UserDetailsService userDetailsService,
                               PasswordEncoder passwordEncoder) {
        this.authenticationManagerBean = authenticationManagerBean;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired


    @Bean
    public TokenStore tokenStore() {
        //将token存储在内存中
        return new InMemoryTokenStore();
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        //token获取验证策略
        security
                //开放/oauth/token_key访问权限
                .tokenKeyAccess("permitAll()")
                //开放/oauth/check_token给认证用户访问
                .checkTokenAccess("isAuthenticated()")
                //配置则运行客户端通过表单认证
                //不配置则使用Basic Auth认证(Headers: Authorization: Basic Base64.encode(client-id:client-secret))
                .allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        //配置内存中存在的认证方式
        clients.inMemory()
                .withClient("client").secret(passwordEncoder.encode("client"))
                .scopes("read","write")
                .authorities("ROLE_USER")
                //授权码/客户端/密码认证方式
                .authorizedGrantTypes("authorization_code", "client_credentials", "password", "refresh_token")
                .accessTokenValiditySeconds(36000)
                .refreshTokenValiditySeconds(360000)
                //不要使用根路径，'/'用于oauth默认测试路径
                .redirectUris("http://localhost:8080/test")
                //不显示approve页面
                .autoApprove(true);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManagerBean)
                .tokenStore(tokenStore())
                .userDetailsService(userDetailsService)     //用于refresh token
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
    }
}
