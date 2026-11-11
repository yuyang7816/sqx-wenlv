<script setup>
import { onMounted, reactive, ref } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import PublishLocationPicker from '@/components/business/PublishLocationPicker.vue'
import { destinationApi } from '@/api/modules/destination'
import {
  fetchTransportSchedule,
  fetchTransportStations,
  transportRouteApi,
  updateTransportRouteStateApi,
} from '@/api/modules/transport'
import { showSuccess } from '@/utils/modal'
import { formatOnShelf } from '@/utils/status-label.js'
import { wrapPublishPayload } from '@/utils/publish-payload.js'
import {
  publishLocationRules,
  ensurePublishLocation,
  sanitizePublishLocation,
  applyPublishLocation,
} from '@/utils/publish-location-rules.js'

const crudRef = ref()

/** 目的地下拉 */
const destinationOptions = ref([])

/** 筛选：ID、名称、起终点站、目的地、状态 */
const filters = reactive({
  id: '',
  name: '',
  startStation: '',
  endStation: '',
  destinationId: '',
  status: '',
})

const STATUS_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

const FOOTER_TYPE_OPTIONS = [
  { label: '展开站点', value: 'expand' },
  { label: '定位展示', value: 'location' },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'name', label: '线路名称', minWidth: 120 },
  { prop: 'destinationName', label: '目的地', minWidth: 100 },
  { prop: 'startStation', label: '起点站', minWidth: 110 },
  { prop: 'endStation', label: '终点站', minWidth: 110 },
  { prop: 'firstBus', label: '首班', width: 80 },
  { prop: 'lastBus', label: '末班', width: 80 },
  { prop: 'stationCount', label: '站点数', width: 80 },
  { prop: 'status', label: '上架状态', width: 90, formatter: formatOnShelf },
  { prop: 'statusLabel', label: '运行状态', width: 90 },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const formRules = {
  routeCode: [{ required: true, message: '请输入线路编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  latitude: publishLocationRules,
  status: [{ required: true, message: '请选择上架状态', trigger: 'change' }],
}

function emptyStationRow() {
  return ''
}

function emptyScheduleRow() {
  return { station: '', timesText: '' }
}

/** 初始化表单编辑区（站点/班次行） */
function ensureEditorFields(form) {
  sanitizePublishLocation(form)
  if (!form._stationRows) {
    form._stationRows = form.stations?.length ? [...form.stations] : [emptyStationRow()]
  }
  if (!form._scheduleRows) {
    form._scheduleRows = form.scheduleRows?.length
      ? form.scheduleRows.map((row) => ({
          station: row.station || '',
          timesText: (row.times || []).join('、'),
        }))
      : [emptyScheduleRow()]
  }
  if (form.id && form.routeCode && !form._supplementLoaded) {
    form._supplementLoaded = true
    loadSupplementForEdit(form)
  }
  return true
}

/** 编辑时从 App 公开接口补充站点/班次（管理端无 info 接口；下架线路可能拉不到） */
async function loadSupplementForEdit(form) {
  try {
    const [stationVo, scheduleVo] = await Promise.all([
      fetchTransportStations(form.routeCode),
      fetchTransportSchedule(form.routeCode),
    ])
    if (stationVo?.stations?.length) {
      form._stationRows = [...stationVo.stations]
    }
    if (scheduleVo?.scheduleRows?.length) {
      form._scheduleRows = scheduleVo.scheduleRows.map((row) => ({
        station: row.station || '',
        timesText: (row.times || []).join('、'),
      }))
    }
  } catch {
    // 下架线路或暂无数据时保持空编辑区
  }
}

function addStationRow(form) {
  ensureEditorFields(form)
  form._stationRows.push(emptyStationRow())
}

function removeStationRow(form, index) {
  if (form._stationRows.length <= 1) {
    form._stationRows[0] = ''
    return
  }
  form._stationRows.splice(index, 1)
}

function addScheduleRow(form) {
  ensureEditorFields(form)
  form._scheduleRows.push(emptyScheduleRow())
}

function removeScheduleRow(form, index) {
  if (form._scheduleRows.length <= 1) {
    form._scheduleRows[0] = emptyScheduleRow()
    return
  }
  form._scheduleRows.splice(index, 1)
}

function parseTimesText(text) {
  return String(text || '')
    .split(/[,，、;；\s]+/)
    .map((item) => item.trim())
    .filter(Boolean)
}

const buildSubmitPayload = wrapPublishPayload((form) => {
  ensurePublishLocation(form, '所属区域')
  const {
    _stationRows,
    _scheduleRows,
    _supplementLoaded,
    destinationName,
    stationCount,
    createTime,
    updateTime,
    address,
    ...rest
  } = form

  const stations = (_stationRows || [])
    .map((name) => String(name || '').trim())
    .filter(Boolean)

  const scheduleRows = (_scheduleRows || [])
    .map((row) => ({
      station: String(row.station || '').trim(),
      times: parseTimesText(row.timesText),
    }))
    .filter((row) => row.station)

  return {
    ...rest,
    stations,
    scheduleRows,
  }
})

function buildQuery() {
  return {
    id: filters.id === '' ? undefined : Number(filters.id),
    name: filters.name || undefined,
    startStation: filters.startStation || undefined,
    endStation: filters.endStation || undefined,
    destinationId: filters.destinationId === '' ? undefined : Number(filters.destinationId),
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, {
    id: '',
    name: '',
    startStation: '',
    endStation: '',
    destinationId: '',
    status: '',
  })
  crudRef.value?.searchData({})
}

async function changeStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateTransportRouteStateApi({ id: row.id, status: nextStatus })
  showSuccess('状态已更新')
  await crudRef.value?.loadData()
}

async function loadDestinationOptions() {
  const data = await destinationApi.page({ page: 1, limit: 50, status: 1 })
  destinationOptions.value = data.list || []
}

onMounted(loadDestinationOptions)
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="公交线路"
    :api="transportRouteApi"
    :columns="columns"
    edit-from-row
    dialog-width="820px"
    :build-payload="buildSubmitPayload"
    :create-defaults="{
      status: 1,
      sort: 0,
      statusKey: 'running',
      statusLabel: '运行中',
      showRealtimeStops: 0,
      expandable: 1,
      footerType: 'expand',
      currentStopIndex: 0,
      latitude: null,
      longitude: null,
      address: '',
      _stationRows: [''],
      _scheduleRows: [{ station: '', timesText: '' }],
    }"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="ID">
          <el-input v-model="filters.id" clearable placeholder="线路ID" />
        </el-form-item>
        <el-form-item label="线路名称">
          <el-input v-model="filters.name" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="起点站">
          <el-input v-model="filters.startStation" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="终点站">
          <el-input v-model="filters.endStation" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="目的地">
          <el-select v-model="filters.destinationId" clearable placeholder="全部" style="width: 160px">
            <el-option v-for="item in destinationOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="上架状态">
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
      <RowStatusSwitch
        :model-value="Number(row.status)"
        active-text="上架"
        inactive-text="下架"
        @change="(status) => changeStatus(row, status)"
      />
    </template>

    <template #form="{ form, editingId }">
      <template v-if="ensureEditorFields(form)" />

      <el-divider content-position="left">基本信息</el-divider>

      <el-form-item label="线路编码" prop="routeCode" :rules="formRules.routeCode">
        <el-input v-model="form.routeCode" :disabled="!!editingId" placeholder="如 101" />
      </el-form-item>
      <el-form-item label="线路名称" prop="name" :rules="formRules.name">
        <el-input v-model="form.name" placeholder="如 101路" />
      </el-form-item>
      <el-form-item label="所属区域" prop="latitude" :rules="formRules.latitude">
        <PublishLocationPicker
          :latitude="form.latitude"
          :longitude="form.longitude"
          :address="form.address"
          @change="(payload) => applyPublishLocation(form, payload)"
          dialog-title="搜索并选择线路所属区域"
          empty-hint="尚未选择区域，请在下方地图中搜索选点（用于匹配目的地）"
        />
      </el-form-item>
      <el-form-item label="首班时间">
        <el-input v-model="form.firstBus" placeholder="如 06:30" />
      </el-form-item>
      <el-form-item label="末班时间">
        <el-input v-model="form.lastBus" placeholder="如 21:00" />
      </el-form-item>
      <el-form-item label="起点站">
        <el-input v-model="form.startStation" placeholder="列表展示用起点站名" />
      </el-form-item>
      <el-form-item label="终点站">
        <el-input v-model="form.endStation" placeholder="列表展示用终点站名" />
      </el-form-item>
      <el-form-item label="上架状态" prop="status" :rules="formRules.status">
        <el-select v-model="form.status">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="运行状态文案">
        <el-input v-model="form.statusLabel" placeholder="如 运行中" />
      </el-form-item>
      <el-form-item label="主题色">
        <el-input v-model="form.accentColor" placeholder="如 #FF7D00" />
      </el-form-item>
      <el-form-item label="浅色主题">
        <el-input v-model="form.accentLight" placeholder="如 #FFF2E8" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>

      <el-divider content-position="left">C 端展示配置</el-divider>

      <el-form-item label="展示实时站">
        <el-select v-model="form.showRealtimeStops">
          <el-option label="否" :value="0" />
          <el-option label="是" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="可展开站点">
        <el-select v-model="form.expandable">
          <el-option label="否" :value="0" />
          <el-option label="是" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="底部类型">
        <el-select v-model="form.footerType" clearable placeholder="选择类型">
          <el-option v-for="item in FOOTER_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="当前站序号">
        <el-input-number v-model="form.currentStopIndex" :min="0" />
      </el-form-item>
      <el-form-item label="当前站">
        <el-input v-model="form.currentStop" />
      </el-form-item>
      <el-form-item label="下一站">
        <el-input v-model="form.nextStop" />
      </el-form-item>
      <el-form-item label="预计到站">
        <el-input v-model="form.nextEta" placeholder="如 5分钟" />
      </el-form-item>

      <el-divider content-position="left">站点列表（按顺序，保存后 C 端 stations 一致）</el-divider>

      <div v-for="(station, index) in form._stationRows" :key="index" class="station-row">
        <el-input v-model="form._stationRows[index]" placeholder="站点名称" />
        <el-button size="small" type="danger" plain :icon="Delete" @click="removeStationRow(form, index)">删除</el-button>
      </div>
      <el-button type="primary" plain :icon="Plus" class="add-row-btn" @click="addStationRow(form)">添加站点</el-button>

      <el-divider content-position="left">班次时刻表（站点 + 时刻，顿号/逗号分隔）</el-divider>

      <div v-for="(row, index) in form._scheduleRows" :key="index" class="schedule-row">
        <el-input v-model="row.station" placeholder="站点名称" class="schedule-row__station" />
        <el-input v-model="row.timesText" placeholder="如 06:30、07:00、07:30" class="schedule-row__times" />
        <el-button size="small" type="danger" plain :icon="Delete" @click="removeScheduleRow(form, index)">删除</el-button>
      </div>
      <el-button type="primary" plain :icon="Plus" class="add-row-btn" @click="addScheduleRow(form)">添加班次行</el-button>
    </template>
  </CrudPage>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}

.station-row,
.schedule-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.schedule-row__station {
  width: 180px;
  flex-shrink: 0;
}

.schedule-row__times {
  flex: 1;
}

.add-row-btn {
  margin-bottom: 8px;
}
</style>
