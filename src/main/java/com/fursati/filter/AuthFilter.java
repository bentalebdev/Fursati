package com.fursati.filter;

import com.ismagi.Fursati.service.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();
        logger.debug("AuthFilter processing: {}", requestURI);

        // Check if the user is trying to access the login page or perform a login
        boolean isLoginRequest = requestURI.equals("/login") ||
                (requestURI.equals("/login") && httpRequest.getMethod().equals("POST"));

        // Checking if the user is authenticated
        boolean isAuthenticated = session != null &&
                session.getAttribute("authenticated") != null &&
                (Boolean) session.getAttribute("authenticated");

        // Paths that don't require authentication
        boolean isPublicResource =
                requestURI.startsWith("/login") ||
                        requestURI.startsWith("/css") ||
                        requestURI.startsWith("/js") ||
                        requestURI.startsWith("/img") ||
                        requestURI.startsWith("/fonts") ||
                        requestURI.startsWith("/uploads") ||
                        requestURI.startsWith("/register") ||
                        requestURI.startsWith("/signup") ||
                        requestURI.equals("/") ||
                        requestURI.startsWith("/error") ||
                        requestURI.startsWith("/jobs") ||
                        requestURI.startsWith("/companies") ||
                        requestURI.startsWith("/candidates") ||
                        requestURI.startsWith("/blog") ||
                        requestURI.startsWith("/about");

        // If the resource is public or it's a login request, continue
        if (isPublicResource || isLoginRequest) {
            logger.debug("Public resource or login request: {}, allowing access", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // If the user is not authenticated, redirect to login page
        if (!isAuthenticated) {
            logger.debug("User not authenticated, redirecting to login: {}", requestURI);
            httpResponse.sendRedirect("/login");
            return;
        }

        // User is authenticated, check access rights based on user type
        AuthService.UserType userType = (AuthService.UserType) session.getAttribute("userType");
        logger.debug("User authenticated as {}, accessing: {}", userType, requestURI);

        // Admin routes
        if (requestURI.startsWith("/admin") && userType != AuthService.UserType.ADMIN) {
            logger.debug("Access denied for non-admin user to: {}", requestURI);
            httpResponse.sendRedirect("/login?error=Accès+non+autorisé");
            return;
        }

        // Candidate routes
        if (requestURI.startsWith("/candidats") && userType != AuthService.UserType.CANDIDAT) {
            logger.debug("Access denied for non-candidate user to: {}", requestURI);
            httpResponse.sendRedirect("/login?error=Accès+non+autorisé");
            return;
        }

        // Recruiter routes
        if ((requestURI.startsWith("/recruteurs") || requestURI.startsWith("/offres") ||
                requestURI.startsWith("/demandes") || requestURI.startsWith("/company"))
                && userType != AuthService.UserType.RECRUTEUR) {
            logger.debug("Access denied for non-recruiter user to: {}", requestURI);
            httpResponse.sendRedirect("/login?error=Accès+non+autorisé");
            return;
        }

        // If there are API endpoints that need special handling
        if (requestURI.contains("/api/") && !hasApiAccess(userType, requestURI)) {
            logger.debug("API access denied for {} to: {}", userType, requestURI);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("{\"error\": \"Access denied\"}");
            return;
        }

        // User is authenticated and has the correct role
        logger.debug("Access granted for {} to: {}", userType, requestURI);
        chain.doFilter(request, response);
    }

    /**
     * Determines if the user has access to specific API endpoints
     * Can be extended for fine-grained API access control
     */
    private boolean hasApiAccess(AuthService.UserType userType, String requestURI) {
        // Admin has access to all APIs
        if (userType == AuthService.UserType.ADMIN) {
            return true;
        }

        // Candidate API access
        if (userType == AuthService.UserType.CANDIDAT) {
            if (requestURI.contains("/api/candidats/") ||
                    requestURI.contains("/api/documents/") ||
                    requestURI.contains("/api/demandes/")) {
                return true;
            }
        }

        // Recruiter API access
        if (userType == AuthService.UserType.RECRUTEUR) {
            if (requestURI.contains("/api/offres/") ||
                    requestURI.contains("/api/company/") ||
                    requestURI.contains("/api/demandes/")) {
                return true;
            }
        }

        // Default: deny access
        return false;
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed");
    }
}