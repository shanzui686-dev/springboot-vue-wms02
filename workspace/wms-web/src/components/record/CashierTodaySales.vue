<template>
  <div class="cashier-today-sales">
    <!-- 顶部统计卡片 — 第1行 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon order-icon">
              <el-icon :size="40"><Tickets /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日销售笔数</div>
              <div class="stat-value">
                <count-to :end-val="summary.count" :duration="2000" :decimals="0" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon sales-icon">
              <el-icon :size="40"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日应收总额</div>
              <div class="stat-value">
                <count-to :end-val="summary.totalAmount" :duration="2000" prefix="¥" />
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
              <div class="stat-label">今日实收总额</div>
              <div class="stat-value">
                <count-to :end-val="summary.realAmount" :duration="2000" prefix="¥" />
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
            <div class="stat-icon return-icon">
              <el-icon :size="40"><RefreshLeft /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日退货笔数</div>
              <div class="stat-value">
                <count-to :end-val="returnSummary.returnCount" :duration="2000" :decimals="0" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon due-icon">
              <el-icon :size="40"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日应退款金额</div>
              <div class="stat-value">
                <count-to :end-val="returnSummary.returnDueAmount" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon paid-icon">
              <el-icon :size="40"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日实退款金额</div>
              <div class="stat-value">
                <count-to :end-val="returnSummary.returnPaidAmount" :duration="2000" prefix="¥" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 饼图区：今日销售了哪些商品 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日商品销售金额占比</span>
            </div>
          </template>
          <div ref="amountChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日商品销量占比</span>
            </div>
          </template>
          <div ref="quantityChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 饼图区：今日退换货情况 -->
    <el-row :gutter="20" class="charts-section">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日退换货数量占比</span>
            </div>
          </template>
          <div ref="returnQtyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>今日退款金额占比</span>
            </div>
          </template>
          <div ref="returnAmountChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, getCurrentInstance, nextTick } from 'vue'
import { Money, Coin, Tickets, RefreshLeft, Warning, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl

// 数字动画组件（与店长统计页 Dashboard.vue 同款）
const CountTo = {
  props: {
    endVal: { type: Number, default: 0 },
    duration: { type: Number, default: 2000 },
    prefix: { type: String, default: '' },
    decimals: { type: Number, default: 2 }
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
    const value = this.displayValue.toFixed(this.decimals)
    return `${this.prefix}${value}`
  }
}

// 用户信息：7天免登录存 localStorage，普通登录存 sessionStorage
const user = JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || '{}')

// 今天日期 YYYY-MM-DD
const getToday = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 统计数据（固定今日 + 本人）
const summary = ref({ count: 0, totalAmount: 0, realAmount: 0 })
const returnSummary = ref({ returnCount: 0, returnDueAmount: 0, returnPaidAmount: 0 })
const goodsSales = ref([])
const returnGoodsSales = ref([])

// 图表引用
const amountChartRef = ref(null)
const quantityChartRef = ref(null)
const returnQtyChartRef = ref(null)
const returnAmountChartRef = ref(null)
let amountChartInstance = null
let quantityChartInstance = null
let returnQtyChartInstance = null
let returnAmountChartInstance = null

// 查询参数（今日 + 本人）
const buildParams = () => {
  const today = getToday()
  return {
    userId: user.id,
    startDate: today + ' 00:00:00',
    endDate: today + ' 23:59:59'
  }
}

// 初始化饼图（与店长统计页分类饼图同款样式）
const initPieChart = (chartInstance, chartRef, data, valueKey) => {
  if (!chartRef.value) return null

  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(chartRef.value)

  const pieData = data.map(item => ({
    name: item.goodsName,
    value: item[valueKey] || 0
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
        name: '销售占比',
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

  chartInstance.setOption(option)
  return chartInstance
}

// 加载销售汇总
const loadSummary = () => {
  axios.get(httpUrl + '/sales/summary', { params: buildParams() })
    .then(res => {
      const result = res.data
      if (result.code === 200 && result.data) {
        summary.value = result.data
      }
    })
    .catch(() => {
      ElMessage.error('销售汇总加载失败')
    })
}

// 加载退换货汇总
const loadReturnSummary = () => {
  axios.get(httpUrl + '/sales/returnSummary', { params: buildParams() })
    .then(res => {
      const result = res.data
      if (result.code === 200 && result.data) {
        returnSummary.value = result.data
      }
    })
    .catch(() => {
      ElMessage.error('退换货汇总加载失败')
    })
}

// 加载商品销售汇总并渲染饼图
const loadGoodsSales = async () => {
  try {
    const res = await axios.get(httpUrl + '/sales/goodsSales', { params: buildParams() })
    const result = res.data
    if (result.code === 200) {
      goodsSales.value = result.data || []
    } else {
      ElMessage.error(result.msg || '商品销售数据加载失败')
    }
    await nextTick()
    amountChartInstance = initPieChart(amountChartInstance, amountChartRef, goodsSales.value, 'totalAmount')
    quantityChartInstance = initPieChart(quantityChartInstance, quantityChartRef, goodsSales.value, 'totalQuantity')
  } catch (error) {
    ElMessage.error('商品销售数据加载失败')
  }
}

// 加载退换货商品汇总并渲染饼图
const loadReturnGoodsSales = async () => {
  try {
    const res = await axios.get(httpUrl + '/sales/returnGoodsSales', { params: buildParams() })
    const result = res.data
    if (result.code === 200) {
      returnGoodsSales.value = result.data || []
    } else {
      ElMessage.error(result.msg || '退换货商品数据加载失败')
    }
    await nextTick()
    // 数量占比：退货/换货分开显示（同商品两段：xxx（退货）/ xxx（换货））
    const qtyData = returnGoodsSales.value.map(item => ({
      ...item,
      goodsName: `${item.goodsName}（${item.type === 2 ? '换货' : '退货'}）`
    }))
    returnQtyChartInstance = initPieChart(returnQtyChartInstance, returnQtyChartRef, qtyData, 'totalQuantity')
    // 退款金额占比：按商品合并（不区分类型）
    const merged = {}
    returnGoodsSales.value.forEach(item => {
      if (!merged[item.goodsName]) {
        merged[item.goodsName] = { ...item, totalAmount: 0 }
      }
      merged[item.goodsName].totalAmount += Number(item.totalAmount) || 0
    })
    returnAmountChartInstance = initPieChart(returnAmountChartInstance, returnAmountChartRef, Object.values(merged), 'totalAmount')
  } catch (error) {
    ElMessage.error('退换货商品数据加载失败')
  }
}

// 加载所有数据
const loadAllData = () => {
  loadSummary()
  loadReturnSummary()
  loadGoodsSales()
  loadReturnGoodsSales()
}

// 图表随窗口缩放自适应
const handleResize = () => {
  amountChartInstance?.resize()
  quantityChartInstance?.resize()
  returnQtyChartInstance?.resize()
  returnAmountChartInstance?.resize()
}

// 实时更新轮询（5秒）
let pollTimer = null
const POLL_INTERVAL = 5000

onMounted(() => {
  loadAllData()
  window.addEventListener('resize', handleResize)
  pollTimer = setInterval(loadAllData, POLL_INTERVAL)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<style scoped>
.cashier-today-sales {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: calc(100vh - 60px);
}

/* 统计卡片样式（与店长统计页一致） */
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

.net-icon {
  background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
  color: #409EFF;
}

.order-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.return-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.due-icon {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  color: #e6a23c;
}

.paid-icon {
  background: linear-gradient(135deg, #96fbc4 0%, #f9f586 100%);
  color: #67C23A;
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
