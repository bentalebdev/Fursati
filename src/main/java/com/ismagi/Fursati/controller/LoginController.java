package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Admin;
import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Recruteur;

import com.ismagi.Fursati.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class LoginController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model, HttpServletRequest request) {
        // If user is already authenticated, redirect to appropriate dashboard
        if (isAuthenticated(request)) {
            logger.debug("Already authenticated user accessing login page, redirecting to home");
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", error);
        }
        model.addAttribute("activeTab", "login");
        // Use the "home" page that contains the login fragment
        return "home";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam(value = "rememberMe", required = false) Boolean rememberMe,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        // Log login attempt (without password)
        logger.info("Login attempt for email: {}", email);

        AuthService.AuthResult result = authService.authenticate(email, password);

        if (result.isSuccess()) {
            // Store user information in session
            session.setAttribute("authenticated", true);
            session.setAttribute("userType", result.getUserType());

            // Set session timeout based on "remember me" option
            if (rememberMe != null && rememberMe) {
                // 30 days in seconds (30 * 24 * 60 * 60)
                session.setMaxInactiveInterval(2592000);
                logger.debug("Extended session timeout for remembered user: {}", email);
            } else {
                // Default: 30 minutes (30 * 60)
                session.setMaxInactiveInterval(1800);
            }

            // Store the user object based on type
            switch (result.getUserType()) {
                case ADMIN:
                    Admin admin = (Admin) result.getUser();
                    session.setAttribute("userId", admin.getAdminId());
                    session.setAttribute("userName", admin.getNom() + " " + admin.getPrenom());
                    logger.info("Admin login successful: {} (ID: {})", admin.getNom() + " " + admin.getPrenom(), admin.getAdminId());
                    return "redirect:/admin/dashboard"; // Redirect to admin dashboard

                case CANDIDAT:
                    Candidat candidat = (Candidat) result.getUser();
                    session.setAttribute("userId", candidat.getId());
                    session.setAttribute("userName", candidat.getFirstName() + " " + candidat.getLastName());
                    logger.info("Candidate login successful: {} (ID: {})", candidat.getFirstName() + " " + candidat.getLastName(), candidat.getId());
                    return "redirect:/candidats/dashboard"; // Redirect to candidate dashboard

                case RECRUTEUR:
                    Recruteur recruteur = (Recruteur) result.getUser();
                    session.setAttribute("userId", recruteur.getIdRecruteur());
                    session.setAttribute("userName", recruteur.getNom() + " " + recruteur.getPrenom());
                    logger.info("Recruiter login successful: {} (ID: {})", recruteur.getNom() + " " + recruteur.getPrenom(), recruteur.getIdRecruteur());
                    return "redirect:/recruteurs/dashboard"; // Redirect to recruiter dashboard

                default:
                    // Unlikely case but handled anyway
                    logger.warn("Unknown user type for login: {}", email);
                    redirectAttributes.addAttribute("error", "Type d'utilisateur non reconnu");
                    return "redirect:/login";
            }
        } else {
            // Authentication failed
            logger.warn("Login failed for {}: {}", email, result.getMessage());
            redirectAttributes.addAttribute("error", result.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        // Log the logout
        if (isAuthenticated(request)) {
            Object userId = session.getAttribute("userId");
            String userName = (String) session.getAttribute("userName");
            AuthService.UserType userType = (AuthService.UserType) session.getAttribute("userType");

            logger.info("User logged out: {} (ID: {}, Type: {})", userName, userId, userType);
        }

        // Invalidate the session
        session.invalidate();
        return "redirect:/login";
    }
}