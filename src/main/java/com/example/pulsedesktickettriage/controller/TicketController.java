package com.example.pulsedesktickettriage.controller;

import com.example.pulsedesktickettriage.dto.TicketResponse;
import com.example.pulsedesktickettriage.service.TicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{ticketId}")
    public TicketResponse getTicket(@PathVariable Long ticketId) {
        return ticketService.getTicketById(ticketId);
    }
}
