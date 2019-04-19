package com.example.spring.boot.oauth2.jwt.config;

import com.example.spring.boot.oauth2.jwt.repository.RoleRepository;
import com.example.spring.boot.oauth2.jwt.repository.UserRepository;
import com.example.spring.boot.oauth2.jwt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Order(2)       //将执行顺序提前至ResourceServerConfig之前
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public WebSecurityConfig(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }

    @Bean
    public UserService userService() { return new UserService(userRepository, roleRepository, passwordEncoder()); }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/webjars/**","/resources/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService()).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().headers().frameOptions().disable().and()
                //由于WebSecurity及ResourceServer的执行顺序问题，这两个/oauth/路径需要通过认证保护
                //https://segmentfault.com/a/1190000012384850
                .requestMatchers().antMatchers("/login","/oauth/authorize","/oauth/confirm_access").and()
                .formLogin().loginPage("/login").permitAll().and()
                .authorizeRequests()
                .antMatchers("/h2-console/**","/resources/**","/favicon.ico",
                        "/**/*.jpg","/**/*.html","/**/*.css", "/**/*.js")
                .permitAll()
                .anyRequest().authenticated();
    }

    //指定页面访问路径
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
        registry.addViewController("/oauth/confirm_access").setViewName("approval");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
