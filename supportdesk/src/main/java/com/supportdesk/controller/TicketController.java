package com.supportdesk.controller;

import com.supportdesk.dto.*;
import com.supportdesk.entity.TicketStatus;
import com.supportdesk.security.SecurityUtils;
import com.supportdesk.service.ClassificationService;
import com.supportdesk.service.TicketHistoryService;
import com.supportdesk.service.TicketReplyService;
import com.supportdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final TicketReplyService replyService;
    private final TicketHistoryService historyService;
    private final ClassificationService classificationService;

    public TicketController(TicketService ticketService,
                            TicketReplyService replyService,
                            TicketHistoryService historyService,
                            ClassificationService classificationService) {
        this.ticketService = ticketService;
        this.replyService = replyService;
        this.historyService = historyService;
        this.classificationService = classificationService;
    }

    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        TicketDTO ticket = ticketService.createTicket(request, currentUserId);
        return new ResponseEntity<>(ticket, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageDTO<TicketDTO>> listTickets(TicketSearchRequest searchRequest) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        PageDTO<TicketDTO> tickets = ticketService.searchTickets(searchRequest, orgId, currentUserId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailDTO> getTicketDetail(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        TicketDetailDTO ticket = ticketService.getTicketDetail(id, currentUserId);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketDTO> updateStatus(@PathVariable Long id,
                                                  @RequestBody Map<String, String> body) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String statusStr = body.get("status");
        String comment = body.get("comment");
        TicketStatus status = TicketStatus.valueOf(statusStr.toUpperCase());
        TicketDTO updated = ticketService.updateTicketStatus(id, status, currentUserId, comment);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<ReplyDTO>> getReplies(@PathVariable Long id) {
        List<ReplyDTO> replies = replyService.getReplies(id);
        return ResponseEntity.ok(replies);
    }

    @PostMapping("/{id}/replies")
    public ResponseEntity<ReplyDTO> addReply(@PathVariable Long id,
                                             @Valid @RequestBody ReplyCreateRequest request) {
        ReplyDTO reply = replyService.addReply(id, request);
        return new ResponseEntity<>(reply, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketHistoryDTO>> getHistory(@PathVariable Long id) {
        List<TicketHistoryDTO> history = historyService.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/classify-preview")
    public ResponseEntity<ClassificationSuggestionDTO> getClassificationPreview(
            @RequestParam String subject,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String domain) {
        Long orgId = SecurityUtils.getCurrentOrgId();
        ClassificationSuggestionDTO suggestion = classificationService.classify(orgId, subject, description, domain);
        return ResponseEntity.ok(suggestion);
    }
}

