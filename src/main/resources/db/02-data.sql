-- 示例数据

USE teaHomeMenu;

-- 插入示例食谱
INSERT INTO `recipe` (`name`, `category_code`, `completion_time`, `source`, `needs_preparation`, `image_paths`, `last_access_time`) VALUES
('红烧肉', 'protein', 90, '家传秘方', 1, '["https://example.com/images/hongshaorou.jpg"]', NOW()),
('番茄炒蛋', 'staple', 15, '家常菜', 0, '["https://example.com/images/fanqiechaoda.jpg"]', NOW()),
('提拉米苏', 'dessert', 120, '意大利甜点', 1, '["https://example.com/images/tiramisu.jpg"]', NOW()),
('全麦面包', 'bread', 180, '健康烘焙', 1, '["https://example.com/images/bread.jpg"]', NOW());

-- 插入食材（红烧肉）
INSERT INTO `recipe_ingredient` (`recipe_id`, `name`, `quantity`, `sort_order`) VALUES
(1, '五花肉', '500g', 1),
(1, '冰糖', '50g', 2),
(1, '生抽', '30ml', 3),
(1, '老抽', '15ml', 4),
(1, '料酒', '30ml', 5),
(1, '姜片', '5片', 6),
(1, '八角', '2个', 7);

-- 插入食材（番茄炒蛋）
INSERT INTO `recipe_ingredient` (`recipe_id`, `name`, `quantity`, `sort_order`) VALUES
(2, '番茄', '2个', 1),
(2, '鸡蛋', '3个', 2),
(2, '盐', '适量', 3),
(2, '糖', '少许', 4),
(2, '葱花', '适量', 5);

-- 插入步骤（红烧肉）
INSERT INTO `recipe_step` (`recipe_id`, `step_number`, `description`) VALUES
(1, 1, '五花肉切块，冷水下锅焯水，撇去浮沫，捞出备用'),
(1, 2, '锅中放少许油，加入冰糖小火炒至糖色'),
(1, 3, '放入焯好的五花肉翻炒上色'),
(1, 4, '加入姜片、八角、料酒、生抽、老抽翻炒均匀'),
(1, 5, '加入开水没过肉块，大火烧开转小火炖60分钟'),
(1, 6, '大火收汁，撒上葱花即可');

-- 插入步骤（番茄炒蛋）
INSERT INTO `recipe_step` (`recipe_id`, `step_number`, `description`) VALUES
(2, 1, '番茄切块，鸡蛋打散加少许盐'),
(2, 2, '锅中热油，倒入蛋液炒熟盛出'),
(2, 3, '锅中再加油，放入番茄翻炒出汁'),
(2, 4, '加入盐和糖调味'),
(2, 5, '倒入炒好的鸡蛋，翻炒均匀即可');

-- 插入示例库存
INSERT INTO `inventory` (`ingredient_name`, `quantity`, `category_code`, `expiry_date`) VALUES
('番茄', '5个', 'vegetables_fruits', DATE_ADD(CURDATE(), INTERVAL 5 DAY)),
('鸡蛋', '10个', 'meat_poultry_eggs', DATE_ADD(CURDATE(), INTERVAL 15 DAY)),
('五花肉', '300g', 'meat_poultry_eggs', DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
('生抽', '1瓶', 'condiments', DATE_ADD(CURDATE(), INTERVAL 180 DAY)),
('老抽', '1瓶', 'condiments', DATE_ADD(CURDATE(), INTERVAL 180 DAY));
