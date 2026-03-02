package com.hellogroup.teamenu.interfaces.rest;

import com.hellogroup.teamenu.application.dto.InventoryDTO;
import com.hellogroup.teamenu.application.service.InventoryApplicationService;
import com.hellogroup.teamenu.common.util.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 库存REST接口
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    @Resource
    private InventoryApplicationService inventoryApplicationService;
    
    /**
     * 添加库存食材
     */
    @PostMapping
    public Result<InventoryDTO> addIngredient(@Valid @RequestBody InventoryDTO dto) {
        InventoryDTO ingredient = inventoryApplicationService.addIngredient(dto);
        return Result.success(ingredient);
    }
    
    /**
     * 更新库存食材
     */
    @PutMapping("/{id}")
    public Result<InventoryDTO> updateIngredient(@PathVariable Long id, @Valid @RequestBody InventoryDTO dto) {
        InventoryDTO ingredient = inventoryApplicationService.updateIngredient(id, dto);
        return Result.success(ingredient);
    }
    
    /**
     * 删除库存食材
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteIngredient(@PathVariable Long id) {
        inventoryApplicationService.deleteIngredient(id);
        return Result.success();
    }
    
    /**
     * 获取库存列表
     */
    @GetMapping
    public Result<List<InventoryDTO>> fetchInventory(@RequestParam(required = false) String category) {
        List<InventoryDTO> inventory = inventoryApplicationService.fetchInventory(category);
        return Result.success(inventory);
    }
    
    /**
     * 获取已过期的食材
     */
    @GetMapping("/expired")
    public Result<List<InventoryDTO>> fetchExpiredIngredients() {
        List<InventoryDTO> ingredients = inventoryApplicationService.fetchExpiredIngredients();
        return Result.success(ingredients);
    }
    
    /**
     * 获取即将过期的食材
     */
    @GetMapping("/expiring-soon")
    public Result<List<InventoryDTO>> fetchExpiringSoonIngredients() {
        List<InventoryDTO> ingredients = inventoryApplicationService.fetchExpiringSoonIngredients();
        return Result.success(ingredients);
    }
    
    /**
     * 批量删除已过期食材
     */
    @DeleteMapping("/expired")
    public Result<Void> deleteExpiredIngredients() {
        inventoryApplicationService.deleteExpiredIngredients();
        return Result.success();
    }
}
