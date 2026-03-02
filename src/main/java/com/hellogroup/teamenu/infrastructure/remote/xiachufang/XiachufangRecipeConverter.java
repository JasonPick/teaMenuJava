package com.hellogroup.teamenu.infrastructure.remote.xiachufang;

import com.hellogroup.teamenu.application.dto.XiachufangRecipeDetailDto;
import com.hellogroup.teamenu.common.constant.ResponseCode;
import com.hellogroup.teamenu.common.exception.BusinessException;
import com.hellogroup.teamenu.domain.model.Recipe;
import com.hellogroup.teamenu.domain.model.RecipeCategory;
import com.hellogroup.teamenu.domain.model.RecipeIngredient;
import com.hellogroup.teamenu.domain.model.RecipeStep;
import com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class XiachufangRecipeConverter {

    @Resource
    private LlmRecipeExtractor llmRecipeExtractor;

    public Recipe convert(XiachufangRecipeDetailDto recipeDetail) {
        if (recipeDetail == null || CollectionUtils.isEmpty(recipeDetail.getContent())) {
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "下厨房返回的食谱内容为空");
        }

        XiachufangRecipeDetailDto.RecipeContent content = recipeDetail.getContent().get(0);

        List<RecipeIngredient> ingredients = convertIngredients(content.getMaterials());
        List<RecipeStep> steps = convertSteps(content.getSteps());
        List<String> imagePaths = buildImagePaths(content);

        RecipeCategory category = RecipeCategory.STAPLE;
        int completionTime = 30;

        // 结构化数据缺失时 fallback 到 LLM；同时用 LLM 识别分类和预估时间
        boolean needLlm = ingredients.isEmpty() || steps.isEmpty();
        if (needLlm) {
            String fullText = buildFullText(content);
            log.info("下厨房结构化数据不完整(食材={}个, 步骤={}个)，使用 LLM 补全", ingredients.size(), steps.size());
            LlmRecipeExtractor.ExtractResult llmResult = llmRecipeExtractor.extract(fullText);
            if (llmResult != null) {
                if (ingredients.isEmpty()) {
                    ingredients = llmResult.getIngredients();
                }
                if (steps.isEmpty()) {
                    steps = llmResult.getSteps();
                }
                completionTime = llmResult.getCompletionTime();
                try {
                    category = RecipeCategory.fromCode(llmResult.getCategoryCode());
                } catch (IllegalArgumentException e) {
                    log.warn("LLM 返回的分类码无效: {}", llmResult.getCategoryCode());
                }
            }
        }

        return Recipe.builder()
                .name(content.getName())
                .category(category)
                .source("下厨房")
                .needsPreparation(false)
                .imagePaths(imagePaths)
                .ingredients(ingredients)
                .steps(steps)
                .completionTime(completionTime)
                .build();
    }

    private String buildFullText(XiachufangRecipeDetailDto.RecipeContent content) {
        StringBuilder sb = new StringBuilder();
        sb.append("菜名：").append(content.getName()).append("\n");
        if (content.getTip() != null) {
            sb.append("小贴士：").append(content.getTip()).append("\n");
        }
        if (!CollectionUtils.isEmpty(content.getMaterials())) {
            sb.append("食材：");
            content.getMaterials().forEach(m -> sb.append(m.getName()).append(" ").append(m.getUnit()).append("、"));
            sb.append("\n");
        }
        if (!CollectionUtils.isEmpty(content.getSteps())) {
            content.getSteps().forEach(s -> sb.append("步骤").append(s.getStep()).append("：").append(s.getDesc()).append("\n"));
        }
        return sb.toString();
    }

    private List<RecipeIngredient> convertIngredients(List<XiachufangRecipeDetailDto.RecipeMaterial> materials) {
        if (CollectionUtils.isEmpty(materials)) {
            return Collections.emptyList();
        }
        List<RecipeIngredient> ingredients = new ArrayList<>(materials.size());
        for (int i = 0; i < materials.size(); i++) {
            XiachufangRecipeDetailDto.RecipeMaterial material = materials.get(i);
            ingredients.add(RecipeIngredient.builder()
                    .name(material.getName())
                    .quantity(material.getUnit())
                    .sortOrder(i + 1)
                    .build());
        }
        return ingredients;
    }

    private List<RecipeStep> convertSteps(List<XiachufangRecipeDetailDto.XiachufangRecipeStep> xiachufangSteps) {
        if (CollectionUtils.isEmpty(xiachufangSteps)) {
            return Collections.emptyList();
        }
        return xiachufangSteps.stream()
                .map(s -> RecipeStep.builder()
                        .stepNumber(s.getStep())
                        .description(s.getDesc())
                        .imagePath(s.getImg())
                        .build())
                .collect(Collectors.toList());
    }

    private List<String> buildImagePaths(XiachufangRecipeDetailDto.RecipeContent content) {
        List<String> images = new ArrayList<>();
        if (content.getCover() != null && !content.getCover().isEmpty()) {
            images.add(content.getCover());
        }
        if (!CollectionUtils.isEmpty(content.getSteps())) {
            for (XiachufangRecipeDetailDto.XiachufangRecipeStep step : content.getSteps()) {
                if (step.getImg() != null && !step.getImg().isEmpty()) {
                    images.add(step.getImg());
                }
            }
        }
        return images;
    }
}
