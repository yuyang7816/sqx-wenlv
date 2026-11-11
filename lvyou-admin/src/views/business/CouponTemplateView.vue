<script setup>
import { reactive, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import { couponTemplateApi } from '@/api/modules/coupon'
import { formatCouponStatus } from '@/utils/status-label.js'

const crudRef = ref()

/** 筛选：ID、名称、适用范围、状态 */
const filters = reactive({
  id: '',
  name: '',
  scope: '',
  status: '',
})

/** 适用范围：对齐 server CouponScope，不含商城遗留券类型字段 */
const SCOPE_OPTIONS = [
  { label: '全场通用', value: 'all' },
  { label: '酒店预订', value: 'hotel' },
  { label: '旅游线路', value: 'route' },
  { label: '非遗好物', value: 'goods' },
]

const STATUS_OPTIONS = [
  { label: '可领取', value: 1 },
  { label: '已下架', value: 0 },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '优惠券名称', minWidth: 140, showOverflowTooltip: true },
  {
    prop: 'amount',
    label: '优惠金额',
    width: 100,
    formatter: (_row, _col, val) => (val != null ? `¥${val}` : '-'),
  },
  {
    prop: 'minAmount',
    label: '最低消费',
    width: 100,
    formatter: (_row, _col, val) => (val != null ? `¥${val}` : '-'),
  },
  { prop: 'scopeLabel', label: '适用范围', width: 110 },
  { prop: 'validDays', label: '有效天数', width: 90 },
  {
    prop: 'totalCount',
    label: '发放总量',
    width: 90,
    formatter: (_row, _col, val) => (val === 0 ? '不限' : val ?? '-'),
  },
  { prop: 'claimedCount', label: '已领取', width: 80 },
  { prop: 'perUserLimit', label: '每人限领', width: 90 },
  { prop: 'statusLabel', label: '状态', width: 90, formatter: formatCouponStatus },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const formRules = {
  templateCode: [{ required: true, message: '请输入模板业务编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入优惠金额', trigger: 'blur' }],
  minAmount: [{ required: true, message: '请输入最低消费门槛', trigger: 'blur' }],
  scope: [{ required: true, message: '请选择适用范围', trigger: 'change' }],
  validDays: [{ required: true, message: '请输入有效天数', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  perUserLimit: [{ required: true, message: '请输入每人限领张数', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function buildQuery() {
  return {
    id: filters.id === '' ? undefined : Number(filters.id),
    name: filters.name || undefined,
    scope: filters.scope || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { id: '', name: '', scope: '', status: '' })
  crudRef.value?.searchData({})
}

/** 剥离列表展示字段，仅提交业务字段 */
function buildSubmitPayload(form) {
  const {
    scopeLabel,
    statusLabel,
    claimedCount,
    createTime,
    updateTime,
    ...rest
  } = form
  return {
    ...rest,
    description: rest.description || '',
  }
}
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="优惠券模板"
    :api="couponTemplateApi"
    :columns="columns"
    edit-from-row
    :build-payload="buildSubmitPayload"
    :create-defaults="{
      scope: 'all',
      validDays: 30,
      totalCount: 0,
      perUserLimit: 1,
      status: 1,
      minAmount: 0,
    }"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="ID">
          <el-input v-model="filters.id" clearable placeholder="模板ID" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="filters.name" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="适用范围">
          <el-select v-model="filters.scope" clearable placeholder="全部" style="width: 130px">
            <el-option v-for="item in SCOPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #form="{ form, editingId }">
      <el-form-item label="模板编码" prop="templateCode" :rules="formRules.templateCode">
        <el-input
          v-model="form.templateCode"
          :disabled="!!editingId"
          placeholder="如 NEW_USER_2026"
        />
      </el-form-item>
      <el-form-item label="优惠券名称" prop="name" :rules="formRules.name">
        <el-input v-model="form.name" placeholder="如 新人满减券" />
      </el-form-item>
      <el-form-item label="优惠金额" prop="amount" :rules="formRules.amount">
        <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="1" style="width: 100%" />
      </el-form-item>
      <el-form-item label="最低消费" prop="minAmount" :rules="formRules.minAmount">
        <el-input-number v-model="form.minAmount" :min="0" :precision="2" :step="1" style="width: 100%" />
      </el-form-item>
      <el-form-item label="适用范围" prop="scope" :rules="formRules.scope">
        <el-select v-model="form.scope" placeholder="选择适用范围" style="width: 100%">
          <el-option v-for="item in SCOPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="有效天数" prop="validDays" :rules="formRules.validDays">
        <el-input-number v-model="form.validDays" :min="1" :step="1" style="width: 100%" />
        <div class="field-tip">用户领取后 N 天内有效</div>
      </el-form-item>
      <el-form-item label="发放总量" prop="totalCount" :rules="formRules.totalCount">
        <el-input-number v-model="form.totalCount" :min="0" :step="100" style="width: 100%" />
        <div class="field-tip">0 表示不限量</div>
      </el-form-item>
      <el-form-item label="每人限领" prop="perUserLimit" :rules="formRules.perUserLimit">
        <el-input-number v-model="form.perUserLimit" :min="1" :step="1" style="width: 100%" />
      </el-form-item>
      <el-form-item label="状态" prop="status" :rules="formRules.status">
        <el-select v-model="form.status" style="width: 100%">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="使用说明">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="领取与使用规则说明"
        />
      </el-form-item>
    </template>
  </CrudPage>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}

.field-tip {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
