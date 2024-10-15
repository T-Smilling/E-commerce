package com.javaweb.security;

import com.javaweb.entity.UserEntity;
import com.javaweb.utils.JWTTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class JWTFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailService;

    @Value("/api/v1")
    private String apiPrefix;

    @Autowired
    private JWTTokenUtils jwtTokenUtils;

    private boolean isByPassToken(@NonNull  HttpServletRequest request) {
        PathMatcher pathMatcher = new AntPathMatcher();
        final List<Pair<String, String>> byPassTokens = Arrays.asList(
                Pair.of(String.format("%s/products", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of("/v3/api-docs", "GET"),
                Pair.of("/v3/api-docs/**", "GET"), // Đường dẫn cho tất cả tài nguyên API docs
                Pair.of("/swagger-ui/", "GET"), // Đường dẫn gốc cho Swagger UI
                Pair.of("/swagger-ui/**", "GET"), // Đường dẫn cho tất cả tài nguyên trong Swagger UI
                Pair.of("/swagger-ui.html", "GET"), // Đường dẫn tới trang HTML của Swagger UI
                Pair.of("/webjars/**", "GET"), // Đường dẫn cho các tài nguyên webjar (CSS, JS)
                Pair.of("/send-email","POST"),
                Pair.of("/send-email**","POST"),
                Pair.of("/api/v1/confirm/**", "GET")
        );
        for(Pair<String, String> byPassToken : byPassTokens) {
            if (pathMatcher.match(byPassToken.getFirst(), request.getServletPath()) &&
                    request.getMethod().equalsIgnoreCase(byPassToken.getSecond())) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            boolean bypassResult = isByPassToken(request); // Kiểm tra bypass token
            log.debug("Bypass token result for {}: {}", request.getServletPath(), bypassResult); // Ghi lại kết quả
            if(isByPassToken(request)) {
                filterChain.doFilter(request, response); //enable bypass
                return;
            }
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Unauthorized");
                return;
            }

            final String token = authHeader.substring(7);
            final String email = jwtTokenUtils.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserEntity user = (UserEntity) userDetailService.loadUserByUsername(email);
                if (jwtTokenUtils.validateToken(token, user)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response); //enable bypass
        }catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}
