package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
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
     * 获取上传目录中的文件
     */
    @GetMapping("/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        String path = request.getRequestURI().substring("/api/files".length());
        logger.info("请求访问文件: {}", path);
        
        try {
            // 尝试从上传目录加载
            Resource resource = fileStorageService.loadFileAsResource("./uploads" + path);
            return prepareResourceResponse(resource, request);
        } catch (Exception e) {
            logger.warn("上传目录中未找到文件: {}, 尝试从静态资源目录加载", path);
            
            try {
                // 尝试从静态资源目录加载
                Path filePath = Paths.get("./src/main/resources/static" + path);
                Resource resource = new UrlResource(filePath.toUri());
                
                if (resource.exists()) {
                    return prepareResourceResponse(resource, request);
                } else {
                    logger.error("文件不存在: {}", path);
                    return ResponseEntity.notFound().build();
                }
            } catch (MalformedURLException ex) {
                logger.error("无法加载文件: {}", path, ex);
                return ResponseEntity.notFound().build();
            }
        }
    }
    
    /**
     * 获取场景图片
     * 专门为场景图片提供的访问接口
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
} 