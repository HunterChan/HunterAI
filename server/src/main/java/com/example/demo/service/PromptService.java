package com.example.demo.service;

import com.example.demo.entity.Prompt;
import com.example.demo.entity.User;
import com.example.demo.repository.PromptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromptService {
    
    private static final Logger logger = LoggerFactory.getLogger(PromptService.class);
    
    private final PromptRepository promptRepository;
    
    @Autowired
    public PromptService(PromptRepository promptRepository) {
        this.promptRepository = promptRepository;
    }
    
    /**
     * 获取用户可用的所有提示词（包括用户自己的和公共的）
     */
    public List<Prompt> getAvailablePrompts(Long userId) {
        return promptRepository.findAllAvailablePrompts(userId);
    }
    
    /**
     * 获取用户的提示词
     */
    public List<Prompt> getUserPrompts(Long userId) {
        return promptRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * 获取公共提示词
     */
    public List<Prompt> getPublicPrompts() {
        return promptRepository.findByIsPublicTrueOrderByCreatedAtDesc();
    }
    
    /**
     * 搜索提示词
     */
    public List<Prompt> searchPrompts(Long userId, String keyword) {
        return promptRepository.findByContentContainingAndAvailableTo(userId, keyword);
    }
    
    /**
     * 根据ID获取提示词
     */
    public Prompt getPrompt(Long id) {
        return promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提示词不存在: " + id));
    }
    
    /**
     * 创建用户提示词
     */
    @Transactional
    public Prompt createPrompt(Prompt prompt, User user) {
        prompt.setUser(user);
        return promptRepository.save(prompt);
    }
    
    /**
     * 创建公共提示词
     */
    @Transactional
    public Prompt createPublicPrompt(String content) {
        Prompt prompt = new Prompt();
        prompt.setContent(content);
        prompt.setIsPublic(true);
        return promptRepository.save(prompt);
    }
    
    /**
     * 更新提示词
     */
    @Transactional
    public Prompt updatePrompt(Long id, String content, Long userId) {
        // 首先检查提示词是否存在且是否属于当前用户
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提示词不存在: " + id));
        
        // 检查所有权 - 公共提示词或用户自己的提示词
        if (!prompt.getIsPublic() && (prompt.getUser() == null || !prompt.getUser().getId().equals(userId))) {
            throw new RuntimeException("无权更新此提示词");
        }
        
        // 更新内容
        prompt.setContent(content);
        return promptRepository.save(prompt);
    }
    
    /**
     * 切换提示词的公共状态
     */
    @Transactional
    public Prompt togglePromptPublicStatus(Long id, Long userId) {
        // 首先检查提示词是否存在且是否属于当前用户
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提示词不存在: " + id));
        
        // 检查所有权
        if (prompt.getUser() == null || !prompt.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权切换此提示词的公共状态");
        }
        
        // 切换公共状态
        prompt.setIsPublic(!prompt.getIsPublic());
        return promptRepository.save(prompt);
    }
    
    /**
     * 删除提示词
     */
    @Transactional
    public void deletePrompt(Long id, Long userId) {
        // 首先检查提示词是否存在
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提示词不存在: " + id));
        
        // 检查所有权 - 只能删除自己的提示词
        if (prompt.getUser() == null || !prompt.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权删除此提示词");
        }
        
        promptRepository.delete(prompt);
        logger.info("提示词已删除: {}", id);
    }
    
    /**
     * 删除公共提示词（管理员操作）
     */
    @Transactional
    public void deletePublicPrompt(Long id) {
        Prompt prompt = promptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("提示词不存在: " + id));
        
        // 检查是否为公共提示词
        if (!prompt.getIsPublic()) {
            throw new RuntimeException("此提示词不是公共提示词");
        }
        
        promptRepository.delete(prompt);
        logger.info("公共提示词已删除: {}", id);
    }
} 