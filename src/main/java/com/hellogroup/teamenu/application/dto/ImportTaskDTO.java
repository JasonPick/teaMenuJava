package com.hellogroup.teamenu.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导入任务 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportTaskDTO implements Serializable {

    private String taskId;

    /**
     * PENDING / PROCESSING / COMPLETED / FAILED
     */
    private String status;

    private String message;

    /**
     * 导入完成后的食谱数据
     */
    private RecipeDTO recipe;

    private LocalDateTime createTime;

    private LocalDateTime finishTime;
}
