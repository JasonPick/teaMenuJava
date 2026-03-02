package com.hellogroup.teamenu.application.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hellogroup.teamenu.infrastructure.util.FlexibleGradeDeserializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class XiachufangRecipeDetailDto implements Serializable {
    /**
     * 内容
     */
    public List<RecipeContent> Content;

    @Data
    public static class RecipeContent implements Serializable{
        /**
         * 封面
         */
        private String cover;
        /**
         * 评分（可能是字符串 "8.8" 或数组 ["8.8"]）
         * 使用自定义反序列化器处理类型不一致问题
         */
        @JsonDeserialize(using = FlexibleGradeDeserializer.class)
        private List<String> grade;
        /**
         * 食材
         */
        private List<RecipeMaterial> materials;
        /**
         * 名称
         */
        private String name;
        /**
         * 步骤
         */
        private List<XiachufangRecipeStep> steps;
        /**
         * tip
         */
        private String tip;

    }
    @Data
    public static class RecipeMaterial implements Serializable{
        /**
         * 名称
         */
        private String name;
        /**
         * 数量
         */
        private String unit;
    }

    @Data
    public static class XiachufangRecipeStep implements Serializable{
        private String desc;
        private String img;
        private int step;
    }
}
