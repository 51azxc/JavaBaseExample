package com.example.spring.boot.oauth2.jwt.config;

import com.example.spring.boot.oauth2.jwt.domain.User;
import com.example.spring.boot.oauth2.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableAuthorizationServer      //配置授权服务器
public class AuthorizationConfig extends AuthorizationServerConfigurerAdapter {

    private final DataSource dataSource;
    private final AuthenticationManager authenticationManagerBean;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthorizationConfig(AuthenticationManager authenticationManagerBean, DataSource dataSource,
                               UserService userService,
                               PasswordEncoder passwordEncoder) {
        this.dataSource = dataSource;
        this.authenticationManagerBean = authenticationManagerBean;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    //在jwt中带一些信息
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        //可以使用这种方式在jwt token中添加额外的信息
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter() {
            @Override
            public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
                Optional.ofNullable(authentication)
                        .map(auth -> auth.getUserAuthentication())
                        .map(userAuth -> userAuth.getPrincipal())
                        .map(principal -> (User)principal)
                        .ifPresent(user -> {
                            final Map<String, Object> map = new HashMap<>();
                            map.put("id", user.getId());
                            map.put("username", user.getUsername());
                            List<String> authorities = user.getAuthorities().stream()
                                    .map(authority -> authority.getAuthority()).collect(Collectors.toList());
                            map.put("authorities", authorities);
                            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(map);
                        });
                OAuth2AccessToken token = super.enhance(accessToken, authentication);
                return token;
            }
        };
        //对称密钥
        converter.setSigningKey("signKey");
        return converter;
    }
/*
    //可以使用TokenEnhancer方式在jwt token中添加额外信息
    @Bean
    public TokenEnhancer tokenEnhancer() {
        class CustomTokenEnhancer implements TokenEnhancer {
            @Override
            public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
                Optional.ofNullable(authentication)
                        .map(auth -> auth.getUserAuthentication())
                        .map(userAuth -> userAuth.getPrincipal())
                        .map(principal -> (User)principal)
                        .ifPresent(user -> {
                            final Map<String, Object> map = new HashMap<>();
                            map.put("id", user.getId());
                            map.put("username", user.getUsername());
                            List<String> authorities = user.getAuthorities().stream()
                                    .map(authority -> authority.getAuthority()).collect(Collectors.toList());
                            map.put("authorities", authorities);
                            ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(map);
                        });
                return accessToken;
            }
        }
        return new CustomTokenEnhancer();
    }
*/

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
                .allowFormAuthenticationForClients()
                .passwordEncoder(passwordEncoder);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //TokenEnhancerChain chain = new TokenEnhancerChain();
        //chain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter()));
        endpoints.authenticationManager(authenticationManagerBean)  //用于密码授权方式
                .tokenStore(tokenStore())
                //.tokenEnhancer(chain)
                .accessTokenConverter(jwtAccessTokenConverter())
                .userDetailsService(userService)     //用于refresh token
                .allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST);
    }
}
