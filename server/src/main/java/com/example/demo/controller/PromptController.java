package com.example.demo.controller;

import com.example.demo.entity.Prompt;
import com.example.demo.entity.User;
import com.example.demo.service.PromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prompts")
public class PromptController {
    
    private static final Logger logger = LoggerFactory.getLogger(PromptController.class);
    
    private final PromptService promptService;
    
    @Autowired
    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }
    
    /**
     * 获取用户可用的所有提示词（包括用户自己的和公共的）
     */
    @GetMapping
    public ResponseEntity<List<Prompt>> getAvailablePrompts(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Prompt> prompts = promptService.getAvailablePrompts(currentUser.getId());
        return ResponseEntity.ok(prompts);
    }
    
    /**
     * 获取用户的提示词
     */
    @GetMapping("/user")
    public ResponseEntity<List<Prompt>> getUserPrompts(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Prompt> prompts = promptService.getUserPrompts(currentUser.getId());
        return ResponseEntity.ok(prompts);
    }
    
    /**
     * 获取公共提示词
     */
    @GetMapping("/public")
    public ResponseEntity<List<Prompt>> getPublicPrompts() {
        List<Prompt> prompts = promptService.getPublicPrompts();
        return ResponseEntity.ok(prompts);
    }
    
    /**
     * 搜索提示词
     */
    @GetMapping("/search")
    public ResponseEntity<List<Prompt>> searchPrompts(
            @RequestParam String keyword,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<Prompt> prompts = promptService.searchPrompts(currentUser.getId(), keyword);
        return ResponseEntity.ok(prompts);
    }
    
    /**
     * 获取单个提示词详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Prompt> getPrompt(@PathVariable Long id) {
        Prompt prompt = promptService.getPrompt(id);
        return ResponseEntity.ok(prompt);
    }
    
    /**
     * 创建用户提示词
     */
    @PostMapping
    public ResponseEntity<Prompt> createPrompt(
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        
        String content = payload.get("content");
        Boolean isPublic = Boolean.valueOf(payload.getOrDefault("isPublic", "false"));
        
        Prompt prompt = new Prompt();
        prompt.setContent(content);
        prompt.setIsPublic(isPublic);
        
        Prompt newPrompt = promptService.createPrompt(prompt, currentUser);
        return ResponseEntity.ok(newPrompt);
    }
    
    /**
     * 创建公共提示词（管理员操作）
     */
    @PostMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Prompt> createPublicPrompt(@RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        Prompt newPrompt = promptService.createPublicPrompt(content);
        return ResponseEntity.ok(newPrompt);
    }
    
    /**
     * 更新提示词
     */
    @PutMapping("/{id}")
    public ResponseEntity<Prompt> updatePrompt(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        String content = payload.get("content");
        
        Prompt updatedPrompt = promptService.updatePrompt(id, content, currentUser.getId());
        return ResponseEntity.ok(updatedPrompt);
    }
    
    /**
     * 切换提示词的公共状态
     */
    @PutMapping("/{id}/toggle-public")
    public ResponseEntity<Prompt> togglePromptPublicStatus(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        Prompt updatedPrompt = promptService.togglePromptPublicStatus(id, currentUser.getId());
        return ResponseEntity.ok(updatedPrompt);
    }
    
    /**
     * 删除提示词
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePrompt(
            @PathVariable Long id,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        promptService.deletePrompt(id, currentUser.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "提示词已删除");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除公共提示词（管理员操作）
     */
    @DeleteMapping("/public/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deletePublicPrompt(@PathVariable Long id) {
        promptService.deletePublicPrompt(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "公共提示词已删除");
        
        return ResponseEntity.ok(response);
    }
} 