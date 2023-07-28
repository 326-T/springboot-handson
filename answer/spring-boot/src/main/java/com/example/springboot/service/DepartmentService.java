package com.example.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.springboot.persistence.repository.DepartmentMapper;
import com.example.springboot.persistence.entity.Department;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    public Department findById(Long id) {
        return departmentMapper.findById(id);
    }

    public Department insert(Department department) {
        return departmentMapper.insert(department);
    }

    public Department update(Department department) {
        return departmentMapper.update(department);
    }

    public void deleteById(Long id) {
        departmentMapper.deleteById(id);
    }
}
