package com.hellogroup.teamenu.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hellogroup.teamenu.domain.model.MealPlanItem;
import com.hellogroup.teamenu.domain.repository.MealPlanRepository;
import com.hellogroup.teamenu.infrastructure.persistence.entity.MealPlanEntity;
import com.hellogroup.teamenu.infrastructure.persistence.mapper.MealPlanMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 菜谱计划仓储实现
 */
@Repository
public class MealPlanRepositoryImpl implements MealPlanRepository {
    
    @Resource
    private MealPlanMapper mealPlanMapper;
    
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("yyyy-'W'ww");
    
    @Override
    public Long addRecipeToPlan(MealPlanItem mealPlanItem) {
        MealPlanEntity entity = new MealPlanEntity();
        entity.setRecipeId(mealPlanItem.getRecipeId());
        entity.setPlanDate(mealPlanItem.getPlanDate());
        entity.setWeekIdentifier(calculateWeekIdentifier(mealPlanItem.getPlanDate()));
        entity.setCreateTime(mealPlanItem.getCreateTime());
        mealPlanMapper.insert(entity);
        return entity.getId();
    }
    
    @Override
    public void removeRecipeFromPlan(Long id) {
        mealPlanMapper.deleteById(id);
    }
    
    @Override
    public List<MealPlanItem> fetchMealPlanByDate(LocalDate date, String weekType) {
        LambdaQueryWrapper<MealPlanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealPlanEntity::getPlanDate, date);
        if (weekType != null && !weekType.isEmpty()) {
            String weekIdentifier = calculateWeekIdentifierForWeekType(weekType);
            wrapper.eq(MealPlanEntity::getWeekIdentifier, weekIdentifier);
        }
        wrapper.orderByAsc(MealPlanEntity::getCreateTime);
        
        List<MealPlanEntity> entities = mealPlanMapper.selectList(wrapper);
        return entities.stream()
                .map(e -> toDomain(e, weekType))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MealPlanItem> fetchAllMealPlans(String weekType) {
        String weekIdentifier = calculateWeekIdentifierForWeekType(weekType);
        LambdaQueryWrapper<MealPlanEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MealPlanEntity::getWeekIdentifier, weekIdentifier);
        wrapper.orderByAsc(MealPlanEntity::getPlanDate, MealPlanEntity::getCreateTime);
        
        List<MealPlanEntity> entities = mealPlanMapper.selectList(wrapper);
        return entities.stream()
                .map(e -> toDomain(e, weekType))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<MealPlanItem> fetchWeekPlan(String weekType, LocalDate startDate, LocalDate endDate) {
        String weekIdentifier = StringUtils.isBlank(weekType) ? StringUtils.EMPTY : calculateWeekIdentifierForWeekType(weekType);
        List<MealPlanEntity> entities = mealPlanMapper.selectByDateRange(startDate, endDate, weekIdentifier);
        return entities.stream()
                .map(e -> toDomain(e, weekType))
                .collect(Collectors.toList());
    }
    
    @Override
    public void cleanupExpiredPlans(LocalDate beforeDate) {
        mealPlanMapper.deleteBeforeDate(beforeDate);
    }
    
    /**
     * 计算weekIdentifier (YYYY-Wnn格式)
     */
    private String calculateWeekIdentifier(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        int year = date.get(weekFields.weekBasedYear());
        int week = date.get(weekFields.weekOfWeekBasedYear());
        return String.format("%d-W%02d", year, week);
    }
    
    /**
     * 根据weekType计算当前或下周的weekIdentifier
     */
    private String calculateWeekIdentifierForWeekType(String weekType) {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        LocalDate targetDate;
        
        if ("next".equals(weekType)) {
            // 下周
            targetDate = now.with(weekFields.dayOfWeek(), 1).plusWeeks(1);
        } else {
            // 本周(current或其他)
            targetDate = now.with(weekFields.dayOfWeek(), 1);
        }
        
        return calculateWeekIdentifier(targetDate);
    }
    
    private MealPlanItem toDomain(MealPlanEntity entity, String weekType) {
        MealPlanItem item = new MealPlanItem();
        BeanUtils.copyProperties(entity, item);
        item.setWeekType(weekType);
        return item;
    }
}
