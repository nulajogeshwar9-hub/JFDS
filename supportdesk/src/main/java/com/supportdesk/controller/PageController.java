package com.supportdesk.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/register.html";
    }

    // Customer Views
    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "forward:/customer/dashboard.html";
    }

    @GetMapping("/customer/tickets")
    public String customerTickets() {
        return "forward:/customer/tickets.html";
    }

    @GetMapping("/customer/tickets/new")
    public String customerNewTicket() {
        return "forward:/customer/ticket-create.html";
    }

    @GetMapping("/customer/tickets/{id}")
    public String customerTicketDetail(@PathVariable Long id) {
        return "forward:/customer/ticket-detail.html";
    }

    @GetMapping("/customer/profile")
    public String customerProfile() {
        return "forward:/customer/profile.html";
    }

    // Agent Views
    @GetMapping("/agent/dashboard")
    public String agentDashboard() {
        return "forward:/agent/dashboard.html";
    }

    @GetMapping("/agent/tickets")
    public String agentTickets() {
        return "forward:/agent/tickets.html";
    }

    @GetMapping("/agent/tickets/{id}")
    public String agentTicketDetail(@PathVariable Long id) {
        return "forward:/agent/ticket-detail.html";
    }

    // Admin Views
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "forward:/admin/dashboard.html";
    }

    @GetMapping("/admin/tickets")
    public String adminTickets() {
        return "forward:/admin/tickets.html";
    }

    @GetMapping("/admin/tickets/{id}")
    public String adminTicketDetail(@PathVariable Long id) {
        return "forward:/admin/ticket-detail.html";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "forward:/admin/users.html";
    }

    @GetMapping("/admin/teams")
    public String adminTeams() {
        return "forward:/admin/teams.html";
    }

    @GetMapping("/admin/categories")
    public String adminCategories() {
        return "forward:/admin/categories.html";
    }

    @GetMapping("/admin/sla")
    public String adminSla() {
        return "forward:/admin/sla.html";
    }

    @GetMapping("/admin/rules")
    public String adminRules() {
        return "forward:/admin/assignment-rules.html";
    }

    @GetMapping("/admin/reports")
    public String adminReports() {
        return "forward:/admin/reports.html";
    }

    @GetMapping("/admin/settings")
    public String adminSettings() {
        return "forward:/admin/settings.html";
    }
}

