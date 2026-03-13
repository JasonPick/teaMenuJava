package com.hellogroup.teamenu.infrastructure.remote.ali;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellogroup.teamenu.domain.model.RecipeIngredient;
import com.hellogroup.teamenu.domain.model.RecipeStep;
import com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ImageRecipeExtractor {

    private static final String SYSTEM_PROMPT = """
            你是一个专业的食谱信息提取助手。用户会提供一系列和食谱相关的图片链接，请从中提取食材清单和制作步骤。

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
    private AliAiService aliAiService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 从图片列表中提取食材、步骤、分类和预估时间
     */
    public LlmRecipeExtractor.ExtractResult extract(List<String> imageUrls) {
        if (!aliAiService.isAvailable()) {
            log.warn("ALI AI不可用，无法使用 LLM 提取食谱信息");
            return null;
        }

        if (imageUrls == null || imageUrls.isEmpty()) {
            log.warn("图片URL列表为空，无法提取食谱信息");
            return null;
        }

        try {
            log.info("开始从{}张图片中提取食谱信息", imageUrls.size());
            String response = aliAiService.imageUnderstanding(SYSTEM_PROMPT, imageUrls);
            if (response == null || response.isBlank()) {
                log.warn("图片理解返回空结果");
                return null;
            }
            return parseResponse(response);
        } catch (Exception e) {
            log.error("图片理解提取食谱信息失败", e);
            return null;
        }
    }

    private LlmRecipeExtractor.ExtractResult parseResponse(String response) {
        try {
            // 清理 Markdown 代码块标记（```json 和 ```）
            String cleanedResponse = response.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7); // 移除 ```json
            } else if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3); // 移除 ```
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3); // 移除末尾 ```
            }
            cleanedResponse = cleanedResponse.trim();
            
            log.debug("清理后的响应: {}", cleanedResponse);
            
            JsonNode root = objectMapper.readTree(cleanedResponse);

            LlmRecipeExtractor.ExtractResult result = new LlmRecipeExtractor.ExtractResult();

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
