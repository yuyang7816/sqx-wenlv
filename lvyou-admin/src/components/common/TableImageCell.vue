<script setup>
import { computed } from 'vue'
import { collectImageUrls } from '@/utils/table-column'

const props = defineProps({
  value: { type: [String, Number, Array, Object], default: '' },
  multiple: { type: Boolean, default: false },
})

const urls = computed(() => collectImageUrls(props.value))
const displayUrls = computed(() => (props.multiple ? urls.value.slice(0, 3) : urls.value.slice(0, 1)))
</script>

<template>
  <div v-if="displayUrls.length" class="table-image-cell">
    <el-image
      v-for="(url, index) in displayUrls"
      :key="`${url}-${index}`"
      :src="url"
      fit="cover"
      class="table-image-cell__img"
      preview-teleported
      :preview-src-list="urls"
      :initial-index="index"
    />
    <span v-if="multiple && urls.length > 3" class="table-image-cell__more">+{{ urls.length - 3 }}</span>
  </div>
  <span v-else class="table-image-cell__empty">-</span>
</template>

<style scoped>
.table-image-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.table-image-cell__img {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  background: var(--el-fill-color-light);
}

.table-image-cell__more {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.table-image-cell__empty {
  color: var(--el-text-color-placeholder);
}
</style>
