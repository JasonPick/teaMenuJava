package com.hellogroup.teamenu.infrastructure.remote.ali;

import com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * VideoRecipeExtractor 单元测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Profile("test")
@DisplayName("视频食谱提取器测试")
class VideoRecipeExtractorTest {

    @Resource
    private AliAiService aliAiService;

    @Resource
    private VideoRecipeExtractor videoRecipeExtractor;

    @Test
    @DisplayName("成功提取视频食谱信息 - 完整数据")
    void extract_Success_WithCompleteData() {
        // Given
        String videoUrl = "https://sns-video-al.xhscdn.com/stream/1/110/259/01e985d76cb747b7010370039c32d2d95c_259.mp4";

        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNotNull();
        
        // 验证食材
        assertThat(result.getIngredients()).hasSize(3);
        assertThat(result.getIngredients().get(0).getName()).isEqualTo("土豆");
        assertThat(result.getIngredients().get(0).getQuantity()).isEqualTo("2个");
        assertThat(result.getIngredients().get(1).getName()).isEqualTo("辣椒粉");
        assertThat(result.getIngredients().get(1).getQuantity()).isEqualTo("适量");
        
        // 验证步骤
        assertThat(result.getSteps()).hasSize(3);
        assertThat(result.getSteps().get(0).getStepNumber()).isEqualTo(1);
        assertThat(result.getSteps().get(0).getDescription()).isEqualTo("土豆洗净切块");
        assertThat(result.getSteps().get(2).getStepNumber()).isEqualTo(3);
        
        // 验证分类和时间
        assertThat(result.getCategoryCode()).isEqualTo("dessert");
        assertThat(result.getCompletionTime()).isEqualTo(20);

        // 验证 Mock 调用
        verify(aliAiService, times(1)).isAvailable();
        verify(aliAiService, times(1)).videoUnderstanding(anyString(), eq(videoUrl));
    }

    @Test
    @DisplayName("成功提取 - 使用默认值（缺少部分字段）")
    void extract_Success_WithDefaultValues() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        String mockResponse = """
                {
                  "ingredients": [
                    {"name": "鸡蛋"}
                  ],
                  "steps": [
                    {"description": "打散鸡蛋"}
                  ]
                }
                """;

        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(mockResponse);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNotNull();
        
        // 验证默认值
        assertThat(result.getIngredients().get(0).getQuantity()).isEqualTo("适量");
        assertThat(result.getSteps().get(0).getStepNumber()).isEqualTo(1);
        assertThat(result.getCategoryCode()).isEqualTo("staple"); // 默认分类
        assertThat(result.getCompletionTime()).isEqualTo(30); // 默认时间
    }

    @Test
    @DisplayName("成功提取 - 空食材和步骤")
    void extract_Success_WithEmptyIngredientsAndSteps() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        String mockResponse = """
                {
                  "ingredients": [],
                  "steps": [],
                  "category": "bread",
                  "completionTime": 60
                }
                """;

        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(mockResponse);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIngredients()).isEmpty();
        assertThat(result.getSteps()).isEmpty();
        assertThat(result.getCategoryCode()).isEqualTo("bread");
        assertThat(result.getCompletionTime()).isEqualTo(60);
    }

    @Test
    @DisplayName("失败 - ALI AI 服务不可用")
    void extract_Fail_AliServiceNotAvailable() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        when(aliAiService.isAvailable()).thenReturn(false);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNull();
        verify(aliAiService, times(1)).isAvailable();
        verify(aliAiService, never()).videoUnderstanding(anyString(), anyString());
    }

    @Test
    @DisplayName("失败 - ALI AI 返回空响应")
    void extract_Fail_EmptyResponse() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn("");

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("失败 - ALI AI 返回 null")
    void extract_Fail_NullResponse() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(null);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("失败 - JSON 解析异常（格式错误）")
    void extract_Fail_InvalidJson() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        String invalidJson = "{ invalid json }";

        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(invalidJson);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("成功提取 - 过滤空名称的食材")
    void extract_Success_FilterEmptyIngredientNames() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        String mockResponse = """
                {
                  "ingredients": [
                    {"name": "土豆", "quantity": "2个"},
                    {"name": "", "quantity": "适量"},
                    {"name": "   ", "quantity": "1勺"}
                  ],
                  "steps": [
                    {"stepNumber": 1, "description": "开始制作"}
                  ],
                  "category": "staple",
                  "completionTime": 30
                }
                """;

        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(mockResponse);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNotNull();
        // 只有有效的食材被保留
        assertThat(result.getIngredients()).hasSize(1);
        assertThat(result.getIngredients().get(0).getName()).isEqualTo("土豆");
    }

    @Test
    @DisplayName("成功提取 - 过滤空描述的步骤")
    void extract_Success_FilterEmptyStepDescriptions() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        String mockResponse = """
                {
                  "ingredients": [
                    {"name": "鸡蛋", "quantity": "2个"}
                  ],
                  "steps": [
                    {"stepNumber": 1, "description": "打散鸡蛋"},
                    {"stepNumber": 2, "description": ""},
                    {"stepNumber": 3, "description": "   "}
                  ],
                  "category": "protein",
                  "completionTime": 15
                }
                """;

        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(mockResponse);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNotNull();
        // 只有有效的步骤被保留
        assertThat(result.getSteps()).hasSize(1);
        assertThat(result.getSteps().get(0).getDescription()).isEqualTo("打散鸡蛋");
    }

    @Test
    @DisplayName("成功提取 - 自动分配 sortOrder")
    void extract_Success_AutoAssignSortOrder() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        String mockResponse = """
                {
                  "ingredients": [
                    {"name": "食材1", "quantity": "适量"},
                    {"name": "食材2", "quantity": "适量"},
                    {"name": "食材3", "quantity": "适量"}
                  ],
                  "steps": [],
                  "category": "staple",
                  "completionTime": 30
                }
                """;

        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(mockResponse);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIngredients()).hasSize(3);
        
        // 验证 sortOrder 按顺序递增
        assertThat(result.getIngredients().get(0).getSortOrder()).isEqualTo(1);
        assertThat(result.getIngredients().get(1).getSortOrder()).isEqualTo(2);
        assertThat(result.getIngredients().get(2).getSortOrder()).isEqualTo(3);
    }

    @Test
    @DisplayName("失败 - ALI AI 抛出异常")
    void extract_Fail_AliServiceThrowsException() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl)))
                .thenThrow(new RuntimeException("API 调用失败"));

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("成功提取 - 步骤编号自动补全")
    void extract_Success_AutoAssignStepNumber() {
        // Given
        String videoUrl = "https://example.com/video.mp4";
        String mockResponse = """
                {
                  "ingredients": [],
                  "steps": [
                    {"description": "步骤描述1"},
                    {"stepNumber": 5, "description": "步骤描述2"},
                    {"description": "步骤描述3"}
                  ],
                  "category": "staple",
                  "completionTime": 30
                }
                """;

        when(aliAiService.isAvailable()).thenReturn(true);
        when(aliAiService.videoUnderstanding(anyString(), eq(videoUrl))).thenReturn(mockResponse);

        // When
        LlmRecipeExtractor.ExtractResult result = videoRecipeExtractor.extract(videoUrl);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSteps()).hasSize(3);
        
        // 第一个步骤：缺少编号，使用 steps.size() + 1 = 1
        assertThat(result.getSteps().get(0).getStepNumber()).isEqualTo(1);
        
        // 第二个步骤：有明确编号 5
        assertThat(result.getSteps().get(1).getStepNumber()).isEqualTo(5);
        
        // 第三个步骤：缺少编号，使用 steps.size() + 1 = 3
        assertThat(result.getSteps().get(2).getStepNumber()).isEqualTo(3);
    }
}
