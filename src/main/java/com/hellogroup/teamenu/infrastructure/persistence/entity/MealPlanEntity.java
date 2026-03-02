package com.hellogroup.teamenu.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 菜谱计划表实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("meal_plan")
public class MealPlanEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("recipe_id")
    private Long recipeId;
    
    @TableField("plan_date")
    private LocalDate planDate;
    
    @TableField("week_identifier")
    private String weekIdentifier;
    
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
