<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import {
  deleteSystemConfigApi,
  getSystemConfigListApi,
  refreshSystemConfigApi,
  saveSystemConfigApi,
  updateSystemConfigApi,
} from '@/api/modules/system-config'
import { validateForm } from '@/utils/form'
import { confirmAction, showSuccess } from '@/utils/modal'
import { useBrandStore } from '@/stores/brand'
import { BRAND_CONFIG_KEY } from '@/api/modules/platform-config'
import { isAuth } from '@/utils/tool'

const brandStore = useBrandStore()

const loading = ref(false)
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const groups = ref([])
const activeTab = ref('')
const tableData = ref([])
const editingId = ref(null)

const form = reactive({
  id: null,
  paramKey: '',
  paramValue: '',
  status: '1',
  remark: '',
  paramType: '',
  paramTypeDesc: '',
})

const rules = {
  paramKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  paramValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
}

/** 配置分组 Tab 中文名（paramType 优先，兼容历史英文 paramTypeDesc） */
const PARAM_TYPE_LABEL = {
  sys: '系统配置',
  switch: '开关配置',
  pay: '支付配置',
  agreement: '协议配置',
  invite: '邀请配置',
  wallet: '提现配置',
  transport: '公共交通配置',
  order: '订单结算配置',
  route_order: '线路退改规则',
  logistics: '物流配置',
  kefu: '客服配置',
  map: '地图配置',
  file: '文件配置',
  sms: '短信配置',
  ali: '支付宝',
  domain: '域名',
  'route refund': '线路退改规则',
  'order settlement': '订单结算配置',
}

const PARAM_TYPE_DESC_LABEL = {
  'route refund': '线路退改规则',
  'order settlement': '订单结算配置',
}

function resolveParamTypeLabel(paramType, paramTypeDesc) {
  const typeKey = String(paramType || '').trim()
  const descKey = String(paramTypeDesc || '').trim()
  if (descKey && /[\u4e00-\u9fff]/.test(descKey)) return descKey
  const descLower = descKey.toLowerCase()
  if (PARAM_TYPE_DESC_LABEL[descLower]) return PARAM_TYPE_DESC_LABEL[descLower]
  if (PARAM_TYPE_LABEL[typeKey]) return PARAM_TYPE_LABEL[typeKey]
  return descKey || typeKey || '—'
}

function syncTableData() {
  tableData.value = groups.value.find((item) => item.paramType === activeTab.value)?.list || []
}

async function loadData() {
  loading.value = true
  try {
    const data = await getSystemConfigListApi()
    groups.value = (data || []).map((item) => ({
      paramType: item.paramType,
      paramTypeDesc: resolveParamTypeLabel(item.paramType, item.paramTypeDesc || item.paramType),
      list: item.sysConfigEntityList || [],
    }))
    if (!activeTab.value && groups.value.length) {
      activeTab.value = groups.value[0].paramType
    }
    syncTableData()
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  syncTableData()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    id: null,
    paramKey: '',
    paramValue: '',
    status: '1',
    remark: '',
    paramType: activeTab.value,
    paramTypeDesc: groups.value.find((item) => item.paramType === activeTab.value)?.paramTypeDesc || '',
  })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    id: row.id,
    paramKey: row.paramKey,
    paramValue: row.paramValue,
    status: row.status || '1',
    remark: row.remark || '',
    paramType: row.paramType || activeTab.value,
    paramTypeDesc: row.paramTypeDesc,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    if (editingId.value) await updateSystemConfigApi({ ...form })
    else await saveSystemConfigApi({ ...form })
    if (form.paramKey === BRAND_CONFIG_KEY && form.paramValue) {
      brandStore.setProductName(form.paramValue)
    }
    showSuccess(editingId.value ? '配置已更新' : '配置已新增')
    dialogVisible.value = false
    await loadData()
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确认删除配置“${row.paramKey}”吗？`)
  await deleteSystemConfigApi([row.id])
  showSuccess('配置已删除')
  await loadData()
}

async function handleRefresh() {
  await refreshSystemConfigApi()
  await brandStore.load()
  showSuccess('配置缓存已刷新')
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>系统配置</strong>
        <el-space>
          <el-button v-if="isAuth('sys:config:update')" @click="handleRefresh">刷新缓存</el-button>
        </el-space>
      </div>
    </template>
    <el-tabs v-if="groups.length" v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane v-for="item in groups" :key="item.paramType" :label="item.paramTypeDesc" :name="item.paramType">
        <el-table v-loading="loading" :data="tableData" border>
          <el-table-column prop="paramKey" label="配置键" min-width="200" />
          <el-table-column prop="paramValue" label="配置值" min-width="320" show-overflow-tooltip />
          <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '1' ? 'success' : 'info'">{{ row.status === '1' ? '启用' : '禁用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button v-if="isAuth('sys:config:update')" size="small" type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button v-if="isAuth('sys:config:delete')" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-empty v-else description="暂无配置数据" />
    <AppDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑配置' : '新增配置'"
      :loading="dialogLoading"
      width="640px"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="配置键" prop="paramKey">
          <el-input v-model="form.paramKey" :readonly="Boolean(editingId)" />
        </el-form-item>
        <el-form-item label="配置值" prop="paramValue">
          <el-input v-model="form.paramValue" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item v-if="form.paramKey === 'sys_default_avatar' && form.paramValue" label="头像预览">
          <el-image
            :src="form.paramValue"
            fit="cover"
            style="width: 80px; height: 80px; border-radius: 50%; border: 1px solid var(--el-border-color)"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
    </AppDialog>
  </el-card>
</template>
