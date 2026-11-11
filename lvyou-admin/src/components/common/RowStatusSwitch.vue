<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: [Number, Boolean, String], required: true },
  activeValue: { type: [Number, Boolean, String], default: 1 },
  inactiveValue: { type: [Number, Boolean, String], default: 0 },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  activeText: { type: String, default: '启用' },
  inactiveText: { type: String, default: '禁用' },
})

const emit = defineEmits(['change'])

const isActive = computed(() => Number(props.modelValue) === Number(props.activeValue))

function handleChange(checked) {
  emit('change', checked ? props.activeValue : props.inactiveValue)
}
</script>

<template>
  <el-switch
    class="row-status-switch"
    :model-value="isActive"
    :loading="loading"
    :disabled="disabled"
    inline-prompt
    :active-text="activeText"
    :inactive-text="inactiveText"
    @change="handleChange"
  />
</template>
