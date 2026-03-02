-- 修改weekIdentifier字段类型从VARCHAR到DATE
-- 这将清空shopping_item表中的现有数据,因为无法自动迁移数据格式

-- 清空表数据(因为weekIdentifier格式改变)
TRUNCATE TABLE shopping_item;

-- 修改字段类型
ALTER TABLE shopping_item 
MODIFY COLUMN week_identifier DATE NOT NULL COMMENT '周标识（存储周一的日期,如2025-03-03）';
