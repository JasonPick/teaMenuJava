package com.hellogroup.teamenu.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellogroup.teamenu.domain.model.Recipe;
import com.hellogroup.teamenu.domain.model.RecipeCategory;
import com.hellogroup.teamenu.domain.model.RecipeIngredient;
import com.hellogroup.teamenu.domain.model.RecipeStep;
import com.hellogroup.teamenu.domain.repository.RecipeRepository;
import com.hellogroup.teamenu.infrastructure.persistence.entity.RecipeEntity;
import com.hellogroup.teamenu.infrastructure.persistence.entity.RecipeIngredientEntity;
import com.hellogroup.teamenu.infrastructure.persistence.entity.RecipeStepEntity;
import com.hellogroup.teamenu.infrastructure.persistence.mapper.RecipeIngredientMapper;
import com.hellogroup.teamenu.infrastructure.persistence.mapper.RecipeMapper;
import com.hellogroup.teamenu.infrastructure.persistence.mapper.RecipeStepMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 食谱仓储实现
 * 
 * @author HelloGroup
 */
@Slf4j
@Repository
public class RecipeRepositoryImpl implements RecipeRepository {
    
    private final RecipeMapper recipeMapper;
    private final RecipeIngredientMapper ingredientMapper;
    private final RecipeStepMapper stepMapper;
    private final ObjectMapper objectMapper;
    
    public RecipeRepositoryImpl(RecipeMapper recipeMapper,
                                RecipeIngredientMapper ingredientMapper,
                                RecipeStepMapper stepMapper,
                                ObjectMapper objectMapper) {
        this.recipeMapper = recipeMapper;
        this.ingredientMapper = ingredientMapper;
        this.stepMapper = stepMapper;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Recipe save(Recipe recipe) {
        try {
            RecipeEntity entity = toEntity(recipe);
            entity.setCreateTime(LocalDateTime.now());
            entity.setUpdateTime(LocalDateTime.now());
            entity.setDeleted(false);
            
            recipeMapper.insert(entity);
            recipe.setId(entity.getId());
            
            // 保存食材
            if (recipe.getIngredients() != null) {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    RecipeIngredientEntity ingredientEntity = toIngredientEntity(ingredient);
                    ingredientEntity.setRecipeId(entity.getId());
                    ingredientMapper.insert(ingredientEntity);
                    ingredient.setId(ingredientEntity.getId());
                }
            }
            
            // 保存步骤
            if (recipe.getSteps() != null) {
                for (RecipeStep step : recipe.getSteps()) {
                    RecipeStepEntity stepEntity = toStepEntity(step);
                    stepEntity.setRecipeId(entity.getId());
                    stepMapper.insert(stepEntity);
                    step.setId(stepEntity.getId());
                }
            }
            
            return recipe;
        } catch (Exception e) {
            log.error("保存食谱失败", e);
            throw new RuntimeException("保存食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Recipe> findById(Long id) {
        try {
            RecipeEntity entity = recipeMapper.selectById(id);
            if (entity == null) {
                return Optional.empty();
            }
            
            Recipe recipe = toDomain(entity);
            loadIngredientsAndSteps(recipe);
            return Optional.of(recipe);
        } catch (Exception e) {
            log.error("查询食谱失败, id={}", id, e);
            throw new RuntimeException("查询食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Recipe> findAll(int page, int size) {
        try {
            Page<RecipeEntity> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<RecipeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(RecipeEntity::getLastAccessTime);
            
            Page<RecipeEntity> result = recipeMapper.selectPage(pageParam, wrapper);
            return result.getRecords().stream()
                    .map(entity -> {
                        Recipe recipe = toDomain(entity);
                        loadIngredientsAndSteps(recipe);
                        return recipe;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("分页查询食谱失败", e);
            throw new RuntimeException("分页查询食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Recipe> findByCategory(RecipeCategory category, int page, int size) {
        try {
            Page<RecipeEntity> pageParam = new Page<>(page, size);
            LambdaQueryWrapper<RecipeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecipeEntity::getCategoryCode, category.getCode())
                   .orderByDesc(RecipeEntity::getLastAccessTime);
            
            Page<RecipeEntity> result = recipeMapper.selectPage(pageParam, wrapper);
            return result.getRecords().stream()
                    .map(entity -> {
                        Recipe recipe = toDomain(entity);
                        loadIngredientsAndSteps(recipe);
                        return recipe;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("按分类查询食谱失败", e);
            throw new RuntimeException("按分类查询食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Recipe> search(String keyword, int page, int size) {
        try {
            int offset = (page - 1) * size;
            List<RecipeEntity> entities = recipeMapper.searchRecipes(keyword, offset, size);
            
            return entities.stream()
                    .map(entity -> {
                        Recipe recipe = toDomain(entity);
                        loadIngredientsAndSteps(recipe);
                        return recipe;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索食谱失败, keyword={}", keyword, e);
            throw new RuntimeException("搜索食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Recipe> findByIds(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<RecipeEntity> entities = recipeMapper.selectBatchIds(ids);
            return entities.stream()
                    .map(entity -> {
                        Recipe recipe = toDomain(entity);
                        loadIngredientsAndSteps(recipe);
                        return recipe;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("批量查询食谱失败", e);
            throw new RuntimeException("批量查询食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Recipe update(Recipe recipe) {
        try {
            RecipeEntity entity = toEntity(recipe);
            entity.setUpdateTime(LocalDateTime.now());
            recipeMapper.updateById(entity);
            
            // 删除旧的食材和步骤
            LambdaQueryWrapper<RecipeIngredientEntity> ingredientWrapper = new LambdaQueryWrapper<>();
            ingredientWrapper.eq(RecipeIngredientEntity::getRecipeId, recipe.getId());
            ingredientMapper.delete(ingredientWrapper);
            
            LambdaQueryWrapper<RecipeStepEntity> stepWrapper = new LambdaQueryWrapper<>();
            stepWrapper.eq(RecipeStepEntity::getRecipeId, recipe.getId());
            stepMapper.delete(stepWrapper);
            
            // 保存新的食材和步骤
            if (recipe.getIngredients() != null) {
                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    RecipeIngredientEntity ingredientEntity = toIngredientEntity(ingredient);
                    ingredientEntity.setRecipeId(recipe.getId());
                    ingredientMapper.insert(ingredientEntity);
                }
            }
            
            if (recipe.getSteps() != null) {
                for (RecipeStep step : recipe.getSteps()) {
                    RecipeStepEntity stepEntity = toStepEntity(step);
                    stepEntity.setRecipeId(recipe.getId());
                    stepMapper.insert(stepEntity);
                }
            }
            
            return recipe;
        } catch (Exception e) {
            log.error("更新食谱失败", e);
            throw new RuntimeException("更新食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteById(Long id) {
        try {
            recipeMapper.deleteById(id);
        } catch (Exception e) {
            log.error("删除食谱失败, id={}", id, e);
            throw new RuntimeException("删除食谱失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long count() {
        try {
            return recipeMapper.selectCount(null);
        } catch (Exception e) {
            log.error("统计食谱总数失败", e);
            throw new RuntimeException("统计食谱总数失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public long countByCategory(RecipeCategory category) {
        try {
            LambdaQueryWrapper<RecipeEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RecipeEntity::getCategoryCode, category.getCode());
            return recipeMapper.selectCount(wrapper);
        } catch (Exception e) {
            log.error("统计分类食谱数量失败", e);
            throw new RuntimeException("统计分类食谱数量失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 加载食材和步骤
     */
    private void loadIngredientsAndSteps(Recipe recipe) {
        // 加载食材
        LambdaQueryWrapper<RecipeIngredientEntity> ingredientWrapper = new LambdaQueryWrapper<>();
        ingredientWrapper.eq(RecipeIngredientEntity::getRecipeId, recipe.getId())
                         .orderByAsc(RecipeIngredientEntity::getSortOrder);
        List<RecipeIngredientEntity> ingredientEntities = ingredientMapper.selectList(ingredientWrapper);
        recipe.setIngredients(ingredientEntities.stream()
                .map(this::toIngredientDomain)
                .collect(Collectors.toList()));
        
        // 加载步骤
        LambdaQueryWrapper<RecipeStepEntity> stepWrapper = new LambdaQueryWrapper<>();
        stepWrapper.eq(RecipeStepEntity::getRecipeId, recipe.getId())
                   .orderByAsc(RecipeStepEntity::getStepNumber);
        List<RecipeStepEntity> stepEntities = stepMapper.selectList(stepWrapper);
        recipe.setSteps(stepEntities.stream()
                .map(this::toStepDomain)
                .collect(Collectors.toList()));
    }
    
    /**
     * 实体转领域模型
     */
    private Recipe toDomain(RecipeEntity entity) {
        List<String> imagePaths = new ArrayList<>();
        try {
            if (entity.getImagePaths() != null && !entity.getImagePaths().isEmpty()) {
                imagePaths = objectMapper.readValue(entity.getImagePaths(), new TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            log.warn("解析图片路径失败", e);
        }
        
        return Recipe.builder()
                .id(entity.getId())
                .name(entity.getName())
                .category(RecipeCategory.fromCode(entity.getCategoryCode()))
                .completionTime(entity.getCompletionTime())
                .source(entity.getSource())
                .needsPreparation(entity.getNeedsPreparation())
                .imagePaths(imagePaths)
                .lastAccessTime(entity.getLastAccessTime())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .deleted(entity.getDeleted())
                .build();
    }
    
    /**
     * 领域模型转实体
     */
    private RecipeEntity toEntity(Recipe recipe) {
        RecipeEntity entity = new RecipeEntity();
        entity.setId(recipe.getId());
        entity.setName(recipe.getName());
        entity.setCategoryCode(recipe.getCategory().getCode());
        entity.setCompletionTime(recipe.getCompletionTime());
        entity.setSource(recipe.getSource());
        entity.setNeedsPreparation(recipe.getNeedsPreparation());
        entity.setLastAccessTime(recipe.getLastAccessTime());
        
        try {
            if (recipe.getImagePaths() != null) {
                entity.setImagePaths(objectMapper.writeValueAsString(recipe.getImagePaths()));
            }
        } catch (Exception e) {
            log.error("序列化图片路径失败", e);
        }
        
        return entity;
    }
    
    private RecipeIngredient toIngredientDomain(RecipeIngredientEntity entity) {
        return RecipeIngredient.builder()
                .id(entity.getId())
                .name(entity.getName())
                .quantity(entity.getQuantity())
                .sortOrder(entity.getSortOrder())
                .build();
    }
    
    private RecipeIngredientEntity toIngredientEntity(RecipeIngredient ingredient) {
        RecipeIngredientEntity entity = new RecipeIngredientEntity();
        entity.setId(ingredient.getId());
        entity.setName(ingredient.getName());
        entity.setQuantity(ingredient.getQuantity());
        entity.setSortOrder(ingredient.getSortOrder());
        return entity;
    }
    
    private RecipeStep toStepDomain(RecipeStepEntity entity) {
        return RecipeStep.builder()
                .id(entity.getId())
                .stepNumber(entity.getStepNumber())
                .description(entity.getDescription())
                .imagePath(entity.getImagePath())
                .build();
    }
    
    private RecipeStepEntity toStepEntity(RecipeStep step) {
        RecipeStepEntity entity = new RecipeStepEntity();
        entity.setId(step.getId());
        entity.setStepNumber(step.getStepNumber());
        entity.setDescription(step.getDescription());
        entity.setImagePath(step.getImagePath());
        return entity;
    }
}
