package com.hellogroup.teamenu.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hellogroup.teamenu.infrastructure.persistence.entity.InventoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存Mapper
 */
@Mapper
public interface InventoryMapper extends BaseMapper<InventoryEntity> {
    
    /**
     * 查询即将过期的食材
     */
    List<InventoryEntity> selectExpiringSoon(@Param("days") int days);
    
    /**
     * 查询已过期的食材
     */
    List<InventoryEntity> selectExpired(@Param("today") LocalDate today);
    
    /**
     * 删除已过期的食材
     */
    int deleteExpired(@Param("today") LocalDate today);
}
