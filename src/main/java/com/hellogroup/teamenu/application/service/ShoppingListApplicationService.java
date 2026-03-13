package com.hellogroup.teamenu.application.service;

import com.hellogroup.teamenu.application.dto.GenerateShoppingListRequest;
import com.hellogroup.teamenu.application.dto.ShoppingItemDTO;
import com.hellogroup.teamenu.domain.model.IngredientCategory;
import com.hellogroup.teamenu.domain.model.InventoryIngredient;
import com.hellogroup.teamenu.domain.model.ShoppingItem;
import com.hellogroup.teamenu.domain.repository.InventoryRepository;
import com.hellogroup.teamenu.domain.repository.ShoppingListRepository;
import com.hellogroup.teamenu.domain.service.IngredientService;
import com.hellogroup.teamenu.domain.service.ShoppingListGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购清单应用服务
 */
@Slf4j
@Service
public class ShoppingListApplicationService {
    
    @Resource
    private ShoppingListRepository shoppingListRepository;
    
    @Resource
    private InventoryRepository inventoryRepository;
    
    @Resource
    private ShoppingListGeneratorService shoppingListGeneratorService;
    
    @Resource
    private IngredientService ingredientService;
    
    /**
     * 生成采购清单
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ShoppingItemDTO> generateShoppingList(GenerateShoppingListRequest request) {
        // 计算weekIdentifier(使用开始日期所在周的周一)
        LocalDate weekIdentifier = shoppingListGeneratorService.getWeekMonday(request.getStartDate());
        
        // 生成采购清单
        List<ShoppingItem> items = shoppingListGeneratorService.generateShoppingList(
            request.getStartDate(),
            request.getEndDate(),
            weekIdentifier
        );
        
        // 批量保存
        shoppingListRepository.batchSave(items);
        
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据周类型生成采购清单
     */
    @Transactional(rollbackFor = Exception.class)
    public List<ShoppingItemDTO> generateShoppingListByWeekType(String weekType) {
        LocalDate currentMonday = shoppingListGeneratorService.getWeekMonday(LocalDate.now());
        LocalDate nextMonday = currentMonday.plusWeeks(1);
        
        List<ShoppingItem> allItems = new ArrayList<>();
        
        if ("current".equals(weekType)) {
            // 只生成本周
            List<ShoppingItem> items = shoppingListGeneratorService.generateShoppingList(
                currentMonday, 
                currentMonday.plusDays(6), 
                currentMonday
            );
            allItems.addAll(items);
        } else if ("next".equals(weekType)) {
            // 只生成下周
            List<ShoppingItem> items = shoppingListGeneratorService.generateShoppingList(
                nextMonday, 
                nextMonday.plusDays(6), 
                nextMonday
            );
            allItems.addAll(items);
        } else if ("both".equals(weekType)) {
            // 生成本周
            List<ShoppingItem> currentItems = shoppingListGeneratorService.generateShoppingList(
                currentMonday, 
                currentMonday.plusDays(6), 
                currentMonday
            );
            allItems.addAll(currentItems);
            
            // 生成下周
            List<ShoppingItem> nextItems = shoppingListGeneratorService.generateShoppingList(
                nextMonday, 
                nextMonday.plusDays(6), 
                nextMonday
            );
            allItems.addAll(nextItems);
        } else {
            throw new IllegalArgumentException("无效的周类型: " + weekType);
        }
        
        shoppingListRepository.batchSave(allItems);
        
        return allItems.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取采购清单
     */
    public List<ShoppingItemDTO> fetchShoppingList(String weekIdentifier) {
        List<ShoppingItem> items;
        if (weekIdentifier != null && !weekIdentifier.isEmpty()) {
            // 将字符串日期转换为LocalDate
            try {
                LocalDate weekDate = LocalDate.parse(weekIdentifier);
                weekDate = shoppingListGeneratorService.getWeekMonday(weekDate);
                items = shoppingListRepository.findByWeek(weekDate);
            } catch (Exception e) {
                // 如果解析失败,返回空列表
                items = Collections.emptyList();
            }
        } else {
            items = shoppingListRepository.findUnpurchased();
        }
        
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取未采购的项
     */
    public List<ShoppingItemDTO> fetchUnpurchasedItems() {
        List<ShoppingItem> items = shoppingListRepository.findUnpurchased();
        return items.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 添加单个采购项
     */
    @Transactional(rollbackFor = Exception.class)
    public ShoppingItemDTO addShoppingItem(ShoppingItemDTO dto) {
        // 计算当前周标识(周一日期)
        LocalDate weekIdentifier = dto.getWeekIdentifier();
        if (weekIdentifier == null) {
            weekIdentifier = shoppingListGeneratorService.getWeekMonday(LocalDate.now());
        }
        
        ShoppingItem item = ShoppingItem.builder()
                .ingredientName(dto.getIngredientName())
                .quantity(dto.getQuantity())
                .purchased(false)
                .weekIdentifier(weekIdentifier)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        ShoppingItem savedItem = shoppingListRepository.save(item);
        return toDTO(savedItem);
    }
    
    /**
     * 更新采购项
     */
    @Transactional(rollbackFor = Exception.class)
    public ShoppingItemDTO updateShoppingItem(Long id, ShoppingItemDTO dto) {
        ShoppingItem item = ShoppingItem.builder()
                .id(id)
                .ingredientName(dto.getIngredientName())
                .quantity(dto.getQuantity())
                .purchased(dto.getPurchased())
                .weekIdentifier(dto.getWeekIdentifier())
                .createTime(dto.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        
        ShoppingItem updated = shoppingListRepository.update(item);
        return toDTO(updated);
    }
    
    /**
     * 标记为已采购
     * 标记后自动将食材添加到冰箱库存
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsPurchased(Long id) {
        ShoppingItem item = shoppingListRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("采购项不存在: " + id));
        
        // 1. 标记为已采购
        item.setPurchased(true);
        item.setUpdateTime(LocalDateTime.now());
        shoppingListRepository.update(item);
        
        // 2. 根据库存状态处理
        String inventoryStatus = item.getInventoryStatus();
        
        if ("EXPIRED".equals(inventoryStatus)) {
            // 有但已过期,需要更新库存(删除旧的,添加新的)
            InventoryIngredient oldInventory = inventoryRepository.findIngredientByName(item.getIngredientName());
            if (oldInventory != null) {
                inventoryRepository.deleteIngredient(oldInventory.getId());
            }
        } else if ("SUFFICIENT".equals(inventoryStatus)) {
            // 有且未过期,不需要添加(理论上不会走到这里,因为有库存的不会出现在采购清单)
            return;
        }
        
        // 3. 使用AI分类服务获取食材分类
        String categoryCode = "OTHER"; // 默认分类
        int expiryDays = 7; // 默认保质期7天
        
        try {
            List<String> ingredientList = Collections.singletonList(item.getIngredientName());
            Map<String, String> classificationResult = ingredientService.ingredientClassification(ingredientList);
            
            if (classificationResult != null && classificationResult.containsKey(item.getIngredientName())) {
                String displayName = classificationResult.get(item.getIngredientName());
                // 根据displayName获取对应的categoryCode和保质期
                IngredientCategory category = IngredientCategory.getByDisplayName(displayName);
                if (category != null) {
                    categoryCode = category.getCode();
                    expiryDays = category.getDefaultExpiryDays();
                    log.info("食材 {} 分类为: {} ({}), 保质期: {}天", 
                            item.getIngredientName(), displayName, categoryCode, expiryDays);
                } else {
                    log.warn("未找到分类 {} 对应的枚举值,使用默认分类 OTHER (保质期7天)", displayName);
                }
            } else {
                log.warn("AI分类服务未返回食材 {} 的分类结果,使用默认分类 OTHER (保质期7天)", item.getIngredientName());
            }
        } catch (Exception e) {
            log.error("调用AI分类服务失败,使用默认分类 OTHER (保质期7天)", e);
        }
        
        // 4. 计算过期日期
        LocalDate expiryDate = LocalDate.now().plusDays(expiryDays);
        
        // 5. 添加到冰箱库存
        InventoryIngredient newInventory = InventoryIngredient.builder()
                .name(item.getIngredientName())
                .quantity(item.getQuantity())
                .categoryCode(categoryCode)
                .expiryDate(expiryDate)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .deleted(false)
                .build();
        
        inventoryRepository.addIngredient(newInventory);
    }
    
    /**
     * 删除采购项
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteShoppingItem(Long id) {
        shoppingListRepository.deleteById(id);
    }
    
    /**
     * 清空采购清单
     */
    @Transactional(rollbackFor = Exception.class)
    public void clearShoppingList(String weekType) {
        if (weekType == null || weekType.isEmpty()) {
            return;
        }
        
        LocalDate currentMonday = shoppingListGeneratorService.getWeekMonday(LocalDate.now());
        
        if ("current".equals(weekType)) {
            // 清空本周
            shoppingListRepository.deleteByWeek(currentMonday);
        } else if ("next".equals(weekType)) {
            // 清空下周
            LocalDate nextMonday = currentMonday.plusWeeks(1);
            shoppingListRepository.deleteByWeek(nextMonday);
        } else if ("both".equals(weekType)) {
            // 清空本周及之后的所有
            shoppingListRepository.deleteByWeekGreaterThanEqual(currentMonday);
        }
    }
    
    private ShoppingItemDTO toDTO(ShoppingItem item) {
        ShoppingItemDTO dto = new ShoppingItemDTO();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }
}
