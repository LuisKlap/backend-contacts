package com.dev.contacts.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

  private final JwtUtil jwtUtil;
  private final UserDetailsService userDetailsService;

  public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader("Authorization");
    String token = null;

    if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
      token = header.substring(7);
    }

    try {
      if (token != null && jwtUtil.validateToken(token)
          && SecurityContextHolder.getContext().getAuthentication() == null) {
        String username = jwtUtil.getUsernameFromToken(token);
        logger.info("JWT username from token: {}", username);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails != null) {
          UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(auth);
          logger.info("Authentication set for user: {}", username);
        } else {
          logger.info("UserDetails returned null for username: {}", username);
        }
      }
    } catch (UsernameNotFoundException ex) {
      logger.info("User not found from token subject: {}", ex.getMessage());
    } catch (JwtException ex) {
      logger.info("JWT processing error: {}", ex.getMessage());
    } catch (Exception ex) {
      logger.info("Unexpected error in JwtAuthFilter", ex);
    }

    filterChain.doFilter(request, response);
  }

}
