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
                     3. 无法归入任何分类的，value 填「未分类」

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
                     分类标签：蔬菜、主食、肉禽蛋
                     食材列表：土豆、米饭、猪肉、白菜、鸡蛋、馒头

                     你的输出：

                     {
                       "土豆": "主食",
                       "米饭": "主食",
                       "猪肉": "肉禽蛋",
                       "白菜": "蔬菜",
                       "鸡蛋": "肉禽蛋",
                       "馒头": "主食"
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
        Map<String, String> aiResultWithCode = new HashMap<>();
        aiResult.forEach(
                (k, v) -> aiResult.put(k, IngredientCategory.getByDisplayName(v).getCode())
        );

//        List<Document> documentList = aiResult.entrySet()
//                .stream()
//                .map(o -> {
//                    String v = IngredientCategory.getByDisplayName(o.getValue()).getCode();
//                    String k = o.getKey();
//                    return Document.from(k, Metadata.from("category", v));
//                })
//                .toList();
//        miluvsEmbeddingStoreIngestor.ingest(documentList);
        result.putAll(aiResultWithCode);
        return result;
    }
}
