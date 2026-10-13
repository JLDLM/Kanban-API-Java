package com.example.kambaapijava;

import com.example.kambaapijava.controller.AuthController;
import com.example.kambaapijava.dto.Dtos.*;
import com.example.kambaapijava.repository.UserRepository;
import com.example.kambaapijava.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiTests {
    @Test
    void loginOk() {
        var manager = mock(AuthenticationManager.class);
        var principal = User.withUsername("demo").password("x").authorities(new SimpleGrantedAuthority("ROLE_USER")).build();
        when(manager.authenticate(any())).thenReturn(new UsernamePasswordAuthenticationToken(principal, "x", principal.getAuthorities()));
        var c = new AuthController(mock(UserRepository.class), new BCryptPasswordEncoder(), manager, mock(JwtService.class));
        var tokens = c.login(new Login("demo", "Demo123!"));
        assertEquals("Bearer", tokens.tokenType());
        verify(manager).authenticate(any());
    }

    @Test
    void loginKo() {
        var manager = mock(AuthenticationManager.class);
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        var c = new AuthController(mock(UserRepository.class), new BCryptPasswordEncoder(), manager, mock(JwtService.class));
        assertThrows(BadCredentialsException.class, () -> c.login(new Login("demo", "wrong")));
    }

    @Test
    void adminRouteRequiresAdmin() {
        try {
            var m = com.example.kambaapijava.controller.KanbanController.class.getMethod("users", int.class, int.class);
            assertTrue(m.isAnnotationPresent(org.springframework.security.access.prepost.PreAuthorize.class));
            assertEquals("hasRole('ADMIN')", m.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class).value());
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void invalidRegistrationPasswordFailsValidation() {
        var v = new LocalValidatorFactoryBean();
        v.afterPropertiesSet();
        assertFalse(v.validate(new Register("demo", "demo@example.com", "weak")).isEmpty());
        v.close();
    }

    @Test
    void paginationHasExpectedTotalPages() {
        var p = new Page<>(java.util.List.of("a", "b"), 1, 2, 5, 3);
        assertEquals(3, p.totalPages());
        assertEquals(2, p.content().size());
    }
}
