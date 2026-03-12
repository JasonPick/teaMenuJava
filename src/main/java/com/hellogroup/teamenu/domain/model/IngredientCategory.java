package com.hellogroup.teamenu.domain.model;

import lombok.Getter;

/**
 * 食材分类枚举
 * 
 * @author HelloGroup
 */
@Getter
public enum IngredientCategory {
    
    /**
     * 蔬菜水果
     */
    VEGETABLES_FRUITS("蔬菜水果", "VEGETABLE"),
    
    /**
     * 肉禽蛋
     */
    MEAT_POULTRY_EGGS("肉禽蛋", "MEAT"),
    
    /**
     * 熟食
     */
    COOKED_FOOD("熟食", "COOKED"),
    
    /**
     * 零食
     */
    SNACKS("零食", "SNACKS"),
    
    /**
     * 酱料
     */
    SEASONING("酱料", "SEASONING");
    
    private final String displayName;
    private final String code;
    
    IngredientCategory(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
    
    /**
     * 根据code获取枚举
     */
    public static IngredientCategory fromCode(String code) {
        for (IngredientCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知的食材分类: " + code);
    }
}
