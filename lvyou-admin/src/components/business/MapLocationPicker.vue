<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { getMapPickerConfig, reverseGeocode } from '@/api/modules/map'
import { formatStoreAddress } from '@/utils/merchant-onboarding-admin'
import { showError } from '@/utils/modal'

const props = defineProps({
  latitude: { type: [Number, String], default: null },
  longitude: { type: [Number, String], default: null },
  storeRegion: { type: String, default: '' },
  storeAddressDetail: { type: String, default: '' },
  storeAddress: { type: String, default: '' },
  dialogTitle: { type: String, default: '搜索并选择地址' },
  emptyHint: { type: String, default: '尚未选择地址，请在下方地图中搜索选点' },
  pickButtonText: { type: String, default: '地图搜索选点' },
  repickButtonText: { type: String, default: '重新选择地址' },
})

const emit = defineEmits([
  'update:latitude',
  'update:longitude',
  'update:storeRegion',
  'update:storeAddressDetail',
  'update:storeAddress',
  'change',
])

const configLoading = ref(false)
const pickerConfig = ref(null)
const iframeUrl = ref('')
const mapError = ref('')

const hasLocation = computed(() => props.latitude != null && props.longitude != null
  && props.latitude !== '' && props.longitude !== '')

/** 仅在已有有效经纬度时展示地址，避免历史手填地址看起来像已选点 */
const displayAddress = computed(() => {
  if (!hasLocation.value) return ''
  const region = String(props.storeRegion || '').trim()
  const detail = String(props.storeAddressDetail || '').trim()
  if (region || detail) return `${region}${detail}`
  const full = String(props.storeAddress || '').trim()
  if (full) return full
  return '已选择地点'
})

function formatRegionFromAddress(addr) {
  if (!addr) return ''
  const parts = [addr.province, addr.city, addr.district].filter(Boolean)
  const unique = []
  for (const part of parts) {
    if (unique.length && unique[unique.length - 1] === part) continue
    unique.push(part)
  }
  return unique.join('')
}

function deriveDetailFromAddress(fullAddress, region, poiName) {
  const full = String(fullAddress || '').trim()
  const regionText = String(region || '').trim()
  if (full && regionText && full.startsWith(regionText)) {
    const detail = full.slice(regionText.length).trim()
    if (detail) return detail
  }
  if (poiName) return String(poiName).trim()
  return full
}

function emitLocation(payload) {
  emit('update:latitude', payload.latitude)
  emit('update:longitude', payload.longitude)
  emit('update:storeRegion', payload.storeRegion)
  emit('update:storeAddressDetail', payload.storeAddressDetail)
  emit('update:storeAddress', payload.storeAddress)
  emit('change', payload)
}

async function resolveLocation(lat, lng, fallbackDetail = '') {
  let storeRegion = ''
  let storeAddressDetail = String(fallbackDetail || '').trim()
  try {
    const addr = await reverseGeocode(String(lat), String(lng))
    if (addr) {
      storeRegion = formatRegionFromAddress(addr) || storeRegion
      storeAddressDetail = deriveDetailFromAddress(addr.address, storeRegion, fallbackDetail) || storeAddressDetail
    }
  } catch (error) {
    /* 坐标仍可用，地址解析失败时保留回退文案 */
  }
  const payload = {
    latitude: Number(lat),
    longitude: Number(lng),
    storeRegion,
    storeAddressDetail,
    storeAddress: formatStoreAddress({ storeRegion, storeAddressDetail }),
  }
  emitLocation(payload)
  return payload
}

function buildPickerUrl(config) {
  const key = String(config?.tencentKey || '').trim()
  const referer = String(config?.referer || 'lvyou-admin').trim()
  if (!key) return ''
  const params = new URLSearchParams({
    search: '1',
    type: '1',
    key,
    referer,
  })
  if (hasLocation.value) {
    params.set('coord', `${props.latitude},${props.longitude}`)
  }
  return `https://apis.map.qq.com/tools/locpicker?${params.toString()}`
}

async function ensurePickerConfig() {
  if (pickerConfig.value) return pickerConfig.value
  configLoading.value = true
  mapError.value = ''
  try {
    pickerConfig.value = await getMapPickerConfig()
    return pickerConfig.value
  } catch (error) {
    mapError.value = error?.msg || error?.message || '加载地图配置失败'
    showError(mapError.value)
    return null
  } finally {
    configLoading.value = false
  }
}

async function loadInlineMap() {
  const config = await ensurePickerConfig()
  const url = buildPickerUrl(config)
  if (!url) {
    mapError.value = '未配置腾讯地图 Key，无法使用地图选点'
    iframeUrl.value = ''
    return
  }
  mapError.value = ''
  iframeUrl.value = url
}

async function onPickerMessage(event) {
  const data = event?.data
  if (!data || data.module !== 'locationPicker') return
  const lat = data?.latlng?.lat
  const lng = data?.latlng?.lng
  if (lat == null || lng == null) return
  const fallbackDetail = String(data.poiname || data.poiaddress || '').trim()
  await resolveLocation(lat, lng, fallbackDetail)
}

onMounted(() => {
  window.addEventListener('message', onPickerMessage)
  loadInlineMap()
})

onBeforeUnmount(() => {
  window.removeEventListener('message', onPickerMessage)
})

watch(
  () => [props.latitude, props.longitude],
  ([lat, lng]) => {
    if (lat == null || lng == null || lat === '' || lng === '') return
    if (props.storeRegion || props.storeAddressDetail || props.storeAddress) return
    resolveLocation(lat, lng)
  },
)
</script>

<template>
  <div class="map-location-picker">
    <div class="map-location-picker__summary">
      <div class="map-location-picker__text" :class="{ 'is-empty': !displayAddress }">
        {{ displayAddress || emptyHint }}
      </div>
    </div>

    <div class="map-location-picker__map">
      <div v-if="configLoading && !iframeUrl" class="map-location-picker__empty">
        地图加载中...
      </div>
      <div v-else-if="mapError && !iframeUrl" class="map-location-picker__empty is-error">
        {{ mapError }}
      </div>
      <div v-else-if="iframeUrl" class="map-location-picker__iframe-wrap">
        <iframe :src="iframeUrl" class="map-location-picker__iframe" frameborder="0" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.map-location-picker {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.map-location-picker__summary {
  padding: 12px 14px;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}

.map-location-picker__text {
  color: var(--el-text-color-primary);
  line-height: 1.5;

  &.is-empty {
    color: var(--el-text-color-placeholder);
  }
}

.map-location-picker__map {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-fill-color-light);
}

.map-location-picker__iframe-wrap {
  width: 100%;
  height: 420px;
}

.map-location-picker__iframe {
  width: 100%;
  height: 100%;
  border: 0;
  display: block;
}

.map-location-picker__empty {
  height: 240px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary);

  &.is-error {
    color: var(--el-color-danger);
    padding: 0 16px;
    text-align: center;
  }
}
</style>
