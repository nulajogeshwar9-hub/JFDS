import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches

output_path = r'J:\JFDS\supportdesk\figures\SupportDesk_System_Architecture.png'
os.makedirs(os.path.dirname(output_path), exist_ok=True)

# Create figure with high DPI and 16:9 widescreen layout
fig, ax = plt.subplots(figsize=(16, 9.5), dpi=300)
ax.set_xlim(0, 16)
ax.set_ylim(0, 10)
ax.axis('off')

# Background
bg = patches.Rectangle((0, 0), 16, 10, facecolor='#f8fafc', edgecolor='none')
ax.add_patch(bg)

# Main Title Header
title_box = patches.FancyBboxPatch((0.5, 9.1), 15.0, 0.75, boxstyle="round,pad=0.08", fc='#0f172a', ec='#1e293b', lw=1.5)
ax.add_patch(title_box)
ax.text(8.0, 9.55, 'SupportDesk — Multi-Domain Enterprise System Architecture', ha='center', va='center', fontsize=16, fontweight='bold', color='#ffffff')
ax.text(8.0, 9.25, 'Three-Tier Architecture: Presentation, Spring Boot 3 Application Services, and Relational Persistence', ha='center', va='center', fontsize=10, color='#94a3b8')

# Helper for drawing rounded group panels
def draw_layer_card(x, y, w, h, title, subtitle, header_color, bg_color, border_color):
    # Main body
    body = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.1", fc=bg_color, ec=border_color, lw=1.8)
    ax.add_patch(body)
    # Header strip
    hdr = patches.FancyBboxPatch((x, y + h - 0.55), w, 0.55, boxstyle="round,pad=0.05", fc=header_color, ec=border_color, lw=1.2)
    ax.add_patch(hdr)
    ax.text(x + 0.3, y + h - 0.28, title, ha='left', va='center', fontsize=11, fontweight='bold', color='#ffffff')
    if subtitle:
        ax.text(x + w - 0.3, y + h - 0.28, subtitle, ha='right', va='center', fontsize=8.5, color='#e2e8f0')

# Helper for drawing component badges
def draw_component(x, y, w, h, title, lines, fc, ec, title_col):
    card = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.06", fc=fc, ec=ec, lw=1.2)
    ax.add_patch(card)
    ax.text(x + w/2, y + h - 0.22, title, ha='center', va='center', fontsize=9.5, fontweight='bold', color=title_col)
    
    start_y = y + h - 0.45
    for idx, line in enumerate(lines):
        ax.text(x + w/2, start_y - (idx * 0.22), line, ha='center', va='center', fontsize=7.8, color='#334155')

# ==============================================================================
# LAYER 1: CLIENT PRESENTATION LAYER (Top)
# ==============================================================================
draw_layer_card(0.6, 6.95, 14.8, 1.95,
                "1. CLIENT PRESENTATION LAYER",
                "HTML5 • CSS3 • JavaScript • Bootstrap 5 • Chart.js • Bootstrap Icons",
                "#1e3a8a", "#eff6ff", "#3b82f6")

# Customer Portal
draw_component(0.9, 7.1, 4.4, 1.25,
               "Customer Self-Service Portal",
               ["• Public ticket creation with rule-based live preview",
                "• Ticket status & priority tracking badge views",
                "• Conversation thread & ticket reopening workflows"],
               "#ffffff", "#93c5fd", "#1d4ed8")

# Agent Portal
draw_component(5.8, 7.1, 4.4, 1.25,
               "Support Agent Workbench",
               ["• Assigned active ticket triage & SLA countdowns",
                "• Unassigned team queue with 1-click claim",
                "• Dual-stream: Public replies vs. Private internal notes"],
               "#ffffff", "#93c5fd", "#1d4ed8")

# Admin Portal
draw_component(10.7, 7.1, 4.4, 1.25,
               "Administrator Executive Dashboard",
               ["• 6 Real-time KPI stat cards (Open, In-Progress, Overdue)",
                "• 6 Interactive Chart.js visual distribution graphs",
                "• User, team, category, SLA policy, & CSV data export"],
               "#ffffff", "#93c5fd", "#1d4ed8")

# Downward Flow Arrow 1
ax.annotate('', xy=(8.0, 6.35), xytext=(8.0, 6.95),
            arrowprops=dict(facecolor='#3b82f6', edgecolor='#1d4ed8', width=2.0, headwidth=7))
ax.text(8.0, 6.65, "HTTPS / REST JSON APIs with CSRF Token Exchange", ha='center', va='center',
        fontsize=8.5, fontweight='bold', color='#1e40af', bbox=dict(boxstyle='square,pad=0.2', fc='#ffffff', ec='#93c5fd'))

# ==============================================================================
# LAYER 2: SECURITY & CONTROLLERS (Upper Middle)
# ==============================================================================
draw_layer_card(0.6, 4.95, 14.8, 1.35,
                "2. SECURITY GATEWAY & REST CONTROLLER LAYER",
                "Spring Security 6 • BCrypt • CookieCsrfTokenRepository • Spring MVC REST Controllers",
                "#0f766e", "#f0fdfa", "#14b8a6")

draw_component(0.9, 5.08, 3.4, 0.72, "Security & Auth Guard",
               ["CsrfFilter • SessionConcurrency", "BCryptPasswordEncoder • RBAC"], "#ffffff", "#99f6e4", "#0f766e")

draw_component(4.6, 5.08, 2.5, 0.72, "TicketController",
               ["/api/tickets/**", "CRUD, Search & Replies"], "#ffffff", "#99f6e4", "#0f766e")

draw_component(7.4, 5.08, 2.5, 0.72, "AgentController",
               ["/api/agent/**", "Queue, Claims & Status"], "#ffffff", "#99f6e4", "#0f766e")

draw_component(10.2, 5.08, 2.5, 0.72, "AdminController",
               ["/api/admin/**", "Users, Teams, SLA, CSV"], "#ffffff", "#99f6e4", "#0f766e")

draw_component(13.0, 5.08, 2.1, 0.72, "DashboardAPI",
               ["/api/dashboard/**", "Real-Time KPI Metrics"], "#ffffff", "#99f6e4", "#0f766e")

# Downward Flow Arrow 2
ax.annotate('', xy=(8.0, 4.35), xytext=(8.0, 4.95),
            arrowprops=dict(facecolor='#14b8a6', edgecolor='#0f766e', width=2.0, headwidth=7))
ax.text(8.0, 4.65, "Transactional Service Invocations & DTO Mapping", ha='center', va='center',
        fontsize=8.5, fontweight='bold', color='#0f766e', bbox=dict(boxstyle='square,pad=0.2', fc='#ffffff', ec='#99f6e4'))

# ==============================================================================
# LAYER 3: CORE APPLICATION ENGINES & SERVICES (Lower Middle)
# ==============================================================================
draw_layer_card(0.6, 2.35, 14.8, 1.95,
                "3. BUSINESS DOMAIN SERVICES & AUTOMATION ENGINES",
                "Java 17 • Spring Boot 3.2 Services • SLA Business Hours • Rule-Based AI • Mail Integration",
                "#4338ca", "#eef2ff", "#6366f1")

# Ticket Service
draw_component(0.9, 2.5, 3.3, 1.25,
               "Ticket Lifecycle Service",
               ["• State-machine validation:",
                "  OPEN → IN_PROGRESS →",
                "  WAITING → RESOLVED → CLOSED",
                "• Immutable TicketHistory audit"],
               "#ffffff", "#c7d2fe", "#3730a3")

# SLA Engine
draw_component(4.5, 2.5, 3.4, 1.25,
               "SLA Monitoring Engine",
               ["• Business hours clock (09:00-17:00)",
                "• Urgent (15m), High (1h), Med (4h)",
                "• Overdue alerts & SLA breach auditor",
                "• Pauses on customer waiting state"],
               "#ffffff", "#c7d2fe", "#3730a3")

# Rule Classifier
draw_component(8.2, 2.5, 3.4, 1.25,
               "Rule-Based AI Classifier",
               ["• Keyword tokenization & scoring",
                "• Suggests category, priority & team",
                "• 6 Configured domains (IT, E-Com,",
                "  Edu, Banking, Health, General)"],
               "#ffffff", "#c7d2fe", "#3730a3")

# Assignment & Analytics
draw_component(11.9, 2.5, 3.2, 1.25,
               "Assignment & Reports",
               ["• Configurable multi-attribute rules",
                "• Auto-assign / unassigned queue",
                "• Email notification dispatch",
                "• CSV data generator & Chart feeds"],
               "#ffffff", "#c7d2fe", "#3730a3")

# Downward Flow Arrow 3
ax.annotate('', xy=(8.0, 1.75), xytext=(8.0, 2.35),
            arrowprops=dict(facecolor='#6366f1', edgecolor='#4338ca', width=2.0, headwidth=7))
ax.text(8.0, 2.05, "Spring Data JPA Repositories & ACID Transactions", ha='center', va='center',
        fontsize=8.5, fontweight='bold', color='#4338ca', bbox=dict(boxstyle='square,pad=0.2', fc='#ffffff', ec='#c7d2fe'))

# ==============================================================================
# LAYER 4: DATA PERSISTENCE TIER (Bottom)
# ==============================================================================
draw_layer_card(0.6, 0.25, 14.8, 1.45,
                "4. DATA PERSISTENCE & SCHEMA MIGRATION TIER",
                "Spring Data JPA • Hibernate 6.4 ORM • HikariCP Connection Pool • Flyway 10.x",
                "#15803d", "#f0fdf4", "#22c55e")

# Flyway Migrations
draw_component(0.9, 0.38, 3.3, 0.78, "Flyway Database Migrations",
               ["Versioned scripts V1__ through V12__", "Schema version control & baseline data"],
               "#ffffff", "#bbf7d0", "#15803d")

# MySQL Database
draw_component(4.5, 0.38, 5.0, 0.78, "MySQL 8.0 Enterprise Relational Database",
               ["Primary Production Store: organizations, users, support_teams, categories,",
                "sla_policies, tickets, ticket_replies, ticket_history, sla_breaches, assignment_rules"],
               "#ffffff", "#bbf7d0", "#15803d")

# Secondary Demo In-Memory H2
draw_component(9.8, 0.38, 5.3, 0.78, "Embedded In-Memory H2 Testing Engine",
               ["Dual-Profile Demo Architecture (--spring.profiles.active=demo)",
                "Self-contained zero-config demonstration environment with dynamic seeds"],
               "#ffffff", "#bbf7d0", "#15803d")

# Save diagram
plt.tight_layout()
plt.savefig(output_path, bbox_inches='tight', dpi=300)
plt.close()

print('System Architecture Image successfully generated at:', output_path)

