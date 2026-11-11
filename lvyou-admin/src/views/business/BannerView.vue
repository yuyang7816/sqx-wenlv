<script setup>
import { onMounted, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import { bannerApi, updateBannerStateApi } from '@/api/modules/banner'
import { fetchDictItems } from '@/api/modules/dict-query'
import { showSuccess } from '@/utils/modal'
import { formatBannerState } from '@/utils/status-label.js'

/** 轮播位置（sys_dict lvyou_banner_classify） */
const CLASSIFY_OPTIONS = ref([
  { label: 'Banner图', value: 1 },
  { label: '金刚区', value: 2 },
])

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '名称', minWidth: 140 },
  { prop: 'imageUrl', label: '图片', width: 100, type: 'image' },
  { prop: 'classify', label: '分类', width: 100 },
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

onMounted(async () => {
  try {
    const items = await fetchDictItems('lvyou_banner_classify')
    if (Array.isArray(items) && items.length) {
      CLASSIFY_OPTIONS.value = items.map((item) => ({
        label: item.label,
        value: Number(item.code),
      }))
    }
  } catch (e) {
    /* 保留内置 fallback */
  }
})
</script>

<template>
  <CrudPage title="轮播图" :api="bannerApi" :columns="columns" :create-defaults="{ classify: 1, sort: 1, state: 1 }">
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
      <el-form-item label="跳转地址"><el-input v-model="form.url" /></el-form-item>
      <el-form-item label="分类">
        <el-select v-model="form.classify">
          <el-option v-for="item in CLASSIFY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
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
