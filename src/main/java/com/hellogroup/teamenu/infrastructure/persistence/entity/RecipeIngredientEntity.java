package com.hellogroup.teamenu.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 食谱食材数据库实体
 * 
 * @author HelloGroup
 */
@Data
@TableName("recipe_ingredient")
public class RecipeIngredientEntity {
    
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
     * 食材名称
     */
    private String name;
    
    /**
     * 数量描述
     */
    private String quantity;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
}
