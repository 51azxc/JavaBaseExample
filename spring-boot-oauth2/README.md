[OAuth2](http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html)支持**授权码**、**密码**、**客户端**及**简化**这四种模式。以下是几种模式的测试方法:

### 授权码模式

首先在浏览器输入
```
http://localhost:8080/oauth/authorize?client_id=client&response_type=code&scope=select&redirect_uri=http://www.baidu.com
```
然后会弹出登陆框，输入账户密码之后会跳转到百度主页，并且附带一个授权码：
```
http://www.baidu.com/?code=w06KOn
```
然后通过这个授权码获取`access_token`:
```
$ curl -X POST -d "grant_type=authorization_code&code=w06KOn&client_id=client&client_secret=client&redirect_uri=http://
www.baidu.com" http://localhost:8080/oauth/token
```
便可以得到所需的数据
```
{
  "access_token":"ad7c1e3b-4a52-4543-971f-b148c2466f40",
  "token_type":"bearer",
  "refresh_token":"ee8c3238-e28e-4ff7-802a-8a965b22b131",
  "expires_in":36000,
  "scope":"select"
}
```
然后可以通过`access_token`来访问保护资源:
```
$ curl http://localhost:8080/users/me?access_token=ad7c1e3b-4a52-4543-971f-b148c2466f40
```

### 密码模式
指定`grant_type`为`password`，然后带入用户的用户名及密码:
```
$ curl -X POST -d "client_id=client&client_secret=client&grant_type=password&username=user&password=user" http://localh
ost:8080/oauth/token
```

### 客户端模式
指定`grant_type`为`client_credentials`:
```
$ curl -X POST -d "client_id=client&client_secret=client&grant_type=client_credentials" http://localhost:8080/oauth/token
```

### 刷新AccessToken
例用`refresh_token`刷新`access_token`，需要指定`grant_type`为`refresh_token`:
```
$ curl -X POST -d "grant_type=refresh_token&refresh_token=0bad0c94-4159-4a50-80a9-3f2f0e55407d&client_id=client&client_
secret=client" http://localhost:8080/oauth/token
```