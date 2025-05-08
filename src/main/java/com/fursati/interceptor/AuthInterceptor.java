package com.fursati.interceptor;

import com.ismagi.Fursati.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestURI = request.getRequestURI();
        logger.info("AUTH INTERCEPTOR: Checking access to: {}", requestURI);

        // Chemins qui ne nécessitent pas d'authentification
        if (requestURI.startsWith("/login") ||
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
                requestURI.startsWith("/about")) {

            logger.info("Public resource, access allowed: {}", requestURI);
            return true;
        }

        // Vérifier l'authentification
        HttpSession session = request.getSession(false);
        boolean isAuthenticated = session != null &&
                session.getAttribute("authenticated") != null &&
                (Boolean) session.getAttribute("authenticated");

        if (!isAuthenticated) {
            logger.info("Not authenticated, redirecting to login: {}", requestURI);
            response.sendRedirect("/login");
            return false;
        }

        // Vérifier les autorisations
        AuthService.UserType userType = (AuthService.UserType) session.getAttribute("userType");
        logger.info("User authenticated as {}, accessing: {}", userType, requestURI);

        // URLs pour Admin
        if (requestURI.startsWith("/admin") && userType != AuthService.UserType.ADMIN) {
            logger.info("Access denied for non-admin to: {}", requestURI);
            response.sendRedirect("/login?error=Accès+non+autorisé");
            return false;
        }

        // URLs pour Candidat - Noter que le mapping correct est "/candidats" (pluriel)
        if (requestURI.startsWith("/candidats") && userType != AuthService.UserType.CANDIDAT) {
            logger.info("Access denied for non-candidate to: {}", requestURI);
            response.sendRedirect("/login?error=Accès+non+autorisé");
            return false;
        }

        // URLs pour Recruteur
        if ((requestURI.startsWith("/recruteurs") || requestURI.startsWith("/offres") ||
                requestURI.startsWith("/demandes") || requestURI.startsWith("/company"))
                && userType != AuthService.UserType.RECRUTEUR) {
            logger.info("Access denied for non-recruiter to: {}", requestURI);
            response.sendRedirect("/login?error=Accès+non+autorisé");
            return false;
        }

        logger.info("Access granted for {} to: {}", userType, requestURI);
        return true;
    }
}