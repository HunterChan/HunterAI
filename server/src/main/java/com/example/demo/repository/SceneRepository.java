package com.example.demo.repository;

import com.example.demo.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SceneRepository extends JpaRepository<Scene, Long> {
    
    // 获取所有激活状态的场景
    List<Scene> findByIsActiveTrueOrderByCreatedAtDesc();
    
    // 根据名称模糊查询
    List<Scene> findByNameContainingAndIsActiveTrue(String name);
    
    // 根据名称精确查询
    Scene findByName(String name);
    
    // 检查名称是否已存在
    boolean existsByName(String name);
} 