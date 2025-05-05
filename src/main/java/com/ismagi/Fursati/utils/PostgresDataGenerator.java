package com.ismagi.Fursati.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * PostgreSQL Test Data Generator for Fursati platform
 * This class handles generation and insertion of test data directly into the database
 */
@Component
public class PostgresDataGenerator {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    // Maximum length for VARCHAR(255) fields
    private static final int MAX_VARCHAR_LENGTH = 250; // Leaving some margin

    @Autowired
    public PostgresDataGenerator(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // Lists for generating realistic Moroccan data
    private final List<String> moroccanCities = Arrays.asList(
            "Casablanca", "Rabat", "Marrakech", "Fès", "Tanger", "Meknès",
            "Agadir", "Tétouan", "Oujda", "Kénitra", "Nador", "Mohammedia",
            "El Jadida", "Béni Mellal", "Témara", "Taza", "Khémisset"
    );

    private final List<String> moroccanSchools = Arrays.asList(
            "Université Mohammed V", "Université Hassan II", "Université Cadi Ayyad",
            "ENSIAS", "ENIM", "ENCG Casablanca", "ENCG Settat", "ENSA Marrakech",
            "EHTP", "INSEA", "ESITH", "INPT", "EMI", "FST Mohammedia",
            "ISEM", "ISCAE", "UIR", "UM6P", "FST Tanger"
    );

    private final List<String> moroccanCompanies = Arrays.asList(
            "Maroc Telecom", "Attijariwafa Bank", "Royal Air Maroc", "OCP Group",
            "BMCE Bank", "Marjane", "Inwi", "Cosumar", "Bank Al-Maghrib",
            "Centrale Danone", "Renault Maroc", "Lydec", "Lafarge Maroc",
            "Wafa Assurance", "Holmarcom", "CIH Bank", "Crédit Agricole du Maroc",
            "Société Générale Maroc", "SNI", "Label'Vie"
    );

    private final List<String> industries = Arrays.asList(
            "Automobile", "Aéronautique", "Technologies de l'Information", "Télécommunications",
            "Services Financiers", "Banque", "Assurance", "Agroalimentaire",
            "Textile", "Tourisme", "BTP", "Énergie", "Agriculture",
            "Pharmaceutique", "Offshoring", "Commerce"
    );

    private final List<String> contractTypes = Arrays.asList(
            "CDI", "CDD", "Stage", "Freelance", "Intérim", "ANAPEC"
    );

    private final List<String> workModes = Arrays.asList(
            "Présentiel", "Télétravail", "Hybride"
    );

    private final List<String> experienceLevels = Arrays.asList(
            "Débutant", "Junior (1-3 ans)", "Intermédiaire (3-5 ans)",
            "Confirmé (5-8 ans)", "Senior (8+ ans)"
    );

    private final List<String> languages = Arrays.asList(
            "Arabe", "Français", "Anglais", "Espagnol", "Allemand", "Amazigh"
    );

    private final List<String> proficiencyLevels = Arrays.asList(
            "Débutant", "Intermédiaire", "Avancé", "Bilingue", "Langue maternelle"
    );

    private final List<String> skills = Arrays.asList(
            "Java", "Spring Boot", "React", "Angular", "Python", "JavaScript",
            "Node.js", "PHP", "SAP", "Oracle", "SQL", "Microsoft Office",
            "Marketing Digital", "Community Management", "Comptabilité",
            "Finance", "Gestion de Projet", "PRINCE2", "PMP", "Agile",
            "Scrum", "Commerce", "Vente", "CSS", "HTML", "UI/UX",
            "AutoCAD", "Revit", "SolidWorks", "Photoshop", "Illustrator"
    );

    private final List<String> jobTitles = Arrays.asList(
            "Développeur Web", "Développeur Full Stack", "Développeur Back-End",
            "Ingénieur Logiciel", "Ingénieur DevOps", "Chef de Projet IT",
            "Comptable", "Auditeur Financier", "Contrôleur de Gestion",
            "Responsable Marketing", "Community Manager", "Responsable Commercial",
            "Délégué Médical", "Technicien de Maintenance", "Ingénieur Industriel",
            "Chef de Produit", "Responsable RH", "Architecte", "Designer Graphique",
            "Juriste d'Entreprise", "Analyste Financier", "Data Scientist"
    );

    private final List<String> degrees = Arrays.asList(
            "Licence", "Master", "Doctorat", "Bac+2", "Bac+3", "Bac+5",
            "Ingénieur d'État", "BTS", "DUT", "DEUG", "MBA", "DESS", "DEA"
    );

    private final List<String> fieldsOfStudy = Arrays.asList(
            "Informatique", "Génie Logiciel", "Réseaux et Télécommunications",
            "Génie Civil", "Électronique", "Mécanique", "Finance", "Marketing",
            "Gestion", "Commerce International", "Droit", "Sciences Économiques",
            "Médecine", "Pharmacie", "Agronomie", "Architecture"
    );

    private final List<String> firstNames = Arrays.asList(
            "Mohammed", "Youssef", "Ahmed", "Amine", "Hamza", "Ali", "Omar", "Ibrahim",
            "Ayman", "Zakaria", "Ilyas", "Reda", "Mehdi", "Karim", "Bilal", "Samir",
            "Fatima", "Maryam", "Sara", "Amal", "Salma", "Hajar", "Zineb", "Nada",
            "Sanaa", "Imane", "Houda", "Ghizlane", "Khadija", "Hanane", "Loubna", "Yasmine"
    );

    private final List<String> lastNames = Arrays.asList(
            "El Amrani", "Benjelloun", "Bouali", "Benkirane", "Benmoussa", "Alaoui", "Idrissi",
            "Berrada", "Tazi", "Bennis", "Chraibi", "Lahlou", "Fassi", "Sebti", "Ouazzani",
            "Harrak", "Bennani", "Tahiri", "Lamrani", "Mansouri", "Kadiri", "Belkadi", "Daoud"
    );

    private final List<String> emailDomains = Arrays.asList(
            "gmail.com", "outlook.com", "yahoo.fr", "hotmail.fr",
            "hotmail.com", "yahoo.com", "outlook.fr", "menara.ma", "iam.net.ma"
    );

    /**
     * Generates test data and inserts it into the database
     */
    @Transactional
    public void generateTestData(int adminCount, int recruiterCount, int candidateCount,
                                 int offersPerRecruiter, boolean clearExistingData) {

        if (clearExistingData) {
            clearAllData();
        }

        // Generate and insert admins
        List<Integer> adminIds = generateAdmins(adminCount);

        // Generate companies first (assuming 5 as a default)
        List<Long> companyIds = generateCompanies(5);

        // Generate and insert recruiters with company associations
        List<Long> recruiterIds = generateRecruiters(recruiterCount, companyIds);

        // Generate and insert job offers
        List<Long> offerIds = generateJobOffers(recruiterIds, offersPerRecruiter);

        // Generate and insert candidates
        List<Long> candidateIds = generateCandidates(candidateCount);

        // Generate and insert applications
        generateApplications(candidateIds, offerIds);

        // Generate and insert other entities
        generateInternautes(20);
        generateTracabilites(30);

        System.out.println("Test data generation completed successfully!");
    }

    /**
     * Clears all existing data from the database
     */
    private void clearAllData() {
        String[] truncateStatements = {
                "TRUNCATE demande CASCADE",
                "TRUNCATE experience CASCADE",
                "TRUNCATE education CASCADE",
                "TRUNCATE skill CASCADE",
                "TRUNCATE language CASCADE",
                "TRUNCATE professional_summary CASCADE",
                "TRUNCATE offre_responsibilities CASCADE",
                "TRUNCATE offre_qualifications CASCADE",
                "TRUNCATE offres CASCADE",
                "TRUNCATE recruteur CASCADE",
                "TRUNCATE candidat CASCADE",
                "TRUNCATE admin CASCADE",
                "TRUNCATE internaute CASCADE",
                "TRUNCATE tracabilite CASCADE"
        };

        for (String statement : truncateStatements) {
            try {
                jdbcTemplate.execute(statement);
            } catch (Exception e) {
                // Some tables might not exist yet, ignore errors
                System.out.println("Warning: Could not truncate table: " + e.getMessage());
            }
        }

        String[] resetSequences = {
                "ALTER SEQUENCE IF EXISTS admin_admin_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS recruteur_id_recruteur_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS offres_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS candidat_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS demande_id_demande_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS professional_summary_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS experience_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS education_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS skill_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS language_id_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS internaute_id_internaute_seq RESTART WITH 1",
                "ALTER SEQUENCE IF EXISTS tracabilite_id_archive_seq RESTART WITH 1"
        };

        for (String statement : resetSequences) {
            try {
                jdbcTemplate.execute(statement);
            } catch (Exception e) {
                // Some sequences might not exist, ignore errors
            }
        }

        System.out.println("Database cleared successfully!");
    }

    /**
     * Generates and inserts admin records
     */
    private List<Integer> generateAdmins(int count) {
        List<Integer> adminIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String firstName = getRandomItem(firstNames);
            String lastName = getRandomItem(lastNames);
            String email = limitLength(generateEmail(firstName, lastName), MAX_VARCHAR_LENGTH);
            String phone = generateMoroccanPhoneNumber();
            int age = 25 + random.nextInt(30); // 25-55 years old

            String sql = "INSERT INTO admin (nom, prenom, age, email, login, password, telephone) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING admin_id";

            Integer adminId = jdbcTemplate.queryForObject(sql, Integer.class,
                    limitLength(lastName, MAX_VARCHAR_LENGTH),
                    limitLength(firstName, MAX_VARCHAR_LENGTH),
                    age,
                    limitLength(email, MAX_VARCHAR_LENGTH),
                    limitLength(firstName.toLowerCase() + "." + lastName.toLowerCase(), MAX_VARCHAR_LENGTH),
                    limitLength(generatePassword(8), MAX_VARCHAR_LENGTH),
                    limitLength(phone, MAX_VARCHAR_LENGTH));

            if (adminId != null) {
                adminIds.add(adminId);
            }
        }

        System.out.println("Generated " + adminIds.size() + " admins");
        return adminIds;
    }

    /**
     * Generates and inserts recruiter records
     */
    /**
     * Generates and inserts recruiter records with company associations
     */
    private List<Long> generateRecruiters(int count, List<Long> companyIds) {
        List<Long> recruiterIds = new ArrayList<>();

        // If no companies are provided, create a default list with nulls
        if (companyIds == null || companyIds.isEmpty()) {
            for (int i = 0; i < count; i++) {
                companyIds.add(null);
            }
        }

        // Distribute recruiters among available companies
        for (int i = 0; i < count; i++) {
            String company = getRandomItem(moroccanCompanies);
            String sector = getRandomItem(industries);

            // Get company ID - either pick one from the list or null if none available
            Long companyId = null;
            if (!companyIds.isEmpty()) {
                // Assign recruiter to a random company from the list
                companyId = companyIds.get(i % companyIds.size());
            }

            String sql = "INSERT INTO recruteur (nom_entreprise, secteur, offres_publiees, company_id) " +
                    "VALUES (?, ?, '0', ?) RETURNING id_recruteur";

            Long recruiterId = jdbcTemplate.queryForObject(sql, Long.class,
                    limitLength(company, MAX_VARCHAR_LENGTH),
                    limitLength(sector, MAX_VARCHAR_LENGTH),
                    companyId);

            if (recruiterId != null) {
                recruiterIds.add(recruiterId);
            }
        }

        System.out.println("Generated " + recruiterIds.size() + " recruiters associated with companies");
        return recruiterIds;
    }

    /**
     * Generates and inserts job offers
     */
    private List<Long> generateJobOffers(List<Long> recruiterIds, int offersPerRecruiter) {
        List<Long> offerIds = new ArrayList<>();

        for (Long recruiterId : recruiterIds) {
            // Get recruiter details
            Map<String, Object> recruiter = jdbcTemplate.queryForMap(
                    "SELECT nom_entreprise, secteur FROM recruteur WHERE id_recruteur = ?",
                    recruiterId);

            String companyName = (String) recruiter.get("nom_entreprise");
            String industry = (String) recruiter.get("secteur");

            for (int i = 0; i < offersPerRecruiter; i++) {
                String title = getRandomItem(jobTitles);
                String description = limitLength(generateJobDescription(), MAX_VARCHAR_LENGTH);
                String location = getRandomItem(moroccanCities);
                String contractType = getRandomItem(contractTypes);
                String workMode = getRandomItem(workModes);
                String experienceLevel = getRandomItem(experienceLevels);

                // Salary range (in MAD)
                int minSalary = 4000 + random.nextInt(16000);
                int maxSalary = minSalary + 2000 + random.nextInt(15000);

                // Dates
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime postedAt = now.minusDays(random.nextInt(30));
                LocalDateTime expiresAt = now.plusDays(15 + random.nextInt(76)); // 15-90 days from now

                String logoUrl = limitLength("https://example.com/logos/" + (1 + random.nextInt(100)) + ".png", MAX_VARCHAR_LENGTH);
                String companyWebsite = limitLength("https://www." + companyName.toLowerCase().replace(" ", "") + ".ma", MAX_VARCHAR_LENGTH);
                String companySize = getRandomItem(Arrays.asList("1-50", "51-200", "201-500", "501-1000", "1000+"));
                String companyHeadquarters = getRandomItem(moroccanCities);
                String companyDescription = limitLength(generateCompanyDescription(companyName, industry), MAX_VARCHAR_LENGTH);

                String status = getRandomItem(Arrays.asList("ACTIVE", "DRAFT", "EXPIRED"));
                long views = random.nextInt(500);

                String sql = "INSERT INTO offres (title, description, company_name, location, industry, " +
                        "contract_type, work_mode, experience_level, min_salary, max_salary, " +
                        "posted_at, expires_at, logo_url, company_website, company_size, " +
                        "company_headquarters, company_description, status, views, recruteur_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                        "RETURNING id";

                Long offerId = jdbcTemplate.queryForObject(sql, Long.class,
                        limitLength(title, MAX_VARCHAR_LENGTH),
                        limitLength(description, MAX_VARCHAR_LENGTH),
                        limitLength(companyName, MAX_VARCHAR_LENGTH),
                        limitLength(location, MAX_VARCHAR_LENGTH),
                        limitLength(industry, MAX_VARCHAR_LENGTH),
                        limitLength(contractType, MAX_VARCHAR_LENGTH),
                        limitLength(workMode, MAX_VARCHAR_LENGTH),
                        limitLength(experienceLevel, MAX_VARCHAR_LENGTH),
                        minSalary, maxSalary,
                        postedAt, expiresAt,
                        limitLength(logoUrl, MAX_VARCHAR_LENGTH),
                        limitLength(companyWebsite, MAX_VARCHAR_LENGTH),
                        limitLength(companySize, MAX_VARCHAR_LENGTH),
                        limitLength(companyHeadquarters, MAX_VARCHAR_LENGTH),
                        limitLength(companyDescription, MAX_VARCHAR_LENGTH),
                        limitLength(status, MAX_VARCHAR_LENGTH),
                        views, recruiterId);

                if (offerId != null) {
                    offerIds.add(offerId);

                    // Add responsibilities (3-8)
                    int numResponsibilities = 3 + random.nextInt(6);
                    for (int r = 0; r < numResponsibilities; r++) {
                        jdbcTemplate.update(
                                "INSERT INTO offre_responsibilities (offre_id, responsibility) VALUES (?, ?)",
                                offerId, limitLength(generateResponsibility(title), MAX_VARCHAR_LENGTH));
                    }

                    // Add qualifications (3-8)
                    int numQualifications = 3 + random.nextInt(6);
                    for (int q = 0; q < numQualifications; q++) {
                        jdbcTemplate.update(
                                "INSERT INTO offre_qualifications (offre_id, qualification) VALUES (?, ?)",
                                offerId, limitLength(generateQualification(title), MAX_VARCHAR_LENGTH));
                    }
                }
            }

            // Update offres_publiees count
            jdbcTemplate.update(
                    "UPDATE recruteur SET offres_publiees = ? WHERE id_recruteur = ?",
                    String.valueOf(offersPerRecruiter), recruiterId);
        }

        System.out.println("Generated " + offerIds.size() + " job offers");
        return offerIds;
    }

    // Additional methods implementation...

    /**
     * Generates candidate records with related entities
     */
    private List<Long> generateCandidates(int count) {
        List<Long> candidateIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            boolean isMale = random.nextBoolean();
            String firstName = getRandomItem(isMale ?
                    firstNames.subList(0, firstNames.size() / 2) :
                    firstNames.subList(firstNames.size() / 2, firstNames.size()));
            String lastName = getRandomItem(lastNames);
            String email = generateEmail(firstName, lastName);
            String phone = generateMoroccanPhoneNumber();

            // Birthdate (22-45 years old)
            int age = 22 + random.nextInt(24); // 22-45 years old (calculated but not stored)
            LocalDate birthdate = LocalDate.now().minusYears(age).minusDays(random.nextInt(365));

            String address = "Rue " + (1 + random.nextInt(100)) + ", " + getRandomItem(moroccanCities);
            String profilePic = "https://example.com/profiles/" + (1 + random.nextInt(100)) + ".jpg";
            String summary = limitLength(generateProfessionalSummary(firstName), MAX_VARCHAR_LENGTH);

            // Updated SQL to match the actual database schema (without age field)
            String sql = "INSERT INTO candidat (first_name, last_name, email, phone, birthdate, " +
                    "address, profile_picture, summary) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

            Long candidateId = jdbcTemplate.queryForObject(sql, Long.class,
                    limitLength(firstName, MAX_VARCHAR_LENGTH),
                    limitLength(lastName, MAX_VARCHAR_LENGTH),
                    limitLength(email, MAX_VARCHAR_LENGTH),
                    limitLength(phone, MAX_VARCHAR_LENGTH),
                    birthdate,
                    limitLength(address, MAX_VARCHAR_LENGTH),
                    limitLength(profilePic, MAX_VARCHAR_LENGTH),
                    limitLength(summary, MAX_VARCHAR_LENGTH));

            if (candidateId != null) {
                candidateIds.add(candidateId);

                // Generate experiences
                generateExperiencesForCandidate(candidateId);

                // Generate education
                generateEducationForCandidate(candidateId);

                // Generate skills
                generateSkillsForCandidate(candidateId);

                // Generate languages
                generateLanguagesForCandidate(candidateId);

                // Generate professional summary
                jdbcTemplate.update(
                        "INSERT INTO professional_summary (summary, candidat_id) VALUES (?, ?)",
                        limitLength(summary, MAX_VARCHAR_LENGTH), candidateId);
            }
        }

        System.out.println("Generated " + candidateIds.size() + " candidates with their profiles");
        return candidateIds;
    }

    /**
     * Helper method to limit string length for VARCHAR fields
     */
    private String limitLength(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        return input.length() > maxLength ? input.substring(0, maxLength) : input;
    }

    // Rest of the methods...

    private void generateExperiencesForCandidate(Long candidateId) {
        int numExperiences = 1 + random.nextInt(4); // 1-5 experiences

        LocalDate currentDate = LocalDate.now();
        LocalDate startDate = currentDate.minusYears(1 + random.nextInt(10));

        for (int i = 0; i < numExperiences; i++) {
            String jobTitle = getRandomItem(jobTitles);
            String company = getRandomItem(moroccanCompanies);
            String location = getRandomItem(moroccanCities);

            // Determine if this is current job (only for the first experience)
            boolean isCurrentJob = i == 0 && random.nextBoolean();

            LocalDate endDate = null;
            if (!isCurrentJob) {
                // Job lasted 6 months to 3 years
                int jobMonths = 6 + random.nextInt(31);
                endDate = startDate.plusMonths(jobMonths);

                // Next job starts 0-6 months after the previous one ended
                startDate = endDate.plusMonths(random.nextInt(7));
            }

            String description = limitLength(generateJobExperienceDescription(jobTitle), MAX_VARCHAR_LENGTH);

            jdbcTemplate.update(
                    "INSERT INTO experience (job_title, company, location, start_date, end_date, " +
                            "current_job, description, candidat_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    limitLength(jobTitle, MAX_VARCHAR_LENGTH),
                    limitLength(company, MAX_VARCHAR_LENGTH),
                    limitLength(location, MAX_VARCHAR_LENGTH),
                    startDate, endDate, isCurrentJob,
                    limitLength(description, MAX_VARCHAR_LENGTH),
                    candidateId);
        }
    }

    private void generateEducationForCandidate(Long candidateId) {
        // Each candidate has 1-3 education entries
        int numEducations = 1 + random.nextInt(3);

        // Current year
        int currentYear = LocalDate.now().getYear();

        // Start with bachelor's degree (typically completed around age 22-23)
        int endYear = currentYear - 1 - random.nextInt(8); // 1-8 years ago

        for (int i = 0; i < numEducations; i++) {
            String degree = getRandomItem(degrees);
            String school = getRandomItem(moroccanSchools);
            String location = getRandomItem(moroccanCities);
            String fieldOfStudy = getRandomItem(fieldsOfStudy);

            // Determine duration based on degree type (approximate)
            int duration;
            if (degree.contains("Bac+2") || degree.contains("BTS") || degree.contains("DUT")) {
                duration = 2;
            } else if (degree.contains("Licence") || degree.contains("Bac+3")) {
                duration = 3;
            } else if (degree.contains("Master") || degree.contains("Bac+5") || degree.contains("Ingénieur")) {
                duration = 5;
            } else if (degree.contains("Doctorat")) {
                duration = 3 + random.nextInt(4); // 3-6 years
            } else {
                duration = 2 + random.nextInt(4); // Default 2-5 years
            }

            int startYear = endYear - duration;

            jdbcTemplate.update(
                    "INSERT INTO education (degree, school, school_location, start_year, end_year, " +
                            "field_of_study, candidat_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    limitLength(degree, MAX_VARCHAR_LENGTH),
                    limitLength(school, MAX_VARCHAR_LENGTH),
                    limitLength(location, MAX_VARCHAR_LENGTH),
                    startYear, endYear,
                    limitLength(fieldOfStudy, MAX_VARCHAR_LENGTH),
                    candidateId);

            // Next education entry would be before this one (earlier in time)
            // Gap between degrees (0-3 years)
            int gap = random.nextInt(4);
            endYear = startYear - gap;
        }
    }

    private void generateSkillsForCandidate(Long candidateId) {
        // Each candidate has 4-8 skills
        int numSkills = 4 + random.nextInt(5);

        // Get a subset of unique skills
        List<String> candidateSkills = getRandomUniqueItems(skills, numSkills);

        String[] proficiencies = {"Débutant", "Intermédiaire", "Avancé", "Expert"};

        for (String skill : candidateSkills) {
            String proficiency = proficiencies[random.nextInt(proficiencies.length)];

            jdbcTemplate.update(
                    "INSERT INTO skill (name, proficiency, candidat_id) VALUES (?, ?, ?)",
                    limitLength(skill, MAX_VARCHAR_LENGTH),
                    limitLength(proficiency, MAX_VARCHAR_LENGTH),
                    candidateId);
        }
    }

    private void generateLanguagesForCandidate(Long candidateId) {
        // Each candidate knows 2-4 languages
        int numLanguages = 2 + random.nextInt(3);

        // Get a subset of unique languages
        List<String> candidateLanguages = getRandomUniqueItems(languages, numLanguages);

        for (String language : candidateLanguages) {
            String proficiency = getRandomItem(proficiencyLevels);

            // Convert proficiency level to a numeric value (0-100)
            int proficiencyValue;
            switch (proficiency) {
                case "Débutant":
                    proficiencyValue = 20 + random.nextInt(20); // 20-39
                    break;
                case "Intermédiaire":
                    proficiencyValue = 40 + random.nextInt(20); // 40-59
                    break;
                case "Avancé":
                    proficiencyValue = 60 + random.nextInt(20); // 60-79
                    break;
                case "Bilingue":
                    proficiencyValue = 80 + random.nextInt(10); // 80-89
                    break;
                case "Langue maternelle":
                    proficiencyValue = 90 + random.nextInt(11); // 90-100
                    break;
                default:
                    proficiencyValue = 50; // Default middle value
            }

            jdbcTemplate.update(
                    "INSERT INTO language (name, proficiency, proficiency_level, candidat_id) VALUES (?, ?, ?, ?)",
                    limitLength(language, MAX_VARCHAR_LENGTH),
                    limitLength(proficiency, MAX_VARCHAR_LENGTH),
                    proficiencyValue,
                    candidateId);
        }
    }

    private void generateApplications(List<Long> candidateIds, List<Long> offerIds) {
        // Each candidate applies to 1-5 job offers
        for (Long candidateId : candidateIds) {
            // Determine how many applications this candidate will submit
            int numApplications = 1 + random.nextInt(5);

            // Get a subset of random offers for this candidate to apply to
            List<Long> selectedOfferIds = getRandomUniqueItems(offerIds, numApplications);

            for (Long offerId : selectedOfferIds) {
                // Generate a random application date (within the last 30 days)
                Date applicationDate = new Date(System.currentTimeMillis() -
                        random.nextInt(30) * 24 * 60 * 60 * 1000L);

                // Generate a random status for the application
                String status = getRandomItem(Arrays.asList(
                        "PENDING", "REVIEWED", "SHORTLISTED", "INTERVIEW", "REJECTED", "ACCEPTED"
                ));

                // Insert the application into the demande table
                jdbcTemplate.update(
                        "INSERT INTO demande (date_demande, etat, candidat_id, offre_id) VALUES (?, ?, ?, ?)",
                        applicationDate,
                        limitLength(status, MAX_VARCHAR_LENGTH),
                        candidateId,
                        offerId
                );
            }
        }

        // Log the total number of applications generated
        int applicationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM demande", Integer.class);
        System.out.println("Generated " + applicationCount + " job applications");
    }
    private String generateAddress() {
        return random.nextInt(100) + " Rue " + getRandomItem(lastNames) + ", " + getRandomItem(moroccanCities);
    }
    private List<Long> generateCompanies(int count) {
        List<Long> companyIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String companyName = getRandomItem(moroccanCompanies);
            String sector = getRandomItem(industries);
            String website = "www." + companyName.toLowerCase().replaceAll("\\s+", "") + ".com";
            String contactEmail = "contact@" + companyName.toLowerCase().replaceAll("\\s+", "") + ".com";
            String description = generateCompanyDescription(companyName, sector);
            Integer foundingYear = 1950 + random.nextInt(74);
            String companySize = getRandomItem(Arrays.asList("1-50", "51-200", "201-500", "501-1000", "1000+"));
            String city = getRandomItem(moroccanCities);
            String address = generateAddress();
            String country = "Maroc";
            String postalCode = String.format("%05d", random.nextInt(100000));
            String phone = generateMoroccanPhoneNumber();
            String businessRegistrationNumber = "RC-" + String.format("%06d", random.nextInt(1000000));
            String logoUrl = "https://example.com/logos/" + companyName.toLowerCase().replaceAll("\\s+", "") + ".png";
            String linkedinUrl = "https://www.linkedin.com/company/" + companyName.toLowerCase().replaceAll("\\s+", "");
            String twitterUrl = "https://twitter.com/" + companyName.toLowerCase().replaceAll("\\s+", "");
            String facebookUrl = "https://www.facebook.com/" + companyName.toLowerCase().replaceAll("\\s+", "");
            String instagramUrl = "https://www.instagram.com/" + companyName.toLowerCase().replaceAll("\\s+", "");

            String sql = "INSERT INTO companies (company_name, sector, website, contact_email, description, " +
                    "founding_year, company_size, address, city, country, postal_code, phone, " +
                    "business_registration_number, logo_url, linkedin_url, twitter_url, facebook_url, instagram_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

            Long companyId = jdbcTemplate.queryForObject(sql, Long.class,
                    limitLength(companyName, MAX_VARCHAR_LENGTH),
                    limitLength(sector, MAX_VARCHAR_LENGTH),
                    limitLength(website, MAX_VARCHAR_LENGTH),
                    limitLength(contactEmail, MAX_VARCHAR_LENGTH),
                    limitLength(description, 4000),
                    foundingYear,
                    limitLength(companySize, MAX_VARCHAR_LENGTH),
                    limitLength(address, MAX_VARCHAR_LENGTH),
                    limitLength(city, MAX_VARCHAR_LENGTH),
                    limitLength(country, MAX_VARCHAR_LENGTH),
                    limitLength(postalCode, MAX_VARCHAR_LENGTH),
                    limitLength(phone, MAX_VARCHAR_LENGTH),
                    limitLength(businessRegistrationNumber, MAX_VARCHAR_LENGTH),
                    limitLength(logoUrl, MAX_VARCHAR_LENGTH),
                    limitLength(linkedinUrl, MAX_VARCHAR_LENGTH),
                    limitLength(twitterUrl, MAX_VARCHAR_LENGTH),
                    limitLength(facebookUrl, MAX_VARCHAR_LENGTH),
                    limitLength(instagramUrl, MAX_VARCHAR_LENGTH));

            if (companyId != null) {
                companyIds.add(companyId);
            }
        }

        System.out.println("Generated " + companyIds.size() + " companies");
        return companyIds;
    }
    @Transactional
    public void generateTestData(int adminCount, int recruiterCount, int candidateCount,
                                 int offersPerRecruiter, int companyCount, boolean clearExistingData) {

        if (clearExistingData) {
            clearAllData();
        }

        // Generate admins
        List<Integer> adminIds = generateAdmins(adminCount);

        // Generate companies first and get their IDs
        List<Long> companyIds = generateCompanies(companyCount);

        // Generate recruiters and link them to companies
        List<Long> recruiterIds = generateRecruiters(recruiterCount, companyIds);

        // Generate job offers
        List<Long> offerIds = generateJobOffers(recruiterIds, offersPerRecruiter);

        // Generate candidates
        List<Long> candidateIds = generateCandidates(candidateCount);

        // Generate applications
        generateApplications(candidateIds, offerIds);

        // Generate other entities
        generateInternautes(20);
        generateTracabilites(30);

        System.out.println("Test data generation completed successfully!");
    }

    private void generateInternautes(int count) {
        // Implementation with limitLength applied to string fields
    }

    private void generateTracabilites(int count) {
        // Implementation with limitLength applied to string fields
    }

    // Helper methods

    private <T> T getRandomItem(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private <T> List<T> getRandomUniqueItems(List<T> list, int count) {
        count = Math.min(count, list.size());
        List<T> shuffled = new ArrayList<>(list);
        Collections.shuffle(shuffled);
        return shuffled.subList(0, count);
    }

    private String generateMoroccanPhoneNumber() {
        // Moroccan mobile prefixes
        String[] mobilePrefixes = {"06", "07"};
        String prefix = mobilePrefixes[random.nextInt(mobilePrefixes.length)];

        // Generate 8 random digits
        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    private String generateEmail(String firstName, String lastName) {
        // Normalize first and last name (remove accents, etc.)
        String normalizedFirstName = firstName.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ç]", "c");

        String normalizedLastName = lastName.toLowerCase()
                .replaceAll("[àáâãäå]", "a")
                .replaceAll("[èéêë]", "e")
                .replaceAll("[ìíîï]", "i")
                .replaceAll("[òóôõö]", "o")
                .replaceAll("[ùúûü]", "u")
                .replaceAll("[ç]", "c");

        // Choose a random email format
        String[] formats = {
                normalizedFirstName + "." + normalizedLastName,
                normalizedFirstName + normalizedLastName,
                normalizedFirstName + "." + normalizedLastName.charAt(0),
                normalizedFirstName.charAt(0) + "." + normalizedLastName,
                normalizedFirstName + "_" + normalizedLastName
        };

        return formats[random.nextInt(formats.length)] + "@" + getRandomItem(emailDomains);
    }

    private String generatePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    // Shorter versions of description generators
    private String generateJobDescription() {
        return "Nous recherchons un(e) professionnel(le) talentueux(se) pour rejoindre notre équipe. " +
                "Vous serez responsable de développer des solutions innovantes. " +
                "Une expérience dans le secteur est souhaitable.";
    }

    private String generateCompanyDescription(String company, String industry) {
        return company + " est une entreprise leader dans le secteur " + industry + " au Maroc. " +
                "Fondée en " + (1980 + random.nextInt(35)) + ", notre entreprise s'est engagée à " +
                "fournir des produits/services de haute qualité.";
    }

    private String generateResponsibility(String jobTitle) {
        String[] responsibilities = {
                "Développer et maintenir des applications",
                "Gérer une équipe de professionnels",
                "Assurer la qualité des projets",
                "Collaborer avec les parties prenantes",
                "Analyser les données et rapports",
                "Participer aux réunions de planification",
                "Optimiser les processus existants",
                "Résoudre les problèmes techniques",
                "Former les nouveaux membres",
                "Assurer la veille technologique",
                "Contribuer à l'amélioration continue",
                "Élaborer des stratégies innovantes",
                "Gérer les relations clients",
                "Suivre les tendances du marché"
        };

        return getRandomItem(Arrays.asList(responsibilities));
    }

    private String generateQualification(String jobTitle) {
        String[] qualifications = {
                "Diplôme en " + getRandomItem(fieldsOfStudy),
                "Expérience de " + (1 + random.nextInt(8)) + " ans",
                "Maîtrise de " + getRandomItem(skills),
                "Excellentes compétences en communication",
                "Capacité à travailler en équipe",
                "Esprit d'analyse et de synthèse",
                "Rigueur et organisation",
                "Maîtrise du français et de l'arabe",
                "Capacité d'adaptation rapide",
                "Orientation résultats",
                "Sens de l'initiative et autonomie",
                "Bonne gestion du stress"
        };

        return getRandomItem(Arrays.asList(qualifications));
    }

    private String generateProfessionalSummary(String firstName) {
        int years = 1 + random.nextInt(15);
        String industry = getRandomItem(industries);
        String skill1 = getRandomItem(skills);
        String skill2 = getRandomItem(skills);

        return "Professionnel avec " + years + " ans d'expérience dans " + industry +
                ". Spécialisé en " + skill1 + " et " + skill2 + ". À la recherche de " +
                "nouveaux défis professionnels.";
    }

    private String generateJobExperienceDescription(String jobTitle) {
        return "Responsable de développement de projets et gestion d'équipe. " +
                "A contribué à l'amélioration des processus. " +
                "A travaillé sur plusieurs projets majeurs.";
    }
}