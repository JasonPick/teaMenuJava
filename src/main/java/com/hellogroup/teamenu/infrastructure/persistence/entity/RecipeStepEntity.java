package com.hellogroup.teamenu.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 食谱步骤数据库实体
 * 
 * @author HelloGroup
 */
@Data
@TableName("recipe_step")
public class RecipeStepEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 食谱ID
     */
    private Long recipeId;
    
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
