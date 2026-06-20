package com.romin.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.romin.user.entity.User;

public interface UserRepo extends JpaRepository<User, Long>{

}
