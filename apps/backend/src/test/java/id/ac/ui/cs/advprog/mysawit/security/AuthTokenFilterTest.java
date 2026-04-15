package id.ac.ui.cs.advprog.mysawit.security;

import id.ac.ui.cs.advprog.mysawit.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtil jwtUtils;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validJwt_setsAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUsernameFromToken(token)).thenReturn("testuser");

        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_noAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidJwt_noAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");
        when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_nonBearerHeader_noAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_exceptionDuringProcessing_continuesFilterChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer some.token");
        when(jwtUtils.validateJwtToken("some.token")).thenThrow(new RuntimeException("Token processing error"));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Filter chain should still be called even on exception
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_emptyBearerToken_noAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        when(jwtUtils.validateJwtToken("")).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
