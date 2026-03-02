package com.hellogroup.teamenu.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 添加菜谱到计划请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMealPlanRequest {
    
    /**
     * 食谱ID
     */
    @NotNull(message = "食谱ID不能为空")
    private Long recipeId;
    
    /**
     * 计划日期
     */
    @NotNull(message = "计划日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;
    
    /**
     * 周类型: current=本周, next=下周
     */
    @NotNull(message = "周类型不能为空")
    private String weekType;
}
