package com.hellogroup.teamenu.infrastructure.remote.ali;

import com.google.common.collect.Lists;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

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

    public String imageUnderstanding(String userPrompt, String imageUrl) {
        if(StringUtils.isBlank(imageUrl)){
            return null;
        }
        return imageUnderstanding(userPrompt, Lists.newArrayList(imageUrl));
    }

    public String imageUnderstanding(String userPrompt, List<String> imageUrlList) {
        if(CollectionUtils.isEmpty(imageUrlList)){
            return null;
        }

        List<Content> contentList = Lists.newArrayList(TextContent.from(userPrompt));
        List<ImageContent> imageContentList = imageUrlList.stream().map(ImageContent::from).toList();
        contentList.addAll(imageContentList);

        UserMessage userMessage = UserMessage.from(contentList);
        ChatResponse resp = aliAiVideoModel.chat(userMessage);

        return resp.aiMessage().text();
    }

    /**
     * 判断智谱AI是否可用（已配置 API Key）
     */
    public boolean isAvailable() {
        return StringUtils.isNotBlank(apiKey);
    }

}
