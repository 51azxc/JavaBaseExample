[OAuth2](http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)支持**授权码**、**密码**、**客户端**及**简化**这四种模式。以下是几种模式的测试方法:

### 授权码模式

首先在浏览器输入
```
http://localhost:8080/oauth/authorize?client_id=client2&client_secret=client&response_type=code&redirect_uri=http://localhost:8080/test
```
接着成功登陆并且确认授权之后，就可以看到对应的数据
```
{
    "access_token": "...",
    "token_type": "bearer",
    "refresh_token": "...",
    "expires_in": 35999,
    "scope": "read write",
    "jti": "..."
}
```
然后可以通过`access_token`来访问保护资源:
```
$ curl http://localhost:8080/users/me?access_token=...
```

### 密码模式
指定`grant_type`为`password`，然后带入用户的用户名及密码:
```
$ curl -X POST -d "client_id=client1&client_secret=client&grant_type=password&username=admin&password=admin" http://localh
ost:8080/oauth/token
```

### 客户端模式
指定`grant_type`为`client_credentials`:
```
$ curl -X POST -d "client_id=client1&client_secret=client&grant_type=client_credentials" http://localhost:8080/oauth/token
```

### 刷新AccessToken
例用`refresh_token`刷新`access_token`，需要指定`grant_type`为`refresh_token`:
```
$ curl -X POST -d "grant_type=refresh_token&refresh_token=...d&client_id=client1&client_
secret=client" http://localhost:8080/oauth/token
```

### JwtToken带额外信息

1. 使用`JwtAccessTokenConverter`:
```java
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter() {
        @Override
        public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
            Optional.ofNullable(authentication)
                    .map(auth -> auth.getUserAuthentication())
                    .map(userAuth -> userAuth.getPrincipal())
                    .map(principal -> (User)principal)
                    .ifPresent(user -> {
                        final Map<String, Object> map = new HashMap<>();
                        map.put("username", user.getUsername());
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
```
然后在`AuthorizationServerEndpointsConfigurer`中通过`.accessTokenConverter(jwtAccessTokenConverter())`指定即可。
```
public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints.accessTokenConverter(jwtAccessTokenConverter());
}
```

2. 通过自定义`TokenEnhancer`的方式:
```java
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey("signKey");
    return converter;
}

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
```

然后在使用一个`TokenEnhancerChain`将他们组合起来，放入到`endpoints`中：
```java
public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    TokenEnhancerChain chain = new TokenEnhancerChain();
    chain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), jwtAccessTokenConverter()));
    endpoints.tokenEnhancer(chain);
}
```

### Jwt对称加密
```java
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    converter.setSigningKey("signKey");
    return converter;
}
```

### Jwt非对称加密

首先通过`keytool`工具生成密钥:
```
keytool -genkeypair -alias client
                    -keyalg RSA
                    -keypass client
                    -keystore client.jks
                    -storepass client
```
然后生成公钥:
```
keytool -list -rfc --keystore client.jks | openssl x509 -inform pem -pubkey
```
将公钥部分复制到`src/main/resources/public.txt`中。
接下来修改授权服务器配置部分:
```java
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    KeyPair keyPair = new KeyStoreKeyFactory(new ClassPathResource("client.jks"), "client".toCharArray())
                .getKeyPair("client");
    converter.setKeyPair(keyPair);
    return converter;
}
```
然后在资源服务器配置中增加读取公钥功能部分：
```java
@Bean
public JwtAccessTokenConverter jwtAccessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    ClassPathResource resource = new ClassPathResource("public.txt");
    StringBuilder sb = new StringBuilder();
    try {
        List<String> lines = Files.readAllLines(resource.getFile().toPath(), StandardCharsets.UTF_8);
        lines.forEach(line -> sb.append(line));
        converter.setVerifierKey(sb.toString());
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    return converter;
}

@Bean
public TokenStore tokenStore() {
    return new JwtTokenStore(jwtAccessTokenConverter());
}
```
详情可参考[Using JWT with Spring Security OAuth](https://www.baeldung.com/spring-security-oauth-jwt)