Spring boot 2 with Reactive提供了异步非阻塞响应式模块WebFlux。

`Mono`表示`0...1`，`Flux`表示`0...n`。

它支持传统的Controller编程（`UserController.java`），

也支持函数式编程（`UserHandler.java`负责响应请求，`Routes.java`负责路由分发）。

同时提供了新的响应式客户端连接工具`WebClient`（参考`UserController.java`），

以及响应式测试类`WebTestClient`（参考src/test/java下的测试类）。