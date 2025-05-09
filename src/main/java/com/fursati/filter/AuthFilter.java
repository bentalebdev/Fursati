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

        // Chemins qui ne nécessitent pas d'authentification
        boolean isPublicResource =
                requestURI.startsWith("/login") ||
                        requestURI.startsWith("/css") ||
                        requestURI.startsWith("/js") ||
                        requestURI.startsWith("/img") ||
                        requestURI.startsWith("/fonts") ||
                        requestURI.startsWith("/register") ||
                        requestURI.startsWith("/signup") ||
                        requestURI.equals("/") ||
                        requestURI.startsWith("/error") ||
                        requestURI.startsWith("/jobs") ||
                        requestURI.startsWith("/companies") ||
                        requestURI.startsWith("/candidates") ||
                        requestURI.startsWith("/blog") ||
                        requestURI.startsWith("/about");

        // Si la ressource est publique, on continue
        if (isPublicResource) {
            logger.debug("Public resource: {}, allowing access", requestURI);
            chain.doFilter(request, response);
            return;
        }

        // Vérifie si l'utilisateur est authentifié
        boolean isAuthenticated = session != null && session.getAttribute("authenticated") != null &&
                (Boolean) session.getAttribute("authenticated");

        if (!isAuthenticated) {
            logger.debug("User not authenticated, redirecting to login: {}", requestURI);
            httpResponse.sendRedirect("/login");
            return;
        }

        // L'utilisateur est authentifié, vérifier les droits d'accès
        AuthService.UserType userType = (AuthService.UserType) session.getAttribute("userType");
        logger.debug("User authenticated as {}, accessing: {}", userType, requestURI);

        // URLs pour Admin
        if (requestURI.startsWith("/admin") && userType != AuthService.UserType.ADMIN) {
            logger.debug("Access denied for non-admin user to: {}", requestURI);
            httpResponse.sendRedirect("/login?error=Accès+non+autorisé");
            return;
        }

        // URLs pour Candidat
        if ((requestURI.startsWith("/candidat") || requestURI.startsWith("/candidats"))
                && userType != AuthService.UserType.CANDIDAT) {
            logger.debug("Access denied for non-candidate user to: {}", requestURI);
            httpResponse.sendRedirect("/login?error=Accès+non+autorisé");
            return;
        }

        // URLs pour Recruteur
        if ((requestURI.startsWith("/recruteurs") || requestURI.startsWith("/offres") ||
                requestURI.startsWith("/demandes") || requestURI.startsWith("/company"))
                && userType != AuthService.UserType.RECRUTEUR) {
            logger.debug("Access denied for non-recruiter user to: {}", requestURI);
            httpResponse.sendRedirect("/login?error=Accès+non+autorisé");
            return;
        }

        // L'utilisateur est authentifié et a le bon rôle
        logger.debug("Access granted for {} to: {}", userType, requestURI);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed");
    }
}