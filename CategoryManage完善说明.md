# 物品分类管理模块完善说明

## 概述

已完成物品分类管理（CategoryManage）页面的创建和后端接口的完善，支持分类的增删改查功能，并为物品管理提供下拉框数据接口。

## 一、前端页面：CategoryManage.vue

### 文件位置
```
workspace/wms-web/src/components/goodstype/CategoryManage.vue
```

### 功能特性

#### 1. 顶部搜索区
- ✅ 分类名称输入框（支持回车查询）
- ✅ 查询按钮
- ✅ 重置按钮（清空搜索条件并重新查询）
- ✅ 新增按钮

```vue
<el-input 
  v-model="name" 
  @keyup.enter="loadPost" 
  placeholder="请输入分类名称" 
  :suffix-icon="Search" 
/>
<el-button type="primary" @click="loadPost">查询</el-button>
<el-button type="success" @click="resetParam">重置</el-button>
<el-button type="primary" @click="add">新增</el-button>
```

#### 2. 主表格展示

**展示字段**：
| 字段 | 说明 | 宽度 | 对齐方式 |
|------|------|------|---------|
| id | 分类ID | 100px | 居中 |
| name | 分类名称 | 200px | 左对齐 |
| remark | 备注 | 自适应 | 左对齐 |
| createTime | 创建时间 | 180px | 左对齐 |
| operate | 操作 | 200px | 居中 |

**操作列功能**：
- **编辑按钮**：打开编辑弹窗，回显当前行数据
- **删除按钮**：带二次确认的删除操作

```vue
<el-table-column prop="createTime" label="创建时间" width="180">
  <template #default="scope">
    {{ scope.row.createTime || '-' }}
  </template>
</el-table-column>
```

#### 3. 分页组件
- 每页条数选项：5、10、20、50
- 显示总记录数
- 支持页码跳转
- 中文显示（上一页、下一页）

```vue
<el-pagination
  @size-change="handleSizeChange"
  @current-change="handleCurrentChange"
  :page-sizes="[5, 10, 20, 50]"
  layout="total, sizes, prev, pager, next, jumper"
/>
```

#### 4. 新增/编辑弹窗

**表单字段**：
- **分类名称**（必填）
  - 最大长度：50字符
  - 显示字数统计
  - 必填校验
  
- **备注**（可选）
  - 多行文本框（3行）
  - 最大长度：200字符
  - 显示字数统计

**表单验证规则**：
```javascript
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}
```

**动态标题**：
- 新增模式：显示"新增分类"
- 编辑模式：显示"编辑分类"

```vue
<el-dialog :title="form.id ? '编辑分类' : '新增分类'" ...>
```

### 核心逻辑

#### 数据加载
```javascript
onMounted(() => {
  loadPost()
})

const loadPost = () => {
  axios.post(httpUrl + '/goodstype/listPage', {
    pagesize: pageSize.value,
    pagenum: pageNum.value,
    param: {
      name: name.value
    }
  }).then(res => {
    const result = res.data
    if (result.code == 200) {
      tableData.value = Array.isArray(result.data) ? result.data : []
      total.value = result.total || 0
    }
  })
}
```

#### 保存逻辑
```javascript
const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      const submitData = {
        name: form.value.name,
        remark: form.value.remark
      }

      if (form.value.id) {
        // 修改操作
        submitData.id = form.value.id
        axios.post(httpUrl + '/goodstype/update', submitData)
      } else {
        // 新增操作
        axios.post(httpUrl + '/goodstype/save', submitData)
      }
    }
  })
}
```

## 二、后端接口

### 1. 实体类：Goodstype.java

**文件位置**：
```
wms/src/main/java/com/wms/entity/Goodstype.java
```

**字段清单**：
```java
@TableId(value = "id", type = IdType.AUTO)
private Integer id;              // 主键

private String name;             // 分类名

private String remark;           // 备注

@TableField("create_time")
private LocalDateTime createTime; // 创建时间
```

**新增字段**：
- `createTime`：创建时间（LocalDateTime 类型）
- 数据库字段：`create_time`
- 默认值：CURRENT_TIMESTAMP

### 2. 控制器：GoodstypeController.java

**文件位置**：
```
wms/src/main/java/com/wms/controller/GoodstypeController.java
```

#### 接口清单

| 接口路径 | 请求方式 | 功能说明 | 参数 |
|---------|---------|---------|------|
| `/goodstype/listPage` | POST | 分页查询 | QueryPageParam（包含 name 模糊查询） |
| `/goodstype/list` | GET | 获取所有分类 | 无 |
| `/goodstype/listAll` | GET | 获取所有分类（下拉框专用） | 无 |
| `/goodstype/save` | POST | 新增分类 | Goodstype 对象 |
| `/goodstype/update` | POST | 更新分类 | Goodstype 对象 |
| `/goodstype/del` | GET | 删除分类 | id（String） |

#### 核心接口代码

**1. 分页查询接口**
```java
@PostMapping("/listPage")
public Result listPage(@RequestBody QueryPageParam query) {
    HashMap param = query.getParam();
    String name = (String) param.get("name");

    Page<Goodstype> page = new Page();
    page.setCurrent(query.getPagenum());
    page.setSize(query.getPagesize());
    
    LambdaQueryWrapper<Goodstype> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    if (StringUtils.isNotBlank(name) && !"null".equals(name)) {
        lambdaQueryWrapper.like(Goodstype::getName, name);
    }

    IPage result = goodstypeService.page(page, lambdaQueryWrapper);
    return Result.suc(result.getRecords(), result.getTotal());
}
```

**2. 下拉框数据接口（供物品管理调用）**
```java
/**
 * 获取所有分类的键值对（用于前端下拉框）
 * @return 分类列表（包含id和name）
 */
@GetMapping("/listAll")
public Result listAll() {
    List<Goodstype> list = goodstypeService.list();
    return Result.suc(list);
}
```

**返回数据示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "食品饮料",
      "remark": "零食、饮料等",
      "createTime": "2026-05-02T10:30:00"
    },
    {
      "id": 2,
      "name": "日用品",
      "remark": "生活必需品",
      "createTime": "2026-05-02T11:00:00"
    }
  ]
}
```

**3. 新增接口**
```java
@PostMapping("/save")
public Result save(@RequestBody Goodstype goodstype) {
    return goodstypeService.save(goodstype) ? Result.suc() : Result.fail();
}
```

**4. 更新接口**
```java
@PostMapping("/update")
public Result update(@RequestBody Goodstype goodstype) {
    return goodstypeService.updateById(goodstype) ? Result.suc() : Result.fail();
}
```

**5. 删除接口**
```java
@GetMapping("/del")
public Result del(@RequestParam String id) {
    return goodstypeService.removeById(id) ? Result.suc() : Result.fail();
}
```

### 3. 数据库迁移脚本

**文件位置**：
```
wms/src/main/resources/sql/add_create_time_to_goodstype.sql
```

**SQL 内容**：
```sql
-- 为 goodstype 表添加 create_time 字段
ALTER TABLE goodstype 
ADD COLUMN IF NOT EXISTS create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER remark;

-- 更新已有数据的创建时间为当前时间
UPDATE goodstype SET create_time = NOW() WHERE create_time IS NULL;
```

**执行方法**：
```bash
# 在 MySQL 客户端执行
source E:/javaEE/springboot-vue-wms02/wms/src/main/resources/sql/add_create_time_to_goodstype.sql
```

## 三、前端调用示例

### 1. 在物品管理中调用分类下拉框

```javascript
// GoodsManage.vue 中加载分类数据
const loadGoodsType = () => {
  return axios.get(httpUrl + '/goodstype/listAll').then(res => {
    const result = res.data
    if (result.code == 200) {
      goodstypeData.value = Array.isArray(result.data) ? result.data : []
    }
  })
}

// 在 onMounted 中并发加载
onMounted(() => {
  Promise.all([
    loadGoodsType(),
    loadStorage(),
    loadSupplier()
  ]).then(() => {
    loadPost()
  })
})
```

### 2. 下拉框使用

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

## 四、测试建议

### 前端测试
- [ ] 访问分类管理页面，验证表格正常加载
- [ ] 输入分类名称进行搜索，验证模糊查询功能
- [ ] 点击"重置"按钮，验证搜索条件清空
- [ ] 点击"新增"按钮，验证弹窗打开且表单为空
- [ ] 提交空表单，验证必填校验生效
- [ ] 输入超长分类名称（>50字符），验证长度限制
- [ ] 新增成功后，验证表格自动刷新
- [ ] 点击"编辑"按钮，验证数据正确回显
- [ ] 修改后保存，验证数据更新成功
- [ ] 点击"删除"按钮，验证二次确认弹窗
- [ ] 确认删除后，验证表格自动刷新
- [ ] 验证分页功能（切换每页条数、翻页）
- [ ] 验证创建时间字段正确显示

### 后端测试
- [ ] 调用 `/goodstype/listAll` 接口，验证返回所有分类
- [ ] 调用 `/goodstype/listPage` 接口，验证分页和模糊查询
- [ ] 调用 `/goodstype/save` 接口，验证新增成功且 create_time 自动填充
- [ ] 调用 `/goodstype/update` 接口，验证更新成功
- [ ] 调用 `/goodstype/del` 接口，验证删除成功
- [ ] 验证数据库中 create_time 字段是否正确存储

### 集成测试
- [ ] 在物品管理页面，验证分类下拉框正确加载数据
- [ ] 选择分类后保存物品，验证关联正确
- [ ] 删除被物品引用的分类，验证是否有约束处理

## 五、注意事项

1. **数据库字段**：必须先执行 SQL 脚本添加 `create_time` 字段，否则后端会报错

2. **时间格式化**：如果前端需要格式化时间，可以使用 dayjs 或 moment.js：
   ```javascript
   import dayjs from 'dayjs'
   {{ dayjs(scope.row.createTime).format('YYYY-MM-DD HH:mm:ss') }}
   ```

3. **删除约束**：如果分类被物品引用，建议在后端添加删除前的检查逻辑，防止误删

4. **性能优化**：`/listAll` 接口返回全量数据，如果分类数量很大（>1000），建议添加缓存机制

5. **权限控制**：目前所有接口未做权限验证，建议根据业务需求添加角色权限控制

## 六、后续优化建议

1. **批量操作**：支持批量删除分类
2. **排序功能**：支持按创建时间、名称排序
3. **导出功能**：支持导出分类列表为 Excel
4. **图标支持**：为分类添加图标字段，提升视觉效果
5. **层级结构**：支持多级分类（父子关系）
6. **软删除**：改为逻辑删除，保留历史数据
