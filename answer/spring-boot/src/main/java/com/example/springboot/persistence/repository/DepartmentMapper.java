package com.example.springboot.persistence.repository;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.springboot.persistence.entity.Department;

@Mapper
public interface DepartmentMapper {

    @Select("SELECT * FROM department WHERE id = #{id}")
    Department findById(Long id);

    @Insert("INSERT INTO department(name, description) VALUES(#{name}, #{description})")
    Department insert(Department department);

    @Update("UPDATE department SET name = #{name}, description = #{description} WHERE id = #{id}")
    Department update(Department department);

    @Delete("DELETE FROM department WHERE id = #{id}")
    void deleteById(Long id);
}
