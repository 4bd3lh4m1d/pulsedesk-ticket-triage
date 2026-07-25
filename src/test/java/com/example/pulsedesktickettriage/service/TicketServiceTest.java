package com.example.pulsedesktickettriage.service;

import com.example.pulsedesktickettriage.ai.TicketAnalysisResult;
import com.example.pulsedesktickettriage.dto.TicketResponse;
import com.example.pulsedesktickettriage.model.Comment;
import com.example.pulsedesktickettriage.model.Ticket;
import com.example.pulsedesktickettriage.model.TicketCategory;
import com.example.pulsedesktickettriage.model.TicketPriority;
import com.example.pulsedesktickettriage.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    private TicketService service() {
        return new TicketService(ticketRepository);
    }

    private static Comment commentWithId(long id) {
        Comment comment = new Comment("the app crashes on login", "email");
        comment.setId(id);
        return comment;
    }

    private static Ticket ticketWithId(long ticketId, Comment comment) {
        Ticket ticket = new Ticket(comment, "App crashes on login",
                TicketCategory.BUG, TicketPriority.HIGH, "User reported a login crash");
        ticket.setId(ticketId);
        return ticket;
    }

    @Test
    void createTicket_builds_ticket_from_comment_and_analysis() {
        Comment comment = commentWithId(3L);
        TicketAnalysisResult result = new TicketAnalysisResult(true, "App crashes on login",
                TicketCategory.BUG, TicketPriority.HIGH, "User reported a login crash");
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service().createTicket(comment, result);

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket saved = captor.getValue();
        assertThat(saved.getComment()).isSameAs(comment);
        assertThat(saved.getTitle()).isEqualTo("App crashes on login");
        assertThat(saved.getCategory()).isEqualTo(TicketCategory.BUG);
        assertThat(saved.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(saved.getSummary()).isEqualTo("User reported a login crash");
    }

    @Test
    void getTicketById_returns_mapped_response_when_found() {
        Ticket ticket = ticketWithId(5L, commentWithId(3L));
        when(ticketRepository.findById(5L)).thenReturn(Optional.of(ticket));

        TicketResponse response = service().getTicketById(5L);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getCommentId()).isEqualTo(3L);
        assertThat(response.getTitle()).isEqualTo("App crashes on login");
        assertThat(response.getCategory()).isEqualTo(TicketCategory.BUG);
        assertThat(response.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(response.getSummary()).isEqualTo("User reported a login crash");
        assertThat(response.getCreatedAt()).isEqualTo(ticket.getCreatedAt());
    }

    @Test
    void getTicketById_throws_404_when_missing() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getTicketById(999L))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).isEqualTo("Ticket 999 not found");
                });
    }

    @Test
    void getAllTickets_maps_all_tickets() {
        Ticket first = ticketWithId(1L, commentWithId(10L));
        Ticket second = ticketWithId(2L, commentWithId(20L));
        when(ticketRepository.findAll()).thenReturn(List.of(first, second));

        List<TicketResponse> responses = service().getAllTickets();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getCommentId()).isEqualTo(10L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);
        assertThat(responses.get(1).getCommentId()).isEqualTo(20L);
    }
}
