package com.hellogroup.teamenu.infrastructure.remote.zhipu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellogroup.teamenu.common.constant.ResponseCode;
import com.hellogroup.teamenu.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 智谱AI大模型服务
 * 调用 GLM-4.7-flash 对话补全 API 进行食谱信息提取
 */
@Slf4j
@Service
public class ZhipuAiService {

    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    @Value("${zhipu.api.key:}")
    private String apiKey;

    @Value("${zhipu.api.model:glm-4.7-flash}")
    private String model;

    @Resource
    private ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    /**
     * 调用智谱AI对话补全接口，要求返回 JSON 格式
     *
     * @param systemPrompt 系统提示词
     * @param userContent  用户输入内容
     * @return 模型返回的文本内容
     */
    public String chatCompletion(String systemPrompt, String userContent) {
        if (!StringUtils.hasText(apiKey)) {
            log.warn("智谱AI API Key 未配置，跳过 LLM 调用");
            return null;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(new ChatRequest(
                    model,
                    new ChatRequest.Message[]{
                            new ChatRequest.Message("system", systemPrompt),
                            new ChatRequest.Message("user", userContent)
                    },
                    new ChatRequest.ResponseFormat("json_object"),
                    0.1f,
                    4096
            ));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            log.info("调用智谱AI, model={}", model);
            long start = System.currentTimeMillis();

            try (Response response = httpClient.newCall(request).execute()) {
                long elapsed = System.currentTimeMillis() - start;
                ResponseBody body = response.body();

                if (!response.isSuccessful() || body == null) {
                    String errorBody = body != null ? body.string() : "empty";
                    log.error("智谱AI请求失败, code={}, elapsed={}ms, body={}", response.code(), elapsed, errorBody);
                    throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR,
                            "智谱AI请求失败, HTTP " + response.code());
                }

                String responseStr = body.string();
                log.info("智谱AI响应成功, elapsed={}ms", elapsed);
                log.debug("智谱AI响应: {}", responseStr);

                JsonNode root = objectMapper.readTree(responseStr);
                JsonNode content = root.path("choices").path(0).path("message").path("content");

                if (content.isMissingNode()) {
                    log.error("智谱AI响应格式异常: {}", responseStr);
                    throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "智谱AI响应格式异常");
                }

                return content.asText();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用智谱AI异常", e);
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "调用智谱AI异常: " + e.getMessage());
        }
    }

    /**
     * 判断智谱AI是否可用（已配置 API Key）
     */
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class ChatRequest {
        private String model;
        private Message[] messages;
        private ResponseFormat response_format;
        private float temperature;
        private int max_tokens;

        @lombok.Data
        @lombok.AllArgsConstructor
        static class Message {
            private String role;
            private String content;
        }

        @lombok.Data
        @lombok.AllArgsConstructor
        static class ResponseFormat {
            private String type;
        }
    }
}
