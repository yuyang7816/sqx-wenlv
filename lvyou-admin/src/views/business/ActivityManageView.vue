<script setup>
import { reactive, ref } from 'vue'
import { Delete, Plus } from '@element-plus/icons-vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import PublishLocationPicker from '@/components/business/PublishLocationPicker.vue'
import { activityApi, updateActivityStateApi } from '@/api/modules/activity'
import { showSuccess } from '@/utils/modal'
import { wrapPublishPayload } from '@/utils/publish-payload.js'
import {
  publishLocationRules,
  ensurePublishLocation,
  sanitizePublishLocation,
  applyPublishLocation,
} from '@/utils/publish-location-rules.js'

const crudRef = ref()

/** 报名概况弹窗（列表行汇总，server 暂无报名明细分页接口） */
const enrollDialogVisible = ref(false)
const enrollSummary = ref(null)

/** 筛选：业务字段 */
const filters = reactive({
  name: '',
  phase: '',
  status: '',
})

const STATUS_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

const PHASE_OPTIONS = [
  { label: '进行中', value: 'ongoing' },
  { label: '即将开始', value: 'upcoming' },
  { label: '已结束', value: 'ended' },
]

function formatEnrollCount(row) {
  const count = row.enrollCount ?? 0
  const limit = row.enrollLimit ?? 0
  if (limit > 0) {
    return `${count} / ${limit}`
  }
  return `${count} / 不限`
}

function formatPriceLabel(row) {
  const price = row.priceFrom
  if (price == null || price === '') return '免费 / 线下说明'
  return `${price}（线下支付）`
}

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '活动名称', minWidth: 140, showOverflowTooltip: true },
  { prop: 'address', label: '活动地址', minWidth: 120, showOverflowTooltip: true },
  { prop: 'priceFrom', label: '参考价格', width: 120, formatter: formatPriceLabel },
  { prop: 'phaseLabel', label: '活动阶段', width: 100 },
  {
    prop: 'enrollCount',
    label: '报名人数',
    width: 110,
    formatter: (row) => formatEnrollCount(row),
  },
  { prop: 'startTime', label: '开始时间', minWidth: 170 },
  { prop: 'endTime', label: '结束时间', minWidth: 170 },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const formRules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  latitude: publishLocationRules,
  status: [{ required: true, message: '请选择上下架状态', trigger: 'change' }],
}

function emptyHighlight() {
  return { label: '', desc: '' }
}

function normalizeTagList(tags) {
  if (Array.isArray(tags)) {
    return tags.map((item) => String(item || '').trim()).filter(Boolean)
  }
  if (typeof tags === 'string' && tags.trim()) {
    return tags
      .split(/[,，、]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
  return []
}

function noticesListToText(notices) {
  if (Array.isArray(notices) && notices.length) {
    return notices.join('\n')
  }
  return ''
}

function noticesTextToList(text) {
  return text
    .split(/\n/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function ensureFormEditorFields(form) {
  sanitizePublishLocation(form)
  if (!Array.isArray(form._tagList)) {
    form._tagList = normalizeTagList(form.tags)
  }
  if (form._tagInput === undefined) {
    form._tagInput = ''
  }
  if (!form._highlights) {
    form._highlights = form.highlights?.length ? form.highlights.map((item) => ({ ...item })) : [emptyHighlight()]
  }
  if (form._noticesText === undefined) {
    form._noticesText = noticesListToText(form.notices)
  }
}

const buildSubmitPayload = wrapPublishPayload((form) => {
  ensurePublishLocation(form, '活动地址')
  const {
    _tagList,
    _tagInput,
    _highlights,
    _noticesText,
    tags,
    highlights,
    notices,
    tag,
    phase,
    phaseLabel,
    statusLabel,
    destinationName,
    enrollCount,
    createTime,
    updateTime,
    ...rest
  } = form
  const highlightPayload = (_highlights || [])
    .filter((item) => item.label?.trim() || item.desc?.trim())
    .map((item) => ({
      label: item.label?.trim() || '',
      desc: item.desc?.trim() || '',
    }))
  return {
    ...rest,
    latitude: Number(form.latitude),
    longitude: Number(form.longitude),
    address: String(form.address || '').trim(),
    tags: normalizeTagList(_tagList),
    highlights: highlightPayload,
    notices: noticesTextToList(_noticesText || ''),
    enrollLimit: rest.enrollLimit ?? 0,
  }
})

function addTag(form) {
  ensureFormEditorFields(form)
  const value = String(form._tagInput || '').trim()
  if (!value) return
  if (!form._tagList.includes(value)) {
    form._tagList.push(value)
  }
  form._tagInput = ''
}

function removeTag(form, index) {
  if (!Array.isArray(form._tagList)) return
  form._tagList.splice(index, 1)
}

function addHighlight(form) {
  ensureFormEditorFields(form)
  form._highlights.push(emptyHighlight())
}

function removeHighlight(form, index) {
  if (form._highlights.length <= 1) {
    Object.assign(form._highlights[0], emptyHighlight())
    return
  }
  form._highlights.splice(index, 1)
}

function buildQuery() {
  return {
    name: filters.name || undefined,
    phase: filters.phase || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { name: '', phase: '', status: '' })
  crudRef.value?.searchData({})
}

async function changeStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateActivityStateApi({ id: row.id, status: nextStatus })
  showSuccess(nextStatus === 1 ? '已上架，C 端列表可见' : '已下架，C 端列表不可见')
  await crudRef.value?.loadData()
}

function openEnrollSummary(row) {
  enrollSummary.value = {
    name: row.name,
    enrollCount: row.enrollCount ?? 0,
    enrollLimit: row.enrollLimit ?? 0,
    phaseLabel: row.phaseLabel || '-',
    startTime: row.startTime || '-',
    endTime: row.endTime || '-',
    statusLabel: row.statusLabel || '-',
  }
  enrollDialogVisible.value = true
}
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="精彩活动"
    :api="activityApi"
    :columns="columns"
    edit-from-row
    dialog-width="760px"
    :build-payload="buildSubmitPayload"
    :create-defaults="{
      status: 1,
      sort: 0,
      enrollLimit: 0,
      latitude: null,
      longitude: null,
      address: '',
      _tagList: [],
      _tagInput: '',
      _highlights: [emptyHighlight()],
      _noticesText: '',
    }"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="活动名称">
          <el-input v-model="filters.name" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="活动阶段">
          <el-select v-model="filters.phase" clearable placeholder="全部" style="width: 130px">
            <el-option v-for="item in PHASE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="上下架">
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
      <el-button size="small" type="primary" @click="openEnrollSummary(row)">报名</el-button>
      <RowStatusSwitch
        :model-value="Number(row.status)"
        active-text="上架"
        inactive-text="下架"
        @change="(status) => changeStatus(row, status)"
      />
    </template>

    <template #form="{ form }">
      <template v-if="ensureFormEditorFields(form)" />

      <el-form-item label="活动名称" prop="name" :rules="formRules.name">
        <el-input v-model="form.name" placeholder="如 万灵古镇民俗体验" />
      </el-form-item>
      <el-form-item label="活动地址" prop="latitude" :rules="formRules.latitude">
        <PublishLocationPicker
          :latitude="form.latitude"
          :longitude="form.longitude"
          :address="form.address"
          dialog-title="搜索并选择活动地址"
          empty-hint="尚未选择活动地址，请在下方地图中搜索选点"
          @change="(payload) => applyPublishLocation(form, payload)"
        />
      </el-form-item>
      <el-form-item label="参考价格">
        <el-input-number v-model="form.priceFrom" :min="0" :precision="2" />
        <span class="field-tip">仅展示说明，线上报名不收费；费用在线下签到时支付</span>
      </el-form-item>
      <el-alert
        class="enroll-mode-tip"
        title="本活动仅支持线上报名，不支持线上收费；有费用时请写清参考价格，用户线下签到支付。"
        type="info"
        :closable="false"
        show-icon
      />
      <el-form-item label="标签">
        <div class="tag-editor">
          <div v-if="form._tagList?.length" class="tag-editor__list">
            <el-tag
              v-for="(tag, index) in form._tagList"
              :key="`${tag}-${index}`"
              closable
              @close="removeTag(form, index)"
            >
              {{ tag }}
            </el-tag>
          </div>
          <div class="tag-editor__add">
            <el-input
              v-model="form._tagInput"
              placeholder="输入一个标签"
              @keyup.enter.prevent="addTag(form)"
            />
            <el-button type="primary" plain :icon="Plus" @click="addTag(form)">添加</el-button>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="封面图">
        <ImageUpload :model-value="form.coverImage || ''" @update:model-value="form.coverImage = $event" />
      </el-form-item>
      <el-form-item label="活动介绍">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="详情页展示的活动介绍" />
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker
          v-model="form.startTime"
          type="datetime"
          placeholder="选择开始时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker
          v-model="form.endTime"
          type="datetime"
          placeholder="选择结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="报名上限">
        <el-input-number v-model="form.enrollLimit" :min="0" />
        <span class="field-tip">0 表示不限人数</span>
      </el-form-item>
      <el-form-item v-if="form.enrollCount != null" label="已报名">
        <el-input :model-value="String(form.enrollCount)" disabled />
      </el-form-item>
      <el-form-item label="上下架" prop="status" :rules="formRules.status">
        <el-select v-model="form.status">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>

      <el-divider content-position="left">活动亮点</el-divider>
      <div v-for="(item, index) in form._highlights" :key="index" class="highlight-block">
        <div class="highlight-block__head">
          <span>亮点 {{ index + 1 }}</span>
          <el-button size="small" type="danger" plain :icon="Delete" @click="removeHighlight(form, index)">删除</el-button>
        </div>
        <el-form-item label="标题" :label-width="80">
          <el-input v-model="item.label" placeholder="如 非遗体验" />
        </el-form-item>
        <el-form-item label="描述" :label-width="80">
          <el-input v-model="item.desc" type="textarea" :rows="2" />
        </el-form-item>
      </div>
      <el-button type="primary" plain :icon="Plus" @click="addHighlight(form)">添加亮点</el-button>

      <el-divider content-position="left">参与须知</el-divider>
      <el-form-item label="须知条目">
        <el-input
          v-model="form._noticesText"
          type="textarea"
          :rows="4"
          placeholder="每行一条，如：请提前15分钟到场"
        />
      </el-form-item>
    </template>
  </CrudPage>

  <el-dialog v-model="enrollDialogVisible" title="报名概况" width="520px" destroy-on-close>
    <el-descriptions v-if="enrollSummary" :column="1" border>
      <el-descriptions-item label="活动名称">{{ enrollSummary.name }}</el-descriptions-item>
      <el-descriptions-item label="活动阶段">{{ enrollSummary.phaseLabel }}</el-descriptions-item>
      <el-descriptions-item label="活动时间">
        {{ enrollSummary.startTime }} ~ {{ enrollSummary.endTime }}
      </el-descriptions-item>
      <el-descriptions-item label="上下架">{{ enrollSummary.statusLabel }}</el-descriptions-item>
      <el-descriptions-item label="已报名人数">{{ enrollSummary.enrollCount }}</el-descriptions-item>
      <el-descriptions-item label="报名上限">
        {{ enrollSummary.enrollLimit > 0 ? enrollSummary.enrollLimit : '不限' }}
      </el-descriptions-item>
    </el-descriptions>
    <el-alert
      class="enroll-tip"
      title="当前 server 仅返回汇总 enroll_count；报名用户明细需后续扩展 admin 报名分页接口。"
      type="info"
      :closable="false"
      show-icon
    />
    <template #footer>
      <el-button @click="enrollDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}

.field-tip {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.enroll-mode-tip {
  margin: 0 0 16px;
}

.highlight-block {
  margin-bottom: 12px;
  padding: 12px;
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
}

.highlight-block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
}

.enroll-tip {
  margin-top: 16px;
}

.tag-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.tag-editor__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-editor__add {
  display: flex;
  gap: 8px;
  width: 100%;
}
</style>
