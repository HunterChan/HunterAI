package com.example.demo.service;

import com.example.demo.entity.Photo;
import com.example.demo.entity.User;
import com.example.demo.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class PhotoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PhotoService.class);
    
    private final PhotoRepository photoRepository;
    private final FileStorageService fileStorageService;
    
    @Autowired
    public PhotoService(PhotoRepository photoRepository, FileStorageService fileStorageService) {
        this.photoRepository = photoRepository;
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * 上传照片
     *
     * @param file 照片文件
     * @param user 用户
     * @return 保存的照片实体
     */
    @Transactional
    public Photo uploadPhoto(MultipartFile file, User user) {
        try {
            // 存储文件，获取文件路径
            String filePath = fileStorageService.storeFile(file, user.getId());
            
            // 创建照片实体
            Photo photo = new Photo();
            photo.setUser(user);
            photo.setFileName(file.getOriginalFilename());
            photo.setFilePath(filePath);
            photo.setFileSize((int) file.getSize());
            photo.setFileType(file.getContentType());
            
            // 保存照片信息到数据库
            Photo savedPhoto = photoRepository.save(photo);
            logger.info("照片已上传: {}", savedPhoto.getId());
            return savedPhoto;
        } catch (Exception e) {
            logger.error("照片上传失败", e);
            throw new RuntimeException("照片上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的照片列表
     *
     * @param userId 用户ID
     * @return 照片列表
     */
    public List<Photo> getUserPhotos(Long userId) {
        return photoRepository.findByUserIdOrderByUploadTimeDesc(userId);
    }
    
    /**
     * 根据ID获取照片
     *
     * @param photoId 照片ID
     * @param userId 用户ID
     * @return 照片
     */
    public Photo getPhoto(Long photoId, Long userId) {
        return photoRepository.findByIdAndUserId(photoId, userId)
                .orElseThrow(() -> new RuntimeException("照片不存在或无权访问"));
    }
    
    /**
     * 获取照片资源
     *
     * @param photoId 照片ID
     * @param userId 用户ID
     * @return 照片资源
     */
    public Resource getPhotoResource(Long photoId, Long userId) {
        Photo photo = getPhoto(photoId, userId);
        try {
            return fileStorageService.loadFileAsResource(photo.getFilePath());
        } catch (Exception e) {
            logger.error("加载照片资源失败: {}", photo.getFilePath(), e);
            throw new RuntimeException("加载照片资源失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除照片
     *
     * @param photoId 照片ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deletePhoto(Long photoId, Long userId) {
        try {
            // 获取照片信息
            Optional<Photo> photoOpt = photoRepository.findByIdAndUserId(photoId, userId);
            
            if (photoOpt.isPresent()) {
                Photo photo = photoOpt.get();
                String filePath = photo.getFilePath();
                
                // 从数据库中删除记录
                photoRepository.delete(photo);
                
                // 删除实际文件
                boolean fileDeleted = fileStorageService.deleteFile(filePath);
                if (!fileDeleted) {
                    logger.warn("物理文件删除失败: {}", filePath);
                }
                
                logger.info("照片已删除: {}", photoId);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.error("照片删除失败", e);
            throw new RuntimeException("照片删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索用户照片
     *
     * @param keyword 关键词
     * @param userId 用户ID
     * @return 照片列表
     */
    public List<Photo> searchUserPhotos(String keyword, Long userId) {
        return photoRepository.findByFileNameContainingAndUserId(keyword, userId);
    }
    
    /**
     * 更新照片描述
     *
     * @param photoId 照片ID
     * @param userId 用户ID
     * @param description 描述
     * @return 更新后的照片
     */
    @Transactional
    public Photo updatePhotoDescription(Long photoId, Long userId, String description) {
        Photo photo = getPhoto(photoId, userId);
        photo.setDescription(description);
        return photoRepository.save(photo);
    }
} 