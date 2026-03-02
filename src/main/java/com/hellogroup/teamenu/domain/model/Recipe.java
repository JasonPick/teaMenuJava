package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 食谱领域模型
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    
    /**
     * 食谱ID
     */
    private Long id;
    
    /**
     * 食谱名称
     */
    private String name;
    
    /**
     * 食谱分类
     */
    private RecipeCategory category;
    
    /**
     * 完成时间（分钟）
     */
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
     * 食材清单
     */
    private List<RecipeIngredient> ingredients;
    
    /**
     * 制作步骤
     */
    private List<RecipeStep> steps;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否已删除
     */
    private Boolean deleted;
    
    /**
     * 检查食材是否齐全
     * 
     * @param availableIngredients 可用食材列表
     * @return 是否齐全
     */
    public boolean hasAllIngredients(List<String> availableIngredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return true;
        }
        
        for (RecipeIngredient ingredient : ingredients) {
            if (!availableIngredients.contains(ingredient.getName())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 更新最后访问时间
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }
}
