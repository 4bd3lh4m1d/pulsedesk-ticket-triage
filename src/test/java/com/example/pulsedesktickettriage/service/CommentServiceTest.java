package com.example.pulsedesktickettriage.service;

import com.example.pulsedesktickettriage.ai.AiAnalysisService;
import com.example.pulsedesktickettriage.ai.TicketAnalysisResult;
import com.example.pulsedesktickettriage.dto.CommentRequest;
import com.example.pulsedesktickettriage.dto.CommentResponse;
import com.example.pulsedesktickettriage.model.Comment;
import com.example.pulsedesktickettriage.model.CommentStatus;
import com.example.pulsedesktickettriage.model.TicketCategory;
import com.example.pulsedesktickettriage.model.TicketPriority;
import com.example.pulsedesktickettriage.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AiAnalysisService aiAnalysisService;

    @Mock
    private TicketService ticketService;

    private CommentService service() {
        return new CommentService(commentRepository, aiAnalysisService, ticketService);
    }

    private void stubSaveReturnsArgumentWithId() {
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            if (c.getId() == null) {
                c.setId(1L);
            }
            return c;
        });
    }

    private static TicketAnalysisResult actionableResult() {
        return new TicketAnalysisResult(true, "App crashes on login",
                TicketCategory.BUG, TicketPriority.HIGH, "User reported a login crash");
    }

    private static CommentRequest request(String text, String channel) {
        CommentRequest request = new CommentRequest();
        request.setText(text);
        request.setChannel(channel);
        return request;
    }

    @Test
    void createComment_creates_ticket_when_analysis_says_actionable() {
        stubSaveReturnsArgumentWithId();
        TicketAnalysisResult result = actionableResult();
        when(aiAnalysisService.analyze("the app crashes")).thenReturn(result);

        CommentResponse response = service().createComment(request("the app crashes", "email"));

        verify(ticketService).createTicket(any(Comment.class), any(TicketAnalysisResult.class));
        assertThat(response.getStatus()).isEqualTo(CommentStatus.ANALYZED);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void createComment_skips_ticket_when_analysis_says_not_actionable() {
        stubSaveReturnsArgumentWithId();
        when(aiAnalysisService.analyze("love the app")).thenReturn(TicketAnalysisResult.noTicket());

        CommentResponse response = service().createComment(request("love the app", "email"));

        verify(ticketService, never()).createTicket(any(), any());
        assertThat(response.getStatus()).isEqualTo(CommentStatus.ANALYZED);
    }

    @Test
    void createComment_marks_analysis_failed_when_ai_call_throws() {
        stubSaveReturnsArgumentWithId();
        when(aiAnalysisService.analyze(anyString())).thenThrow(new RestClientException("HF is down"));

        CommentResponse response = service().createComment(request("the app crashes", "email"));

        verify(ticketService, never()).createTicket(any(), any());
        assertThat(response.getStatus()).isEqualTo(CommentStatus.ANALYSIS_FAILED);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getAllComments_maps_entity_fields_to_response() {
        Comment comment = new Comment("slow search", "chat");
        comment.setId(7L);
        comment.setStatus(CommentStatus.ANALYZED);
        when(commentRepository.findAll()).thenReturn(List.of(comment));

        List<CommentResponse> responses = service().getAllComments();

        assertThat(responses).hasSize(1);
        CommentResponse response = responses.get(0);
        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getText()).isEqualTo("slow search");
        assertThat(response.getChannel()).isEqualTo("chat");
        assertThat(response.getStatus()).isEqualTo(CommentStatus.ANALYZED);
        assertThat(response.getCreatedAt()).isEqualTo(comment.getCreatedAt());
    }
}
