package com.hellogroup.teamenu.infrastructure.remote.ali;

import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
@Service
public class AliAiService {

    @Resource
    private OpenAiChatModel aliAiVideoModel;
    @Value("${ali.ai.video.api.key:}")
    private String apiKey;


    public String videoUnderstanding(String userPrompt, String videoUrl) {
        UserMessage userMessage = UserMessage.from(TextContent.from(userPrompt), VideoContent.from(videoUrl));
        ChatResponse resp = aliAiVideoModel.chat(userMessage);
        return resp.aiMessage().text();
    }
    /**
     * 判断智谱AI是否可用（已配置 API Key）
     */
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }

}
