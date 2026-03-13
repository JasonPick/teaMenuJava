package com.hellogroup.teamenu.infrastructure.remote.xiaohongshu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellogroup.teamenu.common.constant.ResponseCode;
import com.hellogroup.teamenu.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 小红书 API 调用服务
 * 调用部署在远程的 xiaohongshu-api 服务
 * API 地址: http://39.105.19.237:8901/feed/{feedId}?xsec_token={token}
 *
 * @author HelloGroup
 */
@Service
@Slf4j
public class XiaohongshuRemoteService {

    @Value("${xiaohongshu.api.url:}")
    private String apiUrl;

    @Resource
    private ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 获取小红书笔记详情
     * 
     * @param feedId    笔记 ID (例如: 69aab960000000001a035b59)
     * @param xsecToken xsec_token（可选，用于访问需要权限的笔记）
     * @return 笔记详情 JSON，包含 note_id, type, text, user, images, video 等字段
     */
    public JsonNode getFeedDetail(String feedId, String xsecToken) {
        // 构建 URL
        String url = String.format("%s/feed/%s", apiUrl, feedId);
        if (xsecToken != null && !xsecToken.isEmpty()) {
            url += "?xsec_token=" + xsecToken;
        }

        log.info("获取小红书笔记详情, feedId={}, url={}", feedId, url);

        String responseBody = executeGet(url);
        try {
            JsonNode result = objectMapper.readTree(responseBody);
            
            // 验证返回数据的基本结构
            if (result.has("note_id") && result.has("type")) {
                log.info("成功获取小红书笔记详情, note_id={}, type={}", 
                    result.path("note_id").asText(), 
                    result.path("type").asText());
                return result;
            } else {
                log.error("小红书 API 返回数据格式异常, response={}", responseBody);
                throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "小红书 API 返回数据格式异常");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析小红书笔记详情失败, response={}", responseBody, e);
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "解析小红书笔记详情失败: " + e.getMessage());
        }
    }

    /**
     * 执行 GET 请求
     */
    private String executeGet(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                log.error("小红书 API 请求失败, url={}, code={}", url, response.code());
                throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR,
                        "小红书 API 请求失败, HTTP " + response.code());
            }
            return body.string();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用小红书 API 异常, url={}", url, e);
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR,
                    "调用小红书 API 异常: " + e.getMessage());
        }
    }
}
