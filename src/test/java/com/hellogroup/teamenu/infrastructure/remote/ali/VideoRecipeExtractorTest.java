package com.hellogroup.teamenu.infrastructure.remote.ali;

import com.google.common.collect.Lists;
import com.hellogroup.teamenu.application.service.RecipeApiService;
import com.hellogroup.teamenu.domain.service.IngredientService;
import com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;

import java.util.Collections;

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

    @Resource
    private IngredientService ingredientService;
    @Autowired
    private RecipeApiService recipeApiService;

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
    @DisplayName("食材分类")
    void ingredient_category() {
        ingredientService.ingredientClassification(Lists.newArrayList("西葫芦", "沙茶酱", "红薯", "豌豆", "豆角", "辣椒粉", "葱姜蒜"));
    }

    @Test
    void import_xiaohongshu(){
        String url = "酸辣番茄鸡！汤汁浓郁鲜嫩入味！真的巨好吃 想告诉全... http://xhslink.com/o/6lkxbalP5bY\n" +
                "复制后打开【小红书】查看笔记！";
        recipeApiService.importFromXiaohongshu(url);
    }


}
