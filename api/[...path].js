// Vercel Serverless Function: Multi-route API Handler & Spring Boot Proxy
const sampleTickets = [
    {
        id: 1,
        ticketNumber: "SD-2026-00001",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "VPN Gateway Connection Failure (Error 800)",
        description: "Cannot connect to corporate VPN gateway after latest desktop client update. Getting Error 800: Remote connection was not made.",
        domain: "IT_SOFTWARE",
        categoryId: 2,
        categoryName: "Technical Support",
        priority: "HIGH",
        status: "RESOLVED",
        assignedTeamId: 1,
        assignedTeamName: "Technical Support",
        assignedAgentId: 2,
        assignedAgentName: "Alex Agent",
        firstResponseAt: "2026-10-25T17:37:59",
        resolvedAt: "2026-10-26T19:12:59",
        slaFirstResponseBreached: false,
        slaResolutionBreached: false,
        createdAt: "2026-10-25T17:12:59",
        updatedAt: "2026-10-26T19:12:59"
    },
    {
        id: 2,
        ticketNumber: "SD-2026-00002",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "SSO Single Sign-On 500 Internal Error during OAuth Callback",
        description: "Production staff are unable to log in via Okta OAuth SSO. The callback returns HTTP 500 with invalid token signature.",
        domain: "IT_SOFTWARE",
        categoryId: 6,
        categoryName: "Login Issues",
        priority: "URGENT",
        status: "IN_PROGRESS",
        assignedTeamId: 1,
        assignedTeamName: "Technical Support",
        assignedAgentId: 2,
        assignedAgentName: "Alex Agent",
        firstResponseAt: "2026-10-27T18:22:59",
        resolvedAt: null,
        slaFirstResponseBreached: false,
        slaResolutionBreached: false,
        createdAt: "2026-10-27T18:12:59",
        updatedAt: "2026-10-27T18:22:59"
    },
    {
        id: 3,
        ticketNumber: "SD-2026-00003",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "Duplicate Charge on Annual Subscription Renewal",
        description: "Our corporate credit card was charged twice for invoice #INV-98214 ($1,200 each). Please refund the duplicate transaction.",
        domain: "ECOMMERCE",
        categoryId: 3,
        categoryName: "Billing Support",
        priority: "MEDIUM",
        status: "WAITING_FOR_CUSTOMER",
        assignedTeamId: 2,
        assignedTeamName: "Billing Support",
        assignedAgentId: 2,
        assignedAgentName: "Alex Agent",
        firstResponseAt: "2026-10-26T16:57:59",
        resolvedAt: null,
        slaFirstResponseBreached: false,
        slaResolutionBreached: false,
        createdAt: "2026-10-26T16:12:59",
        updatedAt: "2026-10-26T16:57:59"
    },
    {
        id: 4,
        ticketNumber: "SD-2026-00004",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "Official Academic Transcript Verification Request",
        description: "Enquiring about the automated verification process for graduation certificates for Fall 2026 admissions.",
        domain: "EDUCATION",
        categoryId: 1,
        categoryName: "General Inquiry",
        priority: "LOW",
        status: "OPEN",
        assignedTeamId: 3,
        assignedTeamName: "General Support",
        assignedAgentId: null,
        assignedAgentName: null,
        firstResponseAt: null,
        resolvedAt: null,
        slaFirstResponseBreached: false,
        slaResolutionBreached: false,
        createdAt: "2026-10-27T16:12:59",
        updatedAt: "2026-10-27T16:12:59"
    },
    {
        id: 5,
        ticketNumber: "SD-2026-00005",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "Disputed High-Value Wire Transfer #WT-44129",
        description: "Customer flagged an unauthorized international wire transfer requiring immediate freeze and compliance investigation.",
        domain: "BANKING",
        categoryId: 7,
        categoryName: "Payment Issues",
        priority: "URGENT",
        status: "IN_PROGRESS",
        assignedTeamId: 2,
        assignedTeamName: "Billing Support",
        assignedAgentId: 2,
        assignedAgentName: "Alex Agent",
        firstResponseAt: "2026-10-27T04:12:59",
        resolvedAt: null,
        slaFirstResponseBreached: true,
        slaResolutionBreached: true,
        createdAt: "2026-10-27T02:12:59",
        updatedAt: "2026-10-27T19:12:59"
    },
    {
        id: 6,
        ticketNumber: "SD-2026-00006",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "Tele-health Video Consultation Stuttering on Chrome",
        description: "Doctors reporting WebRTC audio packet drops and freezing video frames when conducting patient remote consults.",
        domain: "HEALTHCARE",
        categoryId: 2,
        categoryName: "Technical Support",
        priority: "MEDIUM",
        status: "CLOSED",
        assignedTeamId: 1,
        assignedTeamName: "Technical Support",
        assignedAgentId: 2,
        assignedAgentName: "Alex Agent",
        firstResponseAt: "2026-10-22T21:12:59",
        resolvedAt: "2026-10-23T20:12:59",
        closedAt: "2026-10-24T20:12:59",
        slaFirstResponseBreached: false,
        slaResolutionBreached: false,
        createdAt: "2026-10-22T20:12:59",
        updatedAt: "2026-10-24T20:12:59"
    },
    {
        id: 7,
        ticketNumber: "SD-2026-00007",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "Dark Mode PDF Export Support for Executive Reports",
        description: "Would love the ability to export monthly executive dashboard charts with a clean dark theme styling.",
        domain: "GENERAL",
        categoryId: 5,
        categoryName: "Feature Request",
        priority: "LOW",
        status: "OPEN",
        assignedTeamId: 3,
        assignedTeamName: "General Support",
        assignedAgentId: null,
        assignedAgentName: null,
        firstResponseAt: null,
        resolvedAt: null,
        slaFirstResponseBreached: false,
        slaResolutionBreached: false,
        createdAt: "2026-10-27T19:12:59",
        updatedAt: "2026-10-27T19:12:59"
    },
    {
        id: 8,
        ticketNumber: "SD-2026-00008",
        organizationId: 1,
        customerId: 3,
        customerName: "Chris Customer",
        customerEmail: "customer@supportdesk.com",
        subject: "HikariCP Connection Pool Exhaustion on Nightly Batch",
        description: "Nightly synchronization job at 02:00 UTC hung with ConnectionTimeoutException: Connection is not available.",
        domain: "IT_SOFTWARE",
        categoryId: 4,
        categoryName: "Bug Report",
        priority: "HIGH",
        status: "RESOLVED",
        assignedTeamId: 1,
        assignedTeamName: "Technical Support",
        assignedAgentId: 2,
        assignedAgentName: "Alex Agent",
        firstResponseAt: "2026-10-24T18:42:59",
        resolvedAt: "2026-10-25T15:12:59",
        slaFirstResponseBreached: false,
        slaResolutionBreached: false,
        createdAt: "2026-10-24T18:12:59",
        updatedAt: "2026-10-25T15:12:59"
    }
];

module.exports = async (req, res) => {
    // CORS Headers
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, X-XSRF-TOKEN');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    // Set sample XSRF cookie for frontend API caller
    res.setHeader('Set-Cookie', 'XSRF-TOKEN=demo-token-12345; Path=/; SameSite=Lax');

    const url = req.url || '';
    const cleanUrl = url.split('?')[0];

    // If external Spring Boot backend URL configured, proxy the request
    if (process.env.BACKEND_URL) {
        try {
            const proxyHeaders = { ...req.headers };
            delete proxyHeaders.host;
            const backendRes = await fetch(`${process.env.BACKEND_URL}${url}`, {
                method: req.method,
                headers: proxyHeaders,
                body: req.method !== 'GET' && req.method !== 'HEAD' ? JSON.stringify(req.body) : undefined
            });
            const data = await backendRes.text();
            res.status(backendRes.status);
            backendRes.headers.forEach((v, k) => res.setHeader(k, v));
            return res.send(data);
        } catch (err) {
            console.error('Backend proxy error:', err);
        }
    }

    // Determine current user role from cookie or referer
    let role = 'ADMIN';
    const cookie = req.headers.cookie || '';
    if (cookie.includes('session_role=AGENT') || url.includes('/agent')) {
        role = 'AGENT';
    } else if (cookie.includes('session_role=CUSTOMER') || url.includes('/customer')) {
        role = 'CUSTOMER';
    }

    // ROUTING
    if (cleanUrl === '/api/auth/me') {
        if (role === 'CUSTOMER') {
            return res.json({
                id: 3,
                email: "customer@supportdesk.com",
                fullName: "Chris Customer",
                firstName: "Chris",
                lastName: "Customer",
                role: "CUSTOMER",
                phone: "+1-555-0103",
                organizationName: "TechFlow Solutions"
            });
        } else if (role === 'AGENT') {
            return res.json({
                id: 2,
                email: "agent@supportdesk.com",
                fullName: "Alex Agent",
                firstName: "Alex",
                lastName: "Agent",
                role: "AGENT",
                phone: "+1-555-0102",
                organizationName: "TechFlow Solutions"
            });
        } else {
            return res.json({
                id: 1,
                email: "admin@supportdesk.com",
                fullName: "System Administrator",
                firstName: "System",
                lastName: "Administrator",
                role: "ADMIN",
                phone: "+1-555-0100",
                organizationName: "TechFlow Solutions"
            });
        }
    }

    // Dashboard Stats
    if (cleanUrl === '/api/dashboard/stats' || cleanUrl === '/api/admin/dashboard/stats') {
        return res.json({
            totalTickets: 8,
            openTickets: 2,
            inProgressTickets: 2,
            waitingTickets: 1,
            resolvedTickets: 2,
            closedTickets: 1,
            overdueTickets: 2,
            avgFirstResponseMinutes: 48.3,
            avgResolutionMinutes: 1420.0,
            slaComplianceRate: 87.5
        });
    }

    // Agent Stats
    if (cleanUrl === '/api/agent/dashboard/stats') {
        return res.json({
            totalTickets: 6,
            openTickets: 0,
            inProgressTickets: 2,
            waitingTickets: 1,
            resolvedTickets: 2,
            closedTickets: 1,
            overdueTickets: 2
        });
    }

    // Chart: Tickets by Status
    if (cleanUrl === '/api/admin/dashboard/tickets-by-status') {
        return res.json([
            { status: "OPEN", count: 2 },
            { status: "IN_PROGRESS", count: 2 },
            { status: "WAITING_FOR_CUSTOMER", count: 1 },
            { status: "RESOLVED", count: 2 },
            { status: "CLOSED", count: 1 }
        ]);
    }

    // Chart: Tickets by Priority
    if (cleanUrl === '/api/admin/dashboard/tickets-by-priority') {
        return res.json([
            { priority: "URGENT", count: 2 },
            { priority: "HIGH", count: 2 },
            { priority: "MEDIUM", count: 2 },
            { priority: "LOW", count: 2 }
        ]);
    }

    // Chart: Tickets by Domain
    if (cleanUrl === '/api/admin/dashboard/tickets-by-domain') {
        return res.json([
            { domain: "HEALTHCARE", count: 1 },
            { domain: "BANKING", count: 1 },
            { domain: "IT_SOFTWARE", count: 3 },
            { domain: "EDUCATION", count: 1 },
            { domain: "GENERAL", count: 1 },
            { domain: "ECOMMERCE", count: 1 }
        ]);
    }

    // Chart: Ticket Trends
    if (cleanUrl === '/api/admin/dashboard/ticket-trends') {
        const trends = [];
        for (let i = 13; i >= 0; i--) {
            const d = new Date();
            d.setDate(d.getDate() - i);
            const dateStr = d.toISOString().split('T')[0];
            trends.push({
                date: dateStr,
                createdCount: i === 2 ? 3 : (i === 5 ? 2 : (i % 3 === 0 ? 1 : 0)),
                resolvedCount: i === 1 ? 2 : (i === 4 ? 1 : (i % 4 === 0 ? 1 : 0))
            });
        }
        return res.json(trends);
    }

    // Chart: SLA Compliance
    if (cleanUrl === '/api/admin/dashboard/sla-compliance') {
        const compliances = [];
        for (let i = 13; i >= 0; i--) {
            const d = new Date();
            d.setDate(d.getDate() - i);
            compliances.push({
                date: d.toISOString().split('T')[0],
                complianceRate: i === 1 ? 87.5 : 95.0,
                breachCount: i === 1 ? 1 : 0
            });
        }
        return res.json(compliances);
    }

    // Agent Team Queue
    if (cleanUrl === '/api/agent/team-queue') {
        return res.json(sampleTickets.filter(t => !t.assignedAgentId));
    }

    // Agent Tickets
    if (cleanUrl === '/api/agent/tickets') {
        const agentTickets = sampleTickets.filter(t => t.assignedAgentId === 2);
        return res.json({
            content: agentTickets,
            totalElements: agentTickets.length,
            totalPages: 1,
            page: 0,
            size: 10
        });
    }

    // All Tickets List
    if (cleanUrl === '/api/tickets') {
        return res.json({
            content: sampleTickets,
            totalElements: sampleTickets.length,
            totalPages: 1,
            page: 0,
            size: 10
        });
    }

    // Ticket by ID
    const ticketMatch = cleanUrl.match(/^\/api\/tickets\/(\d+)$/);
    if (ticketMatch) {
        const tid = parseInt(ticketMatch[1], 10);
        const t = sampleTickets.find(item => item.id === tid) || sampleTickets[0];
        return res.json({
            ...t,
            replies: [
                {
                    id: 1,
                    userName: t.customerName,
                    userRole: "CUSTOMER",
                    message: t.description,
                    internalNote: false,
                    createdAt: t.createdAt
                },
                {
                    id: 2,
                    userName: "Alex Agent",
                    userRole: "AGENT",
                    message: "Investigating the issue. Updates will follow shortly.",
                    internalNote: false,
                    createdAt: t.firstResponseAt || t.createdAt
                },
                {
                    id: 3,
                    userName: "Alex Agent",
                    userRole: "AGENT",
                    message: "Internal diagnostic note: Verified service logs and verified connection limits.",
                    internalNote: true,
                    createdAt: t.firstResponseAt || t.createdAt
                }
            ],
            history: [
                { id: 1, userName: t.customerName, fieldChanged: "STATUS", oldValue: null, newValue: "OPEN", comment: "Ticket created", createdAt: t.createdAt },
                { id: 2, userName: "System", fieldChanged: "STATUS", oldValue: "OPEN", newValue: t.status, comment: "Status advanced", createdAt: t.updatedAt }
            ]
        });
    }

    // Classification Preview
    if (cleanUrl === '/api/tickets/classify-preview') {
        return res.json({
            suggestedCategoryId: 2,
            suggestedCategoryName: "Technical Support",
            suggestedPriority: "HIGH",
            suggestedTeamId: 1,
            suggestedTeamName: "Technical Support",
            confidence: 0.92
        });
    }

    // Teams
    if (cleanUrl === '/api/admin/teams') {
        return res.json([
            { id: 1, name: "Technical Support", description: "Handles technical issues and bug reports", active: true },
            { id: 2, name: "Billing Support", description: "Handles billing and payments", active: true },
            { id: 3, name: "General Support", description: "Handles general inquiries", active: true }
        ]);
    }

    // Categories
    if (cleanUrl === '/api/admin/categories') {
        return res.json([
            { id: 1, name: "Login Issues", description: "Authentication problems", active: true },
            { id: 2, name: "Technical Support", description: "Technical troubleshooting", active: true },
            { id: 3, name: "Billing Support", description: "Invoicing and payments", active: true },
            { id: 4, name: "Bug Report", description: "Software defects", active: true },
            { id: 5, name: "General Inquiry", description: "General questions", active: true },
            { id: 6, name: "Feature Request", description: "Enhancements", active: true },
            { id: 7, name: "Payment Issues", description: "Disputes and charges", active: true }
        ]);
    }

    // SLA Policies
    if (cleanUrl === '/api/admin/sla-policies') {
        return res.json([
            { id: 1, name: "Urgent SLA", priority: "URGENT", firstResponseMinutes: 15, resolutionMinutes: 240, active: true },
            { id: 2, name: "High SLA", priority: "HIGH", firstResponseMinutes: 60, resolutionMinutes: 480, active: true },
            { id: 3, name: "Medium SLA", priority: "MEDIUM", firstResponseMinutes: 240, resolutionMinutes: 1440, active: true },
            { id: 4, name: "Low SLA", priority: "LOW", firstResponseMinutes: 480, resolutionMinutes: 2880, active: true }
        ]);
    }

    // Assignment Rules
    if (cleanUrl === '/api/admin/assignment-rules') {
        return res.json([
            { id: 1, name: "Login to Technical", active: true, ruleOrder: 1, assignTeamName: "Technical Support" },
            { id: 2, name: "Payment to Billing", active: true, ruleOrder: 2, assignTeamName: "Billing Support" },
            { id: 3, name: "Bug to Technical", active: true, ruleOrder: 3, assignTeamName: "Technical Support" }
        ]);
    }

    // Users
    if (cleanUrl === '/api/admin/users') {
        return res.json([
            { id: 1, email: "admin@supportdesk.com", firstName: "System", lastName: "Administrator", role: "ADMIN", active: true, phone: "+1-555-0100" },
            { id: 2, email: "agent@supportdesk.com", firstName: "Alex", lastName: "Agent", role: "AGENT", active: true, phone: "+1-555-0102" },
            { id: 3, email: "customer@supportdesk.com", firstName: "Chris", lastName: "Customer", role: "CUSTOMER", active: true, phone: "+1-555-0103" }
        ]);
    }

    // CSV Export
    if (cleanUrl === '/api/admin/reports/export-csv') {
        res.setHeader('Content-Type', 'text/csv');
        res.setHeader('Content-Disposition', 'attachment; filename="supportdesk-tickets.csv"');
        const csv = "Ticket Number,Subject,Domain,Category,Priority,Status,Customer,Assigned Agent,Created At\n" +
            sampleTickets.map(t => `"${t.ticketNumber}","${t.subject}","${t.domain}","${t.categoryName}","${t.priority}","${t.status}","${t.customerName}","${t.assignedAgentName || 'Unassigned'}","${t.createdAt}"`).join("\n");
        return res.send(csv);
    }

    // Default fallback
    return res.json({ success: true, message: "Operation completed successfully" });
};
