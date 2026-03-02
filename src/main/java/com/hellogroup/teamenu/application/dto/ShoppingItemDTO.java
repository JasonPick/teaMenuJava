package com.hellogroup.teamenu.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购清单项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItemDTO {
    
    private Long id;
    
    @NotBlank(message = "食材名称不能为空")
    private String ingredientName;
    
    @NotBlank(message = "数量不能为空")
    private String quantity;
    
    @NotNull(message = "采购状态不能为空")
    private Boolean purchased;
    
    /**
     * 库存状态: NONE(无库存), EXPIRED(有但已过期), SUFFICIENT(有且未过期)
     */
    private String inventoryStatus;
    
    /**
     * 周标识(周一的日期)
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekIdentifier;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
