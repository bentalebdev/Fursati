package com.ismagi.Fursati.service;

import com.ismagi.Fursati.entity.Company;
import com.ismagi.Fursati.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepository;

    public Optional<Company> findById(Long id) {
        return  companyRepository.findById(id);
    }
    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public Company getComapnyByRecruteurId(Long RecruitId) {
        return companyRepository.getCompanyByRecruitId(RecruitId);
    }

    /**
     * Get all companies
     */
    public List<Company> getCompanies() {
        return companyRepository.findAll();
    }

    /**
     * Get filtered companies based on search criteria
     */
    /**
     * Get filtered companies based on search criteria using more efficient database queries
     */
    public List<Company> getFilteredCompanies(String search, Boolean isVerified,
                                              String sector, String city, String companySize) {
        List<Company> filteredCompanies;

        // If no filters applied, return all companies
        if ((search == null || search.trim().isEmpty()) &&
                isVerified == null &&
                (sector == null || sector.trim().isEmpty()) &&
                (city == null || city.trim().isEmpty()) &&
                (companySize == null || companySize.trim().isEmpty())) {
            return companyRepository.findAll();
        }

        // Start with all companies or filtered subset
        if (search != null && !search.trim().isEmpty()) {
            // If search term is present, start with search results
            filteredCompanies = companyRepository.searchCompanies(search.trim());
        } else {
            // Otherwise start with all companies
            filteredCompanies = companyRepository.findAll();
        }

        // Apply additional filters in memory (could be optimized with a custom repository method if needed)
        // Filter by verification status
        if (isVerified != null) {
            filteredCompanies = filteredCompanies.stream()
                    .filter(c -> c.getIsVerified() != null && c.getIsVerified().equals(isVerified))
                    .collect(Collectors.toList());
        }

        // Filter by sector
        if (sector != null && !sector.trim().isEmpty()) {
            filteredCompanies = filteredCompanies.stream()
                    .filter(c -> c.getSecteur() != null && c.getSecteur().equals(sector))
                    .collect(Collectors.toList());
        }

        // Filter by city
        if (city != null && !city.trim().isEmpty()) {
            filteredCompanies = filteredCompanies.stream()
                    .filter(c -> c.getVille() != null && c.getVille().equals(city))
                    .collect(Collectors.toList());
        }

        // Filter by company size
        if (companySize != null && !companySize.trim().isEmpty()) {
            filteredCompanies = filteredCompanies.stream()
                    .filter(c -> c.getTailleEntreprise() != null && c.getTailleEntreprise().equals(companySize))
                    .collect(Collectors.toList());
        }

        return filteredCompanies;
    }

    /**
     * Get a list of all distinct sectors
     */
    public List<String> getAllSectors() {
        return companyRepository.findDistinctSectors();
    }

    /**
     * Get a list of all distinct cities
     */
    public List<String> getAllCities() {
        return companyRepository.findDistinctCities();
    }
    /**
     * Toggle company verification status
     */
    public Company toggleVerificationStatus(Long companyId, boolean newStatus) {
        Optional<Company> companyOpt = companyRepository.findById(companyId);
        if (companyOpt.isPresent()) {
            Company company = companyOpt.get();
            company.setIsVerified(newStatus);
            return companyRepository.save(company);
        }
        return null;
    }

    /**
     * Delete a company by ID
     */
    public boolean deleteCompany(Long companyId) {
        try {
            companyRepository.deleteById(companyId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Delete multiple companies by IDs
     */
    public boolean deleteCompanies(List<Long> companyIds) {
        try {
            companyIds.forEach(companyRepository::deleteById);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Updates an existing company with new information
     * @param companyId ID of the company to update
     * @param updatedCompany Company object containing updated information
     * @return Updated company or null if company not found
     */
    public Company updateCompany(Long companyId, Company updatedCompany) {
        Optional<Company> existingCompany = companyRepository.findById(companyId);

        if (existingCompany.isPresent()) {
            Company company = existingCompany.get();

            // Update company fields
            if (updatedCompany.getNomEntreprise() != null) {
                company.setNomEntreprise(updatedCompany.getNomEntreprise());
            }
            if (updatedCompany.getSecteur() != null) {
                company.setSecteur(updatedCompany.getSecteur());
            }
            if (updatedCompany.getTailleEntreprise() != null) {
                company.setTailleEntreprise(updatedCompany.getTailleEntreprise());
            }
            if (updatedCompany.getAnneeCreation() != null) {
                company.setAnneeCreation(updatedCompany.getAnneeCreation());
            }
            if (updatedCompany.getSiteWeb() != null) {
                company.setSiteWeb(updatedCompany.getSiteWeb());
            }
            if (updatedCompany.getEmailContact() != null) {
                company.setEmailContact(updatedCompany.getEmailContact());
            }
            if (updatedCompany.getDescription() != null) {
                company.setDescription(updatedCompany.getDescription());
            }
            if (updatedCompany.getAdresse() != null) {
                company.setAdresse(updatedCompany.getAdresse());
            }
            if (updatedCompany.getVille() != null) {
                company.setVille(updatedCompany.getVille());
            }
            if (updatedCompany.getPays() != null) {
                company.setPays(updatedCompany.getPays());
            }
            if (updatedCompany.getCodePostal() != null) {
                company.setCodePostal(updatedCompany.getCodePostal());
            }
            if (updatedCompany.getTelephone() != null) {
                company.setTelephone(updatedCompany.getTelephone());
            }
            if (updatedCompany.getNumeroRC() != null) {
                company.setNumeroRC(updatedCompany.getNumeroRC());
            }
            if (updatedCompany.getLinkedinUrl() != null) {
                company.setLinkedinUrl(updatedCompany.getLinkedinUrl());
            }
            if (updatedCompany.getTwitterUrl() != null) {
                company.setTwitterUrl(updatedCompany.getTwitterUrl());
            }
            if (updatedCompany.getFacebookUrl() != null) {
                company.setFacebookUrl(updatedCompany.getFacebookUrl());
            }
            if (updatedCompany.getInstagramUrl() != null) {
                company.setInstagramUrl(updatedCompany.getInstagramUrl());
            }
            if (updatedCompany.getValeurs() != null) {
                company.setValeurs(updatedCompany.getValeurs());
            }
            if (updatedCompany.getLogoUrl() != null) {
                company.setLogoUrl(updatedCompany.getLogoUrl());
            }
            if (updatedCompany.getIsVerified() != null) {
                company.setIsVerified(updatedCompany.getIsVerified());
            }

            return companyRepository.save(company);
        }

        return null;
    }

    /**
     * Updates a company profile based on the recruiter ID
     * @param recruitId ID of the recruiter
     * @param updatedCompany Company object containing updated information
     * @return Updated company or null if company not found
     */
    public Company updateCompanyByRecruitId(Long recruitId, Company updatedCompany) {
        Company existingCompany = companyRepository.getCompanyByRecruitId(recruitId);

        if (existingCompany != null) {
            // Update company fields - same as above method
            if (updatedCompany.getNomEntreprise() != null) {
                existingCompany.setNomEntreprise(updatedCompany.getNomEntreprise());
            }
            if (updatedCompany.getSecteur() != null) {
                existingCompany.setSecteur(updatedCompany.getSecteur());
            }
            if (updatedCompany.getTailleEntreprise() != null) {
                existingCompany.setTailleEntreprise(updatedCompany.getTailleEntreprise());
            }
            if (updatedCompany.getAnneeCreation() != null) {
                existingCompany.setAnneeCreation(updatedCompany.getAnneeCreation());
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
            if (updatedCompany.getNumeroRC() != null) {
                existingCompany.setNumeroRC(updatedCompany.getNumeroRC());
            }
            if (updatedCompany.getLinkedinUrl() != null) {
                existingCompany.setLinkedinUrl(updatedCompany.getLinkedinUrl());
            }
            if (updatedCompany.getTwitterUrl() != null) {
                existingCompany.setTwitterUrl(updatedCompany.getTwitterUrl());
            }
            if (updatedCompany.getFacebookUrl() != null) {
                existingCompany.setFacebookUrl(updatedCompany.getFacebookUrl());
            }
            if (updatedCompany.getInstagramUrl() != null) {
                existingCompany.setInstagramUrl(updatedCompany.getInstagramUrl());
            }
            if (updatedCompany.getValeurs() != null) {
                existingCompany.setValeurs(updatedCompany.getValeurs());
            }
            if (updatedCompany.getLogoUrl() != null) {
                existingCompany.setLogoUrl(updatedCompany.getLogoUrl());
            }
            if (updatedCompany.getIsVerified() != null) {
                existingCompany.setIsVerified(updatedCompany.getIsVerified());
            }

            return companyRepository.save(existingCompany);
        }

        return null;
    }

    /**
     * Update company logo
     * @param companyId ID of the company
     * @param logoUrl URL of the uploaded logo
     * @return Updated company or null if company not found
     */
    public Company updateCompanyLogo(Long companyId, String logoUrl) {
        Optional<Company> existingCompany = companyRepository.findById(companyId);

        if (existingCompany.isPresent()) {
            Company company = existingCompany.get();
            company.setLogoUrl(logoUrl);
            return companyRepository.save(company);
        }

        return null;
    }
}