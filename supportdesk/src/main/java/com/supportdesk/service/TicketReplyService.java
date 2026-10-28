package com.supportdesk.service;

import com.supportdesk.dto.ReplyCreateRequest;
import com.supportdesk.dto.ReplyDTO;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketReply;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.entity.User;
import com.supportdesk.entity.UserRole;
import com.supportdesk.exception.BadRequestException;
import com.supportdesk.exception.ResourceNotFoundException;
import com.supportdesk.exception.UnauthorizedAccessException;
import com.supportdesk.repository.TicketReplyRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketReplyService {

    private final TicketReplyRepository replyRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketHistoryService historyService;
    private final EmailNotificationService emailService;

    public TicketReplyService(TicketReplyRepository replyRepository,
                              TicketRepository ticketRepository,
                              UserRepository userRepository,
                              TicketHistoryService historyService,
                              EmailNotificationService emailService) {
        this.replyRepository = replyRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.historyService = historyService;
        this.emailService = emailService;
    }

    @Transactional
    public ReplyDTO addReply(Long ticketId, ReplyCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        SecurityUtils.validateOrgAccess(ticket.getOrganization().getId());

        // Customers cannot add internal notes
        boolean isInternal = Boolean.TRUE.equals(request.getInternalNote());
        if (currentUser.getRole() == UserRole.CUSTOMER && isInternal) {
            throw new BadRequestException("Customers cannot add internal notes");
        }

        // Validate customer ownership
        if (currentUser.getRole() == UserRole.CUSTOMER && !ticket.getCustomer().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("You cannot reply to another customer's ticket");
        }

        TicketReply reply = TicketReply.builder()
                .ticket(ticket)
                .user(currentUser)
                .message(request.getMessage())
                .internalNote(isInternal)
                .build();

        reply = replyRepository.save(reply);

        // If it's an agent or admin's first public reply, record first response time
        if (!isInternal && (currentUser.getRole() == UserRole.AGENT || currentUser.getRole() == UserRole.ADMIN)) {
            if (ticket.getFirstResponseAt() == null) {
                ticket.setFirstResponseAt(LocalDateTime.now());
                ticketRepository.save(ticket);
                historyService.recordChange(ticket, currentUser, "FIRST_RESPONSE", null, LocalDateTime.now().toString(), "First agent response recorded");
            }

            // If ticket was waiting for customer or open, can set to in progress or waiting
            if (ticket.getStatus() == TicketStatus.OPEN) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                ticketRepository.save(ticket);
            }

            emailService.sendAgentReply(ticket, request.getMessage());
        } else if (!isInternal && currentUser.getRole() == UserRole.CUSTOMER) {
            // Customer replied -> change status from WAITING_FOR_CUSTOMER to IN_PROGRESS
            if (ticket.getStatus() == TicketStatus.WAITING_FOR_CUSTOMER) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
                ticketRepository.save(ticket);
                historyService.recordChange(ticket, currentUser, "STATUS", TicketStatus.WAITING_FOR_CUSTOMER.name(), TicketStatus.IN_PROGRESS.name(), "Customer replied");
            }
        }

        historyService.recordChange(ticket, currentUser, isInternal ? "INTERNAL_NOTE" : "REPLY", null, null,
                isInternal ? "Internal note added" : "Public reply added");

        return mapToDTO(reply);
    }

    @Transactional(readOnly = true)
    public List<ReplyDTO> getReplies(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));

        SecurityUtils.validateOrgAccess(ticket.getOrganization().getId());

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        if (currentUser.getRole() == UserRole.CUSTOMER) {
            if (!ticket.getCustomer().getId().equals(currentUser.getId())) {
                throw new UnauthorizedAccessException("Access denied");
            }
            // Customers never see internal notes
            return replyRepository.findByTicketIdAndInternalNoteFalseOrderByCreatedAtAsc(ticketId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        return replyRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReplyDTO mapToDTO(TicketReply reply) {
        return ReplyDTO.builder()
                .id(reply.getId())
                .ticketId(reply.getTicket().getId())
                .userId(reply.getUser() != null ? reply.getUser().getId() : null)
                .userName(reply.getUser() != null ? reply.getUser().getFullName() : "System")
                .userRole(reply.getUser() != null && reply.getUser().getRole() != null ? reply.getUser().getRole().name() : "SYSTEM")
                .message(reply.getMessage())
                .internalNote(reply.getInternalNote())
                .createdAt(reply.getCreatedAt())
                .build();
    }
}

