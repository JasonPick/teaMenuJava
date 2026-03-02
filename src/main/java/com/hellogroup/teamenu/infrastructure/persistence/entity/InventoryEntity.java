package com.hellogroup.teamenu.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("inventory")
public class InventoryEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("ingredient_name")
    private String ingredientName;
    
    @TableField("quantity")
    private String quantity;
    
    @TableField("category_code")
    private String categoryCode;
    
    @TableField("expiry_date")
    private LocalDate expiryDate;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
