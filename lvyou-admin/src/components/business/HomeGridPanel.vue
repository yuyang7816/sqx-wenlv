<script setup>
import { onMounted, reactive, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import { homeModuleApi, updateHomeModuleStateApi } from '@/api/modules/home-module'
import { destinationApi } from '@/api/modules/destination'
import { showSuccess } from '@/utils/modal'
import { wrapPublishPayload } from '@/utils/publish-payload.js'

const crudRef = ref()
const destinationOptions = ref([])
const GRID_MODULE_TYPE = 'grid'

const filters = reactive({
  label: '',
  status: '',
})

const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'label', label: '展示名称', minWidth: 120 },
  { prop: 'icon', label: '入口图片', width: 100, type: 'image' },
  { prop: 'linkPath', label: '跳转路径', minWidth: 180 },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'status', label: '状态', width: 100 },
  { prop: 'destinationName', label: '目的地', minWidth: 100 },
  { prop: 'createTime', label: '创建时间', minWidth: 160 },
]

const formRules = {
  label: [{ required: true, message: '请输入展示名称', trigger: 'blur' }],
  icon: [{ required: true, message: '请上传入口图片', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function buildQuery() {
  return {
    moduleType: GRID_MODULE_TYPE,
    label: filters.label || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { label: '', status: '' })
  crudRef.value?.searchData({ moduleType: GRID_MODULE_TYPE })
}

const buildPayload = wrapPublishPayload((form) => {
  const payload = {
    ...form,
    moduleType: GRID_MODULE_TYPE,
    iconColor: null,
    bgColor: null,
  }
  delete payload.statusLabel
  delete payload.destinationName
  delete payload.moduleTypeLabel
  delete payload.createTime
  return payload
})

async function toggleStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateHomeModuleStateApi({ id: row.id, status: nextStatus })
  row.status = nextStatus
  row.statusLabel = nextStatus === 1 ? '启用' : '禁用'
  showSuccess(nextStatus === 1 ? '已启用' : '已禁用')
}

async function loadDestinationOptions() {
  const data = await destinationApi.page({ page: 1, limit: 50, status: 1 })
  destinationOptions.value = data.list || []
}

onMounted(() => {
  loadDestinationOptions().catch(() => {})
})
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="金刚区"
    embedded
    :api="homeModuleApi"
    :columns="columns"
    edit-from-row
    :default-query="{ moduleType: GRID_MODULE_TYPE }"
    :create-defaults="{ moduleType: GRID_MODULE_TYPE, sort: 0, status: 1 }"
    :build-payload="buildPayload"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="名称">
          <el-input v-model="filters.label" clearable placeholder="模糊搜索" />
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

    <template #column-status="{ row }">
      <RowStatusSwitch
        :model-value="Number(row.status)"
        active-text="启用"
        inactive-text="禁用"
        @change="(status) => toggleStatus(row, status)"
      />
    </template>

    <template #form="{ form }">
      <el-form-item label="展示名称" prop="label" :rules="formRules.label">
        <el-input v-model="form.label" placeholder="如：酒店住宿、西安美食" />
      </el-form-item>
      <el-form-item label="入口图片" prop="icon" :rules="formRules.icon">
        <ImageUpload :model-value="form.icon || ''" @update:model-value="form.icon = $event" />
        <div class="form-tip">建议上传 88×88 左右的正方形图片，透明底 PNG 效果更佳</div>
      </el-form-item>
      <el-form-item label="跳转路径">
        <el-input v-model="form.linkPath" placeholder="/mobile-design/hotel/list" />
      </el-form-item>
      <el-form-item label="排序" prop="sort" :rules="formRules.sort">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态" prop="status" :rules="formRules.status">
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
