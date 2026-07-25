package com.example.pulsedesktickettriage.dto;

import com.example.pulsedesktickettriage.model.CommentStatus;

import java.time.Instant;

public class CommentResponse {

    private final Long id;
    private final String text;
    private final String channel;
    private final CommentStatus status;
    private final Instant createdAt;

    public CommentResponse(Long id, String text, String channel, CommentStatus status, Instant createdAt) {
        this.id = id;
        this.text = text;
        this.channel = channel;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getChannel() {
        return channel;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
