# 物品分类管理 - 快速开始指南

## 📦 已完成的文件

### 前端文件
- ✅ `workspace/wms-web/src/components/goodstype/CategoryManage.vue` - 分类管理页面

### 后端文件
- ✅ `wms/src/main/java/com/wms/entity/Goodstype.java` - 实体类（新增 createTime 字段）
- ✅ `wms/src/main/java/com/wms/controller/GoodstypeController.java` - 控制器（已有 /listAll 接口）

### 数据库脚本
- ✅ `wms/src/main/resources/sql/add_create_time_to_goodstype.sql` - 添加创建时间字段

---

## 🚀 快速部署步骤

### 1️⃣ 执行数据库脚本
```sql
-- 在 MySQL 中执行
ALTER TABLE goodstype 
ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER remark;

UPDATE goodstype SET create_time = NOW() WHERE create_time IS NULL;
```

### 2️⃣ 重启后端服务
```bash
cd wms
mvn spring-boot:run
```

### 3️⃣ 访问前端页面
浏览器访问：`http://localhost:8080/#/category-manage`

或在菜单中添加路由指向 `CategoryManage.vue`

---

## 🎯 核心功能

### 前端功能
✅ 分类名称搜索（模糊查询）  
✅ 表格展示：ID、分类名称、备注、创建时间  
✅ 新增/编辑弹窗（带表单验证）  
✅ 删除操作（二次确认）  
✅ 分页功能  

### 后端接口
✅ `POST /goodstype/listPage` - 分页查询  
✅ `GET /goodstype/listAll` - 获取所有分类（下拉框专用）  
✅ `POST /goodstype/save` - 新增分类  
✅ `POST /goodstype/update` - 更新分类  
✅ `GET /goodstype/del?id=xxx` - 删除分类  

---

## 💡 在物品管理中使用分类下拉框

```javascript
// GoodsManage.vue
const loadGoodsType = () => {
  return axios.get(httpUrl + '/goodstype/listAll').then(res => {
    if (res.data.code == 200) {
      goodstypeData.value = res.data.data
    }
  })
}

onMounted(() => {
  loadGoodsType() // 加载分类数据
})
```

```vue
<el-select v-model="form.goodsType" placeholder="请选择分类">
  <el-option
    v-for="item in goodstypeData"
    :key="item.id"
    :label="item.name"
    :value="item.id"
  />
</el-select>
```

---

## 📋 API 调用示例

### 获取所有分类（下拉框）
```bash
GET http://localhost:8080/goodstype/listAll
```

**响应**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "食品饮料",
      "remark": "零食、饮料等",
      "createTime": "2026-05-02T10:30:00"
    }
  ]
}
```

### 分页查询
```bash
POST http://localhost:8080/goodstype/listPage
Content-Type: application/json

{
  "pagenum": 1,
  "pagesize": 10,
  "param": {
    "name": "食品"
  }
}
```

### 新增分类
```bash
POST http://localhost:8080/goodstype/save
Content-Type: application/json

{
  "name": "电子产品",
  "remark": "手机、电脑等"
}
```

---

## ⚠️ 注意事项

1. **必须先执行 SQL 脚本**，否则后端会报字段不存在错误
2. **分类名称必填**，长度限制 1-50 字符
3. **备注可选**，长度限制 0-200 字符
4. **创建时间自动填充**，无需手动传入

---

## 🔍 测试清单

- [ ] 数据库脚本执行成功
- [ ] 后端服务启动无报错
- [ ] 访问分类管理页面正常显示
- [ ] 新增分类功能正常
- [ ] 编辑分类功能正常
- [ ] 删除分类功能正常
- [ ] 搜索功能正常
- [ ] 分页功能正常
- [ ] 物品管理中分类下拉框正常加载

---

## 📖 详细文档

查看完整文档：[CategoryManage完善说明.md](./CategoryManage完善说明.md)
