<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import ScenicCategoryPanel from '@/components/business/ScenicCategoryPanel.vue'
import { scenicApi, updateScenicStateApi, fetchScenicCategoryOptionsApi } from '@/api/modules/scenic'
import { showError, showSuccess } from '@/utils/modal'
import PublishLocationPicker from '@/components/business/PublishLocationPicker.vue'
import { formatOnShelfLabel } from '@/utils/status-label.js'
import { wrapPublishPayload } from '@/utils/publish-payload.js'
import {
  publishLocationRules,
  ensurePublishLocation,
  sanitizePublishLocation,
  applyPublishLocation,
} from '@/utils/publish-location-rules.js'

const activeTab = ref('scenic')
const crudRef = ref()
const categoryOptions = ref([])

/** 筛选：业务字段（名称、分类、等级、上下架） */
const filters = reactive({
  name: '',
  category: '',
  levelLabel: '',
  status: '',
})

const STATUS_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

function formatCoordinate(row) {
  const lat = row.latitude
  const lng = row.longitude
  if (lat == null && lng == null) return '-'
  return `${lat ?? '-'}, ${lng ?? '-'}`
}

const columns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '景区名称', minWidth: 140, showOverflowTooltip: true },
  { prop: 'address', label: '地址', minWidth: 120, showOverflowTooltip: true },
  { prop: 'categoryLabel', label: '分类', width: 100 },
  { prop: 'levelLabel', label: '景区等级', width: 90 },
  {
    prop: 'latitude',
    label: '坐标(纬,经)',
    minWidth: 150,
    formatter: (row) => formatCoordinate(row),
  },
  { prop: 'openTime', label: '开放时间', minWidth: 120, showOverflowTooltip: true },
  { prop: 'phone', label: '咨询电话', width: 120 },
  { prop: 'priceFrom', label: '门票起步价', width: 100 },
  { prop: 'statusLabel', label: '上下架', width: 80, formatter: formatOnShelfLabel },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const formRules = {
  name: [{ required: true, message: '请输入景区名称', trigger: 'blur' }],
  latitude: publishLocationRules,
  status: [{ required: true, message: '请选择上下架状态', trigger: 'change' }],
}

function normalizeTagList(tags) {
  if (!Array.isArray(tags)) return []
  return tags.map((item) => String(item || '').trim()).filter(Boolean)
}

function normalizeImageList(value) {
  if (Array.isArray(value)) {
    return value.filter(Boolean)
  }
  if (typeof value === 'string' && value.trim()) {
    return [value.trim()]
  }
  return []
}

function ensureFormEditorFields(form) {
  sanitizePublishLocation(form)
  if (!Array.isArray(form._tagList)) {
    form._tagList = normalizeTagList(form.tags)
  }
  if (form._tagInput === undefined) {
    form._tagInput = ''
  }
  if (!Array.isArray(form._tipList)) {
    form._tipList = normalizeTagList(form.tips)
  }
  if (form._tipInput === undefined) {
    form._tipInput = ''
  }
  if (!Array.isArray(form.bannerImages)) {
    form.bannerImages = normalizeImageList(form.bannerImages)
  }
  if (!Array.isArray(form.featureImages)) {
    form.featureImages = normalizeImageList(form.featureImages || form.featureImage)
  }
}

const buildSubmitPayload = wrapPublishPayload((form) => {
  ensurePublishLocation(form, '景区地址')
  const {
    _tagList,
    _tagInput,
    _tipList,
    _tipInput,
    tags,
    tips,
    featureImage,
    categoryLabel,
    statusLabel,
    destinationName,
    createTime,
    updateTime,
    ...rest
  } = form
  return {
    ...rest,
    tags: normalizeTagList(_tagList),
    tips: normalizeTagList(_tipList),
    bannerImages: normalizeImageList(rest.bannerImages),
    featureImages: normalizeImageList(rest.featureImages),
    priceFrom: rest.priceFrom ?? 0,
    sort: rest.sort ?? 0,
  }
})

function addTag(form) {
  ensureFormEditorFields(form)
  const value = String(form._tagInput || '').trim()
  if (!value) return
  if (/[,，、]/.test(value)) {
    showError('请一次添加一个标签，不要用逗号或顿号拼接')
    return
  }
  if (!form._tagList.includes(value)) {
    form._tagList.push(value)
  }
  form._tagInput = ''
}

function removeTag(form, index) {
  if (!Array.isArray(form._tagList)) return
  form._tagList.splice(index, 1)
}

function addTip(form) {
  ensureFormEditorFields(form)
  const value = String(form._tipInput || '').trim()
  if (!value) return
  if (!form._tipList.includes(value)) {
    form._tipList.push(value)
  }
  form._tipInput = ''
}

function removeTip(form, index) {
  if (!Array.isArray(form._tipList)) return
  form._tipList.splice(index, 1)
}

function buildQuery() {
  return {
    name: filters.name || undefined,
    category: filters.category || undefined,
    levelLabel: filters.levelLabel || undefined,
    status: filters.status === '' ? undefined : Number(filters.status),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, {
    name: '',
    category: '',
    levelLabel: '',
    status: '',
  })
  crudRef.value?.searchData({})
}

async function changeStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateScenicStateApi({ id: row.id, status: nextStatus })
  showSuccess(nextStatus === 1 ? '已上架，C 端列表/地图可见' : '已下架，C 端列表/地图不可见')
  await crudRef.value?.loadData()
}

async function loadCategoryOptions() {
  try {
    const list = await fetchScenicCategoryOptionsApi()
    categoryOptions.value = Array.isArray(list) ? list : []
  } catch (e) {
    categoryOptions.value = []
  }
}

function handleCategoryChanged() {
  loadCategoryOptions()
}

watch(activeTab, (tab) => {
  if (tab === 'scenic') {
    loadCategoryOptions()
  }
})

onMounted(loadCategoryOptions)
</script>

<template>
  <div class="scenic-manage">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="景区列表" name="scenic">
        <CrudPage
          ref="crudRef"
          title="旅游景区"
          embedded
          :show-create="false"
          :api="scenicApi"
          :columns="columns"
          edit-from-row
          dialog-width="820px"
          :build-payload="buildSubmitPayload"
          :create-defaults="{
            status: 1,
            sort: 0,
            priceFrom: 0,
            bannerImages: [],
            featureImages: [],
            _tagList: [],
            _tagInput: '',
            _tipList: [],
            _tipInput: '',
          }"
        >
          <template #toolbar>
            <div class="toolbar-row">
              <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
                <el-form-item label="景区名称">
                  <el-input v-model="filters.name" clearable placeholder="模糊搜索" />
                </el-form-item>
                <el-form-item label="分类">
                  <el-select v-model="filters.category" clearable placeholder="全部" style="width: 130px">
                    <el-option
                      v-for="item in categoryOptions"
                      :key="item.code"
                      :label="item.name"
                      :value="item.code"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="景区等级">
                  <el-input v-model="filters.levelLabel" clearable placeholder="如 5A" style="width: 100px" />
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
              <el-button type="primary" class="toolbar-create" @click="crudRef?.handleCreate()">新增</el-button>
            </div>
          </template>

          <template #actions="{ row }">
            <RowStatusSwitch
              :model-value="Number(row.status)"
              active-text="上架"
              inactive-text="下架"
              @change="(status) => changeStatus(row, status)"
            />
          </template>

          <template #form="{ form }">
            <template v-if="ensureFormEditorFields(form)" />

            <el-form-item label="景区名称" prop="name" :rules="formRules.name">
              <el-input v-model="form.name" placeholder="如 万灵古镇" />
            </el-form-item>
            <el-form-item label="景区地址" prop="latitude" :rules="formRules.latitude">
              <PublishLocationPicker
                :latitude="form.latitude"
                :longitude="form.longitude"
                :address="form.address"
                dialog-title="搜索并选择景区地址"
                empty-hint="尚未选择景区地址，请在下方地图中搜索选点"
                @change="(payload) => applyPublishLocation(form, payload)"
              />
            </el-form-item>
            <el-form-item label="开放时间">
              <el-input v-model="form.openTime" placeholder="如 08:30-17:30" />
            </el-form-item>
            <el-form-item label="咨询电话">
              <el-input v-model="form.phone" placeholder="景区咨询电话" />
            </el-form-item>
            <el-form-item label="景区等级">
              <el-input v-model="form.levelLabel" placeholder="如 5A、4A" />
            </el-form-item>
            <el-form-item label="分类">
              <el-select v-model="form.category" clearable placeholder="选择分类" style="width: 100%">
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.code"
                  :label="item.name"
                  :value="item.code"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="门票起步价">
              <el-input-number v-model="form.priceFrom" :min="0" :precision="2" />
            </el-form-item>
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
                    placeholder="输入一个标签后点击添加"
                    @keyup.enter.prevent="addTag(form)"
                  />
                  <el-button type="primary" plain :icon="Plus" @click="addTag(form)">添加</el-button>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="封面图">
              <ImageUpload :model-value="form.coverImage || ''" @update:model-value="form.coverImage = $event" />
            </el-form-item>
            <el-form-item label="特色配图">
              <ImageUpload
                multiple
                :limit="9"
                tip="点击选择特色配图，可一次多选，最多 9 张"
                :model-value="form.featureImages"
                @update:model-value="form.featureImages = $event"
              />
            </el-form-item>
            <el-form-item label="轮播相册">
              <ImageUpload
                multiple
                :limit="9"
                tip="点击选择轮播图，可一次多选，最多 9 张"
                :model-value="form.bannerImages"
                @update:model-value="form.bannerImages = $event"
              />
            </el-form-item>
            <el-form-item label="景区介绍">
              <el-input v-model="form.intro" type="textarea" :rows="3" placeholder="景区特色介绍，详情页展示" />
            </el-form-item>
            <el-form-item label="游玩提示">
              <div class="tag-editor">
                <div v-if="form._tipList?.length" class="tag-editor__list">
                  <el-tag
                    v-for="(tip, index) in form._tipList"
                    :key="`${tip}-${index}`"
                    closable
                    @close="removeTip(form, index)"
                  >
                    {{ tip }}
                  </el-tag>
                </div>
                <div class="tag-editor__add">
                  <el-input
                    v-model="form._tipInput"
                    placeholder="输入一条提示后点击添加"
                    @keyup.enter.prevent="addTip(form)"
                  />
                  <el-button type="primary" plain :icon="Plus" @click="addTip(form)">添加</el-button>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="上下架" prop="status" :rules="formRules.status">
              <el-select v-model="form.status">
                <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
          </template>
        </CrudPage>
      </el-tab-pane>

      <el-tab-pane label="景区分类" name="category">
        <ScenicCategoryPanel @changed="handleCategoryChanged" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.scenic-manage :deep(.embedded-crud) {
  padding-top: 4px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.filter-form {
  flex: 1;
  min-width: 0;
  margin-bottom: 0;
}

.toolbar-create {
  flex-shrink: 0;
  margin-top: 0;
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
