package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 冰箱库存领域模型
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    
    /**
     * 库存ID
     */
    private Long id;
    
    /**
     * 食材名称
     */
    private String ingredientName;
    
    /**
     * 库存数量
     */
    private String quantity;
    
    /**
     * 食材分类
     */
    private IngredientCategory category;
    
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
    
    /**
     * 计算剩余有效期（天数）
     * 
     * @return 剩余天数，负数表示已过期
     */
    public long getRemainingDays() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
    
    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return getRemainingDays() < 0;
    }
    
    /**
     * 是否即将过期（3天内）
     */
    public boolean isExpiringSoon() {
        long days = getRemainingDays();
        return days >= 0 && days <= 3;
    }
}
