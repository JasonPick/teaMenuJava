package com.hellogroup.teamenu.infrastructure.remote.xiaohongshu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小红书MCP客户端
 * 使用 Java MCP SDK 通过标准 MCP 协议调用小红书服务
 *
 * @author HelloGroup
 */
@Slf4j
@Component
public class XiaohongshuMcpClient {

    private final ObjectMapper objectMapper;
    private McpSyncClient mcpClient;

    @Value("${xiaohongshu.mcp.server.url:}")
    private String mcpServerUrl;

    @Value("${xiaohongshu.mcp.server.sse-endpoint:/sse}")
    private String sseEndpoint;

    public XiaohongshuMcpClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 初始化 MCP 客户端连接
     */
    @PostConstruct
    public void initialize() {
        try {
            JacksonMcpJsonMapper jsonMapper = new JacksonMcpJsonMapper(objectMapper);

            if (!StringUtils.hasText(mcpServerUrl)) {
                return;
            }
            log.info("初始化远程小红书 MCP 客户端, url={}", mcpServerUrl);

            // 解析 URL 获取 baseUri 和 sseEndpoint
            URI serverUri = URI.create(mcpServerUrl);
            String scheme = serverUri.getScheme() != null ? serverUri.getScheme() : "http";
            String host = serverUri.getHost() != null ? serverUri.getHost() : serverUri.getAuthority();
            int port = serverUri.getPort();

            // 构建 baseUri
            String baseUri;
            if (port != -1) {
                baseUri = String.format("%s://%s:%d", scheme, host, port);
            } else {
                baseUri = String.format("%s://%s", scheme, host);
            }

            // SSE 端点路径
            String endpoint = StringUtils.hasText(sseEndpoint) ? sseEndpoint : "/sse";

            log.info("MCP 服务器 baseUri={}, sseEndpoint={}", baseUri, endpoint);

            // 创建 HttpRequest.Builder
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream");

//            // 创建远程传输层 (SSE) - 使用完整构造函数
//            HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(baseUri)
//                    .sseEndpoint(endpoint)
//                    .jsonMapper(jsonMapper)
//                    .requestBuilder(requestBuilder)
//                    .build();
            // 替换原来的 HttpClientSseClientTransport
            HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(baseUri + endpoint)
                    .jsonMapper(jsonMapper)
                    .build();

            mcpClient = McpClient.sync(transport).build();

            // 初始化连接
            mcpClient.initialize();

            log.info("小红书 MCP 客户端初始化成功");

            // 列出可用工具
            listAvailableTools();

        } catch (Exception e) {
            log.error("初始化小红书 MCP 客户端失败", e);
            // 不抛出异常,允许应用继续启动
        }
    }

    /**
     * 列出可用的 MCP 工具
     */
    private void listAvailableTools() {
        try {
            if (mcpClient != null) {
                McpSchema.ListToolsResult tools = mcpClient.listTools();
                log.info("小红书 MCP 可用工具:");
                tools.tools().forEach(tool ->
                        log.info(" - {}: {}", tool.name(), tool.description())
                );
            }
        } catch (Exception e) {
            log.warn("列出 MCP 工具失败", e);
        }
    }

    /**
     * 关闭 MCP 客户端连接
     */
    @PreDestroy
    public void destroy() {
        try {
            if (mcpClient != null) {
                log.info("关闭小红书 MCP 客户端连接");
                mcpClient.closeGracefully();
            }
        } catch (Exception e) {
            log.error("关闭 MCP 客户端失败", e);
        }
    }

    /**
     * 搜索小红书内容
     *
     * @param keyword 搜索关键词
     * @return 搜索结果JSON
     */
    public JsonNode searchFeeds(String keyword) {
        try {
            if (mcpClient == null) {
                throw new IllegalStateException("MCP 客户端未初始化");
            }

            log.info("搜索小红书内容, keyword={}", keyword);

            // 构建工具调用参数
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("keyword", keyword);
            arguments.put("page", 1);
            arguments.put("page_size", 20);

            // 调用 MCP 工具
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("search_feeds", arguments);
            McpSchema.CallToolResult result = mcpClient.callTool(request);

            log.debug("搜索结果: {}", result.content());

            // 解析结果
            return parseToolResult(result);

        } catch (Exception e) {
            log.error("搜索小红书内容失败, keyword={}", keyword, e);
            throw new RuntimeException("搜索小红书内容失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取小红书笔记详情
     *
     * @param feedId    笔记ID
     * @param xsecToken 访问令牌(可选)
     * @return 笔记详情JSON
     */
    public JsonNode getFeedDetail(String feedId, String xsecToken) {
        try {
            if (mcpClient == null) {
                throw new IllegalStateException("MCP 客户端未初始化");
            }

            log.info("获取小红书笔记详情, feedId={}", feedId);

            // 构建工具调用参数
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("feed_id", feedId);
            if (xsecToken != null && !xsecToken.isEmpty()) {
                arguments.put("xsec_token", xsecToken);
            }
            arguments.put("load_all_comments", false);

            // 调用 MCP 工具
            McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("get_feed_detail", arguments);
            McpSchema.CallToolResult result = mcpClient.callTool(request);

            log.debug("笔记详情结果: {}", result.content());

            // 解析结果
            return parseToolResult(result);

        } catch (Exception e) {
            log.error("获取小红书笔记详情失败, feedId={}", feedId, e);
            throw new RuntimeException("获取小红书笔记详情失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析 MCP 工具调用结果
     *
     * @param result MCP 工具调用结果
     * @return JSON 节点
     */
    private JsonNode parseToolResult(McpSchema.CallToolResult result) {
        try {
            // MCP 工具返回的内容通常是文本或 JSON
            List<McpSchema.Content> content = result.content();

            if (CollectionUtils.isEmpty(content)) {
                throw new RuntimeException("MCP 工具返回空结果");
            }

            // 获取第一个内容项
            McpSchema.Content firstContent = content.get(0);
            // 检查内容类型
            if (firstContent instanceof McpSchema.TextContent) {
                McpSchema.TextContent textContent = (McpSchema.TextContent) firstContent;
                String text = textContent.text();
                // 尝试解析为 JSON
                return objectMapper.readTree(text);
            } else {
                // 其他类型的内容,转换为 JSON
                String jsonStr = objectMapper.writeValueAsString(firstContent);
                return objectMapper.readTree(jsonStr);
            }

        } catch (Exception e) {
            log.error("解析 MCP 工具结果失败", e);
            throw new RuntimeException("解析 MCP 工具结果失败: " + e.getMessage(), e);
        }
    }
}
