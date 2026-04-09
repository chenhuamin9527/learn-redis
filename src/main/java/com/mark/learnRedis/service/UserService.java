package com.mark.learnRedis.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mark.learnRedis.entity.User;
import com.mark.learnRedis.mapper.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Cacheable(cacheNames = "user", key = "#id")
    public User getByIdCache(Long id) {
        return this.getById(id);
    }

    @CachePut(cacheNames = "user", key = "#user.id")
    public User updateUser(User user) {
        this.updateById(user);
        return user;
    }

    @CacheEvict(cacheNames = "user", key = "#id")
    public void deleteUser(Long id) {
        this.removeById(id);
    }
}
