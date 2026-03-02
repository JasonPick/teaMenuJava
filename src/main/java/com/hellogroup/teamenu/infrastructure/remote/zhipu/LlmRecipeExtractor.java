package com.hellogroup.teamenu.infrastructure.remote.zhipu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellogroup.teamenu.domain.model.RecipeIngredient;
import com.hellogroup.teamenu.domain.model.RecipeStep;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 基于 LLM 的食谱信息提取器
 * 从非结构化的食谱描述文本中提取食材和步骤
 */
@Slf4j
@Component
public class LlmRecipeExtractor {

    private static final String SYSTEM_PROMPT = """
            你是一个专业的食谱信息提取助手。用户会提供一段食谱相关的文字描述，请从中提取食材清单和制作步骤。

            请严格按照以下 JSON 格式返回，不要包含任何其他文字：
            {
              "ingredients": [
                {"name": "食材名称", "quantity": "用量，如文中未明确提及则填'适量'"}
              ],
              "steps": [
                {"stepNumber": 1, "description": "步骤描述"}
              ],
              "category": "分类，从以下选项中选一个最匹配的: staple(主食), dessert(甜品), bread(面包), protein(蛋白质)",
              "completionTime": 预估制作时间（分钟，整数）
            }

            注意：
            1. 只提取真正用于烹饪的食材，不要提取厨具、工具或与食谱无关的物品
            2. 如果文中有明确的用量就提取用量，没有就填"适量"
            3. 步骤要简洁清晰，按制作顺序排列
            4. 如果原文没有明确的步骤描述，根据上下文归纳出合理的步骤
            5. 去掉广告、话题标签等无关内容
            """;

    @Resource
    private ZhipuAiService zhipuAiService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 从文本中提取食材列表
     */
    public List<RecipeIngredient> extractIngredients(String content) {
        ExtractResult result = extract(content);
        return result != null ? result.getIngredients() : Collections.emptyList();
    }

    /**
     * 从文本中提取制作步骤
     */
    public List<RecipeStep> extractSteps(String content) {
        ExtractResult result = extract(content);
        return result != null ? result.getSteps() : Collections.emptyList();
    }

    /**
     * 一次性从文本中提取食材、步骤、分类和预估时间
     */
    public ExtractResult extract(String content) {
        if (!zhipuAiService.isAvailable()) {
            log.warn("智谱AI不可用，无法使用 LLM 提取食谱信息");
            return null;
        }

        try {
            String response = zhipuAiService.chatCompletion(SYSTEM_PROMPT, content);
            if (response == null || response.isBlank()) {
                return null;
            }
            return parseResponse(response);
        } catch (Exception e) {
            log.error("LLM 提取食谱信息失败", e);
            return null;
        }
    }

    private ExtractResult parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            ExtractResult result = new ExtractResult();

            // 解析食材
            List<RecipeIngredient> ingredients = new ArrayList<>();
            JsonNode ingredientsNode = root.path("ingredients");
            if (ingredientsNode.isArray()) {
                int order = 1;
                for (JsonNode item : ingredientsNode) {
                    String name = item.path("name").asText("").trim();
                    String quantity = item.path("quantity").asText("适量").trim();
                    if (!name.isEmpty()) {
                        ingredients.add(RecipeIngredient.builder()
                                .name(name)
                                .quantity(quantity)
                                .sortOrder(order++)
                                .build());
                    }
                }
            }
            result.setIngredients(ingredients);

            // 解析步骤
            List<RecipeStep> steps = new ArrayList<>();
            JsonNode stepsNode = root.path("steps");
            if (stepsNode.isArray()) {
                for (JsonNode item : stepsNode) {
                    int stepNumber = item.path("stepNumber").asInt(steps.size() + 1);
                    String description = item.path("description").asText("").trim();
                    if (!description.isEmpty()) {
                        steps.add(RecipeStep.builder()
                                .stepNumber(stepNumber)
                                .description(description)
                                .build());
                    }
                }
            }
            result.setSteps(steps);

            // 解析分类
            result.setCategoryCode(root.path("category").asText("staple"));

            // 解析预估时间
            result.setCompletionTime(root.path("completionTime").asInt(30));

            log.info("LLM 提取完成: {}种食材, {}个步骤, 分类={}, 预估时间={}分钟",
                    ingredients.size(), steps.size(), result.getCategoryCode(), result.getCompletionTime());

            return result;
        } catch (Exception e) {
            log.error("解析 LLM 返回结果失败, response={}", response, e);
            return null;
        }
    }

    @Data
    public static class ExtractResult {
        private List<RecipeIngredient> ingredients = Collections.emptyList();
        private List<RecipeStep> steps = Collections.emptyList();
        private String categoryCode = "staple";
        private int completionTime = 30;
    }
}
