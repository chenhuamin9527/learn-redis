package com.mark.learnRedis.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mark.learnRedis.entity.User;
import com.mark.learnRedis.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getByIdCache(id);
    }

    @PostMapping("/user")
    public User updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @PostMapping("/user/{id}")
    public boolean deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return true;
    }

}
