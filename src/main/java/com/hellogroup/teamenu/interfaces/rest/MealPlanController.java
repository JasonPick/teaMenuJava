package com.hellogroup.teamenu.interfaces.rest;

import com.hellogroup.teamenu.application.dto.AddMealPlanRequest;
import com.hellogroup.teamenu.application.dto.MealPlanDTO;
import com.hellogroup.teamenu.application.service.MealPlanApplicationService;
import com.hellogroup.teamenu.common.util.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * 菜谱计划REST接口
 */
@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {
    
    @Resource
    private MealPlanApplicationService mealPlanApplicationService;
    
    /**
     * 添加食谱到计划
     */
    @PostMapping
    public Result<MealPlanDTO> addRecipeToPlan(@Valid @RequestBody AddMealPlanRequest request) {
        MealPlanDTO mealPlan = mealPlanApplicationService.addRecipeToPlan(request);
        return Result.success(mealPlan);
    }
    
    /**
     * 从计划中移除食谱
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeRecipeFromPlan(@PathVariable Long id) {
        mealPlanApplicationService.removeRecipeFromPlan(id);
        return Result.success();
    }
    
    /**
     * 获取指定日期的菜谱计划
     */
    @GetMapping("/date/{date}")
    public Result<List<MealPlanDTO>> fetchMealPlanByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String weekType) {
        List<MealPlanDTO> plans = mealPlanApplicationService.fetchMealPlanByDate(date, weekType);
        return Result.success(plans);
    }
    
    /**
     * 获取整周的菜谱计划
     */
    @GetMapping("/week/{weekType}")
    public Result<List<MealPlanDTO>> fetchWeekPlan(@PathVariable String weekType) {
        List<MealPlanDTO> plans = mealPlanApplicationService.fetchWeekPlan(weekType);
        return Result.success(plans);
    }
    
    /**
     * 获取指定周类型的所有计划项
     */
    @GetMapping
    public Result<List<MealPlanDTO>> fetchAllMealPlans(@RequestParam String weekType) {
        List<MealPlanDTO> plans = mealPlanApplicationService.fetchAllMealPlans(weekType);
        return Result.success(plans);
    }
    
    /**
     * 清理过期的菜谱计划
     */
    @DeleteMapping("/cleanup")
    public Result<Void> cleanupExpiredPlans() {
        mealPlanApplicationService.cleanupExpiredPlans();
        return Result.success();
    }
}
