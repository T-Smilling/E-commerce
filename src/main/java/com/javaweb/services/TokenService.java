package com.javaweb.services;

import com.javaweb.entity.TokenEntity;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public interface TokenService {
    Long saveToken(TokenEntity tokenEntity);

    String deleteToken(Long tokenId);

    TokenEntity findByUserName(String name);

    TokenEntity getByName(String name);
}
