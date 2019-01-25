package com.example.spring.mvc.file;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/*
 * Spring MVC 文件上传与下载
 * 配置类
 * FileController为文件上传下载的控制器
 */

@Configuration
@EnableWebMvc
@ComponentScan("com.example.spring.mvc.file")
@PropertySources({
	@PropertySource(value={"file:${configPath}/config.properties"}, 
					ignoreResourceNotFound=true),
	@PropertySource("classpath:config.properties")
})
public class FileConfig extends WebMvcConfigurerAdapter{
	@Bean 
	public ViewResolver viewResolver(){
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/page/");
		resolver.setSuffix(".jsp");
		return resolver;
	}
	
	@Bean
	public MultipartResolver MultipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setDefaultEncoding("UTF-8");
		//总的文件上传大小
		resolver.setMaxUploadSize(1048576);	
		return resolver;
	}
}
