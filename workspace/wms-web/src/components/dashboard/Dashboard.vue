<template>
  <div class="dashboard-container">
    <!-- 顶部统计卡片 — 第1行 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon sales-icon">
              <el-icon :size="40"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日销售额</div>
              <div class="stat-value">
                <count-to :end-val="todaySales" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon profit-icon">
              <el-icon :size="40"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日毛利</div>
              <div class="stat-value">
                <count-to :end-val="todayProfit" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon net-icon">
              <el-icon :size="40"><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">净利润（毛利-损耗）</div>
              <div class="stat-value">
                <count-to :end-val="netProfit" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 顶部统计卡片 — 第2行 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon loss-icon">
              <el-icon :size="40"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日损耗</div>
              <div class="stat-value loss-value">
                <count-to :end-val="todayLoss" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon warning-icon">
              <el-icon :size="40"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">预警商品数</div>
              <div class="stat-value">
                <count-to :end-val="warningCount" :duration="2000" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon order-icon">
              <el-icon :size="40"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日订单数</div>
              <div class="stat-value">
                <count-to :end-val="todayOrders" :duration="2000" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中间图表区 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="14">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近7天销售趋势</span>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>分类销量占比</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部双列表区 -->
    <el-row :gutter="20" class="tables-section">
      <el-col :span="14">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>补货建议</span>
            </div>
          </template>
          <el-table :data="warningList" style="width: 100%" max-height="350">
            <el-table-column prop="name" label="商品名称" width="150" />
            <el-table-column label="当前库存" width="110">
              <template #default="{ row }">
                <span :class="{ 'danger-text': (row.count - (row.reservedCount||0)) < row.minCount }">
                  {{ row.count - (row.reservedCount||0) }}<span v-if="row.reservedCount" style="color:#909399;font-size:11px"> (预占{{row.reservedCount}})</span>
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="minCount" label="预警下限" width="100" />
            <el-table-column prop="dailyAverageSales" label="日均销量" width="100">
              <template #default="{ row }">
                {{ row.dailyAverageSales?.toFixed(2) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="suggestQuantity" label="建议补货量" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.suggestQuantity > 0" type="danger">
                  {{ row.suggestQuantity }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="状态">
              <template #default="{ row }">
                <el-tag v-if="(row.count - (row.reservedCount||0)) < row.minCount" type="danger" size="small">库存不足</el-tag>
                <el-tag v-else type="success" size="small">正常</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>库存周转率 Top 榜单</span>
            </div>
          </template>
          <el-table :data="turnoverTopList" style="width: 100%" max-height="350">
            <el-table-column type="index" label="排名" width="60">
              <template #default="{ $index }">
                <el-tag 
                  :type="$index < 3 ? 'danger' : 'info'" 
                  size="small"
                  effect="dark"
                >
                  {{ $index + 1 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="goodsName" label="商品名称" />
            <el-table-column prop="currentStock" label="库存" width="80" />
            <el-table-column prop="totalSales" label="30天销量" width="100" />
            <el-table-column prop="turnoverRate" label="周转率" width="100">
              <template #default="{ row }">
                <span class="rate-text">{{ row.turnoverRate }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default { name: 'Dashboard' }
</script>

<script setup>
import { ref, onMounted, onActivated, nextTick, getCurrentInstance } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { Money, TrendCharts, Warning, ShoppingCart, Coin, CircleClose } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl

// 统计数据
const todaySales = ref(0)
const todayProfit = ref(0)
const todayLoss = ref(0)
const netProfit = ref(0)
const warningCount = ref(0)
const todayOrders = ref(0)

// 图表引用
const trendChartRef = ref(null)
const categoryChartRef = ref(null)
let trendChartInstance = null
let categoryChartInstance = null

// 表格数据
const warningList = ref([])
const turnoverTopList = ref([])

// CountTo 组件（简单的数字动画）
const CountTo = {
  props: {
    endVal: { type: Number, default: 0 },
    duration: { type: Number, default: 2000 },
    prefix: { type: String, default: '' }
  },
  data() {
    return {
      displayValue: 0
    }
  },
  mounted() {
    this.animateNumber()
  },
  watch: {
    endVal() {
      this.displayValue = 0
      this.animateNumber()
    }
  },
  methods: {
    animateNumber() {
      const startTime = performance.now()
      const startValue = 0
      const endValue = this.endVal
      const duration = this.duration

      const step = (currentTime) => {
        const elapsed = currentTime - startTime
        const progress = Math.min(elapsed / duration, 1)
        
        // 使用缓动函数
        const easeOutQuart = 1 - Math.pow(1 - progress, 4)
        this.displayValue = startValue + (endValue - startValue) * easeOutQuart

        if (progress < 1) {
          requestAnimationFrame(step)
        } else {
          this.displayValue = endValue
        }
      }

      requestAnimationFrame(step)
    }
  },
  render() {
    const value = this.displayValue.toFixed(2)
    return `${this.prefix}${value}`
  }
}

// 初始化趋势图
const initTrendChart = (data) => {
  if (!trendChartRef.value) return

  if (trendChartInstance) trendChartInstance.dispose()
  trendChartInstance = echarts.init(trendChartRef.value)
  const chart = trendChartInstance
  
  const dates = data.map(item => item.date)
  const salesData = data.map(item => item.totalAmount || 0)
  const profitData = data.map(item => item.grossProfit || 0)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['销售额', '毛利']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: salesData,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      },
      {
        name: '毛利',
        type: 'line',
        smooth: true,
        data: profitData,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      }
    ]
  }

  chart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 初始化分类饼图
const initCategoryChart = (data) => {
  if (!categoryChartRef.value) return

  if (categoryChartInstance) categoryChartInstance.dispose()
  categoryChartInstance = echarts.init(categoryChartRef.value)
  const chart = categoryChartInstance
  
  const pieData = data.map(item => ({
    name: item.categoryName,
    value: item.totalQuantity || item.totalAmount
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '销量占比',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: pieData
      }
    ]
  }

  chart.setOption(option)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

// 加载统计数据
const loadStatsData = async () => {
  try {
    const res = await axios.get(httpUrl + '/stats/todaySummary')
    console.log('今日统计接口返回:', res.data)
    if (res.data.code === 200) {
      const d = res.data.data
      todaySales.value = d.todaySales || 0
      todayProfit.value = d.todayProfit || 0
      todayLoss.value = d.todayLoss || 0
      netProfit.value = d.netProfit || 0
      todayOrders.value = d.todayOrders || 0
    } else {
      console.error('今日统计接口返回错误:', res.data.msg)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    console.error('错误详情:', error.response?.data)
    todaySales.value = 0; todayProfit.value = 0; todayLoss.value = 0; netProfit.value = 0; todayOrders.value = 0
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const [salesRes, profitRes] = await Promise.all([
      axios.get(httpUrl + '/stats/salesTrend', { params: { days: 7 } }),
      axios.get(httpUrl + '/stats/grossProfit', { params: { days: 7 } })
    ])

    const salesData = salesRes.data.data || []
    const profitData = profitRes.data.data || []

    // 合并数据
    const mergedData = salesData.map(sale => {
      const profit = profitData.find(p => p.date === sale.date)
      return {
        ...sale,
        grossProfit: profit ? profit.grossProfit : 0
      }
    })

    await nextTick()
    initTrendChart(mergedData)
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 加载分类数据
const loadCategoryData = async () => {
  try {
    const res = await axios.get(httpUrl + '/stats/categoryRatio')
    await nextTick()
    initCategoryChart(res.data.data || [])
  } catch (error) {
    console.error('加载分类数据失败:', error)
  }
}

// 加载预警列表
const loadWarningList = async () => {
  try {
    const [warningRes, restockRes] = await Promise.all([
      axios.get(httpUrl + '/stats/warningList'),
      axios.get(httpUrl + '/stats/suggestRestock', { params: { cycleDays: 7 } })
    ])

    const warningData = warningRes.data.data || []
    const restockData = restockRes.data.data || []

    // 合并数据
    warningList.value = warningData.map(item => {
      const restock = restockData.find(r => r.goodsId === item.id)
      return {
        ...item,
        dailyAverageSales: restock?.dailyAverageSales || 0,
        suggestQuantity: restock?.suggestQuantity || 0
      }
    })

    warningCount.value = warningList.value.length
  } catch (error) {
    console.error('加载预警列表失败:', error)
    warningList.value = []
    warningCount.value = 0
  }
}

// 加载周转率榜单
const loadTurnoverList = async () => {
  try {
    const res = await axios.get(httpUrl + '/stats/turnoverRate/top')
    turnoverTopList.value = res.data.data || []
  } catch (error) {
    console.error('加载周转率榜单失败:', error)
    turnoverTopList.value = []
  }
}

// 加载所有数据
const loadAllData = async () => {
  await loadStatsData()
  await Promise.all([
    loadTrendData(),
    loadCategoryData(),
    loadWarningList(),
    loadTurnoverList()
  ])
}

onMounted(() => { loadAllData() })
onActivated(() => { loadAllData() })
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.sales-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.profit-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.warning-icon {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  color: #e6a23c;
}

.order-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.net-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #409EFF;
}

.loss-icon {
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: #F56C6C;
}

.loss-value {
  color: #F56C6C !important;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

/* 图表区域样式 */
.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.chart-container {
  height: 350px;
  width: 100%;
}

/* 表格区域样式 */
.tables-section {
  margin-bottom: 20px;
}

.table-card {
  border-radius: 8px;
}

.danger-text {
  color: #f56c6c;
  font-weight: bold;
}

.rate-text {
  color: #67c23a;
  font-weight: bold;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .stat-value {
    font-size: 24px;
  }
  
  .chart-container {
    height: 300px;
  }
}
</style>
