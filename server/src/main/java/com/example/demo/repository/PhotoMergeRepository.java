package com.example.demo.repository;

import com.example.demo.entity.PhotoMerge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoMergeRepository extends JpaRepository<PhotoMerge, Long> {
    
    // 根据用户ID查询合照记录，按创建时间降序排序
    List<PhotoMerge> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 分页查询用户的合照记录
    Page<PhotoMerge> findByUserId(Long userId, Pageable pageable);
    
    // 根据ID和用户ID查询合照记录，确保当前用户只能访问自己的合照
    Optional<PhotoMerge> findByIdAndUserId(Long id, Long userId);
    
    // 查询用户的处理中的合照数量
    long countByUserIdAndStatus(Long userId, String status);
    
    // 查询所有处理中的合照
    List<PhotoMerge> findByStatus(String status);
    
    // 查询特定场景的合照数量
    long countBySceneId(Long sceneId);
    
    // 统计用户的合照总数
    long countByUserId(Long userId);
    
    // 查询使用特定照片的合照记录
    @Query("SELECT pm FROM PhotoMerge pm WHERE pm.photo1.id = ?1 OR pm.photo2.id = ?1")
    List<PhotoMerge> findByPhotoId(Long photoId);
} 