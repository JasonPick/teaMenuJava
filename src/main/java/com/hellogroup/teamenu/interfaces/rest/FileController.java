package com.hellogroup.teamenu.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件访问控制器
 * 提供上传文件的HTTP访问
 * 
 * @author HelloGroup
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${file.storage.local-path}")
    private String localPath;

    /**
     * 获取文件
     * 
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @param filename 文件名
     * @return 文件资源
     */
    @GetMapping("/{year}/{month}/{day}/{filename:.+}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String filename) {
        
        try {
            String filePath = localPath + "/" + year + "/" + month + "/" + day + "/" + filename;
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(path.toFile());
            
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("获取文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
