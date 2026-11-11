<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import BasePagination from '@/components/common/BasePagination.vue'
import { getCanteenDetail, getCanteenPage, postCanteenAction } from '@/api/modules/canteen'
import { isAuth } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'

/** 预约状态筛选项（对齐 CanteenReservationStatusSupport） */
const STATUS_OPTIONS = [
  { label: '待确认', value: 'pending' },
  { label: '待到店', value: 'confirmed' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const actionLoading = ref(false)
const detail = ref(null)

const filters = reactive({
  id: '',
  shopName: '',
  contactPhone: '',
  userPhone: '',
  merchantName: '',
  status: '',
})

const canAction = computed(() => isAuth('lvyou:canteen:action'))

const canConfirm = computed(() => detail.value?.status === 'pending')

const canCancel = computed(() => {
  const status = detail.value?.status
  return status && !['completed', 'cancelled'].includes(status)
})

const canComplete = computed(() => {
  const status = detail.value?.status
  return status && !['completed', 'cancelled'].includes(status)
})

function statusTagType(status) {
  if (status === 'pending') return 'warning'
  if (status === 'confirmed') return 'primary'
  if (status === 'completed') return 'success'
  if (status === 'cancelled') return 'info'
  return 'info'
}

function buildQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    id: filters.id === '' ? undefined : Number(filters.id),
    shopName: filters.shopName || undefined,
    contactPhone: filters.contactPhone || undefined,
    userPhone: filters.userPhone || undefined,
    merchantName: filters.merchantName || undefined,
    status: filters.status || undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getCanteenPage(buildQuery())
    tableData.value = data.list || []
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载餐饮预约列表失败')
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
    shopName: '',
    contactPhone: '',
    userPhone: '',
    merchantName: '',
    status: '',
  })
  pageNum.value = 1
  loadData()
}

async function openDetail(row) {
  drawerVisible.value = true
  drawerLoading.value = true
  detail.value = null
  try {
    detail.value = await getCanteenDetail(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载预约详情失败')
    drawerVisible.value = false
  } finally {
    drawerLoading.value = false
  }
}

async function runAction(action, title, needReason = false) {
  if (!detail.value?.id) return
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
    await ElMessageBox.confirm(
      `确认对预约 ID ${detail.value.id} 执行「${title}」吗？`,
      title,
      { type: 'warning' },
    )
  }
  actionLoading.value = true
  try {
    detail.value = await postCanteenAction({
      reservationId: detail.value.id,
      action,
      reason: reason || undefined,
    })
    showSuccess(`${title}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      showError(error?.msg || error?.message || `${title}失败`)
    }
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>餐饮预约</strong>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="预约ID">
        <el-input v-model="filters.id" clearable placeholder="预约ID" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="门店名称">
        <el-input v-model="filters.shopName" clearable placeholder="门店名称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="联系人手机">
        <el-input v-model="filters.contactPhone" clearable placeholder="联系人手机号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="用户手机">
        <el-input v-model="filters.userPhone" clearable placeholder="下单用户手机号" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="商户名称">
        <el-input v-model="filters.merchantName" clearable placeholder="商户名称" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="预约状态">
        <el-select v-model="filters.status" clearable placeholder="全部">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无餐饮预约">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="shopName" label="门店名称" min-width="140" show-overflow-tooltip />
      <el-table-column prop="merchantName" label="商户名称" min-width="120" show-overflow-tooltip />
      <el-table-column prop="contactName" label="联系人" min-width="100" />
      <el-table-column prop="phone" label="联系电话" min-width="130" />
      <el-table-column prop="userPhone" label="用户手机" min-width="130" />
      <el-table-column prop="guestCount" label="用餐人数" min-width="90" />
      <el-table-column prop="reserveTime" label="预约时间" min-width="160" />
      <el-table-column label="预约状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ row.statusLabel || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" min-width="160" />
      <el-table-column label="操作" min-width="100" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="isAuth('lvyou:canteen:info')" size="small" type="primary" @click="openDetail(row)">详情</el-button>
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

    <el-drawer v-model="drawerVisible" title="餐饮预约详情" size="640px" destroy-on-close>
      <div v-loading="drawerLoading" class="detail-body">
        <template v-if="detail">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="预约ID" :span="2">{{ detail.id ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="预约状态">
              <el-tag :type="statusTagType(detail.status)">{{ detail.statusLabel || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="关联订单ID">{{ detail.orderId ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="门店名称">{{ detail.shopName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="商户名称">{{ detail.merchantName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="目的地">{{ detail.destinationName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="用户昵称">{{ detail.userName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="用户手机">{{ detail.userPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="用餐人数">{{ detail.guestCount ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="预约时间">{{ detail.reserveTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="用餐地址" :span="2">{{ detail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.createdAt || '—' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detail.updateTime || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.remark" label="备注" :span="2">{{ detail.remark }}</el-descriptions-item>
          </el-descriptions>

          <div v-if="canAction && (canConfirm || canCancel || canComplete)" class="drawer-actions">
            <el-button
              v-if="canConfirm"
              type="primary"
              :loading="actionLoading"
              @click="runAction('confirm', '确认预约')"
            >
              确认预约
            </el-button>
            <el-button
              v-if="canComplete"
              type="success"
              :loading="actionLoading"
              @click="runAction('complete', '标记完成')"
            >
              标记完成
            </el-button>
            <el-button
              v-if="canCancel"
              type="danger"
              :loading="actionLoading"
              @click="runAction('cancel', '取消预约', true)"
            >
              取消预约
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>
  </el-card>
</template>

<style scoped lang="scss">
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
</style>
