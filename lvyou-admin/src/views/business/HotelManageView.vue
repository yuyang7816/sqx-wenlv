<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import PublishLocationPicker from '@/components/business/PublishLocationPicker.vue'
import { hotelApi, hotelRoomApi } from '@/api/modules/hotel'
import { getMerchantApplyPage } from '@/api/modules/merchant'
import { formatOnShelfLabel, formatRoomSale } from '@/utils/status-label.js'
import { wrapPublishPayload } from '@/utils/publish-payload.js'
import {
  publishLocationRules,
  ensurePublishLocation,
  sanitizePublishLocation,
  applyPublishLocation,
} from '@/utils/publish-location-rules.js'

const activeTab = ref('hotel')

const hotelCrudRef = ref()
const roomCrudRef = ref()

/** 已通过审核的酒店商户（表单关联商户） */
const merchantOptions = ref([])
/** 房型 Tab 可选酒店列表 */
const hotelOptions = ref([])

/** 酒店列表筛选：业务字段，禁止用内部 ID 做唯一筛选项 */
const hotelFilters = reactive({
  name: '',
  merchantName: '',
  status: '',
})

/** 房型筛选：须先选所属酒店（业务关联） */
const roomHotelId = ref(null)
const roomFilters = reactive({
  name: '',
  status: '',
})

const STATUS_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

const ROOM_STATUS_OPTIONS = [
  { label: '在售', value: 1 },
  { label: '下架', value: 0 },
]

const hotelColumns = [
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '酒店名称', minWidth: 160 },
  { prop: 'merchantName', label: '所属商户', minWidth: 140 },
  { prop: 'address', label: '酒店地址', minWidth: 160, showOverflowTooltip: true },
  { prop: 'starLevel', label: '星级', width: 70 },
  { prop: 'priceFrom', label: '起步价', width: 90 },
  { prop: 'statusLabel', label: '状态', width: 80, formatter: formatOnShelfLabel },
]

const roomColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '房型名称', minWidth: 140 },
  { prop: 'hotelName', label: '所属酒店', minWidth: 140 },
  { prop: 'merchantName', label: '所属商户', minWidth: 120 },
  { prop: 'spec', label: '规格', minWidth: 160 },
  { prop: 'price', label: '每晚价格', width: 100 },
  { prop: 'stock', label: '库存', width: 80 },
  { prop: 'statusLabel', label: '状态', width: 80, formatter: formatRoomSale },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const hotelFormRules = {
  merchantId: [{ required: true, message: '请选择所属商户', trigger: 'change' }],
  name: [{ required: true, message: '请输入酒店名称', trigger: 'blur' }],
  latitude: publishLocationRules,
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const roomFormRules = {
  name: [{ required: true, message: '请输入房型名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入每晚价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const selectedRoomHotel = computed(() =>
  hotelOptions.value.find((item) => item.id === roomHotelId.value),
)

const roomCreateDefaults = computed(() => ({
  hotelId: roomHotelId.value,
  merchantId: selectedRoomHotel.value?.merchantId,
  status: 1,
  stock: 10,
  sort: 0,
}))

const hotelDetailVisible = ref(false)
const hotelDetail = ref(null)

function openHotelDetail(row) {
  hotelDetail.value = row
  hotelDetailVisible.value = true
}

function parseBannerImages(json) {
  if (!json) return []
  try {
    const list = JSON.parse(json)
    return Array.isArray(list) ? list.filter(Boolean) : []
  } catch {
    return []
  }
}

function stringifyBannerImages(list) {
  return JSON.stringify(list || [])
}

function parseFacilitiesLabels(json) {
  if (!json) return ''
  try {
    const list = JSON.parse(json)
    if (!Array.isArray(list)) return ''
    return list.map((item) => item.label).filter(Boolean).join('、')
  } catch {
    return ''
  }
}

function labelsToFacilitiesJson(text) {
  const labels = text.split(/[,，、]/).map((item) => item.trim()).filter(Boolean)
  if (!labels.length) return ''
  return JSON.stringify(
    labels.map((label, index) => ({
      id: index + 1,
      icon: 'grid-fill',
      label,
    })),
  )
}

function buildHotelQuery() {
  return {
    name: hotelFilters.name || undefined,
    merchantName: hotelFilters.merchantName || undefined,
    status: hotelFilters.status === '' ? undefined : Number(hotelFilters.status),
  }
}

const buildHotelSubmitPayload = wrapPublishPayload((form) => {
  sanitizePublishLocation(form)
  ensurePublishLocation(form, '酒店地址')
  return {
    ...form,
    latitude: Number(form.latitude),
    longitude: Number(form.longitude),
    address: String(form.address || '').trim(),
    category: '',
  }
})

function buildRoomQuery() {
  return {
    hotelId: roomHotelId.value,
    name: roomFilters.name || undefined,
    status: roomFilters.status === '' ? undefined : Number(roomFilters.status),
  }
}

function handleHotelSearch() {
  hotelCrudRef.value?.searchData(buildHotelQuery())
}

function handleHotelReset() {
  Object.assign(hotelFilters, {
    name: '',
    merchantName: '',
    status: '',
  })
  hotelCrudRef.value?.searchData({})
}

function handleRoomSearch() {
  if (!roomHotelId.value) return
  roomCrudRef.value?.searchData(buildRoomQuery())
}

function handleRoomReset() {
  Object.assign(roomFilters, { name: '', status: '' })
  if (roomHotelId.value) {
    roomCrudRef.value?.searchData(buildRoomQuery())
  }
}

async function loadMerchantOptions() {
  const data = await getMerchantApplyPage({
    page: 1,
    limit: 50,
    auditStatus: 1,
    storeType: 'hotel',
  })
  merchantOptions.value = (data.list || []).filter((item) => item.merchantId)
}

async function loadHotelOptions() {
  const data = await hotelApi.page({ page: 1, limit: 50, status: 1 })
  hotelOptions.value = data.list || []
}

watch(roomHotelId, (id) => {
  if (id && activeTab.value === 'room') {
    roomCrudRef.value?.searchData(buildRoomQuery())
  }
})

watch(activeTab, (tab) => {
  if (tab === 'room') {
    loadHotelOptions()
  }
})

onMounted(async () => {
  await loadMerchantOptions()
})
</script>

<template>
  <div class="hotel-manage">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="酒店管理" name="hotel">
        <CrudPage
          ref="hotelCrudRef"
          title="酒店"
          :api="hotelApi"
          :columns="hotelColumns"
          edit-from-row
          :create-defaults="{ status: 1, sort: 0, starLevel: 3, latitude: null, longitude: null, address: '' }"
          :build-payload="buildHotelSubmitPayload"
        >
          <template #toolbar>
            <el-form :inline="true" class="filter-form" @submit.prevent="handleHotelSearch">
              <el-form-item label="酒店名称">
                <el-input v-model="hotelFilters.name" clearable placeholder="模糊搜索" />
              </el-form-item>
              <el-form-item label="商户名称">
                <el-input v-model="hotelFilters.merchantName" clearable placeholder="模糊搜索" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="hotelFilters.status" clearable placeholder="全部" style="width: 120px">
                  <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleHotelSearch">查询</el-button>
                <el-button @click="handleHotelReset">重置</el-button>
              </el-form-item>
            </el-form>
          </template>

          <template #actions="{ row }">
            <el-button size="small" @click="openHotelDetail(row)">详情</el-button>
          </template>

          <template #form="{ form }">
            <template v-if="sanitizePublishLocation(form)" />
            <el-form-item label="所属商户" prop="merchantId" :rules="hotelFormRules.merchantId">
              <el-select v-model="form.merchantId" filterable placeholder="选择已通过审核的酒店商户" style="width: 100%">
                <el-option
                  v-for="item in merchantOptions"
                  :key="item.merchantId"
                  :label="`${item.storeName}（ID:${item.id}）`"
                  :value="item.merchantId"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="酒店名称" prop="name" :rules="hotelFormRules.name">
              <el-input v-model="form.name" placeholder="如 荣昌万灵古镇精品客栈" />
            </el-form-item>
            <el-form-item label="星级">
              <el-input-number v-model="form.starLevel" :min="1" :max="5" />
            </el-form-item>
            <el-form-item label="酒店地址" prop="latitude" :rules="hotelFormRules.latitude">
              <PublishLocationPicker
                :latitude="form.latitude"
                :longitude="form.longitude"
                :address="form.address"
                dialog-title="搜索并选择酒店地址"
                empty-hint="尚未选择酒店地址，请在下方地图中搜索选点"
                @change="(payload) => applyPublishLocation(form, payload)"
              />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="前台电话" />
            </el-form-item>
            <el-form-item label="评分">
              <el-input-number v-model="form.score" :min="0" :max="5" :precision="1" :step="0.1" />
            </el-form-item>
            <el-form-item label="标签">
              <el-input v-model="form.tag" placeholder="如 古镇风情" />
            </el-form-item>
            <el-form-item label="起步价">
              <el-input-number v-model="form.priceFrom" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="封面图">
              <ImageUpload
                :model-value="form.coverImage || ''"
                @update:model-value="form.coverImage = $event"
              />
            </el-form-item>
            <el-form-item label="轮播相册">
              <ImageUpload
                multiple
                :limit="9"
                :model-value="parseBannerImages(form.bannerImages)"
                @update:model-value="form.bannerImages = stringifyBannerImages($event)"
              />
            </el-form-item>
            <el-form-item label="入住时间">
              <el-input v-model="form.checkInTime" placeholder="如 14:00 后入住" />
            </el-form-item>
            <el-form-item label="退房时间">
              <el-input v-model="form.checkOutTime" placeholder="如 12:00 前退房" />
            </el-form-item>
            <el-form-item label="酒店设施">
              <el-input
                :model-value="parseFacilitiesLabels(form.facilities)"
                placeholder="免费WiFi、免费停车、含早餐（顿号或逗号分隔）"
                @update:model-value="form.facilities = labelsToFacilitiesJson($event)"
              />
            </el-form-item>
            <el-form-item label="简介">
              <el-input v-model="form.intro" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="状态" prop="status" :rules="hotelFormRules.status">
              <el-select v-model="form.status">
                <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
          </template>
        </CrudPage>

        <el-drawer v-model="hotelDetailVisible" title="酒店详情" size="640px" destroy-on-close>
          <el-descriptions v-if="hotelDetail" :column="2" border>
            <el-descriptions-item label="酒店名称" :span="2">{{ hotelDetail.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="所属商户">{{ hotelDetail.merchantName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="星级">{{ hotelDetail.starLevel ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="起步价">{{ hotelDetail.priceFrom ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ formatOnShelfLabel(hotelDetail) }}</el-descriptions-item>
            <el-descriptions-item label="评分">{{ hotelDetail.score ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="标签">{{ hotelDetail.tag || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ hotelDetail.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="2">{{ hotelDetail.address || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="hotelDetail.latitude != null && hotelDetail.longitude != null" label="坐标" :span="2">
              {{ hotelDetail.latitude }}, {{ hotelDetail.longitude }}
            </el-descriptions-item>
            <el-descriptions-item label="入住时间">{{ hotelDetail.checkInTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="退房时间">{{ hotelDetail.checkOutTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="酒店设施" :span="2">{{ parseFacilitiesLabels(hotelDetail.facilities) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ hotelDetail.intro || '—' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ hotelDetail.sort ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ hotelDetail.createTime || '—' }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="hotelDetail?.coverImage" class="detail-images">
            <p>封面图</p>
            <el-image :src="hotelDetail.coverImage" fit="cover" class="detail-cover" preview-teleported />
          </div>
          <div v-if="parseBannerImages(hotelDetail?.bannerImages).length" class="detail-images">
            <p>轮播相册</p>
            <div class="detail-gallery">
              <el-image
                v-for="(url, index) in parseBannerImages(hotelDetail.bannerImages)"
                :key="`${url}-${index}`"
                :src="url"
                fit="cover"
                class="detail-cover"
                :preview-src-list="parseBannerImages(hotelDetail.bannerImages)"
                preview-teleported
              />
            </div>
          </div>
        </el-drawer>
      </el-tab-pane>

      <el-tab-pane label="房型管理" name="room">
        <el-alert
          v-if="!roomHotelId"
          title="请先选择所属酒店，再维护该酒店下的房型"
          type="info"
          :closable="false"
          show-icon
          class="room-tip"
        />
        <div class="room-toolbar">
          <el-form :inline="true" class="filter-form" @submit.prevent="handleRoomSearch">
            <el-form-item label="所属酒店" required>
              <el-select
                v-model="roomHotelId"
                filterable
                placeholder="选择酒店"
                style="width: 240px"
                @change="handleRoomSearch"
              >
                <el-option v-for="item in hotelOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="房型名称">
              <el-input v-model="roomFilters.name" clearable placeholder="模糊搜索" :disabled="!roomHotelId" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="roomFilters.status" clearable placeholder="全部" style="width: 120px" :disabled="!roomHotelId">
                <el-option v-for="item in ROOM_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :disabled="!roomHotelId" @click="handleRoomSearch">查询</el-button>
              <el-button :disabled="!roomHotelId" @click="handleRoomReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <CrudPage
          v-if="roomHotelId"
          :key="roomHotelId"
          ref="roomCrudRef"
          title="房型"
          :api="hotelRoomApi"
          :columns="roomColumns"
          edit-from-row
          :default-query="{ hotelId: roomHotelId }"
          :create-defaults="roomCreateDefaults"
        >
          <template #form="{ form }">
            <el-form-item label="所属酒店">
              <el-input :model-value="selectedRoomHotel?.name || ''" disabled />
            </el-form-item>
            <el-form-item label="房型名称" prop="name" :rules="roomFormRules.name">
              <el-input v-model="form.name" placeholder="如 古镇观景大床房" />
            </el-form-item>
            <el-form-item label="规格描述">
              <el-input v-model="form.spec" placeholder="28㎡ | 大床 | 含早餐" />
            </el-form-item>
            <el-form-item label="每晚价格" prop="price" :rules="roomFormRules.price">
              <el-input-number v-model="form.price" :min="0" :precision="2" />
            </el-form-item>
            <el-form-item label="库存">
              <el-input-number v-model="form.stock" :min="1" />
            </el-form-item>
            <el-form-item label="封面图">
              <ImageUpload
                :model-value="form.coverImage || ''"
                @update:model-value="form.coverImage = $event"
              />
            </el-form-item>
            <el-form-item label="状态" prop="status" :rules="roomFormRules.status">
              <el-select v-model="form.status">
                <el-option v-for="item in ROOM_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
          </template>
        </CrudPage>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}

.room-tip {
  margin-bottom: 16px;
}

.room-toolbar {
  margin-bottom: 8px;
}

.detail-images {
  margin-top: 16px;

  p {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.detail-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-cover {
  width: 120px;
  height: 90px;
  border-radius: 6px;
}
</style>
