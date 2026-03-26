package com.hellogroup.teamenu.application.service;

import com.hellogroup.teamenu.application.dto.InventoryDTO;
import com.hellogroup.teamenu.domain.model.InventoryIngredient;
import com.hellogroup.teamenu.domain.repository.InventoryRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存应用服务
 */
@Service
public class InventoryApplicationService {
    
    @Resource
    private InventoryRepository inventoryRepository;
    
    @Transactional(rollbackFor = Exception.class)
    public InventoryDTO addIngredient(InventoryDTO dto) {
        InventoryIngredient ingredient = new InventoryIngredient();
        ingredient.setName(dto.getName());
        ingredient.setQuantity(dto.getQuantity());
        ingredient.setCategoryCode(dto.getCategoryCode());
        ingredient.setExpiryDate(dto.getExpiryDate());
        ingredient.setCreateTime(LocalDateTime.now());
        ingredient.setUpdateTime(LocalDateTime.now());
        
        Long id = inventoryRepository.addIngredient(ingredient);
        ingredient.setId(id);
        
        return toDTO(ingredient);
    }
    
    @Transactional(rollbackFor = Exception.class)
    public InventoryDTO updateIngredient(Long id, InventoryDTO dto) {
        InventoryIngredient ingredient = new InventoryIngredient();
        ingredient.setId(id);
        ingredient.setName(dto.getName());
        ingredient.setQuantity(dto.getQuantity());
        ingredient.setCategoryCode(dto.getCategoryCode());
        ingredient.setExpiryDate(dto.getExpiryDate());
        ingredient.setUpdateTime(LocalDateTime.now());
        ingredient.setCreateTime(dto.getCreateTime());
        
        inventoryRepository.updateIngredient(ingredient);
        
        return toDTO(inventoryRepository.findById(id));
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteIngredient(Long id) {
        inventoryRepository.deleteIngredient(id);
    }
    
    public List<InventoryDTO> fetchInventory(String categoryCode) {
        List<InventoryIngredient> ingredients = inventoryRepository.fetchInventory(categoryCode);
        return ingredients.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<InventoryDTO> fetchExpiredIngredients() {
        List<InventoryIngredient> ingredients = inventoryRepository.fetchExpiredIngredients();
        return ingredients.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<InventoryDTO> fetchExpiringSoonIngredients() {
        List<InventoryIngredient> ingredients = inventoryRepository.fetchExpiringSoonIngredients();
        return ingredients.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteExpiredIngredients() {
        inventoryRepository.deleteExpiredIngredients();
    }
    
    private InventoryDTO toDTO(InventoryIngredient ingredient) {
        InventoryDTO dto = new InventoryDTO();
        BeanUtils.copyProperties(ingredient, dto);
        return dto;
    }
}
