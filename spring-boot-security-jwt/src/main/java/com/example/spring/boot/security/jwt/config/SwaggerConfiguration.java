package com.example.spring.boot.security.jwt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//http://localhost:{port}/swagger-ui.html
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    @Value("${server.port:8888}")
    private String port;

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .ignoredParameterTypes(Page.class, Sort.class)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.spring.boot.security.jwt.web"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("REST APIs")
                .description("")
                .termsOfServiceUrl("http://localhost:8080")
                .contact(new Contact("tester", "http://localhost:" + port, "test123@126.com"))
                .version("0.1")
                .build();
    }
}
