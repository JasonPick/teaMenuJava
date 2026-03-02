package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每周菜谱计划领域模型
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlan {
    
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
     * 周标识（如"2026-W04"表示2026年第4周）
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
