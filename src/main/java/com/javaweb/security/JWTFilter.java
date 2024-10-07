package com.javaweb.security;

import com.javaweb.entity.UserEntity;
import com.javaweb.utils.JWTTokenUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class JWTFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailService;

    @Value("api/v1")
    private String apiPrefix;

    @Autowired
    private JWTTokenUtils jwtTokenUtils;

    private boolean isByPassToken(@NonNull  HttpServletRequest request) {
        final List<Pair<String, String>> byPassTokens = Arrays.asList(
                Pair.of(String.format("%s/products", apiPrefix), "GET"),
                Pair.of(String.format("%s/categories", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of("/v3/api-docs", "GET"),
                Pair.of("/swagger-ui/index.html", "GET"),
                Pair.of("/swagger-ui.html", "GET")
        );
        for(Pair<String, String> byPassToken : byPassTokens) {
            if (request.getServletPath().contains(byPassToken.getFirst()) &&
                    request.getMethod().equals(byPassToken.getSecond())) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
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
