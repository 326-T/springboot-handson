package com.example.springboot.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.springboot.persistence.entity.User;
import com.example.springboot.persistence.mapper.UserMapper;

@SpringBootTest
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Test
    void findAll() {
        // given
        List<User> users = List.of(
                User.builder().id(1).name("太郎").email("xxx@example.com").build(),
                User.builder().id(2).name("次郎").email("yyy@example.com").build());
        when(userMapper.findAll()).thenReturn(users);
        // when
        List<User> actual = userService.findAll();
        // then
        assertThat(actual).hasSize(2);
        assertThat(actual.get(0))
                .extracting(User::getId, User::getName, User::getEmail)
                .containsExactly(1, "太郎", "xxx@example.com");
        assertThat(actual.get(1))
                .extracting(User::getId, User::getName, User::getEmail)
                .containsExactly(2, "次郎", "yyy@example.com");
    }

    @Test
    void findById() {
        // given
        User user = User.builder().id(1).name("太郎").email("xxx@example.com").build();
        when(userMapper.findById(1)).thenReturn(user);
        // when
        User actual = userService.findById(1);
        // then
        assertThat(actual)
                .extracting(User::getId, User::getName, User::getEmail)
                .containsExactly(1, "太郎", "xxx@example.com");
    }

    @Test
    void insert() {
        // given
        doNothing().when(userMapper).insert(any(User.class));
        // when
        userService.insert(User.builder().name("太郎").email("xxx@example.com").build());
        // then
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void update() {
        // given
        doNothing().when(userMapper).update(any(User.class));
        // when
        userService.update(User.builder().id(1).name("太郎").email("xxx@example.com").build());
        // then
        verify(userMapper).update(any(User.class));
    }

    @Test
    void deleteById() {
        // given
        doNothing().when(userMapper).deleteById(1);
        // when
        userService.deleteById(1);
        // then
        verify(userMapper).deleteById(1);
    }
}
