package com.hellogroup.teamenu.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hellogroup.teamenu.infrastructure.persistence.entity.RecipeStepEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 食谱步骤Mapper
 * 
 * @author HelloGroup
 */
@Mapper
public interface RecipeStepMapper extends BaseMapper<RecipeStepEntity> {
}
