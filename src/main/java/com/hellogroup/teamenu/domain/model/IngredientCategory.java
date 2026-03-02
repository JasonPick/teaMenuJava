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
    VEGETABLES_FRUITS("蔬菜水果", "vegetables_fruits"),
    
    /**
     * 肉禽蛋
     */
    MEAT_POULTRY_EGGS("肉禽蛋", "meat_poultry_eggs"),
    
    /**
     * 熟食
     */
    COOKED_FOOD("熟食", "cooked_food"),
    
    /**
     * 零食
     */
    SNACKS("零食", "snacks"),
    
    /**
     * 酱料
     */
    CONDIMENTS("酱料", "condiments");
    
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
