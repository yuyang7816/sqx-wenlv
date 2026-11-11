<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import BasePagination from '@/components/common/BasePagination.vue'
import { auditCancelAccount, getCancelAccountPage } from '@/api/modules/cancel-account'
import { isAuth } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'
import { useMenuBadgeStore } from '@/stores/menuBadge'

const menuBadgeStore = useMenuBadgeStore()

const STATUS_OPTIONS = [
  { label: '待审核', value: 'pending' },
  { label: '已通过', value: 'approved' },
  { label: '已驳回', value: 'rejected' },
]

const loading = ref(false)
const auditLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const detail = ref(null)

const filters = reactive({
  status: '',
  userPhone: '',
  userName: '',
})

function statusLabel(status) {
  return STATUS_OPTIONS.find((item) => item.value === status)?.label || status || '—'
}

function statusTagType(status) {
  if (status === 'pending') return 'warning'
  if (status === 'approved') return 'success'
  if (status === 'rejected') return 'danger'
  return 'info'
}

function buildQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    status: filters.status || undefined,
    userPhone: filters.userPhone || undefined,
    userName: filters.userName || undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getCancelAccountPage(buildQuery())
    tableData.value = data.list || []
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载注销申请失败')
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
  Object.assign(filters, { status: '', userPhone: '', userName: '' })
  pageNum.value = 1
  loadData()
}

function openDetail(row) {
  detail.value = { ...row }
  drawerVisible.value = true
}

function canAuditRow(row) {
  return isAuth('base:cancelAccount:audit') && row?.status === 'pending'
}

const canAudit = () => isAuth('base:cancelAccount:audit') && detail.value?.status === 'pending'

async function submitAudit(id, action, reason) {
  auditLoading.value = true
  try {
    await auditCancelAccount({ id, action, reason: reason || undefined })
    showSuccess(action === 'pass' ? '已通过注销申请' : '已驳回注销申请')
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
  await ElMessageBox.confirm(
    `确认通过用户「${target.userName || target.phone || target.userId}」的注销申请吗？通过后账号将无法登录。`,
    '审核通过',
    { type: 'warning' },
  )
  await submitAudit(target.id, 'pass')
}

async function handleReject(row) {
  const target = row || detail.value
  if (!target?.id) return
  const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回注销', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入驳回原因',
    inputValidator: (val) => {
      if (!String(val || '').trim()) return '驳回原因不能为空'
      return true
    },
  })
  await submitAudit(target.id, 'reject', String(value).trim())
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>注销审核</strong>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="审核状态">
        <el-select v-model="filters.status" clearable placeholder="全部">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.userPhone" clearable placeholder="用户手机号" />
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="filters.userName" clearable placeholder="用户昵称" />
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无注销申请">
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="userName" label="昵称" min-width="120" />
      <el-table-column prop="reason" label="注销原因" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="审核状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" min-width="170" />
      <el-table-column prop="auditTime" label="审核时间" min-width="170" />
      <el-table-column label="操作" min-width="220" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="info" @click="openDetail(row)">详情</el-button>
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

    <el-drawer v-model="drawerVisible" title="注销申请详情" size="520px" destroy-on-close>
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户ID">{{ detail.userId || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detail.userName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="注销原因">{{ detail.reason || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ detail.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ detail.auditTime || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.auditRemark" label="审核备注">{{ detail.auditRemark }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="canAudit()" class="drawer-actions">
          <el-button type="success" :loading="auditLoading" @click="handleApprove">通过</el-button>
          <el-button type="danger" :loading="auditLoading" @click="handleReject">驳回</el-button>
        </div>
      </div>
    </el-drawer>
  </el-card>
</template>

<style scoped lang="scss">
.detail-body {
  min-height: 200px;
}

.drawer-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
</style>
