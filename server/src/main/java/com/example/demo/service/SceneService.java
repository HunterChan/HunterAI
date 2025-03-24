package com.example.demo.service;

import com.example.demo.entity.Scene;
import com.example.demo.repository.PhotoMergeRepository;
import com.example.demo.repository.SceneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SceneService {
    
    private static final Logger logger = LoggerFactory.getLogger(SceneService.class);
    
    private final SceneRepository sceneRepository;
    private final PhotoMergeRepository photoMergeRepository;
    private final FileStorageService fileStorageService;
    
    @Autowired
    public SceneService(SceneRepository sceneRepository, 
                       PhotoMergeRepository photoMergeRepository,
                       FileStorageService fileStorageService) {
        this.sceneRepository = sceneRepository;
        this.photoMergeRepository = photoMergeRepository;
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * 获取所有可用场景
     */
    public List<Scene> getAllActiveScenes() {
        return sceneRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
    
    /**
     * 根据ID获取场景
     */
    public Scene getScene(Long id) {
        return sceneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场景不存在: " + id));
    }
    
    /**
     * 搜索场景
     */
    public List<Scene> searchScenes(String keyword) {
        return sceneRepository.findByNameContainingAndIsActiveTrue(keyword);
    }
    
    /**
     * 创建新场景
     */
    @Transactional
    public Scene createScene(Scene scene) {
        // 检查名称是否已存在
        if (sceneRepository.existsByName(scene.getName())) {
            throw new RuntimeException("场景名称已存在: " + scene.getName());
        }
        
        return sceneRepository.save(scene);
    }
    
    /**
     * 更新场景
     */
    @Transactional
    public Scene updateScene(Long id, Scene sceneDetails) {
        Scene scene = getScene(id);
        
        // 检查名称是否已存在（如果名称有变化）
        if (!scene.getName().equals(sceneDetails.getName()) 
                && sceneRepository.existsByName(sceneDetails.getName())) {
            throw new RuntimeException("场景名称已存在: " + sceneDetails.getName());
        }
        
        // 更新字段
        scene.setName(sceneDetails.getName());
        scene.setDescription(sceneDetails.getDescription());
        
        // 如果有新的预览图片，更新预览图
        if (sceneDetails.getPreviewImage() != null && !sceneDetails.getPreviewImage().isEmpty()) {
            scene.setPreviewImage(sceneDetails.getPreviewImage());
        }
        
        return sceneRepository.save(scene);
    }
    
    /**
     * 切换场景的激活状态
     */
    @Transactional
    public Scene toggleSceneStatus(Long id) {
        Scene scene = getScene(id);
        scene.setIsActive(!scene.getIsActive());
        return sceneRepository.save(scene);
    }
    
    /**
     * 删除场景
     */
    @Transactional
    public void deleteScene(Long id) {
        Scene scene = getScene(id);
        
        // 检查是否有合照记录使用此场景
        long mergeCount = photoMergeRepository.countBySceneId(id);
        if (mergeCount > 0) {
            // 如果有合照记录使用此场景，则只禁用它而不是删除
            scene.setIsActive(false);
            sceneRepository.save(scene);
            logger.info("场景已被使用，已将其禁用而非删除: {}", id);
        } else {
            // 删除场景的预览图片
            if (scene.getPreviewImage() != null && !scene.getPreviewImage().isEmpty()) {
                fileStorageService.deleteFile(scene.getPreviewImage());
            }
            
            // 删除场景记录
            sceneRepository.delete(scene);
            logger.info("场景已删除: {}", id);
        }
    }
    
    /**
     * 获取场景使用统计
     */
    public long getSceneUsageCount(Long id) {
        return photoMergeRepository.countBySceneId(id);
    }
} 