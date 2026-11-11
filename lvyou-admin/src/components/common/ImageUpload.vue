<script setup>
import { computed, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { uploadFileApi } from '@/api/modules/system-config'
import { showError } from '@/utils/modal'

function normalizePublicUrl(url) {
  return String(url || '').trim().replace(/\\/g, '/')
}

const props = defineProps({
  /** 单图 URL 或多图 URL 数组 */
  modelValue: { type: [String, Array], default: '' },
  /** 是否多图上传 */
  multiple: { type: Boolean, default: false },
  /** 多图上限 */
  limit: { type: Number, default: 9 },
  /** 底部说明，不传则按单图/多图使用默认文案 */
  tip: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)
/** 多图本地列表，避免一次多选时并行上传互相覆盖 */
const localUrls = ref([])

function readUrls(value) {
  if (props.multiple) {
    const list = Array.isArray(value) ? value : []
    return list.filter(Boolean).map((url) => normalizePublicUrl(url))
  }
  return value ? [normalizePublicUrl(value)] : []
}

watch(
  () => props.modelValue,
  (value) => {
    const next = readUrls(value)
    if (next.join('\n') !== localUrls.value.join('\n')) {
      localUrls.value = next
    }
  },
  { immediate: true, deep: true },
)

const imageUrls = computed(() => (props.multiple ? localUrls.value : readUrls(props.modelValue)))

const fileList = computed(() =>
  imageUrls.value.map((url, index) => ({ name: `image-${index}`, url })),
)

const showAddButton = computed(() => imageUrls.value.length < (props.multiple ? props.limit : 1))

const tipText = computed(() => {
  if (props.tip) return props.tip
  if (props.multiple) return `点击选择图片，可一次多选，最多 ${props.limit} 张`
  return '点击选择封面图片'
})

let uploadChain = Promise.resolve()

function handleUpload(options) {
  const task = uploadChain.then(() => uploadOne(options))
  uploadChain = task.then(
    () => undefined,
    () => undefined,
  )
  return task
}

async function uploadOne(options) {
  uploading.value = true
  try {
    const result = await uploadFileApi(options.file)
    const url = normalizePublicUrl(result?.path || result?.relativePath || '')
    if (!url) {
      showError('上传失败：未返回文件地址')
      options.onError?.(new Error('empty url'))
      return
    }
    if (props.multiple) {
      const next = [...localUrls.value, url]
      localUrls.value = next
      emit('update:modelValue', next)
    } else {
      emit('update:modelValue', url)
    }
    options.onSuccess?.(result)
  } catch (error) {
    options.onError?.(error)
  } finally {
    uploading.value = false
  }
}

function handleExceed() {
  showError(`最多选择 ${props.multiple ? props.limit : 1} 张图片`)
}

function handleRemove(file) {
  if (props.multiple) {
    const next = localUrls.value.filter((url) => url !== file.url)
    localUrls.value = next
    emit('update:modelValue', next)
    return
  }
  emit('update:modelValue', '')
}
</script>

<template>
  <div class="image-upload" :class="{ 'image-upload--full': !showAddButton }">
    <el-upload
      :file-list="fileList"
      list-type="picture-card"
      :multiple="multiple"
      :limit="multiple ? limit : 1"
      :http-request="handleUpload"
      :on-remove="handleRemove"
      :on-exceed="handleExceed"
      :disabled="uploading"
      :show-file-list="true"
      accept="image/*"
    >
      <el-icon v-if="showAddButton"><Plus /></el-icon>
    </el-upload>
    <p class="image-upload__tip">{{ tipText }}</p>
  </div>
</template>

<style scoped>
.image-upload {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 100%;
}

.image-upload :deep(.el-upload-list--picture-card) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  position: static;
  width: 100%;
}

.image-upload :deep(.el-upload-list--picture-card .el-upload-list__item) {
  margin: 0;
}

.image-upload :deep(.el-upload--picture-card) {
  margin: 0;
}

.image-upload--full :deep(.el-upload--picture-card) {
  display: none;
}

.image-upload__tip {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}
</style>
