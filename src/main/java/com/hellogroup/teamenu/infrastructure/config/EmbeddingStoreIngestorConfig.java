package com.hellogroup.teamenu.infrastructure.config;

import dev.langchain4j.model.embedding.onnx.bgesmallenv15q.BgeSmallEnV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author:zhanglin196
 */
@Configuration
public class EmbeddingStoreIngestorConfig {

    @Bean
    public EmbeddingStoreIngestor miluvsEmbeddingStoreIngestor(MilvusEmbeddingStore milvusEmbeddingStore){
        return EmbeddingStoreIngestor.builder()
                                     .embeddingModel(new BgeSmallEnV15QuantizedEmbeddingModel())
                                     .embeddingStore(milvusEmbeddingStore)
                                     .build();
    }
}
