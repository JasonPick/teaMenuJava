package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 菜谱计划项 - 领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanItem {
    
    /**
     * 计划ID
     */
    private Long id;
    
    /**
     * 食谱ID
     */
    private Long recipeId;
    
    /**
     * 计划日期
     */
    private LocalDate planDate;
    
    /**
     * 周类型: current=本周, next=下周
     */
    private String weekType;
    
    /**
     * 周标识(YYYY-Wnn格式,如2026-W04)
     */
    private String weekIdentifier;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 是否已删除
     */
    private Boolean deleted;
}
