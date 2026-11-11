<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, CircleCheck } from '@element-plus/icons-vue'
import IconSvg from '@/components/icon-svg.vue'
import { useAuthStore } from '@/stores/auth'
import { useBrandStore } from '@/stores/brand'
import { validateForm } from '@/utils/form'
import { getCaptchaUrl, getUUID } from '@/utils/tool'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const brandStore = useBrandStore()
const formRef = ref()
const loading = ref(false)
const captchaUrl = ref('')
const DEFAULT_VIEWPORT = 'width=1600, minimum-scale=0.1, maximum-scale=10, user-scalable=yes, viewport-fit=cover'
const LOGIN_VIEWPORT = 'width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover'

const form = reactive({
  username: '',
  password: '',
  captcha: '',
  uuid: ''
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const features = [
  { icon: 'xiangqufill', title: '目的地与景区', desc: '全域资源统筹与内容运营' },
  { icon: 'ordercenter', title: '商户与订单', desc: '交易全链路管理与对账' },
  { icon: 'editor', title: '非遗与活动', desc: '文化内容与营销联动' },
]

const modules = ['景区管理', '酒店预订', '非遗商城', '活动营销', '订单中心', '数据看板']

const securities = ['加密传输', '权限管控', '操作审计']

function getRedirectTarget() {
  const redirect = Array.isArray(route.query.redirect) ? route.query.redirect[0] : route.query.redirect
  if (!redirect || typeof redirect !== 'string' || redirect.startsWith('/login')) {
    return '/dashboard'
  }
  return redirect
}

function refreshCaptcha() {
  form.uuid = getUUID()
  captchaUrl.value = getCaptchaUrl(form.uuid)
}

function setViewport(content) {
  if (typeof document === 'undefined') return
  document.querySelector('meta[name="viewport"]')?.setAttribute('content', content)
}

async function blurActiveField() {
  if (typeof document === 'undefined') return
  const activeElement = document.activeElement
  if (activeElement instanceof HTMLElement) {
    activeElement.blur()
  }
  await nextTick()
}

async function handleLogin() {
  if (loading.value) return
  const valid = await validateForm(formRef)
  if (!valid) return

  loading.value = true
  try {
    await authStore.login(form)
    await blurActiveField()
    ElMessage.success('登录成功')
    window.location.replace(router.resolve(getRedirectTarget()).href)
  } catch (error) {
    ElMessage.error(error.msg || error.message || '登录失败')
    refreshCaptcha()
    form.captcha = ''
  } finally {
    loading.value = false
  }
}

function syncDocumentTitle() {
  if (typeof document === 'undefined') return
  document.title = `登录 | ${brandStore.adminSuffix}`
}

onMounted(async () => {
  setViewport(LOGIN_VIEWPORT)
  refreshCaptcha()
  await brandStore.load()
  syncDocumentTitle()
})

onBeforeUnmount(() => {
  setViewport(DEFAULT_VIEWPORT)
})
</script>

<template>
  <div class="ry-login">
    <div class="ry-login__mesh" aria-hidden="true" />
    <div class="ry-login__grid" aria-hidden="true" />
    <div class="ry-login__glow ry-login__glow--a" aria-hidden="true" />
    <div class="ry-login__glow ry-login__glow--b" aria-hidden="true" />

    <div class="ry-login__shell">
      <div class="ry-login__panel">
        <div class="ry-login__panel-noise" aria-hidden="true" />
        <div class="ry-login__panel-shapes" aria-hidden="true">
          <span class="ry-login__shape ry-login__shape--ring" />
          <span class="ry-login__shape ry-login__shape--dot" />
          <span class="ry-login__shape ry-login__shape--line" />
        </div>

        <aside class="ry-login__brand">
          <div class="ry-login__brand-scene" aria-hidden="true">
            <div class="ry-login__scene-mountain" />
            <div class="ry-login__scene-tower" />
            <div class="ry-login__scene-cloud ry-login__scene-cloud--a" />
            <div class="ry-login__scene-cloud ry-login__scene-cloud--b" />
          </div>

          <div class="ry-login__brand-inner">
            <div class="ry-login__brand-top">
              <div class="ry-login__tag">
                <span class="ry-login__tag-dot" />
                <span>智 · 游 · {{ brandStore.brandName }}</span>
              </div>
              <div class="ry-login__brand-heading">
                <div class="ry-login__logo-wrap">
                  <span class="ry-login__logo-ring" aria-hidden="true" />
                  <div class="ry-login__logo">{{ brandStore.logoText }}</div>
                </div>
                <div class="ry-login__brand-copy">
                  <h1 class="ry-login__title">{{ brandStore.adminTitle }}</h1>
                  <p class="ry-login__subtitle">数智文旅运营中枢，连接目的地、商户与用户</p>
                </div>
              </div>
            </div>

            <ul class="ry-login__features">
              <li v-for="(item, index) in features" :key="item.title" :style="{ '--delay': `${index * 0.08}s` }">
                <span class="ry-login__features-icon">
                  <IconSvg :name="item.icon" />
                </span>
                <div class="ry-login__features-text">
                  <strong>{{ item.title }}</strong>
                  <span>{{ item.desc }}</span>
                </div>
              </li>
            </ul>

            <div class="ry-login__modules">
              <span v-for="name in modules" :key="name" class="ry-login__module">{{ name }}</span>
            </div>
          </div>
        </aside>

        <div class="ry-login__divider" aria-hidden="true" />

        <main class="ry-login__main">
          <div class="ry-login__main-deco" aria-hidden="true">
            <span class="ry-login__main-ring" />
            <span class="ry-login__main-dots" />
          </div>

          <div class="ry-login__form-wrap">
            <div class="ry-login__form-head">
              <div class="ry-login__form-head-main">
                <strong>账号登录</strong>
                <span>欢迎回来，请登录管理账号</span>
              </div>
              <div class="ry-login__secure" title="安全加密传输">
                <el-icon><CircleCheck /></el-icon>
                <span>安全登录</span>
              </div>
            </div>

            <div class="ry-login__steps" aria-hidden="true">
              <span class="is-active">① 输入账号</span>
              <span class="ry-login__steps-line" />
              <span>② 验证身份</span>
              <span class="ry-login__steps-line" />
              <span>③ 进入后台</span>
            </div>

            <div class="ry-login__form-box">
          <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="ry-login__form" @submit.prevent>
            <el-form-item label="账号" prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入管理员账号"
                clearable
                size="large"
                :prefix-icon="User"
                class="ry-login__input"
              />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                show-password
                placeholder="请输入登录密码"
                size="large"
                :prefix-icon="Lock"
                class="ry-login__input"
              />
            </el-form-item>

            <el-form-item label="验证码" prop="captcha">
              <div class="ry-login__captcha">
                <el-input
                  v-model="form.captcha"
                  placeholder="请输入验证码"
                  clearable
                  size="large"
                  :prefix-icon="Key"
                  class="ry-login__input"
                  @keyup.enter="handleLogin"
                />
                <button type="button" class="ry-login__captcha-btn" title="点击刷新验证码" @click="refreshCaptcha">
                  <img :src="captchaUrl" alt="验证码" />
                </button>
              </div>
            </el-form-item>

            <button
              type="button"
              class="ry-login__submit"
              :class="{ 'is-loading': loading }"
              :disabled="loading"
              @click="handleLogin"
            >
              <span class="ry-login__submit-text">{{ loading ? '登录中…' : '进入系统' }}</span>
              <span v-if="!loading" class="ry-login__submit-arrow">→</span>
            </button>
          </el-form>
            </div>

            <ul class="ry-login__securities">
              <li v-for="item in securities" :key="item">
                <span class="ry-login__securities-dot" />
                {{ item }}
              </li>
            </ul>

            <p class="ry-login__hint">仅限授权管理员访问，请妥善保管账号信息</p>
          </div>

          <p class="ry-login__foot">© {{ brandStore.brandName }} · 文旅数字化管理平台</p>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.ry-login {
  --ry-brand: var(--brand, #ff6600);
  --ry-brand-light: #ff8a3d;
  --ry-brand-soft: rgba(255, 102, 0, 0.12);
  --ry-ink: #f5f0eb;
  --ry-muted: rgba(255, 220, 190, 0.55);
  --ry-field-bg: rgba(255, 255, 255, 0.06);
  --ry-field-border: rgba(255, 255, 255, 0.1);
  --ry-brand-panel: linear-gradient(152deg, #1c1410 0%, #2a1812 42%, #1a2438 100%);

  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background: #121018;
}

.ry-login__mesh {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 60% 50% at 20% 30%, rgba(255, 102, 0, 0.14) 0%, transparent 55%),
    radial-gradient(ellipse 50% 40% at 80% 70%, rgba(41, 121, 255, 0.08) 0%, transparent 50%);
  animation: ry-mesh-shift 18s ease-in-out infinite alternate;
  pointer-events: none;
}

.ry-login__grid {
  position: absolute;
  inset: 0;
  opacity: 0.18;
  background-image:
    linear-gradient(rgba(255, 138, 61, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 138, 61, 0.06) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 90% 80% at 50% 50%, #000 20%, transparent 75%);
  pointer-events: none;
}

.ry-login__glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  pointer-events: none;
}

.ry-login__glow--a {
  top: -80px;
  right: 10%;
  width: 360px;
  height: 360px;
  background: var(--login-glow-a, rgba(255, 122, 42, 0.22));
  animation: ry-glow-drift 12s ease-in-out infinite;
}

.ry-login__glow--b {
  bottom: -60px;
  left: 35%;
  width: 280px;
  height: 280px;
  background: var(--login-glow-b, rgba(41, 121, 255, 0.14));
  animation: ry-glow-drift 14s ease-in-out infinite reverse;
}

.ry-login__shell {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: clamp(12px, 2vw, 24px);
}

.ry-login__panel {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) 1px minmax(0, 0.85fr);
  width: min(1280px, calc(100vw - 32px));
  min-height: min(680px, calc(100vh - 32px));
  border-radius: 24px;
  overflow: hidden;
  background: var(--ry-brand-panel);
  box-shadow:
    0 32px 80px rgba(0, 0, 0, 0.45),
    0 0 0 1px rgba(255, 138, 61, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

.ry-login__panel-noise {
  position: absolute;
  inset: 0;
  opacity: 0.04;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)'/%3E%3C/svg%3E");
  pointer-events: none;
}

.ry-login__panel-shapes {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ry-login__brand {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.ry-login__brand-scene {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 120px;
  opacity: 0.55;
  pointer-events: none;
}

.ry-login__divider {
  align-self: stretch;
  background: linear-gradient(
    180deg,
    transparent 0%,
    rgba(255, 138, 61, 0.35) 20%,
    rgba(255, 138, 61, 0.35) 80%,
    transparent 100%
  );
}

.ry-login__shape {
  position: absolute;
  border-radius: 50%;
}

.ry-login__shape--ring {
  top: 12%;
  right: 8%;
  width: 120px;
  height: 120px;
  border: 1px solid rgba(255, 138, 61, 0.2);
  animation: ry-spin 24s linear infinite;
}

.ry-login__shape--dot {
  bottom: 22%;
  left: 10%;
  width: 6px;
  height: 6px;
  background: rgba(255, 138, 61, 0.6);
  box-shadow:
    40px 60px 0 rgba(255, 194, 140, 0.35),
    180px -30px 0 rgba(255, 138, 61, 0.25),
    320px 80px 0 rgba(255, 194, 140, 0.2);
}

.ry-login__shape--line {
  bottom: 35%;
  right: 15%;
  width: 80px;
  height: 1px;
  border-radius: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 138, 61, 0.4), transparent);
  transform: rotate(-25deg);
}

.ry-login__brand-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  padding: clamp(24px, 3vw, 36px) clamp(24px, 3vw, 36px) clamp(20px, 2.5vw, 28px);
  animation: ry-fade-up 0.7s ease both;
}

.ry-login__brand-top {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.ry-login__brand-heading {
  display: flex;
  align-items: center;
  gap: 18px;
}

.ry-login__brand-copy {
  min-width: 0;
  text-align: left;
}

.ry-login__tag {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 138, 61, 0.28);
  backdrop-filter: blur(8px);
  font-size: 13px;
  letter-spacing: 3px;
  color: rgba(255, 194, 140, 0.95);
  font-weight: 500;
}

.ry-login__tag-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb066, var(--ry-brand));
  box-shadow: 0 0 10px rgba(255, 102, 0, 0.6);
  animation: ry-pulse 2.4s ease-in-out infinite;
}

.ry-login__logo-wrap {
  position: relative;
  flex-shrink: 0;
}

.ry-login__logo-ring {
  position: absolute;
  inset: -14px;
  border-radius: 50%;
  border: 1px dashed rgba(255, 138, 61, 0.35);
  animation: ry-spin 20s linear infinite reverse;
}

.ry-login__logo {
  display: grid;
  place-items: center;
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: var(--logo-gradient, linear-gradient(145deg, #ff8a3d 0%, #ff6600 100%));
  color: #fff;
  font-size: 34px;
  font-weight: 800;
  box-shadow:
    0 16px 40px rgba(255, 102, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.25);
}

.ry-login__title {
  margin: 0;
  font-size: clamp(1.35rem, 2.2vw, 1.85rem);
  font-weight: 800;
  letter-spacing: 2px;
  color: #fff;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.25);
  white-space: nowrap;
}

.ry-login__subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.65;
  color: rgba(255, 220, 190, 0.65);
  letter-spacing: 0.3px;
}

.ry-login__features {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  width: 100%;
  margin: 0;
  padding: 0;
  list-style: none;
}

.ry-login__features li {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  padding: 18px 16px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(6px);
  text-align: left;
  transition: background 0.25s ease, border-color 0.25s ease, transform 0.25s ease;
  animation: ry-fade-up 0.6s ease both;
  animation-delay: calc(0.15s + var(--delay, 0s));
}

.ry-login__features li:hover {
  background: rgba(255, 138, 61, 0.1);
  border-color: rgba(255, 138, 61, 0.22);
  transform: translateY(-2px);
}

.ry-login__features-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: rgba(255, 102, 0, 0.18);
  color: #ffb066;
}

.ry-login__features-icon :deep(.icon-svg) {
  display: inline-flex;
  width: 22px;
  height: 22px;
}

.ry-login__features-icon :deep(.icon-svg__svg) {
  width: 100%;
  height: 100%;
  fill: currentColor;
}

.ry-login__features-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.ry-login__features-text strong {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
}

.ry-login__features-text span {
  font-size: 12px;
  line-height: 1.5;
  color: rgba(255, 220, 190, 0.55);
}

.ry-login__modules {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: auto;
  padding-top: 8px;
}

.ry-login__module {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 40px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 138, 61, 0.08);
  border: 1px solid rgba(255, 138, 61, 0.16);
  font-size: 13px;
  color: rgba(255, 194, 140, 0.82);
  letter-spacing: 0.5px;
  text-align: center;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.ry-login__module:hover {
  background: rgba(255, 138, 61, 0.14);
  border-color: rgba(255, 138, 61, 0.28);
}

.ry-login__scene-mountain {
  position: absolute;
  bottom: 0;
  left: 10%;
  width: 320px;
  height: 80px;
  transform: none;
  background: linear-gradient(160deg, transparent 42%, rgba(255, 138, 61, 0.15) 42%, rgba(255, 102, 0, 0.22) 100%);
  clip-path: polygon(0 100%, 18% 42%, 34% 68%, 50% 28%, 66% 58%, 82% 36%, 100% 100%);
}

.ry-login__scene-tower {
  position: absolute;
  bottom: 18px;
  left: 28%;
  width: 28px;
  height: 64px;
  transform: none;
  border-radius: 4px 4px 2px 2px;
  background: linear-gradient(180deg, #ff8a3d 0%, #ff6600 100%);
  box-shadow: 0 8px 24px rgba(255, 102, 0, 0.3);
}

.ry-login__scene-tower::before,
.ry-login__scene-tower::after {
  content: '';
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  background: inherit;
  border-radius: 2px;
}

.ry-login__scene-tower::before {
  top: -10px;
  width: 32px;
  height: 10px;
}

.ry-login__scene-tower::after {
  top: -18px;
  width: 40px;
  height: 8px;
}

.ry-login__scene-cloud {
  position: absolute;
  width: 48px;
  height: 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
}

.ry-login__scene-cloud::before,
.ry-login__scene-cloud::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: inherit;
}

.ry-login__scene-cloud::before {
  width: 20px;
  height: 20px;
  top: -8px;
  left: 6px;
}

.ry-login__scene-cloud::after {
  width: 14px;
  height: 14px;
  top: -5px;
  right: 8px;
}

.ry-login__scene-cloud--a {
  top: 16px;
  left: 10%;
  animation: ry-float 6s ease-in-out infinite;
}

.ry-login__scene-cloud--b {
  top: 6px;
  right: 8%;
  animation: ry-float 7.5s ease-in-out infinite reverse;
}

.ry-login__main {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: center;
  padding: clamp(24px, 3vw, 36px) clamp(24px, 3vw, 36px);
  animation: ry-fade-up 0.7s ease 0.1s both;
  overflow: hidden;
}

.ry-login__main-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.ry-login__main-ring {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 160px;
  height: 160px;
  border: 1px solid rgba(255, 138, 61, 0.12);
  border-radius: 50%;
  animation: ry-spin 30s linear infinite;
}

.ry-login__main-dots {
  position: absolute;
  bottom: 80px;
  right: 24px;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 138, 61, 0.4);
  box-shadow:
    0 24px 0 rgba(255, 194, 140, 0.25),
    -20px 40px 0 rgba(255, 138, 61, 0.2),
    30px 60px 0 rgba(255, 194, 140, 0.15);
}

.ry-login__form-wrap {
  position: relative;
  z-index: 1;
  width: 100%;
}

.ry-login__form-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.ry-login__steps {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 18px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 11px;
  color: rgba(255, 220, 190, 0.45);
  letter-spacing: 0.3px;
}

.ry-login__steps .is-active {
  color: #ffb066;
  font-weight: 600;
}

.ry-login__steps-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, rgba(255, 138, 61, 0.3), rgba(255, 138, 61, 0.08));
}

.ry-login__form-box {
  padding: 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(8px);
}

.ry-login__form-head-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ry-login__form-head-main strong {
  font-size: 24px;
  color: var(--ry-ink);
  letter-spacing: 2px;
}

.ry-login__form-head-main span {
  font-size: 14px;
  color: var(--ry-muted);
}

.ry-login__secure {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 102, 0, 0.12);
  border: 1px solid rgba(255, 138, 61, 0.28);
  font-size: 12px;
  color: #ffb066;
}

.ry-login__secure .el-icon {
  font-size: 14px;
}

.ry-login__form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.ry-login__form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.ry-login__form :deep(.el-form-item__label) {
  color: rgba(255, 220, 190, 0.85);
  font-weight: 500;
  padding-bottom: 8px;
  letter-spacing: 0.5px;
}

.ry-login__form :deep(.el-form-item__label::before) {
  color: #ff8a3d !important;
}

.ry-login__form :deep(.el-input__wrapper) {
  min-height: 50px;
  padding: 0 16px;
  border-radius: 14px;
  background: var(--ry-field-bg);
  box-shadow: 0 0 0 1px var(--ry-field-border) inset;
  transition: box-shadow 0.2s ease, background 0.2s ease;
}

.ry-login__form :deep(.el-input__wrapper:hover) {
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 1px rgba(255, 138, 61, 0.25) inset;
}

.ry-login__form :deep(.el-input__wrapper.is-focus) {
  background: rgba(255, 255, 255, 0.1);
  box-shadow:
    0 0 0 1px rgba(255, 138, 61, 0.5) inset,
    0 0 0 3px rgba(255, 102, 0, 0.12);
}

.ry-login__form :deep(.el-input__inner) {
  color: rgba(255, 255, 255, 0.92);
}

.ry-login__form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 220, 190, 0.35);
}

.ry-login__form :deep(.el-input__prefix .el-icon),
.ry-login__form :deep(.el-input__suffix .el-icon) {
  color: #ffb066;
  font-size: 18px;
}

.ry-login__form :deep(.el-input__clear),
.ry-login__form :deep(.el-input__password) {
  color: rgba(255, 194, 140, 0.6);
}

.ry-login__form :deep(.el-form-item__error) {
  color: #ff9a6b;
  padding-top: 4px;
}

.ry-login__captcha {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 140px;
  gap: 12px;
  width: 100%;
  align-items: stretch;
}

.ry-login__captcha-btn {
  flex-shrink: 0;
  width: 140px;
  height: 50px;
  padding: 0;
  border: none;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 0 0 1px rgba(255, 138, 61, 0.2) inset;
  transition: box-shadow 0.2s ease, transform 0.15s ease;
}

.ry-login__captcha-btn:hover {
  box-shadow: 0 0 0 1px rgba(255, 138, 61, 0.45) inset, 0 4px 16px rgba(255, 102, 0, 0.15);
  transform: scale(1.02);
}

.ry-login__captcha-btn img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: rgba(255, 255, 255, 0.95);
}

.ry-login__securities {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
}

.ry-login__securities li {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: rgba(255, 220, 190, 0.45);
  letter-spacing: 0.5px;
}

.ry-login__securities-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffb066, var(--ry-brand));
  box-shadow: 0 0 6px rgba(255, 102, 0, 0.4);
}

.ry-login__submit {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  height: 50px;
  margin-top: 6px;
  border: none;
  border-radius: 999px;
  background: var(--logo-gradient, linear-gradient(135deg, #ff8a3d 0%, #ff6600 100%));
  box-shadow: 0 16px 40px rgba(255, 102, 0, 0.28);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 2px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: transform 0.15s ease, box-shadow 0.15s ease, opacity 0.15s ease;
}

.ry-login__submit::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 60%;
  height: 100%;
  background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: ry-shimmer 3s ease-in-out infinite;
}

.ry-login__submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 20px 48px rgba(255, 102, 0, 0.36);
}

.ry-login__submit:active:not(:disabled) {
  transform: translateY(0);
}

.ry-login__submit:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.ry-login__submit-text {
  position: relative;
  z-index: 1;
}

.ry-login__submit-arrow {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  font-size: 14px;
  transition: transform 0.2s ease;
}

.ry-login__submit:hover:not(:disabled) .ry-login__submit-arrow {
  transform: translateX(3px);
}

.ry-login__hint {
  margin: 14px 0 0;
  text-align: center;
  font-size: 11px;
  color: rgba(255, 220, 190, 0.38);
  line-height: 1.5;
}

.ry-login__foot {
  position: relative;
  z-index: 1;
  margin: 16px 0 0;
  text-align: center;
  font-size: 11px;
  color: rgba(255, 220, 190, 0.32);
  letter-spacing: 0.5px;
}

@keyframes ry-fade-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes ry-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes ry-pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(0.85);
  }
}

@keyframes ry-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes ry-mesh-shift {
  0% {
    transform: scale(1) translate(0, 0);
  }
  100% {
    transform: scale(1.05) translate(2%, -1%);
  }
}

@keyframes ry-glow-drift {
  0%,
  100% {
    transform: translate(0, 0);
  }
  50% {
    transform: translate(20px, -15px);
  }
}

@keyframes ry-shimmer {
  0% {
    left: -100%;
  }
  50%,
  100% {
    left: 150%;
  }
}

@media (max-width: 1100px) {
  .ry-login__features {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 992px) {
  .ry-login__panel {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1px auto;
    min-height: auto;
  }

  .ry-login__divider {
    height: 1px;
    width: auto;
    margin: 0 clamp(20px, 4vw, 32px);
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 138, 61, 0.35) 30%,
      rgba(255, 138, 61, 0.35) 70%,
      transparent 100%
    );
  }

  .ry-login__brand-scene {
    display: none;
  }

  .ry-login__brand-inner {
    padding: 24px 24px 20px;
  }

  .ry-login__features,
  .ry-login__modules {
    display: none;
  }

  .ry-login__brand-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .ry-login__title {
    white-space: normal;
    letter-spacing: 2px;
    font-size: 1.4rem;
  }

  .ry-login__main {
    padding: 20px 24px 28px;
  }

  .ry-login__steps {
    display: none;
  }

  .ry-login__captcha {
    grid-template-columns: 1fr;
  }

  .ry-login__captcha-btn {
    width: 100%;
    height: 50px;
  }
}

@media (max-width: 480px) {
  .ry-login__secure span {
    display: none;
  }

  .ry-login__form-box {
    padding: 16px;
  }
}
</style>
