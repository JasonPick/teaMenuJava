package com.hellogroup.teamenu.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 生成采购清单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateShoppingListRequest {
    
    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    
    /**
     * 周类型: current=本周, next=下周, both=本周+下周
     */
    private String weekType;
}
