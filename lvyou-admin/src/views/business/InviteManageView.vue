<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { getInviteCommissionPage, getInviteRelationPage } from '@/api/modules/invite'
import { showError } from '@/utils/modal'

/** Tab：邀请关系 / 佣金明细 */
const TABS = [
  { name: 'relation', label: '邀请关系' },
  { name: 'commission', label: '佣金明细' },
]

/** 佣金层级筛选项 */
const LEVEL_OPTIONS = [
  { label: '一级', value: 1 },
  { label: '二级', value: 2 },
]

/** 佣金状态筛选项 */
const STATUS_OPTIONS = [
  { label: '已结算', value: 'settled' },
]

const activeTab = ref('relation')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const relationFilters = reactive({
  inviteCode: '',
  memberPhone: '',
  inviterPhone: '',
  inviterLevel2Phone: '',
})

const commissionFilters = reactive({
  id: '',
  beneficiaryPhone: '',
  sourcePhone: '',
  commissionLevel: '',
  status: '',
})

function levelLabel(level) {
  return LEVEL_OPTIONS.find((item) => item.value === level)?.label || '—'
}

function statusLabel(status) {
  return STATUS_OPTIONS.find((item) => item.value === status)?.label || status || '—'
}

function formatAmount(value) {
  if (value == null || value === '') return '—'
  return `¥${Number(value).toFixed(2)}`
}

function formatRate(value) {
  if (value == null || value === '') return '—'
  const num = Number(value)
  if (Number.isNaN(num)) return value
  if (num <= 1) {
    return `${(num * 100).toFixed(2)}%`
  }
  return `${num}%`
}

function buildRelationQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    inviteCode: relationFilters.inviteCode || undefined,
    memberPhone: relationFilters.memberPhone || undefined,
    inviterPhone: relationFilters.inviterPhone || undefined,
    inviterLevel2Phone: relationFilters.inviterLevel2Phone || undefined,
  }
}

function buildCommissionQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    id: commissionFilters.id === '' ? undefined : Number(commissionFilters.id),
    beneficiaryPhone: commissionFilters.beneficiaryPhone || undefined,
    sourcePhone: commissionFilters.sourcePhone || undefined,
    commissionLevel: commissionFilters.commissionLevel || undefined,
    status: commissionFilters.status || undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data =
      activeTab.value === 'relation'
        ? await getInviteRelationPage(buildRelationQuery())
        : await getInviteCommissionPage(buildCommissionQuery())
    tableData.value = data.list || []
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载邀请数据失败')
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
  if (activeTab.value === 'relation') {
    Object.assign(relationFilters, {
      inviteCode: '',
      memberPhone: '',
      inviterPhone: '',
      inviterLevel2Phone: '',
    })
  } else {
    Object.assign(commissionFilters, {
      id: '',
      beneficiaryPhone: '',
      sourcePhone: '',
      commissionLevel: '',
      status: '',
    })
  }
  pageNum.value = 1
  loadData()
}

function handleTabChange() {
  pageNum.value = 1
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div>
    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="card-header">
          <span>邀请分销</span>
          <el-text type="info" size="small">邀请配置请前往「平台配置 → 邀请配置」维护</el-text>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane v-for="tab in TABS" :key="tab.name" :label="tab.label" :name="tab.name" />
      </el-tabs>

      <el-form v-if="activeTab === 'relation'" inline class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="邀请码">
          <el-input v-model="relationFilters.inviteCode" placeholder="注册填写邀请码" clearable />
        </el-form-item>
        <el-form-item label="被邀请人手机">
          <el-input v-model="relationFilters.memberPhone" placeholder="被邀请用户手机号" clearable />
        </el-form-item>
        <el-form-item label="一级邀请人手机">
          <el-input v-model="relationFilters.inviterPhone" placeholder="一级邀请人手机号" clearable />
        </el-form-item>
        <el-form-item label="二级邀请人手机">
          <el-input v-model="relationFilters.inviterLevel2Phone" placeholder="二级邀请人手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-form v-else inline class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="记录ID">
          <el-input v-model="commissionFilters.id" placeholder="佣金记录ID" clearable />
        </el-form-item>
        <el-form-item label="受益人手机">
          <el-input v-model="commissionFilters.beneficiaryPhone" placeholder="受益人手机号" clearable />
        </el-form-item>
        <el-form-item label="下单人手机">
          <el-input v-model="commissionFilters.sourcePhone" placeholder="下单用户手机号" clearable />
        </el-form-item>
        <el-form-item label="佣金层级">
          <el-select v-model="commissionFilters.commissionLevel" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in LEVEL_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="commissionFilters.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-if="activeTab === 'relation'" v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="memberName" label="被邀请人" min-width="100" />
        <el-table-column prop="memberPhone" label="被邀请人手机" min-width="120" />
        <el-table-column prop="memberInviteCode" label="被邀请人邀请码" min-width="120" />
        <el-table-column prop="inviteCode" label="注册邀请码" min-width="120" />
        <el-table-column prop="inviterName" label="一级邀请人" min-width="100" />
        <el-table-column prop="inviterPhone" label="一级邀请人手机" min-width="120" />
        <el-table-column prop="inviterInviteCode" label="一级邀请码" min-width="120" />
        <el-table-column prop="inviterLevel2Name" label="二级邀请人" min-width="100" />
        <el-table-column prop="inviterLevel2Phone" label="二级邀请人手机" min-width="120" />
        <el-table-column prop="inviterLevel2InviteCode" label="二级邀请码" min-width="120" />
        <el-table-column prop="bindTime" label="绑定时间" min-width="160" />
        <template #empty>
          <el-empty description="暂无邀请关系" />
        </template>
      </el-table>

      <el-table v-else v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="beneficiaryName" label="受益人" min-width="100" />
        <el-table-column prop="beneficiaryPhone" label="受益人手机" min-width="120" />
        <el-table-column prop="sourceName" label="下单用户" min-width="100" />
        <el-table-column prop="sourcePhone" label="下单用户手机" min-width="120" />
        <el-table-column label="佣金层级" min-width="90">
          <template #default="{ row }">
            {{ row.commissionLevelLabel || levelLabel(row.commissionLevel) }}
          </template>
        </el-table-column>
        <el-table-column label="订单金额" min-width="100">
          <template #default="{ row }">{{ formatAmount(row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column label="佣金比例" min-width="90">
          <template #default="{ row }">{{ formatRate(row.commissionRate) }}</template>
        </el-table-column>
        <el-table-column label="佣金金额" min-width="100">
          <template #default="{ row }">{{ formatAmount(row.commissionAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <el-tag type="success">{{ row.statusLabel || statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="settleTime" label="结算时间" min-width="160" />
        <template #empty>
          <el-empty description="暂无佣金记录" />
        </template>
      </el-table>

      <BasePagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        @update:current-page="pageNum = $event; loadData()"
        @update:page-size="pageSize = $event; pageNum = 1; loadData()"
      />
    </el-card>
  </div>
</template>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-header > span {
  font-weight: 600;
  color: var(--ink);
}

.filter-form {
  margin-bottom: 16px;
}
</style>
