package com.hellogroup.teamenu.infrastructure.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 灵活的 Grade 反序列化器
 * 处理下厨房 API 返回的 grade 字段类型不一致问题：
 * - 当 grade 是字符串时（如 "8.8"），转换为单元素列表
 * - 当 grade 是数组时，正常解析为列表
 * 
 * @author HelloGroup
 */
public class FlexibleGradeDeserializer extends JsonDeserializer<List<String>> {
    
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        if (node.isArray()) {
            // 如果是数组，正常解析
            List<String> result = new ArrayList<>();
            node.forEach(item -> result.add(item.asText()));
            return result;
        } else if (node.isTextual()) {
            // 如果是字符串，包装成单元素列表
            return Collections.singletonList(node.asText());
        } else if (node.isNumber()) {
            // 如果是数字，转换为字符串后包装成列表
            return Collections.singletonList(node.asText());
        } else if (node.isNull()) {
            // 如果是 null，返回空列表
            return Collections.emptyList();
        } else {
            // 其他情况返回空列表
            return Collections.emptyList();
        }
    }
}
