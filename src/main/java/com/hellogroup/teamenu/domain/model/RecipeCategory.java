package com.hellogroup.teamenu.domain.model;

import lombok.Getter;

/**
 * 食谱分类枚举
 * 
 * @author HelloGroup
 */
@Getter
public enum RecipeCategory {
    
    /**
     * 主食
     */
    STAPLE("主食", "staple"),
    
    /**
     * 甜品
     */
    DESSERT("甜品", "dessert"),
    
    /**
     * 面包
     */
    BREAD("面包", "bread"),
    
    /**
     * 蛋白质
     */
    PROTEIN("蛋白质", "protein");
    
    private final String displayName;
    private final String code;
    
    RecipeCategory(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
    
    /**
     * 根据code获取枚举
     */
    public static RecipeCategory fromCode(String code) {
        for (RecipeCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        throw new IllegalArgumentException("未知的食谱分类: " + code);
    }
}
