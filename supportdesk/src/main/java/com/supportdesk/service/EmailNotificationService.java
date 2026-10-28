package com.supportdesk.service;

import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${spring.mail.username:noreply@supportdesk.com}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailNotificationService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean isMailEnabled() {
        return mailEnabled && mailSender != null;
    }

    public void sendTicketCreated(Ticket ticket) {
        if (!isMailEnabled()) {
            log.info("[EMAIL DISABLED] Ticket created notification suppressed for {}", ticket.getTicketNumber());
            return;
        }

        String to = ticket.getCustomer().getEmail();
        String subject = String.format("[SupportDesk] Ticket Created: %s - %s", ticket.getTicketNumber(), ticket.getSubject());
        String body = String.format("Hello %s,\n\nYour support ticket %s has been received.\n\nSubject: %s\nPriority: %s\nStatus: %s\n\nYou can track and reply to your ticket from your customer dashboard.\n\nBest regards,\nSupportDesk Team",
                ticket.getCustomer().getFullName(), ticket.getTicketNumber(), ticket.getSubject(), ticket.getPriority(), ticket.getStatus());

        sendMail(to, subject, body);
    }

    public void sendTicketAssigned(Ticket ticket) {
        if (!isMailEnabled() || ticket.getAssignedAgent() == null) {
            log.info("[EMAIL DISABLED] Ticket assignment notification suppressed for {}", ticket.getTicketNumber());
            return;
        }

        String to = ticket.getAssignedAgent().getEmail();
        String subject = String.format("[SupportDesk] Ticket Assigned: %s", ticket.getTicketNumber());
        String body = String.format("Hello %s,\n\nYou have been assigned ticket %s.\n\nSubject: %s\nPriority: %s\nCustomer: %s\n\nPlease review it in your agent dashboard.\n\nBest regards,\nSupportDesk Team",
                ticket.getAssignedAgent().getFullName(), ticket.getTicketNumber(), ticket.getSubject(), ticket.getPriority(), ticket.getCustomer().getFullName());

        sendMail(to, subject, body);
    }

    public void sendStatusUpdate(Ticket ticket, String oldStatus, String newStatus) {
        if (!isMailEnabled()) {
            log.info("[EMAIL DISABLED] Status update notification suppressed for {}", ticket.getTicketNumber());
            return;
        }

        String to = ticket.getCustomer().getEmail();
        String subject = String.format("[SupportDesk] Status Changed: %s is now %s", ticket.getTicketNumber(), newStatus);
        String body = String.format("Hello %s,\n\nThe status of your ticket %s has changed from %s to %s.\n\nBest regards,\nSupportDesk Team",
                ticket.getCustomer().getFullName(), ticket.getTicketNumber(), oldStatus, newStatus);

        sendMail(to, subject, body);
    }

    public void sendAgentReply(Ticket ticket, String message) {
        if (!isMailEnabled()) {
            log.info("[EMAIL DISABLED] Reply notification suppressed for {}", ticket.getTicketNumber());
            return;
        }

        String to = ticket.getCustomer().getEmail();
        String subject = String.format("[SupportDesk] New Reply on Ticket %s", ticket.getTicketNumber());
        String body = String.format("Hello %s,\n\nAn agent replied to your ticket %s:\n\n\"%s\"\n\nYou can view and reply at your customer portal.\n\nBest regards,\nSupportDesk Team",
                ticket.getCustomer().getFullName(), ticket.getTicketNumber(), message);

        sendMail(to, subject, body);
    }

    public void sendResolutionNotification(Ticket ticket) {
        if (!isMailEnabled()) {
            log.info("[EMAIL DISABLED] Resolution notification suppressed for {}", ticket.getTicketNumber());
            return;
        }

        String to = ticket.getCustomer().getEmail();
        String subject = String.format("[SupportDesk] Ticket %s Has Been Resolved", ticket.getTicketNumber());
        String body = String.format("Hello %s,\n\nYour support ticket %s has been marked as RESOLVED.\nIf you feel your issue is not completely solved, you may reopen it from your customer portal.\n\nThank you,\nSupportDesk Team",
                ticket.getCustomer().getFullName(), ticket.getTicketNumber());

        sendMail(to, subject, body);
    }

    private void sendMail(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email successfully sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

