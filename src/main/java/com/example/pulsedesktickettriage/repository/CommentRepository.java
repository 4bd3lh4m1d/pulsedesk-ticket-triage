package com.example.pulsedesktickettriage.repository;

import com.example.pulsedesktickettriage.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
