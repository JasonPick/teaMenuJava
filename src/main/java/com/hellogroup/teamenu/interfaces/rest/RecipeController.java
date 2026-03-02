package com.hellogroup.teamenu.interfaces.rest;

import com.hellogroup.teamenu.application.dto.ImportRecipeRequest;
import com.hellogroup.teamenu.application.dto.ImportTaskDTO;
import com.hellogroup.teamenu.application.dto.RecipeDTO;
import com.hellogroup.teamenu.application.service.RecipeApiService;
import com.hellogroup.teamenu.application.service.RecipeImportService;
import com.hellogroup.teamenu.common.constant.ResponseCode;
import com.hellogroup.teamenu.common.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 食谱REST接口
 * 
 * @author HelloGroup
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Resource
    private RecipeApiService recipeApiService;

    @Resource
    private RecipeImportService recipeImportService;
    
    /**
     * 创建食谱
     */
    @PostMapping
    public Result<RecipeDTO> createRecipe(@Valid @RequestBody RecipeDTO recipeDTO) {
        log.info("创建食谱, name={}", recipeDTO.getName());
        RecipeDTO result = recipeApiService.createRecipe(recipeDTO);
        return Result.success(result);
    }
    
    /**
     * 查询食谱详情
     */
    @GetMapping("/{id}")
    public Result<RecipeDTO> getRecipe(@PathVariable Long id) {
        log.info("查询食谱详情, id={}", id);
        RecipeDTO result = recipeApiService.getRecipeById(id);
        return Result.success(result);
    }
    
    /**
     * 分页查询所有食谱
     */
    @GetMapping
    public Result<List<RecipeDTO>> listRecipes(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("分页查询食谱, page={}, size={}", page, size);
        List<RecipeDTO> result = recipeApiService.listRecipes(page, size);
        return Result.success(result);
    }
    
    /**
     * 根据分类查询食谱
     */
    @GetMapping("/category/{categoryCode}")
    public Result<List<RecipeDTO>> listRecipesByCategory(
            @PathVariable String categoryCode,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("按分类查询食谱, categoryCode={}, page={}, size={}", categoryCode, page, size);
        List<RecipeDTO> result = recipeApiService.listRecipesByCategory(categoryCode, page, size);
        return Result.success(result);
    }
    
    /**
     * 搜索食谱
     */
    @GetMapping("/search")
    public Result<List<RecipeDTO>> searchRecipes(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("搜索食谱, keyword={}, page={}, size={}", keyword, page, size);
        List<RecipeDTO> result = recipeApiService.searchRecipes(keyword, page, size);
        return Result.success(result);
    }
    
    /**
     * 更新食谱
     */
    @PutMapping("/{id}")
    public Result<RecipeDTO> updateRecipe(
            @PathVariable Long id,
            @Valid @RequestBody RecipeDTO recipeDTO) {
        log.info("更新食谱, id={}", id);
        RecipeDTO result = recipeApiService.updateRecipe(id, recipeDTO);
        return Result.success(result);
    }
    
    /**
     * 删除食谱
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRecipe(@PathVariable Long id) {
        log.info("删除食谱, id={}", id);
        recipeApiService.deleteRecipe(id);
        return Result.success();
    }
    
    /**
     * 提交食谱导入任务（异步）
     * 立即返回 taskId，通过查询接口轮询导入结果
     */
    @PostMapping("/import")
    public Result<ImportTaskDTO> importRecipe(@Valid @RequestBody ImportRecipeRequest request) {
        log.info("提交导入食谱任务, platform={}, url={}", request.getPlatform(), request.getUrl());
        ImportTaskDTO task = recipeImportService.submitImportTask(request);
        return Result.success(task);
    }

    /**
     * 查询导入任务状态
     */
    @GetMapping("/import/tasks/{taskId}")
    public Result<ImportTaskDTO> getImportTaskStatus(@PathVariable String taskId) {
        ImportTaskDTO task = recipeImportService.getTaskStatus(taskId);
        if (task == null) {
            return Result.fail(ResponseCode.RESOURCE_NOT_FOUND, "任务不存在");
        }
        return Result.success(task);
    }
}
