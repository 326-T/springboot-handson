package com.example.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.persistence.entity.User;
import com.example.springboot.persistence.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public Boolean existsById(String id) {
        return userRepository.existsById(id);
    }

    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    public Long count() {
        return userRepository.count();
    }

    public void deleteById(String id) {
        userRepository.deleteById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
}
