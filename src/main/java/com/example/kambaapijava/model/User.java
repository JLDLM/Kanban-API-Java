package com.example.kambaapijava.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record User(long id, String username, String email, String passwordHash, String role,
                   boolean active) implements UserDetails {
 public Collection<? extends GrantedAuthority> getAuthorities() {
  return java.util.List.of(new SimpleGrantedAuthority("ROLE_" + role));
 }

 public String getPassword() {
  return passwordHash;
 }

 public String getUsername() {
  return username;
 }

 public boolean isAccountNonExpired() {
  return true;
 }

 public boolean isAccountNonLocked() {
  return active;
 }

 public boolean isCredentialsNonExpired() {
  return true;
 }

 public boolean isEnabled() {
  return active;
 }
}
