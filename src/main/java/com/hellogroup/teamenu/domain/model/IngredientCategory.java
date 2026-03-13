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
    SEASONING("酱料", "SEASONING"),
    
    /**
     * 其他
     */
    OTHER("其他", "OTHER");
    
    private final String displayName;
    private final String code;
    
    IngredientCategory(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
    
    /**
     * 获取该分类食材的默认保质期（天数）
     * 
     * @return 保质期天数
     */
    public int getDefaultExpiryDays() {
        return switch (this) {
            case VEGETABLES_FRUITS -> 7;      // 蔬菜水果：7天
            case MEAT_POULTRY_EGGS -> 3;      // 肉禽蛋：3天（冷藏）
            case COOKED_FOOD -> 2;            // 熟食：2天
            case SNACKS -> 30;                // 零食：30天
            case SEASONING -> 90;             // 酱料：90天
            case OTHER -> 7;                  // 其他：默认7天
        };
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
