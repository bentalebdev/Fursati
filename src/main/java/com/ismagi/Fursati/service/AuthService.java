package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Admin;
import com.ismagi.Fursati.entity.Candidat;
import com.ismagi.Fursati.entity.Recruteur;
import com.ismagi.Fursati.repository.AdminRepository;
import com.ismagi.Fursati.repository.CandidatRepository;
import com.ismagi.Fursati.repository.RecruteurRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RecruteurRepository recruteurRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    public enum UserType {
        ADMIN, RECRUTEUR, CANDIDAT
    }

    @Data
    public static class AuthResult {
        private boolean success;
        private String message;
        private UserType userType;
        private Object user;

        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public AuthResult(boolean success, String message, UserType userType, Object user) {
            this.success = success;
            this.message = message;
            this.userType = userType;
            this.user = user;
        }
    }

    public AuthResult authenticate(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return new AuthResult(false, "L'email est requis");
        }

        if (password == null || password.trim().isEmpty()) {
            return new AuthResult(false, "Le mot de passe est requis");
        }

        logger.debug("Tentative d'authentification pour: {}", email);

        // Vérifier si c'est un administrateur
        Optional<Admin> adminOpt = adminRepository.findAdminByEmail(email);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get(); // Unwrap the Optional
            logger.debug("Utilisateur trouvé en tant qu'admin: {}", email);
            if (password.equals(admin.getPassword())) {
                logger.info("Authentification réussie pour admin: {}", email);
                return new AuthResult(true, "Authentification réussie", UserType.ADMIN, admin);
            } else {
                logger.warn("Échec d'authentification pour admin (mot de passe incorrect): {}", email);
                return new AuthResult(false, "Mot de passe incorrect pour administrateur");
            }
        }

        // Vérifier si c'est un recruteur
        Recruteur recruteur = recruteurRepository.findRecruteurByEmail(email);
        if (recruteur != null) {
            logger.debug("Utilisateur trouvé en tant que recruteur: {}", email);

            // Vérifier si le recruteur est actif
            if (!"ACTIVE".equals(recruteur.getStatus())) {
                logger.warn("Tentative de connexion par un recruteur inactif: {}", email);
                return new AuthResult(false, "Votre compte recruteur n'est pas actif. Veuillez contacter l'administrateur.");
            }

            if (password.equals(recruteur.getPassword())) {
                logger.info("Authentification réussie pour recruteur: {}", email);
                return new AuthResult(true, "Authentification réussie", UserType.RECRUTEUR, recruteur);
            } else {
                logger.warn("Échec d'authentification pour recruteur (mot de passe incorrect): {}", email);
                return new AuthResult(false, "Mot de passe incorrect pour recruteur");
            }
        }

        // Vérifier si c'est un candidat
        Candidat candidat = candidatRepository.findCandidatByEmail(email);
        if (candidat != null) {
            logger.debug("Utilisateur trouvé en tant que candidat: {}", email);

            // Vérifier si le candidat est actif
            if (!"active".equals(candidat.getStatus())) {
                logger.warn("Tentative de connexion par un candidat inactif: {}", email);
                return new AuthResult(false, "Votre compte candidat n'est pas actif. Veuillez contacter l'administrateur.");
            }

            if (password.equals(candidat.getPassword())) {
                logger.info("Authentification réussie pour candidat: {}", email);
                return new AuthResult(true, "Authentification réussie", UserType.CANDIDAT, candidat);
            } else {
                logger.warn("Échec d'authentification pour candidat (mot de passe incorrect): {}", email);
                return new AuthResult(false, "Mot de passe incorrect pour candidat");
            }
        }

        // Si l'utilisateur n'existe pas dans aucune des tables
        logger.warn("Aucun utilisateur trouvé avec cet email: {}", email);
        return new AuthResult(false, "Aucun utilisateur trouvé avec cet email");
    }
}