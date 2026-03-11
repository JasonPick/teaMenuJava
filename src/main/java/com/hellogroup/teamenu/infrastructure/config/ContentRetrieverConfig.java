package com.hellogroup.teamenu.infrastructure.config;

import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author:zhanglin196
 */
@Configuration
public class ContentRetrieverConfig {

    @Bean
    public ContentRetriever miluvsContentRetriever(MilvusEmbeddingStore milvusEmbeddingStore){
        return EmbeddingStoreContentRetriever.builder()
                                             .embeddingModel(new BgeSmallEnV15QuantizedEmbeddingModel())
                                             .embeddingStore(milvusEmbeddingStore).build();
    }
}
