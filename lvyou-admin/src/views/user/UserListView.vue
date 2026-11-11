<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import { deleteAppUserApi, getAppUserInfo, getAppUserPage, updateAppUserApi } from '@/api/modules/app-user'
import { validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filters = reactive({
  username: '',
  phone: '',
  status: '',
})

const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  userId: null,
  userName: '',
  phone: '',
  status: 1,
})

const detailVisible = ref(false)
const detailLoading = ref(false)
const detailData = ref(null)

const walletVisible = ref(false)
const walletLoading = ref(false)
const walletFormRef = ref()
const walletForm = reactive({
  userId: null,
  userName: '',
  balance: '0.00',
  adjustType: 'increase',
  adjustAmount: '',
})

const commissionVisible = ref(false)
const commissionLoading = ref(false)
const commissionFormRef = ref()
const commissionForm = reactive({
  userId: null,
  userName: '',
  commissionRateLevel1: '',
  commissionRateLevel2: '',
  defaultCommissionRateLevel1: '0.05',
  defaultCommissionRateLevel2: '0.02',
})

const editRules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

const walletRules = {
  adjustAmount: [
    { required: true, message: '请输入调整金额', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        const num = Number(value)
        if (!value || Number.isNaN(num) || num <= 0) {
          callback(new Error('调整金额必须大于 0'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

function formatRatePercent(rate) {
  if (rate == null || rate === '') return '-'
  const num = Number(rate)
  if (Number.isNaN(num)) return String(rate)
  const percent = num > 1 ? num : num * 100
  return `${Number(percent.toFixed(4))}%`
}

function formatCommissionCell(userRate, defaultRate) {
  if (userRate != null && userRate !== '') {
    return `${formatRatePercent(userRate)}（自定义）`
  }
  return `${formatRatePercent(defaultRate)}（默认）`
}

function formatStatus(status) {
  if (status === 1) return '正常'
  if (status === 2) return '禁用'
  if (status === 3) return '已注销'
  return status ?? '-'
}

function formatSex(sex) {
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
  return '-'
}

function displayValue(value) {
  if (value == null || value === '') return '-'
  return value
}

async function loadData() {
  loading.value = true
  try {
    const data = await getAppUserPage({
      page: pageNum.value,
      limit: pageSize.value,
      username: filters.username || undefined,
      phone: filters.phone || undefined,
      status: filters.status === '' ? undefined : Number(filters.status),
    })
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, { username: '', phone: '', status: '' })
  pageNum.value = 1
  loadData()
}

async function fetchUserInfo(userId) {
  try {
    return await getAppUserInfo(userId)
  } catch (error) {
    showError(error?.msg || error?.message || '加载用户失败')
    return null
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detailData.value = null
  try {
    detailData.value = await fetchUserInfo(row.userId)
    if (!detailData.value) {
      detailVisible.value = false
    }
  } finally {
    detailLoading.value = false
  }
}

async function openEdit(row) {
  editVisible.value = true
  editLoading.value = true
  try {
    const data = await fetchUserInfo(row.userId)
    if (!data) {
      editVisible.value = false
      return
    }
    Object.assign(editForm, {
      userId: data.userId,
      userName: data.userName,
      phone: data.phone,
      status: data.status,
    })
  } finally {
    editLoading.value = false
  }
}

async function openWallet(row) {
  walletVisible.value = true
  walletLoading.value = true
  try {
    const data = await fetchUserInfo(row.userId)
    if (!data) {
      walletVisible.value = false
      return
    }
    Object.assign(walletForm, {
      userId: data.userId,
      userName: data.userName,
      balance: data.balance || '0.00',
      adjustType: 'increase',
      adjustAmount: '',
    })
  } finally {
    walletLoading.value = false
  }
}

async function openCommission(row) {
  commissionVisible.value = true
  commissionLoading.value = true
  try {
    const data = await fetchUserInfo(row.userId)
    if (!data) {
      commissionVisible.value = false
      return
    }
    Object.assign(commissionForm, {
      userId: data.userId,
      userName: data.userName,
      commissionRateLevel1: data.commissionRateLevel1 != null ? String(data.commissionRateLevel1) : '',
      commissionRateLevel2: data.commissionRateLevel2 != null ? String(data.commissionRateLevel2) : '',
      defaultCommissionRateLevel1: data.defaultCommissionRateLevel1 || '0.05',
      defaultCommissionRateLevel2: data.defaultCommissionRateLevel2 || '0.02',
    })
  } finally {
    commissionLoading.value = false
  }
}

async function handleEditSubmit() {
  const valid = await validateForm(editFormRef)
  if (!valid) return
  editLoading.value = true
  try {
    await updateAppUserApi({
      userId: editForm.userId,
      userName: editForm.userName,
      status: editForm.status,
    })
    showSuccess('用户信息已更新')
    editVisible.value = false
    loadData()
  } finally {
    editLoading.value = false
  }
}

async function handleWalletSubmit() {
  const valid = await validateForm(walletFormRef)
  if (!valid) return
  walletLoading.value = true
  try {
    await updateAppUserApi({
      userId: walletForm.userId,
      walletAdjustType: walletForm.adjustType,
      walletAdjustAmount: walletForm.adjustAmount,
    })
    showSuccess(walletForm.adjustType === 'decrease' ? '余额已减少' : '余额已增加')
    walletVisible.value = false
    loadData()
  } finally {
    walletLoading.value = false
  }
}

async function handleCommissionSubmit() {
  commissionLoading.value = true
  try {
    await updateAppUserApi({
      userId: commissionForm.userId,
      commissionRateLevel1: commissionForm.commissionRateLevel1,
      commissionRateLevel2: commissionForm.commissionRateLevel2,
    })
    showSuccess('佣金比例已更新')
    commissionVisible.value = false
    loadData()
  } finally {
    commissionLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除用户“${row.userName || row.phone}”吗？`)
  await deleteAppUserApi({ userId: row.userId })
  showSuccess('用户已删除')
  loadData()
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>C端用户</strong>
      </div>
    </template>
    <el-form :inline="true" class="search-grid">
      <el-form-item label="用户名">
        <el-input v-model="filters.username" clearable placeholder="用户名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="filters.phone" clearable placeholder="手机号" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable placeholder="全部">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="2" />
          <el-option label="已注销" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="userId" label="ID" min-width="80" />
      <el-table-column prop="userName" label="用户名" min-width="140" />
      <el-table-column prop="phone" label="手机号" min-width="140" />
      <el-table-column prop="balance" label="余额" min-width="110">
        <template #default="{ row }">
          <span>¥{{ row.balance || '0.00' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="一级佣金" min-width="130">
        <template #default="{ row }">
          {{ formatCommissionCell(row.commissionRateLevel1, row.defaultCommissionRateLevel1) }}
        </template>
      </el-table-column>
      <el-table-column label="二级佣金" min-width="130">
        <template #default="{ row }">
          {{ formatCommissionCell(row.commissionRateLevel2, row.defaultCommissionRateLevel2) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 3 ? 'info' : 'danger'">{{ formatStatus(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" min-width="320" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="isAuth('base:user:info')" size="small" type="info" @click="openDetail(row)">详情</el-button>
            <el-button v-if="isAuth('base:user:update')" size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="isAuth('base:user:update')" size="small" type="warning" @click="openWallet(row)">钱包</el-button>
            <el-button v-if="isAuth('base:user:update')" size="small" type="success" @click="openCommission(row)">佣金</el-button>
            <el-button v-if="isAuth('base:user:delete')" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="detailVisible" title="用户详情" width="720px" destroy-on-close>
      <div v-loading="detailLoading">
        <el-descriptions v-if="detailData" :column="2" border>
          <el-descriptions-item label="用户ID">{{ detailData.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ displayValue(detailData.userName) }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ displayValue(detailData.phone) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">{{ formatStatus(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="钱包余额">¥{{ detailData.balance || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">{{ formatUserType(detailData.userType) }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ formatSex(detailData.sex) }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ displayValue(detailData.age) }}</el-descriptions-item>
          <el-descriptions-item label="是否会员">{{ formatVip(detailData.isVip) }}</el-descriptions-item>
          <el-descriptions-item label="注册来源">{{ displayValue(detailData.platform) }}</el-descriptions-item>
          <el-descriptions-item label="邀请码">{{ displayValue(detailData.invitationCode) }}</el-descriptions-item>
          <el-descriptions-item label="上级邀请码">{{ displayValue(detailData.superiorInvitationCode) }}</el-descriptions-item>
          <el-descriptions-item label="一级佣金">
            {{ formatCommissionCell(detailData.commissionRateLevel1, detailData.defaultCommissionRateLevel1) }}
          </el-descriptions-item>
          <el-descriptions-item label="二级佣金">
            {{ formatCommissionCell(detailData.commissionRateLevel2, detailData.defaultCommissionRateLevel2) }}
          </el-descriptions-item>
          <el-descriptions-item label="系统默认一级">{{ formatRatePercent(detailData.defaultCommissionRateLevel1) }}</el-descriptions-item>
          <el-descriptions-item label="系统默认二级">{{ formatRatePercent(detailData.defaultCommissionRateLevel2) }}</el-descriptions-item>
          <el-descriptions-item label="当前目的地ID">{{ displayValue(detailData.currentDestinationId) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ displayValue(detailData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ displayValue(detailData.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="头像" :span="2">
            <el-image
              v-if="detailData.avatar"
              :src="detailData.avatar"
              fit="cover"
              class="detail-avatar"
              :preview-src-list="[detailData.avatar]"
            />
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <AppDialog v-model="editVisible" title="编辑用户" :loading="editLoading" width="480px" @confirm="handleEditSubmit">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="96px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="editForm.userName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
    </AppDialog>

    <AppDialog v-model="walletVisible" title="调整钱包余额" :loading="walletLoading" width="480px" @confirm="handleWalletSubmit">
      <el-form ref="walletFormRef" :model="walletForm" :rules="walletRules" label-width="96px">
        <el-form-item label="用户">
          <el-input :model-value="walletForm.userName" disabled />
        </el-form-item>
        <el-form-item label="当前余额">
          <el-input :model-value="`¥${walletForm.balance}`" disabled />
        </el-form-item>
        <el-form-item label="调整方式">
          <el-radio-group v-model="walletForm.adjustType">
            <el-radio value="increase">增加</el-radio>
            <el-radio value="decrease">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整金额" prop="adjustAmount">
          <el-input v-model="walletForm.adjustAmount" placeholder="请输入大于 0 的金额">
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        <p class="field-tip">按所选方式增加或减少余额，并记录管理员调整流水；减少时余额不足将无法提交</p>
      </el-form>
    </AppDialog>

    <AppDialog v-model="commissionVisible" title="调整佣金比例" :loading="commissionLoading" width="520px" @confirm="handleCommissionSubmit">
      <el-form ref="commissionFormRef" :model="commissionForm" label-width="120px">
        <el-form-item label="用户">
          <el-input :model-value="commissionForm.userName" disabled />
        </el-form-item>
        <el-form-item label="一级佣金比例">
          <el-input
            v-model="commissionForm.commissionRateLevel1"
            :placeholder="`留空使用默认 ${formatRatePercent(commissionForm.defaultCommissionRateLevel1)}`"
            clearable
          />
        </el-form-item>
        <el-form-item label="二级佣金比例">
          <el-input
            v-model="commissionForm.commissionRateLevel2"
            :placeholder="`留空使用默认 ${formatRatePercent(commissionForm.defaultCommissionRateLevel2)}`"
            clearable
          />
        </el-form-item>
        <p class="field-tip">支持 0.05 或 5 表示 5%；留空表示恢复系统默认比例</p>
      </el-form>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.field-tip {
  margin: 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.detail-avatar {
  width: 72px;
  height: 72px;
  border-radius: 8px;
}
</style>
