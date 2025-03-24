package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    private final Path fileStorageLocation;
    private final Path staticResourceLocation;
    
    public FileStorageService() {
        this.fileStorageLocation = Paths.get("./uploads").toAbsolutePath().normalize();
        this.staticResourceLocation = Paths.get("./src/main/resources/static").toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("文件存储目录已初始化: {}", this.fileStorageLocation);
            
            // 不需要创建静态资源目录，应该已经存在
            logger.info("静态资源目录: {}", this.staticResourceLocation);
        } catch (Exception ex) {
            logger.error("无法创建文件存储目录", ex);
            throw new RuntimeException("无法创建文件存储目录", ex);
        }
    }
    
    public Resource loadFileAsResource(String filePath) throws Exception {
        try {
            Path resolvedPath = Paths.get(filePath).normalize();
            logger.info("尝试加载文件: {}", resolvedPath);
            
            Resource resource = new UrlResource(resolvedPath.toUri());
            if (resource.exists()) {
                logger.info("文件加载成功: {}", resolvedPath);
                return resource;
            } else {
                // 如果文件不存在，尝试在静态资源目录查找
                String staticPath = filePath;
                if (filePath.startsWith("./uploads/")) {
                    // 将 ./uploads/xxx 转换为 ./src/main/resources/static/xxx
                    staticPath = filePath.replace("./uploads/", "./src/main/resources/static/");
                }
                
                Path staticFilePath = Paths.get(staticPath).normalize();
                logger.info("在静态资源目录中查找文件: {}", staticFilePath);
                
                Resource staticResource = new UrlResource(staticFilePath.toUri());
                if (staticResource.exists()) {
                    logger.info("在静态资源目录中找到文件: {}", staticFilePath);
                    return staticResource;
                }
                
                logger.error("文件不存在: {}", filePath);
                throw new Exception("文件不存在 " + filePath);
            }
        } catch (MalformedURLException ex) {
            logger.error("文件URL格式错误: {}", filePath, ex);
            throw new Exception("文件URL格式错误 " + filePath, ex);
        }
    }
    
    public String storeFile(byte[] fileContent, String targetPath) throws IOException {
        try {
            // 确保目标目录存在
            Path targetFilePath = Paths.get(targetPath).normalize();
            Path targetDir = targetFilePath.getParent();
            if (targetDir != null) {
                Files.createDirectories(targetDir);
            }
            
            // 写入文件
            Files.write(targetFilePath, fileContent);
            logger.info("文件已保存: {}", targetFilePath);
            
            return targetFilePath.toString();
        } catch (IOException ex) {
            logger.error("无法存储文件到 {}", targetPath, ex);
            throw new IOException("无法存储文件到 " + targetPath, ex);
        }
    }
    
    /**
     * 存储上传的照片文件
     *
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 存储的文件路径
     */
    public String storeFile(MultipartFile file, Long userId) {
        if (file == null) {
            throw new RuntimeException("文件不能为空");
        }
        
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                throw new RuntimeException("无法存储空文件");
            }
            
            // 清理文件名
            String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
            
            // 检查文件名中是否包含无效字符
            if (originalFilename.contains("..")) {
                throw new RuntimeException("文件名包含无效路径序列: " + originalFilename);
            }
            
            // 为用户创建单独的目录
            Path userDir = this.fileStorageLocation.resolve("user_" + userId);
            Files.createDirectories(userDir);
            
            // 生成唯一文件名，避免覆盖
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + "." + fileExtension;
            
            // 存储文件
            Path targetLocation = userDir.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("文件已存储: {}", targetLocation);
            return targetLocation.toString();
        } catch (IOException ex) {
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            logger.error("无法存储文件 " + filename, ex);
            throw new RuntimeException("无法存储文件 " + filename, ex);
        }
    }
    
    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }
    
    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否成功删除
     */
    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                logger.warn("尝试删除空路径");
                return false;
            }
            
            Path path = Paths.get(filePath);
            logger.info("尝试删除文件: {}", path);
            
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                logger.info("文件已成功删除: {}", path);
            } else {
                logger.warn("文件不存在，无法删除: {}", path);
            }
            
            return deleted;
        } catch (IOException ex) {
            logger.error("删除文件失败: {}", filePath, ex);
            return false;
        }
    }
} 