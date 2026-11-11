<script setup>
import { onMounted, reactive, ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import {
  deleteMerchantStoreType,
  getMerchantStoreTypeList,
  saveMerchantStoreType,
  updateMerchantStoreType,
} from '@/api/modules/merchant'
import { patchFormModel, resetFormModel, validateForm } from '@/utils/form'
import { confirmAction, showError, showSuccess } from '@/utils/modal'
import { isAuth } from '@/utils/tool'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const dialogLoading = ref(false)
const formRef = ref()
const editingId = ref(null)

const initialForm = {
  code: '',
  label: '',
  depositAmount: 5000,
  sortOrder: 0,
  remark: '',
}
const form = reactive({ ...initialForm })

const rules = {
  code: [
    { required: true, message: '请输入类型编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '仅小写字母、数字、下划线，且以字母开头', trigger: 'blur' },
  ],
  label: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
  depositAmount: [{ required: true, message: '请输入保证金', trigger: 'blur' }],
}

const canUpdate = isAuth('lvyou:merchant:update') || isAuth('lvyou:merchant:page') || isAuth('lvyou:merchant:adjust')

async function loadData() {
  loading.value = true
  try {
    tableData.value = await getMerchantStoreTypeList()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetFormModel(form, initialForm)
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  patchFormModel(form, {
    code: row.code,
    label: row.label,
    depositAmount: row.depositAmount,
    sortOrder: row.sortOrder ?? 0,
    remark: row.remark || '',
  }, initialForm)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  dialogLoading.value = true
  try {
    const payload = {
      id: editingId.value || undefined,
      code: String(form.code || '').trim(),
      label: String(form.label || '').trim(),
      depositAmount: Number(form.depositAmount),
      sortOrder: Number(form.sortOrder || 0),
      remark: String(form.remark || '').trim() || undefined,
    }
    if (editingId.value) {
      await updateMerchantStoreType(payload)
      showSuccess('已更新入驻类型')
    } else {
      await saveMerchantStoreType(payload)
      showSuccess('已新增入驻类型')
    }
    dialogVisible.value = false
    await loadData()
  } catch (e) {
    showError(e.message || '保存失败')
  } finally {
    dialogLoading.value = false
  }
}

async function handleDelete(row) {
  await confirmAction(`确定删除入驻类型「${row.label}」吗？`, '删除后 C 端将不再展示该类型')
  try {
    await deleteMerchantStoreType(row.id)
    showSuccess('已删除')
    await loadData()
  } catch (e) {
    showError(e.message || '删除失败')
  }
}

function formatDeposit(value) {
  const num = Number(value)
  if (!Number.isFinite(num)) return '--'
  return `¥${num.toFixed(2)}`
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <strong>入驻类型</strong>
          <p class="card-desc">维护商户可选入驻类型及对应保证金，C 端入驻页与商户钱包实时生效</p>
        </div>
        <el-button v-if="canUpdate" type="primary" @click="openCreate">新增类型</el-button>
      </div>
    </template>

    <el-table v-loading="loading" :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="类型编码" min-width="120" />
      <el-table-column prop="label" label="类型名称" min-width="120" />
      <el-table-column label="保证金" min-width="120">
        <template #default="{ row }">{{ formatDeposit(row.depositAmount) }}</template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column v-if="canUpdate" label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <AppDialog
      v-model="dialogVisible"
      :title="editingId ? '编辑入驻类型' : '新增入驻类型'"
      width="520px"
      :loading="dialogLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类型编码" prop="code">
          <el-input v-model="form.code" :disabled="!!editingId" placeholder="如 hotel、restaurant" />
        </el-form-item>
        <el-form-item label="类型名称" prop="label">
          <el-input v-model="form.label" placeholder="如 酒店、餐饮" />
        </el-form-item>
        <el-form-item label="保证金(元)" prop="depositAmount">
          <el-input-number v-model="form.depositAmount" :min="0" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选，如经营说明" />
        </el-form-item>
      </el-form>
    </AppDialog>
  </el-card>
</template>

<style scoped>
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.card-desc {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
