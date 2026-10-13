package com.example.kambaapijava.service;
import com.example.kambaapijava.repository.UserRepository;import org.springframework.security.core.userdetails.*;import org.springframework.stereotype.Service;
@Service public class UserDetailsServiceImpl implements UserDetailsService {private final UserRepository repo;public UserDetailsServiceImpl(UserRepository repo){this.repo=repo;}public UserDetails loadUserByUsername(String u){var user=repo.byUsername(u);if(user==null)throw new UsernameNotFoundException("Credenciales inválidas");return user;}}
