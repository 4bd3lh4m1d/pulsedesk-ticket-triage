package com.example.pulsedesktickettriage.dto;

import com.example.pulsedesktickettriage.model.TicketCategory;
import com.example.pulsedesktickettriage.model.TicketPriority;

import java.time.Instant;

public class TicketResponse {

    private final Long id;
    private final Long commentId;
    private final String title;
    private final TicketCategory category;
    private final TicketPriority priority;
    private final String summary;
    private final Instant createdAt;

    public TicketResponse(Long id, Long commentId, String title, TicketCategory category,
                          TicketPriority priority, String summary, Instant createdAt) {
        this.id = id;
        this.commentId = commentId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.summary = summary;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCommentId() {
        return commentId;
    }

    public String getTitle() {
        return title;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public String getSummary() {
        return summary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
