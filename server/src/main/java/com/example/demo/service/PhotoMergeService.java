package com.example.demo.service;

import com.example.demo.entity.Photo;
import com.example.demo.entity.PhotoMerge;
import com.example.demo.entity.Scene;
import com.example.demo.entity.User;
import com.example.demo.repository.PhotoMergeRepository;
import com.example.demo.repository.PhotoRepository;
import com.example.demo.repository.SceneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PhotoMergeService {
    
    private static final Logger logger = LoggerFactory.getLogger(PhotoMergeService.class);
    
    private final PhotoMergeRepository photoMergeRepository;
    private final PhotoRepository photoRepository;
    private final SceneRepository sceneRepository;
    private final FileStorageService fileStorageService;
    private final AIService aiService;
    
    @Autowired
    public PhotoMergeService(PhotoMergeRepository photoMergeRepository,
                          PhotoRepository photoRepository,
                          SceneRepository sceneRepository,
                          FileStorageService fileStorageService,
                          AIService aiService) {
        this.photoMergeRepository = photoMergeRepository;
        this.photoRepository = photoRepository;
        this.sceneRepository = sceneRepository;
        this.fileStorageService = fileStorageService;
        this.aiService = aiService;
    }
    
    /**
     * 获取用户的合照记录列表
     */
    public List<PhotoMerge> getUserMerges(Long userId) {
        return photoMergeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 分页获取用户的合照记录
     */
    public Page<PhotoMerge> getUserMergesPaged(Long userId, Pageable pageable) {
        return photoMergeRepository.findByUserId(userId, pageable);
    }
    
    /**
     * 获取合照详情
     */
    public PhotoMerge getMerge(Long id, Long userId) {
        return photoMergeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("合照记录不存在或无权访问"));
    }
    
    /**
     * 创建合照请求并异步处理
     */
    @Async
    @Transactional
    public CompletableFuture<PhotoMerge> createMerge(Long userId, Long photo1Id, Long photo2Id, 
                                               Long sceneId, String promptText) {
        // 验证照片和场景
        User user = new User();
        user.setId(userId);
        
        Photo photo1 = photoRepository.findById(photo1Id)
                .orElseThrow(() -> new RuntimeException("照片1不存在"));
        
        Photo photo2 = photoRepository.findById(photo2Id)
                .orElseThrow(() -> new RuntimeException("照片2不存在"));
        
        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new RuntimeException("场景不存在"));
        
        // 创建合照记录
        PhotoMerge photoMerge = new PhotoMerge();
        photoMerge.setUser(user);
        photoMerge.setPhoto1(photo1);
        photoMerge.setPhoto2(photo2);
        photoMerge.setScene(scene);
        photoMerge.setPromptText(promptText);
        photoMerge.setStatus("PROCESSING");
        
        // 保存记录
        PhotoMerge savedMerge = photoMergeRepository.save(photoMerge);
        Long mergeId = savedMerge.getId();
        
        // 异步处理合照生成
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 使用测试方法返回固定图片（临时方案，前后端联通测试用）
                byte[] resultImageData = aiService.generateTestImage();
                
                /* 
                // 实际项目中应该调用AI服务生成合照
                byte[] resultImageData = aiService.generateMergedPhoto(
                        photo1.getFilePath(), 
                        photo2.getFilePath(), 
                        scene.getPreviewImage(), 
                        promptText
                );
                */
                
                // 保存生成的合照文件
                String resultPath = saveResultImage(resultImageData, userId, mergeId);
                
                // 更新合照记录
                return photoMergeRepository.findById(mergeId).map(merge -> {
                    merge.setResultPath(resultPath);
                    merge.setStatus("COMPLETED");
                    merge.setCompletedAt(LocalDateTime.now());
                    return photoMergeRepository.save(merge);
                }).orElseThrow(() -> new RuntimeException("合照记录不存在"));
                
            } catch (Exception e) {
                // 处理失败，更新状态
                logger.error("合照生成失败", e);
                photoMergeRepository.findById(mergeId).ifPresent(merge -> {
                    merge.setStatus("FAILED");
                    photoMergeRepository.save(merge);
                });
                throw new RuntimeException("合照生成失败: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 保存结果图片
     */
    private String saveResultImage(byte[] imageData, Long userId, Long mergeId) {
        try {
            // 构建存储路径
            Path userResultDir = Paths.get("./uploads/results/user_" + userId + "/merges");
            logger.info("ResultImage Path: {}", userResultDir.toAbsolutePath());
            java.nio.file.Files.createDirectories(userResultDir);
            
            // 生成唯一文件名
            String fileName = mergeId + "_" + UUID.randomUUID().toString() + ".jpg";
            Path targetPath = userResultDir.resolve(fileName);
            
            // 保存文件
            java.nio.file.Files.write(targetPath, imageData);
            
            return targetPath.toString();
        } catch (Exception e) {
            logger.error("保存合照结果失败", e);
            throw new RuntimeException("保存合照结果失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取合照结果资源
     */
    public Resource getMergeResource(Long id, Long userId) {
        PhotoMerge merge = getMerge(id, userId);
        
        if (!"COMPLETED".equals(merge.getStatus()) || merge.getResultPath() == null) {
            throw new RuntimeException("合照结果尚未生成或生成失败");
        }
        
        try {
            return fileStorageService.loadFileAsResource(merge.getResultPath());
        } catch (Exception e) {
            logger.error("加载合照资源失败: {}", merge.getResultPath(), e);
            throw new RuntimeException("加载合照资源失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除合照记录
     */
    @Transactional
    public void deleteMerge(Long id, Long userId) {
        PhotoMerge merge = getMerge(id, userId);
        
        // 删除物理文件
        if (merge.getResultPath() != null && !merge.getResultPath().isEmpty()) {
            fileStorageService.deleteFile(merge.getResultPath());
        }
        
        // 删除数据库记录
        photoMergeRepository.delete(merge);
        logger.info("合照记录已删除: {}", id);
    }
    
    /**
     * 重新生成合照
     */
    @Async
    @Transactional
    public CompletableFuture<PhotoMerge> regenerateMerge(Long id, Long userId) {
        final PhotoMerge merge = getMerge(id, userId);
        
        // 更新状态为处理中
        merge.setStatus("PROCESSING");
        final PhotoMerge savedMerge = photoMergeRepository.save(merge);
        
        final Long mergeId = savedMerge.getId();
        
        // 异步处理合照生成
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 删除旧的结果文件（如果存在）
                if (savedMerge.getResultPath() != null && !savedMerge.getResultPath().isEmpty()) {
                    fileStorageService.deleteFile(savedMerge.getResultPath());
                }
                
                // 调用AI服务生成合照
                final byte[] resultImageData = aiService.generateMergedPhoto(
                        savedMerge.getPhoto1().getFilePath(), 
                        savedMerge.getPhoto2().getFilePath(), 
                        savedMerge.getScene().getPreviewImage(), 
                        savedMerge.getPromptText()
                );
                
                // 保存生成的合照文件
                final String resultPath = saveResultImage(resultImageData, userId, mergeId);
                
                // 更新合照记录
                return photoMergeRepository.findById(mergeId).map(m -> {
                    m.setResultPath(resultPath);
                    m.setStatus("COMPLETED");
                    m.setCompletedAt(LocalDateTime.now());
                    return photoMergeRepository.save(m);
                }).orElseThrow(() -> new RuntimeException("合照记录不存在"));
                
            } catch (Exception e) {
                // 处理失败，更新状态
                logger.error("合照重新生成失败", e);
                photoMergeRepository.findById(mergeId).ifPresent(m -> {
                    m.setStatus("FAILED");
                    photoMergeRepository.save(m);
                });
                throw new RuntimeException("合照重新生成失败: " + e.getMessage(), e);
            }
        });
    }
} 