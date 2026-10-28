package com.supportdesk.config;

import com.supportdesk.entity.*;
import com.supportdesk.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DataBootstrap.class);

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final SupportTeamRepository supportTeamRepository;
    private final TicketCategoryRepository ticketCategoryRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final SlaBreachRepository slaBreachRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${supportdesk.admin.email:admin@supportdesk.com}")
    private String adminEmail;

    @Value("${supportdesk.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${supportdesk.admin.first-name:System}")
    private String adminFirstName;

    @Value("${supportdesk.admin.last-name:Administrator}")
    private String adminLastName;

    public DataBootstrap(UserRepository userRepository,
                         OrganizationRepository organizationRepository,
                         SlaPolicyRepository slaPolicyRepository,
                         SupportTeamRepository supportTeamRepository,
                         TicketCategoryRepository ticketCategoryRepository,
                         TeamMembershipRepository teamMembershipRepository,
                         TicketRepository ticketRepository,
                         TicketReplyRepository ticketReplyRepository,
                         TicketHistoryRepository ticketHistoryRepository,
                         SlaBreachRepository slaBreachRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.supportTeamRepository = supportTeamRepository;
        this.ticketCategoryRepository = ticketCategoryRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.ticketRepository = ticketRepository;
        this.ticketReplyRepository = ticketReplyRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.slaBreachRepository = slaBreachRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        log.info("Bootstrapping core configuration and sample data...");

        Organization org = organizationRepository.findAll().stream().findFirst()
                .orElseGet(this::createDefaultOrganization);

        User admin = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> createDefaultAdmin(org));

        createDefaultSlaPolicies(org);
        createDefaultTeams(org);
        createDefaultCategories(org);

        User agent = userRepository.findByEmail("agent@supportdesk.com")
                .orElseGet(() -> createDefaultAgent(org));

        User customer = userRepository.findByEmail("customer@supportdesk.com")
                .orElseGet(() -> createDefaultCustomer(org));

        setupTeamMemberships(org, agent);

        if (ticketRepository.count() == 0) {
            createSampleTickets(org, admin, agent, customer);
        }

        log.warn("========================================================");
        log.warn("  SUPPORTDESK DEMO ACCOUNTS READY:");
        log.warn("  1. ADMIN:    admin@supportdesk.com    / Admin@123");
        log.warn("  2. AGENT:    agent@supportdesk.com    / Agent@123");
        log.warn("  3. CUSTOMER: customer@supportdesk.com / Customer@123");
        log.warn("  Total Tickets Seeded: {}", ticketRepository.count());
        log.warn("========================================================");
    }

    private Organization createDefaultOrganization() {
        Organization org = Organization.builder()
                .name("TechFlow Solutions")
                .domain("IT_SOFTWARE")
                .description("Default Enterprise IT Support Organization")
                .active(true)
                .build();
        org = organizationRepository.save(org);
        log.info("Created default organization: {}", org.getName());
        return org;
    }

    private User createDefaultAdmin(Organization org) {
        User admin = User.builder()
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .role(UserRole.ADMIN)
                .active(true)
                .organization(org)
                .phone("+1-555-0100")
                .build();
        admin = userRepository.save(admin);
        log.info("Created default admin user: {}", adminEmail);
        return admin;
    }

    private User createDefaultAgent(Organization org) {
        User agent = User.builder()
                .email("agent@supportdesk.com")
                .passwordHash(passwordEncoder.encode("Agent@123"))
                .firstName("Alex")
                .lastName("Agent")
                .role(UserRole.AGENT)
                .active(true)
                .organization(org)
                .phone("+1-555-0102")
                .build();
        agent = userRepository.save(agent);
        log.info("Created default agent: agent@supportdesk.com");
        return agent;
    }

    private User createDefaultCustomer(Organization org) {
        User customer = User.builder()
                .email("customer@supportdesk.com")
                .passwordHash(passwordEncoder.encode("Customer@123"))
                .firstName("Chris")
                .lastName("Customer")
                .role(UserRole.CUSTOMER)
                .active(true)
                .organization(org)
                .phone("+1-555-0103")
                .build();
        customer = userRepository.save(customer);
        log.info("Created default customer: customer@supportdesk.com");
        return customer;
    }

    private void createDefaultSlaPolicies(Organization org) {
        if (slaPolicyRepository.findByOrganizationId(org.getId()).isEmpty()) {
            createSlaPolicy(org, "Urgent SLA", Priority.URGENT, 15, 240);      // 15 min response, 4 hr resolution
            createSlaPolicy(org, "High SLA", Priority.HIGH, 60, 480);           // 1 hr response, 8 hr resolution
            createSlaPolicy(org, "Medium SLA", Priority.MEDIUM, 240, 1440);     // 4 hr response, 24 hr resolution
            createSlaPolicy(org, "Low SLA", Priority.LOW, 480, 2880);           // 8 hr response, 48 hr resolution
            log.info("Created default SLA policies");
        }
    }

    private void createSlaPolicy(Organization org, String name, Priority priority,
                                 int responseMinutes, int resolutionMinutes) {
        SlaPolicy policy = SlaPolicy.builder()
                .name(name)
                .priority(priority)
                .firstResponseMinutes(responseMinutes)
                .resolutionMinutes(resolutionMinutes)
                .organization(org)
                .active(true)
                .build();
        slaPolicyRepository.save(policy);
    }

    private void createDefaultTeams(Organization org) {
        if (supportTeamRepository.findByOrganizationId(org.getId()).isEmpty()) {
            createTeam(org, "Technical Support", "Handles technical issues and bug reports");
            createTeam(org, "Billing Support", "Handles billing and payment inquiries");
            createTeam(org, "General Support", "Handles general inquiries and requests");
            log.info("Created default support teams");
        }
    }

    private void createTeam(Organization org, String name, String description) {
        SupportTeam team = SupportTeam.builder()
                .name(name)
                .description(description)
                .organization(org)
                .active(true)
                .build();
        supportTeamRepository.save(team);
    }

    private void createDefaultCategories(Organization org) {
        if (ticketCategoryRepository.findByOrganizationId(org.getId()).isEmpty()) {
            createCategory(org, "General Inquiry", "General questions and information requests");
            createCategory(org, "Technical Support", "Technical issues and troubleshooting");
            createCategory(org, "Billing Support", "Billing, payment, and subscription issues");
            createCategory(org, "Bug Report", "Software bugs and defects");
            createCategory(org, "Feature Request", "Feature suggestions and enhancements");
            createCategory(org, "Login Issues", "Login, password, and authentication problems");
            createCategory(org, "Payment Issues", "Payment processing and refund requests");
            log.info("Created default categories");
        }
    }

    private void createCategory(Organization org, String name, String description) {
        TicketCategory category = TicketCategory.builder()
                .name(name)
                .description(description)
                .organization(org)
                .active(true)
                .build();
        ticketCategoryRepository.save(category);
    }

    private void setupTeamMemberships(Organization org, User agent) {
        for (SupportTeam team : supportTeamRepository.findByOrganizationId(org.getId())) {
            if (!teamMembershipRepository.existsByTeamIdAndUserId(team.getId(), agent.getId())) {
                TeamMembership membership = TeamMembership.builder()
                        .team(team)
                        .user(agent)
                        .isTeamLead(team.getName().contains("Technical"))
                        .build();
                teamMembershipRepository.save(membership);
            }
        }
    }

    private void createSampleTickets(Organization org, User admin, User agent, User customer) {
        log.info("Seeding realistic sample tickets across domains and statuses...");

        var teams = supportTeamRepository.findByOrganizationId(org.getId());
        SupportTeam techTeam = teams.stream().filter(t -> t.getName().contains("Technical")).findFirst().orElse(null);
        SupportTeam billingTeam = teams.stream().filter(t -> t.getName().contains("Billing")).findFirst().orElse(null);
        SupportTeam generalTeam = teams.stream().filter(t -> t.getName().contains("General")).findFirst().orElse(null);

        var categories = ticketCategoryRepository.findByOrganizationId(org.getId());
        TicketCategory catTech = categories.stream().filter(c -> c.getName().contains("Technical")).findFirst().orElse(null);
        TicketCategory catLogin = categories.stream().filter(c -> c.getName().contains("Login")).findFirst().orElse(null);
        TicketCategory catBilling = categories.stream().filter(c -> c.getName().contains("Billing")).findFirst().orElse(null);
        TicketCategory catPayment = categories.stream().filter(c -> c.getName().contains("Payment")).findFirst().orElse(null);
        TicketCategory catBug = categories.stream().filter(c -> c.getName().contains("Bug")).findFirst().orElse(null);
        TicketCategory catGeneral = categories.stream().filter(c -> c.getName().contains("General")).findFirst().orElse(null);
        TicketCategory catFeature = categories.stream().filter(c -> c.getName().contains("Feature")).findFirst().orElse(null);

        SlaPolicy slaUrgent = slaPolicyRepository.findByOrganizationIdAndPriority(org.getId(), Priority.URGENT).orElse(null);
        SlaPolicy slaHigh = slaPolicyRepository.findByOrganizationIdAndPriority(org.getId(), Priority.HIGH).orElse(null);
        SlaPolicy slaMedium = slaPolicyRepository.findByOrganizationIdAndPriority(org.getId(), Priority.MEDIUM).orElse(null);
        SlaPolicy slaLow = slaPolicyRepository.findByOrganizationIdAndPriority(org.getId(), Priority.LOW).orElse(null);

        LocalDateTime now = LocalDateTime.now();

        // -------------------------------------------------------------
        // Ticket 1: RESOLVED (IT / Software, High Priority)
        // -------------------------------------------------------------
        LocalDateTime t1Created = now.minusDays(2).minusHours(3);
        LocalDateTime t1FirstResp = t1Created.plusMinutes(25);
        LocalDateTime t1Resolved = now.minusDays(1).minusHours(1);

        Ticket t1 = Ticket.builder()
                .ticketNumber("SD-2026-00001")
                .organization(org)
                .customer(customer)
                .subject("VPN Gateway Connection Failure (Error 800)")
                .description("Cannot connect to corporate VPN gateway after latest desktop client update. Getting Error 800: Remote connection was not made.")
                .domain("IT_SOFTWARE")
                .category(catTech)
                .priority(Priority.HIGH)
                .status(TicketStatus.RESOLVED)
                .assignedTeam(techTeam)
                .assignedAgent(agent)
                .slaPolicy(slaHigh)
                .firstResponseDeadline(t1Created.plusHours(1))
                .resolutionDeadline(t1Created.plusHours(8))
                .firstResponseAt(t1FirstResp)
                .resolvedAt(t1Resolved)
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .createdAt(t1Created)
                .updatedAt(t1Resolved)
                .build();
        t1 = ticketRepository.save(t1);

        addReply(t1, agent, "Hello Chris, please flush your local DNS cache (`ipconfig /flushdns`) and download patch v4.2.1.", false, t1FirstResp);
        addReply(t1, customer, "Applied v4.2.1 patch and verified connection is restored. Thank you!", false, t1FirstResp.plusHours(2));
        addReply(t1, agent, "Great! Marking this ticket as resolved. Feel free to reopen if any further issues occur.", false, t1Resolved);
        addReply(t1, agent, "Root cause: Expired intermediate certificate in v4.2.0 bundle.", true, t1Resolved.plusMinutes(2));

        addHistory(t1, customer, "STATUS", null, "OPEN", "Ticket submitted by customer", t1Created);
        addHistory(t1, admin, "ASSIGNMENT", "Unassigned", "Alex Agent", "Assigned via Technical Rule", t1Created.plusMinutes(10));
        addHistory(t1, agent, "STATUS", "OPEN", "IN_PROGRESS", "Investigation started", t1FirstResp);
        addHistory(t1, agent, "STATUS", "IN_PROGRESS", "RESOLVED", "Issue confirmed resolved by customer", t1Resolved);

        // -------------------------------------------------------------
        // Ticket 2: IN_PROGRESS (IT / Software, Urgent Priority)
        // -------------------------------------------------------------
        LocalDateTime t2Created = now.minusHours(2);
        LocalDateTime t2FirstResp = t2Created.plusMinutes(10);

        Ticket t2 = Ticket.builder()
                .ticketNumber("SD-2026-00002")
                .organization(org)
                .customer(customer)
                .subject("SSO Single Sign-On 500 Internal Error during OAuth Callback")
                .description("Production staff are unable to log in via Okta OAuth SSO. The callback returns HTTP 500 with invalid token signature.")
                .domain("IT_SOFTWARE")
                .category(catLogin)
                .priority(Priority.URGENT)
                .status(TicketStatus.IN_PROGRESS)
                .assignedTeam(techTeam)
                .assignedAgent(agent)
                .slaPolicy(slaUrgent)
                .firstResponseDeadline(t2Created.plusMinutes(15))
                .resolutionDeadline(t2Created.plusHours(4))
                .firstResponseAt(t2FirstResp)
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .createdAt(t2Created)
                .updatedAt(t2FirstResp)
                .build();
        t2 = ticketRepository.save(t2);

        addReply(t2, agent, "We are actively investigating the SSO certificate signing keys in production auth cluster.", false, t2FirstResp);
        addReply(t2, agent, "Rotated JWKS cache on cluster nodes 1-3. Waiting for node 4 sync.", true, t2FirstResp.plusMinutes(20));

        addHistory(t2, customer, "STATUS", null, "OPEN", "Ticket submitted", t2Created);
        addHistory(t2, agent, "STATUS", "OPEN", "IN_PROGRESS", "Assigned and under urgent diagnosis", t2FirstResp);

        // -------------------------------------------------------------
        // Ticket 3: WAITING_FOR_CUSTOMER (E-Commerce / Billing, Medium Priority)
        // -------------------------------------------------------------
        LocalDateTime t3Created = now.minusDays(1).minusHours(4);
        LocalDateTime t3FirstResp = t3Created.plusMinutes(45);

        Ticket t3 = Ticket.builder()
                .ticketNumber("SD-2026-00003")
                .organization(org)
                .customer(customer)
                .subject("Duplicate Charge on Annual Subscription Renewal")
                .description("Our corporate credit card was charged twice for invoice #INV-98214 ($1,200 each). Please refund the duplicate transaction.")
                .domain("ECOMMERCE")
                .category(catBilling)
                .priority(Priority.MEDIUM)
                .status(TicketStatus.WAITING_FOR_CUSTOMER)
                .assignedTeam(billingTeam)
                .assignedAgent(agent)
                .slaPolicy(slaMedium)
                .firstResponseDeadline(t3Created.plusHours(4))
                .resolutionDeadline(t3Created.plusHours(24))
                .firstResponseAt(t3FirstResp)
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .createdAt(t3Created)
                .updatedAt(t3FirstResp)
                .build();
        t3 = ticketRepository.save(t3);

        addReply(t3, agent, "Hello Chris, we located the duplicate pending hold on Stripe gateway. Could you please confirm the last 4 digits of the card used for the transaction?", false, t3FirstResp);
        addHistory(t3, customer, "STATUS", null, "OPEN", "Created", t3Created);
        addHistory(t3, agent, "STATUS", "OPEN", "WAITING_FOR_CUSTOMER", "Pending verification of card digits", t3FirstResp);

        // -------------------------------------------------------------
        // Ticket 4: OPEN / UNASSIGNED QUEUE (Education, Low Priority)
        // -------------------------------------------------------------
        LocalDateTime t4Created = now.minusHours(4);

        Ticket t4 = Ticket.builder()
                .ticketNumber("SD-2026-00004")
                .organization(org)
                .customer(customer)
                .subject("Official Academic Transcript Verification Request")
                .description("Enquiring about the automated verification process for graduation certificates for Fall 2026 admissions.")
                .domain("EDUCATION")
                .category(catGeneral)
                .priority(Priority.LOW)
                .status(TicketStatus.OPEN)
                .assignedTeam(generalTeam)
                .assignedAgent(null) // In team unassigned queue!
                .slaPolicy(slaLow)
                .firstResponseDeadline(t4Created.plusHours(8))
                .resolutionDeadline(t4Created.plusHours(48))
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .createdAt(t4Created)
                .updatedAt(t4Created)
                .build();
        ticketRepository.save(t4);

        // -------------------------------------------------------------
        // Ticket 5: OVERDUE / SLA BREACHED (Banking, Urgent Priority)
        // -------------------------------------------------------------
        LocalDateTime t5Created = now.minusHours(18);
        LocalDateTime t5FirstRespDeadline = t5Created.plusMinutes(15);
        LocalDateTime t5ResolutionDeadline = t5Created.plusHours(4);
        LocalDateTime t5FirstResp = t5Created.plusHours(2); // Breached first response!

        Ticket t5 = Ticket.builder()
                .ticketNumber("SD-2026-00005")
                .organization(org)
                .customer(customer)
                .subject("Disputed High-Value Wire Transfer #WT-44129")
                .description("Customer flagged an unauthorized international wire transfer requiring immediate freeze and compliance investigation.")
                .domain("BANKING")
                .category(catPayment)
                .priority(Priority.URGENT)
                .status(TicketStatus.IN_PROGRESS)
                .assignedTeam(billingTeam)
                .assignedAgent(agent)
                .slaPolicy(slaUrgent)
                .firstResponseDeadline(t5FirstRespDeadline)
                .resolutionDeadline(t5ResolutionDeadline)
                .firstResponseAt(t5FirstResp)
                .slaFirstResponseBreached(true)
                .slaResolutionBreached(true)
                .createdAt(t5Created)
                .updatedAt(now.minusHours(1))
                .build();
        t5 = ticketRepository.save(t5);

        addReply(t5, agent, "Transfer hold initiated with Federal Reserve gateway. Fraud operations unit notified.", false, t5FirstResp);
        addReply(t5, agent, "Risk team review pending compliance approval. Ticket exceeded standard 4hr resolution target.", true, now.minusHours(2));

        // Create SlaBreach records
        createBreach(t5, slaUrgent, BreachType.FIRST_RESPONSE, t5FirstRespDeadline, t5FirstRespDeadline.plusMinutes(1));
        createBreach(t5, slaUrgent, BreachType.RESOLUTION, t5ResolutionDeadline, t5ResolutionDeadline.plusMinutes(1));

        addHistory(t5, customer, "STATUS", null, "OPEN", "Submitted by customer", t5Created);
        addHistory(t5, agent, "STATUS", "OPEN", "IN_PROGRESS", "Investigation escalated", t5FirstResp);

        // -------------------------------------------------------------
        // Ticket 6: CLOSED (Healthcare, Medium Priority)
        // -------------------------------------------------------------
        LocalDateTime t6Created = now.minusDays(5);
        LocalDateTime t6FirstResp = t6Created.plusHours(1);
        LocalDateTime t6Resolved = now.minusDays(4);
        LocalDateTime t6Closed = now.minusDays(3);

        Ticket t6 = Ticket.builder()
                .ticketNumber("SD-2026-00006")
                .organization(org)
                .customer(customer)
                .subject("Tele-health Video Consultation Stuttering on Chrome")
                .description("Doctors reporting WebRTC audio packet drops and freezing video frames when conducting patient remote consults.")
                .domain("HEALTHCARE")
                .category(catTech)
                .priority(Priority.MEDIUM)
                .status(TicketStatus.CLOSED)
                .assignedTeam(techTeam)
                .assignedAgent(agent)
                .slaPolicy(slaMedium)
                .firstResponseDeadline(t6Created.plusHours(4))
                .resolutionDeadline(t6Created.plusHours(24))
                .firstResponseAt(t6FirstResp)
                .resolvedAt(t6Resolved)
                .closedAt(t6Closed)
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .createdAt(t6Created)
                .updatedAt(t6Closed)
                .build();
        t6 = ticketRepository.save(t6);

        addReply(t6, agent, "Turn server bandwidth limits increased from 2Mbps to 10Mbps per stream. Latency metrics stabilized.", false, t6FirstResp);
        addReply(t6, customer, "Confirmed video call quality is crisp and smooth throughout the clinic morning session.", false, t6Resolved);
        addHistory(t6, customer, "STATUS", null, "OPEN", "Ticket created", t6Created);
        addHistory(t6, agent, "STATUS", "OPEN", "RESOLVED", "Config updated", t6Resolved);
        addHistory(t6, admin, "STATUS", "RESOLVED", "CLOSED", "Ticket auto-closed after 24h", t6Closed);

        // -------------------------------------------------------------
        // Ticket 7: OPEN / UNASSIGNED QUEUE (General Business, Low Priority)
        // -------------------------------------------------------------
        LocalDateTime t7Created = now.minusHours(1);

        Ticket t7 = Ticket.builder()
                .ticketNumber("SD-2026-00007")
                .organization(org)
                .customer(customer)
                .subject("Dark Mode PDF Export Support for Executive Reports")
                .description("Would love the ability to export monthly executive dashboard charts with a clean dark theme styling.")
                .domain("GENERAL")
                .category(catFeature)
                .priority(Priority.LOW)
                .status(TicketStatus.OPEN)
                .assignedTeam(generalTeam)
                .assignedAgent(null) // Unassigned in queue
                .slaPolicy(slaLow)
                .firstResponseDeadline(t7Created.plusHours(8))
                .resolutionDeadline(t7Created.plusHours(48))
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .createdAt(t7Created)
                .updatedAt(t7Created)
                .build();
        ticketRepository.save(t7);

        // -------------------------------------------------------------
        // Ticket 8: RESOLVED (IT / Software, Bug Report, High Priority)
        // -------------------------------------------------------------
        LocalDateTime t8Created = now.minusDays(3).minusHours(2);
        LocalDateTime t8FirstResp = t8Created.plusMinutes(30);
        LocalDateTime t8Resolved = now.minusDays(2).minusHours(5);

        Ticket t8 = Ticket.builder()
                .ticketNumber("SD-2026-00008")
                .organization(org)
                .customer(customer)
                .subject("HikariCP Connection Pool Exhaustion on Nightly Batch")
                .description("Nightly synchronization job at 02:00 UTC hung with ConnectionTimeoutException: Connection is not available.")
                .domain("IT_SOFTWARE")
                .category(catBug)
                .priority(Priority.HIGH)
                .status(TicketStatus.RESOLVED)
                .assignedTeam(techTeam)
                .assignedAgent(agent)
                .slaPolicy(slaHigh)
                .firstResponseDeadline(t8Created.plusHours(1))
                .resolutionDeadline(t8Created.plusHours(8))
                .firstResponseAt(t8FirstResp)
                .resolvedAt(t8Resolved)
                .slaFirstResponseBreached(false)
                .slaResolutionBreached(false)
                .createdAt(t8Created)
                .updatedAt(t8Resolved)
                .build();
        t8 = ticketRepository.save(t8);

        addReply(t8, agent, "Found unclosed ResultSet in BatchProcessor.java line 88. Fixed and deployed hotfix patch.", false, t8FirstResp);
        addReply(t8, customer, "Nightly batch ran in 14 minutes without errors. Resolution confirmed.", false, t8Resolved);
        addHistory(t8, customer, "STATUS", null, "OPEN", "Reported", t8Created);
        addHistory(t8, agent, "STATUS", "OPEN", "RESOLVED", "Hotfix released", t8Resolved);

        log.info("Successfully seeded 8 tickets with replies, history, and SLA breach data!");
    }

    private void addReply(Ticket ticket, User user, String message, boolean internalNote, LocalDateTime timestamp) {
        TicketReply reply = TicketReply.builder()
                .ticket(ticket)
                .user(user)
                .message(message)
                .internalNote(internalNote)
                .createdAt(timestamp)
                .build();
        ticketReplyRepository.save(reply);
    }

    private void addHistory(Ticket ticket, User user, String field, String oldVal, String newVal, String comment, LocalDateTime timestamp) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .user(user)
                .fieldChanged(field)
                .oldValue(oldVal)
                .newValue(newVal)
                .comment(comment)
                .createdAt(timestamp)
                .build();
        ticketHistoryRepository.save(history);
    }

    private void createBreach(Ticket ticket, SlaPolicy policy, BreachType type, LocalDateTime deadline, LocalDateTime breachedAt) {
        SlaBreach breach = SlaBreach.builder()
                .ticket(ticket)
                .slaPolicy(policy)
                .breachType(type)
                .deadline(deadline)
                .breachedAt(breachedAt)
                .build();
        slaBreachRepository.save(breach);
    }
}
