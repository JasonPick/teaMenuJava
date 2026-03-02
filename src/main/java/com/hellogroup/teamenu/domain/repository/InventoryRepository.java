package com.hellogroup.teamenu.domain.repository;

import com.hellogroup.teamenu.domain.model.InventoryIngredient;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存食材仓储接口
 */
public interface InventoryRepository {
    
    /**
     * 添加库存食材
     */
    Long addIngredient(InventoryIngredient ingredient);
    
    /**
     * 更新库存食材
     */
    void updateIngredient(InventoryIngredient ingredient);
    
    /**
     * 删除库存食材
     */
    void deleteIngredient(Long id);
    
    /**
     * 获取库存列表
     */
    List<InventoryIngredient> fetchInventory(String categoryCode);
    
    /**
     * 获取已过期的食材
     */
    List<InventoryIngredient> fetchExpiredIngredients();
    
    /**
     * 获取即将过期的食材(3天内)
     */
    List<InventoryIngredient> fetchExpiringSoonIngredients();
    
    /**
     * 批量删除已过期食材
     */
    void deleteExpiredIngredients();
    
    /**
     * 根据名称查找库存食材
     */
    InventoryIngredient findIngredientByName(String name);
    
    /**
     * 根据ID查找
     */
    InventoryIngredient findById(Long id);
}
