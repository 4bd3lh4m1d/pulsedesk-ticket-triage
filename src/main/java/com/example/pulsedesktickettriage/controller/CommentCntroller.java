package com.example.pulsedesktickettriage.controller;


import com.example.pulsedesktickettriage.dto.CommentRequest;
import com.example.pulsedesktickettriage.dto.CommentResponse;
import com.example.pulsedesktickettriage.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentCntroller {

    private final CommentService commentService;

    public CommentCntroller(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(request));
    }

    @GetMapping
    public List<CommentResponse> getAllComments() {
        return commentService.getAllComments();
    }
}
