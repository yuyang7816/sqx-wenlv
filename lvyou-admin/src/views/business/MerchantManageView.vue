<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import MapLocationPicker from '@/components/business/MapLocationPicker.vue'
import {
  adjustMerchant,
  adjustMerchantDeposit,
  auditMerchantDepositRefund,
  deleteMerchant,
  getMerchantDetail,
  getMerchantPage,
  saveMerchant,
  updateMerchant,
  getMerchantStoreTypeList,
} from '@/api/modules/merchant'
import { getAppUserInfo, getAppUserPage } from '@/api/modules/app-user'
import { destinationApi } from '@/api/modules/destination'
import { fetchHeritageShopCategoryOptionsApi } from '@/api/modules/heritage'
import { isAuth } from '@/utils/tool'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import {
  formatStoreAddress,
  getEmptyApplyForm,
  getQualificationsByType,
  isQualMultiItem,
  getQualMaxCount,
  normalizeQualImagesForForm,
  serializeQualImagesForSubmit,
  validateApplyForm,
} from '@/utils/merchant-onboarding-admin'

const STORE_TYPE_OPTIONS = ref([])

const STATUS_OPTIONS = [
  { label: '正常', value: 1 },
  { label: '已禁用', value: 2 },
]

const OPEN_OPTIONS = [
  { label: '营业中', value: 1 },
  { label: '休息中', value: 0 },
]

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const actionLoading = ref(false)
const rowAdjustLoadingId = ref(null)
const detail = ref(null)

const formRef = ref()
const formDialogVisible = ref(false)
const formLoading = ref(false)
const editingId = ref(null)
const destinationOptions = ref([])
const userOptions = ref([])
const userSearchLoading = ref(false)
const shopCategoryOptions = ref([])

const form = reactive(getEmptyApplyForm())

const depositDialogVisible = ref(false)
const depositDialogLoading = ref(false)
const depositFormRef = ref()
const depositForm = reactive({
  merchantId: null,
  storeName: '',
  adjustType: 'paid_increase',
  amount: 100,
  remark: '',
})
const depositAdjustOptions = [
  { label: '增加已缴保证金', value: 'paid_increase' },
  { label: '减少已缴保证金', value: 'paid_decrease' },
  { label: '增加应缴保证金', value: 'required_increase' },
  { label: '减少应缴保证金', value: 'required_decrease' },
]
const depositRules = {
  adjustType: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入调整金额', trigger: 'blur' }],
}

const refundDialogVisible = ref(false)
const refundDialogLoading = ref(false)
const refundTarget = ref(null)
const refundRemark = ref('')

const currentQual = computed(() => getQualificationsByType(form.storeType))

const qualUploadItems = computed(() =>
  (currentQual.value?.required || []).filter((item) => item.key !== 'license' && item.key !== 'legalId'),
)

const formRules = {
  userId: [{ required: true, message: '请选择关联用户', trigger: 'change' }],
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  storeType: [{ required: true, message: '请选择门店类型', trigger: 'change' }],
  shopCategory: [{
    validator: (_rule, value, callback) => {
      if (form.storeType === 'culture' && !value) {
        callback(new Error('请选择好店分类'))
        return
      }
      callback()
    },
    trigger: 'change',
  }],
  contactName: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  storeRegion: [{ required: true, message: '请通过地图选择门店位置', trigger: 'change' }],
  storeAddressDetail: [{ required: true, message: '请通过地图选择门店位置', trigger: 'change' }],
  licenseNo: [{ required: true, message: '请填写统一社会信用代码', trigger: 'blur' }],
  licenseImage: [{ required: true, message: '请上传营业执照', trigger: 'change' }],
  legalName: [{ required: true, message: '请填写法人姓名', trigger: 'blur' }],
  legalIdNo: [{ required: true, message: '请填写法人身份证号', trigger: 'blur' }],
  legalIdFront: [{ required: true, message: '请上传身份证人像面', trigger: 'change' }],
  legalIdBack: [{ required: true, message: '请上传身份证国徽面', trigger: 'change' }],
}

const filters = reactive({
  id: '',
  storeName: '',
  contactPhone: '',
  storeType: '',
  status: '',
  isOpen: '',
})

const canSave = computed(() => isAuth('lvyou:merchant:save') || isAuth('lvyou:merchant:page') || isAuth('lvyou:merchant:adjust'))
const canUpdate = computed(() => isAuth('lvyou:merchant:update') || isAuth('lvyou:merchant:page') || isAuth('lvyou:merchant:adjust'))
const canDelete = computed(() => isAuth('lvyou:merchant:delete') || isAuth('lvyou:merchant:page') || isAuth('lvyou:merchant:adjust'))
const canAdjust = computed(() => isAuth('lvyou:merchant:adjust'))
const canViewOrders = computed(() => isAuth('lvyou:order:page'))

const canEnable = computed(() => canAdjust.value && detail.value?.status === 2)
const canDisable = computed(() => canAdjust.value && detail.value?.status === 1)
function canRowOpen(row) {
  return canAdjust.value && row?.isOpen === 0
}

function canRowClose(row) {
  return canAdjust.value && row?.isOpen === 1
}

watch(
  () => form.storeType,
  (next, prev) => {
    if (!prev || next === prev) return
    form.qualImages = {}
    if (next !== 'culture') {
      form.shopCategory = ''
    }
  },
)

function statusTagType(status) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

function openTagType(isOpen) {
  return isOpen === 1 ? 'success' : 'warning'
}

function depositTagType(status) {
  return status === 'paid' ? 'success' : 'warning'
}

function formatDepositAmount(value) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '—'
  return `¥${num.toFixed(2)}`
}

function openDepositAdjust(row) {
  depositForm.merchantId = row.id
  depositForm.storeName = row.storeName || ''
  depositForm.adjustType = 'paid_increase'
  depositForm.amount = 100
  depositForm.remark = ''
  depositDialogVisible.value = true
}

async function submitDepositAdjust() {
  const valid = await validateForm(depositFormRef)
  if (!valid) return
  depositDialogLoading.value = true
  try {
    await adjustMerchantDeposit({
      merchantId: depositForm.merchantId,
      adjustType: depositForm.adjustType,
      amount: Number(depositForm.amount),
      remark: depositForm.remark || undefined,
    })
    showSuccess('保证金已调整')
    depositDialogVisible.value = false
    await loadData()
    if (detail.value?.id === depositForm.merchantId) {
      detail.value = await getMerchantDetail(depositForm.merchantId)
    }
  } catch (error) {
    showError(error?.msg || error?.message || '调整失败')
  } finally {
    depositDialogLoading.value = false
  }
}

function openRefundAudit(row) {
  refundTarget.value = row
  refundRemark.value = ''
  refundDialogVisible.value = true
}

async function submitRefundAudit(action) {
  const row = refundTarget.value
  if (!row?.pendingDepositRefundNo) return
  if (action === 'approve' && Number(row.unprocessedOrderCount || 0) > 0) {
    showError('该商户仍有未处理订单，暂不可退保')
    return
  }
  const title = action === 'approve' ? '确认通过退保' : '确认驳回退保'
  try {
    if (action === 'reject') {
      const { value } = await ElMessageBox.prompt('请填写驳回原因（将展示给商户）', title, {
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入驳回原因',
        inputValidator: (val) => {
          if (!String(val || '').trim()) return '驳回原因不能为空'
          return true
        },
      })
      refundRemark.value = String(value || '').trim()
    } else {
      await ElMessageBox.confirm(
        `确认退还保证金 ${formatDepositAmount(row.pendingDepositRefundAmount)} 至商户可提现余额吗？`,
        title,
        { type: 'warning' },
      )
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    return
  }
  refundDialogLoading.value = true
  try {
    await auditMerchantDepositRefund({
      merchantId: row.id,
      refundNo: row.pendingDepositRefundNo,
      action,
      remark: refundRemark.value || undefined,
    })
    showSuccess(action === 'approve' ? '退保已完成' : '已驳回退保申请')
    refundDialogVisible.value = false
    refundTarget.value = null
    await loadData()
    if (detail.value?.id === row.id) {
      detail.value = await getMerchantDetail(row.id)
    }
  } catch (error) {
    showError(error?.msg || error?.message || '审核失败')
  } finally {
    refundDialogLoading.value = false
  }
}

function buildQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    id: filters.id === '' ? undefined : Number(filters.id),
    storeName: filters.storeName || undefined,
    contactPhone: filters.contactPhone || undefined,
    storeType: filters.storeType || undefined,
    status: filters.status === '' ? undefined : filters.status,
    isOpen: filters.isOpen === '' ? undefined : filters.isOpen,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getMerchantPage(buildQuery())
    tableData.value = data.list || []
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载商户列表失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, {
    id: '',
    storeName: '',
    contactPhone: '',
    storeType: '',
    status: '',
    isOpen: '',
  })
  pageNum.value = 1
  loadData()
}

async function openDetail(row) {
  drawerVisible.value = true
  drawerLoading.value = true
  detail.value = null
  try {
    detail.value = await getMerchantDetail(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载商户详情失败')
    drawerVisible.value = false
  } finally {
    drawerLoading.value = false
  }
}

async function runAdjust(action, title, options = {}) {
  const { needReason = false, row = null } = options
  const target = row || detail.value
  if (!target?.id) return
  let reason = ''
  if (needReason) {
    const { value } = await ElMessageBox.prompt('请填写操作原因', title, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputType: 'textarea',
      inputPlaceholder: '请输入原因（可选）',
    })
    reason = String(value || '').trim()
  } else {
    await ElMessageBox.confirm(`确认对商户「${target.storeName}」执行「${title}」吗？`, title, {
      type: 'warning',
    })
  }
  if (row) {
    rowAdjustLoadingId.value = row.id
  } else {
    actionLoading.value = true
  }
  try {
    const result = await adjustMerchant({
      id: target.id,
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
      rowAdjustLoadingId.value = null
    } else {
      actionLoading.value = false
    }
  }
}

async function loadStoreTypeOptions() {
  try {
    const items = await getMerchantStoreTypeList()
    STORE_TYPE_OPTIONS.value = (items || []).map((item) => ({
      label: item.label,
      value: item.code,
    }))
  } catch (e) {
    STORE_TYPE_OPTIONS.value = []
  }
}

async function loadDestinationOptions() {
  try {
    const data = await destinationApi.page({ page: 1, limit: 100, status: 1 })
    destinationOptions.value = data.list || []
  } catch (e) {
    destinationOptions.value = []
  }
}

async function loadShopCategoryOptions() {
  try {
    shopCategoryOptions.value = await fetchHeritageShopCategoryOptionsApi()
  } catch (e) {
    shopCategoryOptions.value = []
  }
}

function resetForm(initial = {}) {
  const base = getEmptyApplyForm()
  Object.keys(form).forEach((key) => {
    delete form[key]
  })
  Object.assign(form, base, initial)
  if (!form.qualImages || typeof form.qualImages !== 'object') {
    form.qualImages = {}
  }
}

function formatUserLabel(user) {
  const name = user?.userName || user?.nickname || '用户'
  const phone = user?.phone ? ` / ${user.phone}` : ''
  return `${name}${phone}`
}

async function searchUsers(keyword) {
  const query = String(keyword || '').trim()
  if (!query) {
    userOptions.value = form.userId
      ? userOptions.value.filter((item) => item.userId === form.userId)
      : []
    return
  }
  userSearchLoading.value = true
  try {
    const data = await getAppUserPage({
      page: 1,
      limit: 20,
      username: query,
      phone: /^\d+$/.test(query) ? query : undefined,
    })
    userOptions.value = data.list || []
  } catch (error) {
    userOptions.value = []
  } finally {
    userSearchLoading.value = false
  }
}

async function onUserChange(userId) {
  if (!userId) return
  try {
    const user = await getAppUserInfo(userId)
    if (!user) return
    if (!String(form.contactName || '').trim()) {
      form.contactName = user.userName || user.nickname || ''
    }
    if (!String(form.contactPhone || '').trim()) {
      form.contactPhone = user.phone || ''
    }
    const exists = userOptions.value.some((item) => item.userId === user.userId)
    if (!exists) {
      userOptions.value = [user, ...userOptions.value]
    }
  } catch (error) {
    /* 忽略预填失败 */
  }
}

function onLocationChange(payload) {
  form.storeAddress = payload?.storeAddress || formatStoreAddress(form)
}

function openCreate() {
  editingId.value = null
  userOptions.value = []
  resetForm()
  formDialogVisible.value = true
}

async function openEdit(row) {
  editingId.value = row.id
  formDialogVisible.value = true
  formLoading.value = true
  try {
    const data = await getMerchantDetail(row.id)
    userOptions.value = data.userId
      ? [{ userId: data.userId, userName: data.userName, phone: data.userPhone }]
      : []
    resetForm({
      userId: data.userId || null,
      storeName: data.storeName || '',
      storeType: data.storeType || '',
      shopCategory: data.shopCategory || '',
      contactName: data.contactName || '',
      contactPhone: data.contactPhone || '',
      storeRegion: data.storeRegion || '',
      storeAddressDetail: data.storeAddressDetail || '',
      storeAddress: data.storeAddress || '',
      latitude: data.latitude != null ? Number(data.latitude) : null,
      longitude: data.longitude != null ? Number(data.longitude) : null,
      storeHours: data.storeHours || '09:00-21:00',
      licenseNo: data.licenseNo || '',
      licenseImage: data.licenseImage || '',
      legalName: data.legalName || '',
      legalIdNo: data.legalIdNo || '',
      legalIdFront: data.legalIdFront || '',
      legalIdBack: data.legalIdBack || '',
      qualImages: normalizeQualImagesForForm(data.qualImages || {}, data.storeType || ''),
      status: data.status ?? 1,
      isOpen: data.isOpen ?? 1,
    })
  } catch (error) {
    showError(error?.msg || error?.message || '加载商户信息失败')
    formDialogVisible.value = false
  } finally {
    formLoading.value = false
  }
}

function buildFormPayload() {
  const storeAddress = formatStoreAddress(form)
  return {
    userId: form.userId,
    storeName: form.storeName,
    storeType: form.storeType,
    shopCategory: form.storeType === 'culture' ? form.shopCategory : undefined,
    contactName: form.contactName,
    contactPhone: form.contactPhone,
    storeRegion: form.storeRegion,
    storeAddressDetail: form.storeAddressDetail,
    storeAddress,
    latitude: form.latitude,
    longitude: form.longitude,
    storeHours: form.storeHours || undefined,
    licenseNo: form.licenseNo,
    licenseImage: form.licenseImage,
    legalName: form.legalName,
    legalIdNo: form.legalIdNo,
    legalIdFront: form.legalIdFront,
    legalIdBack: form.legalIdBack,
    qualImages: serializeQualImagesForSubmit(form.qualImages, form.storeType),
    status: form.status,
    isOpen: form.isOpen,
  }
}

async function submitForm() {
  const valid = await validateForm(formRef)
  if (!valid) return
  const check = validateApplyForm(form)
  if (!check.ok) {
    showError(check.message)
    return
  }
  formLoading.value = true
  try {
    const payload = buildFormPayload()
    if (editingId.value) {
      await updateMerchant({ ...payload, id: editingId.value })
      showSuccess('更新成功')
    } else {
      await saveMerchant(payload)
      showSuccess('创建成功')
    }
    formDialogVisible.value = false
    loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存失败')
  } finally {
    formLoading.value = false
  }
}

function viewMerchantOrders(row) {
  router.push({
    path: '/business/order',
    query: {
      merchantId: row.id,
      merchantName: row.storeName || '',
    },
  })
}

async function handleDelete(row) {
  try {
    await confirmAction(`确认删除商户「${row.storeName}」吗？删除后不可恢复。`, '删除商户')
    await deleteMerchant({ id: row.id })
    showSuccess('删除成功')
    if (detail.value?.id === row.id) {
      drawerVisible.value = false
      detail.value = null
    }
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error?.msg || error?.message || '删除失败')
    }
  }
}

onMounted(() => {
  loadStoreTypeOptions()
  loadDestinationOptions()
  loadShopCategoryOptions()
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>商户管理</strong>
        <el-button v-if="canSave" type="primary" @click="openCreate">新增商户</el-button>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="商户ID">
        <el-input v-model="filters.id" clearable placeholder="商户ID" />
      </el-form-item>
      <el-form-item label="门店名称">
        <el-input v-model="filters.storeName" clearable placeholder="门店名称" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="filters.contactPhone" clearable placeholder="联系电话" />
      </el-form-item>
      <el-form-item label="门店类型">
        <el-select v-model="filters.storeType" clearable placeholder="全部">
          <el-option v-for="item in STORE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="账号状态">
        <el-select v-model="filters.status" clearable placeholder="全部">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="营业状态">
        <el-select v-model="filters.isOpen" clearable placeholder="全部">
          <el-option v-for="item in OPEN_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无商户">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="storeName" label="门店名称" min-width="140" />
      <el-table-column prop="storeTypeLabel" label="门店类型" min-width="100" />
      <el-table-column prop="contactName" label="联系人" min-width="100" />
      <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
      <el-table-column label="店铺地址" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ formatStoreAddress(row) || '—' }}</template>
      </el-table-column>
      <el-table-column prop="userName" label="关联用户" min-width="100" />
      <el-table-column prop="userPhone" label="用户手机" min-width="130" />
      <el-table-column label="账号状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusLabel || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="营业状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="openTagType(row.isOpen)">{{ row.isOpenLabel || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="应缴保证金" min-width="110">
        <template #default="{ row }">{{ formatDepositAmount(row.depositRequired) }}</template>
      </el-table-column>
      <el-table-column label="已缴保证金" min-width="110">
        <template #default="{ row }">{{ formatDepositAmount(row.depositPaid) }}</template>
      </el-table-column>
      <el-table-column label="保证金" min-width="100">
        <template #default="{ row }">
          <el-tag :type="depositTagType(row.depositStatus)">{{ row.depositStatusLabel || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="未处理订单" width="100">
        <template #default="{ row }">{{ row.unprocessedOrderCount ?? 0 }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="入驻时间" min-width="170" />
      <el-table-column label="操作" min-width="520" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="canViewOrders" size="small" @click="viewMerchantOrders(row)">查看订单</el-button>
            <el-button v-if="canUpdate" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="canAdjust" size="small" type="primary" plain @click="openDepositAdjust(row)">
              调整保证金
            </el-button>
            <el-button
              v-if="canAdjust && row.pendingDepositRefundStatus === 'pending'"
              size="small"
              type="warning"
              @click="openRefundAudit(row)"
            >
              审核退保
            </el-button>
            <el-button
              v-if="canRowOpen(row)"
              size="small"
              type="success"
              :loading="rowAdjustLoadingId === row.id"
              @click="runAdjust('open', '设为营业', { row })"
            >
              设为营业
            </el-button>
            <el-button
              v-if="canRowClose(row)"
              size="small"
              type="warning"
              :loading="rowAdjustLoadingId === row.id"
              @click="runAdjust('close', '设为休息', { row })"
            >
              设为休息
            </el-button>
            <el-button v-if="canDelete" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-drawer v-model="drawerVisible" title="商户详情" size="640px" destroy-on-close>
      <div v-loading="drawerLoading" class="detail-body">
        <template v-if="detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="商户ID">{{ detail.id ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="门店名称">{{ detail.storeName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="门店类型">{{ detail.storeTypeLabel || detail.storeType || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.storeType === 'culture'" label="好店分类">
              {{ detail.shopCategoryLabel || detail.shopCategory || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="所属目的地">{{ detail.destinationName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.contactPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="门店地区">{{ detail.storeRegion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{ detail.storeAddressDetail || detail.storeAddress || '—' }}</el-descriptions-item>
            <el-descriptions-item label="地图坐标">
              <template v-if="detail.latitude != null && detail.longitude != null">
                {{ detail.latitude }}, {{ detail.longitude }}
              </template>
              <template v-else>—</template>
            </el-descriptions-item>
            <el-descriptions-item label="营业时间">{{ detail.storeHours || '—' }}</el-descriptions-item>
            <el-descriptions-item label="关联用户">{{ detail.userName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="用户手机">{{ detail.userPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="账号状态">
              <el-tag :type="statusTagType(detail.status)">{{ detail.statusLabel || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="营业状态">
              <el-tag :type="openTagType(detail.isOpen)">{{ detail.isOpenLabel || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="应缴保证金">{{ formatDepositAmount(detail.depositRequired) }}</el-descriptions-item>
            <el-descriptions-item label="已缴保证金">{{ formatDepositAmount(detail.depositPaid) }}</el-descriptions-item>
            <el-descriptions-item label="保证金状态">
              <el-tag :type="depositTagType(detail.depositStatus)">{{ detail.depositStatusLabel || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="未处理订单">{{ detail.unprocessedOrderCount ?? 0 }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.pendingDepositRefundNo" label="退保申请">
              {{ formatDepositAmount(detail.pendingDepositRefundAmount) }}（{{ detail.pendingDepositRefundNo }}）
            </el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ detail.licenseNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="法人姓名">{{ detail.legalName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="入驻时间">{{ detail.createTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detail.updateTime || '—' }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="canAdjust && (canEnable || canDisable)" class="drawer-actions">
            <el-button v-if="canEnable" type="success" :loading="actionLoading" @click="runAdjust('enable', '启用账号')">
              启用账号
            </el-button>
            <el-button v-if="canDisable" type="danger" :loading="actionLoading" @click="runAdjust('disable', '禁用账号', { needReason: true })">
              禁用账号
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <AppDialog
      v-model="depositDialogVisible"
      title="调整保证金"
      width="520px"
      :loading="depositDialogLoading"
      @confirm="submitDepositAdjust"
    >
      <el-form ref="depositFormRef" :model="depositForm" :rules="depositRules" label-width="120px">
        <el-form-item label="商户">
          <el-input :model-value="depositForm.storeName" disabled />
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustType">
          <el-select v-model="depositForm.adjustType" style="width: 100%">
            <el-option v-for="item in depositAdjustOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整金额(元)" prop="amount">
          <el-input-number v-model="depositForm.amount" :min="0.01" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="depositForm.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog
      v-model="refundDialogVisible"
      title="审核退保申请"
      width="520px"
      :loading="refundDialogLoading"
      @confirm="submitRefundAudit('approve')"
    >
      <template v-if="refundTarget">
        <p class="refund-tip">商户：{{ refundTarget.storeName }}</p>
        <p class="refund-tip">申请金额：{{ formatDepositAmount(refundTarget.pendingDepositRefundAmount) }}</p>
        <p class="refund-tip">未处理订单：{{ refundTarget.unprocessedOrderCount ?? 0 }}</p>
        <p v-if="Number(refundTarget.unprocessedOrderCount || 0) > 0" class="refund-warn">
          存在未处理订单时不可退保，请先处理完订单。
        </p>
      </template>
      <template #footer>
        <el-space>
          <el-button @click="refundDialogVisible = false">取消</el-button>
          <el-button type="danger" plain :loading="refundDialogLoading" @click="submitRefundAudit('reject')">
            驳回
          </el-button>
          <el-button
            type="primary"
            :loading="refundDialogLoading"
            :disabled="Number(refundTarget?.unprocessedOrderCount || 0) > 0"
            @click="submitRefundAudit('approve')"
          >
            通过并退款
          </el-button>
        </el-space>
      </template>
    </AppDialog>

    <AppDialog
      v-model="formDialogVisible"
      :title="editingId ? '编辑商户' : '新增商户'"
      width="920px"
      :loading="formLoading"
      @confirm="submitForm"
    >
      <div class="merchant-form-scroll">
        <el-form ref="formRef" v-loading="formLoading" :model="form" :rules="formRules" label-width="130px">
          <div class="form-section-title">基础信息</div>
          <el-form-item label="关联用户" prop="userId">
            <el-select
              v-model="form.userId"
              filterable
              remote
              reserve-keyword
              clearable
              :remote-method="searchUsers"
              :loading="userSearchLoading"
              placeholder="搜索用户名或手机号"
              style="width: 100%"
              @change="onUserChange"
            >
              <el-option
                v-for="item in userOptions"
                :key="item.userId"
                :label="formatUserLabel(item)"
                :value="item.userId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="门店类型" prop="storeType">
            <el-select v-model="form.storeType" placeholder="请选择门店类型" style="width: 100%">
              <el-option v-for="item in STORE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.storeType === 'culture'" label="好店分类" prop="shopCategory">
            <el-select
              v-model="form.shopCategory"
              filterable
              clearable
              placeholder="选择好店分类"
              style="width: 100%"
            >
              <el-option v-for="item in shopCategoryOptions" :key="item.code" :label="item.name" :value="item.code" />
            </el-select>
          </el-form-item>
          <el-alert
            v-if="currentQual.note"
            :title="currentQual.note"
            type="warning"
            :closable="false"
            show-icon
            class="form-alert"
          />

          <div class="form-section-title">联系人信息</div>
          <el-form-item label="商户联系人" prop="contactName">
            <el-input v-model="form.contactName" placeholder="请输入联系人姓名" />
          </el-form-item>
          <el-form-item label="联系方式" prop="contactPhone">
            <el-input v-model="form.contactPhone" maxlength="11" placeholder="请输入联系电话" />
          </el-form-item>

          <div class="form-section-title">门店信息</div>
          <el-form-item label="门店名称" prop="storeName">
            <el-input v-model="form.storeName" placeholder="请输入门店名称" />
          </el-form-item>
          <el-form-item label="门店位置" required>
            <MapLocationPicker
              v-model:latitude="form.latitude"
              v-model:longitude="form.longitude"
              v-model:store-region="form.storeRegion"
              v-model:store-address-detail="form.storeAddressDetail"
              v-model:store-address="form.storeAddress"
              @change="onLocationChange"
            />
          </el-form-item>
          <el-form-item label="所在地区" prop="storeRegion">
            <el-input v-model="form.storeRegion" readonly placeholder="地图选点后自动填充" />
          </el-form-item>
          <el-form-item label="详细地址" prop="storeAddressDetail">
            <el-input v-model="form.storeAddressDetail" readonly placeholder="地图选点后自动填充，不可手填" />
          </el-form-item>
          <el-form-item label="营业时间">
            <el-input v-model="form.storeHours" placeholder="如 09:00-21:00" />
          </el-form-item>

          <div class="form-section-title">营业执照 · 法人信息</div>
          <el-form-item label="统一社会信用代码" prop="licenseNo">
            <el-input v-model="form.licenseNo" placeholder="请输入统一社会信用代码" />
          </el-form-item>
          <el-form-item label="营业执照照片" prop="licenseImage">
            <ImageUpload v-model="form.licenseImage" />
          </el-form-item>
          <el-form-item label="法人姓名" prop="legalName">
            <el-input v-model="form.legalName" placeholder="请输入法人姓名" />
          </el-form-item>
          <el-form-item label="法人身份证号" prop="legalIdNo">
            <el-input v-model="form.legalIdNo" maxlength="18" placeholder="请输入法人身份证号" />
          </el-form-item>
          <el-form-item label="身份证人像面" prop="legalIdFront">
            <ImageUpload v-model="form.legalIdFront" />
          </el-form-item>
          <el-form-item label="身份证国徽面" prop="legalIdBack">
            <ImageUpload v-model="form.legalIdBack" />
          </el-form-item>

          <template v-if="form.storeType">
            <div class="form-section-title">{{ currentQual.title || '行业资质' }}</div>
            <el-form-item
              v-for="item in qualUploadItems"
              :key="item.key"
              :label="item.label"
              :required="item.required"
            >
              <ImageUpload
                :model-value="form.qualImages[item.key]"
                :multiple="isQualMultiItem(item)"
                :limit="getQualMaxCount(item)"
                @update:model-value="(value) => { form.qualImages[item.key] = value }"
              />
              <div v-if="item.desc" class="field-hint">{{ item.desc }}</div>
            </el-form-item>
          </template>

          <div class="form-section-title">运营状态</div>
          <el-form-item label="账号状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="营业状态">
            <el-select v-model="form.isOpen" style="width: 100%">
              <el-option v-for="item in OPEN_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>
    </AppDialog>
  </el-card>
</template>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.detail-body {
  min-height: 200px;
}

.drawer-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.merchant-form-scroll {
  max-height: 70vh;
  overflow-y: auto;
  padding-right: 8px;
}

.refund-tip {
  margin: 0 0 8px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.refund-warn {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--el-color-danger);
}

.form-section-title {
  margin: 8px 0 16px;
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.form-alert {
  margin-bottom: 16px;
}

.field-hint {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
