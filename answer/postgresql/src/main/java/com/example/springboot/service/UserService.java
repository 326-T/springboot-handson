package com.example.springboot.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.exception.exceptions.NotFoundException;
import com.example.springboot.persistence.entity.User;
import com.example.springboot.persistence.mapper.UserMapper;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> findAll() {
        return userMapper.findAll();
    }

    public User findById(Integer id) throws NotFoundException {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new NotFoundException("IDが %s のユーザーは存在しません。".formatted(id));
        }
        return user;
    }

    public void insert(User user) {
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.update(user);
    }

    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }
}
