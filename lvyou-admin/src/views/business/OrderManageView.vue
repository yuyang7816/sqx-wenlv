<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import BasePagination from '@/components/common/BasePagination.vue'
import { getAppUserInfo } from '@/api/modules/app-user'
import { hotelApi } from '@/api/modules/hotel'
import { getMerchantDetail } from '@/api/modules/merchant'
import { getOrderDetail, getOrderPage, getRouteRefundPreview, postOrderAction } from '@/api/modules/order'
import { fetchDictItems } from '@/api/modules/dict-query'
import { isAuth } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'

/** 订单分类（对齐 AppOrderStatusSupport.allowedCategories） */
const CATEGORY_OPTIONS = [
  { label: '酒店', value: 'hotel' },
  { label: '线路', value: 'route' },
  { label: '非遗好物', value: 'goods' },
  { label: '服务', value: 'service' },
]

/** 支付方式中文展示 */
const PAY_METHOD_OPTIONS = [
  { label: '微信支付', value: 'wx' },
  { label: '支付宝', value: 'ali' },
  { label: '余额支付', value: 'wallet' },
]

function payMethodLabel(value) {
  return PAY_METHOD_OPTIONS.find((item) => item.value === value)?.label || value || '—'
}

/** 订单状态筛选项（sys_dict lvyou_order_status） */
const STATUS_OPTIONS = ref([
  { label: '待支付', value: 'pending_pay' },
  { label: '待确认', value: 'pending_confirm' },
  { label: '待使用/待发货', value: 'pending_use' },
  { label: '待收货', value: 'pending_receive' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
  { label: '退款中', value: 'refunding' },
])

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const actionLoading = ref(false)
const rowActionLoadingId = ref(null)
const detail = ref(null)

const merchantDetailVisible = ref(false)
const merchantDetailLoading = ref(false)
const merchantDetail = ref(null)

const hotelDetailVisible = ref(false)
const hotelDetailLoading = ref(false)
const hotelDetail = ref(null)

const userDetailVisible = ref(false)
const userDetailLoading = ref(false)
const userDetail = ref(null)

const routeRefundDialogVisible = ref(false)
const routeRefundTarget = ref(null)
const routeRefundForm = reactive({
  refundRatio: 100,
  reason: '',
})
const routeRefundPreview = ref(null)
const routeRefundPreviewLoading = ref(false)
const routeRefundSubmitting = ref(false)

const filters = reactive({
  orderNo: '',
  category: '',
  status: '',
  dateRange: [],
  merchantId: '',
  merchantName: '',
})

const merchantFilterLabel = computed(() => {
  if (!filters.merchantId) return ''
  return filters.merchantName || `商户ID ${filters.merchantId}`
})

const canAction = computed(() => isAuth('lvyou:order:action'))

/** 管理端可取消：非已完成/已取消/退款中 */
const canCancel = computed(() => canCancelRow(detail.value))

/** 管理端可强制完成：非已完成/已取消 */
const canComplete = computed(() => canCompleteRow(detail.value))

/** 管理端可发起退款：已支付订单且状态为待确认/待使用/待收货 */
const canRefund = computed(() => canRefundRow(detail.value))

/** 无在线支付的订单类型（餐饮等仅预约） */
const NO_PAY_ORDER_CATEGORIES = new Set(['restaurant'])

function canCancelRow(row) {
  const code = row?.statusCode
  return code && !['completed', 'cancelled', 'refunding'].includes(code)
}

function canCompleteRow(row) {
  const code = row?.statusCode
  return code && !['completed', 'cancelled'].includes(code)
}

function isRouteOrder(row) {
  return row?.orderCategory === 'route'
}

/** 线路 R8：已支付且非终态（含已完成争议退款） */
function canRouteRefundRow(row) {
  const code = row?.statusCode
  if (!code) return false
  if (NO_PAY_ORDER_CATEGORIES.has(row.orderCategory)) return false
  if (!row.payTime && Number(row.amount ?? 0) <= 0) return false
  return !['pending_pay', 'cancelled', 'refunded', 'refunding'].includes(code)
}

function canRefundRow(row) {
  if (NO_PAY_ORDER_CATEGORIES.has(row.orderCategory)) return false
  if (!row.payTime && Number(row.amount ?? 0) <= 0) return false
  if (isRouteOrder(row)) {
    return canRouteRefundRow(row)
  }
  const code = row?.statusCode
  return code && ['pending_confirm', 'pending_use', 'pending_receive'].includes(code)
}

function refundActionLabel(row) {
  return isRouteOrder(row) ? '强制退款' : '发起退款'
}

function categoryLabel(value) {
  if (value === 'culture') return '非遗好物'
  return CATEGORY_OPTIONS.find((item) => item.value === value)?.label || value || '—'
}

function statusTagType(statusCode) {
  if (statusCode === 'pending_pay') return 'info'
  if (statusCode === 'completed') return 'success'
  if (statusCode === 'cancelled') return 'info'
  if (statusCode === 'refunding') return 'danger'
  return 'warning'
}

function merchantStatusTagType(status) {
  if (status === 1) return 'success'
  return 'danger'
}

function merchantOpenTagType(isOpen) {
  if (isOpen === 1) return 'success'
  return 'warning'
}

function formatUserStatus(status) {
  if (status === 1) return '正常'
  if (status === 2) return '禁用'
  if (status === 3) return '已注销'
  return status ?? '—'
}

function formatUserSex(sex) {
  if (sex === 1) return '男'
  if (sex === 2) return '女'
  return '未设置'
}

function formatUserType(userType) {
  if (userType === 2) return 'VIP用户'
  return '普通用户'
}

function formatVip(isVip) {
  if (isVip === 1) return '是'
  if (isVip === 0) return '否'
  return '—'
}

function displayValue(value) {
  if (value == null || value === '') return '—'
  return value
}

function itemTitle(row) {
  if (!row) return '—'
  if (row.orderCategory === 'hotel') return row.roomName || '—'
  if (row.orderCategory === 'route') return row.routeName || '—'
  if (row.orderCategory === 'goods' || row.orderCategory === 'culture' || row.orderCategory === 'service') {
    return row.goodsName || '—'
  }
  return row.roomName || row.routeName || row.goodsName || '—'
}

function formatAmount(value) {
  if (value == null || value === '') return '—'
  return `¥${Number(value).toFixed(2)}`
}

function formatGuestExportLine(guest) {
  const role = guest?.roleLabel || (guest?.role === 'booker' ? '预约人' : '同行人')
  let line = `${role}：姓名 ${guest?.name || ''} 身份证号${guest?.idCard || ''}`
  if (guest?.phone) line += `  联系电话 ${guest.phone}`
  return line
}

async function copyGuestInfo() {
  const list = detail.value?.guestList
  if (!Array.isArray(list) || !list.length) {
    showError('暂无出行人信息')
    return
  }
  const text = list.map(formatGuestExportLine).join('\n\n')
  try {
    await navigator.clipboard.writeText(text)
    showSuccess('出行人信息已复制')
  } catch (e) {
    showError('复制失败，请手动选择复制')
  }
}

function applyRouteQuery() {
  const merchantId = route.query.merchantId
  if (merchantId) {
    filters.merchantId = Number(merchantId) || merchantId
    filters.merchantName = String(route.query.merchantName || '').trim()
    return
  }
  filters.merchantId = ''
  filters.merchantName = ''
}

function buildQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    orderNo: filters.orderNo.trim() || undefined,
    category: filters.category || undefined,
    status: filters.status || undefined,
    merchantId: filters.merchantId || undefined,
  }
}

function clearMerchantFilter() {
  filters.merchantId = ''
  filters.merchantName = ''
  if (route.query.merchantId) {
    router.replace({ path: route.path, query: {} })
  }
  pageNum.value = 1
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const data = await getOrderPage(buildQuery())
    tableData.value = data.list || []
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载订单列表失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onCategoryTab(name) {
  filters.category = name === 'all' ? '' : name
  pageNum.value = 1
  loadData()
}

function handleSearch() {
  if (filters.dateRange?.length === 2) {
    showError('下单日期筛选需后端扩展 createTimeStart/createTimeEnd，当前请用订单编号/分类/状态筛选')
  }
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, {
    orderNo: '',
    category: '',
    status: '',
    dateRange: [],
    merchantId: '',
    merchantName: '',
  })
  if (route.query.merchantId) {
    router.replace({ path: route.path, query: {} })
  }
  pageNum.value = 1
  loadData()
}

async function openDetail(row) {
  drawerVisible.value = true
  drawerLoading.value = true
  detail.value = null
  try {
    detail.value = await getOrderDetail(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载订单详情失败')
    drawerVisible.value = false
  } finally {
    drawerLoading.value = false
  }
}

async function fetchHotelDetail(row) {
  const query = { page: 1, limit: 1 }
  if (row.hotelId) {
    query.id = row.hotelId
  } else if (row.merchantId) {
    query.merchantId = row.merchantId
  } else {
    return null
  }
  const data = await hotelApi.page(query)
  return data.list?.[0] || null
}

async function openMerchantDetail(row) {
  if (!row?.merchantId && !row?.hotelId) {
    showError('暂无商户信息')
    return
  }
  if (row.orderCategory === 'hotel') {
    hotelDetailVisible.value = true
    hotelDetailLoading.value = true
    hotelDetail.value = null
    try {
      hotelDetail.value = await fetchHotelDetail(row)
      if (!hotelDetail.value) {
        showError('未找到关联酒店信息')
        hotelDetailVisible.value = false
      }
    } catch (error) {
      showError(error?.msg || error?.message || '加载酒店详情失败')
      hotelDetailVisible.value = false
    } finally {
      hotelDetailLoading.value = false
    }
    return
  }
  merchantDetailVisible.value = true
  merchantDetailLoading.value = true
  merchantDetail.value = null
  try {
    merchantDetail.value = await getMerchantDetail(row.merchantId)
  } catch (error) {
    showError(error?.msg || error?.message || '加载商户详情失败')
    merchantDetailVisible.value = false
  } finally {
    merchantDetailLoading.value = false
  }
}

async function openUserDetail(row) {
  if (!row?.userId) {
    showError('暂无下单用户信息')
    return
  }
  userDetailVisible.value = true
  userDetailLoading.value = true
  userDetail.value = null
  try {
    userDetail.value = await getAppUserInfo(row.userId)
    if (!userDetail.value) {
      userDetailVisible.value = false
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载用户详情失败')
    userDetailVisible.value = false
  } finally {
    userDetailLoading.value = false
  }
}

async function loadRouteRefundPreview() {
  const target = routeRefundTarget.value
  if (!target?.id) return
  routeRefundPreviewLoading.value = true
  try {
    routeRefundPreview.value = await getRouteRefundPreview(target.id, routeRefundForm.refundRatio)
  } catch (error) {
    routeRefundPreview.value = null
    showError(error?.msg || error?.message || '加载退款预览失败')
  } finally {
    routeRefundPreviewLoading.value = false
  }
}

function openRouteRefundDialog(row = null) {
  const target = row || detail.value
  if (!target?.id) return
  routeRefundTarget.value = target
  routeRefundForm.refundRatio = 100
  routeRefundForm.reason = ''
  routeRefundPreview.value = null
  routeRefundDialogVisible.value = true
  loadRouteRefundPreview()
}

async function confirmRouteRefund() {
  const target = routeRefundTarget.value
  if (!target?.id) return
  const reason = String(routeRefundForm.reason || '').trim()
  if (!reason) {
    showError('请填写操作原因')
    return
  }
  if (routeRefundForm.refundRatio == null || routeRefundForm.refundRatio < 0 || routeRefundForm.refundRatio > 100) {
    showError('退款比例须在 0–100 之间')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认对订单 ID ${target.id} 执行平台强制退款？\n用户预计退款：${formatAmount(routeRefundPreview.value?.userRefund ?? 0)}`,
      '确认强制退款',
      { type: 'warning' },
    )
  } catch {
    return
  }
  routeRefundSubmitting.value = true
  if (detail.value?.id === target.id) {
    actionLoading.value = true
  } else {
    rowActionLoadingId.value = target.id
  }
  try {
    const result = await postOrderAction({
      orderId: target.id,
      action: 'refund',
      reason,
      refundRatio: routeRefundForm.refundRatio,
    })
    if (detail.value?.id === target.id) {
      detail.value = result
    }
    routeRefundDialogVisible.value = false
    showSuccess('强制退款成功')
    loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '强制退款失败')
  } finally {
    routeRefundSubmitting.value = false
    actionLoading.value = false
    rowActionLoadingId.value = null
  }
}

function handleRefundClick(row = null) {
  const target = row || detail.value
  if (isRouteOrder(target)) {
    openRouteRefundDialog(row)
    return
  }
  runAction('refund', '发起退款', { needReason: true, row })
}

async function runAction(action, title, options = {}) {
  const { needReason = false, row = null } = options
  const target = row || detail.value
  if (!target?.id) return
  let reason = ''
  if (needReason) {
    const { value } = await ElMessageBox.prompt('请填写操作原因', title, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入原因',
      inputValidator: (val) => {
        if (!String(val || '').trim()) return '原因不能为空'
        return true
      },
    })
    reason = String(value).trim()
  } else {
    await ElMessageBox.confirm(`确认对订单 ID ${target.id} 执行「${title}」吗？`, title, {
      type: 'warning',
    })
  }
  if (row) {
    rowActionLoadingId.value = row.id
  } else {
    actionLoading.value = true
  }
  try {
    const result = await postOrderAction({
      orderId: target.id,
      action,
      reason: reason || undefined,
    })
    if (detail.value?.id === target.id) {
      detail.value = result
    }
    showSuccess(`${title}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error?.msg || error?.message || `${title}失败`)
    }
  } finally {
    if (row) {
      rowActionLoadingId.value = null
    } else {
      actionLoading.value = false
    }
  }
}

async function loadStatusOptions() {
  try {
    const items = await fetchDictItems('lvyou_order_status')
    if (Array.isArray(items) && items.length) {
      STATUS_OPTIONS.value = items.map((item) => ({
        label: item.label,
        value: item.code,
      }))
    }
  } catch (e) {
    /* 保留内置 fallback */
  }
}

watch(
  () => route.query.merchantId,
  () => {
    applyRouteQuery()
    pageNum.value = 1
    loadData()
  },
)

watch(
  () => routeRefundForm.refundRatio,
  () => {
    if (!routeRefundDialogVisible.value) return
    loadRouteRefundPreview()
  },
)

onMounted(() => {
  applyRouteQuery()
  loadStatusOptions()
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>订单监管</strong>
        <el-tag v-if="merchantFilterLabel" closable type="info" @close="clearMerchantFilter">
          商户：{{ merchantFilterLabel }}
        </el-tag>
      </div>
    </template>

    <el-tabs
      :model-value="filters.category || 'all'"
      class="order-category-tabs"
      @tab-change="onCategoryTab"
    >
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane
        v-for="item in CATEGORY_OPTIONS"
        :key="item.value"
        :label="item.label"
        :name="item.value"
      />
    </el-tabs>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="订单编号">
        <el-input v-model="filters.orderNo" clearable placeholder="订单编号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="订单状态">
        <el-select v-model="filters.status" clearable placeholder="全部">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="下单日期">
        <el-date-picker
          v-model="filters.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          clearable
        />
        <span class="date-hint">后端暂未支持日期区间筛选</span>
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无订单">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column label="订单分类" min-width="90">
        <template #default="{ row }">{{ categoryLabel(row.orderCategory) }}</template>
      </el-table-column>
      <el-table-column label="商品/服务" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">{{ itemTitle(row) }}</template>
      </el-table-column>
      <el-table-column label="商户名称" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">
          <el-link
            v-if="row.merchantName && (row.merchantId || row.hotelId)"
            type="primary"
            :underline="false"
            @click="openMerchantDetail(row)"
          >
            {{ row.merchantName }}
          </el-link>
          <span v-else>{{ row.merchantName || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="contactName" label="联系人" min-width="100" />
      <el-table-column prop="phone" label="联系电话" min-width="130" />
      <el-table-column label="下单用户手机" min-width="130">
        <template #default="{ row }">
          <el-link
            v-if="row.userId"
            type="primary"
            :underline="false"
            @click="openUserDetail(row)"
          >
            {{ row.userPhone || '—' }}
          </el-link>
          <span v-else>{{ row.userPhone || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="实付金额" min-width="100">
        <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="订单状态" min-width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.statusCode)">{{ row.status || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" min-width="170">
        <template #default="{ row }">{{ row.createTime || row.date || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="360" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="isAuth('lvyou:order:info')" size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="canAction && canCancelRow(row)"
              size="small"
              type="danger"
              :loading="rowActionLoadingId === row.id"
              @click="runAction('cancel', '强制取消', { needReason: true, row })"
            >
              强制取消
            </el-button>
            <el-button
              v-if="canAction && canCompleteRow(row)"
              size="small"
              type="success"
              :loading="rowActionLoadingId === row.id"
              @click="runAction('complete', '强制完成', { row })"
            >
              强制完成
            </el-button>
            <el-button
              v-if="canAction && canRefundRow(row)"
              size="small"
              type="warning"
              :loading="rowActionLoadingId === row.id"
              @click="handleRefundClick(row)"
            >
              {{ refundActionLabel(row) }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />

    <el-drawer v-model="drawerVisible" title="订单详情" size="680px" destroy-on-close>
      <div v-loading="drawerLoading" class="detail-body">
        <template v-if="detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单ID" :span="2">{{ detail.id ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="订单分类">{{ categoryLabel(detail.orderCategory) }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="statusTagType(detail.statusCode)">{{ detail.status || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单总额">{{ formatAmount(detail.totalAmount) }}</el-descriptions-item>
            <el-descriptions-item label="优惠抵扣">{{ formatAmount(detail.discountAmount) }}</el-descriptions-item>
            <el-descriptions-item label="实付金额">{{ formatAmount(detail.amount) }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ detail.payMethodLabel || payMethodLabel(detail.payMethod) }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ detail.createTime || detail.date || '—' }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ detail.payTime || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.refundAmount != null && Number(detail.refundAmount) > 0" label="退款金额">
              {{ formatAmount(detail.refundAmount) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.merchantSettleAmount != null && Number(detail.merchantSettleAmount) > 0" label="商户结算">
              {{ formatAmount(detail.merchantSettleAmount) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.platformFeeAmount != null && Number(detail.platformFeeAmount) > 0" label="平台抽成">
              {{ formatAmount(detail.platformFeeAmount) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.settleTime" label="结算时间">{{ detail.settleTime }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.remark" label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
          </el-descriptions>

          <section class="detail-section">
            <h4>商户信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商户ID">{{ detail.merchantId ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="商户名称">
                <el-link
                  v-if="detail.merchantName && (detail.merchantId || detail.hotelId)"
                  type="primary"
                  :underline="false"
                  @click="openMerchantDetail(detail)"
                >
                  {{ detail.merchantName }}
                </el-link>
                <span v-else>{{ detail.merchantName || '—' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="联系人">{{ detail.merchantContactName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ detail.merchantPhone || '—' }}</el-descriptions-item>
              <el-descriptions-item label="所属目的地">{{ detail.destinationName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="所在地区">{{ detail.merchantRegion || '—' }}</el-descriptions-item>
              <el-descriptions-item label="门店地址" :span="2">{{ detail.merchantAddress || '—' }}</el-descriptions-item>
              <el-descriptions-item v-if="detail.latitude != null && detail.longitude != null" label="地图坐标" :span="2">
                {{ detail.latitude }}, {{ detail.longitude }}
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="detail.hotelCover" class="goods-cover">
              <p>酒店封面</p>
              <el-image :src="detail.hotelCover" fit="cover" class="cover-image" preview-teleported />
            </div>
          </section>

          <section class="detail-section">
            <h4>下单人信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="用户ID">{{ detail.userId ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="用户名">
                <el-link
                  v-if="detail.userId"
                  type="primary"
                  :underline="false"
                  @click="openUserDetail(detail)"
                >
                  {{ detail.userName || '查看用户' }}
                </el-link>
                <span v-else>{{ detail.userName || '—' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="手机号">{{ detail.userPhone || '—' }}</el-descriptions-item>
              <el-descriptions-item label="订单联系人">{{ detail.contactName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="订单联系电话" :span="2">{{ detail.phone || '—' }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section v-if="detail.orderCategory === 'hotel'" class="detail-section">
            <h4>酒店预订信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="房型">{{ detail.roomName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="房型规格">{{ detail.roomSpec || '—' }}</el-descriptions-item>
              <el-descriptions-item label="入住晚数">{{ detail.nights ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="预订间数">{{ detail.roomCount ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="入住日期">{{ detail.checkIn || '—' }}</el-descriptions-item>
              <el-descriptions-item label="离店日期">{{ detail.checkOut || '—' }}</el-descriptions-item>
              <el-descriptions-item label="入住人" :span="2">{{ detail.guestNames || '—' }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section v-else-if="detail.orderCategory === 'route'" class="detail-section">
            <h4>线路信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="线路名称">{{ detail.routeName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="出行日期">{{ detail.tripDate || '—' }}</el-descriptions-item>
              <el-descriptions-item label="出行人数">{{ detail.guestCount ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="行程时长">{{ detail.duration || '—' }}</el-descriptions-item>
              <el-descriptions-item label="集合地点">{{ detail.meetPoint || '—' }}</el-descriptions-item>
              <el-descriptions-item label="途经景点">{{ detail.spots || '—' }}</el-descriptions-item>
              <el-descriptions-item label="出行人摘要" :span="2">{{ detail.guestNames || '—' }}</el-descriptions-item>
            </el-descriptions>
            <div v-if="detail.guestList?.length" class="guest-detail-block">
              <div class="guest-detail-head">
                <h5>出行人明细</h5>
                <el-button size="small" @click="copyGuestInfo">复制出行人信息</el-button>
              </div>
              <el-table :data="detail.guestList" border size="small">
                <el-table-column prop="roleLabel" label="角色" width="90" />
                <el-table-column prop="name" label="姓名" min-width="100" />
                <el-table-column prop="idCard" label="身份证号" min-width="180" />
                <el-table-column prop="phone" label="联系电话" min-width="120" />
              </el-table>
            </div>
          </section>

          <section v-else-if="detail.orderCategory === 'goods' || detail.orderCategory === 'culture' || detail.orderCategory === 'service'" class="detail-section">
            <h4>非遗好物信息</h4>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="商品名称">{{ detail.goodsName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="规格">{{ detail.goodsSpec || '—' }}</el-descriptions-item>
              <el-descriptions-item label="数量">{{ detail.quantity ?? '—' }}</el-descriptions-item>
              <el-descriptions-item label="收货地址" :span="2">{{ detail.addressText || '—' }}</el-descriptions-item>
            </el-descriptions>
            <div v-if="detail.goodsCover" class="goods-cover">
              <p>商品封面</p>
              <el-image :src="detail.goodsCover" fit="cover" class="cover-image" preview-teleported />
            </div>
          </section>

          <section v-if="detail.items?.length" class="detail-section">
            <h4>订单明细</h4>
            <el-table :data="detail.items" border size="small">
              <el-table-column prop="name" label="名称" min-width="140" />
              <el-table-column prop="spec" label="规格" min-width="100" />
              <el-table-column prop="quantity" label="数量" width="80" />
              <el-table-column label="单价" width="100">
                <template #default="{ row }">{{ formatAmount(row.unitPrice) }}</template>
              </el-table-column>
              <el-table-column label="小计" width="100">
                <template #default="{ row }">{{ formatAmount(row.totalPrice) }}</template>
              </el-table-column>
            </el-table>
          </section>

          <div v-if="canAction && (canCancel || canComplete || canRefund)" class="drawer-actions">
            <el-button
              v-if="canCancel"
              type="danger"
              :loading="actionLoading"
              @click="runAction('cancel', '强制取消', { needReason: true })"
            >
              强制取消
            </el-button>
            <el-button
              v-if="canComplete"
              type="success"
              :loading="actionLoading"
              @click="runAction('complete', '强制完成')"
            >
              强制完成
            </el-button>
            <el-button
              v-if="canRefund"
              type="warning"
              :loading="actionLoading"
              @click="handleRefundClick()"
            >
              {{ refundActionLabel(detail) }}
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="hotelDetailVisible" title="酒店详情" width="720px" destroy-on-close>
      <div v-loading="hotelDetailLoading">
        <el-descriptions v-if="hotelDetail" :column="2" border>
          <el-descriptions-item label="酒店ID">{{ hotelDetail.id ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="酒店名称">{{ hotelDetail.name || '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属商户">{{ hotelDetail.merchantName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属目的地">{{ hotelDetail.destinationName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="分类">{{ hotelDetail.categoryLabel || hotelDetail.category || '—' }}</el-descriptions-item>
          <el-descriptions-item label="星级">{{ hotelDetail.starLevel ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ hotelDetail.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="起步价">{{ formatAmount(hotelDetail.priceFrom) }}</el-descriptions-item>
          <el-descriptions-item label="地址" :span="2">{{ hotelDetail.address || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="hotelDetail.latitude != null && hotelDetail.longitude != null" label="地图坐标" :span="2">
            {{ hotelDetail.latitude }}, {{ hotelDetail.longitude }}
          </el-descriptions-item>
          <el-descriptions-item label="入住时间">{{ hotelDetail.checkInTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="退房时间">{{ hotelDetail.checkOutTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="hotelDetail.status === 1 ? 'success' : 'info'">{{ hotelDetail.statusLabel || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="评分">{{ hotelDetail.score ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="标签" :span="2">{{ hotelDetail.tag || '—' }}</el-descriptions-item>
          <el-descriptions-item label="简介" :span="2">{{ hotelDetail.intro || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="hotelDetail.coverImage" label="封面" :span="2">
            <el-image :src="hotelDetail.coverImage" fit="cover" class="cover-image" preview-teleported />
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="hotelDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="merchantDetailVisible" title="商户详情" width="720px" destroy-on-close>
      <div v-loading="merchantDetailLoading">
        <el-descriptions v-if="merchantDetail" :column="2" border>
          <el-descriptions-item label="商户ID">{{ merchantDetail.id ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="门店名称">{{ merchantDetail.storeName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="门店类型">{{ merchantDetail.storeTypeLabel || merchantDetail.storeType || '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属目的地">{{ merchantDetail.destinationName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ merchantDetail.contactName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ merchantDetail.contactPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="门店地区">{{ merchantDetail.storeRegion || '—' }}</el-descriptions-item>
          <el-descriptions-item label="详细地址">{{ merchantDetail.storeAddressDetail || merchantDetail.storeAddress || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="merchantDetail.latitude != null && merchantDetail.longitude != null" label="地图坐标" :span="2">
            {{ merchantDetail.latitude }}, {{ merchantDetail.longitude }}
          </el-descriptions-item>
          <el-descriptions-item label="营业时间">{{ merchantDetail.storeHours || '—' }}</el-descriptions-item>
          <el-descriptions-item label="关联用户">{{ merchantDetail.userName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="用户手机">{{ merchantDetail.userPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="merchantStatusTagType(merchantDetail.status)">{{ merchantDetail.statusLabel || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="营业状态">
            <el-tag :type="merchantOpenTagType(merchantDetail.isOpen)">{{ merchantDetail.isOpenLabel || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="统一社会信用代码" :span="2">{{ merchantDetail.licenseNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="法人姓名">{{ merchantDetail.legalName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="入驻时间">{{ merchantDetail.createTime || '—' }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="merchantDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="routeRefundDialogVisible"
      title="线路订单 · 平台强制退款（R8）"
      width="560px"
      destroy-on-close
      @closed="routeRefundTarget = null"
    >
      <div v-if="routeRefundTarget" class="route-refund-dialog">
        <p class="route-refund-tip">
          订单 ID {{ routeRefundTarget.id }} · 实付 {{ formatAmount(routeRefundTarget.amount) }}
        </p>
        <el-form label-width="100px" @submit.prevent>
          <el-form-item label="退款比例" required>
            <el-input-number
              v-model="routeRefundForm.refundRatio"
              :min="0"
              :max="100"
              :step="1"
              controls-position="right"
            />
            <span class="ratio-hint">0 = 不退款；100 = 全额退用户</span>
          </el-form-item>
          <el-form-item label="操作原因" required>
            <el-input
              v-model="routeRefundForm.reason"
              type="textarea"
              :rows="3"
              placeholder="请填写强制退款原因（必填）"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>
        </el-form>
        <div v-loading="routeRefundPreviewLoading" class="refund-preview-box">
          <template v-if="routeRefundPreview">
            <h5>退款分账预览</h5>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="适用规则">{{ routeRefundPreview.ruleLabel || '平台强制退款' }}</el-descriptions-item>
              <el-descriptions-item label="退用户比例">{{ routeRefundPreview.userRefundRatio ?? routeRefundPreview.refundRatio }}%</el-descriptions-item>
              <el-descriptions-item label="扣款比例">{{ routeRefundPreview.refundRatio }}%</el-descriptions-item>
              <el-descriptions-item label="平台抽成比例">{{ routeRefundPreview.commissionRate ?? 0 }}%</el-descriptions-item>
              <el-descriptions-item label="预计退用户">{{ formatAmount(routeRefundPreview.userRefund) }}</el-descriptions-item>
              <el-descriptions-item v-if="Number(routeRefundPreview.platformFee) > 0" label="平台服务费">
                {{ formatAmount(routeRefundPreview.platformFee) }}
              </el-descriptions-item>
              <el-descriptions-item v-if="Number(routeRefundPreview.merchantIncome) > 0" label="商户预计入账">
                {{ formatAmount(routeRefundPreview.merchantIncome) }}
              </el-descriptions-item>
            </el-descriptions>
            <p class="refund-arrival-hint">用户退款将原路返回，预计 1–3 个工作日到账</p>
          </template>
          <el-empty v-else-if="!routeRefundPreviewLoading" description="暂无预览" :image-size="60" />
        </div>
      </div>
      <template #footer>
        <el-button @click="routeRefundDialogVisible = false">取消</el-button>
        <el-button type="warning" :loading="routeRefundSubmitting" @click="confirmRouteRefund">确认强制退款</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="userDetailVisible" title="用户详情" width="720px" destroy-on-close>
      <div v-loading="userDetailLoading">
        <el-descriptions v-if="userDetail" :column="2" border>
          <el-descriptions-item label="用户ID">{{ userDetail.userId ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ displayValue(userDetail.userName) }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ displayValue(userDetail.phone) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="userDetail.status === 1 ? 'success' : 'danger'">{{ formatUserStatus(userDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="钱包余额">¥{{ userDetail.balance || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">{{ formatUserType(userDetail.userType) }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ formatUserSex(userDetail.sex) }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ displayValue(userDetail.age) }}</el-descriptions-item>
          <el-descriptions-item label="是否会员">{{ formatVip(userDetail.isVip) }}</el-descriptions-item>
          <el-descriptions-item label="注册来源">{{ displayValue(userDetail.platform) }}</el-descriptions-item>
          <el-descriptions-item label="邀请码">{{ displayValue(userDetail.invitationCode) }}</el-descriptions-item>
          <el-descriptions-item label="上级邀请码">{{ displayValue(userDetail.superiorInvitationCode) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ displayValue(userDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ displayValue(userDetail.updateTime) }}</el-descriptions-item>
          <el-descriptions-item v-if="userDetail.avatar" label="头像" :span="2">
            <el-image :src="userDetail.avatar" fit="cover" class="cover-image" preview-teleported />
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="userDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped lang="scss">
.detail-body {
  min-height: 200px;
}

.order-category-tabs {
  margin-bottom: 8px;
}

.detail-section {
  margin-top: 20px;

  h4 {
    margin: 0 0 12px;
    font-size: 15px;
    font-weight: 600;
  }
}

.goods-cover {
  margin-top: 12px;

  p {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.cover-image {
  width: 120px;
  height: 120px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
}

.guest-detail-block {
  margin-top: 16px;
}

.guest-detail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;

  h5 {
    margin: 0;
    font-size: 14px;
    font-weight: 600;
  }
}

.drawer-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.date-hint {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.route-refund-dialog {
  .route-refund-tip {
    margin: 0 0 16px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }

  .ratio-hint {
    margin-left: 12px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.refund-preview-box {
  min-height: 120px;
  margin-top: 8px;

  h5 {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
  }
}

.refund-arrival-hint {
  margin: 12px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
