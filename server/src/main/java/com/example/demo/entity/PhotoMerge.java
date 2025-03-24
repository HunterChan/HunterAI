package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "photo_merges")
public class PhotoMerge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo1_id", nullable = false)
    private Photo photo1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo2_id", nullable = false)
    private Photo photo2;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;
    
    @Column(name = "prompt_text", columnDefinition = "TEXT")
    private String promptText;
    
    @Column(name = "result_path")
    private String resultPath;
    
    @Column(nullable = false)
    private String status = "PROCESSING";
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    // 默认构造函数
    public PhotoMerge() {
    }
    
    // 带基本信息的构造函数
    public PhotoMerge(User user, Photo photo1, Photo photo2, Scene scene, String promptText) {
        this.user = user;
        this.photo1 = photo1;
        this.photo2 = photo2;
        this.scene = scene;
        this.promptText = promptText;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Photo getPhoto1() {
        return photo1;
    }

    public void setPhoto1(Photo photo1) {
        this.photo1 = photo1;
    }

    public Photo getPhoto2() {
        return photo2;
    }

    public void setPhoto2(Photo photo2) {
        this.photo2 = photo2;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    // 辅助方法：完成合照处理
    public void complete(String resultPath) {
        this.resultPath = resultPath;
        this.status = "COMPLETED";
        this.completedAt = LocalDateTime.now();
    }
    
    // 辅助方法：标记为失败
    public void markAsFailed() {
        this.status = "FAILED";
    }
    
    // 辅助方法：检查是否处理中
    public boolean isProcessing() {
        return "PROCESSING".equals(this.status);
    }
    
    // 辅助方法：检查是否已完成
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }
    
    // 辅助方法：检查是否失败
    public boolean isFailed() {
        return "FAILED".equals(this.status);
    }
} 