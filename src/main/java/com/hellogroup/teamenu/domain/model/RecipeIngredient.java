package com.hellogroup.teamenu.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 食谱食材值对象
 * 
 * @author HelloGroup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {
    
    /**
     * 食材ID
     */
    private Long id;
    
    /**
     * 食材名称
     */
    private String name;
    
    /**
     * 数量描述（如"200g"、"2个"）
     */
    private String quantity;
    
    /**
     * 排序序号
     */
    private Integer sortOrder;
}
