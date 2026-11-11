<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import CrudPage from '@/components/common/CrudPage.vue'
import RowStatusSwitch from '@/components/common/RowStatusSwitch.vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import PublishLocationPicker from '@/components/business/PublishLocationPicker.vue'
import HeritageShopCategoryPanel from '@/components/business/HeritageShopCategoryPanel.vue'
import {
  heritageCraftApi,
  heritageGoodsApi,
  heritageShopApi,
  fetchHeritageShopCategoryOptionsApi,
  updateGoodsStateApi,
  updateHeritageCraftStateApi,
  updateHeritageShopStateApi,
} from '@/api/modules/heritage'
import { showSuccess } from '@/utils/modal'
import {
  createOptionLabelFormatter,
  formatOnShelfLabel,
  formatShopOpen,
} from '@/utils/status-label.js'
import { wrapPublishPayload } from '@/utils/publish-payload.js'
import {
  publishLocationRules,
  ensurePublishLocation,
  sanitizePublishLocation,
} from '@/utils/publish-location-rules.js'
import {
  HERITAGE_CRAFT_LEVEL_OPTIONS,
  resolveCraftLevelLabel,
} from '@/utils/heritage-craft-level.js'
import {
  normalizeShopCategoryOptions,
  resolveDefaultShopCategoryCode,
  resolveShopCategoryLabel,
} from '@/utils/heritage-shop-category.js'

const activeTab = ref('shop')

const shopCrudRef = ref()
const craftCrudRef = ref()
const goodsCrudRef = ref()

const shopOptions = ref([])
const shopCategoryOptions = ref([])

const shopFilters = reactive({
  name: '',
  category: '',
  status: '',
})

const craftFilters = reactive({
  name: '',
  tag: '',
  status: '',
})

const goodsFilters = reactive({
  name: '',
  shopName: '',
  category: '',
  status: '',
})

const GOODS_CATEGORY_OPTIONS = [
  { label: '精选', value: 'featured' },
  { label: '热销', value: 'hot' },
  { label: '新品', value: 'new' },
]

const SHOP_STATUS_OPTIONS = [
  { label: '营业', value: 1 },
  { label: '歇业', value: 0 },
]

const ONLINE_STATUS_OPTIONS = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

const shopColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '店铺名称', minWidth: 140, showOverflowTooltip: true },
  { prop: 'category', label: '分类', width: 90, formatter: (row) => resolveShopCategoryLabel(row.category, shopCategoryOptions.value) },
  { prop: 'address', label: '地址', minWidth: 120, showOverflowTooltip: true },
  { prop: 'tag', label: '主标签', width: 90 },
  { prop: 'phone', label: '联系电话', minWidth: 120 },
  { prop: 'rating', label: '评分', width: 70 },
  { prop: 'reviewCount', label: '评价数', width: 80 },
  { prop: 'statusLabel', label: '状态', width: 80, formatter: formatShopOpen },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const craftColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '工艺名称', minWidth: 140, showOverflowTooltip: true },
  { prop: 'levelLabel', label: '级别', width: 90 },
  {
    prop: 'tags',
    label: '分类标签',
    width: 120,
    showOverflowTooltip: true,
    formatter: (row) => tagsListToText(row.tags?.length ? row.tags : (row.tag ? [row.tag] : [])),
  },
  { prop: 'shopCategory', label: '好店分类', width: 90, formatter: (row) => resolveShopCategoryLabel(row.shopCategory, shopCategoryOptions.value) },
  { prop: 'description', label: '短描述', minWidth: 140, showOverflowTooltip: true },
  { prop: 'statusLabel', label: '状态', width: 80, formatter: formatOnShelfLabel },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const shopViewVisible = ref(false)
const shopViewRow = ref(null)

const goodsViewVisible = ref(false)
const goodsViewRow = ref(null)

const goodsColumns = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'coverImage', label: '封面', width: 100, type: 'image' },
  { prop: 'name', label: '商品名称', minWidth: 140, showOverflowTooltip: true },
  { prop: 'merchantServiceId', label: '来源', width: 100, formatter: (row) => (row.merchantServiceId ? '商户发布' : '平台录入') },
  { prop: 'shopName', label: '所属店铺', minWidth: 130 },
  { prop: 'category', label: '分类', width: 80, formatter: createOptionLabelFormatter(GOODS_CATEGORY_OPTIONS, 'category') },
  { prop: 'price', label: '售价', width: 90 },
  { prop: 'sales', label: '销量', width: 70 },
  { prop: 'stock', label: '库存', width: 70 },
  { prop: 'rating', label: '评分', width: 70 },
  { prop: 'statusLabel', label: '状态', width: 80, formatter: formatOnShelfLabel },
  { prop: 'sort', label: '排序', width: 70 },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

const shopFormRules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择好店分类', trigger: 'change' }],
  latitude: publishLocationRules,
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const craftFormRules = {
  name: [{ required: true, message: '请输入工艺名称', trigger: 'blur' }],
  latitude: publishLocationRules,
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const goodsFormRules = {
  shopId: [{ required: true, message: '请选择所属店铺', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function tagsListToText(tags) {
  if (Array.isArray(tags) && tags.length) return tags.join('、')
  return ''
}

function tagsTextToList(text) {
  return text
    .split(/[,，、]/)
    .map((item) => item.trim())
    .filter(Boolean)
}

function normalizeImageList(value) {
  if (Array.isArray(value)) {
    return value.map((item) => String(item || '').trim()).filter(Boolean)
  }
  if (typeof value === 'string' && value.trim()) {
    return [value.trim()]
  }
  return []
}

function normalizeCraftTagList(tags, fallbackTag) {
  if (Array.isArray(tags) && tags.length) {
    return tags.map((item) => String(item || '').trim()).filter(Boolean)
  }
  if (typeof tags === 'string' && tags.trim()) {
    return tagsTextToList(tags)
  }
  if (typeof fallbackTag === 'string' && fallbackTag.trim()) {
    return [fallbackTag.trim()]
  }
  return []
}

function bannersToUrls(banners) {
  if (!Array.isArray(banners)) return []
  return banners.map((item) => item?.image).filter(Boolean)
}

function urlsToBanners(urls) {
  return (urls || []).map((image) => ({ image }))
}

function ensureShopEditorFields(form) {
  const editorKey = form.id ?? 'new'
  if (form._editorPreparedForKey !== editorKey) {
    form._editorPreparedForKey = editorKey
    form._tagsText = tagsListToText(form.tags)
    form._servicesText = tagsListToText(form.services)
    delete form._locationSanitized
    sanitizePublishLocation(form)
  }
  if (form.rating != null && form.rating !== '') form.rating = Number(form.rating)
  if (form.reviewCount != null) form.reviewCount = Number(form.reviewCount)
  if (!form.category) {
    form.category = resolveDefaultShopCategoryCode(shopCategoryOptions.value)
  }
  if (form.status != null) form.status = Number(form.status)
  return true
}

function ensureCraftEditorFields(form) {
  const editorKey = form.id ?? 'new'
  if (form._editorPreparedForKey !== editorKey) {
    form._editorPreparedForKey = editorKey
    delete form._locationSanitized
    sanitizePublishLocation(form)
    form._tagList = normalizeCraftTagList(form.tags, form.tag)
    form._tagInput = ''
    if (form.levelCode && !form.levelLabel) {
      form.levelLabel = resolveCraftLevelLabel(form.levelCode, form.levelLabel)
    }
    if (!form.shopCategory) {
      form.shopCategory = resolveDefaultShopCategoryCode(shopCategoryOptions.value)
    }
    form.bannerImages = normalizeImageList(form.bannerImages || (form.coverImage ? [form.coverImage] : []))
  }
  if (form.status != null) form.status = Number(form.status)
  return true
}

function addCraftTag(form) {
  ensureCraftEditorFields(form)
  const value = String(form._tagInput || '').trim()
  if (!value) return
  if (!form._tagList.includes(value)) {
    form._tagList.push(value)
  }
  form._tagInput = ''
}

function removeCraftTag(form, index) {
  if (!Array.isArray(form._tagList)) return
  form._tagList.splice(index, 1)
}

function ensureGoodsEditorFields(form) {
  if (form._specsText === undefined) form._specsText = tagsListToText(form.specs)
  if (form._bannerUrls === undefined) form._bannerUrls = bannersToUrls(form.banners)
}

const buildShopPayload = wrapPublishPayload((form) => {
  ensurePublishLocation(form, '店铺地址')
  const {
    _tagsText,
    _servicesText,
    destinationName,
    statusLabel,
    coverFrom,
    coverTo,
    createTime,
    updateTime,
    ...rest
  } = form
  return {
    ...rest,
    tags: tagsTextToList(_tagsText || ''),
    services: tagsTextToList(_servicesText || ''),
    category: rest.category || resolveDefaultShopCategoryCode(shopCategoryOptions.value),
    sort: rest.sort ?? 0,
    reviewCount: rest.reviewCount ?? 0,
  }
})

const buildCraftPayload = wrapPublishPayload((form) => {
  ensurePublishLocation(form, '项目地址')
  ensureCraftEditorFields(form)
  const {
    _tagList,
    _tagInput,
    destinationName,
    statusLabel,
    createTime,
    updateTime,
    icon,
    coverFrom,
    coverTo,
    tag,
    tags,
    ...rest
  } = form
  const levelLabel = resolveCraftLevelLabel(rest.levelCode, rest.levelLabel)
  return {
    ...rest,
    levelLabel,
    tags: normalizeCraftTagList(_tagList),
    bannerImages: normalizeImageList(rest.bannerImages),
    shopCategory: rest.shopCategory || resolveDefaultShopCategoryCode(shopCategoryOptions.value),
    sort: rest.sort ?? 0,
  }
})

function buildGoodsPayload(form) {
  const {
    _specsText,
    _bannerUrls,
    shopName,
    statusLabel,
    sales,
    createTime,
    ...rest
  } = form
  return {
    ...rest,
    specs: tagsTextToList(_specsText || ''),
    banners: urlsToBanners(_bannerUrls || []),
    sort: rest.sort ?? 0,
    stock: rest.stock ?? 0,
  }
}

function onCraftLevelChange(form) {
  form.levelLabel = resolveCraftLevelLabel(form.levelCode, form.levelLabel)
}

function buildShopQuery() {
  return {
    name: shopFilters.name || undefined,
    category: shopFilters.category || undefined,
    status: shopFilters.status === '' ? undefined : Number(shopFilters.status),
  }
}

function buildCraftQuery() {
  return {
    name: craftFilters.name || undefined,
    tag: craftFilters.tag || undefined,
    status: craftFilters.status === '' ? undefined : Number(craftFilters.status),
  }
}

function buildGoodsQuery() {
  return {
    name: goodsFilters.name || undefined,
    shopName: goodsFilters.shopName || undefined,
    category: goodsFilters.category || undefined,
    status: goodsFilters.status === '' ? undefined : Number(goodsFilters.status),
  }
}

function handleShopSearch() {
  shopCrudRef.value?.searchData(buildShopQuery())
}

function handleShopReset() {
  Object.assign(shopFilters, { name: '', category: '', status: '' })
  shopCrudRef.value?.searchData({})
}

function handleCraftSearch() {
  craftCrudRef.value?.searchData(buildCraftQuery())
}

function handleCraftReset() {
  Object.assign(craftFilters, { name: '', tag: '', status: '' })
  craftCrudRef.value?.searchData({})
}

function handleGoodsSearch() {
  goodsCrudRef.value?.searchData(buildGoodsQuery())
}

function handleGoodsReset() {
  Object.assign(goodsFilters, { name: '', shopName: '', category: '', status: '' })
  goodsCrudRef.value?.searchData({})
}

async function changeShopStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateHeritageShopStateApi({ id: row.id, status: nextStatus })
  showSuccess(nextStatus === 1 ? '店铺已设为营业' : '店铺已设为歇业')
  await shopCrudRef.value?.loadData()
}

async function changeCraftStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateHeritageCraftStateApi({ id: row.id, status: nextStatus })
  showSuccess(nextStatus === 1 ? '工艺已上架' : '工艺已下架')
  await craftCrudRef.value?.loadData()
}

function openShopView(row) {
  shopViewVisible.value = true
  shopViewRow.value = { ...row }
  heritageShopApi.info(row.id).then((data) => {
    shopViewRow.value = data || row
  }).catch(() => {
    // 列表行兜底
  })
}

function openGoodsView(row) {
  goodsViewRow.value = { ...row }
  goodsViewVisible.value = true
}

async function changeGoodsStatus(row, status) {
  const nextStatus = Number(status)
  if (Number(row.status) === nextStatus) return
  await updateGoodsStateApi({ id: row.id, status: nextStatus })
  showSuccess(nextStatus === 1 ? '好物已上架' : '好物已强制下架')
  await goodsCrudRef.value?.loadData()
}

async function loadShopOptions() {
  const data = await heritageShopApi.page({ page: 1, limit: 50, status: 1 })
  shopOptions.value = data.list || []
}

async function loadShopCategoryOptions() {
  try {
    const list = await fetchHeritageShopCategoryOptionsApi()
    shopCategoryOptions.value = normalizeShopCategoryOptions(list)
  } catch (e) {
    shopCategoryOptions.value = normalizeShopCategoryOptions([])
  }
}

function handleShopCategoryChanged() {
  loadShopCategoryOptions()
}

watch(activeTab, (tab) => {
  if (tab === 'goods') loadShopOptions()
  if (tab === 'shop' || tab === 'craft' || tab === 'category') loadShopCategoryOptions()
})

onMounted(async () => {
  await Promise.all([loadShopOptions(), loadShopCategoryOptions()])
})
</script>

<template>
  <div class="heritage-manage">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="非遗好店" name="shop">
        <CrudPage
          ref="shopCrudRef"
          title="非遗店铺"
          :api="heritageShopApi"
          :columns="shopColumns"
          dialog-width="760px"
          :build-payload="buildShopPayload"
          :create-defaults="{ status: 1, sort: 0, reviewCount: 0, latitude: null, longitude: null, address: '', category: '', _tagsText: '', _servicesText: '' }"
        >
          <template #toolbar>
            <el-form :inline="true" class="filter-form" @submit.prevent="handleShopSearch">
              <el-form-item label="店铺名称">
                <el-input v-model="shopFilters.name" clearable placeholder="模糊搜索" />
              </el-form-item>
              <el-form-item label="分类">
                <el-select v-model="shopFilters.category" clearable placeholder="全部" style="width: 120px">
                  <el-option v-for="item in shopCategoryOptions" :key="item.code" :label="item.name" :value="item.code" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="shopFilters.status" clearable placeholder="全部" style="width: 120px">
                  <el-option v-for="item in SHOP_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleShopSearch">查询</el-button>
                <el-button @click="handleShopReset">重置</el-button>
              </el-form-item>
            </el-form>
          </template>

          <template #actions="{ row }">
            <el-button size="small" @click="openShopView(row)">查看详情</el-button>
            <RowStatusSwitch
              :model-value="Number(row.status)"
              active-text="营业"
              inactive-text="歇业"
              @change="(status) => changeShopStatus(row, status)"
            />
          </template>

          <template #form="{ form }">
            <template v-if="ensureShopEditorFields(form)" />
            <el-form-item label="店铺名称" prop="name" :rules="shopFormRules.name">
              <el-input v-model="form.name" placeholder="如 安陶非遗体验店" />
            </el-form-item>
            <el-form-item label="好店分类" prop="category" :rules="shopFormRules.category">
              <el-select v-model="form.category" placeholder="选择好店 Tab 分类" style="width: 100%">
                <el-option v-for="item in shopCategoryOptions" :key="item.code" :label="item.name" :value="item.code" />
              </el-select>
            </el-form-item>
            <el-form-item label="店铺地址" prop="latitude" :rules="shopFormRules.latitude">
              <PublishLocationPicker
                v-model:latitude="form.latitude"
                v-model:longitude="form.longitude"
                v-model:address="form.address"
                dialog-title="搜索并选择店铺地址"
                empty-hint="尚未选择店铺地址，请在下方地图中搜索选点"
              />
            </el-form-item>
            <el-form-item label="主标签">
              <el-input v-model="form.tag" placeholder="如 陶艺体验" />
            </el-form-item>
            <el-form-item label="标签列表">
              <el-input v-model="form._tagsText" placeholder="非遗展示、手工制陶（顿号或逗号分隔）" />
            </el-form-item>
            <el-form-item label="封面图">
              <ImageUpload :model-value="form.coverImage || ''" @update:model-value="form.coverImage = $event" />
            </el-form-item>
            <el-form-item label="简介">
              <el-input v-model="form.intro" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="详细介绍">
              <el-input v-model="form.introDetail" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="列表描述">
              <el-input v-model="form.description" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="营业时间">
              <el-input v-model="form.businessHours" placeholder="如 09:00-18:00" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="前台/预约电话" />
            </el-form-item>
            <el-form-item label="评分">
              <el-input-number v-model="form.rating" :min="0" :max="5" :precision="1" :step="0.1" />
            </el-form-item>
            <el-form-item label="评价数">
              <el-input-number v-model="form.reviewCount" :min="0" />
            </el-form-item>
            <el-form-item label="服务项目">
              <el-input v-model="form._servicesText" placeholder="陶艺体验、文创销售（顿号或逗号分隔）" />
            </el-form-item>
            <el-form-item label="状态" prop="status" :rules="shopFormRules.status">
              <el-select v-model="form.status">
                <el-option v-for="item in SHOP_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
          </template>
        </CrudPage>

        <el-dialog v-model="shopViewVisible" title="店铺详情" width="760px" destroy-on-close>
          <el-descriptions v-if="shopViewRow" :column="2" border>
            <el-descriptions-item label="ID">{{ shopViewRow.id }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ formatShopOpen(shopViewRow) }}</el-descriptions-item>
            <el-descriptions-item label="店铺名称" :span="2">{{ shopViewRow.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ resolveShopCategoryLabel(shopViewRow.category, shopCategoryOptions) }}</el-descriptions-item>
            <el-descriptions-item label="目的地">{{ shopViewRow.destinationName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="主标签">{{ shopViewRow.tag || '—' }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="2">{{ shopViewRow.address || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ shopViewRow.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="营业时间">{{ shopViewRow.businessHours || '—' }}</el-descriptions-item>
            <el-descriptions-item label="评分">{{ shopViewRow.rating ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="评价数">{{ shopViewRow.reviewCount ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ shopViewRow.sort ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="标签列表" :span="2">{{ tagsListToText(shopViewRow.tags) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="服务项目" :span="2">{{ tagsListToText(shopViewRow.services) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="简介" :span="2">{{ shopViewRow.intro || '—' }}</el-descriptions-item>
            <el-descriptions-item label="详细介绍" :span="2">{{ shopViewRow.introDetail || '—' }}</el-descriptions-item>
            <el-descriptions-item label="列表描述" :span="2">{{ shopViewRow.description || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ shopViewRow.createTime || '—' }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="shopViewRow?.coverImage" class="goods-view-cover">
            <p>封面图</p>
            <el-image :src="shopViewRow.coverImage" fit="cover" style="width: 120px; height: 120px" preview-teleported />
          </div>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="非遗名录" name="craft">
        <CrudPage
          ref="craftCrudRef"
          title="非遗工艺"
          :api="heritageCraftApi"
          :columns="craftColumns"
          dialog-width="720px"
          :build-payload="buildCraftPayload"
          :create-defaults="{ status: 1, sort: 0, latitude: null, longitude: null, address: '', bannerImages: [], _tagList: [], _tagInput: '' }"
        >
          <template #toolbar>
            <el-form :inline="true" class="filter-form" @submit.prevent="handleCraftSearch">
              <el-form-item label="工艺名称">
                <el-input v-model="craftFilters.name" clearable placeholder="模糊搜索" />
              </el-form-item>
              <el-form-item label="分类标签">
                <el-input v-model="craftFilters.tag" clearable placeholder="如 夏布" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="craftFilters.status" clearable placeholder="全部" style="width: 120px">
                  <el-option v-for="item in ONLINE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleCraftSearch">查询</el-button>
                <el-button @click="handleCraftReset">重置</el-button>
              </el-form-item>
            </el-form>
          </template>

          <template #actions="{ row }">
            <RowStatusSwitch
              :model-value="Number(row.status)"
              active-text="上架"
              inactive-text="下架"
              @change="(status) => changeCraftStatus(row, status)"
            />
          </template>

          <template #form="{ form }">
            <template v-if="ensureCraftEditorFields(form)" />
            <el-form-item label="工艺名称" prop="name" :rules="craftFormRules.name">
              <el-input v-model="form.name" placeholder="如 荣昌夏布" />
            </el-form-item>
            <el-form-item label="项目地址" prop="latitude" :rules="craftFormRules.latitude">
              <PublishLocationPicker
                v-model:latitude="form.latitude"
                v-model:longitude="form.longitude"
                v-model:address="form.address"
                dialog-title="搜索并选择项目地址"
                empty-hint="尚未选择项目地址，请在下方地图中搜索选点"
              />
            </el-form-item>
            <el-form-item label="短描述">
              <el-input v-model="form.description" placeholder="列表页展示文案" />
            </el-form-item>
            <el-form-item label="非遗级别">
              <el-select v-model="form.levelCode" clearable placeholder="选择非遗级别" style="width: 100%" @change="onCraftLevelChange(form)">
                <el-option v-for="item in HERITAGE_CRAFT_LEVEL_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="好店分类">
              <el-select v-model="form.shopCategory" placeholder="选择好店 Tab 分类" style="width: 100%">
                <el-option v-for="item in shopCategoryOptions" :key="item.code" :label="item.name" :value="item.code" />
              </el-select>
              <p class="field-tip">选项来自「好店分类」Tab；工艺关联店铺默认跟随此分类展示</p>
            </el-form-item>
            <el-form-item label="分类标签">
              <div class="tag-editor">
                <div v-if="form._tagList?.length" class="tag-editor__list">
                  <el-tag
                    v-for="(tagItem, index) in form._tagList"
                    :key="`${tagItem}-${index}`"
                    closable
                    @close="removeCraftTag(form, index)"
                  >
                    {{ tagItem }}
                  </el-tag>
                </div>
                <div class="tag-editor__add">
                  <el-input
                    v-model="form._tagInput"
                    placeholder="输入一个标签，回车或点添加"
                    @keyup.enter.prevent="addCraftTag(form)"
                  />
                  <el-button type="primary" plain :icon="Plus" @click="addCraftTag(form)">添加</el-button>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="封面图">
              <ImageUpload :model-value="form.coverImage || ''" @update:model-value="form.coverImage = $event" />
              <p class="field-tip">列表页封面；未上传轮播图时将作为详情页轮播兜底</p>
            </el-form-item>
            <el-form-item label="轮播图">
              <ImageUpload
                multiple
                :limit="9"
                tip="点击选择轮播图，可一次多选，最多 9 张"
                :model-value="form.bannerImages"
                @update:model-value="form.bannerImages = $event"
              />
            </el-form-item>
            <el-form-item label="概述">
              <el-input v-model="form.overview" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="工艺特点">
              <el-input v-model="form.feature" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="文化价值">
              <el-input v-model="form.culture" type="textarea" :rows="2" />
            </el-form-item>
            <el-form-item label="状态" prop="status" :rules="craftFormRules.status">
              <el-select v-model="form.status">
                <el-option v-for="item in ONLINE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" />
            </el-form-item>
          </template>
        </CrudPage>
      </el-tab-pane>

      <el-tab-pane label="非遗好物" name="goods">
        <CrudPage
          ref="goodsCrudRef"
          title="非遗好物"
          :api="heritageGoodsApi"
          :columns="goodsColumns"
          :show-create="false"
          :show-edit="false"
          :show-delete="false"
        >
          <template #toolbar>
            <el-form :inline="true" class="filter-form" @submit.prevent="handleGoodsSearch">
              <el-form-item label="商品名称">
                <el-input v-model="goodsFilters.name" clearable placeholder="模糊搜索" />
              </el-form-item>
              <el-form-item label="店铺名称">
                <el-input v-model="goodsFilters.shopName" clearable placeholder="模糊搜索" />
              </el-form-item>
              <el-form-item label="分类">
                <el-select v-model="goodsFilters.category" clearable placeholder="全部" style="width: 120px">
                  <el-option v-for="item in GOODS_CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="goodsFilters.status" clearable placeholder="全部" style="width: 120px">
                  <el-option v-for="item in ONLINE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleGoodsSearch">查询</el-button>
                <el-button @click="handleGoodsReset">重置</el-button>
              </el-form-item>
            </el-form>
          </template>

          <template #actions="{ row }">
            <el-button size="small" @click="openGoodsView(row)">查看</el-button>
            <RowStatusSwitch
              :model-value="Number(row.status)"
              active-text="上架"
              inactive-text="下架"
              @change="(status) => changeGoodsStatus(row, status)"
            />
          </template>
        </CrudPage>

        <el-dialog v-model="goodsViewVisible" title="好物详情" width="720px" destroy-on-close>
          <el-descriptions v-if="goodsViewRow" :column="2" border>
            <el-descriptions-item label="ID">{{ goodsViewRow.id }}</el-descriptions-item>
            <el-descriptions-item label="来源">{{ goodsViewRow.merchantServiceId ? '商户发布' : '平台录入' }}</el-descriptions-item>
            <el-descriptions-item label="商品名称" :span="2">{{ goodsViewRow.name || '—' }}</el-descriptions-item>
            <el-descriptions-item label="所属店铺">{{ goodsViewRow.shopName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ createOptionLabelFormatter(GOODS_CATEGORY_OPTIONS, 'category')(goodsViewRow) }}</el-descriptions-item>
            <el-descriptions-item label="售价">{{ goodsViewRow.price ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="库存">{{ goodsViewRow.stock ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="销量">{{ goodsViewRow.sales ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="评分">{{ goodsViewRow.rating ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ formatOnShelfLabel(goodsViewRow) }}</el-descriptions-item>
            <el-descriptions-item label="排序">{{ goodsViewRow.sort ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="列表描述" :span="2">{{ goodsViewRow.description || '—' }}</el-descriptions-item>
            <el-descriptions-item label="详情介绍" :span="2">{{ goodsViewRow.detail || '—' }}</el-descriptions-item>
            <el-descriptions-item label="规格" :span="2">{{ tagsListToText(goodsViewRow.specs) || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">{{ goodsViewRow.createTime || '—' }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="goodsViewRow?.coverImage" class="goods-view-cover">
            <p>封面图</p>
            <el-image :src="goodsViewRow.coverImage" fit="cover" style="width: 120px; height: 120px" preview-teleported />
          </div>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="好店分类" name="category">
        <HeritageShopCategoryPanel @changed="handleShopCategoryChanged" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}

.goods-view-cover {
  margin-top: 16px;
}

.goods-view-cover p {
  margin: 0 0 8px;
  font-size: 14px;
  color: #606266;
}

.tag-editor {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tag-editor__list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-editor__add {
  display: flex;
  gap: 8px;
}

.field-tip {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}
</style>
