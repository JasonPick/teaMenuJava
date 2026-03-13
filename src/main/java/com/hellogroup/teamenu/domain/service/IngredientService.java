package com.hellogroup.teamenu.domain.service;

import com.google.common.collect.Maps;
import com.hellogroup.teamenu.domain.model.IngredientCategory;
import com.hellogroup.teamenu.domain.model.RecipeCategory;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author:zhanglin196
 */
@Component
public class IngredientService {

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private ContentRetriever miluvsContentRetriever;

    @Resource
    private EmbeddingStoreIngestor miluvsEmbeddingStoreIngestor;

    public interface IngredientClassificationAssistant {

        @UserMessage("""
                     你是一个食材分类助手。根据用户提供的【分类标签】，对【食材列表】中的每个食材归入对应分类。
                     
                     ## 规则
                     
                     1. 分类只能使用用户提供的分类标签，不得自行创建新分类
                     2. 每个食材只归入一个分类
                     3. 无法归入任何分类的，value 填「其他」
                     
                     ## 重要分类指引
                     
                     **蔬菜水果类包括**：
                     - 所有蔬菜（如白菜、菠菜、芹菜、生菜、番茄、黄瓜、茄子、辣椒、胡萝卜等）
                     - 所有水果（如苹果、香蕉、橙子、葡萄、西瓜等）
                     - **所有菌菇类**（如香菇、蘑菇、金针菇、杏鲍菇、海鲜菇、木耳、银耳、平菇、草菇、茶树菇等）
                     - 豆类蔬菜（如豆芽、豌豆、四季豆、扁豆等）
                     - 根茎类（如土豆、红薯、山药、莲藕、芋头等）
                     
                     **肉禽蛋类包括**：
                     - 肉类（猪肉、牛肉、羊肉、兔肉等）
                     - 禽类（鸡肉、鸭肉、鹅肉等）
                     - 蛋类（鸡蛋、鸭蛋、鹌鹑蛋等）
                     - 海鲜（鱼、虾、蟹、贝类等）
                     
                     **熟食类包括**：
                     - 熟制肉类（卤肉、烧鸡、火腿等）
                     - 剩菜剩饭
                     - 已烹饪的食物
                     
                     **零食类包括**：
                     - 饼干、薯片、糖果等
                     - 坚果、果脯等
                     
                     **酱料类包括**：
                     - 调味酱（酱油、醋、蚝油、豆瓣酱等）
                     - 调味料（盐、糖、味精、鸡精、胡椒粉、五香粉等）
                     - 油类（食用油、香油等）
                     
                     ## 输入格式
                     
                     分类标签：[标签1]、[标签2]、[标签3]...
                     食材列表：[食材1]、[食材2]、[食材3]...
                     
                     ## 输出格式
                     
                     以 JSON 格式返回，key 为食材，value 为分类：
                     
                     {
                       "食材1": "分类X",
                       "食材2": "分类Y"
                     }
                     
                     ## 示例
                     
                     用户输入：
                     分类标签：蔬菜水果、肉禽蛋、酱料
                     食材列表：海鲜菇、香菇、金针菇、猪肉、鸡精、白菜
                     
                     你的输出：
                     
                     {
                       "海鲜菇": "蔬菜水果",
                       "香菇": "蔬菜水果",
                       "金针菇": "蔬菜水果",
                       "猪肉": "肉禽蛋",
                       "鸡精": "酱料",
                       "白菜": "蔬菜水果"
                     }
                     
                     食材列表：{{ingredient}}
                     分类标签：{{classification}}
                """)
        @Agent
        Map<String, String> ingredientUnderstanding(@V("ingredient") String ingredient, @V("classification") String classification);
    }

    private IngredientClassificationAssistant assistant;

    @PostConstruct
    public void init() {
        assistant = AgenticServices.agentBuilder(IngredientClassificationAssistant.class)
                .chatModel(openAiChatModel)
                .outputKey("result")
                .build();
    }

    public Map<String, String> ingredientClassification(List<String> ingredientList) {
        if (CollectionUtils.isEmpty(ingredientList)) {
            return Maps.newHashMap();
        }

        Map<String, String> result = Maps.newHashMap();

//        List<String> notFoundIngredientList = Lists.newArrayList();
//        for (String ingredient : ingredientList) {
//            List<Content> retrieve = miluvsContentRetriever.retrieve(Query.from(ingredient));
//            if (CollectionUtils.isEmpty(retrieve)) {
//                notFoundIngredientList.add(ingredient);
//                continue;
//            }
//            retrieve.sort(Comparator.comparingDouble(o -> (Double) o.metadata().get(ContentMetadata.SCORE)));
//            Content content = retrieve.get(retrieve.size() - 1);
//
//            //TODO
//            String category = content.textSegment().metadata().getString("category");
//            result.put(ingredient, category);
//        }
//        if (CollectionUtils.isEmpty(notFoundIngredientList)) {
//            return result;
//        }

        String ingredient = String.join("、", ingredientList);
//        String classification = Arrays.stream(IngredientCategory.values()).map(IngredientCategory::getDisplayName).collect(Collectors.joining("、"));
        String classification = String.join("、", IngredientCategory.names());
        Map<String, String> aiResult = assistant.ingredientUnderstanding(ingredient, classification);
        if (MapUtils.isEmpty(aiResult)) {
            return result;
        }

        // 直接返回 AI 的结果（displayName），不需要转换
        result.putAll(aiResult);
        return result;
    }
}
