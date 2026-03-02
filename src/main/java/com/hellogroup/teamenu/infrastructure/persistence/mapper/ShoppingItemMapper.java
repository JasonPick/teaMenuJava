package com.hellogroup.teamenu.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hellogroup.teamenu.infrastructure.persistence.entity.ShoppingItemEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购清单项Mapper
 * 
 * @author HelloGroup
 */
@Mapper
public interface ShoppingItemMapper extends BaseMapper<ShoppingItemEntity> {
}
