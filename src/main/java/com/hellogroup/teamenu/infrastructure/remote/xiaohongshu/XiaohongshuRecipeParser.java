package com.hellogroup.teamenu.infrastructure.remote.xiaohongshu;

import cn.hutool.core.collection.ListUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.hellogroup.teamenu.domain.model.Recipe;
import com.hellogroup.teamenu.domain.model.RecipeCategory;
import com.hellogroup.teamenu.domain.model.RecipeIngredient;
import com.hellogroup.teamenu.domain.model.RecipeStep;
import com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小红书食谱解析器
 * 从小红书笔记内容中提取食谱信息
 * 优先使用正则提取，失败后 fallback 到 LLM（智谱 GLM-4.7-flash）
 *
 * @author HelloGroup
 */
@Slf4j
@Component
public class XiaohongshuRecipeParser {

    @Resource
    private LlmRecipeExtractor llmRecipeExtractor;

    /**
     * 解析小红书笔记为食谱
     *
     * @param feedDetail 笔记详情JSON（新 API 格式，根节点即包含所有数据）
     * @return 食谱对象
     */
    public Recipe parse(JsonNode feedDetail) {
        try {
            Recipe.RecipeBuilder builder = Recipe.builder();
            // 新 API 格式不需要提取 data.note，根节点即包含数据

            String title = extractTitle(feedDetail);
            builder.name(title);

            List<String> images = extractImages(feedDetail);
            builder.imagePaths(images);

            String content = extractContent(feedDetail);

//            // 先用正则尝试提取
//            List<RecipeIngredient> ingredients = parseIngredients(content);
//            List<RecipeStep> steps = parseSteps(content);
//            Integer completionTime = extractCompletionTime(content);
//            RecipeCategory category = RecipeCategory.STAPLE;
//
//            // 正则提取不到食材时，fallback 到 LLM
//            boolean needLlm = ingredients.isEmpty() || steps.isEmpty();
//            if (needLlm) {
//                log.info("正则提取不充分(食材={}个, 步骤={}个)，使用 LLM 提取", ingredients.size(), steps.size());
            List<RecipeIngredient> ingredients = ListUtil.empty();
            List<RecipeStep> steps = ListUtil.empty();
            Integer completionTime = 30;
            RecipeCategory category = RecipeCategory.STAPLE;

            LlmRecipeExtractor.ExtractResult llmResult = llmRecipeExtractor.extract(content);
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
//            }

            builder.ingredients(ingredients);
            builder.steps(steps);
            builder.completionTime(completionTime);
            builder.category(category);

            String authorName = extractAuthorName(feedDetail);
            builder.source("小红书 - " + authorName);
            builder.needsPreparation(false);

            return builder.build();
        } catch (Exception e) {
            log.error("解析小红书食谱失败", e);
            throw new RuntimeException("解析小红书食谱失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取标题
     * 新 API 格式: text.title 或 text.desc 第一行
     */
    private String extractTitle(JsonNode feedDetail) {
        // 先尝试 text.title
        JsonNode textNode = feedDetail.path("text");
        if (!textNode.isMissingNode()) {
            JsonNode titleNode = textNode.path("title");
            if (!titleNode.isMissingNode() && !titleNode.asText().isEmpty()) {
                return titleNode.asText();
            }
            
            // 如果 title 为空，取 desc 的第一行
            JsonNode descNode = textNode.path("desc");
            if (!descNode.isMissingNode()) {
                String desc = descNode.asText();
                String[] lines = desc.split("\n");
                if (lines.length > 0 && !lines[0].trim().isEmpty()) {
                    return lines[0].trim();
                }
            }
        }

        return "未命名食谱";
    }

    /**
     * 提取图片
     * 新 API 格式: images 数组，每项包含 url, width, height, live_photo 等字段
     */
    private List<String> extractImages(JsonNode feedDetail) {
        List<String> images = new ArrayList<>();

        // 新 API 格式: images 数组
        JsonNode imagesNode = feedDetail.path("images");
        if (imagesNode.isArray()) {
            for (JsonNode imageNode : imagesNode) {
                // 新格式字段是 "url" 而不是 "urlDefault"
                String url = imageNode.path("url").asText();
                if (url != null && !url.isEmpty()) {
                    // 将HTTP转换为HTTPS以支持iOS的ATS
                    if (url.startsWith("http://")) {
                        url = url.replace("http://", "https://");
                    }
                    images.add(url);
                }
            }
        }

        return images;
    }

    /**
     * 提取内容
     * 新 API 格式: text.title 和 text.desc
     */
    private String extractContent(JsonNode feedDetail) {
        JsonNode textNode = feedDetail.path("text");
        if (!textNode.isMissingNode()) {
            String title = textNode.path("title").asText("");
            String desc = textNode.path("desc").asText("");
            
            // 组合 title 和 desc
            if (!title.isEmpty() && !desc.isEmpty()) {
                return title + "\n\n" + desc;
            } else if (!desc.isEmpty()) {
                return desc;
            } else if (!title.isEmpty()) {
                return title;
            }
        }

        return "";
    }

    /**
     * 解析食材
     * 识别常见的食材列表格式
     */
    private List<RecipeIngredient> parseIngredients(String content) {
        List<RecipeIngredient> ingredients = new ArrayList<>();

        // 查找食材部分
        Pattern sectionPattern = Pattern.compile("(?:食材|材料|用料)[：:](.*?)(?=(?:步骤|做法|制作方法)|$)", Pattern.DOTALL);
        Matcher sectionMatcher = sectionPattern.matcher(content);

        if (sectionMatcher.find()) {
            String ingredientSection = sectionMatcher.group(1);

            // 解析每一行食材
            String[] lines = ingredientSection.split("\n");
            int order = 1;
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // 移除列表符号
                line = line.replaceAll("^[•·\\-\\d+\\.]+\\s*", "");

                // 尝试分离食材名称和数量
                String[] parts = line.split("\\s+", 2);
                if (parts.length >= 1) {
                    String name = parts[0];
                    String quantity = parts.length > 1 ? parts[1] : "适量";

                    ingredients.add(RecipeIngredient.builder()
                            .name(name)
                            .quantity(quantity)
                            .sortOrder(order++)
                            .build());
                }
            }
        }

        // 如果没有找到明确的食材部分，尝试从整个内容中提取
        if (ingredients.isEmpty()) {
            // 简化处理：提取可能的食材关键词
            Pattern ingredientPattern = Pattern.compile("([\\u4e00-\\u9fa5]{2,4})\\s*(\\d+[克毫升个勺杯斤两g ml]+)");
            Matcher matcher = ingredientPattern.matcher(content);

            int order = 1;
            while (matcher.find()) {
                String name = matcher.group(1);
                String quantity = matcher.group(2);

                ingredients.add(RecipeIngredient.builder()
                        .name(name)
                        .quantity(quantity)
                        .sortOrder(order++)
                        .build());
            }
        }

        return ingredients;
    }

    /**
     * 解析步骤
     */
    private List<RecipeStep> parseSteps(String content) {
        List<RecipeStep> steps = new ArrayList<>();

        // 查找步骤部分
        Pattern sectionPattern = Pattern.compile("(?:步骤|做法|制作方法)[：:](.*?)$", Pattern.DOTALL);
        Matcher sectionMatcher = sectionPattern.matcher(content);

        String stepSection = content;
        if (sectionMatcher.find()) {
            stepSection = sectionMatcher.group(1);
        }

        // 解析每一步
        String[] lines = stepSection.split("\n");
        int stepNumber = 1;
        StringBuilder currentStep = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // 检查是否是新步骤的开始
            if (line.matches("^[\\d+\\.、）\\)]+.*") || line.startsWith("步骤")) {
                // 保存上一步
                if (currentStep.length() > 0) {
                    steps.add(RecipeStep.builder()
                            .stepNumber(stepNumber++)
                            .description(currentStep.toString().trim())
                            .build());
                    currentStep = new StringBuilder();
                }

                // 移除步骤编号
                line = line.replaceAll("^[\\d+\\.、）\\)步骤]+\\s*", "");
            }

            if (!line.isEmpty()) {
                if (currentStep.length() > 0) {
                    currentStep.append(" ");
                }
                currentStep.append(line);
            }
        }

        // 保存最后一步
        if (currentStep.length() > 0) {
            steps.add(RecipeStep.builder()
                    .stepNumber(stepNumber)
                    .description(currentStep.toString().trim())
                    .build());
        }

        return steps;
    }

    /**
     * 提取完成时间
     */
    private Integer extractCompletionTime(String content) {
        // 尝试从内容中提取时间信息
        Pattern timePattern = Pattern.compile("(\\d+)\\s*(?:分钟|分|min)");
        Matcher matcher = timePattern.matcher(content);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("解析完成时间失败", e);
            }
        }

        // 默认30分钟
        return 30;
    }

    /**
     * 提取作者名称
     * 新 API 格式: user.nickname
     */
    private String extractAuthorName(JsonNode feedDetail) {
        JsonNode userNode = feedDetail.path("user").path("nickname");
        if (!userNode.isMissingNode()) {
            return userNode.asText();
        }

        return "未知作者";
    }
}
