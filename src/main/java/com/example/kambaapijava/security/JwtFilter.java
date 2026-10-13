package com.example.kambaapijava.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService users;

    public JwtFilter(JwtService j, UserDetailsService u) {
        jwt = j;
        users = u;
    }

    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) try {
            var c = jwt.claims(h.substring(7));
            if ("access".equals(c.get("typ"))) {
                var u = users.loadUserByUsername(c.getSubject());
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities()));
            }
        } catch (JwtException | org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(req, res);
    }
}
