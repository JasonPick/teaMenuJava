package com.hellogroup.teamenu.infrastructure.remote.xiachufang;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellogroup.teamenu.application.dto.XiachufangPageSearchResp;
import com.hellogroup.teamenu.application.dto.XiachufangRecipeDetailDto;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 下厨房api调用
 */
@Service
@Slf4j
public class XiachufangRemoteService {

    @Value("${xiachufang.api.url:http://39.105.19.237:5000}")
    private String apiUrl;

    @Resource
    private ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 搜索食谱
     * http://0.0.0.0:5000/search/鸡蛋&page=1
     * @param keyword 关键字
     * @param page 分页参数
     * @return 搜索结果
     */
    public XiachufangPageSearchResp searchRecipes(String keyword, int page) {
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String url = String.format("%s/search/%s&page=%d", apiUrl, encodedKeyword, page);

        log.info("搜索下厨房食谱, keyword={}, page={}, url={}", keyword, page, url);

        String responseBody = executeGet(url);
        try {
            return objectMapper.readValue(responseBody, XiachufangPageSearchResp.class);
        } catch (Exception e) {
            log.error("解析下厨房搜索结果失败, response={}", responseBody, e);
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "解析下厨房搜索结果失败");
        }
    }

    /**
     * 获取食谱详情
     * http://0.0.0.0:5000/recipe/recipeId
     * @param recipeId 食谱id
     * @return 食谱详情
     */
    public XiachufangRecipeDetailDto getRecipeDetail(String recipeId) {
        String url = String.format("%s/recipe/%s/", apiUrl, recipeId);

        log.info("获取下厨房食谱详情, recipeId={}, url={}", recipeId, url);

        String responseBody = executeGet(url);
        try {
            return objectMapper.readValue(responseBody, XiachufangRecipeDetailDto.class);
        } catch (Exception e) {
            log.error("解析下厨房食谱详情失败, response={}", responseBody, e);
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "解析下厨房食谱详情失败");
        }
    }

    private String executeGet(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                log.error("下厨房API请求失败, url={}, code={}", url, response.code());
                throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR,
                        "下厨房API请求失败, HTTP " + response.code());
            }
            return body.string();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用下厨房API异常, url={}", url, e);
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR,
                    "调用下厨房API异常: " + e.getMessage());
        }
    }
}
