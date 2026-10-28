import os
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as patches

fig_dir = r'J:\JFDS\supportdesk\figures'
os.makedirs(fig_dir, exist_ok=True)

# -------------------------------------------------------------
# FIG 1: High-Level Multi-Tier Architecture
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 5), dpi=300)
ax.set_xlim(0, 10)
ax.set_ylim(0, 5)
ax.axis('off')

# Title
ax.text(5, 4.6, 'SupportDesk: Multi-Tier Enterprise Architecture', ha='center', va='center', fontsize=14, fontweight='bold', color='#0f172a')

# Tier 1: Client
rect_client = patches.FancyBboxPatch((0.5, 3.2), 9, 0.9, boxstyle="round,pad=0.1", fc='#eff6ff', ec='#3b82f6', lw=1.5)
ax.add_patch(rect_client)
ax.text(5, 3.8, 'Presentation Layer (Responsive HTML5 / CSS3 / JavaScript / Bootstrap 5 / Chart.js)', ha='center', va='center', fontsize=10, fontweight='bold', color='#1e3a8a')
ax.text(5, 3.45, 'Customer Self-Service Desk  |  Agent Triage Workbench  |  Administrator Executive Dashboard', ha='center', va='center', fontsize=9, color='#334155')

# Arrow down
ax.annotate('', xy=(5, 2.7), xytext=(5, 3.2), arrowprops=dict(facecolor='#3b82f6', edgecolor='#1e40af', width=1.5, headwidth=6))

# Tier 2: Backend
rect_server = patches.FancyBboxPatch((0.5, 1.4), 9, 1.3, boxstyle="round,pad=0.1", fc='#f8fafc', ec='#0ea5e9', lw=1.5)
ax.add_patch(rect_server)
ax.text(5, 2.45, 'Spring Boot 3.x Application Tier (Java 17 / Spring MVC / REST APIs)', ha='center', va='center', fontsize=10, fontweight='bold', color='#0369a1')

# Sub boxes inside Tier 2
ax.text(2.0, 1.95, 'Security & Auth\n(Spring Security, CSRF,\nBCrypt, Role-Guard)', ha='center', va='center', fontsize=8, bbox=dict(boxstyle='square,pad=0.3', fc='#e0f2fe', ec='#38bdf8'))
ax.text(5.0, 1.95, 'Business Logic Services\n(Ticket, Reply, Assignment,\nSLA Business Hours Engine)', ha='center', va='center', fontsize=8, bbox=dict(boxstyle='square,pad=0.3', fc='#e0f2fe', ec='#38bdf8'))
ax.text(8.0, 1.95, 'Rule Classifier & Email\n(Domain Rule Matcher,\nSpring Mail Notifications)', ha='center', va='center', fontsize=8, bbox=dict(boxstyle='square,pad=0.3', fc='#e0f2fe', ec='#38bdf8'))

# Arrow down
ax.annotate('', xy=(5, 0.9), xytext=(5, 1.4), arrowprops=dict(facecolor='#0ea5e9', edgecolor='#0284c7', width=1.5, headwidth=6))

# Tier 3: Persistence
rect_data = patches.FancyBboxPatch((0.5, 0.1), 9, 0.75, boxstyle="round,pad=0.1", fc='#f0fdf4', ec='#22c55e', lw=1.5)
ax.add_patch(rect_data)
ax.text(5, 0.6, 'Data Persistence Tier (Spring Data JPA / Hibernate / Flyway Migrations)', ha='center', va='center', fontsize=10, fontweight='bold', color='#15803d')
ax.text(5, 0.3, 'MySQL 8.0 Enterprise Relational Store  /  Dual In-Memory H2 Testing Engine', ha='center', va='center', fontsize=9, color='#334155')

plt.tight_layout()
plt.savefig(os.path.join(fig_dir, 'fig1_architecture.png'), bbox_inches='tight')
plt.close()

# -------------------------------------------------------------
# FIG 2: Ticket Lifecycle State Machine
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 4.5), dpi=300)
ax.set_xlim(0, 10)
ax.set_ylim(0, 4.5)
ax.axis('off')

ax.text(5, 4.1, 'SupportDesk: Complete Ticket Lifecycle State Machine', ha='center', va='center', fontsize=13, fontweight='bold', color='#0f172a')

# Boxes for statuses
statuses = [
    ('OPEN', 1.2, 2.5, '#3b82f6', 'Ticket Created\nUnassigned/Assigned'),
    ('IN_PROGRESS', 3.8, 2.5, '#f59e0b', 'Agent Investigating\nFirst Response Sent'),
    ('WAITING_FOR_\nCUSTOMER', 6.4, 2.5, '#8b5cf6', 'Pending Customer\nClarification'),
    ('RESOLVED', 8.8, 2.5, '#10b981', 'Solution Verified\nEligible to Reopen'),
    ('CLOSED', 8.8, 0.8, '#64748b', 'Permanent Archive\nLifecycle Completed')
]

for name, x, y, col, sub in statuses:
    rect = patches.FancyBboxPatch((x-0.9, y-0.45), 1.8, 0.9, boxstyle="round,pad=0.08", fc='#ffffff', ec=col, lw=2)
    ax.add_patch(rect)
    ax.text(x, y+0.12, name, ha='center', va='center', fontsize=8.5, fontweight='bold', color=col)
    ax.text(x, y-0.22, sub, ha='center', va='center', fontsize=6.8, color='#475569')

# Connectors
ax.annotate('', xy=(2.9, 2.5), xytext=(2.1, 2.5), arrowprops=dict(facecolor='#3b82f6', edgecolor='#1d4ed8', width=1.2, headwidth=5))
ax.annotate('', xy=(5.5, 2.5), xytext=(4.7, 2.5), arrowprops=dict(facecolor='#f59e0b', edgecolor='#d97706', width=1.2, headwidth=5))
ax.annotate('', xy=(7.9, 2.5), xytext=(7.3, 2.5), arrowprops=dict(facecolor='#8b5cf6', edgecolor='#6d28d9', width=1.2, headwidth=5))
ax.annotate('', xy=(8.8, 1.25), xytext=(8.8, 2.05), arrowprops=dict(facecolor='#10b981', edgecolor='#047857', width=1.2, headwidth=5))

# Curved return arrows (Reopen & Customer Reply)
ax.annotate('Customer Replies', xy=(4.2, 2.05), xytext=(6.0, 1.6), arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=0.3", color='#8b5cf6', lw=1.2), fontsize=7.5, color='#6d28d9')
ax.annotate('Customer Reopens', xy=(1.5, 2.05), xytext=(8.0, 1.5), arrowprops=dict(arrowstyle="->", connectionstyle="arc3,rad=-0.25", color='#ef4444', lw=1.2), fontsize=7.5, color='#b91c1c')

plt.tight_layout()
plt.savefig(os.path.join(fig_dir, 'fig2_lifecycle.png'), bbox_inches='tight')
plt.close()

# -------------------------------------------------------------
# FIG 3: Database Entity Topology
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 4.8), dpi=300)
ax.set_xlim(0, 10)
ax.set_ylim(0, 4.8)
ax.axis('off')

ax.text(5, 4.5, 'SupportDesk: Multi-Domain Entity-Relationship Topology', ha='center', va='center', fontsize=13, fontweight='bold', color='#0f172a')

nodes = [
    ('organizations', 1.5, 3.5, '#e0e7ff', '#4338ca'),
    ('users\n(ADMIN/AGENT/CUSTOMER)', 4.0, 3.5, '#e0f2fe', '#0369a1'),
    ('support_teams', 1.5, 1.8, '#fef3c7', '#b45309'),
    ('ticket_categories', 1.5, 0.6, '#fef3c7', '#b45309'),
    ('sla_policies', 4.0, 0.6, '#fce7f3', '#be185d'),
    ('tickets\n(Primary Aggregate)', 6.8, 2.3, '#dcfce7', '#15803d'),
    ('ticket_replies', 9.2, 3.5, '#f1f5f9', '#475569'),
    ('ticket_history', 9.2, 2.3, '#f1f5f9', '#475569'),
    ('sla_breaches', 9.2, 1.0, '#fee2e2', '#b91c1c'),
    ('assignment_rules', 4.0, 1.8, '#ede9fe', '#6d28d9')
]

for name, x, y, bg, border in nodes:
    rect = patches.FancyBboxPatch((x-0.9, y-0.4), 1.8, 0.8, boxstyle="round,pad=0.06", fc=bg, ec=border, lw=1.5)
    ax.add_patch(rect)
    ax.text(x, y, name, ha='center', va='center', fontsize=7.5, fontweight='bold', color=border)

# Draw linking lines
edges = [
    ((1.5, 3.1), (1.5, 2.2)),
    ((2.4, 3.5), (3.1, 3.5)),
    ((4.9, 3.5), (5.9, 2.6)),
    ((2.4, 1.8), (5.9, 2.2)),
    ((4.9, 0.6), (6.2, 1.9)),
    ((7.7, 2.6), (8.3, 3.3)),
    ((7.7, 2.3), (8.3, 2.3)),
    ((7.7, 2.0), (8.3, 1.2))
]
for p1, p2 in edges:
    ax.plot([p1[0], p2[0]], [p1[1], p2[1]], color='#94a3b8', lw=1.2, ls='--')

plt.tight_layout()
plt.savefig(os.path.join(fig_dir, 'fig3_schema.png'), bbox_inches='tight')
plt.close()

# -------------------------------------------------------------
# FIG 4: Multi-Role Portals
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 4.2), dpi=300)
ax.set_xlim(0, 10)
ax.set_ylim(0, 4.2)
ax.axis('off')

ax.text(5, 3.8, 'SupportDesk: Multi-Role User Portals & Separation of Concerns', ha='center', va='center', fontsize=13, fontweight='bold', color='#0f172a')

portals = [
    ('Customer Portal', 1.8, 2.0, '#3b82f6', [
        '• Self-service ticket creation',
        '• Live AI category & priority preview',
        '• Search & view own ticket history',
        '• Send public replies & reopen tickets'
    ]),
    ('Agent Portal', 5.0, 2.0, '#0ea5e9', [
        '• Active assigned triage queue',
        '• Overdue countdown timer alerts',
        '• Claim from unassigned team queue',
        '• Public reply vs. Private internal note'
    ]),
    ('Administrator Portal', 8.2, 2.0, '#10b981', [
        '• 6 Real-time KPI summary cards',
        '• 6 Chart.js executive analytics graphs',
        '• Reassign tickets across agents/teams',
        '• Configure domains, categories & SLA'
    ])
]

for title, x, y, col, bullets in portals:
    rect = patches.FancyBboxPatch((x-1.4, y-1.3), 2.8, 2.6, boxstyle="round,pad=0.1", fc='#f8fafc', ec=col, lw=2)
    ax.add_patch(rect)
    header_rect = patches.FancyBboxPatch((x-1.4, y+0.8), 2.8, 0.5, boxstyle="round,pad=0.05", fc=col, ec=col)
    ax.add_patch(header_rect)
    ax.text(x, y+1.05, title, ha='center', va='center', fontsize=9.5, fontweight='bold', color='#ffffff')
    
    by = y + 0.45
    for b in bullets:
        ax.text(x-1.25, by, b, ha='left', va='center', fontsize=7.2, color='#334155')
        by -= 0.38

plt.tight_layout()
plt.savefig(os.path.join(fig_dir, 'fig4_portals.png'), bbox_inches='tight')
plt.close()

# -------------------------------------------------------------
# FIG 5: SLA Business Hours & Rule Classifier Workflow
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 4.2), dpi=300)
ax.set_xlim(0, 10)
ax.set_ylim(0, 4.2)
ax.axis('off')

ax.text(5, 3.8, 'SLA Business Hours Engine & Rule-Based AI Classification Workflow', ha='center', va='center', fontsize=13, fontweight='bold', color='#0f172a')

# Step boxes
steps = [
    ('Ticket Ingestion\n(Subject, Body, Domain)', 1.2, 2.0, '#e0f2fe', '#0284c7'),
    ('Keyword Tokenizer\n& Weight Scorer', 3.4, 2.0, '#e0e7ff', '#4f46e5'),
    ('Domain Matcher\n(Category, Priority, Team)', 5.6, 2.0, '#fef3c7', '#d97706'),
    ('SLA Policy Lookup\n& Business Hours Clock', 7.8, 2.0, '#dcfce7', '#16a34a'),
    ('Persist & Alert\n(Deadlines & Breach Monitor)', 9.2, 2.0, '#fee2e2', '#dc2626')
]

for name, x, y, bg, border in steps:
    rect = patches.FancyBboxPatch((x-0.85, y-0.6), 1.7, 1.2, boxstyle="round,pad=0.08", fc=bg, ec=border, lw=1.5)
    ax.add_patch(rect)
    ax.text(x, y, name, ha='center', va='center', fontsize=7.5, fontweight='bold', color=border)

for i in range(len(steps)-1):
    x1 = steps[i][1] + 0.85
    x2 = steps[i+1][1] - 0.85
    ax.annotate('', xy=(x2, 2.0), xytext=(x1, 2.0), arrowprops=dict(facecolor='#64748b', edgecolor='#475569', width=1.2, headwidth=5))

plt.tight_layout()
plt.savefig(os.path.join(fig_dir, 'fig5_sla_classifier.png'), bbox_inches='tight')
plt.close()

# -------------------------------------------------------------
# FIG 6: Security Architecture & CSRF Flow
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 4.2), dpi=300)
ax.set_xlim(0, 10)
ax.set_ylim(0, 4.2)
ax.axis('off')

ax.text(5, 3.8, 'SupportDesk: Spring Security, Session Control & CSRF Protection', ha='center', va='center', fontsize=13, fontweight='bold', color='#0f172a')

sec_steps = [
    ('Incoming Request\n(Headers, Cookies)', 1.5, 2.0, '#f8fafc', '#64748b'),
    ('CsrfFilter\n(Cookie Token vs Header)', 3.8, 2.0, '#fef2f2', '#ef4444'),
    ('SecurityContextFilter\n(Session Authentication)', 6.2, 2.0, '#eff6ff', '#3b82f6'),
    ('AuthorizationFilter\n(ADMIN/AGENT/CUSTOMER)', 8.5, 2.0, '#f0fdf4', '#22c55e')
]

for name, x, y, bg, border in sec_steps:
    rect = patches.FancyBboxPatch((x-0.95, y-0.6), 1.9, 1.2, boxstyle="round,pad=0.08", fc=bg, ec=border, lw=1.5)
    ax.add_patch(rect)
    ax.text(x, y, name, ha='center', va='center', fontsize=8, fontweight='bold', color=border)

for i in range(len(sec_steps)-1):
    x1 = sec_steps[i][1] + 0.95
    x2 = sec_steps[i+1][1] - 0.95
    ax.annotate('', xy=(x2, 2.0), xytext=(x1, 2.0), arrowprops=dict(facecolor='#3b82f6', edgecolor='#1e40af', width=1.2, headwidth=5))

plt.tight_layout()
plt.savefig(os.path.join(fig_dir, 'fig6_security.png'), bbox_inches='tight')
plt.close()

# -------------------------------------------------------------
# FIG 7: Real-Time Analytics & SLA Compliance Reports
# -------------------------------------------------------------
fig, ax = plt.subplots(figsize=(10, 4.5), dpi=300)
ax.set_xlim(0, 10)
ax.set_ylim(0, 4.5)
ax.axis('off')

ax.text(5, 4.1, 'SupportDesk: Executive KPI Dashboards & Real-Time Chart.js Metrics', ha='center', va='center', fontsize=13, fontweight='bold', color='#0f172a')

# 4 Dashboard Card Previews
cards = [
    ('Total Tickets: 8', 'Open: 2 | In-Progress: 2\nWaiting: 1 | Resolved: 3', 2.3, 2.7, '#eff6ff', '#1d4ed8'),
    ('SLA Compliance: 87.5%', 'Breached: 1 (Banking)\nCompliant: 7 tickets', 7.7, 2.7, '#f0fdf4', '#15803d'),
    ('Avg First Response', '48.3 Minutes\nTarget: < 60 Min', 2.3, 1.1, '#fef3c7', '#b45309'),
    ('Avg Resolution Time', '23.6 Hours\nTarget: < 48 Hours', 7.7, 1.1, '#f5f3ff', '#6d28d9')
]

for title, sub, x, y, bg, border in cards:
    rect = patches.FancyBboxPatch((x-2.1, y-0.55), 4.2, 1.1, boxstyle="round,pad=0.1", fc=bg, ec=border, lw=1.5)
    ax.add_patch(rect)
    ax.text(x, y+0.2, title, ha='center', va='center', fontsize=9.5, fontweight='bold', color=border)
    ax.text(x, y-0.2, sub, ha='center', va='center', fontsize=8, color='#334155')

plt.tight_layout()
plt.savefig(os.path.join(fig_dir, 'fig7_dashboard.png'), bbox_inches='tight')
plt.close()

print('All 7 figures generated successfully in:', fig_dir)

