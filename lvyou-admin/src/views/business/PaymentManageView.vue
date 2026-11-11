<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getPaymentInfo, getPaymentPage } from '@/api/modules/payment'
import { showError } from '@/utils/modal'

/** 业务类型筛选项（对齐 BusinessType） */
const BUSINESS_TYPE_OPTIONS = [
  { label: '文旅订单', value: 'tourism' },
  { label: '钱包充值', value: 'wallet_recharge' },
]

/** 支付方式筛选项 */
const PAY_METHOD_OPTIONS = [
  { label: '微信支付', value: 'wx' },
  { label: '支付宝', value: 'ali' },
  { label: '余额支付', value: 'wallet' },
]

const recordLoading = ref(false)
const recordData = ref([])
const recordTotal = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const detail = ref(null)

const recordFilters = reactive({
  orderNo: '',
  businessType: '',
  payMethod: '',
  userPhone: '',
  dateRange: [],
})

function businessTypeLabel(value) {
  return BUSINESS_TYPE_OPTIONS.find((item) => item.value === value)?.label || value || '—'
}

function payMethodLabel(value) {
  return PAY_METHOD_OPTIONS.find((item) => item.value === value)?.label || value || '—'
}

function formatAmount(value) {
  if (value == null || value === '') return '—'
  return `¥${Number(value).toFixed(2)}`
}

function buildRecordQuery() {
  const query = {
    page: pageNum.value,
    limit: pageSize.value,
    payNo: recordFilters.orderNo.trim() || undefined,
    businessType: recordFilters.businessType || undefined,
    payMethod: recordFilters.payMethod || undefined,
    userPhone: recordFilters.userPhone || undefined,
  }
  if (recordFilters.dateRange?.length === 2) {
    query.payTimeStart = `${recordFilters.dateRange[0]} 00:00:00`
    query.payTimeEnd = `${recordFilters.dateRange[1]} 23:59:59`
  }
  return query
}

async function loadRecords() {
  recordLoading.value = true
  try {
    const data = await getPaymentPage(buildRecordQuery())
    recordData.value = data.list || []
    recordTotal.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载支付流水失败')
    recordData.value = []
    recordTotal.value = 0
  } finally {
    recordLoading.value = false
  }
}

function handleRecordSearch() {
  pageNum.value = 1
  loadRecords()
}

function handleRecordReset() {
  Object.assign(recordFilters, {
    orderNo: '',
    businessType: '',
    payMethod: '',
    userPhone: '',
    dateRange: [],
  })
  pageNum.value = 1
  loadRecords()
}

async function openDetail(row) {
  drawerVisible.value = true
  drawerLoading.value = true
  detail.value = null
  try {
    detail.value = await getPaymentInfo(row.payNo, row.businessType)
  } catch (error) {
    showError(error?.msg || error?.message || '加载支付详情失败')
    drawerVisible.value = false
  } finally {
    drawerLoading.value = false
  }
}

onMounted(() => {
  loadRecords()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>支付监管</strong>
      </div>
    </template>

    <el-form :inline="true" class="search-grid" @submit.prevent="handleRecordSearch">
      <el-form-item label="订单编号">
        <el-input v-model="recordFilters.orderNo" clearable placeholder="订单编号" />
      </el-form-item>
      <el-form-item label="业务类型">
        <el-select v-model="recordFilters.businessType" clearable placeholder="全部">
          <el-option v-for="item in BUSINESS_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付方式">
        <el-select v-model="recordFilters.payMethod" clearable placeholder="全部">
          <el-option v-for="item in PAY_METHOD_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户手机">
        <el-input v-model="recordFilters.userPhone" clearable placeholder="手机号" />
      </el-form-item>
      <el-form-item label="支付时间">
        <el-date-picker
          v-model="recordFilters.dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始"
          end-placeholder="结束"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleRecordSearch">查询</el-button>
        <el-button @click="handleRecordReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="recordLoading" :data="recordData" border stripe empty-text="暂无支付流水">
      <el-table-column prop="payNo" label="订单编号" min-width="160" show-overflow-tooltip />
      <el-table-column label="业务类型" min-width="110">
        <template #default="{ row }">{{ row.businessTypeLabel || businessTypeLabel(row.businessType) }}</template>
      </el-table-column>
      <el-table-column label="支付方式" min-width="100">
        <template #default="{ row }">{{ row.payMethodLabel || payMethodLabel(row.payMethod) }}</template>
      </el-table-column>
      <el-table-column label="金额" min-width="100">
        <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
      </el-table-column>
      <el-table-column prop="userPhone" label="用户手机" min-width="120" />
      <el-table-column prop="userName" label="用户昵称" min-width="100" show-overflow-tooltip />
      <el-table-column prop="payTime" label="支付时间" min-width="170" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" @click="openDetail(row)">详情</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      v-model:page="pageNum"
      v-model:page-size="pageSize"
      :total="recordTotal"
      @change="loadRecords"
    />

    <el-drawer v-model="drawerVisible" title="支付详情" size="480px">
      <div v-loading="drawerLoading">
        <el-descriptions v-if="detail" :column="1" border>
          <el-descriptions-item label="订单编号">{{ detail.payNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ detail.businessTypeLabel || businessTypeLabel(detail.businessType) }}</el-descriptions-item>
          <el-descriptions-item label="支付方式">{{ detail.payMethodLabel || payMethodLabel(detail.payMethod) }}</el-descriptions-item>
          <el-descriptions-item label="支付金额">{{ formatAmount(detail.amount) }}</el-descriptions-item>
          <el-descriptions-item label="用户手机">{{ detail.userPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="用户昵称">{{ detail.userName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="第三方交易号">{{ detail.tradeNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ detail.payTime || '—' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.businessType === 'tourism'" label="订单分类">
            {{ detail.orderCategoryLabel || detail.orderCategory || '—' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.businessType === 'tourism'" label="商户名称">
            {{ detail.merchantName || '—' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </el-card>
</template>

