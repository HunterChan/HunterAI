package com.example.demo.controller;

import com.example.demo.entity.Photo;
import com.example.demo.entity.PhotoMerge;
import com.example.demo.entity.Prompt;
import com.example.demo.entity.User;
import com.example.demo.service.PhotoMergeService;
import com.example.demo.service.PhotoService;
import com.example.demo.service.PromptService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/merges")
public class PhotoMergeController {
    
    private static final Logger logger = LoggerFactory.getLogger(PhotoMergeController.class);
    
    private final PhotoMergeService photoMergeService;
    private final PhotoService photoService;
    private final PromptService promptService;
    
    @Autowired
    public PhotoMergeController(PhotoMergeService photoMergeService, 
                              PhotoService photoService, 
                              PromptService promptService) {
        this.photoMergeService = photoMergeService;
        this.photoService = photoService;
        this.promptService = promptService;
    }
    
    /**
     * 获取用户的合照记录列表
     */
    @GetMapping
    public ResponseEntity<List<PhotoMerge>> getUserMerges(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<PhotoMerge> merges = photoMergeService.getUserMerges(currentUser.getId());
        return ResponseEntity.ok(merges);
    }
    
    /**
     * 分页获取用户的合照记录
     */
    @GetMapping("/paged")
    public ResponseEntity<Page<PhotoMerge>> getUserMergesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PhotoMerge> merges = photoMergeService.getUserMergesPaged(currentUser.getId(), pageable);
        
        return ResponseEntity.ok(merges);
    }
    
    /**
     * 获取合照详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhotoMerge> getMerge(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        PhotoMerge merge = photoMergeService.getMerge(id, currentUser.getId());
        return ResponseEntity.ok(merge);
    }
    
    /**
     * 创建合照
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMerge(
            @RequestParam("photo1") MultipartFile photo1,
            @RequestParam("photo2") MultipartFile photo2,
            @RequestParam("sceneId") Long sceneId,
            @RequestParam(value = "promptText", required = false) String promptText,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        
        try {
            // 步骤1: 保存用户上传的图片
            Photo savedPhoto1 = photoService.uploadPhoto(photo1, currentUser);
            Photo savedPhoto2 = photoService.uploadPhoto(photo2, currentUser);
            
            // 步骤2: 保存提示词（如果有）
            if (promptText != null && !promptText.trim().isEmpty()) {
                Prompt prompt = new Prompt();
                prompt.setUser(currentUser);
                prompt.setContent(promptText);
                prompt.setIsPublic(false);
                promptService.createPrompt(prompt, currentUser);
            }
            
            // 步骤3: 创建合照记录并开始处理
            CompletableFuture<PhotoMerge> future = photoMergeService.createMerge(
                currentUser.getId(), 
                savedPhoto1.getId(), 
                savedPhoto2.getId(), 
                sceneId, 
                promptText
            );
            
            // 获取创建的合照记录ID
            Long mergeId = future.join().getId();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "合照请求已提交，正在处理中");
            response.put("mergeId", mergeId);
            
            return ResponseEntity.accepted().body(response);
        } catch (Exception e) {
            logger.error("创建合照失败", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建合照失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取合照结果图片
     */
    @GetMapping("/{id}/result")
    public ResponseEntity<Resource> getMergeResult(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        
        User currentUser = (User) authentication.getPrincipal();
        
        // 获取合照资源
        Resource resource = photoMergeService.getMergeResource(id, currentUser.getId());
        
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
     * 下载合照结果
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadMergeResult(
            @PathVariable Long id,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        
        // 获取合照资源
        Resource resource = photoMergeService.getMergeResource(id, currentUser.getId());
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"merge_" + id + ".jpg\"")
                .body(resource);
    }
    
    /**
     * 删除合照记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMerge(
            @PathVariable Long id,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        
        photoMergeService.deleteMerge(id, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "合照记录已删除");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 重新生成合照
     */
    @PostMapping("/{id}/regenerate")
    public ResponseEntity<Map<String, Object>> regenerateMerge(
            @PathVariable Long id,
            Authentication authentication) {
        
        User currentUser = (User) authentication.getPrincipal();
        
        // 异步处理合照重新生成
        photoMergeService.regenerateMerge(id, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "合照重新生成请求已提交，正在处理中");
        
        return ResponseEntity.accepted().body(response);
    }
} 