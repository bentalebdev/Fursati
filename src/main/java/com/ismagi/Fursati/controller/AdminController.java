package com.ismagi.Fursati.controller;

import com.ismagi.Fursati.entity.Admin;
import com.ismagi.Fursati.entity.Company;
import com.ismagi.Fursati.entity.Offre;
import com.ismagi.Fursati.service.AdminService;
import com.ismagi.Fursati.service.CompanyService;
import com.ismagi.Fursati.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller // Changed from @RestController
@RequestMapping("/admin") // Changed from "/admins"
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private OffreService offreService;
    @Autowired
    private CompanyService companyService;


    @GetMapping
    public String showAdmin(Model model) {
        model.addAttribute("activeTab", "dashboard");
        return "adminboard";
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("activeTab", "dashboard");
        // Add dashboard data...
        return "adminboard";
    }

    @GetMapping("/users")
    public String showAdminUsers(Model model) {
        model.addAttribute("activeTab", "users");
        // Add users data...
        return "adminboard";
    }

    @GetMapping("/companies")
    public String showAdminCompanies(Model model) {
        List<Company> companyList=companyService.getCompanies();
        model.addAttribute("companies", companyList);
        model.addAttribute("activeTab", "companies");
        return "adminboard";
    }
    @GetMapping("/jobs")
    public String showAdminJobs(Model model) {
       List< Offre> offres =  offreService.getAllOffres();
        model.addAttribute("jobs", offres);
        model.addAttribute("activeTab", "jobs");

        return "adminboard";
    }
}