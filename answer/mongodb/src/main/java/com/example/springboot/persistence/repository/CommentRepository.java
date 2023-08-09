package com.example.springboot.persistence.repository;

import org.springframework.stereotype.Repository;

import com.example.springboot.persistence.entity.Comment;

import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

}
