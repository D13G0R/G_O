package com.core.G_O.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

import org.springframework.context.annotation.Lazy;

import com.core.G_O.service.JwtService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired private JwtService jwtService;
    @Autowired @Lazy private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userPhone;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userPhone = jwtService.extractUsername(jwt);

        if (userPhone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = this.userDetailsService.loadUserByUsername(userPhone);
    
    if (jwtService.isTokenValid(jwt, userPhone)) { 
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails, 
            null, 
            userDetails.getAuthorities()
        );
        
            // ⚠️ MUY IMPORTANTE: Agrega los detalles de la petición web al token
        authToken.setDetails(new org.springframework.security.web.authentication.WebAuthenticationDetailsSource().buildDetails(request));
        
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
        filterChain.doFilter(request, response);

        
    }
}
