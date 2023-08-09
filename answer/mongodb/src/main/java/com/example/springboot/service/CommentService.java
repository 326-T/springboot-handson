package com.example.springboot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.persistence.entity.Comment;
import com.example.springboot.persistence.repository.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Comment findById(String id) {
        return commentRepository.findById(id).orElse(null);
    }

    public Long count() {
        return commentRepository.count();
    }

    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteById(String id) {
        commentRepository.deleteById(id);
    }

    public void deleteAll() {
        commentRepository.deleteAll();
    }
}
