package com.hellogroup.teamenu.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hellogroup.teamenu.infrastructure.persistence.entity.RecipeIngredientEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食谱食材Mapper
 * 
 * @author HelloGroup
 */
@Mapper
public interface RecipeIngredientMapper extends BaseMapper<RecipeIngredientEntity> {
}
