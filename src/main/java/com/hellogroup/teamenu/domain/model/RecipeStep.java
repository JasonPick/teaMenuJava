package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 食谱步骤值对象
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStep {
    
    /**
     * 步骤ID
     */
    private Long id;
    
    /**
     * 步骤序号
     */
    private Integer stepNumber;
    
    /**
     * 步骤描述
     */
    private String description;
    
    /**
     * 步骤图片路径
     */
    private String imagePath;
}
