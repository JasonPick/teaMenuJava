package com.hellogroup.teamenu.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 菜谱计划项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanDTO {
    
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;
    
    /**
     * 周类型: current=本周, next=下周
     */
    private String weekType;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
