package com.clayfin.training.minirib.security;

import com.clayfin.training.minirib.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ERROR = "authError";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/v1/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = jwtService.extractClaims(token);
            UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject());

            if (!user.isAccountNonLocked()) {
                request.setAttribute(AUTH_ERROR, ErrorCode.USER_LOCKED);
            } else if (!user.isEnabled()) {
                request.setAttribute(AUTH_ERROR, ErrorCode.USER_INACTIVE);
            } else {
                // principal is the CIF, later APIs read the customer from here
                String cif = claims.get("cif", String.class);
                var auth = new UsernamePasswordAuthenticationToken(cif, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute(AUTH_ERROR, ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            request.setAttribute(AUTH_ERROR, ErrorCode.TOKEN_INVALID);
        }

        filterChain.doFilter(request, response);
    }
}
