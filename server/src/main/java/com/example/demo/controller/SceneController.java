package com.example.demo.controller;

import com.example.demo.entity.Scene;
import com.example.demo.service.SceneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scenes")
@CrossOrigin(origins = "*")
public class SceneController {
    
    private static final Logger logger = LoggerFactory.getLogger(SceneController.class);
    
    private final SceneService sceneService;
    
    @Autowired
    public SceneController(SceneService sceneService) {
        this.sceneService = sceneService;
    }
    
    /**
     * 获取所有可用场景
     */
    @GetMapping
    public ResponseEntity<List<Scene>> getAllScenes() {
        logger.info("获取所有场景列表");
        List<Scene> scenes = sceneService.getAllActiveScenes();
        logger.info("返回场景列表，包含 {} 个场景", scenes.size());
        return ResponseEntity.ok(scenes);
    }
    
    /**
     * 获取单个场景详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Scene> getScene(@PathVariable Long id) {
        Scene scene = sceneService.getScene(id);
        return ResponseEntity.ok(scene);
    }
    
    /**
     * 搜索场景
     */
    @GetMapping("/search")
    public ResponseEntity<List<Scene>> searchScenes(@RequestParam String keyword) {
        List<Scene> scenes = sceneService.searchScenes(keyword);
        return ResponseEntity.ok(scenes);
    }
    
    /**
     * 创建新场景（管理员操作）
     */
    @PostMapping
    public ResponseEntity<Scene> createScene(@RequestBody Scene scene) {
        Scene newScene = sceneService.createScene(scene);
        return ResponseEntity.ok(newScene);
    }
    
    /**
     * 更新场景（管理员操作）
     */
    @PutMapping("/{id}")
    public ResponseEntity<Scene> updateScene(@PathVariable Long id, @RequestBody Scene sceneDetails) {
        Scene updatedScene = sceneService.updateScene(id, sceneDetails);
        return ResponseEntity.ok(updatedScene);
    }
    
    /**
     * 切换场景的激活状态（管理员操作）
     */
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Scene> toggleSceneStatus(@PathVariable Long id) {
        Scene updatedScene = sceneService.toggleSceneStatus(id);
        return ResponseEntity.ok(updatedScene);
    }
    
    /**
     * 删除场景（管理员操作）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteScene(@PathVariable Long id) {
        sceneService.deleteScene(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "场景已删除");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取场景使用统计（管理员操作）
     */
    @GetMapping("/{id}/usage")
    public ResponseEntity<Map<String, Object>> getSceneUsage(@PathVariable Long id) {
        long usageCount = sceneService.getSceneUsageCount(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sceneId", id);
        response.put("usageCount", usageCount);
        
        return ResponseEntity.ok(response);
    }
} 