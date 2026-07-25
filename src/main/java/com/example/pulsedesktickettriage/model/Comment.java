package com.example.pulsedesktickettriage.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String text;

    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status=CommentStatus.RECEIVED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Comment(String text, String channel) {
        this.text = text;
        this.channel = channel;
    }

    protected Comment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant created) {
        this.createdAt = created;
    }
}
