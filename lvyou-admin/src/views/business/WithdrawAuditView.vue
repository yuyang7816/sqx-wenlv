<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import BasePagination from '@/components/common/BasePagination.vue'
import TableImageCell from '@/components/common/TableImageCell.vue'
import { auditWithdraw, getWithdrawPage } from '@/api/modules/withdraw'
import { isAuth } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'
import { useMenuBadgeStore } from '@/stores/menuBadge'

const menuBadgeStore = useMenuBadgeStore()

/** 主体类型 Tab：用户提现 / 商户提现 */
const OWNER_TABS = [
  { name: 'user', label: '用户提现', ownerType: 'user' },
  { name: 'merchant', label: '商户提现', ownerType: 'merchant' },
]

/** 提现状态筛选项（对齐 WithdrawStatus） */
const STATUS_OPTIONS = [
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'success' },
  { label: '已驳回', value: 'failed' },
]

/** 提现方式筛选项（对齐 WithdrawMethod） */
const METHOD_OPTIONS = [
  { label: '支付宝', value: 'alipay' },
  { label: '微信', value: 'wechat' },
]

const activeOwnerTab = ref('user')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const auditLoading = ref(false)
const detail = ref(null)

const filters = reactive({
  id: '',
  status: '',
  method: '',
  userPhone: '',
  merchantName: '',
})

const currentOwnerType = computed(() => {
  return OWNER_TABS.find((item) => item.name === activeOwnerTab.value)?.ownerType || 'user'
})

const isUserTab = computed(() => currentOwnerType.value === 'user')

const canAudit = computed(() => isAuth('lvyou:withdraw:audit') && detail.value?.status === 'pending')

function statusLabel(status) {
  return STATUS_OPTIONS.find((item) => item.value === status)?.label || status || '—'
}

function methodLabel(method) {
  return METHOD_OPTIONS.find((item) => item.value === method)?.label || method || '—'
}

function statusTagType(status) {
  if (status === 'pending') return 'warning'
  if (status === 'success') return 'success'
  if (status === 'failed') return 'danger'
  return 'info'
}

function formatAmount(value) {
  if (value == null || value === '') return '—'
  return `¥${Number(value).toFixed(2)}`
}

function buildQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    ownerType: currentOwnerType.value,
    id: filters.id === '' ? undefined : Number(filters.id),
    status: filters.status || undefined,
    method: filters.method || undefined,
    userPhone: isUserTab.value ? filters.userPhone || undefined : undefined,
    merchantName: !isUserTab.value ? filters.merchantName || undefined : undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getWithdrawPage(buildQuery())
    tableData.value = data.list || []
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载提现列表失败')
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
    status: '',
    method: '',
    userPhone: '',
    merchantName: '',
  })
  pageNum.value = 1
  loadData()
}

function handleOwnerTabChange() {
  pageNum.value = 1
  loadData()
}

function openDetail(row) {
  detail.value = { ...row }
  drawerVisible.value = true
}

function canAuditRow(row) {
  return isAuth('lvyou:withdraw:audit') && row?.status === 'pending'
}

async function submitAudit(id, withdrawNo, amount, action, reason) {
  auditLoading.value = true
  try {
    await auditWithdraw({ id, action, reason: reason || undefined })
    showSuccess(action === 'pass' ? '提现已审核通过' : '提现已驳回')
    drawerVisible.value = false
    loadData()
    menuBadgeStore.refresh()
  } catch (error) {
    showError(error?.msg || error?.message || '审核操作失败')
  } finally {
    auditLoading.value = false
  }
}

async function handleApprove(row) {
  const target = row || detail.value
  if (!target?.id) return
  const ownerLabel = isUserTab.value ? '用户' : '商户'
  await ElMessageBox.confirm(
    `确认通过该${ownerLabel}提现申请（ID：${target.id}，金额：${formatAmount(target.amount)}）吗？`,
    '审核通过',
    { type: 'warning' },
  )
  await submitAudit(target.id, target.withdrawNo, target.amount, 'pass')
}

async function handleReject(row) {
  const target = row || detail.value
  if (!target?.id) return
  const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回提现', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入驳回原因',
    inputValidator: (val) => {
      if (!String(val || '').trim()) return '驳回原因不能为空'
      return true
    },
  })
  await submitAudit(target.id, target.withdrawNo, target.amount, 'reject', String(value).trim())
}

watch(activeOwnerTab, handleOwnerTabChange)

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>提现审核</strong>
      </div>
    </template>

    <el-tabs v-model="activeOwnerTab" class="owner-tabs">
      <el-tab-pane v-for="tab in OWNER_TABS" :key="tab.name" :label="tab.label" :name="tab.name" />
    </el-tabs>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="提现ID">
        <el-input v-model="filters.id" clearable placeholder="提现ID" />
      </el-form-item>
      <el-form-item label="审核状态">
        <el-select v-model="filters.status" clearable placeholder="全部">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="提现方式">
        <el-select v-model="filters.method" clearable placeholder="全部">
          <el-option v-for="item in METHOD_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="isUserTab" label="用户手机号">
        <el-input v-model="filters.userPhone" clearable placeholder="用户手机号" />
      </el-form-item>
      <el-form-item v-else label="商户名称">
        <el-input v-model="filters.merchantName" clearable placeholder="商户名称" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无提现记录">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column v-if="isUserTab" prop="userPhone" label="用户手机号" min-width="130" />
      <el-table-column v-if="isUserTab" prop="userName" label="用户昵称" min-width="110" />
      <el-table-column v-if="!isUserTab" prop="merchantName" label="商户名称" min-width="140" />
      <el-table-column prop="amount" label="提现金额" min-width="110">
        <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="feeAmount" label="手续费" min-width="100">
        <template #default="{ row }">{{ formatAmount(row.feeAmount || 0) }}</template>
      </el-table-column>
      <el-table-column prop="actualAmount" label="实际到账" min-width="110">
        <template #default="{ row }">{{ formatAmount(row.actualAmount ?? row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="method" label="提现方式" min-width="100">
        <template #default="{ row }">{{ methodLabel(row.method) }}</template>
      </el-table-column>
      <el-table-column label="微信收款码" min-width="100" align="center">
        <template #default="{ row }">
          <TableImageCell v-if="row.method === 'wechat'" :value="row.qrCodeUrl" />
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="account" label="到账账户" min-width="140" show-overflow-tooltip />
      <el-table-column prop="realName" label="收款姓名" min-width="100" />
      <el-table-column prop="status" label="审核状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" min-width="170" />
      <el-table-column prop="auditTime" label="审核时间" min-width="170" />
      <el-table-column label="操作" min-width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="canAuditRow(row)">
              <el-button size="small" type="success" :loading="auditLoading" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" :loading="auditLoading" @click="handleReject(row)">驳回</el-button>
            </template>
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

    <el-drawer v-model="drawerVisible" title="提现详情" size="560px" destroy-on-close>
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="提现ID">{{ detail.id ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="主体类型">{{ isUserTab ? '用户' : '商户' }}</el-descriptions-item>
          <el-descriptions-item v-if="isUserTab" label="用户手机号">{{ detail.userPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="isUserTab" label="用户昵称">{{ detail.userName || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="!isUserTab" label="商户名称">{{ detail.merchantName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="提现金额">{{ formatAmount(detail.amount) }}</el-descriptions-item>
          <el-descriptions-item label="手续费">{{ formatAmount(detail.feeAmount || 0) }}</el-descriptions-item>
          <el-descriptions-item label="实际到账">{{ formatAmount(detail.actualAmount ?? detail.amount) }}</el-descriptions-item>
          <el-descriptions-item label="提现方式">{{ methodLabel(detail.method) }}</el-descriptions-item>
          <el-descriptions-item label="到账账户">{{ detail.account || '—' }}</el-descriptions-item>
          <el-descriptions-item label="收款姓名">{{ detail.realName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detail.auditTime || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.remark" label="备注">{{ detail.remark }}</el-descriptions-item>
        </el-descriptions>

        <section v-if="detail.method === 'wechat' && detail.qrCodeUrl" class="qr-section">
          <h4>微信收款码</h4>
          <el-image
            :src="detail.qrCodeUrl"
            :preview-src-list="[detail.qrCodeUrl]"
            fit="contain"
            class="qr-image"
            preview-teleported
          />
        </section>

        <div v-if="canAudit" class="drawer-actions">
          <el-button type="success" :loading="auditLoading" @click="handleApprove">通过</el-button>
          <el-button type="danger" :loading="auditLoading" @click="handleReject">驳回</el-button>
        </div>
      </div>
    </el-drawer>
  </el-card>
</template>

<style scoped lang="scss">
.owner-tabs {
  margin-bottom: 16px;
}

.detail-body {
  min-height: 200px;
}

.qr-section {
  margin-top: 20px;

  h4 {
    margin: 0 0 12px;
    font-size: 15px;
    font-weight: 600;
  }
}

.qr-image {
  width: 200px;
  height: 200px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
}

.drawer-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.muted {
  color: var(--el-text-color-placeholder);
}
</style>
