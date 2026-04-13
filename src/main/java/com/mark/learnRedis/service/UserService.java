package com.mark.learnRedis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mark.learnRedis.config.BloomFilter;
import com.mark.learnRedis.entity.User;
import com.mark.learnRedis.mapper.UserMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    private final BloomFilter bloomFilter;

    public UserService(BloomFilter bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    @PostConstruct
    public void init() {
        bloomFilter.init("user", 10000, 0.01);
    }

    @Cacheable(cacheNames = "user", key = "#id")
    public User getByIdCache(Long id) {
        // 布隆过滤器返回 false，说明确定不存在，直接返回 null
        if (!bloomFilter.contains("user", String.valueOf(id))) {
            return null;
        }
        // 布隆过滤器返回 true，可能存在，查询数据库
        User user = this.getById(id);
        if (user != null) {
            // 存在则添加到布隆过滤器
            bloomFilter.add("user", String.valueOf(id));
        }
        return user;
    }

    @CachePut(cacheNames = "user", key = "#user.id")
    public User updateUser(User user) {
        this.updateById(user);
        return user;
    }

    public boolean saveUser(User user) {
        boolean saved = this.save(user);
        if (saved) {
            bloomFilter.add("user", String.valueOf(user.getId()));
        }
        return saved;
    }

    @CacheEvict(cacheNames = "user", key = "#id")
    public void deleteUser(Long id) {
        this.removeById(id);
    }
}
