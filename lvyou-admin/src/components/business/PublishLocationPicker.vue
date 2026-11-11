<script setup>
import { computed } from 'vue'
import MapLocationPicker from '@/components/business/MapLocationPicker.vue'

const props = defineProps({
  latitude: { type: [Number, String], default: null },
  longitude: { type: [Number, String], default: null },
  address: { type: String, default: '' },
  dialogTitle: { type: String, default: '搜索并选择地址' },
  emptyHint: { type: String, default: '尚未选择地址，请在下方地图中搜索选点' },
  pickButtonText: { type: String, default: '地图搜索选点' },
  repickButtonText: { type: String, default: '重新选择地址' },
  /** 无坐标时是否仍回显地址（仅用于集合点等不落库坐标的字段） */
  allowOrphanAddress: { type: Boolean, default: false },
})

const emit = defineEmits(['update:latitude', 'update:longitude', 'update:address', 'change'])

const hasCoordinates = computed(() => props.latitude != null && props.longitude != null
  && props.latitude !== '' && props.longitude !== '')

/** 默认仅回显已落库坐标对应地址；allowOrphanAddress 时允许展示无坐标历史文案 */
const storeAddress = computed(() => {
  if (hasCoordinates.value || props.allowOrphanAddress) {
    return String(props.address || '')
  }
  return ''
})

function onPickerChange(payload) {
  const latitude = payload?.latitude
  const longitude = payload?.longitude
  const address = String(payload?.storeAddress || '').trim()
  // 先写齐 lat/lng，再写地址，减少父级分次 v-model 的中间态窗口
  emit('update:latitude', latitude)
  emit('update:longitude', longitude)
  emit('update:address', address)
  emit('change', {
    latitude,
    longitude,
    storeAddress: address,
    address,
  })
}
</script>

<template>
  <MapLocationPicker
    :latitude="latitude"
    :longitude="longitude"
    :store-region="''"
    :store-address-detail="''"
    :store-address="storeAddress"
    :dialog-title="dialogTitle"
    :empty-hint="emptyHint"
    :pick-button-text="pickButtonText"
    :repick-button-text="repickButtonText"
    @change="onPickerChange"
  />
</template>
