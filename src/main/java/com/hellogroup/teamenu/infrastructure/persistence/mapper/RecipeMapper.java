package com.hellogroup.teamenu.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hellogroup.teamenu.infrastructure.persistence.entity.RecipeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 食谱Mapper
 * 
 * @author HelloGroup
 */
@Mapper
public interface RecipeMapper extends BaseMapper<RecipeEntity> {
    
    /**
     * 搜索食谱（按名称或食材）
     * 
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 食谱列表
     */
    List<RecipeEntity> searchRecipes(@Param("keyword") String keyword, 
                                      @Param("offset") int offset, 
                                      @Param("limit") int limit);
}
