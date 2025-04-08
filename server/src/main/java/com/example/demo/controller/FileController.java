package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import com.example.demo.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件控制器
 * 处理静态文件和上传文件的访问
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {
    
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    
    private final FileStorageService fileStorageService;
    
    @Autowired
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * 通用文件访问接口
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename, HttpServletRequest request) {
        logger.info("请求访问文件: {}", filename);
        
        try {
            // 加载文件资源
            Resource resource = fileStorageService.loadFileAsResource("./uploads/" + filename);
            return prepareResourceResponse(resource, request);
        } catch (Exception e) {
            logger.error("无法加载文件: {}", filename, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 用户上传的图片访问接口
     */
    @GetMapping("/user/{userId}/{filename:.+}")
    public ResponseEntity<Resource> getUserFile(
            @PathVariable Long userId, 
            @PathVariable String filename, 
            HttpServletRequest request) {
        logger.info("请求访问用户 {} 的文件: {}", userId, filename);
        
        try {
            // 构建用户文件路径
            String filePath = "./uploads/user_" + userId + "/" + filename;
            Resource resource = fileStorageService.loadFileAsResource(filePath);
            return prepareResourceResponse(resource, request);
        } catch (Exception e) {
            logger.error("无法加载用户文件: {}/{}", userId, filename, e);
            return ResponseEntity.notFound().build();
        }
    }
    
        /**
     * 结果图片访问接口
     */
    @GetMapping("/images/results/{filename:.+}")
    public ResponseEntity<Resource> getResultImage(@PathVariable String filename, HttpServletRequest request) {
        logger.info("请求访问结果图片: {}", filename);
        
        // 获取当前用户ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = SecurityUtils.getCurrentUserId(authentication);
        logger.info("当前访问用户ID: {}", userId);
        // 测试用
        userId = 1L;
        String resultPath = "./uploads/results/user_" + userId + "/merges/" + filename;
        try {
            // 首先尝试从上传目录获取
            Resource resource = fileStorageService.loadFileAsResource(resultPath);
            return prepareResourceResponse(resource, request);
        } catch (Exception e) {
            logger.warn("上传目录中未找到结果图片: {}, 尝试从静态资源目录加载", filename);
            
            try {
                // 尝试从静态资源目录加载
                Path path = Paths.get("./src/main/resources/static/images/scenes/" + filename);
                Resource resource = new UrlResource(path.toUri());
                
                if (resource.exists()) {
                    return prepareResourceResponse(resource, request);
                } else {
                    logger.error("场景图片不存在: {}", filename);
                    return ResponseEntity.notFound().build();
                }
            } catch (MalformedURLException ex) {
                logger.error("无法加载场景图片: {}", filename, ex);
                return ResponseEntity.notFound().build();
            }
        }
    }

    /**
     * 场景图片访问接口
     */
    @GetMapping("/images/scenes/{filename:.+}")
    public ResponseEntity<Resource> getSceneImage(@PathVariable String filename, HttpServletRequest request) {
        logger.info("请求访问场景图片: {}", filename);
        
        try {
            // 首先尝试从上传目录获取
            Resource resource = fileStorageService.loadFileAsResource("./uploads/scenes/" + filename);
            return prepareResourceResponse(resource, request);
        } catch (Exception e) {
            logger.warn("上传目录中未找到场景图片: {}, 尝试从静态资源目录加载", filename);
            
            try {
                // 尝试从静态资源目录加载
                Path path = Paths.get("./src/main/resources/static/images/scenes/" + filename);
                Resource resource = new UrlResource(path.toUri());
                
                if (resource.exists()) {
                    return prepareResourceResponse(resource, request);
                } else {
                    logger.error("场景图片不存在: {}", filename);
                    return ResponseEntity.notFound().build();
                }
            } catch (MalformedURLException ex) {
                logger.error("无法加载场景图片: {}", filename, ex);
                return ResponseEntity.notFound().build();
            }
        }
    }
    
    /**
     * 准备资源响应
     */
    private ResponseEntity<Resource> prepareResourceResponse(Resource resource, HttpServletRequest request) {
        // 尝试确定文件的内容类型
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.warn("无法确定文件类型", ex);
        }
        
        // 如果无法确定类型，则使用通用二进制流类型
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
} 