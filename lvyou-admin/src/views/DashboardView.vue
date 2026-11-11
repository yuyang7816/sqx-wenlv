<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import IconSvg from '@/components/icon-svg.vue'
import { getStatsOverview, getStatsTrend } from '@/api/modules/stats'
import { useBrandStore } from '@/stores/brand'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const brandStore = useBrandStore()

const loading = ref(false)
const overview = ref(createEmptyOverview())
const trendData = ref(createEmptyTrend())
const trendDays = ref(30)

const mainChartRef = ref()
const categoryChartRef = ref()
const statusChartRef = ref()
const userChartRef = ref()

let mainChart
let categoryChart
let statusChart
let userChart

const BRAND = '#FF6600'
const BRAND_LIGHT = '#FF8A3D'
const CHART_COLORS = ['#FF6600', '#2979FF', '#2EAF6A', '#9C27B0', '#FFB800', '#5D4037']
const CHART_TEXT = '#333333'
const CHART_TEXT_MUTED = '#666666'
const CHART_AXIS = {
  axisLabel: { color: CHART_TEXT_MUTED },
  axisLine: { lineStyle: { color: '#e8e8e8' } },
  splitLine: { lineStyle: { color: '#f0f0f0' } },
  nameTextStyle: { color: CHART_TEXT_MUTED },
}

const todayText = computed(() => {
  const now = new Date()
  return now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
})

const kpiGroups = computed(() => [
  {
    title: '经营概况',
    desc: '今日与累计交易表现',
    items: [
      { key: 'todayGmv', label: '今日 GMV', format: 'money', path: '/business/order', accent: BRAND },
      { key: 'monthGmv', label: '本月 GMV', format: 'money', path: '/business/order', accent: '#2979FF' },
      { key: 'todayOrderCount', label: '今日订单', format: 'integer', path: '/business/order', accent: '#2EAF6A' },
      { key: 'orderCount', label: '累计订单', format: 'integer', path: '/business/order', accent: '#9C27B0' },
    ],
  },
  {
    title: '监管待办',
    desc: '需优先关注的异常与审核',
    items: [
      { key: 'pendingOrderCount', label: '待处理订单', format: 'integer', path: '/business/order', accent: '#FFB800', alert: true },
      { key: 'refundingOrderCount', label: '退款中', format: 'integer', path: '/business/order', accent: '#F56C6C', alert: true },
      { key: 'pendingMerchantCount', label: '待审商户', format: 'integer', path: '/business/merchant-audit', accent: '#E6A23C', alert: true },
      { key: 'newUserCount', label: '今日新增用户', format: 'integer', path: '/users', accent: '#13C2C2' },
    ],
  },
])

const resourceStrip = computed(() => [
  { key: 'scenicOnlineCount', label: '上架景区', icon: 'xiangqufill', path: '/business/scenic' },
  { key: 'activityOnlineCount', label: '精彩活动', icon: 'renwu', path: '/business/activity' },
  { key: 'merchantCount', label: '入驻商户', icon: 'ordercenter', path: '/business/merchant-manage' },
  { key: 'userCount', label: '注册用户', icon: 'yonghu', path: '/users' },
])

const todoAlerts = computed(() => {
  const list = []
  if (Number(overview.value.pendingMerchantCount) > 0) {
    list.push({ text: `${overview.value.pendingMerchantCount} 个商户入驻待审核`, path: '/business/merchant-audit', type: 'warning' })
  }
  if (Number(overview.value.refundingOrderCount) > 0) {
    list.push({ text: `${overview.value.refundingOrderCount} 笔订单退款处理中`, path: '/business/order', type: 'danger' })
  }
  if (Number(overview.value.pendingOrderCount) > 0) {
    list.push({ text: `${overview.value.pendingOrderCount} 笔订单待跟进`, path: '/business/order', type: 'info' })
  }
  return list
})

function createEmptyOverview() {
  return {
    todayGmv: 0,
    monthGmv: 0,
    orderCount: 0,
    todayOrderCount: 0,
    pendingOrderCount: 0,
    refundingOrderCount: 0,
    merchantCount: 0,
    pendingMerchantCount: 0,
    userCount: 0,
    newUserCount: 0,
    destinationOnlineCount: 0,
    scenicOnlineCount: 0,
    activityOnlineCount: 0,
  }
}

function createEmptyTrend() {
  return {
    orderTrend: [],
    gmvTrend: [],
    userTrend: [],
    categoryBreakdown: [],
    statusBreakdown: [],
  }
}

function formatValue(item, raw) {
  if (item.format === 'money') {
    return Number(raw ?? 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  }
  return Number(raw ?? 0).toLocaleString('zh-CN')
}

function shortDateLabel(dateStr) {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  return parts.length >= 3 ? `${parts[1]}/${parts[2]}` : dateStr
}

function buildMainChartOption(orderTrend = [], gmvTrend = []) {
  const dates = orderTrend.map((item) => item.date)
  return {
    color: [BRAND, '#2979FF'],
    tooltip: { trigger: 'axis' },
    legend: { data: ['GMV', '订单数'], top: 0, textStyle: { color: CHART_TEXT } },
    grid: { left: 56, right: 48, top: 40, bottom: 28 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: { formatter: (value) => shortDateLabel(value), color: CHART_TEXT_MUTED },
      axisLine: { lineStyle: { color: '#e8e8e8' } },
    },
    yAxis: [
      {
        type: 'value',
        name: 'GMV（元）',
        ...CHART_AXIS,
        axisLabel: {
          color: CHART_TEXT_MUTED,
          formatter: (value) => (value >= 10000 ? `${(value / 10000).toFixed(1)}万` : value),
        },
      },
      {
        type: 'value',
        name: '订单数',
        minInterval: 1,
        splitLine: { show: false },
        ...CHART_AXIS,
      },
    ],
    series: [
      {
        name: 'GMV',
        type: 'line',
        smooth: true,
        showSymbol: false,
        yAxisIndex: 0,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,102,0,0.28)' },
            { offset: 1, color: 'rgba(255,102,0,0.02)' },
          ]),
        },
        lineStyle: { width: 3 },
        data: gmvTrend.map((item) => Number(item.amount ?? 0)),
      },
      {
        name: '订单数',
        type: 'bar',
        yAxisIndex: 1,
        barMaxWidth: 14,
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#5B9DFF' },
            { offset: 1, color: '#2979FF' },
          ]),
        },
        data: orderTrend.map((item) => Number(item.count ?? 0)),
      },
    ],
  }
}

function buildCategoryChartOption(items = []) {
  return {
    color: CHART_COLORS,
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        const amount = items[params.dataIndex]?.amount
        const amountText = amount != null
          ? `<br/>GMV：${Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2 })} 元`
          : ''
        return `${params.name}<br/>订单：${params.value} 笔 (${params.percent}%)${amountText}`
      },
    },
    legend: { bottom: 0, type: 'scroll', textStyle: { color: CHART_TEXT } },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '44%'],
        itemStyle: { borderRadius: 8, borderColor: 'rgba(28, 20, 16, 0.9)', borderWidth: 2 },
        label: { formatter: '{b}\n{d}%', color: CHART_TEXT_MUTED },
        data: items.map((item) => ({ name: item.label, value: Number(item.count ?? 0) })),
      },
    ],
  }
}

function buildStatusChartOption(items = []) {
  return {
    color: ['#FFB800', '#FF6600', '#2979FF', '#2EAF6A', '#67C23A', '#909399', '#F56C6C'],
    tooltip: { trigger: 'item' },
    series: [
      {
        type: 'pie',
        radius: ['0%', '68%'],
        center: ['50%', '50%'],
        roseType: 'radius',
        itemStyle: { borderRadius: 6 },
        label: { formatter: '{b}\n{c} 笔', color: CHART_TEXT_MUTED },
        data: items.map((item) => ({ name: item.label, value: Number(item.count ?? 0) })),
      },
    ],
  }
}

function buildUserChartOption(points = []) {
  return {
    color: [BRAND_LIGHT],
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 20, top: 24, bottom: 28 },
    xAxis: {
      type: 'category',
      data: points.map((item) => item.date),
      axisLabel: { formatter: (value) => shortDateLabel(value), color: CHART_TEXT_MUTED },
      axisLine: { lineStyle: { color: '#e8e8e8' } },
    },
    yAxis: { type: 'value', name: '新增用户', minInterval: 1, ...CHART_AXIS },
    series: [
      {
        type: 'line',
        smooth: true,
        showSymbol: true,
        symbolSize: 6,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255,138,61,0.25)' },
            { offset: 1, color: 'rgba(255,138,61,0.02)' },
          ]),
        },
        data: points.map((item) => Number(item.count ?? 0)),
      },
    ],
  }
}

function initChart(refEl, existing) {
  if (existing || !refEl.value) return existing
  return echarts.init(refEl.value)
}

function renderCharts(data) {
  mainChart = initChart(mainChartRef, mainChart)
  categoryChart = initChart(categoryChartRef, categoryChart)
  statusChart = initChart(statusChartRef, statusChart)
  userChart = initChart(userChartRef, userChart)

  mainChart?.setOption(buildMainChartOption(data?.orderTrend || [], data?.gmvTrend || []), true)
  categoryChart?.setOption(buildCategoryChartOption(data?.categoryBreakdown || []), true)
  statusChart?.setOption(buildStatusChartOption(data?.statusBreakdown || []), true)
  userChart?.setOption(buildUserChartOption(data?.userTrend || []), true)
}

function handleResize() {
  mainChart?.resize()
  categoryChart?.resize()
  statusChart?.resize()
  userChart?.resize()
}

async function loadStats() {
  loading.value = true
  try {
    const [overviewData, trend] = await Promise.all([
      getStatsOverview(),
      getStatsTrend(trendDays.value),
    ])
    overview.value = {
      ...createEmptyOverview(),
      ...overviewData,
      pendingMerchantCount: overviewData?.pendingMerchantCount ?? overviewData?.pendingAudit ?? 0,
      monthGmv: overviewData?.monthGmv ?? overviewData?.gmv ?? 0,
    }
    trendData.value = {
      ...createEmptyTrend(),
      ...trend,
    }
    await nextTick()
    renderCharts(trendData.value)
  } finally {
    loading.value = false
  }
}

function goTo(path) {
  router.push(path)
}

function changeTrendDays(days) {
  trendDays.value = days
  loadStats()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadStats()
})

watch(
  () => route.path,
  (path) => {
    if (path === '/dashboard') loadStats()
  },
)

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  mainChart?.dispose()
  categoryChart?.dispose()
  statusChart?.dispose()
  userChart?.dispose()
})
</script>

<template>
  <div v-loading="loading" class="dash">
    <header class="dash-hero">
      <div class="dash-hero__main">
        <p class="dash-hero__date">{{ todayText }}</p>
        <h1 class="dash-hero__title">文旅运营工作台</h1>
        <p class="dash-hero__sub">
          欢迎回来，{{ authStore.profile?.name || '管理员' }} · 一眼掌握 {{ brandStore.brandName }} 目的地经营与监管态势
        </p>
      </div>
      <div class="dash-hero__actions">
        <el-radio-group :model-value="trendDays" size="small" @change="changeTrendDays">
          <el-radio-button :value="7">近7天</el-radio-button>
          <el-radio-button :value="30">近30天</el-radio-button>
          <el-radio-button :value="90">近90天</el-radio-button>
        </el-radio-group>
      </div>
    </header>

    <div v-if="todoAlerts.length" class="dash-alerts">
      <div
        v-for="(alert, index) in todoAlerts"
        :key="index"
        class="dash-alert"
        :class="`dash-alert--${alert.type}`"
        @click="goTo(alert.path)"
      >
        <span class="dash-alert__dot" />
        <span>{{ alert.text }}</span>
        <span class="dash-alert__link">去处理 →</span>
      </div>
    </div>

    <section class="dash-overview">
      <div class="dash-overview__card">
        <div
          v-for="(group, groupIndex) in kpiGroups"
          :key="group.title"
          class="dash-overview__group"
        >
          <div class="dash-overview__group-head">
            <h2>{{ group.title }}</h2>
            <span>{{ group.desc }}</span>
          </div>
          <div class="dash-overview__grid">
            <button
              v-for="item in group.items"
              :key="item.key"
              type="button"
              class="dash-stat"
              :class="{ 'dash-stat--hot': item.alert && Number(overview[item.key]) > 0 }"
              @click="goTo(item.path)"
            >
              <span class="dash-stat__accent" :style="{ background: item.accent }" />
              <span class="dash-stat__body">
                <span class="dash-stat__label">{{ item.label }}</span>
                <span class="dash-stat__value" :style="{ color: item.accent }">
                  {{ formatValue(item, overview[item.key]) }}<small v-if="item.format === 'money'">元</small>
                </span>
              </span>
            </button>
          </div>
          <div v-if="groupIndex < kpiGroups.length - 1" class="dash-overview__divider" />
        </div>

        <div class="dash-overview__divider" />

        <div class="dash-overview__resources">
          <div class="dash-overview__group-head dash-overview__group-head--inline">
            <h2>文旅资源</h2>
            <span>内容资产与商户生态</span>
          </div>
          <div class="dash-resource-row">
            <button
              v-for="item in resourceStrip"
              :key="item.key"
              type="button"
              class="dash-resource-chip"
              @click="goTo(item.path)"
            >
              <span class="dash-resource-chip__icon">
                <IconSvg :name="item.icon" />
              </span>
              <span class="dash-resource-chip__meta">
                <strong>{{ formatValue({ format: 'integer' }, overview[item.key]) }}</strong>
                <em>{{ item.label }}</em>
              </span>
            </button>
          </div>
        </div>
      </div>
    </section>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="page-card dash-chart-card">
          <template #header>
            <div class="dash-chart-head">
              <strong>交易趋势</strong>
              <span>近 {{ trendDays }} 日 GMV 与订单量</span>
            </div>
          </template>
          <div ref="mainChartRef" class="dash-chart dash-chart--lg" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="page-card dash-chart-card">
          <template #header>
            <div class="dash-chart-head">
              <strong>订单品类构成</strong>
              <span>酒店 / 线路 / 好物 / 服务</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="dash-chart" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="page-card dash-chart-card">
          <template #header>
            <div class="dash-chart-head">
              <strong>订单状态分布</strong>
              <span>全平台订单监管视角</span>
            </div>
          </template>
          <div ref="statusChartRef" class="dash-chart" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" class="page-card dash-chart-card">
          <template #header>
            <div class="dash-chart-head">
              <strong>用户增长趋势</strong>
              <span>近 {{ trendDays }} 日新增注册用户</span>
            </div>
          </template>
          <div ref="userChartRef" class="dash-chart" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="page-card dash-quick">
      <template #header><strong>快捷入口</strong></template>
      <div class="dash-quick__grid">
        <el-button @click="goTo('/business/order')">订单监管</el-button>
        <el-button @click="goTo('/business/merchant-audit')">商户审核</el-button>
        <el-button @click="goTo('/business/hotel')">酒店管理</el-button>
        <el-button @click="goTo('/business/scenic')">景区管理</el-button>
        <el-button @click="goTo('/business/activity')">活动管理</el-button>
        <el-button @click="goTo('/business/notice')">公告发布</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped lang="scss">
.dash {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.dash-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 22px;
  border-radius: 16px;
  background: rgba(255, 138, 61, 0.08);
  border: 1px solid rgba(255, 138, 61, 0.14);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.dash-hero__date {
  margin: 0 0 6px;
  font-size: 13px;
  color: #ffb066;
  letter-spacing: 1px;
}

.dash-hero__title {
  margin: 0;
  font-size: clamp(1.5rem, 2.5vw, 2rem);
  font-weight: 800;
  color: var(--ink);
  letter-spacing: 2px;
}

.dash-hero__sub {
  margin: 10px 0 0;
  font-size: 14px;
  color: var(--muted);
  line-height: 1.6;
}

.dash-alerts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.dash-alert {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.15s ease;
}

.dash-alert:hover {
  transform: translateY(-1px);
}

.dash-alert--warning {
  background: rgba(255, 184, 0, 0.12);
  color: #a66d00;
}

.dash-alert--danger {
  background: rgba(245, 108, 108, 0.12);
  color: #c45656;
}

.dash-alert--info {
  background: rgba(41, 121, 255, 0.1);
  color: #1f5fbf;
}

.dash-alert__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.dash-alert__link {
  margin-left: 4px;
  font-weight: 600;
}

.dash-overview__card {
  padding: 16px 18px;
  border-radius: 16px;
  background: var(--card-bg);
  border: 1px solid var(--line);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.dash-overview__group + .dash-overview__group {
  margin-top: 14px;
}

.dash-overview__group-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 10px;
}

.dash-overview__group-head--inline {
  margin-bottom: 8px;
}

.dash-overview__group-head h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.dash-overview__group-head span {
  font-size: 12px;
  color: var(--muted);
}

.dash-overview__divider {
  height: 1px;
  margin: 14px 0;
  background: linear-gradient(90deg, rgba(255, 138, 61, 0.16), rgba(255, 138, 61, 0.04));
}

.dash-overview__grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.dash-stat {
  display: flex;
  align-items: stretch;
  gap: 10px;
  min-height: 58px;
  padding: 10px 12px;
  border: 1px solid rgba(255, 138, 61, 0.1);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
  cursor: pointer;
  text-align: left;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.15s ease;
}

.dash-stat:hover {
  background: rgba(255, 138, 61, 0.08);
  border-color: rgba(255, 102, 0, 0.28);
  transform: translateY(-1px);
}

.dash-stat--hot {
  background: rgba(255, 102, 0, 0.1);
  border-color: rgba(255, 102, 0, 0.28);
}

.dash-stat__accent {
  flex-shrink: 0;
  width: 3px;
  border-radius: 999px;
}

.dash-stat__body {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 2px;
  min-width: 0;
}

.dash-stat__label {
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dash-stat__value {
  font-size: 20px;
  font-weight: 800;
  line-height: 1.15;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dash-stat__value small {
  margin-left: 2px;
  font-size: 12px;
  font-weight: 500;
}

.dash-resource-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dash-resource-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 8px 12px;
  border: 1px solid rgba(255, 138, 61, 0.16);
  border-radius: 999px;
  background: rgba(255, 138, 61, 0.08);
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease;
}

.dash-resource-chip:hover {
  border-color: rgba(255, 102, 0, 0.35);
  background: rgba(255, 138, 61, 0.14);
}

.dash-resource-chip__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  color: #ff6600;
}

.dash-resource-chip__icon :deep(.icon-svg) {
  display: inline-flex;
  width: 18px;
  height: 18px;
}

.dash-resource-chip__icon :deep(.icon-svg__svg) {
  width: 100%;
  height: 100%;
  fill: currentColor;
}

.dash-resource-chip__meta {
  display: flex;
  align-items: baseline;
  gap: 6px;
  min-width: 0;
}

.dash-resource-chip__meta strong {
  font-size: 16px;
  font-weight: 800;
  color: #ff6600;
}

.dash-resource-chip__meta em {
  font-style: normal;
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
}

.dash-chart-card {
  margin-bottom: 16px;
}

.dash-chart-head {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dash-chart-head span {
  font-size: 12px;
  color: var(--muted);
  font-weight: 400;
}

.dash-chart {
  width: 100%;
  height: 300px;
}

.dash-chart--lg {
  height: 360px;
}

.dash-quick__grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 1200px) {
  .dash-overview__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .dash-hero {
    flex-direction: column;
  }

  .dash-overview__grid {
    grid-template-columns: 1fr;
  }

  .dash-resource-row {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dash-resource-chip {
    border-radius: 12px;
    width: 100%;
  }
}
</style>
