<!-- LoginPage.vue -->
<template>
  <div class="login-container">
    <!-- Left Content Section -->
    <div class="left-content">
      <div class="brand">
        <div class="brand-icon">
          <Sparkles :size="16" />
        </div>
        <span>YourBrand</span>
      </div>

      <div class="characters-container">
        <div class="character-scene" style="width: 550px; height: 400px">
          <!-- Purple Character -->
          <div
              ref="purpleRef"
              class="character"
              :style="{
              left: '70px',
              width: '180px',
              height: (isTyping || (password.length > 0 && !showPassword)) ? '440px' : '400px',
              backgroundColor: '#6C3FF5',
              borderRadius: '10px 10px 0 0',
              zIndex: 1,
              transform: (password.length > 0 && showPassword)
                ? 'skewX(0deg)'
                : (isTyping || (password.length > 0 && !showPassword))
                  ? `skewX(${(purplePos.bodySkew || 0) - 12}deg) translateX(40px)`
                  : `skewX(${purplePos.bodySkew || 0}deg)`,
            }"
          >
            <div
                class="character-face"
                :style="{
                left: (password.length > 0 && showPassword) ? `${20}px` : isLookingAtEachOther ? `${55}px` : `${45 + purplePos.faceX}px`,
                top: (password.length > 0 && showPassword) ? `${35}px` : isLookingAtEachOther ? `${65}px` : `${40 + purplePos.faceY}px`,
              }"
            >
              <EyeBall
                  :size="18"
                  :pupil-size="7"
                  :max-distance="5"
                  eye-color="white"
                  pupil-color="#2D2D2D"
                  :is-blinking="isPurpleBlinking"
                  :look-at-password="password.length > 0 && !showPassword"
                  :force-look-x="(password.length > 0 && showPassword) ? (isPurplePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? (isPurplePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : undefined"
              />
              <EyeBall
                  :size="18"
                  :pupil-size="7"
                  :max-distance="5"
                  eye-color="white"
                  pupil-color="#2D2D2D"
                  :is-blinking="isPurpleBlinking"
                  :look-at-password="password.length > 0 && !showPassword"
                  :force-look-x="(password.length > 0 && showPassword) ? (isPurplePeeking ? 4 : -4) : isLookingAtEachOther ? 3 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? (isPurplePeeking ? 5 : -4) : isLookingAtEachOther ? 4 : undefined"
              />
            </div>
          </div>

          <!-- Black Character -->
          <div
              ref="blackRef"
              class="character"
              :style="{
              left: '240px',
              width: '120px',
              height: '310px',
              backgroundColor: '#2D2D2D',
              borderRadius: '8px 8px 0 0',
              zIndex: 2,
              transform: (password.length > 0 && showPassword)
                ? 'skewX(0deg)'
                : isLookingAtEachOther
                  ? `skewX(${(blackPos.bodySkew || 0) * 1.5 + 10}deg) translateX(20px)`
                  : (isTyping || (password.length > 0 && !showPassword))
                    ? `skewX(${(blackPos.bodySkew || 0) * 1.5}deg)`
                    : `skewX(${blackPos.bodySkew || 0}deg)`,
            }"
          >
            <div
                class="character-face"
                :style="{
                left: (password.length > 0 && showPassword) ? `${10}px` : isLookingAtEachOther ? `${32}px` : `${26 + blackPos.faceX}px`,
                top: (password.length > 0 && showPassword) ? `${28}px` : isLookingAtEachOther ? `${12}px` : `${32 + blackPos.faceY}px`,
              }"
            >
              <EyeBall
                  :size="16"
                  :pupil-size="6"
                  :max-distance="4"
                  eye-color="white"
                  pupil-color="#2D2D2D"
                  :is-blinking="isBlackBlinking"
                  :look-at-password="password.length > 0 && !showPassword"
                  :force-look-x="(password.length > 0 && showPassword) ? -4 : isLookingAtEachOther ? 0 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? -4 : isLookingAtEachOther ? -4 : undefined"
              />
              <EyeBall
                  :size="16"
                  :pupil-size="6"
                  :max-distance="4"
                  eye-color="white"
                  pupil-color="#2D2D2D"
                  :is-blinking="isBlackBlinking"
                  :look-at-password="password.length > 0 && !showPassword"
                  :force-look-x="(password.length > 0 && showPassword) ? -4 : isLookingAtEachOther ? 0 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? -4 : isLookingAtEachOther ? -4 : undefined"
              />
            </div>
          </div>

          <!-- Orange Character -->
          <div
              ref="orangeRef"
              class="character"
              :style="{
              left: '0px',
              width: '240px',
              height: '200px',
              zIndex: 3,
              backgroundColor: '#FF9B6B',
              borderRadius: '120px 120px 0 0',
              transform: (password.length > 0 && showPassword) ? 'skewX(0deg)' : `skewX(${orangePos.bodySkew || 0}deg)`,
            }"
          >
            <div
                class="character-face"
                :style="{
                left: (password.length > 0 && showPassword) ? `${50}px` : `${82 + (orangePos.faceX || 0)}px`,
                top: (password.length > 0 && showPassword) ? `${85}px` : `${90 + (orangePos.faceY || 0)}px`,
              }"
            >
              <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :look-at-password="password.length > 0 && !showPassword" :force-look-x="(password.length > 0 && showPassword) ? -5 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -4 : undefined" />
              <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :look-at-password="password.length > 0 && !showPassword" :force-look-x="(password.length > 0 && showPassword) ? -5 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -4 : undefined" />
            </div>
          </div>

          <!-- Yellow Character -->
          <div
              ref="yellowRef"
              class="character"
              :style="{
              left: '310px',
              width: '140px',
              height: '230px',
              backgroundColor: '#E8D754',
              borderRadius: '70px 70px 0 0',
              zIndex: 4,
              transform: (password.length > 0 && showPassword) ? 'skewX(0deg)' : `skewX(${yellowPos.bodySkew || 0}deg)`,
            }"
          >
            <div
                class="character-face"
                :style="{
                left: (password.length > 0 && showPassword) ? `${20}px` : `${52 + (yellowPos.faceX || 0)}px`,
                top: (password.length > 0 && showPassword) ? `${35}px` : `${40 + (yellowPos.faceY || 0)}px`,
              }"
            >
              <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :look-at-password="password.length > 0 && !showPassword" :force-look-x="(password.length > 0 && showPassword) ? -5 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -4 : undefined" />
              <Pupil :size="12" :max-distance="5" pupil-color="#2D2D2D" :look-at-password="password.length > 0 && !showPassword" :force-look-x="(password.length > 0 && showPassword) ? -5 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -4 : undefined" />
            </div>
            <div
                class="character-mouth"
                :style="{
                left: (password.length > 0 && showPassword) ? `${10}px` : `${40 + (yellowPos.faceX || 0)}px`,
                top: (password.length > 0 && showPassword) ? `${88}px` : `${88 + (yellowPos.faceY || 0)}px`,
              }"
            />
          </div>
        </div>
      </div>

      <div class="footer-links">
        <a href="#">Privacy Policy</a>
        <a href="#">Terms of Service</a>
        <a href="#">Contact</a>
      </div>

      <div class="bg-grid"></div>
      <div class="blur-orb-1"></div>
      <div class="blur-orb-2"></div>
    </div>

    <!-- Right Login Section -->
    <div class="right-content">
      <div class="login-wrapper">
        <div class="mobile-brand">
          <div class="mobile-brand-icon">
            <Sparkles :size="16" />
          </div>
          <span>YourBrand</span>
        </div>

        <div class="login-header">
          <h1 class="login-title">Welcome back!</h1>
          <p class="login-subtitle">Please enter your details</p>
        </div>

        <form class="login-form" @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="email" class="form-label">Email</label>
            <Input
                id="email"
                type="email"
                placeholder="anna@gmail.com"
                v-model="email"
                autocomplete="off"
                @focus="isTyping = true"
                @blur="isTyping = false"
                required
                class="form-input"
            />
          </div>

          <div class="form-group">
            <label for="password" class="form-label">Password</label>
            <div class="password-wrapper">
              <Input
                  id="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="••••••••"
                  v-model="password"
                  required
                  class="form-input"
              />
              <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="password-toggle"
              >
                <EyeOff v-if="showPassword" :size="20" />
                <Eye v-else :size="20" />
              </button>
            </div>
          </div>

          <div class="form-options">
            <div class="remember-me">
              <input type="checkbox" id="remember" v-model="remember" class="checkbox" />
              <label for="remember" class="form-label" style="font-weight: normal; cursor: pointer;">
                Remember for 30 days
              </label>
            </div>
            <a href="#" class="forgot-password">
              Forgot password?
            </a>
          </div>

          <div v-if="error" class="error-message">
            {{ error }}
          </div>

          <button
              type="submit"
              class="submit-btn"
              :disabled="isLoading"
              @click="handleSubmit"
          >
            {{ isLoading ? "Signing in..." : "Log in" }}
          </button>
        </form>

        <button
            class="google-btn"
            type="button"
            @click="() => {}"
        >
          <Mail :size="20" />
          Log in with Google
        </button>

        <div class="signup-link">
          Don't have an account?
          <a href="#">
            Sign Up
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, defineComponent, h, watch } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import store from '../srtore'
import { getCurrentInstance } from 'vue'
import { Eye, EyeOff, Mail, Sparkles } from 'lucide-vue-next'

// 子组件 - 使用渲染函数
const Pupil = defineComponent({
  props: {
    size: { type: Number, default: 12 },
    maxDistance: { type: Number, default: 5 },
    pupilColor: { type: String, default: 'black' },
    forceLookX: { type: Number, default: undefined },
    forceLookY: { type: Number, default: undefined },
    lookAtPassword: { type: Boolean, default: false }
  },
  setup(props) {
    const mouseX = ref(0)
    const mouseY = ref(0)
    const pupilRef = ref(null)

    const updateMouse = (e) => {
      mouseX.value = e.clientX
      mouseY.value = e.clientY
    }

    onMounted(() => {
      window.addEventListener('mousemove', updateMouse)
    })
    onUnmounted(() => {
      window.removeEventListener('mousemove', updateMouse)
    })

    const pupilPosition = computed(() => {
      if (!pupilRef.value) return { x: 0, y: 0 }
      if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
        return { x: props.forceLookX, y: props.forceLookY }
      }
      if (props.lookAtPassword) {
        // 看向右侧密码框
        return { x: 8, y: 5 }
      }
      const rect = pupilRef.value.getBoundingClientRect()
      const cx = rect.left + rect.width / 2
      const cy = rect.top + rect.height / 2
      const dx = mouseX.value - cx
      const dy = mouseY.value - cy
      const dist = Math.min(Math.hypot(dx, dy), props.maxDistance)
      const angle = Math.atan2(dy, dx)
      return {
        x: Math.cos(angle) * dist,
        y: Math.sin(angle) * dist,
      }
    })

    return () => h('div', {
      ref: pupilRef,
      class: 'rounded-full',
      style: {
        width: props.size + 'px',
        height: props.size + 'px',
        backgroundColor: props.pupilColor,
        transform: `translate(${pupilPosition.value.x}px, ${pupilPosition.value.y}px)`,
        transition: 'transform 0.1s ease-out',
      }
    })
  },
})

const EyeBall = defineComponent({
  props: {
    size: { type: Number, default: 48 },
    pupilSize: { type: Number, default: 16 },
    maxDistance: { type: Number, default: 10 },
    eyeColor: { type: String, default: 'white' },
    pupilColor: { type: String, default: 'black' },
    isBlinking: { type: Boolean, default: false },
    forceLookX: { type: Number, default: undefined },
    forceLookY: { type: Number, default: undefined },
    lookAtPassword: { type: Boolean, default: false }
  },
  setup(props) {
    const mouseX = ref(0)
    const mouseY = ref(0)
    const eyeRef = ref(null)

    const updateMouse = (e) => {
      mouseX.value = e.clientX
      mouseY.value = e.clientY
    }

    onMounted(() => {
      window.addEventListener('mousemove', updateMouse)
    })
    onUnmounted(() => {
      window.removeEventListener('mousemove', updateMouse)
    })

    const pupilPosition = computed(() => {
      if (!eyeRef.value) return { x: 0, y: 0 }
      if (props.forceLookX !== undefined && props.forceLookY !== undefined) {
        return { x: props.forceLookX, y: props.forceLookY }
      }
      if (props.lookAtPassword) {
        // 看向右侧密码框
        return { x: 10, y: 5 }
      }
      const rect = eyeRef.value.getBoundingClientRect()
      const cx = rect.left + rect.width / 2
      const cy = rect.top + rect.height / 2
      const dx = mouseX.value - cx
      const dy = mouseY.value - cy
      const dist = Math.min(Math.hypot(dx, dy), props.maxDistance)
      const angle = Math.atan2(dy, dx)
      return {
        x: Math.cos(angle) * dist,
        y: Math.sin(angle) * dist,
      }
    })

    return () => h('div', {
      ref: eyeRef,
      class: 'rounded-full flex items-center justify-center transition-all duration-150',
      style: {
        width: props.size + 'px',
        height: props.isBlinking ? '2px' : props.size + 'px',
        backgroundColor: props.eyeColor,
        overflow: 'hidden',
      }
    }, props.isBlinking ? [] : [h('div', {
      class: 'rounded-full',
      style: {
        width: props.pupilSize + 'px',
        height: props.pupilSize + 'px',
        backgroundColor: props.pupilColor,
        transform: `translate(${pupilPosition.value.x}px, ${pupilPosition.value.y}px)`,
        transition: 'transform 0.1s ease-out',
      }
    })])
  },
})

// shadcn 基础组件（使用渲染函数，但实际模板中已改用原生 HTML）
// 保留这些定义以备将来使用
const Button = defineComponent({
  props: ['variant', 'disabled', 'type', 'className'],
  emits: ['click'],
  setup(props, { slots, emit }) {
    const handleClick = () => emit('click')
    const baseClass = 'inline-flex items-center justify-center rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 h-10 px-4 py-2'
    const variantClass = props.variant === 'outline' 
      ? 'border border-input bg-background hover:bg-accent hover:text-accent-foreground' 
      : 'bg-primary text-primary-foreground hover:bg-primary/90'
    
    return () => h('button', {
      class: `${baseClass} ${variantClass}`,
      disabled: props.disabled,
      type: props.type,
      onClick: handleClick
    }, slots.default())
  },
})

const Input = defineComponent({
  props: ['modelValue', 'type', 'placeholder', 'autocomplete', 'required', 'className', 'id'],
  emits: ['update:modelValue', 'focus', 'blur'],
  setup(props, { emit }) {
    const handleInput = (e) => emit('update:modelValue', e.target.value)
    const handleFocus = (e) => emit('focus', e)
    const handleBlur = (e) => emit('blur', e)
    
    return () => h('input', {
      class: 'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
      value: props.modelValue,
      type: props.type,
      placeholder: props.placeholder,
      autocomplete: props.autocomplete,
      required: props.required,
      id: props.id,
      onInput: handleInput,
      onFocus: handleFocus,
      onBlur: handleBlur
    })
  },
})

const Label = defineComponent({
  props: ['for', 'className'],
  setup(props, { slots }) {
    return () => h('label', {
      for: props.for,
      class: 'text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70'
    }, slots.default())
  },
})

const Checkbox = defineComponent({
  props: ['modelValue', 'id'],
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const handleChange = (e) => emit('update:modelValue', e.target.checked)
    
    return () => h('input', {
      type: 'checkbox',
      id: props.id,
      class: 'peer h-4 w-4 rounded-sm border border-primary text-primary ring-offset-background focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
      checked: props.modelValue,
      onChange: handleChange
    })
  },
})

// 主页面状态
const showPassword = ref(false)
const email = ref('')
const password = ref('')
const error = ref('')
const isLoading = ref(false)
const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isTyping = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)
const remember = ref(false)

const { proxy } = getCurrentInstance()
const httpUrl = proxy.$httpUrl
const router = useRouter()

const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)

// 鼠标监听
const updateMouse = (e) => {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
}
onMounted(() => {
  window.addEventListener('mousemove', updateMouse)
})
onUnmounted(() => {
  window.removeEventListener('mousemove', updateMouse)
})

// 眨眼动画
const blink = (setter) => {
  const loop = () => {
    const t = setTimeout(() => {
      setter.value = true
      setTimeout(() => {
        setter.value = false
        loop()
      }, 150)
    }, Math.random() * 4000 + 3000)
    return t
  }
  return loop()
}
onMounted(() => {
  const t1 = blink(isPurpleBlinking)
  const t2 = blink(isBlackBlinking)
  return () => {
    clearTimeout(t1)
    clearTimeout(t2)
  }
})

// 对视动画
let lookTimer = null
watch(isTyping, (val) => {
  if (val) {
    isLookingAtEachOther.value = true
    if (lookTimer) clearTimeout(lookTimer)
    lookTimer = setTimeout(() => {
      isLookingAtEachOther.value = false
    }, 800)
  } else {
    isLookingAtEachOther.value = false
  }
})

// 清理对视定时器
onUnmounted(() => {
  if (lookTimer) clearTimeout(lookTimer)
})

// 偷看动画
let peekTimer = null
watch([password, showPassword], ([pw, show]) => {
  if (pw && show) {
    if (peekTimer) clearTimeout(peekTimer)
    const peek = () => {
      peekTimer = setTimeout(() => {
        isPurplePeeking.value = true
        setTimeout(() => {
          isPurplePeeking.value = false
        }, 800)
      }, Math.random() * 3000 + 2000)
      return peekTimer
    }
    peek()
  } else {
    isPurplePeeking.value = false
  }
})

// 清理偷看定时器
onUnmounted(() => {
  if (peekTimer) clearTimeout(peekTimer)
})

// 位置计算
const calc = (ref) => {
  if (!ref.value) return { faceX: 0, faceY: 0, bodySkew: 0 }
  const r = ref.value.getBoundingClientRect()
  const cx = r.left + r.width / 2
  const cy = r.top + r.height / 3
  const dx = mouseX.value - cx
  const dy = mouseY.value - cy
  return {
    faceX: Math.max(-15, Math.min(15, dx / 20)),
    faceY: Math.max(-10, Math.min(10, dy / 30)),
    bodySkew: Math.max(-6, Math.min(6, -dx / 120)),
  }
}

const purplePos = computed(() => calc(purpleRef))
const blackPos = computed(() => calc(blackRef))
const yellowPos = computed(() => calc(yellowRef))
const orangePos = computed(() => calc(orangeRef))

// 提交
const handleSubmit = async () => {
  error.value = ''
  if (!email.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }
  
  isLoading.value = true
  
  try {
    const loginData = {
      no: email.value,
      password: password.value
    }
    
    const res = await axios.post(httpUrl + '/user/login', loginData)
    console.log('登录结果:', res)
    console.log('响应数据:', res.data)
    
    if (res.data && res.data.code === 200) {
      if (proxy.$message) {
        proxy.$message.success('登录成功!')
      }
      // 保存用户信息到 sessionStorage
      sessionStorage.setItem('user', JSON.stringify(res.data.data.user))
      console.log('菜单数据:', res.data.data.menus)
      console.log('保存前的 store menu:', store.state.menu)
      store.commit('setMenu', res.data.data.menus)
      console.log('保存后的 store menu:', store.state.menu)
      // 跳转到主页
      router.push('/Index')
    } else {
      error.value = res.data?.msg || '登录失败，请检查账号密码'
      isLoading.value = false
    }
  } catch (error) {
    console.error('登录失败:', error)
    error.value = '登录失败：' + (error.message || '网络错误')
    isLoading.value = false
  }
}
</script>

<style scoped>
/* 基础变量定义 */
.login-container {
  --background: #ffffff;
  --foreground: #020817;
  --primary: #7c3aed;
  --primary-foreground: #ffffff;
  --muted: #64748b;
  --muted-foreground: #64748b;
  --border: #e2e8f0;
  --input: #e2e8f0;
  --ring: #7c3aed;
  --radius: 0.5rem;
}

/* 主容器 */
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

@media (min-width: 1024px) {
  .login-container {
    flex-direction: row;
  }
}

/* 左侧内容区 */
.left-content {
  position: relative;
  display: none;
  flex-direction: column;
  justify-content: space-between;
  background: #1a1a1a;
  padding: 3rem;
  color: rgba(255, 255, 255, 0.8);
  min-height: 100vh;
  width: 50%;
}

@media (min-width: 1024px) {
  .left-content {
    display: flex;
  }
}

/* 品牌标识 */
.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  position: relative;
  z-index: 20;
  margin-bottom: auto;
}

.brand-icon {
  width: 2rem;
  height: 2rem;
  border-radius: 0.5rem;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 角色动画容器 */
.characters-container {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 500px;
}

.character-scene {
  position: relative;
  width: 550px;
  height: 400px;
}

/* 右侧登录区 */
.right-content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: #ffffff;
  width: 50%;
  min-height: 100vh;
}

.login-wrapper {
  width: 100%;
  max-width: 420px;
}

/* 移动端品牌 */
.mobile-brand {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
  margin-bottom: 3rem;
}

@media (min-width: 1024px) {
  .mobile-brand {
    display: none;
  }
}

.mobile-brand-icon {
  width: 2rem;
  height: 2rem;
  border-radius: 0.5rem;
  background: rgba(124, 58, 237, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.mobile-brand-icon svg {
  width: 1rem;
  height: 1rem;
  color: var(--primary);
}

/* 标题区 */
.login-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.login-title {
  font-size: 1.875rem;
  font-weight: 700;
  letter-spacing: -0.025em;
  margin-bottom: 0.5rem;
}

.login-subtitle {
  font-size: 0.875rem;
  color: var(--muted-foreground);
}

/* 表单样式 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 500;
}

.form-input {
  height: 3rem;
  background: var(--background);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 0 0.75rem;
  font-size: 0.875rem;
  outline: none;
  transition: border-color 0.2s;
}

.form-input:focus {
  border-color: var(--primary);
}

.form-input::placeholder {
  color: var(--muted-foreground);
}

/* 密码输入框 */
.password-wrapper {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--muted-foreground);
  background: none;
  border: none;
  cursor: pointer;
  transition: color 0.2s;
}

.password-toggle:hover {
  color: var(--foreground);
}

.password-toggle svg {
  width: 1.25rem;
  height: 1.25rem;
}

/* 表单选项 */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.checkbox {
  width: 1rem;
  height: 1rem;
  border-radius: 0.25rem;
  border: 2px solid var(--primary);
  cursor: pointer;
}

.forgot-password {
  font-size: 0.875rem;
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.forgot-password:hover {
  text-decoration: underline;
}

/* 错误提示 */
.error-message {
  padding: 0.75rem;
  font-size: 0.875rem;
  color: #f87171;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 0.5rem;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 3rem;
  font-size: 1rem;
  font-weight: 500;
  background: var(--primary);
  color: var(--primary-foreground);
  border: none;
  border-radius: var(--radius);
  cursor: pointer;
  transition: opacity 0.2s;
}

.submit-btn:hover {
  opacity: 0.9;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Google 登录按钮 */
.google-btn {
  width: 100%;
  height: 3rem;
  background: var(--background);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 1.5rem;
}

.google-btn:hover {
  background: #f1f5f9;
}

.google-btn svg {
  width: 1.25rem;
  height: 1.25rem;
}

/* 注册链接 */
.signup-link {
  text-align: center;
  font-size: 0.875rem;
  color: var(--muted-foreground);
  margin-top: 2rem;
}

.signup-link a {
  color: var(--foreground);
  font-weight: 500;
  text-decoration: none;
}

.signup-link a:hover {
  text-decoration: underline;
}

/* 底部链接 */
.footer-links {
  position: relative;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 2rem;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 2rem;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.6);
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: white;
}

/* 背景装饰 */
.bg-grid {
  position: absolute;
  inset: 0;
  background-image: none;
  opacity: 0;
}

.blur-orb-1 {
  position: absolute;
  top: 25%;
  right: 25%;
  width: 16rem;
  height: 16rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  filter: blur(48px);
  opacity: 0;
}

.blur-orb-2 {
  position: absolute;
  bottom: 25%;
  left: 25%;
  width: 24rem;
  height: 24rem;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 50%;
  filter: blur(48px);
  opacity: 0;
}

/* 眼睛组件样式 */
.eye-ball {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  overflow: hidden;
}

.pupil {
  border-radius: 50%;
  transition: transform 0.1s ease-out;
  display: block;
}

.rounded-full {
  border-radius: 9999px;
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.justify-center {
  justify-content: center;
}

/* 角色动画样式 */
.character {
  position: absolute;
  bottom: 0;
  transition: all 0.7s ease-in-out;
  will-change: transform;
  transform-origin: bottom center;
  display: block;
}

.character-face {
  position: absolute;
  display: flex;
  gap: 2rem;
  transition: all 0.7s ease-in-out;
  will-change: transform;
}

.eye-container {
  display: flex;
  gap: 0.5rem;
}

.pupil {
  border-radius: 50%;
  transition: transform 0.1s ease-out;
  will-change: transform;
  display: block;
}

.character-mouth {
  position: absolute;
  width: 20px;
  height: 4px;
  background-color: #2D2D2D;
  border-radius: 9999px;
  transition: all 0.2s ease-out;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .left-content {
    display: none;
  }
  
  .right-content {
    padding: 1rem;
  }
  
  .login-wrapper {
    max-width: 100%;
  }
}

/* 修复角色定位 */
.character-scene {
  position: relative;
  width: 550px;
  height: 400px;
  margin: 0 auto;
}

/* 确保角色正确定位 */
.characters-container {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  height: 500px;
  overflow: hidden;
  flex: 1;
  margin: 2rem 0;
}
</style>