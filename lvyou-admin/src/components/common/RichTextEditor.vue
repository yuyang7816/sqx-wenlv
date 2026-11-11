<script setup>
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

/** 富文本正文编辑（公告等内容字段） */
const model = defineModel({ type: String, default: '' })

const props = defineProps({
  placeholder: { type: String, default: '请输入正文内容' },
  minHeight: { type: String, default: '200px' },
})
</script>

<template>
  <div class="rich-text-editor">
    <QuillEditor
      v-model:content="model"
      content-type="html"
      theme="snow"
      :placeholder="placeholder"
      :style="{ minHeight: props.minHeight }"
      toolbar="essential"
    />
  </div>
</template>

<style scoped>
.rich-text-editor {
  width: 100%;
}

.rich-text-editor :deep(.ql-container) {
  font-size: 14px;
}
</style>
