package com.rtech.mandiplus.config;

import com.rtech.mandiplus.entity.User;
import com.rtech.mandiplus.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Enforces mandatory profile name setup before granting access to protected business APIs.
 * Satisfies TC-PROFILE-007.
 */
@Component
public class ProfileCompletionFilter extends OncePerRequestFilter {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // If repository is not available in current test slice, bypass filter
        if (userRepository == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Exempt endpoints that must remain accessible prior to profile completion
        if (isExemptPath(path) || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String email = authentication.getName();
            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isProfileComplete()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"PROFILE_SETUP_REQUIRED\",\"message\":\"Mandatory profile name setup is required before accessing protected services.\"}");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExemptPath(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
                || path.startsWith("/webhook")
                || path.startsWith("/ws-mandiplus")
                || path.equals("/error");
    }
}
