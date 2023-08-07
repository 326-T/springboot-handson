package com.example.springboot.persistence.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.springboot.persistence.entity.User;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users;")
    List<User> findAll();

    @Select("SELECT * FROM users WHERE id = #{id};")
    User findById(Integer id);

    @Insert("INSERT INTO users(name, email) VALUES(#{name}, #{email});")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User users);

    @Update("UPDATE users SET name = #{name}, email = #{email} WHERE id = #{id};")
    void update(User users);

    @Delete("DELETE FROM users WHERE id = #{id};")
    void deleteById(Integer id);
}
