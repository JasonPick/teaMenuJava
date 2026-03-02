package com.hellogroup.teamenu.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 食谱步骤DTO
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepDTO {
    
    /**
     * 步骤ID
     */
    private Long id;
    
    /**
     * 步骤序号
     */
    @NotNull(message = "步骤序号不能为空")
    private Integer stepNumber;
    
    /**
     * 步骤描述
     */
    @NotBlank(message = "步骤描述不能为空")
    private String description;
    
    /**
     * 步骤图片路径
     */
    private String imagePath;
}
