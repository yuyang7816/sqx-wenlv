<script setup>
import { reactive, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import { scenicCategoryApi, updateScenicCategoryStateApi } from '@/api/modules/scenic'
import { showSuccess } from '@/utils/modal'
import { wrapPublishPayload } from '@/utils/publish-payload.js'

const emit = defineEmits(['changed'])

const crudRef = ref()

const categoryApi = {
  page: scenicCategoryApi.page,
  info: scenicCategoryApi.info,
  async save(data) {
    const result = await scenicCategoryApi.save(data)
    emit('changed')
    return result
  },
  async update(data) {
    const result = await scenicCategoryApi.update(data)
    emit('changed')
    return result
  },
  async delete(data) {
    const result = await scenicCategoryApi.delete(data)
    emit('changed')
    return result
  },
}

const filters = reactive({
  name: '',
  code: '',
  status: '',
})

const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '分类名称', minWidth: 140 },
  { prop: 'code', label: '分类编码', minWidth: 140 },
  { prop: 'sort', label: '排序', width: 80 },
  { prop: 'status', label: '是否启用', width: 120 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '仅小写字母、数字、下划线，且以字母开头', trigger: 'blur' },
  ],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择是否启用', trigger: 'change' }],
}

function buildQuery() {
  return {
    name: filters.name || undefined,
    code: filters.code || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { name: '', code: '', status: '' })
  crudRef.value?.searchData({})
}

const buildPayload = wrapPublishPayload((form) => {
  const payload = {
    ...form,
    code: String(form.code || '').trim(),
    name: String(form.name || '').trim(),
    sort: form.sort ?? 0,
    status: Number(form.status),
  }
  delete payload.statusLabel
  delete payload.createTime
  delete payload.updateTime
  return payload
})

async function toggleStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateScenicCategoryStateApi({ id: row.id, status: nextStatus })
  row.status = nextStatus
  row.statusLabel = nextStatus === 1 ? '启用' : '禁用'
  showSuccess(nextStatus === 1 ? '已启用' : '已禁用')
  emit('changed')
}

function notifyChanged() {
  emit('changed')
}

defineExpose({ notifyChanged })
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="景区分类"
    embedded
    :api="categoryApi"
    :columns="columns"
    edit-from-row
    :create-defaults="{ sort: 0, status: 1 }"
    :build-payload="buildPayload"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="名称">
          <el-input v-model="filters.name" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="filters.code" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="启用">
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

    <template #column-status="{ row }">
      <RowStatusSwitch
        :model-value="Number(row.status)"
        active-text="启用"
        inactive-text="禁用"
        @change="(status) => toggleStatus(row, status)"
      />
    </template>

    <template #form="{ form, editingId }">
      <el-form-item label="分类名称" prop="name" :rules="formRules.name">
        <el-input v-model="form.name" placeholder="如：人文古迹" />
      </el-form-item>
      <el-form-item label="分类编码" prop="code" :rules="formRules.code">
        <el-input
          v-model="form.code"
          :disabled="!!editingId"
          placeholder="如：culture"
        />
        <div class="form-tip">创建后不可修改；景区保存时按该编码关联</div>
      </el-form-item>
      <el-form-item label="排序" prop="sort" :rules="formRules.sort">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="是否启用" prop="status" :rules="formRules.status">
        <el-select v-model="form.status" style="width: 100%">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </template>
  </CrudPage>
</template>

<style scoped>
.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
