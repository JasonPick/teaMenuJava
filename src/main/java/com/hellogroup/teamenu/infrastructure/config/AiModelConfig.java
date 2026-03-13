package com.hellogroup.teamenu.infrastructure.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfig {

    @Value("${ali.ai.video.api.key:}")
    public String aliAiVideoApiKey;

    @Value("${ali.ai.video.api.baseUrl:https://dashscope.aliyuncs.com/compatible-mode/v1}")
    public String aliAiVideoApiBaseUrl;

    @Value("${ali.ai.video.model:qwen3.5-plus}")
    public String aliAiVideoModel;

    @Value("${openai.api.key:}")
    public String openAiApiKey;

    @Bean
    public OpenAiChatModel aliAiVideoModel() {
        return OpenAiChatModel.builder()
                .baseUrl(aliAiVideoApiBaseUrl)
                .apiKey(aliAiVideoApiKey)
                .modelName(aliAiVideoModel)
                .logRequests(true)
                .logResponses(true)
                .build();
        
    }

    @Bean
    public OpenAiChatModel openAiChatModel() {
        return OpenAiChatModel.builder()
                              .baseUrl("http://langchain4j.dev/demo/openai/v1")
                              .apiKey("demo")
                              .logRequests(true)
                              .logResponses(true)
                              .modelName("gpt-4o-mini")
                              .build();
    }
}
