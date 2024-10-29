package com.javaweb.services;

import com.javaweb.entity.RedisToken;

public interface RedisTokenService {
    String save(RedisToken token);
    void delete(String id);
    RedisToken getById(String id);
}
