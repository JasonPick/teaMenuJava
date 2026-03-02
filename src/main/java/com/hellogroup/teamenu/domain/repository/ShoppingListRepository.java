package com.hellogroup.teamenu.domain.repository;

import com.hellogroup.teamenu.domain.model.ShoppingItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 采购清单仓储接口
 * 
 * @author HelloGroup
 */
public interface ShoppingListRepository {
    
    /**
     * 保存采购项
     * 
     * @param item 采购项
     * @return 保存后的采购项
     */
    ShoppingItem save(ShoppingItem item);
    
    /**
     * 根据ID查询采购项
     * 
     * @param id 采购项ID
     * @return 采购项
     */
    Optional<ShoppingItem> findById(Long id);
    
    /**
     * 根据周标识查询采购清单
     * 
     * @param weekIdentifier 周标识(周一的日期)
     * @return 采购项列表
     */
    List<ShoppingItem> findByWeek(LocalDate weekIdentifier);
    
    /**
     * 查询所有未采购的项
     * 
     * @return 采购项列表
     */
    List<ShoppingItem> findUnpurchased();
    
    /**
     * 更新采购项
     * 
     * @param item 采购项
     * @return 更新后的采购项
     */
    ShoppingItem update(ShoppingItem item);
    
    /**
     * 删除采购项
     * 
     * @param id 采购项ID
     */
    void deleteById(Long id);
    
    /**
     * 批量保存采购项
     * 
     * @param items 采购项列表
     */
    void batchSave(List<ShoppingItem> items);
    
    /**
     * 根据周标识删除采购清单
     * 
     * @param weekIdentifier 周标识(周一的日期)
     */
    void deleteByWeek(LocalDate weekIdentifier);
    
    /**
     * 删除指定周一及之后的所有采购清单
     * 
     * @param weekIdentifier 周标识(周一的日期)
     */
    void deleteByWeekGreaterThanEqual(LocalDate weekIdentifier);
}
