package com.example.pulsedesktickettriage.service;

import com.example.pulsedesktickettriage.ai.AiAnalysisService;
import com.example.pulsedesktickettriage.ai.TicketAnalysisResult;
import com.example.pulsedesktickettriage.dto.CommentRequest;
import com.example.pulsedesktickettriage.dto.CommentResponse;
import com.example.pulsedesktickettriage.model.Comment;
import com.example.pulsedesktickettriage.model.CommentStatus;
import com.example.pulsedesktickettriage.repository.CommentRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final CommentRepository commentRepository;
    private final AiAnalysisService aiAnalysisService;
    private final TicketService ticketService;

    public CommentService(CommentRepository commentRepository,
                          AiAnalysisService aiAnalysisService,
                          TicketService ticketService) {
        this.commentRepository = commentRepository;
        this.aiAnalysisService = aiAnalysisService;
        this.ticketService = ticketService;
    }

    public CommentResponse createComment(CommentRequest request) {
        Comment comment = commentRepository.save(new Comment(request.getText(), request.getChannel()));

        TicketAnalysisResult result;
        try {
            result = aiAnalysisService.analyze(comment.getText());
        } catch (org.springframework.web.client.RestClientException e) {
            log.warn("AI analysis failed for comment {}", comment.getId(), e);
            comment.setStatus(CommentStatus.ANALYSIS_FAILED);
            return toResponse(commentRepository.save(comment));
        }

        if (result.shouldCreateTicket()) {
            ticketService.createTicket(comment, result);
        }
        comment.setStatus(CommentStatus.ANALYZED);
        return toResponse(commentRepository.save(comment));
    }

    public List<CommentResponse> getAllComments() {
        return commentRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText(), comment.getChannel(),
                comment.getStatus(), comment.getCreatedAt());
    }
}
