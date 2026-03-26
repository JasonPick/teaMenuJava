package com.hellogroup.teamenu.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hellogroup.teamenu.application.dto.*;
import com.hellogroup.teamenu.common.constant.ResponseCode;
import com.hellogroup.teamenu.common.exception.BusinessException;
import com.hellogroup.teamenu.domain.model.*;
import com.hellogroup.teamenu.domain.repository.InventoryRepository;
import com.hellogroup.teamenu.domain.repository.RecipeRepository;
import com.hellogroup.teamenu.infrastructure.remote.xiachufang.XiachufangRecipeConverter;
import com.hellogroup.teamenu.infrastructure.remote.xiachufang.XiachufangRemoteService;
import com.hellogroup.teamenu.infrastructure.remote.xiaohongshu.XiaohongshuMcpClient;
import com.hellogroup.teamenu.infrastructure.remote.xiaohongshu.XiaohongshuRecipeParser;
import com.hellogroup.teamenu.infrastructure.remote.xiaohongshu.XiaohongshuRemoteService;
import com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 食谱应用服务
 * 
 * @author HelloGroup
 */
@Slf4j
@Service
public class RecipeApiService {

    @Resource
    private RecipeRepository recipeRepository;

    @Resource
    private InventoryRepository inventoryRepository;

    @Resource
    private XiaohongshuMcpClient xiaohongshuMcpClient;

    @Resource
    private XiaohongshuRecipeParser xiaohongshuRecipeParser;
    
    @Resource
    private XiaohongshuRemoteService xiaohongshuRemoteService;

    @Resource
    private XiachufangRemoteService xiachufangRemoteService;

    @Resource
    private XiachufangRecipeConverter xiachufangRecipeConverter;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private com.hellogroup.teamenu.infrastructure.remote.ali.VideoRecipeExtractor videoRecipeExtractor;

    @Resource
    private com.hellogroup.teamenu.infrastructure.remote.ali.ImageRecipeExtractor imageRecipeExtractor;

    @Resource
    private com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor llmRecipeExtractor;

    
    /**
     * 创建食谱
     */
    @Transactional(rollbackFor = Exception.class)
    public RecipeDTO createRecipe(RecipeDTO recipeDTO) {
        try {
            Recipe recipe = toRecipe(recipeDTO);
            recipe.setCreateTime(LocalDateTime.now());
            recipe.setUpdateTime(LocalDateTime.now());
            recipe.setLastAccessTime(LocalDateTime.now());
            recipe.setDeleted(false);
            
            Recipe savedRecipe = recipeRepository.save(recipe);
            return toRecipeDTO(savedRecipe);
        } catch (Exception e) {
            log.error("创建食谱失败", e);
            throw new BusinessException(ResponseCode.BUSINESS_ERROR, "创建食谱失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询食谱详情
     */
    public RecipeDTO getRecipeById(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND, "食谱不存在"));
        
        // 更新最后访问时间
        recipe.updateLastAccessTime();
        recipeRepository.update(recipe);
        
        return toRecipeDTO(recipe);
    }
    
    /**
     * 分页查询所有食谱
     */
    public List<RecipeDTO> listRecipes(int page, int size) {
        List<Recipe> recipes = recipeRepository.findAll(page, size);
        return recipes.stream()
                .map(this::toRecipeDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据分类查询食谱
     */
    public List<RecipeDTO> listRecipesByCategory(String categoryCode, int page, int size) {
        RecipeCategory category = RecipeCategory.fromCode(categoryCode);
        List<Recipe> recipes = recipeRepository.findByCategory(category, page, size);
        return recipes.stream()
                .map(this::toRecipeDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 搜索食谱
     */
    public List<RecipeDTO> searchRecipes(String keyword, int page, int size) {
        List<Recipe> recipes = recipeRepository.search(keyword, page, size);
        return recipes.stream()
                .map(this::toRecipeDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新食谱
     */
    @Transactional(rollbackFor = Exception.class)
    public RecipeDTO updateRecipe(Long id, RecipeDTO recipeDTO) {
        Recipe existingRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND, "食谱不存在"));
        
        Recipe recipe = toRecipe(recipeDTO);
        recipe.setId(id);
        recipe.setCreateTime(existingRecipe.getCreateTime());
        recipe.setUpdateTime(LocalDateTime.now());
        
        Recipe updatedRecipe = recipeRepository.update(recipe);
        return toRecipeDTO(updatedRecipe);
    }
    
    /**
     * 删除食谱
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }
    
    /**
     * 从外部链接导入食谱
     */
    public RecipeDTO importRecipe(ImportRecipeRequest request) {
        try {
            if ("xiaohongshu".equals(request.getPlatform())) {
                return importFromXiaohongshu(request.getUrl());
            } else if ("xiachufang".equals(request.getPlatform())) {
                return importFromXicChufang(request.getUrl());
            } else {
                throw new BusinessException(ResponseCode.PARAM_ERROR, "不支持的平台类型");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("导入食谱失败", e);
            throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, "导入食谱失败: " + e.getMessage());
        }
    }

    /**
     * 从下厨房导入食谱
     * @param url url
     * @return
     */
    private RecipeDTO importFromXicChufang(String url) {
        //下厨房的url是 https://www.xiachufang.com/recipe/xxxxx/，需要提取recipeId
        String recipeId = extractRecipeIdFromUrl(url);
        if (recipeId == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR, "无效的下厨房链接");
        }
        // 调用下厨房api
        XiachufangRecipeDetailDto recipeDetail = xiachufangRemoteService.getRecipeDetail(recipeId);
        // 解析并保存食谱
        Recipe recipe = xiachufangRecipeConverter.convert(recipeDetail);
        // 4. 保存食谱
        recipe.setCreateTime(LocalDateTime.now());
        recipe.setUpdateTime(LocalDateTime.now());
        recipe.setLastAccessTime(LocalDateTime.now());
        recipe.setDeleted(false);

        Recipe savedRecipe = recipeRepository.save(recipe);
        return toRecipeDTO(savedRecipe);
    }

    private String extractRecipeIdFromUrl(String url) {
        // 下厨房链接格式：https://www.xiachufang.com/recipe/104534972/
        Pattern pattern = Pattern.compile("/recipe/(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static final Pattern EXPLORE_PATTERN = Pattern.compile("xiaohongshu\\.com/(?:explore|discovery/item)/([a-f0-9]+)");
    private static final Pattern XHS_LINK_PATTERN = Pattern.compile("https?://xhslink\\.com/[\\w/]+");
    private static final Pattern XSEC_TOKEN_PATTERN = Pattern.compile("[?&]xsec_token=([^&]+)");

    /**
     * 从小红书导入食谱
     * 优先使用新的 HTTP API (xiaohongshu-api)，失败后 fallback 到 MCP
     * 根据笔记类型(视频/图文/纯文字)智能选择对应的 AI 提取器
     */
    public RecipeDTO importFromXiaohongshu(String text) {
        String resolvedUrl = resolveXiaohongshuUrl(text);
        if (resolvedUrl == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR, "无效的小红书链接");
        }

        String feedId = extractFeedId(resolvedUrl);
        if (feedId == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR, "无法从链接中提取笔记ID");
        }

        String xsecToken = extractXsecToken(resolvedUrl);
        log.info("提取小红书参数, feedId={}, xsecToken={}", feedId, xsecToken);

        // 步骤1: 获取笔记数据 (优先 API,失败则 MCP)
        JsonNode feedDetail = null;
        try {
            feedDetail = xiaohongshuRemoteService.getFeedDetail(feedId, xsecToken);
            log.info("使用 xiaohongshu-api 服务获取笔记详情成功");
        } catch (Exception e) {
            log.warn("xiaohongshu-api 服务调用失败，尝试 fallback 到 MCP: {}", e.getMessage());
            try {
                feedDetail = xiaohongshuMcpClient.getFeedDetail(feedId, xsecToken);
                log.info("使用 MCP 方式获取笔记详情成功");
            } catch (Exception mcpError) {
                log.error("MCP 方式也失败", mcpError);
                throw new BusinessException(ResponseCode.EXTERNAL_SERVICE_ERROR, 
                    "获取小红书笔记失败，API 和 MCP 方式均不可用");
            }
        }

        // 步骤2: 根据笔记类型选择 AI Extractor
        LlmRecipeExtractor.ExtractResult extractResult = null;
        
        JsonNode videoNode = feedDetail.path("video");
        JsonNode imagesNode = feedDetail.path("images");
        JsonNode textNode = feedDetail.path("text");
        
        String noteType = ""; // 用于日志
        
        // 判断优先级: video > images > text
        if (videoNode != null && !videoNode.isNull() && videoNode.has("url")) {
            // 视频笔记 - 使用阿里云视频理解
            String videoUrl = videoNode.path("url").asText("");
            if (!videoUrl.isEmpty()) {
                noteType = "视频笔记";
                log.info("检测到视频笔记, 使用 VideoRecipeExtractor, videoUrl={}", videoUrl);
                try {
                    extractResult = videoRecipeExtractor.extract(videoUrl);
                    if (extractResult != null) {
                        log.info("视频笔记提取成功: {}种食材, {}个步骤", 
                                extractResult.getIngredients().size(), 
                                extractResult.getSteps().size());
                    }
                } catch (Exception e) {
                    log.error("视频提取失败, 将尝试降级方案", e);
                }
            }
        } else if (imagesNode != null && imagesNode.isArray() && imagesNode.size() > 0) {
            // 图文笔记 - 使用阿里云图像理解
            List<String> imageUrls = extractImageUrls(imagesNode);
            if (!imageUrls.isEmpty()) {
                noteType = "图文笔记";
                log.info("检测到图文笔记, 使用 ImageRecipeExtractor, 图片数量={}", imageUrls.size());
                try {
                    extractResult = imageRecipeExtractor.extract(imageUrls);
                    if (extractResult != null) {
                        log.info("图文笔记提取成功: {}种食材, {}个步骤", 
                                extractResult.getIngredients().size(), 
                                extractResult.getSteps().size());
                    }
                } catch (Exception e) {
                    log.error("图文提取失败, 将尝试降级方案", e);
                }
            }
        } else {
            // 纯文字笔记 - 使用智谱 AI 文本理解
            String textContent = textNode.path("desc").asText("");
            if (!textContent.isEmpty()) {
                noteType = "纯文字笔记";
                log.info("检测到纯文字笔记, 使用 LlmRecipeExtractor");
                try {
                    extractResult = llmRecipeExtractor.extract(textContent);
                    if (extractResult != null) {
                        log.info("纯文字笔记提取成功: {}种食材, {}个步骤", 
                                extractResult.getIngredients().size(), 
                                extractResult.getSteps().size());
                    }
                } catch (Exception e) {
                    log.error("文本提取失败, 将尝试降级方案", e);
                }
            }
        }

        // 步骤3: 构建 Recipe 对象
        Recipe recipe;
        if (extractResult != null && !extractResult.getIngredients().isEmpty()) {
            // AI 提取成功,使用提取结果
            log.info("使用 AI 提取结果构建食谱, 类型={}", noteType);
            recipe = buildRecipeFromExtractResult(feedDetail, extractResult);
        } else {
            // AI 提取失败或为空,使用降级方案
            log.warn("AI 提取失败或结果为空, 使用降级方案保存基本信息, 类型={}", noteType);
            recipe = buildBasicRecipe(feedDetail);
            
            log.error("⚠️ 小红书笔记 AI 提取失败需要人工关注! feedId={}, noteType={}", feedId, noteType);
        }

        // 步骤4: 保存食谱
        recipe.setCreateTime(LocalDateTime.now());
        recipe.setUpdateTime(LocalDateTime.now());
        recipe.setLastAccessTime(LocalDateTime.now());
        recipe.setDeleted(false);

        Recipe savedRecipe = recipeRepository.save(recipe);
        log.info("小红书食谱导入成功, recipeId={}, feedId={}, noteType={}", 
                savedRecipe.getId(), feedId, noteType);
        
        return toRecipeDTO(savedRecipe);
    }

    /**
     * 将分享文本或URL解析为完整的 xiaohongshu.com 长链接
     * 支持：
     * 1. 直接包含 xiaohongshu.com 链接的文本
     * 2. 包含 xhslink.com 短链接的分享文本（自动跟随重定向）
     */
    private String resolveXiaohongshuUrl(String text) {
        Matcher exploreMatcher = EXPLORE_PATTERN.matcher(text);
        if (exploreMatcher.find()) {
            return text;
        }

        Matcher xhsLinkMatcher = XHS_LINK_PATTERN.matcher(text);
        if (xhsLinkMatcher.find()) {
            String shortUrl = xhsLinkMatcher.group();
            String redirectedUrl = resolveRedirect(shortUrl);
            if (redirectedUrl != null) {
                return redirectedUrl;
            }
        }

        return null;
    }

    private String extractFeedId(String url) {
        Matcher matcher = EXPLORE_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractXsecToken(String url) {
        Matcher matcher = XSEC_TOKEN_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * 跟随短链接重定向，获取最终 URL（不自动跟随，只读取 Location 头）
     */
    private String resolveRedirect(String shortUrl) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(shortUrl).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int code = conn.getResponseCode();
            if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == 307 || code == 308) {
                String location = conn.getHeaderField("Location");
                log.info("小红书短链接重定向: {} -> {}", shortUrl, location);
                return location;
            }
            log.warn("小红书短链接未返回重定向, code={}", code);
        } catch (Exception e) {
            log.error("解析小红书短链接失败, url={}", shortUrl, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }
    
    /**
     * 领域模型转DTO
     */
    private RecipeDTO toRecipeDTO(Recipe recipe) {
        // 检查食材是否齐全
        List<InventoryIngredient> inventories = inventoryRepository.fetchInventory(null);
        java.time.LocalDate today = java.time.LocalDate.now();
        List<String> availableIngredients = inventories.stream()
                .filter(inv -> {
                    // 检查是否过期
                    return inv.getExpiryDate() == null || !inv.getExpiryDate().isBefore(today);
                })
                .map(InventoryIngredient::getName)
                .collect(Collectors.toList());
        
        boolean hasAllIngredients = recipe.hasAllIngredients(availableIngredients);
        
        return RecipeDTO.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .categoryCode(recipe.getCategory().getCode())
                .completionTime(recipe.getCompletionTime())
                .source(recipe.getSource())
                .needsPreparation(recipe.getNeedsPreparation())
                .imagePaths(recipe.getImagePaths())
                .ingredients(recipe.getIngredients() == null ? null : 
                        recipe.getIngredients().stream()
                                .map(this::toIngredientDTO)
                                .collect(Collectors.toList()))
                .steps(recipe.getSteps() == null ? null :
                        recipe.getSteps().stream()
                                .map(this::toStepDTO)
                                .collect(Collectors.toList()))
                .hasAllIngredients(hasAllIngredients)
                .lastAccessTime(recipe.getLastAccessTime())
                .createTime(recipe.getCreateTime())
                .build();
    }
    
    /**
     * DTO转领域模型
     */
    private Recipe toRecipe(RecipeDTO dto) {
        // 处理图片：如果有imagesData，保存并获取URL；否则使用imagePaths
        List<String> finalImagePaths;
        try {
            if (dto.getImagesData() != null && !dto.getImagesData().isEmpty()) {
                // 有Base64图片数据，保存并获取URL
                log.info("检测到Base64图片数据, 数量={}", dto.getImagesData().size());
                finalImagePaths = fileStorageService.saveBase64Images(dto.getImagesData());
                log.info("保存Base64图片成功, 生成URL数量={}", finalImagePaths.size());
            } else {
                // 使用现有的imagePaths
                finalImagePaths = dto.getImagePaths();
                log.info("使用现有imagePaths, 数量={}", finalImagePaths != null ? finalImagePaths.size() : 0);
            }
        } catch (Exception e) {
            log.error("处理图片失败", e);
            throw new BusinessException(ResponseCode.BUSINESS_ERROR, "处理图片失败: " + e.getMessage());
        }
        
        return Recipe.builder()
                .id(dto.getId())
                .name(dto.getName())
                .category(RecipeCategory.fromCode(dto.getCategoryCode()))
                .completionTime(dto.getCompletionTime())
                .source(dto.getSource())
                .needsPreparation(dto.getNeedsPreparation())
                .imagePaths(finalImagePaths)
                .ingredients(dto.getIngredients() == null ? null :
                        dto.getIngredients().stream()
                                .map(this::toIngredient)
                                .collect(Collectors.toList()))
                .steps(dto.getSteps() == null ? null :
                        dto.getSteps().stream()
                                .map(this::toStep)
                                .collect(Collectors.toList()))
                .build();
    }
    
    private RecipeIngredientDTO toIngredientDTO(RecipeIngredient ingredient) {
        return RecipeIngredientDTO.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .sortOrder(ingredient.getSortOrder())
                .build();
    }
    
    private RecipeIngredient toIngredient(RecipeIngredientDTO dto) {
        return RecipeIngredient.builder()
                .id(dto.getId())
                .name(dto.getName())
                .quantity(dto.getQuantity())
                .sortOrder(dto.getSortOrder())
                .build();
    }
    
    private RecipeStepDTO toStepDTO(RecipeStep step) {
        return RecipeStepDTO.builder()
                .id(step.getId())
                .stepNumber(step.getStepNumber())
                .description(step.getDescription())
                .imagePath(step.getImagePath())
                .build();
    }
    
    private RecipeStep toStep(RecipeStepDTO dto) {
        return RecipeStep.builder()
                .id(dto.getId())
                .stepNumber(dto.getStepNumber())
                .description(dto.getDescription())
                .imagePath(dto.getImagePath())
                .build();
    }

    /**
     * 从 JsonNode 中提取图片 URL 列表
     */
    private List<String> extractImageUrls(JsonNode imagesNode) {
        List<String> urls = new java.util.ArrayList<>();
        if (imagesNode != null && imagesNode.isArray()) {
            for (JsonNode imgNode : imagesNode) {
                String url = imgNode.path("url").asText("");
                if (!url.isEmpty()) {
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    /**
     * 从 AI 提取结果和笔记详情构建 Recipe 对象
     */
    private Recipe buildRecipeFromExtractResult(JsonNode feedDetail, 
                                                com.hellogroup.teamenu.infrastructure.remote.zhipu.LlmRecipeExtractor.ExtractResult extractResult) {
        JsonNode textNode = feedDetail.path("text");
        JsonNode userNode = feedDetail.path("user");
        
        String title = textNode.path("title").asText("未命名食谱");
        String desc = textNode.path("desc").asText("");
        String author = userNode.path("nickname").asText("小红书用户");
        
        // 提取图片URLs
        List<String> imagePaths = extractImageUrls(feedDetail.path("images"));
        
        // 如果有视频,也添加视频封面
        JsonNode videoNode = feedDetail.path("video");
        if (videoNode != null && !videoNode.isNull()) {
            String videoUrl = videoNode.path("url").asText("");
            if (!videoUrl.isEmpty() && !imagePaths.contains(videoUrl)) {
                // 视频URL记录到source中
                log.info("笔记包含视频: {}", videoUrl);
            }
        }
        
        return Recipe.builder()
                .name(title)
                .category(RecipeCategory.fromCode(extractResult.getCategoryCode()))
                .completionTime(extractResult.getCompletionTime())
                .source("小红书-" + author)
                .needsPreparation(false)
                .imagePaths(imagePaths)
                .ingredients(extractResult.getIngredients())
                .steps(extractResult.getSteps())
                .build();
    }

    /**
     * 构建基础食谱信息(AI提取失败时的降级方案)
     */
    private Recipe buildBasicRecipe(JsonNode feedDetail) {
        JsonNode textNode = feedDetail.path("text");
        JsonNode userNode = feedDetail.path("user");
        
        String title = textNode.path("title").asText("未命名食谱");
        String desc = textNode.path("desc").asText("");
        String author = userNode.path("nickname").asText("小红书用户");
        
        List<String> imagePaths = extractImageUrls(feedDetail.path("images"));
        
        // 创建一个简单的步骤,包含笔记描述
        List<RecipeStep> steps = new java.util.ArrayList<>();
        if (!desc.isEmpty()) {
            steps.add(RecipeStep.builder()
                    .stepNumber(1)
                    .description(desc)
                    .build());
        }
        
        return Recipe.builder()
                .name(title)
                .category(RecipeCategory.STAPLE) // 默认分类
                .completionTime(30) // 默认30分钟
                .source("小红书-" + author)
                .needsPreparation(false)
                .imagePaths(imagePaths)
                .ingredients(new java.util.ArrayList<>()) // 空食材列表
                .steps(steps)
                .build();
    }
}
