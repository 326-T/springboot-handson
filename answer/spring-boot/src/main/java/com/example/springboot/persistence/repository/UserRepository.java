package com.example.springboot.persistence.repository;

import org.springframework.stereotype.Repository;

import com.example.springboot.persistence.entity.User;

import org.springframework.data.mongodb.repository.MongoRepository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

}
