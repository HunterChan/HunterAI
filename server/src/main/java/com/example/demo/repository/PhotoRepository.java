package com.example.demo.repository;

import com.example.demo.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    // 根据用户ID查询照片列表，按上传时间降序排序
    List<Photo> findByUserIdOrderByUploadTimeDesc(Long userId);
    
    // 根据ID和用户ID查询照片，确保当前用户只能访问自己的照片
    Optional<Photo> findByIdAndUserId(Long id, Long userId);
    
    // 根据文件名模糊查询照片
    List<Photo> findByFileNameContainingAndUserId(String fileName, Long userId);
    
    // 根据文件类型查询照片
    List<Photo> findByFileTypeAndUserId(String fileType, Long userId);
    
    // 统计用户上传的照片数量
    Long countByUserId(Long userId);
} 