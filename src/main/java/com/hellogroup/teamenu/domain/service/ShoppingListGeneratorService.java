package com.hellogroup.teamenu.domain.service;

import com.hellogroup.teamenu.domain.model.*;
import com.hellogroup.teamenu.domain.repository.InventoryRepository;
import com.hellogroup.teamenu.domain.repository.MealPlanRepository;
import com.hellogroup.teamenu.domain.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购清单生成领域服务
 * 
 * @author HelloGroup
 */
@Service
public class ShoppingListGeneratorService {
    
    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;
    private final InventoryRepository inventoryRepository;
    
    public ShoppingListGeneratorService(
            MealPlanRepository mealPlanRepository,
            RecipeRepository recipeRepository,
            InventoryRepository inventoryRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.recipeRepository = recipeRepository;
        this.inventoryRepository = inventoryRepository;
    }
    
    /**
     * 根据日期范围生成采购清单
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param weekIdentifier 周标识(周一日期)
     * @return 采购项列表
     */
    public List<ShoppingItem> generateShoppingList(LocalDate startDate, LocalDate endDate, LocalDate weekIdentifier) {
        // 1. 获取日期范围内的所有菜谱计划
        List<MealPlanItem> mealPlans = mealPlanRepository.fetchWeekPlan(null, startDate, endDate);
        
        if (mealPlans.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. 获取所有食谱ID
        List<Long> recipeIds = mealPlans.stream()
                .map(MealPlanItem::getRecipeId)
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 批量查询食谱详情
        List<Recipe> recipes = new ArrayList<>();
        for (Long recipeId : recipeIds) {
            Optional<Recipe> recipe = recipeRepository.findById(recipeId);
            if (recipe != null) {
                recipes.add(recipe.get());
            }
        }
        
        // 4. 汇总所有食材需求
        Map<String, String> ingredientMap = new HashMap<>();
        for (Recipe recipe : recipes) {
            if (recipe.getIngredients() != null) {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    String name = ingredient.getName();
                    String quantity = ingredient.getQuantity();
                    
                    if (ingredientMap.containsKey(name)) {
                        ingredientMap.put(name, mergeQuantity(ingredientMap.get(name), quantity));
                    } else {
                        ingredientMap.put(name, quantity);
                    }
                }
            }
        }
        
        // 5. 查询冰箱库存
        List<InventoryIngredient> inventories = inventoryRepository.fetchInventory(null);
        Map<String, InventoryIngredient> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryIngredient::getName, inv -> inv, (a, b) -> a));
        
        // 6. 生成采购清单
        List<ShoppingItem> shoppingItems = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (Map.Entry<String, String> entry : ingredientMap.entrySet()) {
            String ingredientName = entry.getKey();
            String requiredQuantity = entry.getValue();
            
            // 检查库存
            InventoryIngredient inventory = inventoryMap.get(ingredientName);
            String inventoryStatus;
            boolean needToBuy;
            
            if (inventory == null) {
                inventoryStatus = "NONE";
                needToBuy = true;
            } else {
                boolean isExpired = inventory.getExpiryDate() != null 
                    && inventory.getExpiryDate().isBefore(today);
                
                if (isExpired) {
                    inventoryStatus = "EXPIRED";
                    needToBuy = true;
                } else {
                    inventoryStatus = "SUFFICIENT";
                    needToBuy = false;
                }
            }
            
            if (needToBuy) {
                ShoppingItem item = ShoppingItem.builder()
                        .ingredientName(ingredientName)
                        .quantity(requiredQuantity)
                        .purchased(false)
                        .inventoryStatus(inventoryStatus)
                        .weekIdentifier(weekIdentifier)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                shoppingItems.add(item);
            }
        }
        
        return shoppingItems;
    }
    
    /**
     * 获取指定日期所在周的周一
     */
    public LocalDate getWeekMonday(LocalDate date) {
        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
        return date.minusDays(dayOfWeek.getValue() - 1);
    }
    
    /**
     * 合并数量（简化实现）
     * 实际应用中需要解析单位并进行转换
     */
    private String mergeQuantity(String quantity1, String quantity2) {
        // 简化实现：直接拼接
        return quantity1 + " + " + quantity2;
    }
}
