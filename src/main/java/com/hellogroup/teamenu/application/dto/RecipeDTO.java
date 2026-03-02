package com.hellogroup.teamenu.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 食谱DTO
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDTO {
    
    /**
     * 食谱ID
     */
    private Long id;
    
    /**
     * 食谱名称
     */
    @NotBlank(message = "食谱名称不能为空")
    private String name;
    
    /**
     * 食谱分类代码
     */
    @NotBlank(message = "食谱分类不能为空")
    private String categoryCode;
    
    /**
     * 完成时间（分钟）
     */
    @NotNull(message = "完成时间不能为空")
    private Integer completionTime;
    
    /**
     * 来源简述
     */
    private String source;
    
    /**
     * 是否需要预处理
     */
    private Boolean needsPreparation;
    
    /**
     * 食谱图片路径列表
     */
    private List<String> imagePaths;
    
    /**
     * Base64编码的图片数据列表（用于上传本地图片）
     */
    private List<String> imagesData;
    
    /**
     * 食材清单
     */
    private List<RecipeIngredientDTO> ingredients;
    
    /**
     * 制作步骤
     */
    private List<RecipeStepDTO> steps;
    
    /**
     * 食材是否齐全
     */
    private Boolean hasAllIngredients;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
