<script setup>
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import { bannerApi, updateBannerStateApi } from '@/api/modules/banner'
import { showSuccess } from '@/utils/modal'
import { formatBannerState } from '@/utils/status-label.js'

const BANNER_CLASSIFY = 1

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'imageUrl', label: '图片', width: 100, type: 'image' },
  { prop: 'sort', label: '排序', width: 80 },
  { prop: 'state', label: '状态', width: 110, formatter: formatBannerState },
]

async function toggleState(row, state) {
  const nextState = Number(state)
  if (Number(row.state) === nextState) return
  await updateBannerStateApi({ id: row.id, state: nextState })
  showSuccess(nextState === 1 ? '已显示' : '已隐藏')
  row.state = nextState
}

function buildPayload(form) {
  return {
    ...form,
    classify: BANNER_CLASSIFY,
  }
}
</script>

<template>
  <CrudPage
    title="轮播图"
    embedded
    :api="bannerApi"
    :columns="columns"
    :default-query="{ classify: BANNER_CLASSIFY }"
    :create-defaults="{ classify: BANNER_CLASSIFY, sort: 1, state: 1 }"
    :build-payload="buildPayload"
  >
    <template #column-state="{ row }">
      <RowStatusSwitch
        :model-value="Number(row.state)"
        :active-value="1"
        :inactive-value="2"
        active-text="显示"
        inactive-text="隐藏"
        @change="(state) => toggleState(row, state)"
      />
    </template>
    <template #form="{ form }">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="轮播图片">
        <ImageUpload :model-value="form.imageUrl || ''" @update:model-value="form.imageUrl = $event" />
      </el-form-item>
      <el-form-item label="跳转地址"><el-input v-model="form.url" placeholder="小程序页面路径或 H5 链接" /></el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
      <el-form-item label="状态">
        <el-select v-model="form.state">
          <el-option label="显示" :value="1" />
          <el-option label="隐藏" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
    </template>
  </CrudPage>
</template>
