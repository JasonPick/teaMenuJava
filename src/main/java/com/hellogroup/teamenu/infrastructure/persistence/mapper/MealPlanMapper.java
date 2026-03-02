package com.hellogroup.teamenu.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hellogroup.teamenu.infrastructure.persistence.entity.MealPlanEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 菜谱计划Mapper
 */
@Mapper
public interface MealPlanMapper extends BaseMapper<MealPlanEntity> {
    
    /**
     * 查询指定日期范围内的计划
     */
    List<MealPlanEntity> selectByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("weekType") String weekType
    );
    
    /**
     * 删除指定日期之前的计划
     */
    int deleteBeforeDate(@Param("beforeDate") LocalDate beforeDate);
}
