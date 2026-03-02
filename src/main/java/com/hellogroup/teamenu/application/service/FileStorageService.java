package com.hellogroup.teamenu.application.service;

import com.hellogroup.teamenu.common.constant.ResponseCode;
import com.hellogroup.teamenu.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储服务
 * 
 * @author HelloGroup
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${file.storage.local-path}")
    private String localPath;

    @Value("${file.storage.url-prefix}")
    private String urlPrefix;
    
    /**
     * 初始化：确保根目录存在
     */
    @javax.annotation.PostConstruct
    public void init() {
        log.info("初始化文件存储服务, localPath={}, urlPrefix={}", localPath, urlPrefix);
        File rootDir = new File(localPath);
        if (!rootDir.exists()) {
            boolean created = rootDir.mkdirs();
            log.info("创建根目录: {}, 结果={}", localPath, created);
            if (!created && !rootDir.exists()) {
                log.error("无法创建文件存储根目录: {}", localPath);
            }
        } else {
            log.info("文件存储根目录已存在: {}", localPath);
        }
    }

    /**
     * 保存Base64编码的图片列表
     * 
     * @param base64Images Base64编码的图片数据列表
     * @return 保存后的图片URL列表
     */
    public List<String> saveBase64Images(List<String> base64Images) {
        if (base64Images == null || base64Images.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> imagePaths = new ArrayList<>();
        
        for (String base64Image : base64Images) {
            try {
                String imagePath = saveBase64Image(base64Image);
                imagePaths.add(imagePath);
            } catch (Exception e) {
                log.error("保存Base64图片失败", e);
                throw new BusinessException(ResponseCode.BUSINESS_ERROR, "保存图片失败: " + e.getMessage());
            }
        }
        
        return imagePaths;
    }

    /**
     * 保存单个Base64编码的图片
     * 
     * @param base64Image Base64编码的图片数据
     * @return 保存后的图片URL
     */
    private String saveBase64Image(String base64Image) throws IOException {
        try {
            log.info("开始保存Base64图片, 数据长度={}", base64Image != null ? base64Image.length() : 0);
            
            // 移除可能存在的 data:image/jpeg;base64, 前缀
            String cleanedBase64 = base64Image;
            if (base64Image.startsWith("data:")) {
                int commaIndex = base64Image.indexOf(",");
                if (commaIndex > 0) {
                    cleanedBase64 = base64Image.substring(commaIndex + 1);
                    log.info("移除Base64前缀后长度={}", cleanedBase64.length());
                }
            }
            
            byte[] imageBytes = Base64.getDecoder().decode(cleanedBase64);
            log.info("Base64解码成功, 图片字节数={}", imageBytes.length);
            
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + ".jpg";
            
            String dirPath = localPath + "/" + datePath;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("创建目录: {}, 结果={}", dirPath, created);
            }
            
            String filePath = dirPath + "/" + fileName;
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(imageBytes);
            }
            log.info("图片保存成功: {}", filePath);
            
            String relativePath = datePath + "/" + fileName;
            String url = urlPrefix + "/" + relativePath;
            log.info("生成图片URL: {}", url);
            return url;
        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败", e);
            throw new IOException("Base64解码失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("保存图片失败", e);
            throw new IOException("保存图片失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除图片文件
     * 
     * @param imagePath 图片URL路径
     */
    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        try {
            String relativePath = imagePath.replace(urlPrefix + "/", "");
            String fullPath = localPath + "/" + relativePath;
            
            Path path = Paths.get(fullPath);
            Files.deleteIfExists(path);
            
            log.info("删除图片文件: {}", fullPath);
        } catch (Exception e) {
            log.error("删除图片文件失败: {}", imagePath, e);
        }
    }
    
    /**
     * 批量删除图片文件
     * 
     * @param imagePaths 图片URL路径列表
     */
    public void deleteImages(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        
        for (String imagePath : imagePaths) {
            deleteImage(imagePath);
        }
    }
}
