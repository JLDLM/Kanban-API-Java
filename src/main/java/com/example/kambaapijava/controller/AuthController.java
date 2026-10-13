package com.example.kambaapijava.controller;

import com.example.kambaapijava.dto.Dtos.*;
import com.example.kambaapijava.repository.UserRepository;
import com.example.kambaapijava.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager auth;
    private final JwtService jwt;

    public AuthController(UserRepository r, PasswordEncoder p, AuthenticationManager a, JwtService j) {
        repo = r;
        encoder = p;
        auth = a;
        jwt = j;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Tokens register(@Valid @RequestBody Register x) {
        repo.create(x.username(), x.email(), encoder.encode(x.password()));
        return tokens(x.username(), "USER");
    }

    @PostMapping("/login")
    public Tokens login(@Valid @RequestBody Login x) {
        var a = auth.authenticate(new UsernamePasswordAuthenticationToken(x.username(), x.password()));
        var u = (UserDetails) a.getPrincipal();
        return tokens(u.getUsername(), u.getAuthorities().iterator().next().getAuthority().substring(5));
    }

    @PostMapping("/refresh")
    public Tokens refresh(@Valid @RequestBody Refresh x) {
        var c = jwt.claims(x.refreshToken());
        if (!"refresh".equals(c.get("typ")))
            throw new BadCredentialsException("Refresh token inválido");
        var u = repo.byUsername(c.getSubject());
        if (u == null || !u.active())
            throw new BadCredentialsException("Refresh token inválido");
        return tokens(u.username(), u.role());
    }

    private Tokens tokens(String u, String r) {
        return new Tokens(jwt.create(u, r, false), jwt.create(u, r, true));
    }
} 
