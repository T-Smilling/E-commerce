package com.javaweb.repository;

import com.javaweb.entity.RoleEntity;
import com.javaweb.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.addresses a WHERE a.id = ?1")
    List<UserEntity> findByAddress(Long addressId);

    Optional<UserEntity> findByEmail(String username);

    @Query("SELECT u FROM UserEntity u WHERE u.name =:name")
    Optional<UserEntity> findByName(String name);
}
