package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AIService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${ai.api.url:https://api.example.com/merge-photos}")
    private String apiUrl;
    
    @Value("${ai.api.key:demo-key}")
    private String apiKey;
    
    public AIService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * 发送请求到AI服务生成合照
     * 
     * @param photo1Path 照片1路径
     * @param photo2Path 照片2路径
     * @param scenePath 场景图片路径
     * @param promptText 提示文本
     * @return 返回生成的图片二进制数据
     */
    public byte[] generateMergedPhoto(String photo1Path, String photo2Path, String scenePath, String promptText) {
        logger.info("准备调用AI服务生成合照");
        logger.info("照片1路径: {}", photo1Path);
        logger.info("照片2路径: {}", photo2Path);
        logger.info("场景路径: {}", scenePath);
        logger.info("提示文本: {}", promptText);
        
        try {
            // 准备文件资源
            File photo1File = new File(photo1Path);
            File photo2File = new File(photo2Path);
            File sceneFile = new File(scenePath);
            
            // 构建多部分请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("photo1", new FileSystemResource(photo1File));
            body.add("photo2", new FileSystemResource(photo2File));
            body.add("scene", new FileSystemResource(sceneFile));
            body.add("prompt", promptText);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-API-Key", apiKey);
            
            // 创建请求实体
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            // 发送请求到AI服务
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );
            
            // 检查响应状态
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("AI服务成功生成合照");
                return response.getBody();
            } else {
                logger.error("AI服务返回错误: {}", response.getStatusCode());
                throw new RuntimeException("AI服务生成合照失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("调用AI服务失败", e);
            throw new RuntimeException("调用AI服务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 模拟AI服务生成合照（用于测试）
     * 在实际AI服务不可用的情况下使用此方法模拟生成
     */
    public byte[] mockGenerateMergedPhoto(String photo1Path, String photo2Path, String scenePath, String promptText) {
        logger.info("使用模拟方法生成合照");
        
        try {
            // 简单返回第一张照片的内容作为结果
            // 在实际应用中，这里应该调用真正的AI服务
            Path path = Paths.get(photo1Path);
            return java.nio.file.Files.readAllBytes(path);
        } catch (Exception e) {
            logger.error("模拟生成合照失败", e);
            throw new RuntimeException("模拟生成合照失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 用于前后端联通测试，返回固定的示例图片
     */
    public byte[] generateTestImage() {
        logger.info("生成测试图片用于前后端联通");
        
        try {
            // 返回示例图片路径（请确保此路径下有图片）
            Path path = Paths.get("./uploads/sample_merge_result.jpg");
            
            // 如果示例图片不存在，则尝试创建一个简单的图片
            if (!java.nio.file.Files.exists(path)) {
                logger.warn("示例图片不存在，将返回第一个找到的图片");
                
                // 尝试在uploads目录找到第一张图片
                Path uploadsDir = Paths.get("./uploads");
                if (java.nio.file.Files.exists(uploadsDir)) {
                    // 查找第一个jpg文件
                    java.util.Optional<Path> firstImage = java.nio.file.Files.walk(uploadsDir)
                            .filter(p -> p.toString().toLowerCase().endsWith(".jpg") || 
                                   p.toString().toLowerCase().endsWith(".jpeg") ||
                                   p.toString().toLowerCase().endsWith(".png"))
                            .findFirst();
                    
                    if (firstImage.isPresent()) {
                        path = firstImage.get();
                    } else {
                        throw new RuntimeException("未找到任何图片文件");
                    }
                } else {
                    throw new RuntimeException("上传目录不存在");
                }
            }
            
            return java.nio.file.Files.readAllBytes(path);
        } catch (Exception e) {
            logger.error("生成测试图片失败", e);
            throw new RuntimeException("生成测试图片失败: " + e.getMessage(), e);
        }
    }
} 