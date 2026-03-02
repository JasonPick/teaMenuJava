# 家庭菜单应用后端API文档

## 基础信息

- **Base URL**: `http://your-server:8900/teaHomeMenu`
- **字符集**: UTF-8
- **数据格式**: JSON

## 统一响应格式

所有API都返回统一的JSON格式:

```json
{
  "code": "0000",
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1706234567890
}
```

---

## 一、食谱管理 API

### 1. 创建食谱

**POST** `/api/recipes`

### 2. 获取食谱详情

**GET** `/api/recipes/{id}`

### 3. 分页查询所有食谱

**GET** `/api/recipes?page=1&size=10`

### 4. 按分类查询食谱

**GET** `/api/recipes/category/{categoryCode}?page=1&size=10`

### 5. 搜索食谱

**GET** `/api/recipes/search?keyword=xxx&page=1&size=10`

### 6. 更新食谱

**PUT** `/api/recipes/{id}`

### 7. 删除食谱

**DELETE** `/api/recipes/{id}`

### 8. 导入食谱(异步)

**POST** `/api/recipes/import`

### 9. 查询导入任务状态

**GET** `/api/recipes/import/tasks/{taskId}`

---

## 二、菜单计划 API

### 1. 添加食谱到计划

**POST** `/api/meal-plans`

**请求体:**
```json
{
  "recipeId": 1001,
  "planDate": "2026-02-01",
  "weekType": "current"
}
```

**响应:**
```json
{
  "code": "0000",
  "message": "操作成功",
  "data": {
    "id": 1,
    "recipeId": 1001,
    "planDate": "2026-02-01",
    "weekType": "current",
    "createTime": "2026-01-26 10:30:00"
  },
  "timestamp": 1706234567890
}
```

### 2. 从计划中移除食谱

**DELETE** `/api/meal-plans/{id}`

### 3. 获取指定日期的菜谱计划

**GET** `/api/meal-plans/date/{date}?weekType=current`

### 4. 获取整周的菜谱计划

**GET** `/api/meal-plans/week/{weekType}`

参数: weekType = current (本周) 或 next (下周)

### 5. 获取指定周类型的所有计划项

**GET** `/api/meal-plans?weekType=current`

### 6. 清理过期的菜谱计划

**DELETE** `/api/meal-plans/cleanup`

---

## 三、冰箱库存 API

### 1. 添加库存食材

**POST** `/api/inventory`

**请求体:**
```json
{
  "name": "牛奶",
  "quantity": "1L",
  "categoryCode": "MEAT",
  "expiryDate": "2026-02-10"
}
```

### 2. 更新库存食材

**PUT** `/api/inventory/{id}`

### 3. 删除库存食材

**DELETE** `/api/inventory/{id}`

### 4. 获取库存列表

**GET** `/api/inventory?category=VEGETABLE`

可选分类: VEGETABLE, MEAT, COOKED, SNACKS, SEASONING

### 5. 获取已过期的食材

**GET** `/api/inventory/expired`

### 6. 获取即将过期的食材(3天内)

**GET** `/api/inventory/expiring-soon`

### 7. 批量删除已过期食材

**DELETE** `/api/inventory/expired`

---

## 四、购物清单 API

### 1. 生成采购清单(按日期范围)

**POST** `/api/shopping-list/generate`

**请求体:**
```json
{
  "startDate": "2026-02-01",
  "endDate": "2026-02-07",
  "weekType": "current"
}
```

**响应:**
```json
{
  "code": "0000",
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "ingredientName": "鸡蛋",
      "quantity": "10个",
      "purchased": false,
      "weekIdentifier": "2026-W05",
      "createTime": "2026-01-26 10:30:00"
    }
  ],
  "timestamp": 1706234567890
}
```

### 2. 根据周类型生成采购清单

**POST** `/api/shopping-list/generate/{weekType}`

参数: weekType = current (本周) 或 next (下周) 或 both (本周+下周)

### 3. 获取采购清单

**GET** `/api/shopping-list?weekIdentifier=2026-W05`

参数: weekIdentifier (可选) - 周标识,不传则返回所有未采购的项

### 4. 获取未采购的项

**GET** `/api/shopping-list/unpurchased`

### 5. 更新采购项

**PUT** `/api/shopping-list/{id}`

**请求体:**
```json
{
  "ingredientName": "鸡蛋",
  "quantity": "12个",
  "purchased": false,
  "weekIdentifier": "2026-W05"
}
```

### 6. 标记为已采购

**PUT** `/api/shopping-list/{id}/purchase`

### 7. 删除采购项

**DELETE** `/api/shopping-list/{id}`

### 8. 清空采购清单

**DELETE** `/api/shopping-list?weekIdentifier=2026-W05`

参数: weekIdentifier (可选) - 周标识,不传则清空所有

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 0000 | 成功 |
| 1001 | 参数错误 |
| 1002 | 数据不存在 |
| 1003 | 业务逻辑错误 |
| 9999 | 系统错误 |

---

## 日期时间格式

- 日期: `yyyy-MM-dd` (如: 2026-01-26)
- 日期时间: `yyyy-MM-dd HH:mm:ss` (如: 2026-01-26 10:30:00)
- 时区: Asia/Shanghai (UTC+8)

---

## iOS客户端集成说明

iOS客户端已实现以下API Repository:

1. **APIRecipeRepository** - 食谱管理
2. **APIMealPlanRepository** - 菜单计划
3. **APIInventoryRepository** - 冰箱库存

配置API Base URL:
```swift
// Debug模式
APIConfig.shared.setBaseURL("http://localhost:8900/teaHomeMenu")

// 生产环境
APIConfig.shared.setBaseURL("http://your-server:8900/teaHomeMenu")
```
