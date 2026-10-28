package com.supportdesk.service;

import com.supportdesk.dto.TicketHistoryDTO;
import com.supportdesk.entity.Ticket;
import com.supportdesk.entity.TicketHistory;
import com.supportdesk.entity.User;
import com.supportdesk.repository.TicketHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository historyRepository;

    public TicketHistoryService(TicketHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Transactional
    public void recordChange(Ticket ticket, User user, String fieldChanged, String oldValue, String newValue, String comment) {
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .user(user)
                .fieldChanged(fieldChanged)
                .oldValue(oldValue)
                .newValue(newValue)
                .comment(comment)
                .build();
        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public List<TicketHistoryDTO> getHistory(Long ticketId) {
        return historyRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TicketHistoryDTO mapToDTO(TicketHistory history) {
        return TicketHistoryDTO.builder()
                .id(history.getId())
                .ticketId(history.getTicket().getId())
                .userId(history.getUser() != null ? history.getUser().getId() : null)
                .userName(history.getUser() != null ? history.getUser().getFullName() : "System")
                .userRole(history.getUser() != null && history.getUser().getRole() != null ? history.getUser().getRole().name() : "SYSTEM")
                .fieldChanged(history.getFieldChanged())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .comment(history.getComment())
                .createdAt(history.getCreatedAt())
                .build();
    }
}

