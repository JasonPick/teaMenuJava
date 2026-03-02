package com.hellogroup.teamenu.application.service;

import com.hellogroup.teamenu.application.dto.AddMealPlanRequest;
import com.hellogroup.teamenu.application.dto.MealPlanDTO;
import com.hellogroup.teamenu.domain.model.MealPlanItem;
import com.hellogroup.teamenu.domain.repository.MealPlanRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 菜谱计划应用服务
 */
@Service
public class MealPlanApplicationService {
    
    @Resource
    private MealPlanRepository mealPlanRepository;
    
    /**
     * 添加食谱到计划
     */
    @Transactional(rollbackFor = Exception.class)
    public MealPlanDTO addRecipeToPlan(AddMealPlanRequest request) {
        MealPlanItem item = MealPlanItem.builder()
                .recipeId(request.getRecipeId())
                .planDate(request.getPlanDate())
                .weekType(request.getWeekType())
                .createTime(LocalDateTime.now())
                .build();
        
        Long id = mealPlanRepository.addRecipeToPlan(item);
        item.setId(id);
        
        return toDTO(item);
    }
    
    /**
     * 从计划中移除食谱
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeRecipeFromPlan(Long id) {
        mealPlanRepository.removeRecipeFromPlan(id);
    }
    
    /**
     * 获取指定日期的菜谱计划
     */
    public List<MealPlanDTO> fetchMealPlanByDate(LocalDate date, String weekType) {
        List<MealPlanItem> items = mealPlanRepository.fetchMealPlanByDate(date, weekType);
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取指定周类型的所有计划项
     */
    public List<MealPlanDTO> fetchAllMealPlans(String weekType) {
        List<MealPlanItem> items = mealPlanRepository.fetchAllMealPlans(weekType);
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取整周的菜谱计划
     */
    public List<MealPlanDTO> fetchWeekPlan(String weekType) {
        LocalDate startDate;
        LocalDate endDate;
        
        if ("current".equals(weekType)) {
            startDate = getCurrentWeekStart();
            endDate = startDate.plusDays(6);
        } else if ("next".equals(weekType)) {
            startDate = getNextWeekStart();
            endDate = startDate.plusDays(6);
        } else {
            throw new IllegalArgumentException("无效的周类型: " + weekType);
        }
        
        List<MealPlanItem> items = mealPlanRepository.fetchWeekPlan(weekType, startDate, endDate);
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 清理过期的菜谱计划
     */
    @Transactional(rollbackFor = Exception.class)
    public void cleanupExpiredPlans() {
        // 清理上周及更早的计划
        LocalDate lastWeekEnd = getCurrentWeekStart().minusDays(1);
        mealPlanRepository.cleanupExpiredPlans(lastWeekEnd);
    }
    
    /**
     * 获取当前周的开始日期(周一)
     */
    private LocalDate getCurrentWeekStart() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        return now.with(weekFields.dayOfWeek(), 1);
    }
    
    /**
     * 获取下周的开始日期
     */
    private LocalDate getNextWeekStart() {
        return getCurrentWeekStart().plusWeeks(1);
    }
    
    private MealPlanDTO toDTO(MealPlanItem item) {
        MealPlanDTO dto = new MealPlanDTO();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }
}
