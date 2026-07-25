package com.example.pulsedesktickettriage.service;

import com.example.pulsedesktickettriage.ai.TicketAnalysisResult;
import com.example.pulsedesktickettriage.dto.TicketResponse;
import com.example.pulsedesktickettriage.model.Comment;
import com.example.pulsedesktickettriage.model.Ticket;
import com.example.pulsedesktickettriage.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket createTicket(Comment comment, TicketAnalysisResult result) {
        Ticket ticket = new Ticket(comment, result.title(), result.category(), result.priority(), result.summary());
        return ticketRepository.save(ticket);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream().map(this::toResponse).toList();
    }

    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket " + id + " not found"));
        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getComment().getId(), ticket.getTitle(),
                ticket.getCategory(), ticket.getPriority(), ticket.getSummary(), ticket.getCreatedAt());
    }
}
