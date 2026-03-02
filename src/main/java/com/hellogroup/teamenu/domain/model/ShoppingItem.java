package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购清单项领域模型
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItem {
    
    /**
     * 采购项ID
     */
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
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否已删除
     */
    private Boolean deleted;
}
