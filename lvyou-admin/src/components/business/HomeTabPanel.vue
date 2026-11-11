<script setup>
import { reactive, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import { homeModuleApi, updateHomeModuleStateApi } from '@/api/modules/home-module'
import { showSuccess } from '@/utils/modal'
import { wrapPublishPayload } from '@/utils/publish-payload.js'

const crudRef = ref()
const TAB_MODULE_TYPE = 'tab'

const filters = reactive({
  label: '',
  status: '',
})

const STATUS_OPTIONS = [
  { label: '展示', value: 1 },
  { label: '隐藏', value: 0 },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'label', label: 'Tab 名称', minWidth: 120 },
  { prop: 'moduleKey', label: '内部标识', minWidth: 110 },
  { prop: 'linkPath', label: '列表页路径', minWidth: 180 },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'status', label: '是否展示', width: 110 },
  { prop: 'createTime', label: '创建时间', minWidth: 160 },
]

const formRules = {
  label: [{ required: true, message: '请输入 Tab 名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择是否展示', trigger: 'change' }],
}

function buildQuery() {
  return {
    moduleType: TAB_MODULE_TYPE,
    label: filters.label || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { label: '', status: '' })
  crudRef.value?.searchData({ moduleType: TAB_MODULE_TYPE })
}

const buildPayload = wrapPublishPayload((form) => {
  const payload = {
    ...form,
    moduleType: TAB_MODULE_TYPE,
    icon: null,
    iconColor: null,
    bgColor: null,
  }
  delete payload.moduleKey
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
  row.statusLabel = nextStatus === 1 ? '展示' : '隐藏'
  showSuccess(nextStatus === 1 ? '已展示' : '已隐藏')
}
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="推荐 Tab"
    embedded
    :api="homeModuleApi"
    :columns="columns"
    edit-from-row
    :default-query="{ moduleType: TAB_MODULE_TYPE }"
    :create-defaults="{ moduleType: TAB_MODULE_TYPE, sort: 0, status: 1 }"
    :build-payload="buildPayload"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="名称">
          <el-input v-model="filters.label" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="展示">
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
        active-text="展示"
        inactive-text="隐藏"
        @change="(status) => toggleStatus(row, status)"
      />
    </template>

    <template #form="{ form }">
      <el-form-item label="Tab 名称" prop="label" :rules="formRules.label">
        <el-input v-model="form.label" placeholder="如：综合推荐、酒店住宿" />
      </el-form-item>
      <el-form-item label="列表页路径">
        <el-input v-model="form.linkPath" placeholder="/mobile-design/hotel/list（综合推荐可留空）" />
        <div class="form-tip">内部标识会根据 Tab 名称或列表页路径自动生成，商户发布时也会按门店类型自动归类，无需手动填写</div>
      </el-form-item>
      <el-form-item label="排序" prop="sort" :rules="formRules.sort">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="是否展示" prop="status" :rules="formRules.status">
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
