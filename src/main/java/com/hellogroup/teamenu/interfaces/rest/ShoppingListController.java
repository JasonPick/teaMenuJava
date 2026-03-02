package com.hellogroup.teamenu.interfaces.rest;

import com.hellogroup.teamenu.application.dto.GenerateShoppingListRequest;
import com.hellogroup.teamenu.application.dto.ShoppingItemDTO;
import com.hellogroup.teamenu.application.service.ShoppingListApplicationService;
import com.hellogroup.teamenu.common.util.Result;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 采购清单REST接口
 */
@RestController
@RequestMapping("/api/shopping-list")
public class ShoppingListController {
    
    @Resource
    private ShoppingListApplicationService shoppingListApplicationService;
    
    /**
     * 生成采购清单
     */
    @PostMapping("/generate")
    public Result<List<ShoppingItemDTO>> generateShoppingList(@Valid @RequestBody GenerateShoppingListRequest request) {
        List<ShoppingItemDTO> items = shoppingListApplicationService.generateShoppingList(request);
        return Result.success(items);
    }
    
    /**
     * 根据周类型生成采购清单
     */
    @PostMapping("/generate/{weekType}")
    public Result<List<ShoppingItemDTO>> generateShoppingListByWeekType(@PathVariable String weekType) {
        List<ShoppingItemDTO> items = shoppingListApplicationService.generateShoppingListByWeekType(weekType);
        return Result.success(items);
    }
    
    /**
     * 获取采购清单
     */
    @GetMapping
    public Result<List<ShoppingItemDTO>> fetchShoppingList(@RequestParam(required = false) String weekIdentifier) {
        List<ShoppingItemDTO> items = shoppingListApplicationService.fetchShoppingList(weekIdentifier);
        return Result.success(items);
    }
    
    /**
     * 获取未采购的项
     */
    @GetMapping("/unpurchased")
    public Result<List<ShoppingItemDTO>> fetchUnpurchasedItems() {
        List<ShoppingItemDTO> items = shoppingListApplicationService.fetchUnpurchasedItems();
        return Result.success(items);
    }
    
    /**
     * 添加单个采购项
     */
    @PostMapping
    public Result<ShoppingItemDTO> addShoppingItem(@Valid @RequestBody ShoppingItemDTO dto) {
        ShoppingItemDTO item = shoppingListApplicationService.addShoppingItem(dto);
        return Result.success(item);
    }
    
    /**
     * 更新采购项
     */
    @PutMapping("/{id}")
    public Result<ShoppingItemDTO> updateShoppingItem(
            @PathVariable Long id,
            @Valid @RequestBody ShoppingItemDTO dto) {
        ShoppingItemDTO item = shoppingListApplicationService.updateShoppingItem(id, dto);
        return Result.success(item);
    }
    
    /**
     * 标记为已采购
     */
    @PutMapping("/{id}/purchase")
    public Result<Void> markAsPurchased(@PathVariable Long id) {
        shoppingListApplicationService.markAsPurchased(id);
        return Result.success();
    }
    
    /**
     * 删除采购项
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteShoppingItem(@PathVariable Long id) {
        shoppingListApplicationService.deleteShoppingItem(id);
        return Result.success();
    }
    
    /**
     * 清空采购清单
     */
    @DeleteMapping
    public Result<Void> clearShoppingList(@RequestParam(required = false) String weekType) {
        shoppingListApplicationService.clearShoppingList(weekType);
        return Result.success();
    }
}
