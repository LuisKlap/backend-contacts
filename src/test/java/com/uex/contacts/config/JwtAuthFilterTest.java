package com.uex.contacts.config;

import io.jsonwebtoken.JwtException;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private UserDetailsService userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthFilter jwtAuthFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldAuthenticateWithValidToken() throws ServletException, IOException {
    String token = "valid.jwt.token";
    String username = "testuser";
    UserDetails userDetails = User.builder()
        .username(username)
        .password("password")
        .authorities(Collections.emptyList())
        .build();

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(true);
    when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotAuthenticateWithoutToken() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verify(jwtUtil, never()).validateToken(any());
  }

  @Test
  void shouldNotAuthenticateWithInvalidToken() throws ServletException, IOException {
    String token = "invalid.jwt.token";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(false);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verify(userDetailsService, never()).loadUserByUsername(any());
  }

  @Test
  void shouldNotAuthenticateWithInvalidAuthorizationHeader() throws ServletException, IOException {
    when(request.getHeader("Authorization")).thenReturn("InvalidHeader");

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
    verify(jwtUtil, never()).validateToken(any());
  }

  @Test
  void shouldHandleUsernameNotFoundException() throws ServletException, IOException {
    String token = "valid.jwt.token";
    String username = "nonexistent";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(true);
    when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username))
        .thenThrow(new UsernameNotFoundException("User not found"));

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldHandleJwtException() throws ServletException, IOException {
    String token = "valid.jwt.token";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(true);
    when(jwtUtil.getUsernameFromToken(token)).thenThrow(new JwtException("JWT error"));

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotAuthenticateWhenUserDetailsIsNull() throws ServletException, IOException {
    String token = "valid.jwt.token";
    String username = "testuser";

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(true);
    when(jwtUtil.getUsernameFromToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenReturn(null);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void shouldNotAuthenticateWhenAlreadyAuthenticated() throws ServletException, IOException {
    String token = "valid.jwt.token";
    String username = "testuser";
    UserDetails userDetails = User.builder()
        .username(username)
        .password("password")
        .authorities(Collections.emptyList())
        .build();

    org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(existingAuth);

    when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    when(jwtUtil.validateToken(token)).thenReturn(true);

    jwtAuthFilter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    verify(filterChain).doFilter(request, response);
    verify(userDetailsService, never()).loadUserByUsername(any());
  }
}
