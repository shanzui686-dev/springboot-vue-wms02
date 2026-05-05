# GoodsManage.vue 页面重构说明

## 重构概述

根据业务需求，对物品管理页面进行了全面优化，包括搜索功能、表格展示和表单验证。

## 主要修改内容

### 1. 顶部搜索区优化

#### 修改前
```vue
<el-select v-model="storage" placeholder="请选择仓库">
<el-select v-model="goodstype" placeholder="请选择分类">
<el-checkbox v-model="showUrgentOnly">只看告急商品</el-checkbox>
```

#### 修改后
```vue
<el-select v-model="warehouseId" placeholder="请选择仓库">
<el-select v-model="categoryId" placeholder="请选择分类">
<el-checkbox v-model="isWarning" @change="handleWarningChange">只看告急商品</el-checkbox>
```

**关键改进**：
- ✅ 参数名称与后端接口对齐（`warehouseId`、`categoryId`、`isWarning`）
- ✅ 勾选/取消"只看告急商品"时自动触发查询（`@change="handleWarningChange"`）

### 2. 主表格优化

#### 新增"进价"列
在"零售价"之前添加进价列：
```vue
<el-table-column prop="purchasePrice" label="进价" width="100">
  <template #default="scope">
    {{ scope.row.purchasePrice ? '¥' + Number(scope.row.purchasePrice).toFixed(2) : '-' }}
  </template>
</el-table-column>
```

#### 库存告警视觉增强
使用 `el-tag` 组件替代简单的文字样式：

**修改前**：
```vue
<span :style="{ color: isAlert(scope.row) ? 'red' : '', fontWeight: isAlert(scope.row) ? 'bold' : '' }">
  {{ scope.row.count }}
</span>
<el-tag v-if="isAlert(scope.row)" type="danger" size="small">库存告急</el-tag>
```

**修改后**：
```vue
<el-tag v-if="isAlert(scope.row)" type="danger" effect="dark" style="font-weight: bold;">
  {{ scope.row.count }}
</el-tag>
<span v-else>{{ scope.row.count }}</span>
```

**效果**：
- 告急商品：红色深色标签包裹，数字加粗显示
- 正常商品：普通文本显示

#### 连表字段展示优化
直接使用后端返回的名称字段，无需前端转换：

**修改前**：
```vue
<el-table-column prop="storage" :formatter="formatStorage"></el-table-column>
<el-table-column prop="goodsType" :formatter="formatGoodsTypeDisplay"></el-table-column>
```

**修改后**：
```vue
<el-table-column prop="warehouseName" label="仓库">
  <template #default="scope">
    {{ scope.row.warehouseName || '-' }}
  </template>
</el-table-column>
<el-table-column prop="categoryName" label="分类">
  <template #default="scope">
    {{ scope.row.categoryName || '-' }}
  </template>
</el-table-column>
```

### 3. 新增/编辑弹窗优化

#### 表单字段完整清单
- ✅ 条码（必填）
- ✅ 物品名（必填）
- ✅ 规格
- ✅ 单位
- ✅ 进价（el-input-number，精度2位）
- ✅ 零售价（el-input-number，精度2位，必填）
- ✅ 仓库（下拉选择，必填）
- ✅ 分类（下拉选择，必填）
- ✅ 供应商（下拉选择，可清空）
- ✅ 安全库存（el-input-number，默认值5）
- ✅ 数量（必填）
- ✅ 备注（多行文本）

#### 表单验证规则
```javascript
const rules = {
  name: [
    { required: true, message: '请输入物品名', trigger: 'blur' }
  ],
  barcode: [
    { required: true, message: '请输入商品条码', trigger: 'blur' }
  ],
  storage: [
    { required: true, message: '请选择仓库', trigger: 'change' }
  ],
  goodsType: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  count: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { pattern: /^([1-9][0-9]*){1,4}$/, message: '数量必须为正整数', trigger: 'blur' }
  ],
  retailPrice: [
    { required: true, message: '请输入零售价', trigger: 'blur' }
  ]
}
```

### 4. 数据加载逻辑优化

#### 并发加载下拉数据
使用 `Promise.all` 并发请求，提升加载速度：

```javascript
onMounted(() => {
  Promise.all([
    loadGoodsType(),
    loadStorage(),
    loadSupplier()
  ]).then(() => {
    // 所有数据加载完成后，再加载表格数据
    loadPost()
  }).catch(error => {
    console.error('数据加载失败:', error)
    $message.error('数据加载失败')
  })
})
```

**优势**：
- ✅ 三个下拉数据同时请求，减少等待时间
- ✅ 确保所有下拉数据加载完成后再查询表格
- ✅ 统一的错误处理

#### 查询参数调整
```javascript
const loadPost = () => {
  axios.post(httpUrl+'/goods/listPage', {
    pagesize: pageSize.value,
    pagenum: pageNum.value,
    param: {
      name: name.value,
      warehouseId: warehouseId.value,  // 原 storage
      categoryId: categoryId.value,     // 原 goodstype
      isWarning: isWarning.value        // 原 showUrgentOnly
    }
  })
}
```

### 5. 告警判断逻辑调整

**修改前**：
```javascript
const isAlert = (row) => {
  return row.count < row.minCount  // 小于
}
```

**修改后**：
```javascript
const isAlert = (row) => {
  return row.count <= row.minCount  // 小于等于
}
```

**原因**：与后端 SQL 查询条件保持一致（`count <= min_count`）

## 修改的文件清单

| 文件 | 修改内容 |
|------|---------|
| `GoodsManage.vue` | 完整重构搜索区、表格、弹窗、数据加载逻辑 |

## 测试建议

### 1. 搜索功能测试
- [ ] 输入物品名进行模糊查询
- [ ] 选择仓库筛选
- [ ] 选择分类筛选
- [ ] 勾选"只看告急商品"，验证是否自动触发查询
- [ ] 组合多个条件查询
- [ ] 点击"重置"按钮清空所有条件

### 2. 表格展示测试
- [ ] 验证"进价"列正确显示（保留2位小数）
- [ ] 验证"仓库"列显示仓库名称而非ID
- [ ] 验证"分类"列显示分类名称而非ID
- [ ] 库存告急商品：数值被红色深色标签包裹且加粗
- [ ] 正常库存商品：普通文本显示

### 3. 新增/编辑弹窗测试
- [ ] 点击"新增"打开空表单
- [ ] 点击"编辑"回显商品数据
- [ ] 验证必填字段校验（物品名、条码、仓库、分类、零售价、数量）
- [ ] 验证数量和价格只能输入正数
- [ ] 验证下拉选项正确加载（仓库、分类、供应商）
- [ ] 提交成功后列表自动刷新

### 4. 数据加载测试
- [ ] 页面加载时，三个下拉数据并发请求
- [ ] 所有下拉数据加载完成后才查询表格
- [ ] 网络异常时显示错误提示

## 注意事项

1. **前后端参数对齐**：确保前端发送的参数名（`warehouseId`、`categoryId`、`isWarning`）与后端接收的参数名一致

2. **货币格式化**：价格和进价都使用 `toFixed(2)` 保留两位小数

3. **告警条件一致性**：前端 `isAlert()` 使用 `<=`，与后端 SQL 的 `count <= min_count` 保持一致

4. **组件命名规范**：Vue 组件使用多词命名（`GoodsManage`），符合 ESLint 规范

5. **生命周期钩子**：使用 `onMounted` 替代 `onBeforeMount`，确保 DOM 挂载后再操作

## 后续优化建议

1. **性能优化**：如果下拉数据量大，可以考虑缓存或懒加载
2. **用户体验**：添加加载状态（loading）提示
3. **错误处理**：细化错误提示，区分网络错误和业务错误
4. **响应式设计**：适配不同屏幕尺寸的表格列宽
