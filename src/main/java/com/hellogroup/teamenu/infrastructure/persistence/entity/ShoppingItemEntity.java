package com.hellogroup.teamenu.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购清单项数据库实体
 * 
 * @author HelloGroup
 */
@Data
@TableName("shopping_item")
public class ShoppingItemEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 食材名称
     */
    private String ingredientName;
    
    /**
     * 需要采购的数量
     */
    private String quantity;
    
    /**
     * 是否已采购
     */
    private Boolean purchased;
    
    /**
     * 库存状态: NONE(无库存), EXPIRED(有但已过期), SUFFICIENT(有且未过期)
     */
    private String inventoryStatus;
    
    /**
     * 周标识（存储周一的日期）
     */
    private LocalDate weekIdentifier;
    
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
