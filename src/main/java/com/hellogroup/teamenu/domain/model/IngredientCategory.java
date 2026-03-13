package com.hellogroup.teamenu.domain.model;

import dev.langchain4j.data.document.Document;
import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

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
    public static List<String> codes(){
        return Stream.of(IngredientCategory.values()).map(IngredientCategory::getCode).toList();
    }

    public static List<String> names(){
        return Stream.of(IngredientCategory.values()).map(IngredientCategory::getDisplayName).toList();
    }

    public static IngredientCategory getByDisplayName(String displayName) {
        for (IngredientCategory category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知的食材分类: " + displayName);
    }
}
