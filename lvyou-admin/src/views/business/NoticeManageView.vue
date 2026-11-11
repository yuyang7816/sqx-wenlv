<script setup>
import { onMounted, reactive, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import RichTextEditor from '@/components/common/RichTextEditor.vue'
import { destinationApi } from '@/api/modules/destination'
import { noticeApi, updateNoticePinnedApi, updateNoticeStateApi } from '@/api/modules/notice'
import { showError, showSuccess } from '@/utils/modal'
import { formatNoticeState } from '@/utils/status-label.js'
import { wrapPublishPayload } from '@/utils/publish-payload.js'

const crudRef = ref()
const destinationOptions = ref([])

/** 筛选：标题、分类、目的地、发布状态（发布时间见列表列展示与表单设置） */
const filters = reactive({
  title: '',
  category: '',
  destinationId: '',
  state: '',
})

const CATEGORY_OPTIONS = [
  { label: '活动', value: 'activity' },
  { label: '景区', value: 'scenic' },
  { label: '交通', value: 'traffic' },
  { label: '政策', value: 'policy' },
]

const STATE_OPTIONS = [
  { label: '已发布', value: 1 },
  { label: '草稿', value: 0 },
]

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'title', label: '公告标题', minWidth: 160, showOverflowTooltip: true },
  { prop: 'categoryLabel', label: '分类', width: 90 },
  {
    prop: 'destinationName',
    label: '目的地',
    minWidth: 110,
    formatter: (_row, _col, val) => val || '全平台',
  },
  { prop: 'source', label: '来源机构', minWidth: 120, showOverflowTooltip: true },
  { prop: 'pinned', label: '置顶', width: 100 },
  { prop: 'publishTime', label: '发布时间', minWidth: 170 },
  { prop: 'expireTime', label: '过期时间', minWidth: 170 },
  { prop: 'stateLabel', label: '状态', width: 90, formatter: formatNoticeState },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const formRules = {
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入公告正文', trigger: 'blur' }],
  publishTime: [{ required: true, message: '请选择发布时间', trigger: 'change' }],
  state: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function defaultPublishTime() {
  const now = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

function buildQuery() {
  return {
    title: filters.title || undefined,
    category: filters.category || undefined,
    destinationId: filters.destinationId === '' ? undefined : Number(filters.destinationId),
    state: filters.state === '' ? undefined : Number(filters.state),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { title: '', category: '', destinationId: '', state: '' })
  crudRef.value?.searchData({})
}

const buildSubmitPayload = wrapPublishPayload((form) => {
  const {
    categoryLabel,
    destinationName,
    stateLabel,
    createTime,
    updateTime,
    ...rest
  } = form
  return {
    ...rest,
    summary: rest.summary || '',
    source: rest.source || '',
  }
})

async function changeState(row, state) {
  const nextState = Number(state)
  if (Number(row.state) === nextState) return
  await updateNoticeStateApi({ id: row.id, state: nextState })
  showSuccess(nextState === 1 ? '已发布' : '已转为草稿')
  await crudRef.value?.loadData()
}

async function changePinned(row, pinned) {
  const nextPinned = Number(pinned)
  if (Number(row.pinned) === nextPinned) return
  if (nextPinned === 1 && Number(row.state) !== 1) {
    showError('请先发布公告后再置顶')
    return
  }
  await updateNoticePinnedApi({ id: row.id, pinned: nextPinned })
  showSuccess(nextPinned === 1 ? '已置顶，将展示在首页公告栏' : '已取消置顶')
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
    title="公告管理"
    :api="noticeApi"
    :columns="columns"
    edit-from-row
    dialog-width="860px"
    :create-defaults="{ state: 0, pinned: 0, sort: 0, publishTime: defaultPublishTime(), content: '' }"
    :build-payload="buildSubmitPayload"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="标题">
          <el-input v-model="filters.title" clearable placeholder="模糊搜索" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="filters.category" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="item in CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="目的地">
          <el-select v-model="filters.destinationId" clearable filterable placeholder="全部" style="width: 160px">
            <el-option v-for="item in destinationOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.state" clearable placeholder="全部" style="width: 120px">
            <el-option v-for="item in STATE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #column-pinned="{ row }">
      <RowStatusSwitch
        :key="`${row.id}-${row.pinned}`"
        :model-value="Number(row.pinned)"
        :active-value="1"
        :inactive-value="0"
        active-text="置顶"
        inactive-text="否"
        @change="(pinned) => changePinned(row, pinned)"
      />
    </template>

    <template #actions="{ row }">
      <RowStatusSwitch
        :model-value="Number(row.state)"
        :active-value="1"
        :inactive-value="0"
        active-text="发布"
        inactive-text="草稿"
        @change="(state) => changeState(row, state)"
      />
    </template>

    <template #form="{ form }">
      <el-form-item label="公告标题" prop="title" :rules="formRules.title">
        <el-input v-model="form.title" placeholder="如 荣昌区暑期文旅活动公告" />
      </el-form-item>
      <el-form-item label="摘要">
        <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="列表与跑马灯展示用短摘要" />
      </el-form-item>
      <el-form-item label="分类" prop="category" :rules="formRules.category">
        <el-select v-model="form.category" placeholder="选择分类" style="width: 100%">
          <el-option v-for="item in CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="来源机构">
        <el-input v-model="form.source" placeholder="如 荣昌区文旅委" />
      </el-form-item>
      <el-form-item label="是否置顶">
        <el-select v-model="form.pinned" :disabled="form.state !== 1">
          <el-option label="否" :value="0" />
          <el-option label="是（全局仅一条，展示在首页公告栏）" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="发布时间" prop="publishTime" :rules="formRules.publishTime">
        <el-date-picker
          v-model="form.publishTime"
          type="datetime"
          placeholder="选择发布时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="过期时间">
        <el-date-picker
          v-model="form.expireTime"
          type="datetime"
          placeholder="留空表示长期有效"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="状态" prop="state" :rules="formRules.state">
        <el-select v-model="form.state">
          <el-option v-for="item in STATE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" />
      </el-form-item>
      <el-form-item label="公告正文" prop="content" :rules="formRules.content">
        <RichTextEditor v-model="form.content" min-height="240px" />
      </el-form-item>
    </template>
  </CrudPage>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}
</style>
