# RecordManage.vue 页面重构说明

## 一、重构概述

基于 Vue 3 + Element Plus，对记录管理页面进行了全面重构，增强了搜索功能、优化了表格展示，并新增了数据导出能力。

## 二、主要变更

### 1. 顶部搜索区增强

#### 保留的原有功能
- ✅ 物品名搜索（支持回车快速查询）
- ✅ 仓库下拉选择
- ✅ 分类下拉选择

#### 新增功能

**① 操作类型下拉框**
```vue
<el-select v-model="operationType" placeholder="请选择操作类型">
  <el-option label="全部" value=""></el-option>
  <el-option label="采购入库" value="采购入库"></el-option>
  <el-option label="销售出库" value="销售出库"></el-option>
  <el-option label="退货入库" value="退货入库"></el-option>
  <el-option label="盘点盈亏" value="盘点盈亏"></el-option>
  <el-option label="其他" value="其他"></el-option>
</el-select>
```

**② 日期范围选择器**
- 拆分为两个独立的日期选择器（开始日期 + 结束日期）
- 使用 `disabledStartDate` 和 `disabledEndDate` 实现智能禁用
- 开始日期不能选择未来日期
- 结束日期不能早于开始日期，也不能晚于今天

```vue
<el-date-picker
    v-model="startDate"
    type="date"
    placeholder="开始日期"
    format="YYYY-MM-DD"
    value-format="YYYY-MM-DD"
    :disabled-date="disabledStartDate">
</el-date-picker>

<el-date-picker
    v-model="endDate"
    type="date"
    placeholder="结束日期"
    format="YYYY-MM-DD"
    value-format="YYYY-MM-DD"
    :disabled-date="disabledEndDate">
</el-date-picker>
```

**③ 导出 Excel 按钮**
- 位置：搜索按钮和重置按钮旁边
- 图标：使用 `Download` 图标
- 样式：`type="warning"` 黄色按钮
- 功能：根据当前查询条件导出Excel文件

```vue
<el-button type="warning" @click="handleExport" :icon="Download">导出 Excel</el-button>
```

### 2. 表格列优化

#### 新增列

**① 操作类型列**
- 位置：在"分类"和"申请人"之间
- 显示方式：使用 `el-tag` 标签，不同操作类型显示不同颜色
  - 采购入库：绿色 (success)
  - 销售出库：橙色 (warning)
  - 退货入库：蓝色 (primary)
  - 盘点盈亏：灰色 (info)
  - 其他：默认色

```vue
<el-table-column prop="operationType" label="操作类型" width="120">
  <template #default="{row}">
    <el-tag :type="getOperationTypeTag(row.operationType)" size="small">
      {{ row.operationType || '未分类' }}
    </el-tag>
  </template>
</el-table-column>
```

**② 关联单号列**
- 位置：在"操作类型"之后
- 显示逻辑：有值时显示蓝色文字，无值时显示灰色横杠

```vue
<el-table-column prop="refOrderNum" label="关联单号" width="150">
  <template #default="{row}">
    <span v-if="row.refOrderNum" style="color: #409eff;">{{ row.refOrderNum }}</span>
    <span v-else style="color: #909399;">-</span>
  </template>
</el-table-column>
```

#### 数量列样式重构

使用 `<template>` 插槽实现动态样式：
- **入库**（数量 > 0）：绿色文字 + 加粗 + "+" 前缀
- **出库**（数量 < 0）：红色文字 + 加粗 + 负数显示

```vue
<el-table-column prop="count" label="数量" width="120">
  <template #default="{row}">
    <span :style="{ color: row.count > 0 ? '#67c23a' : '#f56c6c', fontWeight: 'bold' }">
      {{ row.count > 0 ? '+' + row.count : row.count }}
    </span>
  </template>
</el-table-column>
```

#### 隐藏操作列

- ❌ 删除了原有的"操作"列（包含通过/拒绝按钮）
- 原因：流水账记录应保证数据真实性，不允许人为点击"处理"或删除

### 3. 交互逻辑优化

#### 新增方法

**① disabledStartDate**
```javascript
const disabledStartDate = (time) => {
  return time.getTime() > Date.now() // 只能选择今天及之前的日期
}
```

**② disabledEndDate**
```javascript
const disabledEndDate = (time) => {
  const now = Date.now()
  const start = startDate.value ? new Date(startDate.value).getTime() : null
  
  if (start && time.getTime() < start) {
    return true // 不能早于开始日期
  }
  
  return time.getTime() > now // 不能晚于今天
}
```

**③ getOperationTypeTag**
```javascript
const getOperationTypeTag = (operationType) => {
  const typeMap = {
    '采购入库': 'success',
    '销售出库': 'warning',
    '退货入库': 'primary',
    '盘点盈亏': 'info',
    '其他': ''
  }
  return typeMap[operationType] || ''
}
```

**④ handleExport**
```javascript
const handleExport = () => {
  axios.post(httpUrl+'/record/export', {
    pagesize: pageSize.value,
    pagenum: pageNum.value,
    param: {
      name: name.value,
      storage: storage.value,
      goodstype: goodstype.value,
      operationType: operationType.value,
      startDate: startDate.value,
      endDate: endDate.value,
      roleId: user.roleId,
      userId: user.id
    }
  }, {
    responseType: 'blob' // 重要：设置响应类型为 blob
  }).then(res => {
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    
    // 从响应头中获取文件名
    const contentDisposition = res.headers['content-disposition']
    let fileName = '出入库记录.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename\*=utf-8''(.+)/)
      if (fileNameMatch && fileNameMatch[1]) {
        fileName = decodeURIComponent(fileNameMatch[1])
      }
    }
    
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    $message.success('导出成功')
  }).catch(error => {
    console.error('导出失败:', error)
    $message.error('导出失败：' + (error.message || '未知错误'))
  })
}
```

#### 修改的方法

**① resetParam**
- 清空所有新增的查询条件
- 包括：operationType、startDate、endDate

```javascript
const resetParam = () => {
  name.value = ''
  storage.value = ''
  goodstype.value = ''
  operationType.value = ''
  startDate.value = ''
  endDate.value = ''
  loadPost()
}
```

**② loadPost**
- 新增参数传递：operationType、startDate、endDate
- 与后端接口完全对应

#### 删除的方法

- ❌ `handleConfirm` - 确认出库（已移除）
- ❌ `handleReject` - 拒绝出库（已移除）

### 4. UI细节优化

#### 间距统一
- 所有搜索组件之间的左边距统一为 `10px`（原来是 `5px`）
- 保持与其他管理页面（如 AdminManage.vue）的一致性

#### 分页组件优化
- 页码选项调整：`[5, 10, 20, 50]`（原来是 `[2, 5, 10, 20]`）
- 移除了不必要的 `:page-texts` 属性
- 简化配置，使用Element Plus默认文本

#### 表格列宽度调整
- 记录ID：140px → 100px（更紧凑）
- 物品名：120px → 150px（避免截断）
- 备注：固定宽度 → `min-width="150"`（自适应）

#### 标签尺寸统一
- 所有 `el-tag` 添加 `size="small"` 属性
- 使界面更加精致

## 三、代码结构

### 导入部分
```javascript
import { Search, Download } from '@element-plus/icons-vue'
```
- 新增 `Download` 图标用于导出按钮

### 响应式数据
```javascript
const operationType = ref('')  // 新增
const startDate = ref('')      // 新增
const endDate = ref('')        // 新增
// 删除了 const status = ref('')
```

### 注释规范
- 所有方法都添加了 JSDoc 风格的注释
- 符合 Vue 组件开发规范

## 四、兼容性说明

### 与后端的对接
- ✅ `/record/listPageCC` - 已支持 operationType、startDate、endDate 参数
- ✅ `/record/export` - 新增导出接口，接收相同查询参数

### 数据字段映射
| 前端字段 | 后端字段 | 说明 |
|---------|---------|------|
| operationType | operation_type | 操作类型 |
| startDate | startDate | 开始时间 |
| endDate | endDate | 结束时间 |
| refOrderNum | ref_order_num | 关联单据号 |

## 五、测试建议

### 功能测试
1. **搜索功能**
   - 单独测试每个搜索条件
   - 组合多个条件进行搜索
   - 测试日期范围的边界情况（同一天、跨月等）

2. **导出功能**
   - 测试无条件导出（导出全部）
   - 测试带条件导出
   - 验证导出文件的正确性

3. **日期选择器**
   - 验证开始日期不能选未来
   - 验证结束日期不能早于开始日期
   - 验证结束日期不能选未来

### UI测试
1. 检查表格列的顺序是否正确
2. 验证数量列的颜色显示（绿色/红色）
3. 检查操作类型标签的颜色是否匹配
4. 验证关联单号的空值显示

### 兼容性测试
- Chrome、Firefox、Edge 等主流浏览器
- 不同分辨率下的显示效果

## 六、注意事项

1. **日期格式**
   - 前端使用 `YYYY-MM-DD` 格式
   - 后端接收字符串后进行解析
   - 确保时区一致性

2. **大数据量导出**
   - 当前实现是一次性加载所有数据到内存
   - 如果数据量超过10万条，建议后端实现流式导出

3. **权限控制**
   - 已移除操作列，所有用户都无法直接审核
   - 如需恢复审核功能，需要重新设计业务流程

4. **性能优化**
   - 如果经常按操作类型查询，建议数据库添加索引
   - 前端可以考虑添加防抖处理

## 七、后续优化建议

1. **快捷日期选择**
   - 添加"今天"、"昨天"、"本周"、"本月"等快捷按钮

2. **高级筛选面板**
   - 将搜索条件折叠到可展开的面板中
   - 节省页面空间

3. **列自定义**
   - 允许用户自定义显示/隐藏某些列
   - 保存用户的列配置偏好

4. **批量导出**
   - 支持勾选多条记录进行选择性导出

5. **打印功能**
   - 添加打印预览和打印按钮

## 八、技术栈

- Vue 3 (Composition API)
- Element Plus
- Axios
- JavaScript (ES6+)

## 九、相关文件

- 前端页面：`workspace/wms-web/src/components/record/RecordManage.vue`
- 后端控制器：`wms/src/main/java/com/wms/controller/RecordController.java`
- 后端实体：`wms/src/main/java/com/wms/entity/Record.java`
- 导出VO：`wms/src/main/java/com/wms/entity/RecordExportVO.java`
