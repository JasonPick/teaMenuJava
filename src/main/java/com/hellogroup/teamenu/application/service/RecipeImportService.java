package com.hellogroup.teamenu.application.service;

import com.hellogroup.teamenu.application.dto.ImportRecipeRequest;
import com.hellogroup.teamenu.application.dto.ImportTaskDTO;
import com.hellogroup.teamenu.application.dto.RecipeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 食谱异步导入服务
 * 提交导入任务后立即返回 taskId，后台异步执行，客户端轮询查询结果
 */
@Slf4j
@Service
public class RecipeImportService {

    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";

    private final Map<String, ImportTaskDTO> taskStore = new ConcurrentHashMap<>();

    @Resource
    private RecipeApiService recipeApiService;

    /**
     * 提交导入任务，立即返回 taskId
     */
    public ImportTaskDTO submitImportTask(ImportRecipeRequest request) {
        String taskId = UUID.randomUUID().toString().replace("-", "");

        ImportTaskDTO task = ImportTaskDTO.builder()
                .taskId(taskId)
                .status(STATUS_PROCESSING)
                .message("正在导入食谱...")
                .createTime(LocalDateTime.now())
                .build();

        taskStore.put(taskId, task);

        executeImportAsync(taskId, request);

        return task;
    }

    /**
     * 查询导入任务状态
     */
    public ImportTaskDTO getTaskStatus(String taskId) {
        return taskStore.get(taskId);
    }

    /**
     * 异步执行导入
     */
    @Async("importTaskExecutor")
    public void executeImportAsync(String taskId, ImportRecipeRequest request) {
        ImportTaskDTO task = taskStore.get(taskId);
        if (task == null) {
            return;
        }

        try {
            log.info("开始异步导入食谱, taskId={}, platform={}", taskId, request.getPlatform());

            RecipeDTO result = recipeApiService.importRecipe(request);

            task.setStatus(STATUS_COMPLETED);
            task.setMessage("导入成功");
            task.setRecipe(result);
            task.setFinishTime(LocalDateTime.now());

            log.info("异步导入食谱完成, taskId={}, recipeName={}", taskId, result.getName());
        } catch (Exception e) {
            log.error("异步导入食谱失败, taskId={}", taskId, e);
            task.setStatus(STATUS_FAILED);
            task.setMessage("导入失败: " + e.getMessage());
            task.setFinishTime(LocalDateTime.now());
        }
    }
}
