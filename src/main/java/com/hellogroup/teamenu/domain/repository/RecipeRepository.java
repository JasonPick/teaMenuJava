package com.hellogroup.teamenu.domain.repository;

import com.hellogroup.teamenu.domain.model.Recipe;
import com.hellogroup.teamenu.domain.model.RecipeCategory;

import java.util.List;
import java.util.Optional;

/**
 * 食谱仓储接口
 * 
 * @author HelloGroup
 */
public interface RecipeRepository {
    
    /**
     * 保存食谱
     * 
     * @param recipe 食谱
     * @return 保存后的食谱
     */
    Recipe save(Recipe recipe);
    
    /**
     * 根据ID查询食谱
     * 
     * @param id 食谱ID
     * @return 食谱
     */
    Optional<Recipe> findById(Long id);
    
    /**
     * 分页查询所有食谱
     * 
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 食谱列表
     */
    List<Recipe> findAll(int page, int size);
    
    /**
     * 根据分类分页查询食谱
     * 
     * @param category 分类
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 食谱列表
     */
    List<Recipe> findByCategory(RecipeCategory category, int page, int size);
    
    /**
     * 搜索食谱（按名称或食材）
     * 
     * @param keyword 关键词
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 食谱列表
     */
    List<Recipe> search(String keyword, int page, int size);
    
    /**
     * 根据ID列表批量查询食谱
     * 
     * @param ids 食谱ID列表
     * @return 食谱列表
     */
    List<Recipe> findByIds(List<Long> ids);
    
    /**
     * 更新食谱
     * 
     * @param recipe 食谱
     * @return 更新后的食谱
     */
    Recipe update(Recipe recipe);
    
    /**
     * 删除食谱（软删除）
     * 
     * @param id 食谱ID
     */
    void deleteById(Long id);
    
    /**
     * 统计食谱总数
     * 
     * @return 总数
     */
    long count();
    
    /**
     * 统计分类下的食谱数量
     * 
     * @param category 分类
     * @return 数量
     */
    long countByCategory(RecipeCategory category);
}
