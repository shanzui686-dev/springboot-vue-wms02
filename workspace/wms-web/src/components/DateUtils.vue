<template>
  <div style="text-align: center; font-size: 24px; color: #67C23A;">
    <span class="time" id="time">
      今天是：<span class="date">{{ nowTime }}</span>
      <span class="hour" style="margin: 0 5px;">{{ time.hour }}</span>
      <a class="split">:</a>
      <span class="minitus">{{ time.minitus }}</span>
      <a class="split">:</a>
      <span class="seconds">{{ time.seconds }}</span>
    </span>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const nowTime = ref('')
const time = ref({
  hour: '',
  minitus: '',
  seconds: ''
})

let timer = null

const updateTime = () => {
  const now = new Date()
  nowTime.value = now.toLocaleDateString()
  time.value = {
    hour: String(now.getHours()).padStart(2, '0'),
    minitus: String(now.getMinutes()).padStart(2, '0'),
    seconds: String(now.getSeconds()).padStart(2, '0')
  }
}

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>

<style scoped>
.time {
  font-weight: bold;
}

.date {
  color: #409EFF;
  margin-right: 10px;
}

.hour {
  color: #F56C6C;
  font-size: 28px;
}

.minitus {
  color: #E6A23C;
  font-size: 28px;
}

.seconds {
  color: #67C23A;
  font-size: 28px;
}

.split {
  color: #909399;
  margin: 0 3px;
  text-decoration: none;
}
</style>
