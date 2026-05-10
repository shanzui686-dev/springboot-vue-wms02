<template>
  <div class="cashier-desk">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="cart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><ShoppingCart /></el-icon>
              <span>扫码与购物车</span>
            </div>
          </template>

          <div class="barcode-section">
            <el-input
                v-model="barcode"
                placeholder="请扫描商品条码或输入条码后按回车"
                prefix-icon="Search"
                clearable
                size="large"
                @keyup.enter="handleBarcodeSearch"
                class="barcode-input"
            >
              <template #append>
                <el-button type="primary" @click="handleBarcodeSearch">
                  <el-icon><Search /></el-icon>
                  查询
                </el-button>
              </template>
            </el-input>
          </div>

          <el-table
              :data="cartItems"
              border
              stripe
              style="width: 100%;"
              empty-text="购物车为空，请扫描商品条码"
          >
            <el-table-column prop="name" label="商品名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="specs" label="规格" width="120" show-overflow-tooltip />
            <el-table-column prop="unit" label="单位" width="80" align="center" />
            <el-table-column label="单价" width="120" align="right">
              <template #default="{ row }">
                <span class="price">¥{{ formatPrice(row.retailPrice) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="数量" width="160" align="center">
              <template #default="{ row }">
                <el-input-number
                    v-model="row.count"
                    :min="1"
                    :max="row.maxCount"
                    size="small"
                    controls-position="right"
                    @change="handleQuantityChange(row)"
                />
                <div class="stock-info">库存: {{ row.maxCount }}</div>
              </template>
            </el-table-column>
            <el-table-column label="小计" width="140" align="right">
              <template #default="{ row }">
                <span class="subtotal">¥{{ formatPrice(calculateSubtotal(row)) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ $index }">
                <el-button
                    type="danger"
                    size="small"
                    :icon="Delete"
                    @click="handleDeleteItem($index)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty
              v-if="cartItems.length === 0"
              description="购物车为空，请扫描商品条码"
              :image-size="120"
              style="margin-top: 50px"
          />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="checkout-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><CreditCard /></el-icon>
              <span>结算中心</span>
            </div>
          </template>

          <div class="checkout-panel">
            <div class="amount-item total-amount">
              <div class="amount-label">
                <el-icon><Money /></el-icon>
                <span>应收总额</span>
              </div>
              <div class="amount-value">¥{{ formatPrice(totalAmount) }}</div>
            </div>

            <div class="amount-item">
              <div class="amount-label">
                <el-icon><Wallet /></el-icon>
                <span>顾客实收</span>
              </div>
              <el-input-number
                  v-model="realAmount"
                  :min="0"
                  :precision="2"
                  :step="10"
                  size="large"
                  controls-position="right"
                  placeholder="请输入实收金额"
                  style="width: 100%"
              />
            </div>

            <div class="amount-item change-amount">
              <div class="amount-label">
                <el-icon><Coin /></el-icon>
                <span>找零金额</span>
              </div>
              <div
                  class="amount-value"
                  :class="{ 'insufficient': changeAmount < 0 }"
              >
                ¥{{ formatPrice(changeAmount) }}
              </div>
            </div>

            <div class="payment-method">
              <div class="payment-label">支付方式：</div>
              <el-radio-group v-model="paymentMethod" size="large">
                <el-radio-button value="cash">现金</el-radio-button>
                <el-radio-button value="wechat">微信</el-radio-button>
                <el-radio-button value="alipay">支付宝</el-radio-button>
                <el-radio-button value="card">银行卡</el-radio-button>
              </el-radio-group>
            </div>

            <el-button
                type="success"
                size="large"
                class="checkout-button"
                :disabled="!canCheckout"
                :loading="checkingOut"
                @click="showPaymentDialog"
            >
              <el-icon v-if="!checkingOut"><Check /></el-icon>
              {{ checkingOut ? '处理中...' : '发起收款' }}
            </el-button>

            <el-button
                type="info"
                size="large"
                class="clear-button"
                :disabled="cartItems.length === 0"
                @click="handleClearCart"
            >
              <el-icon><RefreshLeft /></el-icon>
              清空购物车
            </el-button>

            <el-divider />

            <div class="refund-section">
              <div class="refund-title">
                <el-icon><RefreshRight /></el-icon>
                <span>退货退款</span>
              </div>
              <el-input
                  v-model="refundOrderId"
                  placeholder="输入销售单流水号"
                  size="large"
                  clearable
                  style="margin-bottom: 10px"
              >
                <template #prepend>单号</template>
              </el-input>
              <el-button
                  type="warning"
                  size="large"
                  class="refund-button"
                  :disabled="!refundOrderId || refunding"
                  :loading="refunding"
                  @click="handleRefund"
              >
                <el-icon v-if="!refunding"><RefreshRight /></el-icon>
                {{ refunding ? '退款中...' : '确认退款' }}
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="paymentDialogVisible" title="选择收款方式" width="500px">
      <div class="payment-dialog-content">
        <div class="payment-info">
          <div class="info-row">
            <span class="label">应收金额：</span>
            <span class="value highlight">¥{{ formatPrice(totalAmount) }}</span>
          </div>
          <div class="info-row">
            <span class="label">找零金额：</span>
            <span class="value">¥{{ formatPrice(changeAmount) }}</span>
          </div>
        </div>
        <div class="payment-options">
          <el-button type="primary" size="large" @click="handleCashPayment">现金收款</el-button>
          <el-button type="success" size="large" @click="handleScanPayment">扫码收款</el-button>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="scanPaymentVisible" title="等待扫码支付" width="400px" :show-close="false">
      <div class="scan-payment-content" style="text-align: center; padding: 20px;">
        <el-icon class="is-loading" :size="50" color="#409eff"><Loading /></el-icon>
        <p style="margin-top: 15px">等待顾客扫码中... ({{ countdownSeconds }}s)</p>
      </div>
    </el-dialog>

    <div id="print-area" style="display: none;">
      <div class="receipt-header">
        <h1 class="store-name">超市收银结算单</h1>
        <p class="receipt-subtitle">欢迎光临，谢谢惠顾</p>
      </div>

      <div class="receipt-info">
        <div class="info-item"><span>流水单号：</span><span>{{ currentOrderId }}</span></div>
        <div class="info-item"><span>收银人员：</span><span>{{ cashierName }}</span></div>
        <div class="info-item"><span>结账时间：</span><span>{{ currentDate }}</span></div>
      </div>

      <div class="receipt-divider"></div>

      <div class="receipt-items">
        <div class="item-header">
          <span class="col-name">商品名称</span>
          <span class="col-price">单价</span>
          <span class="col-qty">数量</span>
          <span class="col-total">小计</span>
        </div>
        <div class="item-list">
          <div class="item-row" v-for="(item, index) in printData.items" :key="index">
            <span class="col-name">{{ item.name }}</span>
            <span class="col-price">{{ formatPrice(item.retailPrice) }}</span>
            <span class="col-qty">{{ item.count }}</span>
            <span class="col-total">{{ formatPrice(calculateSubtotal(item)) }}</span>
          </div>
        </div>
      </div>

      <div class="receipt-divider"></div>

      <div class="receipt-summary">
        <div class="summary-item">
          <span>应收总额：</span>
          <span class="amount">¥ {{ formatPrice(printData.totalAmount) }}</span>
        </div>
        <div class="summary-item"><span>实收金额：</span><span>¥ {{ formatPrice(printData.realAmount) }}</span></div>
        <div class="summary-item"><span>找零金额：</span><span>¥ {{ formatPrice(printData.changeAmount) }}</span></div>
        <div class="summary-item"><span>支付方式：</span><span>{{ currentPaymentMethod }}</span></div>
      </div>

      <div class="receipt-footer">
        <p>请妥善保管小票，作为退换货凭证</p>
        <p class="footer-msg">谢谢惠顾，欢迎下次光临！</p>
        <div class="barcode-area">
          <div class="barcode-line"></div>
          <span class="barcode-num">{{ currentOrderId }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, getCurrentInstance } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Search, Delete, CreditCard, Money, Wallet, Coin, Check, RefreshLeft, Loading, RefreshRight } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl || 'http://localhost:8090'
const user = JSON.parse(sessionStorage.getItem('user') || '{}')

const barcode = ref('')
const cartItems = ref([])
const realAmount = ref(0)
const paymentMethod = ref('cash')
const checkingOut = ref(false)
const paymentDialogVisible = ref(false)
const scanPaymentVisible = ref(false)
const countdownSeconds = ref(3)
const currentOrderId = ref('')
const currentPaymentMethod = ref('')
const printData = ref({ items: [], totalAmount: 0, realAmount: 0, changeAmount: 0 })
const refundOrderId = ref('')
const refunding = ref(false)

const cashierName = computed(() => user.name || '收银员')
const currentDate = computed(() => new Date().toLocaleString())
const totalAmount = computed(() => cartItems.value.reduce((sum, item) => sum + (item.retailPrice * item.count), 0))
const changeAmount = computed(() => realAmount.value - totalAmount.value)
const canCheckout = computed(() => cartItems.value.length > 0 && realAmount.value >= totalAmount.value && !checkingOut.value)

const formatPrice = (p) => Number(p || 0).toFixed(2)
const calculateSubtotal = (item) => (item.retailPrice || 0) * (item.count || 0)

const handleBarcodeSearch = async () => {
  if (!barcode.value.trim()) return ElMessage.warning('请输入条码')
  try {
    const res = await axios.get(`${httpUrl}/goods/findByBarcode`, { params: { barcode: barcode.value.trim() } })
    if (res.data.code === 200 && res.data.data) {
      const goods = res.data.data
      const item = cartItems.value.find(i => i.id === goods.id)
      if (item) {
        if (item.count < goods.count) item.count++
        else ElMessage.warning('库存不足')
      } else {
        if (goods.count > 0) cartItems.value.push({ ...goods, count: 1, maxCount: goods.count })
        else ElMessage.error('库存不足')
      }
    } else ElMessage.error(res.data.msg || '未找到商品')
  } catch (e) { ElMessage.error('查询异常') }
  barcode.value = ''
}

const handleQuantityChange = (item) => { if (item.count > item.maxCount) item.count = item.maxCount }
const handleDeleteItem = (idx) => cartItems.value.splice(idx, 1)
const handleClearCart = () => { cartItems.value = []; realAmount.value = 0 }
const showPaymentDialog = () => paymentDialogVisible.value = true
const handleCashPayment = () => { paymentDialogVisible.value = false; processCheckout('cash') }
const handleScanPayment = () => {
  paymentDialogVisible.value = false
  scanPaymentVisible.value = true
  let timer = setInterval(() => {
    countdownSeconds.value--
    if (countdownSeconds.value <= 0) {
      clearInterval(timer)
      scanPaymentVisible.value = false
      processCheckout('scan')
      countdownSeconds.value = 3
    }
  }, 1000)
}

// 核心结账逻辑
const processCheckout = async (type) => {
  checkingOut.value = true
  try {
    const details = cartItems.value.map(i => ({ goodsId: i.id, count: i.count, price: i.retailPrice, subtotal: calculateSubtotal(i) }))
    const data = { userId: user.id, totalAmount: totalAmount.value, realAmount: realAmount.value, changeAmount: changeAmount.value, details }
    const res = await axios.post(`${httpUrl}/sales/checkout`, data)

    if (res.data.code === 200) {
      currentOrderId.value = res.data.data || `ORD${Date.now()}`
      currentPaymentMethod.value = type === 'cash' ? '现金' : '扫码支付'
      printData.value = { items: [...cartItems.value], totalAmount: totalAmount.value, realAmount: realAmount.value, changeAmount: changeAmount.value }

      ElMessage.success('结账成功！正在为您打印小票...')

      setTimeout(() => {
        printReceipt()
        handleClearCart()
      }, 300)
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch (e) {
    ElMessage.error('结算异常')
  }
  checkingOut.value = false
}

// 退款逻辑
const handleRefund = async () => {
  if (!refundOrderId.value.trim()) {
    return ElMessage.warning('请输入销售单流水号')
  }

  try {
    // 二次确认
    await ElMessageBox.confirm(
        `确认对销售单【${refundOrderId.value}】进行退款？退款后将回滚库存。`,
        '退款确认',
        {
          confirmButtonText: '确认退款',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )

    refunding.value = true

    // 调用后端退款接口
    const res = await axios.post(`${httpUrl}/sales/refund`, null, {
      params: { orderNum: refundOrderId.value.trim() }
    })

    if (res.data.code === 200) {
      ElMessage.success('退款成功，库存已回滚')
      refundOrderId.value = ''
    } else {
      ElMessage.error(res.data.msg || '退款失败')
    }
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.msg || '退款异常')
    }
  } finally {
    refunding.value = false
  }
}

// =======================================================================
// 精修版打印逻辑：更紧凑的字号，强制开启背景色渲染（显示条形码）
// =======================================================================
const printReceipt = () => {
  const printContent = document.getElementById('print-area').innerHTML

  const iframe = document.createElement('iframe')
  iframe.style.position = 'absolute'
  iframe.style.width = '80mm'
  iframe.style.height = '100vh'
  iframe.style.left = '-9999px'
  iframe.style.top = '-9999px'
  document.body.appendChild(iframe)

  const doc = iframe.contentWindow.document
  doc.write(`
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="utf-8">
        <title>打印小票</title>
        <style>
          @page { margin: 0; size: auto; }

          body {
            margin: 0;
            padding: 4mm; /* 稍微缩小边距 */
            width: 72mm; /* 控制宽度 */
            box-sizing: border-box;
            font-family: "Microsoft YaHei", "SimHei", sans-serif;
            font-size: 11px; /* 整体字号缩小，显得更紧凑专业 */
            color: #000;
            /* 强制浏览器打印背景色，这是条形码能显示出来的核心！ */
            -webkit-print-color-adjust: exact;
            print-color-adjust: exact;
          }

          /* 排版细节 */
          .store-name { text-align: center; font-size: 16px; font-weight: bold; margin: 0 0 3px 0; }
          .receipt-subtitle { text-align: center; font-size: 11px; margin: 0 0 10px 0; }
          .receipt-info { font-size: 11px; margin-bottom: 8px; }
          .info-item { display: flex; justify-content: space-between; margin-bottom: 3px; }
          .receipt-divider { border-top: 1px dashed #000; margin: 6px 0; }

          /* 商品表格 */
          .receipt-items { width: 100%; font-size: 11px; }
          .item-header, .item-row { display: flex; justify-content: space-between; align-items: center; width: 100%; padding: 3px 0; }
          .item-header { border-bottom: 1px solid #000; font-weight: bold; }

          .col-name { width: 45%; text-align: left; word-break: break-all; }
          .col-price { width: 20%; text-align: right; }
          .col-qty { width: 15%; text-align: center; }
          .col-total { width: 20%; text-align: right; }

          /* 结算信息 */
          .receipt-summary { margin-top: 8px; }
          .summary-item { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 4px; }
          .summary-item:first-child { font-size: 14px; font-weight: bold; border-top: 1px dashed #000; padding-top: 6px; margin-bottom: 6px; }

          /* 底部结尾 */
          .receipt-footer { text-align: center; margin-top: 15px; font-size: 11px; }
          .receipt-footer p { margin: 4px 0; }
          .footer-msg { font-weight: bold; margin: 8px 0; }

          /* 模拟条形码 */
          .barcode-area { margin-top: 15px; display: flex; flex-direction: column; align-items: center; }
          .barcode-line {
            width: 140px; height: 30px;
            background: repeating-linear-gradient(90deg, #000, #000 2px, #fff 2px, #fff 4px, #000 4px, #000 5px, #fff 5px, #fff 8px);
          }
          .barcode-num { font-size: 10px; margin-top: 4px; letter-spacing: 2px; }
        </style>
      </head>
      <body>
        ${printContent}
      </body>
    </html>
  `)
  doc.close()

  setTimeout(() => {
    iframe.contentWindow.focus()
    iframe.contentWindow.print()
    setTimeout(() => {
      document.body.removeChild(iframe)
    }, 1000)
  }, 300)
}
</script>

<style scoped>
/* 页面整体样式 */
.cashier-desk { 
  padding: 24px; 
  background: linear-gradient(135deg, #f5f7fa 0%, #e8edf2 100%);
  min-height: calc(100vh - 60px);
}

/* 卡片通用样式 */
:deep(.el-card) {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

/* 购物车卡片特殊样式 */
.cart-card {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 108px);
}

.cart-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 20px;
}

/* 结算中心卡片样式 */
.checkout-card {
  height: calc(100vh - 108px);
  overflow-y: auto;
}

.checkout-card :deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-card:hover) {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}

:deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 24px;
  border-bottom: none;
  border-radius: 12px 12px 0 0;
}

.card-header { 
  display: flex; 
  align-items: center; 
  gap: 10px; 
  font-weight: 600; 
  font-size: 18px;
}

/* 条码输入区域 */
.barcode-section { 
  margin-bottom: 16px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
  flex-shrink: 0;
}

:deep(.barcode-input .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 表格样式优化 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  flex: 1;
}

:deep(.el-table__header th) {
  background: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
}

.price { 
  color: #f56c6c; 
  font-weight: bold;
  font-size: 14px;
}

.subtotal { 
  color: #f56c6c; 
  font-weight: bold;
  font-size: 15px;
}

.stock-info {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

/* 结算面板样式 */
.checkout-panel { 
  padding: 20px 0;
}

.amount-item { 
  display: flex; 
  flex-direction: column; 
  gap: 12px; 
  margin-bottom: 20px; 
  padding: 20px; 
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.amount-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

.amount-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.amount-value { 
  font-size: 32px; 
  font-weight: bold; 
  text-align: right;
  color: #303133;
}

.total-amount { 
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.total-amount .amount-label,
.total-amount .amount-value {
  color: white;
}

.change-amount { 
  border: 2px solid #67c23a;
  background: #f0f9ff;
}

.change-amount .amount-value { 
  color: #67c23a;
  font-size: 36px;
}

.change-amount .amount-value.insufficient { 
  color: #f56c6c;
}

/* 支付方式样式 */
.payment-method {
  margin: 20px 0;
  padding: 16px;
  background: #fafbfc;
  border-radius: 8px;
}

.payment-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

:deep(.el-radio-group) {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

:deep(.el-radio-button__inner) {
  border-radius: 8px !important;
  padding: 12px 16px;
  font-weight: 500;
}

/* 按钮样式 */
.checkout-button { 
  width: 100%; 
  height: 56px; 
  font-size: 20px; 
  font-weight: bold;
  border-radius: 10px;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  margin-top: 16px;
  transition: all 0.3s ease;
}

.checkout-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
}

.clear-button { 
  width: 100%; 
  margin-top: 12px;
  height: 44px;
  border-radius: 10px;
}

/* 分割线样式 */
:deep(.el-divider) {
  margin: 24px 0;
  background: #e8e8e8;
}

/* 退款区域样式 */
.refund-section { 
  margin-top: 10px; 
  padding: 20px;
  background: #fff7e6;
  border-radius: 10px;
  border: 1px solid #ffd591;
}

.refund-title { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  font-size: 15px; 
  font-weight: 600; 
  margin-bottom: 16px; 
  color: #fa8c16;
}

.refund-button { 
  width: 100%; 
  height: 48px; 
  font-size: 16px; 
  font-weight: bold;
  border-radius: 8px;
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
  border: none;
  color: white;
}

.refund-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(250, 173, 20, 0.3);
}

/* 打印预览区域 */
#print-area {
  font-family: "Microsoft YaHei", "SimHei", sans-serif;
}

/* 响应式优化 */
@media (max-width: 1400px) {
  .cashier-desk {
    padding: 16px;
  }
  
  :deep(.el-radio-group) {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>