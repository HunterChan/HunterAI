package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "file_name", nullable = false)
    @NotBlank(message = "文件名不能为空")
    private String fileName;
    
    @Column(name = "file_path", nullable = false)
    @NotBlank(message = "文件路径不能为空")
    private String filePath;
    
    @Column(name = "file_size", nullable = false)
    @NotNull(message = "文件大小不能为空")
    private Integer fileSize;
    
    @Column(name = "file_type", nullable = false)
    @NotBlank(message = "文件类型不能为空")
    private String fileType;
    
    @Column(name = "upload_time", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime uploadTime;
    
    @Column(name = "description")
    private String description;
    
    // 默认构造函数
    public Photo() {
    }
    
    // 带基本信息的构造函数
    public Photo(User user, String fileName, String filePath, Integer fileSize, String fileType) {
        this.user = user;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileType = fileType;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
} 