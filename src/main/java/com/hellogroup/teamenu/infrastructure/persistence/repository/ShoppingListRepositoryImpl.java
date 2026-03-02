package com.hellogroup.teamenu.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hellogroup.teamenu.domain.model.ShoppingItem;
import com.hellogroup.teamenu.domain.repository.ShoppingListRepository;
import com.hellogroup.teamenu.infrastructure.persistence.entity.ShoppingItemEntity;
import com.hellogroup.teamenu.infrastructure.persistence.mapper.ShoppingItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 采购清单仓储实现
 * 
 * @author HelloGroup
 */
@Slf4j
@Repository
public class ShoppingListRepositoryImpl implements ShoppingListRepository {
    
    private final ShoppingItemMapper shoppingItemMapper;
    
    public ShoppingListRepositoryImpl(ShoppingItemMapper shoppingItemMapper) {
        this.shoppingItemMapper = shoppingItemMapper;
    }
    
    @Override
    public ShoppingItem save(ShoppingItem item) {
        try {
            ShoppingItemEntity entity = toEntity(item);
            shoppingItemMapper.insert(entity);
            item.setId(entity.getId());
            return item;
        } catch (Exception e) {
            log.error("保存采购项失败", e);
            throw new RuntimeException("保存采购项失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<ShoppingItem> findById(Long id) {
        try {
            ShoppingItemEntity entity = shoppingItemMapper.selectById(id);
            return Optional.ofNullable(entity).map(this::toDomain);
        } catch (Exception e) {
            log.error("查询采购项失败, id={}", id, e);
            throw new RuntimeException("查询采购项失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ShoppingItem> findByWeek(LocalDate weekIdentifier) {
        try {
            LambdaQueryWrapper<ShoppingItemEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingItemEntity::getWeekIdentifier, weekIdentifier);
            
            List<ShoppingItemEntity> entities = shoppingItemMapper.selectList(wrapper);
            return entities.stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询周采购清单失败, weekIdentifier={}", weekIdentifier, e);
            throw new RuntimeException("查询周采购清单失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<ShoppingItem> findUnpurchased() {
        try {
            LambdaQueryWrapper<ShoppingItemEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingItemEntity::getPurchased, false);
            
            List<ShoppingItemEntity> entities = shoppingItemMapper.selectList(wrapper);
            return entities.stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询未采购项失败", e);
            throw new RuntimeException("查询未采购项失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public ShoppingItem update(ShoppingItem item) {
        try {
            ShoppingItemEntity entity = toEntity(item);
            shoppingItemMapper.updateById(entity);
            return item;
        } catch (Exception e) {
            log.error("更新采购项失败", e);
            throw new RuntimeException("更新采购项失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteById(Long id) {
        try {
            shoppingItemMapper.deleteById(id);
        } catch (Exception e) {
            log.error("删除采购项失败, id={}", id, e);
            throw new RuntimeException("删除采购项失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSave(List<ShoppingItem> items) {
        try {
            if (items != null && !items.isEmpty()) {
                for (ShoppingItem item : items) {
                    save(item);
                }
            }
        } catch (Exception e) {
            log.error("批量保存采购项失败", e);
            throw new RuntimeException("批量保存采购项失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteByWeek(LocalDate weekIdentifier) {
        try {
            LambdaQueryWrapper<ShoppingItemEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShoppingItemEntity::getWeekIdentifier, weekIdentifier);
            shoppingItemMapper.delete(wrapper);
        } catch (Exception e) {
            log.error("删除周采购清单失败, weekIdentifier={}", weekIdentifier, e);
            throw new RuntimeException("删除周采购清单失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteByWeekGreaterThanEqual(LocalDate weekIdentifier) {
        try {
            LambdaQueryWrapper<ShoppingItemEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(ShoppingItemEntity::getWeekIdentifier, weekIdentifier);
            shoppingItemMapper.delete(wrapper);
        } catch (Exception e) {
            log.error("删除周采购清单失败, weekIdentifier>={}", weekIdentifier, e);
            throw new RuntimeException("删除周采购清单失败: " + e.getMessage(), e);
        }
    }
    
    private ShoppingItem toDomain(ShoppingItemEntity entity) {
        return ShoppingItem.builder()
                .id(entity.getId())
                .ingredientName(entity.getIngredientName())
                .quantity(entity.getQuantity())
                .purchased(entity.getPurchased())
                .inventoryStatus(entity.getInventoryStatus())
                .weekIdentifier(entity.getWeekIdentifier())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .deleted(entity.getDeleted())
                .build();
    }
    
    private ShoppingItemEntity toEntity(ShoppingItem item) {
        ShoppingItemEntity entity = new ShoppingItemEntity();
        entity.setId(item.getId());
        entity.setIngredientName(item.getIngredientName());
        entity.setQuantity(item.getQuantity());
        entity.setPurchased(item.getPurchased());
        entity.setInventoryStatus(item.getInventoryStatus());
        entity.setWeekIdentifier(item.getWeekIdentifier());
        entity.setCreateTime(item.getCreateTime());
        entity.setUpdateTime(item.getUpdateTime());
        return entity;
    }
}
