package com.example.pulsedesktickettriage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    @NotBlank(message = "text must not be blank")
    @Size(max = 2000, message = "text must be at most 2000 characters")
    private String text;

    private String channel;

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public CommentRequest() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
