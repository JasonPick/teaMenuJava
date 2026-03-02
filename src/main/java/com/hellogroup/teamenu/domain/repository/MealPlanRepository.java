package com.hellogroup.teamenu.domain.repository;

import com.hellogroup.teamenu.domain.model.MealPlanItem;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜谱计划仓储接口
 */
public interface MealPlanRepository {
    
    /**
     * 添加食谱到计划
     */
    Long addRecipeToPlan(MealPlanItem mealPlanItem);
    
    /**
     * 从计划中移除食谱
     */
    void removeRecipeFromPlan(Long id);
    
    /**
     * 获取指定日期的菜谱计划
     */
    List<MealPlanItem> fetchMealPlanByDate(LocalDate date, String weekType);
    
    /**
     * 获取指定周类型的所有计划项
     */
    List<MealPlanItem> fetchAllMealPlans(String weekType);
    
    /**
     * 获取整周的菜谱计划
     */
    List<MealPlanItem> fetchWeekPlan(String weekType, LocalDate startDate, LocalDate endDate);
    
    /**
     * 清理过期的菜谱计划(指定日期之前的)
     */
    void cleanupExpiredPlans(LocalDate beforeDate);
}
