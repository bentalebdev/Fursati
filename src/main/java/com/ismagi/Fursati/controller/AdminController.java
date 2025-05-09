package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.*;
import com.ismagi.Fursati.service.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @Autowired
    private OffreService offreService;

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CandidatService candidatService;

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private RecruteurService recruteurService;


    @GetMapping
    public String showAdmin(Model model) {
        model.addAttribute("activeTab", "dashboard");
        return "adminboard";
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("activeTab", "dashboard");
        // Alimenter le dashboard avec les données
        prepareAdminDashboard(model);
        return "adminboard";
    }

    /**
     * Méthode pour préparer les données du dashboard
     */
    private void prepareAdminDashboard(Model model) {
        // Récupérer les statistiques globales
        Map<String, Long> stats = getGlobalStats();
        model.addAttribute("totalCandidats", stats.get("totalCandidats"));
        model.addAttribute("totalOffres", stats.get("totalOffres"));
        model.addAttribute("totalDemandes", stats.get("totalDemandes"));
        model.addAttribute("totalRecruteurs", stats.get("totalRecruteurs"));
        model.addAttribute("totalEntreprises", stats.get("totalEntreprises"));
        model.addAttribute("offresActives", stats.get("offresActives"));

        // Statistiques des candidatures
        Map<String, Long> demandeStats = getDemandeStats();
        model.addAttribute("demandesPending", demandeStats.get("PENDING"));
        model.addAttribute("demandesReviewed", demandeStats.get("REVIEWED"));
        model.addAttribute("demandesInterview", demandeStats.get("INTERVIEW"));
        model.addAttribute("demandesAccepted", demandeStats.get("SHORTLISTED"));
        model.addAttribute("demandesRejected", demandeStats.get("REJECTED"));

        // Récentes activités
        model.addAttribute("recentDemandes", getRecentDemandes());
        model.addAttribute("recentOffres", getRecentOffres());

        // Candidats récents
        model.addAttribute("recentCandidats", getRecentCandidats());
    }

    /**
     * Récupère les statistiques globales du système
     */
    private Map<String, Long> getGlobalStats() {
        Map<String, Long> stats = new HashMap<>();

        // Candidats
        List<Candidat> candidats = candidatService.getAllCandidats();
        stats.put("totalCandidats", (long) candidats.size());

        // Offres
        List<Offre> offres = offreService.getAllOffres();
        stats.put("totalOffres", (long) offres.size());

        // Offres actives
        long offresActives = offres.stream()
                .filter(o -> "ACTIVE".equals(o.getStatus()))
                .count();
        stats.put("offresActives", offresActives);

        // Demandes
        List<Demande> demandes = demandeService.getAllDemandes();
        stats.put("totalDemandes", (long) demandes.size());

        // Recruteurs
        List<Recruteur> recruteurs = recruteurService.getAllRecruteurs();
        stats.put("totalRecruteurs", (long) recruteurs.size());

        // Entreprises
        List<Company> entreprises = companyService.getCompanies();
        stats.put("totalEntreprises", (long) entreprises.size());

        return stats;
    }

    /**
     * Récupère les statistiques des demandes par état
     */
    private Map<String, Long> getDemandeStats() {
        Map<String, Long> stats = new HashMap<>();
        List<Demande> demandes = demandeService.getAllDemandes();

        // Initialiser tous les états à 0
        stats.put("PENDING", 0L);
        stats.put("REVIEWED", 0L);
        stats.put("INTERVIEW", 0L);
        stats.put("SHORTLISTED", 0L);
        stats.put("REJECTED", 0L);

        // Compter les demandes par état
        for (Demande demande : demandes) {
            String etat = demande.getEtat();
            if (etat != null) {
                stats.put(etat, stats.getOrDefault(etat, 0L) + 1);
            }
        }

        return stats;
    }

    /**
     * Récupère les demandes récentes (5 dernières)
     */
    private List<Demande> getRecentDemandes() {
        List<Demande> allDemandes = demandeService.getAllDemandes();

        // Trier par date de demande (la plus récente en premier)
        return allDemandes.stream()
                .filter(d -> d.getDateDemande() != null)
                .sorted(Comparator.comparing(Demande::getDateDemande).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les offres récentes (5 dernières)
     */
    private List<Offre> getRecentOffres() {
        List<Offre> allOffres = offreService.getAllOffres();

        // Trier par date de publication (la plus récente en premier)
        return allOffres.stream()
                .filter(o -> o.getPostedAt() != null)
                .sorted(Comparator.comparing(Offre::getPostedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les candidats récents (5 derniers)
     */
    private List<Candidat> getRecentCandidats() {
        List<Candidat> allCandidats = candidatService.getAllCandidats();

        // Ici, nous n'avons pas de date d'inscription pour les candidats
        // On prend simplement les 5 derniers par ID (hypothèse que l'ID est auto-incrémenté)
        return allCandidats.stream()
                .sorted(Comparator.comparing(Candidat::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }


    /**
     * Méthodes pour la gestion des candidats
     */

    @GetMapping("/candidats")
    public String showAdminCandidats(Model model,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String skill,
                                     @RequestParam(required = false) String city,
                                     @RequestParam(required = false) String ageRange) {

        // Récupérer les compétences et villes pour les filtres
        List<String> allSkills = getDistinctSkills();
        List<String> cities = getDistinctCities();

        // Récupérer les candidats (filtres à implémenter)
        List<Candidat> candidats = candidatService.getAllCandidats();

        // Filtrer les candidats selon les critères (à mettre dans un service)
        if (search != null || status != null || skill != null || city != null || ageRange != null) {
            candidats = filterCandidats(candidats, search, status, skill, city, ageRange);
        }

        // Ajouter les données au modèle
        model.addAttribute("candidats", candidats);
        model.addAttribute("allSkills", allSkills);
        model.addAttribute("cities", cities);
        model.addAttribute("activeTab", "candidats");

        // Ajouter les paramètres de filtre pour pagination
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("skill", skill);
        model.addAttribute("city", city);
        model.addAttribute("ageRange", ageRange);

        return "adminboard";
    }

    /**
     * Méthode pour récupérer les données d'un candidat
     */
    @GetMapping("/candidats/{id}/data")
    @ResponseBody
    public Candidat getCandidatData(@PathVariable Long id) {
        return candidatService.getCandidatById(id);
    }

    /**
     * Méthode pour ajouter un candidat
     */
    @PostMapping("/candidats/add")
    @ResponseBody
    public Map<String, Object> addCandidat(@RequestBody Candidat candidat) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Sauvegarder le candidat
            Candidat savedCandidat = candidatService.saveCandidat(candidat);

            response.put("success", true);
            response.put("candidat", savedCandidat);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Méthode pour mettre à jour un candidat
     */
    @PostMapping("/candidats/{id}/update")
    @ResponseBody
    public Map<String, Object> updateCandidat(@PathVariable Long id, @RequestBody Candidat updatedCandidat) {
        Map<String, Object> response = new HashMap<>();

        try {
            Candidat existingCandidat = candidatService.getCandidatById(id);

            if (existingCandidat != null) {
                // Mettre à jour les informations de base
                existingCandidat.setFirstName(updatedCandidat.getFirstName());
                existingCandidat.setLastName(updatedCandidat.getLastName());
                existingCandidat.setEmail(updatedCandidat.getEmail());
                existingCandidat.setPhone(updatedCandidat.getPhone());
                existingCandidat.setBirthdate(updatedCandidat.getBirthdate());
                existingCandidat.setAddress(updatedCandidat.getAddress());
                existingCandidat.setSummary(updatedCandidat.getSummary());

                // Sauvegarder le candidat mis à jour
                Candidat savedCandidat = candidatService.saveCandidat(existingCandidat);

                response.put("success", true);
                response.put("candidat", savedCandidat);
            } else {
                response.put("success", false);
                response.put("error", "Candidat non trouvé");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Méthode pour supprimer un candidat
     */
    @DeleteMapping("/candidats/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteCandidat(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            candidatService.deleteCandidat(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Méthode pour supprimer plusieurs candidats
     */
    @PostMapping("/candidats/batch-delete")
    @ResponseBody
    public Map<String, Object> batchDeleteCandidats(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Long> ids = request.get("ids");
            if (ids != null && !ids.isEmpty()) {
                for (Long id : ids) {
                    candidatService.deleteCandidat(id);
                }
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("error", "Aucun ID fourni");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Méthode pour exporter les candidats en CSV
     */
    @GetMapping("/candidats/export")
    public void exportCandidats(HttpServletResponse response,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String skill,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String ageRange) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"candidats.csv\"");

        // Récupérer les candidats
        List<Candidat> candidats = candidatService.getAllCandidats();

        // Appliquer les filtres si nécessaire
        if (search != null || status != null || skill != null || city != null || ageRange != null) {
            candidats = filterCandidats(candidats, search, status, skill, city, ageRange);
        }

        // Définir les en-têtes CSV
        String[] headers = {
                "ID", "Prénom", "Nom", "Email", "Téléphone", "Date de naissance", "Adresse"
        };

        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT.withHeader(headers))) {
            for (Candidat candidat : candidats) {
                csvPrinter.printRecord(
                        candidat.getId(),
                        candidat.getFirstName(),
                        candidat.getLastName(),
                        candidat.getEmail(),
                        candidat.getPhone(),
                        candidat.getBirthdate() != null ? candidat.getBirthdate().toString() : "",
                        candidat.getAddress()
                );
            }
        }
    }

    /**
     * Méthode utilitaire pour filtrer les candidats
     */
    /**
     * Méthode utilitaire pour filtrer les candidats
     */
    private List<Candidat> filterCandidats(List<Candidat> candidats, String search, String status,
                                           String skill, String city, String ageRange) {

        List<Candidat> filteredCandidats = new ArrayList<>(candidats);

        // Filtre par recherche (nom, email, téléphone)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            filteredCandidats = filteredCandidats.stream()
                    .filter(c -> (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(searchLower)) ||
                            (c.getLastName() != null && c.getLastName().toLowerCase().contains(searchLower)) ||
                            (c.getEmail() != null && c.getEmail().toLowerCase().contains(searchLower)) ||
                            (c.getPhone() != null && c.getPhone().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        // Filtre par statut maintenant que le champ status a été ajouté
        if (status != null && !status.trim().isEmpty()) {
            filteredCandidats = filteredCandidats.stream()
                    .filter(c -> c.getStatus() != null && c.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        // Filtre par compétence - this needs custom handling since we no longer directly access skills
        if (skill != null && !skill.trim().isEmpty()) {
            // This will need custom implementation or query to filter by skills
            // For now, we'll just return all candidates when skill filter is used
            System.out.println("Skill filtering is not currently implemented");
        }

        // Filtre par ville
        if (city != null && !city.trim().isEmpty()) {
            filteredCandidats = filteredCandidats.stream()
                    .filter(c -> c.getAddress() != null && c.getAddress().toLowerCase().contains(city.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filtre par tranche d'âge
        if (ageRange != null && !ageRange.trim().isEmpty()) {
            LocalDate now = LocalDate.now();

            int minAge = 0;
            int maxAge = 150; // Valeur par défaut élevée

            switch (ageRange) {
                case "18-25":
                    minAge = 18;
                    maxAge = 25;
                    break;
                case "26-35":
                    minAge = 26;
                    maxAge = 35;
                    break;
                case "36-45":
                    minAge = 36;
                    maxAge = 45;
                    break;
                case "46+":
                    minAge = 46;
                    maxAge = 150;
                    break;
            }

            final int finalMinAge = minAge;
            final int finalMaxAge = maxAge;

            filteredCandidats = filteredCandidats.stream()
                    .filter(c -> {
                        if (c.getBirthdate() == null) {
                            return false;
                        }

                        int age = Period.between(c.getBirthdate(), now).getYears();
                        return age >= finalMinAge && age <= finalMaxAge;
                    })
                    .collect(Collectors.toList());
        }

        return filteredCandidats;
    }

    /**
     * Méthode utilitaire pour récupérer toutes les compétences distinctes
     */
    private List<String> getDistinctSkills() {
        // Cette implémentation est simplifiée car le modèle Candidat n'a plus de champ skills
        // Dans une implémentation réelle, vous récupéreriez cela depuis une base de données
        return Arrays.asList("Java", "Spring", "JavaScript", "React", "Angular", "Python", "SQL", "DevOps");
    }

    /**
     * Méthode utilitaire pour récupérer toutes les villes distinctes
     */
    private List<String> getDistinctCities() {
        return candidatService.getAllCandidats().stream()
                .map(Candidat::getAddress)
                .filter(address -> address != null && !address.isEmpty())
                .map(address -> {
                    // Extraire la ville de l'adresse (on suppose que la ville est après la dernière virgule)
                    int commaIndex = address.lastIndexOf(',');
                    if (commaIndex > 0 && commaIndex < address.length() - 1) {
                        return address.substring(commaIndex + 1).trim();
                    }
                    return address;
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /*
     * COMPANY MANAGEMENT METHODS
     */

    @GetMapping("/companies")
    public String showAdminCompanies(Model model,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(required = false) String verificationStatus,
                                     @RequestParam(required = false) String sector,
                                     @RequestParam(required = false) String city,
                                     @RequestParam(required = false) String companySize) {

        // Get available sectors and cities for filters
        List<String> sectors = companyService.getAllSectors();
        List<String> cities = companyService.getAllCities();

        // Get filtered companies
        List<Company> companyList;

        // Apply filters if any are present
        if (search != null || verificationStatus != null || sector != null ||
                city != null || companySize != null) {

            // Convert verification status string to Boolean if present
            Boolean isVerified = null;
            if (verificationStatus != null && !verificationStatus.isEmpty()) {
                isVerified = Boolean.valueOf(verificationStatus);
            }

            companyList = companyService.getFilteredCompanies(search, isVerified, sector, city, companySize);
        } else {
            companyList = companyService.getCompanies();
        }

        // Add data to the model
        model.addAttribute("companies", companyList);
        model.addAttribute("sectors", sectors);
        model.addAttribute("cities", cities);
        model.addAttribute("activeTab", "companies");

        // Add filter parameters to support pagination
        model.addAttribute("search", search);
        model.addAttribute("verificationStatus", verificationStatus);
        model.addAttribute("sector", sector);
        model.addAttribute("city", city);
        model.addAttribute("companySize", companySize);

        return "adminboard";
    }

    @PostMapping("/companies/{id}/toggle-verification")
    @ResponseBody
    public Map<String, Object> toggleCompanyVerification(@PathVariable Long id,
                                                         @RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean newStatus = (boolean) request.get("isVerified");
            Company updatedCompany = companyService.toggleVerificationStatus(id, newStatus);

            if (updatedCompany != null) {
                response.put("success", true);
                response.put("company", updatedCompany);
            } else {
                response.put("success", false);
                response.put("error", "Company not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @DeleteMapping("/companies/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteCompany(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean success = companyService.deleteCompany(id);
            response.put("success", success);
            if (!success) {
                response.put("error", "Failed to delete company. It may be referenced by other entities.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/companies/batch-delete")
    @ResponseBody
    public Map<String, Object> batchDeleteCompanies(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Long> ids = request.get("ids");
            if (ids != null && !ids.isEmpty()) {
                boolean success = companyService.deleteCompanies(ids);
                response.put("success", success);
                if (!success) {
                    response.put("error", "Failed to delete some companies. They may be referenced by other entities.");
                }
            } else {
                response.put("success", false);
                response.put("error", "No company IDs provided");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/companies/export")
    public void exportCompanies(HttpServletResponse response,
                                @RequestParam(required = false) String search,
                                @RequestParam(required = false) String verificationStatus,
                                @RequestParam(required = false) String sector,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String companySize) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"companies.csv\"");

        // Get filtered companies
        List<Company> companies;

        // Apply filters if any are present
        if (search != null || verificationStatus != null || sector != null ||
                city != null || companySize != null) {

            // Convert verification status string to Boolean if present
            Boolean isVerified = null;
            if (verificationStatus != null && !verificationStatus.isEmpty()) {
                isVerified = Boolean.valueOf(verificationStatus);
            }

            companies = companyService.getFilteredCompanies(search, isVerified, sector, city, companySize);
        } else {
            companies = companyService.getCompanies();
        }

        // Define CSV headers
        String[] headers = {
                "ID", "Nom", "Secteur", "Site Web", "Email", "Téléphone", "Ville", "Pays",
                "Taille", "Année Création", "Vérifiée", "Nombre de Recruteurs"
        };

        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT.withHeader(headers))) {
            for (Company company : companies) {
                csvPrinter.printRecord(
                        company.getId(),
                        company.getNomEntreprise(),
                        company.getSecteur(),
                        company.getSiteWeb(),
                        company.getEmailContact(),
                        company.getTelephone(),
                        company.getVille(),
                        company.getPays(),
                        company.getTailleEntreprise(),
                        company.getAnneeCreation(),
                        company.getIsVerified() != null && company.getIsVerified() ? "Oui" : "Non",
                        company.getRecruteurs() != null ? company.getRecruteurs().size() : 0
                );
            }
        }
    }
    /**
     * Get company data by ID for editing
     */
    @GetMapping("/companies/{id}/data")
    @ResponseBody
    public Company getCompanyData(@PathVariable Long id) {
        Optional<Company> company = companyService.findById(id);
        return company.orElse(null);
    }

    /**
     * Add a new company
     */
    @PostMapping("/companies/add")
    @ResponseBody
    public Map<String, Object> addCompany(@RequestBody Company company) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Set default values if needed
            if (company.getIsVerified() == null) {
                company.setIsVerified(false);
            }
            if (company.getPays() == null || company.getPays().isEmpty()) {
                company.setPays("Maroc");
            }

            // Save the company
            Company savedCompany = companyService.save(company);

            response.put("success", true);
            response.put("company", savedCompany);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Update an existing company
     */
    @PostMapping("/companies/{id}/update")
    @ResponseBody
    public Map<String, Object> updateCompany(@PathVariable Long id, @RequestBody Company updatedCompany) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find existing company
            Optional<Company> existingCompanyOpt = companyService.findById(id);

            if (existingCompanyOpt.isPresent()) {
                Company existingCompany = existingCompanyOpt.get();

                // Update fields
                if (updatedCompany.getNomEntreprise() != null) {
                    existingCompany.setNomEntreprise(updatedCompany.getNomEntreprise());
                }
                if (updatedCompany.getSecteur() != null) {
                    existingCompany.setSecteur(updatedCompany.getSecteur());
                }
                if (updatedCompany.getSiteWeb() != null) {
                    existingCompany.setSiteWeb(updatedCompany.getSiteWeb());
                }
                if (updatedCompany.getEmailContact() != null) {
                    existingCompany.setEmailContact(updatedCompany.getEmailContact());
                }
                if (updatedCompany.getDescription() != null) {
                    existingCompany.setDescription(updatedCompany.getDescription());
                }
                if (updatedCompany.getAnneeCreation() != null) {
                    existingCompany.setAnneeCreation(updatedCompany.getAnneeCreation());
                }
                if (updatedCompany.getTailleEntreprise() != null) {
                    existingCompany.setTailleEntreprise(updatedCompany.getTailleEntreprise());
                }
                if (updatedCompany.getAdresse() != null) {
                    existingCompany.setAdresse(updatedCompany.getAdresse());
                }
                if (updatedCompany.getVille() != null) {
                    existingCompany.setVille(updatedCompany.getVille());
                }
                if (updatedCompany.getPays() != null) {
                    existingCompany.setPays(updatedCompany.getPays());
                }
                if (updatedCompany.getCodePostal() != null) {
                    existingCompany.setCodePostal(updatedCompany.getCodePostal());
                }
                if (updatedCompany.getTelephone() != null) {
                    existingCompany.setTelephone(updatedCompany.getTelephone());
                }
                if (updatedCompany.getIsVerified() != null) {
                    existingCompany.setIsVerified(updatedCompany.getIsVerified());
                }

                // Save updated company
                Company savedCompany = companyService.save(existingCompany);

                response.put("success", true);
                response.put("company", savedCompany);
            } else {
                response.put("success", false);
                response.put("error", "Entreprise non trouvée");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/jobs")
    public String showAdminJobs(Model model) {
        List<Offre> offres = offreService.getAllOffres();
        model.addAttribute("jobs", offres);
        model.addAttribute("activeTab", "jobs");

        return "adminboard";
    }

    // Endpoint pour récupérer les données d'une offre
    @GetMapping("/jobs/{id}/data")
    @ResponseBody
    public Offre getJobData(@PathVariable Long id) {
        return offreService.getOffreById(id);
    }

    // Endpoint pour mettre à jour une offre
    @PutMapping("/jobs/{id}")
    @ResponseBody
    public Map<String, Object> updateJob(@PathVariable Long id, @RequestBody Offre updatedOffre) {
        Map<String, Object> response = new HashMap<>();
        try {
            Offre existingOffre = offreService.getOffreById(id);
            if (existingOffre == null) {
                response.put("success", false);
                response.put("error", "Offre non trouvée");
                return response;
            }

            // Mise à jour des champs simples
            existingOffre.setTitle(updatedOffre.getTitle());
            existingOffre.setDescription(updatedOffre.getDescription());
            existingOffre.setIndustry(updatedOffre.getIndustry());
            existingOffre.setLocation(updatedOffre.getLocation());
            existingOffre.setContractType(updatedOffre.getContractType());
            existingOffre.setWorkMode(updatedOffre.getWorkMode());
            existingOffre.setExperienceLevel(updatedOffre.getExperienceLevel());
            existingOffre.setStatus(updatedOffre.getStatus());
            existingOffre.setMinSalary(updatedOffre.getMinSalary());
            existingOffre.setMaxSalary(updatedOffre.getMaxSalary());

            // Mise à jour des responsabilités et qualifications
            if (updatedOffre.getResponsibilities() != null) {
                existingOffre.setResponsibilities(updatedOffre.getResponsibilities());
            }

            if (updatedOffre.getQualifications() != null) {
                existingOffre.setQualifications(updatedOffre.getQualifications());
            }

            // Mise à jour du recruteur si spécifié
            if (updatedOffre.getRecruteur() != null && updatedOffre.getRecruteur().getIdRecruteur() != null) {
                Recruteur recruteur = recruteurService.getRecruteurById(updatedOffre.getRecruteur().getIdRecruteur());
                if (recruteur != null) {
                    existingOffre.setRecruteur(recruteur);
                }
            }

            // Enregistrer les modifications
            offreService.saveOffre(existingOffre);

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    // Endpoint pour supprimer une offre
    @DeleteMapping("/jobs/{id}")
    @ResponseBody
    public Map<String, Object> deleteJob(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            offreService.deleteOffre(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Endpoint pour supprimer plusieurs offres
    @PostMapping("/jobs/batch-delete")
    @ResponseBody
    public Map<String, Object> batchDeleteJobs(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> ids = request.get("ids");
            if (ids != null && !ids.isEmpty()) {
                for (Long id : ids) {
                    offreService.deleteOffre(id);
                }
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("error", "Aucun ID fourni");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Endpoint pour changer le statut d'une offre
    @PostMapping("/jobs/{id}/status")
    @ResponseBody
    public Map<String, Object> changeJobStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                response.put("success", false);
                response.put("error", "Statut non fourni");
                return response;
            }

            Offre offre = offreService.getOffreById(id);
            if (offre == null) {
                response.put("success", false);
                response.put("error", "Offre non trouvée");
                return response;
            }

            offre.setStatus(newStatus);

            // Pour les offres activées, mettre à jour les dates
            if ("ACTIVE".equals(newStatus)) {
                offre.setPostedAt(LocalDateTime.now());
                offre.setExpiresAt(LocalDateTime.now().plusDays(30));
            }

            offreService.saveOffre(offre);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Endpoint pour exporter les offres en CSV
    @GetMapping("/jobs/export")
    public void exportJobs(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"offres.csv\"");

        List<Offre> offres = offreService.getAllOffres();

        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT
                .withHeader("ID", "Titre", "Entreprise", "Industrie", "Lieu", "Type", "Statut", "Publiée le"))) {

            for (Offre offre : offres) {
                csvPrinter.printRecord(
                        offre.getId(),
                        offre.getTitle(),
                        offre.getCompanyName(),
                        offre.getIndustry(),
                        offre.getLocation(),
                        offre.getContractType(),
                        offre.getStatus(),
                        offre.getPostedAt() != null ? offre.getPostedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-"
                );
            }
        }
    }

    /**
     * Méthodes pour la gestion des recruteurs
     */

    @GetMapping("/recruiters")
    public String showRecruiters(Model model,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) Long company,
                                 @RequestParam(required = false, defaultValue = "0") int page,
                                 @RequestParam(required = false, defaultValue = "10") int size) {

        // Récupérer tous les recruteurs
        List<Recruteur> recruteurs = recruteurService.getAllRecruteurs();

        // Récupérer toutes les entreprises pour le filtre
        List<Company> companies = companyService.getCompanies();

        // Appliquer les filtres si nécessaire
        if (search != null || status != null || company != null) {
            recruteurs = filterRecruiters(recruteurs, search, status, company);
        }

        // Calculer la pagination
        int totalRecruiters = recruteurs.size();
        int totalPages = (int) Math.ceil((double) totalRecruiters / size);

        // S'assurer que la page est dans les limites
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        // Sous-ensemble des recruteurs pour la page actuelle
        int start = page * size;
        int end = Math.min(start + size, totalRecruiters);

        List<Recruteur> pagedRecruiters = start < totalRecruiters ? recruteurs.subList(start, end) : new ArrayList<>();

        // Ajouter les données au modèle
        model.addAttribute("recruteurs", pagedRecruiters);
        model.addAttribute("companies", companies);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);

        // Ajouter les paramètres de filtre pour la pagination
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("company", company);

        if (company != null) {
            Optional<Company> selectedCompany = companyService.findById(company);
            selectedCompany.ifPresent(value -> model.addAttribute("selectedCompany", value));
        }

        model.addAttribute("activeTab", "recruiters");
        return "adminboard";
    }

    /**
     * Méthode utilitaire pour filtrer les recruteurs
     */
    private List<Recruteur> filterRecruiters(List<Recruteur> recruteurs, String search, String status, Long companyId) {
        List<Recruteur> filteredRecruiters = new ArrayList<>(recruteurs);

        // Filtre par recherche (nom, email, téléphone)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            filteredRecruiters = filteredRecruiters.stream()
                    .filter(r -> (r.getNom() != null && r.getNom().toLowerCase().contains(searchLower)) ||
                            (r.getPrenom() != null && r.getPrenom().toLowerCase().contains(searchLower)) ||
                            (r.getEmail() != null && r.getEmail().toLowerCase().contains(searchLower)) ||
                            (r.getTelephone() != null && r.getTelephone().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        // Filtre par statut
        if (status != null && !status.trim().isEmpty()) {
            filteredRecruiters = filteredRecruiters.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        // Filtre par entreprise
        if (companyId != null) {
            filteredRecruiters = filteredRecruiters.stream()
                    .filter(r -> r.getCompany() != null && r.getCompany().getId().equals(companyId))
                    .collect(Collectors.toList());
        }

        return filteredRecruiters;
    }

    /**
     * Endpoint pour récupérer les données d'un recruteur
     */
    @GetMapping("/recruteurs/{id}/data")
    @ResponseBody
    public Recruteur getRecruiterData(@PathVariable Long id) {
        return recruteurService.getRecruteurById(id);
    }

    /**
     * Endpoint pour ajouter un recruteur
     */
    @PostMapping("/recruteurs/add")
    @ResponseBody
    public Map<String, Object> addRecruiter(@RequestBody Recruteur recruteur) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Définir la date d'inscription si elle n'est pas définie
            if (recruteur.getDateInscription() == null) {
                recruteur.setDateInscription(LocalDate.now());
            }

            // Sauvegarder le recruteur
            Recruteur savedRecruiter = recruteurService.saveRecruteur(recruteur);

            response.put("success", true);
            response.put("recruteur", savedRecruiter);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Endpoint pour mettre à jour un recruteur
     */
    @PostMapping("/recruteurs/{id}/update")
    @ResponseBody
    public Map<String, Object> updateRecruiter(@PathVariable Long id, @RequestBody Recruteur updatedRecruiter) {
        Map<String, Object> response = new HashMap<>();

        try {
            Recruteur existingRecruiter = recruteurService.getRecruteurById(id);

            if (existingRecruiter != null) {
                // Mettre à jour les informations du recruteur
                if (updatedRecruiter.getNom() != null) {
                    existingRecruiter.setNom(updatedRecruiter.getNom());
                }
                if (updatedRecruiter.getPrenom() != null) {
                    existingRecruiter.setPrenom(updatedRecruiter.getPrenom());
                }
                if (updatedRecruiter.getEmail() != null) {
                    existingRecruiter.setEmail(updatedRecruiter.getEmail());
                }
                if (updatedRecruiter.getTelephone() != null) {
                    existingRecruiter.setTelephone(updatedRecruiter.getTelephone());
                }
                if (updatedRecruiter.getPoste() != null) {
                    existingRecruiter.setPoste(updatedRecruiter.getPoste());
                }
                if (updatedRecruiter.getStatus() != null) {
                    existingRecruiter.setStatus(updatedRecruiter.getStatus());
                }

                // Mettre à jour l'entreprise associée
                existingRecruiter.setCompany(updatedRecruiter.getCompany());

                // Sauvegarder le recruteur mis à jour
                Recruteur savedRecruiter = recruteurService.saveRecruteur(existingRecruiter);

                response.put("success", true);
                response.put("recruteur", savedRecruiter);
            } else {
                response.put("success", false);
                response.put("error", "Recruteur non trouvé");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Endpoint pour supprimer un recruteur
     */
    @DeleteMapping("/recruteurs/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteRecruiter(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            recruteurService.deleteRecruteur(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Endpoint pour supprimer plusieurs recruteurs
     */
    @PostMapping("/recruteurs/batch-delete")
    @ResponseBody
    public Map<String, Object> batchDeleteRecruiters(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Long> ids = request.get("ids");
            if (ids != null && !ids.isEmpty()) {
                for (Long id : ids) {
                    try {
                        recruteurService.deleteRecruteur(id);
                    } catch (Exception e) {
                        // Log error but continue
                        System.err.println("Erreur lors de la suppression du recruteur ID " + id + ": " + e.getMessage());
                    }
                }
                response.put("success", true);
            } else {
                response.put("success", false);
                response.put("error", "Aucun ID fourni");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Endpoint pour changer le statut d'un recruteur
     */
    @PostMapping("/recruteurs/{id}/toggle-status")
    @ResponseBody
    public Map<String, Object> toggleRecruiterStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String newStatus = request.get("status");
            if (newStatus == null || newStatus.isEmpty()) {
                response.put("success", false);
                response.put("error", "Statut non fourni");
                return response;
            }

            Recruteur recruteur = recruteurService.getRecruteurById(id);
            if (recruteur == null) {
                response.put("success", false);
                response.put("error", "Recruteur non trouvé");
                return response;
            }

            recruteur.setStatus(newStatus);
            recruteurService.saveRecruteur(recruteur);

            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Endpoint pour exporter les recruteurs en CSV
     */
    @GetMapping("/recruteurs/export")
    public void exportRecruiters(HttpServletResponse response,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) Long company) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"recruteurs.csv\"");

        // Récupérer tous les recruteurs
        List<Recruteur> recruteurs = recruteurService.getAllRecruteurs();

        // Appliquer les filtres si nécessaire
        if (search != null || status != null || company != null) {
            recruteurs = filterRecruiters(recruteurs, search, status, company);
        }

        // Définir les en-têtes CSV
        String[] headers = {
                "ID", "Nom", "Prénom", "Email", "Téléphone", "Entreprise", "Poste", "Statut", "Date d'inscription", "Nombre d'offres"
        };

        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT.withHeader(headers))) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Recruteur recruteur : recruteurs) {
                csvPrinter.printRecord(
                        recruteur.getIdRecruteur(),
                        recruteur.getNom() != null ? recruteur.getNom() : "",
                        recruteur.getPrenom() != null ? recruteur.getPrenom() : "",
                        recruteur.getEmail() != null ? recruteur.getEmail() : "",
                        recruteur.getTelephone() != null ? recruteur.getTelephone() : "",
                        recruteur.getCompany() != null ? recruteur.getCompany().getNomEntreprise() : "Sans entreprise",
                        recruteur.getPoste() != null ? recruteur.getPoste() : "",
                        recruteur.getStatus() != null ? recruteur.getStatus() : "Non défini",
                        recruteur.getDateInscription() != null ? recruteur.getDateInscription().format(formatter) : "",
                        recruteur.getOffres() != null ? recruteur.getOffres().size() : 0
                );
            }
        }
    }

    /**
     * Méthodes pour la gestion des demandes (candidatures)
     */

    @GetMapping("/applications")
    public String showAdminApplications(Model model,
                                        @RequestParam(required = false) String search,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) Long companyId,
                                        @RequestParam(required = false) String dateRange,
                                        @RequestParam(required = false, defaultValue = "0") int page,
                                        @RequestParam(required = false, defaultValue = "10") int size) {

        // Récupérer toutes les demandes
        List<Demande> demandes = demandeService.getAllDemandes();

        // Appliquer les filtres
        demandes = filterApplications(demandes, search, status, companyId, dateRange);

        // Calculer les statistiques
        long pendingCount = demandes.stream().filter(d -> "PENDING".equals(d.getEtat())).count();
        long interviewCount = demandes.stream().filter(d -> "INTERVIEW".equals(d.getEtat())).count();
        long shortlistedCount = demandes.stream().filter(d -> "SHORTLISTED".equals(d.getEtat())).count();
        long rejectedCount = demandes.stream().filter(d -> "REJECTED".equals(d.getEtat())).count();
        long reviewedCount = demandes.stream().filter(d -> "REVIEWED".equals(d.getEtat())).count();

        // Pagination (simple)
        int totalApplications = demandes.size();
        int totalPages = (int) Math.ceil((double) totalApplications / size);

        // S'assurer que la page est dans les limites
        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        // Sous-ensemble des demandes pour la page actuelle
        int start = page * size;
        int end = Math.min(start + size, totalApplications);

        List<Demande> pagedDemandes = start < totalApplications ? demandes.subList(start, end) : new ArrayList<>();

        // Extraire les entreprises uniques pour le filtre
        Set<Company> uniqueCompanies = new HashSet<>();
        for (Demande demande : demandes) {
            if (demande.getOffre() != null &&
                    demande.getOffre().getRecruteur() != null &&
                    demande.getOffre().getRecruteur().getCompany() != null) {
                uniqueCompanies.add(demande.getOffre().getRecruteur().getCompany());
            }
        }

        // Ajouter les données au modèle
        model.addAttribute("applications", pagedDemandes);
        model.addAttribute("uniqueCompanies", uniqueCompanies);
        model.addAttribute("totalCount", totalApplications);
        model.addAttribute("inProgressCount", interviewCount + reviewedCount);
        model.addAttribute("selectedCount", shortlistedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("reviewedCount", reviewedCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("pendingCount", pendingCount);

        // Ajouter les informations de pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);

        // Ajouter les paramètres de filtre pour la pagination
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("companyId", companyId);
        model.addAttribute("dateRange", dateRange);

        // Définir l'onglet actif
        model.addAttribute("activeTab", "applications");

        return "adminboard";
    }

    /**
     * Méthode pour mettre à jour le statut d'une candidature (API REST)
     */
    @PutMapping("/applications/{id}/status")
    @ResponseBody
    public Map<String, Object> updateApplicationStatus(@PathVariable Long id,
                                                       @RequestBody StatusUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Demande demande = demandeService.updateDemandeStatus(id, request.getStatus());

            // Si besoin d'envoyer un email
            if (request.isSendEmail() && demande.getCandidat() != null &&
                    demande.getCandidat().getEmail() != null && !demande.getCandidat().getEmail().isEmpty()) {

                // Ici, on simulerait l'envoi d'un email
                // Pour un projet réel, intégrer un service d'email
                System.out.println("Simuler l'envoi d'un email à " + demande.getCandidat().getEmail() +
                        " concernant le changement de statut de la candidature " + id +
                        " vers " + request.getStatus());
            }

            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Méthode pour mettre à jour le statut de plusieurs candidatures en masse
     */
    @PutMapping("/applications/bulk-status")
    @ResponseBody
    public Map<String, Object> bulkUpdateStatus(@RequestBody BulkStatusUpdateRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            int updatedCount = 0;
            List<Long> ids = request.getIds();

            if (ids == null || ids.isEmpty()) {
                response.put("success", false);
                response.put("error", "Aucune candidature sélectionnée");
                return response;
            }

            for (Long id : ids) {
                try {
                    Demande demande = demandeService.updateDemandeStatus(id, request.getStatus());
                    updatedCount++;

                    // Si besoin d'envoyer un email
                    if (request.isSendEmail() && demande.getCandidat() != null &&
                            demande.getCandidat().getEmail() != null && !demande.getCandidat().getEmail().isEmpty()) {

                        // Ici, on simulerait l'envoi d'un email
                        System.out.println("Simuler l'envoi d'un email à " + demande.getCandidat().getEmail() +
                                " concernant le changement de statut de la candidature " + id +
                                " vers " + request.getStatus());
                    }
                } catch (Exception e) {
                    // Continuer avec les autres candidatures même si une échoue
                    System.err.println("Erreur lors de la mise à jour de la candidature " + id + ": " + e.getMessage());
                }
            }

            response.put("success", true);
            response.put("updatedCount", updatedCount);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Méthode pour contacter un candidat
     */
    @PostMapping("/applications/{id}/contact")
    @ResponseBody
    public Map<String, Object> contactCandidate(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            String subject = request.get("subject");
            String message = request.get("message");

            if (email == null || email.isEmpty() || subject == null || subject.isEmpty() ||
                    message == null || message.isEmpty()) {
                response.put("success", false);
                response.put("error", "Email, sujet et message sont requis");
                return response;
            }

            // Ici, on simulerait l'envoi d'un email
            // Pour un projet réel, intégrer un service d'email
            System.out.println("Simuler l'envoi d'un email à " + email +
                    " avec le sujet: " + subject +
                    " et le message: " + message);

            response.put("success", true);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    /**
     * Méthode pour exporter les candidatures en CSV
     */
    @GetMapping("/applications/export")
    public void exportApplications(HttpServletResponse response,
                                   @RequestParam(required = false) String search,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) Long companyId,
                                   @RequestParam(required = false) String dateRange) throws IOException {

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"candidatures.csv\"");

        // Récupérer toutes les candidatures
        List<Demande> demandes = demandeService.getAllDemandes();

        // Appliquer les filtres
        demandes = filterApplications(demandes, search, status, companyId, dateRange);

        // Définir les en-têtes CSV
        String[] headers = {
                "ID", "Candidat", "Email", "Poste", "Entreprise", "Date de candidature", "Statut"
        };

        try (CSVPrinter csvPrinter = new CSVPrinter(response.getWriter(), CSVFormat.DEFAULT.withHeader(headers))) {
            for (Demande demande : demandes) {
                String candidatName = demande.getCandidat() != null ?
                        demande.getCandidat().getFirstName() + " " + demande.getCandidat().getLastName() : "N/A";

                String candidatEmail = demande.getCandidat() != null ?
                        demande.getCandidat().getEmail() : "N/A";

                String poste = demande.getOffre() != null ?
                        demande.getOffre().getTitle() : "N/A";

                String entreprise = demande.getOffre() != null && demande.getOffre().getCompanyName() != null ?
                        demande.getOffre().getCompanyName() : "N/A";

                String dateDemande = demande.getDateDemande() != null ?
                        new SimpleDateFormat("dd/MM/yyyy").format(demande.getDateDemande()) : "N/A";

                // Traduire le statut en français
                String statut;
                switch(demande.getEtat()) {
                    case "PENDING": statut = "En attente"; break;
                    case "INTERVIEW": statut = "Entretien"; break;
                    case "SHORTLISTED": statut = "Présélectionné"; break;
                    case "REJECTED": statut = "Rejeté"; break;
                    case "REVIEWED": statut = "Examiné"; break;
                    default: statut = demande.getEtat();
                }

                csvPrinter.printRecord(
                        demande.getIdDemande(),
                        candidatName,
                        candidatEmail,
                        poste,
                        entreprise,
                        dateDemande,
                        statut
                );
            }
        }
    }

    /**
     * Méthode utilitaire pour filtrer les candidatures
     */
    private List<Demande> filterApplications(List<Demande> demandes, String search, String status,
                                             Long companyId, String dateRange) {

        List<Demande> filteredDemandes = new ArrayList<>(demandes);

        // Filtre par recherche (nom du candidat, poste)
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            filteredDemandes = filteredDemandes.stream()
                    .filter(d -> {
                        // Recherche sur le nom du candidat
                        boolean matchCandidat = d.getCandidat() != null &&
                                ((d.getCandidat().getFirstName() != null &&
                                        d.getCandidat().getFirstName().toLowerCase().contains(searchLower)) ||
                                        (d.getCandidat().getLastName() != null &&
                                                d.getCandidat().getLastName().toLowerCase().contains(searchLower)));

                        // Recherche sur le titre du poste
                        boolean matchPoste = d.getOffre() != null &&
                                d.getOffre().getTitle() != null &&
                                d.getOffre().getTitle().toLowerCase().contains(searchLower);

                        return matchCandidat || matchPoste;
                    })
                    .collect(Collectors.toList());
        }

        // Filtre par statut
        if (status != null && !status.trim().isEmpty()) {
            filteredDemandes = filteredDemandes.stream()
                    .filter(d -> d.getEtat() != null && d.getEtat().equals(status))
                    .collect(Collectors.toList());
        }

        // Filtre par entreprise
        if (companyId != null) {
            filteredDemandes = filteredDemandes.stream()
                    .filter(d -> d.getOffre() != null &&
                            d.getOffre().getRecruteur() != null &&
                            d.getOffre().getRecruteur().getCompany() != null &&
                            d.getOffre().getRecruteur().getCompany().getId().equals(companyId))
                    .collect(Collectors.toList());
        }

        // Filtre par date
        if (dateRange != null && !dateRange.trim().isEmpty()) {
            Date now = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);

            Date startDate;

            switch (dateRange) {
                case "week":
                    // Cette semaine (7 derniers jours)
                    cal.add(Calendar.DAY_OF_MONTH, -7);
                    startDate = cal.getTime();
                    break;
                case "month":
                    // Ce mois (30 derniers jours)
                    cal.add(Calendar.DAY_OF_MONTH, -30);
                    startDate = cal.getTime();
                    break;
                case "quarter":
                    // Ce trimestre (90 derniers jours)
                    cal.add(Calendar.DAY_OF_MONTH, -90);
                    startDate = cal.getTime();
                    break;
                default:
                    startDate = null;
            }

            if (startDate != null) {
                final Date finalStartDate = startDate;
                filteredDemandes = filteredDemandes.stream()
                        .filter(d -> d.getDateDemande() != null && d.getDateDemande().after(finalStartDate))
                        .collect(Collectors.toList());
            }
        }

        return filteredDemandes;
    }

    @GetMapping("/applications/{id}/update-status")
    public String updateApplicationStatus(@PathVariable Long id, @RequestParam String status,
                                          RedirectAttributes redirectAttributes) {
        try {
            demandeService.updateDemandeStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Statut de la candidature mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour: " + e.getMessage());
        }
        return "redirect:/admin/applications";
    }

    /**
     * Endpoint pour récupérer la liste des recruteurs
     * @return Liste des recruteurs au format JSON
     */
    @GetMapping("/recruteurs/list")
    @ResponseBody
    public List<Map<String, Object>> getRecruteursList() {
        List<Recruteur> recruteurs = recruteurService.getAllRecruteurs();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Recruteur recruteur : recruteurs) {
            Map<String, Object> recruteurMap = new HashMap<>();
            recruteurMap.put("idRecruteur", recruteur.getIdRecruteur());

            String nomEntreprise = "Sans entreprise";
            if (recruteur.getCompany() != null && recruteur.getCompany().getNomEntreprise() != null) {
                nomEntreprise = recruteur.getCompany().getNomEntreprise();
            }

            recruteurMap.put("nomEntreprise", nomEntreprise);
            result.add(recruteurMap);
        }

        return result;
    }

    // Support classes for request handling
    public static class StatusUpdateRequest {
        private String status;
        private boolean sendEmail;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isSendEmail() {
            return sendEmail;
        }

        public void setSendEmail(boolean sendEmail) {
            this.sendEmail = sendEmail;
        }
    }

    public static class BulkStatusUpdateRequest {
        private List<Long> ids;
        private String status;
        private boolean sendEmail;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isSendEmail() {
            return sendEmail;
        }

        public void setSendEmail(boolean sendEmail) {
            this.sendEmail = sendEmail;
        }
    }
}