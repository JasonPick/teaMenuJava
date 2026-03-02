package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存食材 - 领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryIngredient {
    
    /**
     * 库存ID
     */
    private Long id;
    
    /**
     * 食材名称
     */
    private String name;
    
    /**
     * 库存数量描述
     */
    private String quantity;
    
    /**
     * 食材分类代码
     */
    private String categoryCode;
    
    /**
     * 过期日期
     */
    private LocalDate expiryDate;
    
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
