package com.hellogroup.teamenu.infrastructure.config;

import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author:zhanglin196
 */
@Configuration
public class EmbeddingStoreConfig {

    @Value("${milvus.uri:https://in03-3d1890416f7e66d.serverless.aws-eu-central-1.cloud.zilliz.com}")
    private String milvusUri;

    @Value("${milvus.token:5a05ff6a6f1e2b06ceddc2558f5c72c03e047f3d2d694c0a9acd08b24c7215c22a6204754c7b143b1226c043a9f8615e856ec992}")
    private String milvusToken;


    @Bean
    public MilvusEmbeddingStore agentDemoMilvusEmbeddingStore(){
        return MilvusEmbeddingStore.builder().uri(milvusUri).token(milvusToken)
                                   .collectionName("agentDemo1").idFieldName("id").textFieldName("segment").vectorFieldName("segment_vector").build();
    }
}
