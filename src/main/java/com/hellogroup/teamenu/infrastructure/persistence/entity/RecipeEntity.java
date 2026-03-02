package com.hellogroup.teamenu.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 食谱数据库实体
 * 
 * @author HelloGroup
 */
@Data
@TableName("recipe")
public class RecipeEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 食谱名称
     */
    private String name;
    
    /**
     * 食谱分类代码
     */
    private String categoryCode;
    
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
     * 食谱图片路径（JSON数组字符串）
     */
    private String imagePaths;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 是否已删除
     */
    @TableLogic
    private Boolean deleted;
}
