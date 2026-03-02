package com.hellogroup.teamenu.infrastructure.remote.xiaohongshu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 小红书模拟客户端
 * 用于测试环境,返回模拟数据
 * 
 * @author HelloGroup
 */
@Slf4j
@Component
@Profile("mock")
public class XiaohongshuMockClient extends XiaohongshuMcpClient {
    
    private final ObjectMapper objectMapper;
    
    public XiaohongshuMockClient(ObjectMapper objectMapper) {
        super(objectMapper);
        this.objectMapper = objectMapper;
    }
    
    @Override
    public JsonNode searchFeeds(String keyword) {
        log.warn("使用模拟数据进行搜索: {}", keyword);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("success", true);
        
        ObjectNode data = objectMapper.createObjectNode();
        ArrayNode items = objectMapper.createArrayNode();
        
        // 模拟搜索结果
        ObjectNode item = objectMapper.createObjectNode();
        item.put("id", "mock_note_123456");
        item.put("title", keyword + "的做法");
        item.put("desc", "超简单的" + keyword + "教程");
        item.put("type", "normal");
        
        items.add(item);
        data.set("items", items);
        result.set("data", data);
        
        return result;
    }
    
    @Override
    public JsonNode getFeedDetail(String feedId, String xsecToken) {
        log.warn("使用模拟数据获取笔记详情: {}", feedId);
        
        ObjectNode result = objectMapper.createObjectNode();
        result.put("success", true);
        
        ObjectNode data = objectMapper.createObjectNode();
        ObjectNode noteCard = objectMapper.createObjectNode();
        
        // 模拟笔记详情
        noteCard.put("note_id", feedId);
        noteCard.put("title", "红烧肉的做法");
        noteCard.put("desc", "家常红烧肉,肥而不腻,入口即化");
        noteCard.put("type", "normal");
        
        // 模拟作者信息
        ObjectNode user = objectMapper.createObjectNode();
        user.put("nickname", "美食达人");
        user.put("user_id", "mock_user_123");
        noteCard.set("user", user);
        
        // 模拟图片列表
        ArrayNode imageList = objectMapper.createArrayNode();
        ObjectNode image = objectMapper.createObjectNode();
        ObjectNode infoList = objectMapper.createObjectNode();
        infoList.put("url", "https://example.com/image1.jpg");
        image.set("info_list", objectMapper.createArrayNode().add(infoList));
        imageList.add(image);
        noteCard.set("image_list", imageList);
        
        // 模拟标签
        ArrayNode tagList = objectMapper.createArrayNode();
        ObjectNode tag = objectMapper.createObjectNode();
        tag.put("name", "美食");
        tag.put("type", "topic");
        tagList.add(tag);
        noteCard.set("tag_list", tagList);
        
        data.set("note_card", noteCard);
        result.set("data", data);
        
        return result;
    }
}
