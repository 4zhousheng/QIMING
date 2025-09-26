package com.zhousheng.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {



    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // 1. 注入你自己的 JWT 过滤器
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF 防护
                // 因为我们使用 JWT，所以不需要 CSRF 防护
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. 配置 URL 的授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许所有人访问登录和注册接口
                        .requestMatchers("/api/user/login", "/api/user/registerUser").permitAll()
                        // 允许所有人访问公开的商品查询接口
                        .requestMatchers("/api/product/public/**").permitAll()
                        // 除了上面允许的请求，其他任何请求都需要身份认证
                        .anyRequest().hasAuthority("ROLE_USER")
                )

                // 3. 配置 Session 管理策略
                // 我们是无状态的 RESTful API，所以不需要 Session
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 【第 2 步】创建 CORS 配置源 Bean
     * 这是定义具体跨域规则的地方
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的来源 (Origins)
        // 对于开发环境，允许来自 IDE 内置服务器的任何端口
        // 在生产环境中，应该替换成你的前端域名: e.g., "https://www.venus-commerce.com"
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));

        // 允许的 HTTP 方法 (Methods)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许的请求头 (Headers)
        // "*" 表示允许任何请求头
        configuration.setAllowedHeaders(Collections.singletonList("*"));

        // 是否允许发送 Cookie 等凭证信息
        configuration.setAllowCredentials(true);

        // 暴露给浏览器的响应头 (Exposed Headers)
        // 允许前端 JS 访问的响应头，比如自定义的 Header 或分页信息
        // configuration.setExposedHeaders(Arrays.asList("X-Custom-Header", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 URL路径 ("/**") 应用这个CORS配置
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 强哈希函数加密密码
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
