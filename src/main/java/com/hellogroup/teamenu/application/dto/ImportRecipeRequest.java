package com.hellogroup.teamenu.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 导入食谱请求
 * 
 * @author HelloGroup
 */
@Data
public class ImportRecipeRequest {
    
    /**
     * 外部链接（小红书或下厨房）
     */
    @NotBlank(message = "链接不能为空")
    private String url;
    
    /**
     * 平台类型：xiaohongshu | xiachufang
     */
    @NotBlank(message = "平台类型不能为空")
    private String platform;
}
