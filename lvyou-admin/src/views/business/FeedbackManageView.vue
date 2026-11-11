<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import BasePagination from '@/components/common/BasePagination.vue'
import TableImageCell from '@/components/common/TableImageCell.vue'
import { getFeedbackPage, processFeedback } from '@/api/modules/feedback'
import { isAuth } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'

const TYPE_OPTIONS = [
  { label: '功能异常', value: '功能异常' },
  { label: '界面体验', value: '界面体验' },
  { label: '订单问题', value: '订单问题' },
  { label: '建议反馈', value: '建议反馈' },
  { label: '其他', value: '其他' },
]

const STATUS_OPTIONS = [
  { label: '待处理', value: 'pending' },
  { label: '已处理', value: 'processed' },
]

const loading = ref(false)
const processLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const drawerVisible = ref(false)
const detail = ref(null)

const filters = reactive({
  type: '',
  status: '',
  contactPhone: '',
  userPhone: '',
})

function statusLabel(status) {
  return STATUS_OPTIONS.find((item) => item.value === status)?.label || status || '—'
}

function statusTagType(status) {
  if (status === 'pending') return 'warning'
  if (status === 'processed') return 'success'
  return 'info'
}

function formatUser(row) {
  if (row.userName) {
    return row.userPhone ? `${row.userName}（${row.userPhone}）` : row.userName
  }
  return row.userId ?? '—'
}

function buildQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    type: filters.type || undefined,
    status: filters.status || undefined,
    contactPhone: filters.contactPhone || undefined,
    userPhone: filters.userPhone || undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getFeedbackPage(buildQuery())
    tableData.value = data.list || []
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载意见反馈失败')
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
  Object.assign(filters, { type: '', status: '', contactPhone: '', userPhone: '' })
  pageNum.value = 1
  loadData()
}

function openDetail(row) {
  detail.value = { ...row }
  drawerVisible.value = true
}

function canProcessRow(row) {
  return isAuth('lvyou:feedback:process') && row?.status === 'pending'
}

const canProcessDetail = () => canProcessRow(detail.value)

async function handleProcess(row) {
  const target = row || detail.value
  if (!target?.id || !canProcessRow(target)) return
  await ElMessageBox.confirm('确认将该反馈标记为已处理吗？', '标记已处理', { type: 'warning' })
  processLoading.value = true
  try {
    await processFeedback({ id: target.id })
    showSuccess('已标记为已处理')
    drawerVisible.value = false
    loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '操作失败')
  } finally {
    processLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>意见反馈</strong>
      </div>
    </template>

    <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
      <el-form-item label="反馈类型">
        <el-select v-model="filters.type" clearable placeholder="全部" style="width: 140px">
          <el-option v-for="item in TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态">
        <el-select v-model="filters.status" clearable placeholder="全部" style="width: 120px">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="filters.contactPhone" clearable placeholder="反馈页填写手机号" style="width: 180px" />
      </el-form-item>
      <el-form-item label="用户手机">
        <el-input v-model="filters.userPhone" clearable placeholder="账号绑定手机号" style="width: 180px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="type" label="类型" width="110" />
      <el-table-column prop="content" label="反馈内容" min-width="220" show-overflow-tooltip />
      <el-table-column label="用户" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">{{ formatUser(row) }}</template>
      </el-table-column>
      <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
      <el-table-column label="截图" width="120">
        <template #default="{ row }">
          <TableImageCell :value="row.images" multiple />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusLabel || statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" min-width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="info" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="canProcessRow(row)"
              size="small"
              type="success"
              :loading="processLoading"
              @click="handleProcess(row)"
            >
              标记已处理
            </el-button>
          </div>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无意见反馈" />
      </template>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />

    <el-drawer v-model="drawerVisible" title="反馈详情" size="560px" destroy-on-close>
      <div v-if="detail" class="detail-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="反馈ID">{{ detail.id || '—' }}</el-descriptions-item>
          <el-descriptions-item label="反馈类型">{{ detail.type || '—' }}</el-descriptions-item>
          <el-descriptions-item label="用户">{{ formatUser(detail) }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detail.contactPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="statusTagType(detail.status)">{{ detail.statusLabel || statusLabel(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ detail.createTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="反馈内容">{{ detail.content || '—' }}</el-descriptions-item>
          <el-descriptions-item label="截图">
            <TableImageCell :value="detail.images" multiple />
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="canProcessDetail()" class="drawer-actions">
          <el-button type="success" :loading="processLoading" @click="handleProcess()">标记已处理</el-button>
        </div>
      </div>
    </el-drawer>
  </el-card>
</template>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.filter-form {
  margin-bottom: 16px;
}

.detail-body {
  min-height: 200px;
}

.drawer-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.table-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
