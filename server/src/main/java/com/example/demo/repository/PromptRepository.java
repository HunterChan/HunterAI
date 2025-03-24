package com.example.demo.repository;

import com.example.demo.entity.Prompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptRepository extends JpaRepository<Prompt, Long> {
    
    // 获取用户的所有提示词
    List<Prompt> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 获取所有公共提示词
    List<Prompt> findByIsPublicTrueOrderByCreatedAtDesc();
    
    // 获取用户的所有提示词和所有公共提示词
    @Query("SELECT p FROM Prompt p WHERE p.isPublic = true OR p.user.id = ?1 ORDER BY p.createdAt DESC")
    List<Prompt> findAllAvailablePrompts(Long userId);
    
    // 根据内容模糊查询用户可用的提示词
    @Query("SELECT p FROM Prompt p WHERE (p.isPublic = true OR p.user.id = ?1) AND p.content LIKE %?2% ORDER BY p.createdAt DESC")
    List<Prompt> findByContentContainingAndAvailableTo(Long userId, String content);
    
    // 检查用户是否拥有特定提示词
    boolean existsByIdAndUserId(Long id, Long userId);
} 