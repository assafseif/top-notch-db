package com.project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("=== JWT FILTER START ===");
        logger.info("Request URI: {}", request.getRequestURI());

        String authHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        // Step 1: Check Authorization header
        if (authHeader == null) {
            logger.warn("Authorization header is missing");
        } else {
            logger.info("Authorization header found");
        }

        // Step 2: Extract JWT
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
            logger.info("JWT extracted: {}", jwt);

            try {
                username = jwtTokenProvider.getUsernameFromToken(jwt);
                logger.info("Username extracted from JWT: {}", username);
            } catch (Exception e) {
                logger.error("Error extracting username from JWT", e);
            }
        } else {
            logger.warn("Authorization header does not start with Bearer");
        }

        // Step 3: Check if authentication already exists
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("No existing authentication found in SecurityContext");

            try {
                // Step 4: Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                logger.info("UserDetails loaded: {}", userDetails.getUsername());

                // Step 5: Validate token
                if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                    logger.info("JWT token is valid");

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Step 6: Set authentication
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set in SecurityContext for user: {}", username);

                } else {
                    logger.warn("JWT token is invalid");
                }

            } catch (Exception e) {
                logger.error("Error during authentication process", e);
            }

        } else {
            if (username == null) {
                logger.warn("Username is null, skipping authentication");
            } else {
                logger.info("Authentication already exists, skipping");
            }
        }

        logger.info("Continuing filter chain...");
        filterChain.doFilter(request, response);

        logger.info("=== JWT FILTER END ===");
    }
}