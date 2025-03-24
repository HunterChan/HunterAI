package com.example.demo.controller;

import com.example.demo.entity.Photo;
import com.example.demo.entity.User;
import com.example.demo.service.PhotoService;
import com.example.demo.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {
    
    private static final Logger logger = LoggerFactory.getLogger(PhotoController.class);
    
    private final PhotoService photoService;
    
    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }
    
    /**
     * 上传照片
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        logger.info("接收到照片上传请求");
        
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUserOrTestUser(authentication);
        
        // 上传照片
        Photo photo = photoService.uploadPhoto(file, currentUser);
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("id", photo.getId());
        response.put("fileName", photo.getFileName());
        response.put("fileSize", photo.getFileSize());
        response.put("fileType", photo.getFileType());
        response.put("uploadTime", photo.getUploadTime());
        
        // 构建照片访问URL
        String photoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/photos/")
                .path(photo.getId().toString())
                .toUriString();
        response.put("url", photoUrl);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户的照片列表
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserPhotos(Authentication authentication) {
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUserOrTestUser(authentication);
        
        // 获取用户照片列表
        List<Photo> photos = photoService.getUserPhotos(currentUser.getId());
        
        // 转换为前端需要的格式
        List<Map<String, Object>> response = photos.stream().map(photo -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", photo.getId());
            map.put("fileName", photo.getFileName());
            map.put("fileSize", photo.getFileSize());
            map.put("fileType", photo.getFileType());
            map.put("uploadTime", photo.getUploadTime());
            map.put("description", photo.getDescription());
            
            // 构建照片访问URL
            String photoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/photos/")
                    .path(photo.getId().toString())
                    .toUriString();
            map.put("url", photoUrl);
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取照片
     */
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getPhoto(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUserOrTestUser(authentication);
        
        // 获取照片资源
        Resource resource = photoService.getPhotoResource(id, currentUser.getId());
        
        // 确定文件的内容类型
        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.warn("无法确定文件类型", ex);
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    /**
     * 删除照片
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePhoto(
            @PathVariable Long id,
            Authentication authentication) {
        
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUserOrTestUser(authentication);
        
        // 删除照片
        boolean deleted = photoService.deletePhoto(id, currentUser.getId());
        
        // 构建响应
        Map<String, Object> response = new HashMap<>();
        if (deleted) {
            response.put("success", true);
            response.put("message", "照片删除成功");
        } else {
            response.put("success", false);
            response.put("message", "照片不存在或无权删除");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新照片描述
     */
    @PutMapping("/{id}")
    public ResponseEntity<Photo> updatePhoto(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUserOrTestUser(authentication);
        
        // 更新照片描述
        String description = payload.get("description");
        Photo updatedPhoto = photoService.updatePhotoDescription(id, currentUser.getId(), description);
        
        return ResponseEntity.ok(updatedPhoto);
    }
    
    /**
     * 搜索照片
     */
    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchPhotos(
            @RequestParam String keyword,
            Authentication authentication) {
        
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUserOrTestUser(authentication);
        
        // 搜索照片 - 使用已有的searchUserPhotos方法
        List<Photo> photos = photoService.searchUserPhotos(keyword, currentUser.getId());
        
        // 转换为前端需要的格式
        List<Map<String, Object>> response = photos.stream().map(photo -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", photo.getId());
            map.put("fileName", photo.getFileName());
            map.put("fileSize", photo.getFileSize());
            map.put("fileType", photo.getFileType());
            map.put("uploadTime", photo.getUploadTime());
            map.put("description", photo.getDescription());
            
            // 构建照片访问URL
            String photoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/photos/")
                    .path(photo.getId().toString())
                    .toUriString();
            map.put("url", photoUrl);
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
} 