package com.zhousheng.security;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 这个过滤器会在每个请求到达 Controller 之前被执行，用于验证请求头中的 JWT。
 * 它继承自 OncePerRequestFilter，确保在一次请求中只执行一次。
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil; // 注入我们之前创建的 JWT 工具类

    @Autowired
    private UserDetailsService userDetailsService; // 注入 Spring Security 的用户服务

    /**
     * 过滤器的核心逻辑
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头中获取 Authorization 字段
        final String authHeader = request.getHeader("Authorization");

        // 2. 检查 Header 是否存在，以及是否以 "Bearer " 开头
        // 如果不存在或格式不正确，则直接放行，让后续的过滤器处理（很可能会因为未认证而被拒绝）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取 JWT
        final String jwt = authHeader.substring(7);
        final String username;

        try {
            // 4. 从 JWT 中解析出用户名
            username = jwtUtil.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token has expired for request: {}", request.getRequestURI());
            // 对于 token 过期等异常，我们不直接在这里抛出，而是让请求继续往下走
            // 后续的 Security 过滤器会因为 SecurityContext 中没有认证信息而拒绝访问
            filterChain.doFilter(request, response);

            return;
        } catch (SignatureException e) {
            logger.error("JWT signature does not match for request: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            logger.error("Could not parse JWT token for request: {}", request.getRequestURI(), e);
            filterChain.doFilter(request, response);
            return;
        }


        // 5. 核心认证逻辑：当用户名存在，且 SecurityContext 中还没有认证信息时
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // a. 根据用户名，从数据库中加载用户核心信息 (UserDetails)
            // 这一步是为了确认用户在我们的系统中是真实存在的
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // b. 验证 JWT 是否有效（签名是否正确，且用户名与 UserDetails 中的一致）
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // c. 如果 Token 有效，则创建一个已认证的 Authentication 对象
                // 这个对象包含了用户的身份信息、权限和凭证(我们不存密码，设为null)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // 凭证(credentials)设为 null，因为我们已经通过 JWT 验证了
                        userDetails.getAuthorities() // 用户的权限列表
                );

                // d. 将当前请求的详细信息（如 IP 地址、Session ID）设置到 Authentication 对象中
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // e. 【最关键的一步】将这个已认证的 Authentication 对象，更新到 SecurityContext 中
                // 这样，Spring Security 就知道当前请求的用户是谁，以及他已经被认证了。
                // 后续的授权检查（比如 @PreAuthorize）就可以正常工作了。
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.debug("User '{}' authenticated successfully. Setting security context.", username);
            }
        }

        // 6. 无论认证成功与否，都将请求传递给过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}
