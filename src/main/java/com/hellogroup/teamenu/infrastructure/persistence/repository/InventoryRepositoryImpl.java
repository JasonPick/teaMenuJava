package com.hellogroup.teamenu.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hellogroup.teamenu.domain.model.InventoryIngredient;
import com.hellogroup.teamenu.domain.repository.InventoryRepository;
import com.hellogroup.teamenu.infrastructure.persistence.entity.InventoryEntity;
import com.hellogroup.teamenu.infrastructure.persistence.mapper.InventoryMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存仓储实现
 */
@Repository
public class InventoryRepositoryImpl implements InventoryRepository {
    
    @Resource
    private InventoryMapper inventoryMapper;
    
    @Override
    public Long addIngredient(InventoryIngredient ingredient) {
        InventoryEntity entity = new InventoryEntity();
        entity.setIngredientName(ingredient.getName());
        entity.setQuantity(ingredient.getQuantity());
        entity.setCategoryCode(ingredient.getCategoryCode());
        entity.setExpiryDate(ingredient.getExpiryDate());
        entity.setCreateTime(ingredient.getCreateTime());
        entity.setUpdateTime(ingredient.getUpdateTime());
        inventoryMapper.insert(entity);
        return entity.getId();
    }
    
    @Override
    public void updateIngredient(InventoryIngredient ingredient) {
        InventoryEntity entity = new InventoryEntity();
        entity.setId(ingredient.getId());
        entity.setIngredientName(ingredient.getName());
        entity.setQuantity(ingredient.getQuantity());
        entity.setCategoryCode(ingredient.getCategoryCode());
        entity.setExpiryDate(ingredient.getExpiryDate());
        entity.setUpdateTime(ingredient.getUpdateTime());
        entity.setCreateTime(ingredient.getCreateTime());
        inventoryMapper.updateById(entity);
    }
    
    @Override
    public void deleteIngredient(Long id) {
        inventoryMapper.deleteById(id);
    }
    
    @Override
    public List<InventoryIngredient> fetchInventory(String categoryCode) {
        LambdaQueryWrapper<InventoryEntity> wrapper = new LambdaQueryWrapper<>();
        if (categoryCode != null && !categoryCode.isEmpty()) {
            wrapper.eq(InventoryEntity::getCategoryCode, categoryCode);
        }
        wrapper.orderByDesc(InventoryEntity::getCreateTime);
        
        List<InventoryEntity> entities = inventoryMapper.selectList(wrapper);
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryIngredient> fetchExpiredIngredients() {
        List<InventoryEntity> entities = inventoryMapper.selectExpired(LocalDate.now());
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<InventoryIngredient> fetchExpiringSoonIngredients() {
        List<InventoryEntity> entities = inventoryMapper.selectExpiringSoon(3);
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteExpiredIngredients() {
        inventoryMapper.deleteExpired(LocalDate.now());
    }
    
    @Override
    public InventoryIngredient findIngredientByName(String name) {
        LambdaQueryWrapper<InventoryEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryEntity::getIngredientName, name);
        wrapper.last("LIMIT 1");
        
        InventoryEntity entity = inventoryMapper.selectOne(wrapper);
        return entity != null ? toDomain(entity) : null;
    }
    
    @Override
    public InventoryIngredient findById(Long id) {
        InventoryEntity entity = inventoryMapper.selectById(id);
        return entity != null ? toDomain(entity) : null;
    }
    
    private InventoryIngredient toDomain(InventoryEntity entity) {
        InventoryIngredient ingredient = new InventoryIngredient();
        ingredient.setId(entity.getId());
        ingredient.setName(entity.getIngredientName());
        ingredient.setQuantity(entity.getQuantity());
        ingredient.setCategoryCode(entity.getCategoryCode());
        ingredient.setExpiryDate(entity.getExpiryDate());
        ingredient.setCreateTime(entity.getCreateTime());
        ingredient.setUpdateTime(entity.getUpdateTime());
        ingredient.setDeleted(entity.getDeleted());
        return ingredient;
    }
}
