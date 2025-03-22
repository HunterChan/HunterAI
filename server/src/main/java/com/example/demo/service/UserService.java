package com.example.demo.service;

import com.example.demo.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    
    List<User> findAllUsers();
    
    Optional<User> findUserById(Long id);
    
    User saveUser(User user);
    
    void deleteUserById(Long id);
} 