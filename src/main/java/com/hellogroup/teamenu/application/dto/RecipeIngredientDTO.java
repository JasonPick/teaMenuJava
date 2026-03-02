package com.hellogroup.teamenu.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 食谱食材DTO
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientDTO {
    
    /**
     * 食材ID
     */
    private Long id;
    
    /**
     * 食材名称
     */
    @NotBlank(message = "食材名称不能为空")
    private String name;
    
    /**
     * 数量描述
     */
    @NotBlank(message = "食材数量不能为空")
    private String quantity;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
}
