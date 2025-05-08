package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Admin;
import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Recruteur;

import com.ismagi.Fursati.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class LoginController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", error);
        }
        model.addAttribute("activeTab", "login");
        // Utiliser la page "home" qui contient le fragment login
        return "home";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam(value = "rememberMe", required = false) Boolean rememberMe,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        AuthService.AuthResult result = authService.authenticate(email, password);

        if (result.isSuccess()) {
            // Stocker les informations de l'utilisateur dans la session
            session.setAttribute("authenticated", true);
            session.setAttribute("userType", result.getUserType());

            // Stocker l'objet utilisateur selon son type
            switch (result.getUserType()) {
                case ADMIN:
                    Admin admin = (Admin) result.getUser();
                    session.setAttribute("userId", admin.getAdminId());
                    session.setAttribute("userName", admin.getNom() + " " + admin.getPrenom());
                    return "redirect:/admin/dashboard"; // Rediriger vers le dashboard admin

                case CANDIDAT:
                    Candidat candidat = (Candidat) result.getUser();
                    session.setAttribute("userId", candidat.getId());
                    session.setAttribute("userName", candidat.getFirstName() + " " + candidat.getLastName());
                    return "redirect:/candidats/dashboard"; // Redirection corrigée vers /candidats au lieu de /candidat

                case RECRUTEUR:
                    Recruteur recruteur = (Recruteur) result.getUser();
                    session.setAttribute("userId", recruteur.getIdRecruteur());
                    session.setAttribute("userName", recruteur.getNom() + " " + recruteur.getPrenom());
                    return "redirect:/recruteurs/dashboard"; // Rediriger vers le dashboard recruteur

                default:
                    // Cas improbable mais géré quand même
                    redirectAttributes.addAttribute("error", "Type d'utilisateur non reconnu");
                    return "redirect:/login";
            }
        } else {
            // Authentification échouée
            redirectAttributes.addAttribute("error", result.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Invalider la session
        session.invalidate();
        return "redirect:/login";
    }
}