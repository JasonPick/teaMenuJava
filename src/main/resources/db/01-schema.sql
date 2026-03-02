-- ============================================================
-- 茶饮菜单应用数据库表结构
-- 数据库版本: MySQL 5.7+
-- 字符集: utf8mb4
-- 创建日期: 2026-01-28
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS teaHomeMenu
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE teaHomeMenu;

-- ============================================================
-- 表: recipe (食谱表)
-- 描述: 存储食谱的基本信息,包括名称、分类、完成时间等
-- ============================================================
CREATE TABLE IF NOT EXISTS `recipe` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '食谱名称',
    `category_code` VARCHAR(50) NOT NULL COMMENT '食谱分类代码(BREAKFAST/LUNCH/DINNER/DESSERT/DRINK)',
    `completion_time` INT(11) NOT NULL COMMENT '完成时间（分钟）',
    `source` VARCHAR(500) DEFAULT NULL COMMENT '来源简述(如小红书链接、书籍等)',
    `needs_preparation` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要预处理(0-否,1-是)',
    `image_paths` TEXT DEFAULT NULL COMMENT '食谱图片路径（JSON数组格式）',
    `last_access_time` DATETIME DEFAULT NULL COMMENT '最后访问时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category_code`),
    KEY `idx_last_access` (`last_access_time`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_name` (`name`(50))
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱表';

-- ============================================================
-- 表: recipe_ingredient (食谱食材表)
-- 描述: 存储每个食谱所需的食材及用量
-- ============================================================
CREATE TABLE IF NOT EXISTS `recipe_ingredient` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `recipe_id` BIGINT(20) NOT NULL COMMENT '食谱ID(关联recipe.id)',
    `name` VARCHAR(100) NOT NULL COMMENT '食材名称',
    `quantity` VARCHAR(100) NOT NULL COMMENT '数量描述(如:500g、2个、适量)',
    `sort_order` INT(11) NOT NULL DEFAULT 0 COMMENT '排序序号(越小越靠前)',
    PRIMARY KEY (`id`),
    KEY `idx_recipe_id` (`recipe_id`),
    KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱食材表';

-- ============================================================
-- 表: recipe_step (食谱步骤表)
-- 描述: 存储食谱的制作步骤详情
-- ============================================================
CREATE TABLE IF NOT EXISTS `recipe_step` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `recipe_id` BIGINT(20) NOT NULL COMMENT '食谱ID(关联recipe.id)',
    `step_number` INT(11) NOT NULL COMMENT '步骤序号(从1开始)',
    `description` TEXT NOT NULL COMMENT '步骤描述',
    `image_path` VARCHAR(500) DEFAULT NULL COMMENT '步骤图片路径',
    PRIMARY KEY (`id`),
    KEY `idx_recipe_id` (`recipe_id`),
    KEY `idx_step_number` (`step_number`),
    UNIQUE KEY `uk_recipe_step` (`recipe_id`, `step_number`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='食谱步骤表';

-- ============================================================
-- 表: meal_plan (每周菜谱计划表)
-- 描述: 存储用户的每周菜谱计划安排
-- ============================================================
CREATE TABLE IF NOT EXISTS `meal_plan` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `recipe_id` BIGINT(20) NOT NULL COMMENT '食谱ID(关联recipe.id)',
    `plan_date` DATE NOT NULL COMMENT '计划日期',
    `week_identifier` VARCHAR(20) NOT NULL COMMENT '周标识（格式:YYYY-Wnn,如2026-W04）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_recipe_id` (`recipe_id`),
    KEY `idx_plan_date` (`plan_date`),
    KEY `idx_week_identifier` (`week_identifier`),
    KEY `idx_deleted` (`deleted`),
    UNIQUE KEY `uk_plan_date_recipe` (`plan_date`, `recipe_id`, `deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每周菜谱计划表';

-- ============================================================
-- 表: inventory (冰箱库存表)
-- 描述: 存储冰箱中的食材库存信息
-- ============================================================
CREATE TABLE IF NOT EXISTS `inventory` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ingredient_name` VARCHAR(100) NOT NULL COMMENT '食材名称',
    `quantity` VARCHAR(100) NOT NULL COMMENT '库存数量(如:500g、2个)',
    `category_code` VARCHAR(50) NOT NULL COMMENT '食材分类代码(VEGETABLE/MEAT/SEAFOOD/FRUIT/DAIRY/GRAIN/SEASONING/OTHER)',
    `expiry_date` DATE NOT NULL COMMENT '过期日期',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_ingredient_name` (`ingredient_name`),
    KEY `idx_category` (`category_code`),
    KEY `idx_expiry_date` (`expiry_date`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='冰箱库存表';

-- ============================================================
-- 表: shopping_item (采购清单表)
-- 描述: 存储需要采购的食材清单
-- ============================================================
CREATE TABLE IF NOT EXISTS `shopping_item` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ingredient_name` VARCHAR(100) NOT NULL COMMENT '食材名称',
    `quantity` VARCHAR(100) NOT NULL COMMENT '需要采购的数量(如:500g、2个)',
    `purchased` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已采购(0-未采购,1-已采购)',
    `inventory_status` VARCHAR(20) DEFAULT 'NONE' COMMENT '库存状态(NONE-无库存,EXPIRED-有但已过期,SUFFICIENT-有且未过期)',
    `week_identifier` DATE NOT NULL COMMENT '周标识（存储周一的日期,如2025-03-03）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记(0-未删除,1-已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_week_identifier` (`week_identifier`),
    KEY `idx_purchased` (`purchased`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_ingredient_name` (`ingredient_name`),
    KEY `idx_inventory_status` (`inventory_status`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购清单表';

-- ============================================================
-- 索引说明
-- ============================================================
-- recipe表:
--   - idx_category: 按分类查询食谱
--   - idx_last_access: 按访问时间排序(最近使用)
--   - idx_deleted: 逻辑删除过滤
--   - idx_name: 按名称搜索食谱
--
-- recipe_ingredient表:
--   - idx_recipe_id: 查询某个食谱的所有食材
--   - idx_name: 按食材名称搜索
--
-- recipe_step表:
--   - idx_recipe_id: 查询某个食谱的所有步骤
--   - idx_step_number: 按步骤序号排序
--   - uk_recipe_step: 确保同一食谱的步骤序号唯一
--
-- meal_plan表:
--   - idx_recipe_id: 查询某个食谱的计划安排
--   - idx_plan_date: 按日期查询计划
--   - idx_week_identifier: 按周查询计划
--   - uk_plan_date_recipe: 防止同一天重复安排同一食谱
--
-- inventory表:
--   - idx_ingredient_name: 按食材名称查询库存
--   - idx_category: 按分类查询库存
--   - idx_expiry_date: 查询即将过期的食材
--
-- shopping_item表:
--   - idx_week_identifier: 按周查询采购清单
--   - idx_purchased: 查询未采购的项目
--   - idx_ingredient_name: 按食材名称搜索
