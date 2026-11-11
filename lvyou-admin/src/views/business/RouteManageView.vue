<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import { getMerchantApplyPage } from '@/api/modules/merchant'
import { routeApi, updateRouteStateApi } from '@/api/modules/route'
import { showSuccess } from '@/utils/modal'
import { formatOnShelfLabel } from '@/utils/status-label.js'
import { wrapPublishPayload } from '@/utils/publish-payload.js'

const crudRef = ref()

/** 已通过审核的旅游商户 */
const merchantOptions = ref([])

/** 筛选：业务字段 */
const filters = reactive({
  name: '',
  merchantName: '',
  status: '',
})

const routeDetailVisible = ref(false)
const routeDetail = ref(null)

const STATUS_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '线路名称', minWidth: 140 },
  { prop: 'merchantName', label: '所属商户', minWidth: 120 },
  { prop: 'priceFrom', label: '起步价', width: 90 },
  { prop: 'duration', label: '行程时长', width: 100 },
  { prop: 'meetPoint', label: '集合地点', minWidth: 120 },
  { prop: 'spots', label: '途经景点', minWidth: 140, showOverflowTooltip: true },
  { prop: 'statusLabel', label: '状态', width: 80, formatter: formatOnShelfLabel },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const formRules = {
  merchantId: [{ required: true, message: '请选择所属商户', trigger: 'change' }],
  name: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  priceFrom: [{ required: true, message: '请输入起步价', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function emptyItineraryDay(dayNo = 1) {
  return {
    dayNo,
    dayLabel: `第${dayNo}天`,
    title: '',
    content: '',
    images: [],
  }
}

function parseDayImages(item) {
  if (Array.isArray(item?.images) && item.images.length) {
    return item.images.filter(Boolean)
  }
  if (item?.coverImage) {
    return [item.coverImage]
  }
  return []
}

/** 扁平行程列表 → 按天编辑结构（与移动端商家发布一致：一条记录 = 一天） */
function listToItineraryDays(list) {
  if (!list?.length) {
    return [emptyItineraryDay()]
  }
  const sorted = [...list].sort((a, b) => (a.sort ?? 0) - (b.sort ?? 0))
  const days = sorted.map((item, index) => {
    const dayNo = index + 1
    return {
      dayNo,
      dayLabel: item.timeLabel || `第${dayNo}天`,
      title: item.name || '',
      content: item.description || '',
      images: parseDayImages(item),
    }
  })
  return days.length ? days : [emptyItineraryDay()]
}

/** 按天编辑结构 → 提交用 itineraryList */
function flattenItineraryDays(days) {
  const result = []
  for (const [dayIndex, day] of (days || []).entries()) {
    const title = String(day.title || '').trim()
    if (!title) continue
    const images = (day.images || []).filter(Boolean)
    const dayNo = day.dayNo ?? dayIndex + 1
    result.push({
      sort: dayIndex * 100,
      timeLabel: day.dayLabel || `第${dayNo}天`,
      name: title,
      description: day.content || '',
      images,
      coverImage: images[0] || '',
    })
  }
  return result
}

function parseTagsText(json) {
  if (!json) return ''
  try {
    const list = JSON.parse(json)
    if (Array.isArray(list)) {
      return list
        .map((item) => (typeof item === 'string' ? item : item?.text))
        .filter(Boolean)
        .join('、')
    }
  } catch {
    return json
  }
  return ''
}

function tagsTextToJson(text) {
  const labels = text.split(/[,，、]/).map((item) => item.trim()).filter(Boolean)
  if (!labels.length) return ''
  return JSON.stringify(labels)
}

function ensureFormEditorFields(form) {
  const editorKey = form.id ?? 'new'
  if (form._editorPreparedForKey !== editorKey) {
    form._editorPreparedForKey = editorKey
    form._itineraryDays = listToItineraryDays(form.itineraryList)
    form._tagsText = parseTagsText(form.tags)
  }
  return true
}

function openRouteDetail(row) {
  routeDetail.value = row
  routeDetailVisible.value = true
}

function itineraryDaysForDetail(list) {
  return listToItineraryDays(list).filter((day) => day.title?.trim())
}

const buildSubmitPayload = wrapPublishPayload((form) => {
  const {
    _itineraryDays,
    _tagsText,
    itineraryList,
    tags,
    statusLabel,
    merchantName,
    destinationName,
    createTime,
    updateTime,
    ...rest
  } = form
  return {
    ...rest,
    meetPoint: String(form.meetPoint || '').trim(),
    tags: tagsTextToJson(_tagsText),
    itineraryList: flattenItineraryDays(_itineraryDays),
  }
})

function addItineraryDay(form) {
  ensureFormEditorFields(form)
  const nextDay = (form._itineraryDays?.length || 0) + 1
  form._itineraryDays.push(emptyItineraryDay(nextDay))
}

function removeItineraryDay(form, dayIndex) {
  if (form._itineraryDays.length <= 1) return
  form._itineraryDays.splice(dayIndex, 1)
  form._itineraryDays.forEach((day, index) => {
    day.dayNo = index + 1
    day.dayLabel = `第${index + 1}天`
  })
}

function buildQuery() {
  return {
    name: filters.name || undefined,
    merchantName: filters.merchantName || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { name: '', merchantName: '', status: '' })
  crudRef.value?.searchData({})
}

async function changeStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateRouteStateApi({ id: row.id, status: nextStatus })
  showSuccess('状态已更新')
  await crudRef.value?.loadData()
}

async function loadMerchantOptions() {
  const data = await getMerchantApplyPage({
    page: 1,
    limit: 50,
    auditStatus: 1,
    storeType: 'travel',
  })
  merchantOptions.value = (data.list || []).filter((item) => item.merchantId)
}

onMounted(async () => {
  await loadMerchantOptions()
})
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="旅游线路"
    :api="routeApi"
    :columns="columns"
    edit-from-row
    dialog-width="860px"
    :build-payload="buildSubmitPayload"
    :create-defaults="{
      status: 1,
      sort: 0,
      meetPoint: '',
      _itineraryDays: [emptyItineraryDay()],
      _tagsText: '',
    }"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="线路名称">
          <el-input v-model="filters.name" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="商户名称">
          <el-input v-model="filters.merchantName" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="状态">
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

    <template #actions="{ row }">
      <el-button size="small" @click="openRouteDetail(row)">详情</el-button>
      <RowStatusSwitch
        :model-value="Number(row.status)"
        active-text="上架"
        inactive-text="下架"
        @change="(status) => changeStatus(row, status)"
      />
    </template>

    <template #form="{ form }">
      <template v-if="ensureFormEditorFields(form)" />

      <el-form-item label="所属商户" prop="merchantId" :rules="formRules.merchantId">
        <el-select v-model="form.merchantId" filterable placeholder="选择已通过审核的旅游商户" style="width: 100%">
          <el-option
            v-for="item in merchantOptions"
            :key="item.merchantId"
            :label="`${item.storeName}（ID:${item.id}）`"
            :value="item.merchantId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="线路名称" prop="name" :rules="formRules.name">
        <el-input v-model="form.name" placeholder="如 万灵古镇一日游" />
      </el-form-item>
      <el-form-item label="短描述">
        <el-input v-model="form.description" type="textarea" :rows="2" placeholder="列表页展示的一句话简介" />
      </el-form-item>
      <el-form-item label="详细介绍">
        <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="详情页完整介绍" />
      </el-form-item>
      <el-form-item label="起步价" prop="priceFrom" :rules="formRules.priceFrom">
        <el-input-number v-model="form.priceFrom" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="行程时长">
        <el-input v-model="form.duration" placeholder="如 1天、2天1晚" />
      </el-form-item>
      <el-form-item label="集合地点">
        <el-input v-model="form.meetPoint" placeholder="如：荣昌区游客中心" />
      </el-form-item>
      <el-form-item label="途经景点">
        <el-input v-model="form.spots" placeholder="逗号分隔，如 万灵古镇,安陶小镇" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form._tagsText" placeholder="古镇、一日游（顿号或逗号分隔）" />
      </el-form-item>
      <el-form-item label="封面图">
        <ImageUpload :model-value="form.coverImage || ''" @update:model-value="form.coverImage = $event" />
      </el-form-item>
      <el-form-item label="状态" prop="status" :rules="formRules.status">
        <el-select v-model="form.status">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>

      <el-divider content-position="left">每日行程（与移动端商家发布一致，一天一条）</el-divider>

      <div v-for="(day, dayIndex) in form._itineraryDays" :key="dayIndex" class="itinerary-day">
        <div class="itinerary-day__head">
          <span class="itinerary-day__idx">{{ day.dayLabel || `第${dayIndex + 1}天` }}</span>
          <el-button
            v-if="form._itineraryDays.length > 1"
            link
            type="danger"
            :icon="Delete"
            @click="removeItineraryDay(form, dayIndex)"
          >
            删除该天
          </el-button>
        </div>
        <el-form-item label="当天标题" required>
          <el-input v-model="day.title" placeholder="如：集合、景点游览" />
        </el-form-item>
        <el-form-item label="详细内容">
          <el-input v-model="day.content" type="textarea" :rows="3" placeholder="请填写当天行程说明" />
        </el-form-item>
        <el-form-item label="行程图片">
          <ImageUpload
            multiple
            :limit="9"
            tip="可上传多张，与移动端商家发布一致"
            :model-value="day.images || []"
            @update:model-value="day.images = $event"
          />
        </el-form-item>
      </div>

      <el-button type="success" plain :icon="Plus" class="add-day-btn" @click="addItineraryDay(form)">
        添加一天行程
      </el-button>
    </template>
  </CrudPage>

  <el-drawer v-model="routeDetailVisible" title="线路详情" size="720px" destroy-on-close>
    <template v-if="routeDetail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="线路名称" :span="2">{{ routeDetail.name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="所属商户">{{ routeDetail.merchantName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ formatOnShelfLabel(routeDetail) }}</el-descriptions-item>
        <el-descriptions-item label="起步价">{{ routeDetail.priceFrom ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="行程时长">{{ routeDetail.duration || '—' }}</el-descriptions-item>
        <el-descriptions-item label="集合地点" :span="2">{{ routeDetail.meetPoint || '—' }}</el-descriptions-item>
        <el-descriptions-item label="途经景点" :span="2">{{ routeDetail.spots || '—' }}</el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">{{ parseTagsText(routeDetail.tags) || '—' }}</el-descriptions-item>
        <el-descriptions-item label="短描述" :span="2">{{ routeDetail.description || '—' }}</el-descriptions-item>
        <el-descriptions-item label="详细介绍" :span="2">{{ routeDetail.intro || '—' }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ routeDetail.sort ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ routeDetail.createTime || '—' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="routeDetail.coverImage" class="detail-images">
        <p>封面图</p>
        <el-image :src="routeDetail.coverImage" fit="cover" class="detail-cover" preview-teleported />
      </div>

      <div v-if="itineraryDaysForDetail(routeDetail.itineraryList).length" class="detail-itinerary">
        <p>行程安排</p>
        <div
          v-for="(day, dayIndex) in itineraryDaysForDetail(routeDetail.itineraryList)"
          :key="dayIndex"
          class="detail-itinerary-day"
        >
          <h4>{{ day.dayLabel || `第${dayIndex + 1}天` }}</h4>
          <div v-if="day.title?.trim()" class="detail-itinerary-node">
            <div class="detail-itinerary-node__title">{{ day.title }}</div>
            <div v-if="day.content" class="detail-itinerary-node__desc">{{ day.content }}</div>
            <div v-if="(day.images || []).length" class="detail-node-gallery">
              <el-image
                v-for="(url, imgIndex) in day.images"
                :key="`${url}-${imgIndex}`"
                :src="url"
                fit="cover"
                class="detail-node-cover"
                :preview-src-list="day.images"
                preview-teleported
              />
            </div>
          </div>
        </div>
      </div>
    </template>
  </el-drawer>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}

.itinerary-day {
  margin-bottom: 20px;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  background: var(--el-fill-color-blank);
}

.itinerary-day__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.itinerary-day__idx {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.add-day-btn {
  margin-top: 4px;
}

.detail-images,
.detail-itinerary {
  margin-top: 16px;

  p {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.detail-cover {
  width: 120px;
  height: 90px;
  border-radius: 6px;
}

.detail-itinerary-day {
  margin-bottom: 16px;
  padding: 12px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;

  h4 {
    margin: 0 0 10px;
    font-size: 14px;
  }
}

.detail-itinerary-node {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px dashed var(--el-border-color-lighter);

  &:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: none;
  }
}

.detail-itinerary-node__title {
  font-weight: 600;
  margin-bottom: 4px;
}

.detail-itinerary-node__meta,
.detail-itinerary-node__desc {
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-bottom: 4px;
}

.detail-node-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 6px;
}

.detail-node-cover {
  width: 100px;
  height: 75px;
  border-radius: 6px;
  margin-top: 6px;
}
</style>
