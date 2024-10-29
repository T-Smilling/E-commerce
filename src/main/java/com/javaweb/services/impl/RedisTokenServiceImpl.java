package com.javaweb.services.impl;

import com.javaweb.entity.RedisToken;
import com.javaweb.exception.APIException;
import com.javaweb.repository.RedisTokenRepository;
import com.javaweb.services.RedisTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisTokenServiceImpl implements RedisTokenService {
    @Autowired
    private RedisTokenRepository redisTokenRepository;

    @Override
    public String save(RedisToken token) {
        RedisToken redisToken = redisTokenRepository.save(token);
        return redisToken.getId();
    }

    @Override
    public void delete(String id) {
        redisTokenRepository.deleteById(id);
    }

    @Override
    public RedisToken getById(String id) {
        return redisTokenRepository.findById(id).orElseThrow(()->new APIException("Token not found"));
    }
}
