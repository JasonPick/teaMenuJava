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
    private XiachufangRemoteService xiachufangRemoteService;

    @Resource
    private XiachufangRecipeConverter xiachufangRecipeConverter;

    @Resource
    private FileStorageService fileStorageService;

    
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
     */
    private RecipeDTO importFromXiaohongshu(String text) {
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

        JsonNode feedDetail = xiaohongshuMcpClient.getFeedDetail(feedId, xsecToken);

        Recipe recipe = xiaohongshuRecipeParser.parse(feedDetail);

        recipe.setCreateTime(LocalDateTime.now());
        recipe.setUpdateTime(LocalDateTime.now());
        recipe.setLastAccessTime(LocalDateTime.now());
        recipe.setDeleted(false);

        Recipe savedRecipe = recipeRepository.save(recipe);
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
}
