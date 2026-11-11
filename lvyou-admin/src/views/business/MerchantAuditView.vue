<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import BasePagination from '@/components/common/BasePagination.vue'
import { auditMerchantApply, getMerchantApplyDetail, getMerchantApplyPage, getMerchantStoreTypeList } from '@/api/modules/merchant'
import { isAuth } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'
import { useMenuBadgeStore } from '@/stores/menuBadge'
import {
  formatStoreAddress,
  getQualificationsByType,
  parseQualImageUrls,
} from '@/utils/merchant-onboarding-admin'

const menuBadgeStore = useMenuBadgeStore()

/** 审核状态 Tab：pending / approved / rejected */
const AUDIT_TABS = [
  { name: 'pending', label: '待审核', status: 0 },
  { name: 'approved', label: '已通过', status: 1 },
  { name: 'rejected', label: '已驳回', status: 2 },
]

const STORE_TYPE_OPTIONS = ref([])

const activeTab = ref('pending')
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const drawerVisible = ref(false)
const drawerLoading = ref(false)
const auditLoading = ref(false)
const detail = ref(null)

const filters = reactive({
  contactName: '',
  storeName: '',
  contactPhone: '',
  storeType: '',
})

const currentAuditStatus = computed(() => {
  return AUDIT_TABS.find((item) => item.name === activeTab.value)?.status
})

const canAudit = computed(() => isAuth('lvyou:merchant:apply:audit') && [0, 2].includes(detail.value?.auditStatus))

const qualImageList = computed(() => {
  const images = detail.value?.qualImages
  if (!images || typeof images !== 'object') return []
  const storeType = detail.value?.storeType
  const items = []
  Object.entries(images).forEach(([key, raw]) => {
    const urls = parseQualImageUrls(raw)
    if (!urls.length) return
    const label = formatQualLabel(key, storeType)
    urls.forEach((url, index) => {
      items.push({
        key: `${key}-${index}`,
        url,
        label: urls.length > 1 ? `${label} ${index + 1}` : label,
      })
    })
  })
  return items
})

const allPreviewUrls = computed(() => {
  if (!detail.value) return []
  const urls = [
    detail.value.licenseImage,
    detail.value.legalIdFront,
    detail.value.legalIdBack,
    ...qualImageList.value.map((item) => item.url),
  ].filter(Boolean)
  return [...new Set(urls)]
})

function formatQualLabel(key, storeType) {
  if (storeType) {
    const item = getQualificationsByType(storeType).required.find((entry) => entry.key === key)
    if (item) return item.label
  }
  const labelMap = {
    license: '营业执照',
    legalId: '法人身份证',
    foodLicense: '食品经营许可证',
    healthLicense: '卫生许可证',
  }
  return labelMap[key] || `行业资质（${key}）`
}

function auditStatusTagType(status) {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'info'
}

function buildQuery() {
  return {
    page: pageNum.value,
    limit: pageSize.value,
    auditStatus: currentAuditStatus.value,
    contactName: filters.contactName || undefined,
    storeName: filters.storeName || undefined,
    contactPhone: filters.contactPhone || undefined,
    storeType: filters.storeType || undefined,
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await getMerchantApplyPage(buildQuery())
    tableData.value = data.list
    total.value = data.total
  } catch (error) {
    showError(error?.msg || error?.message || '加载申请列表失败')
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pageNum.value = 1
  loadData()
}

function handleReset() {
  Object.assign(filters, { contactName: '', storeName: '', contactPhone: '', storeType: '' })
  pageNum.value = 1
  loadData()
}

function handleTabChange() {
  pageNum.value = 1
  loadData()
}

async function openDetail(row) {
  drawerVisible.value = true
  drawerLoading.value = true
  detail.value = null
  try {
    detail.value = await getMerchantApplyDetail(row.id)
  } catch (error) {
    showError(error?.msg || error?.message || '加载申请详情失败')
    drawerVisible.value = false
  } finally {
    drawerLoading.value = false
  }
}

async function submitAudit(id, storeName, action, reason) {
  auditLoading.value = true
  try {
    await auditMerchantApply({ id, action, reason: reason || undefined })
    showSuccess(action === 'pass' ? '已通过入驻申请' : '已驳回入驻申请')
    drawerVisible.value = false
    loadData()
    menuBadgeStore.refresh()
  } catch (error) {
    showError(error?.msg || error?.message || '审核操作失败')
  } finally {
    auditLoading.value = false
  }
}

async function handleApprove(row) {
  const target = row || detail.value
  if (!target?.id) return
  await ElMessageBox.confirm(`确认通过「${target.storeName}」的入驻申请吗？`, '审核通过', { type: 'warning' })
  await submitAudit(target.id, target.storeName, 'pass')
}

async function handleReject(row) {
  const target = row || detail.value
  if (!target?.id) return
  const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回申请', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入驳回原因',
    inputValidator: (val) => {
      if (!String(val || '').trim()) return '驳回原因不能为空'
      return true
    },
  })
  await submitAudit(target.id, target.storeName, 'reject', String(value).trim())
}

function canAuditRow(row) {
  return isAuth('lvyou:merchant:apply:audit') && [0, 2].includes(row?.auditStatus)
}

watch(activeTab, handleTabChange)

async function loadStoreTypeOptions() {
  try {
    const items = await getMerchantStoreTypeList()
    STORE_TYPE_OPTIONS.value = (items || []).map((item) => ({
      label: item.label,
      value: item.code,
    }))
  } catch (e) {
    STORE_TYPE_OPTIONS.value = []
  }
}

onMounted(() => {
  loadStoreTypeOptions()
  loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <strong>商户入驻审核</strong>
      </div>
    </template>

    <el-tabs v-model="activeTab" class="audit-tabs">
      <el-tab-pane v-for="tab in AUDIT_TABS" :key="tab.name" :label="tab.label" :name="tab.name" />
    </el-tabs>

    <el-form :inline="true" class="search-grid" @submit.prevent>
      <el-form-item label="联系人">
        <el-input v-model="filters.contactName" clearable placeholder="联系人" />
      </el-form-item>
      <el-form-item label="门店名称">
        <el-input v-model="filters.storeName" clearable placeholder="门店名称" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="filters.contactPhone" clearable placeholder="联系电话" />
      </el-form-item>
      <el-form-item label="门店类型">
        <el-select v-model="filters.storeType" clearable placeholder="全部">
          <el-option v-for="item in STORE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-space>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-space>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border empty-text="暂无申请记录">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="storeName" label="门店名称" min-width="140" />
      <el-table-column prop="storeTypeLabel" label="门店类型" min-width="100" />
      <el-table-column prop="contactName" label="联系人" min-width="100" />
      <el-table-column prop="contactPhone" label="联系电话" min-width="130" />
      <el-table-column label="店铺地址" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">{{ formatStoreAddress(row) || '—' }}</template>
      </el-table-column>
      <el-table-column prop="userName" label="申请人" min-width="100" />
      <el-table-column prop="userPhone" label="申请人手机" min-width="130" />
      <el-table-column prop="auditStatusLabel" label="审核状态" min-width="100">
        <template #default="{ row }">
          <el-tag :type="auditStatusTagType(row.auditStatus)">{{ row.auditStatusLabel || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submittedAt" label="提交时间" min-width="170" />
      <el-table-column prop="auditedAt" label="审核时间" min-width="170" />
      <el-table-column label="操作" min-width="180" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="canAuditRow(row)">
              <el-button size="small" type="success" :loading="auditLoading" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" :loading="auditLoading" @click="handleReject(row)">驳回</el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />

    <el-drawer v-model="drawerVisible" title="入驻申请详情" size="640px" destroy-on-close>
      <div v-loading="drawerLoading" class="detail-body">
        <template v-if="detail">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="申请ID">{{ detail.id ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="审核状态">
              <el-tag :type="auditStatusTagType(detail.auditStatus)">{{ detail.auditStatusLabel || '—' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="门店名称">{{ detail.storeName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="门店类型">{{ detail.storeTypeLabel || detail.storeType || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.storeType === 'culture'" label="好店分类">
              {{ detail.shopCategoryLabel || detail.shopCategory || '—' }}
            </el-descriptions-item>
            <el-descriptions-item label="意向目的地">{{ detail.destinationName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.contactPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="门店地区">{{ detail.storeRegion || '—' }}</el-descriptions-item>
            <el-descriptions-item label="详细地址">{{ detail.storeAddress || '—' }}</el-descriptions-item>
            <el-descriptions-item label="营业时间">{{ detail.storeHours || '—' }}</el-descriptions-item>
            <el-descriptions-item label="申请人">{{ detail.userName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="申请人手机">{{ detail.userPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="统一社会信用代码">{{ detail.licenseNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="法人姓名">{{ detail.legalName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="法人身份证号">{{ detail.legalIdNo || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.merchantId" label="商户ID">{{ detail.merchantId }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.submittedAt || '—' }}</el-descriptions-item>
            <el-descriptions-item label="审核时间">{{ detail.auditedAt || '—' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.auditRemark" label="审核备注">{{ detail.auditRemark }}</el-descriptions-item>
          </el-descriptions>

          <section class="cert-section">
            <h4>证照资料</h4>
            <div class="cert-grid">
              <div v-if="detail.licenseImage" class="cert-item">
                <p>营业执照</p>
                <el-image
                  :src="detail.licenseImage"
                  :preview-src-list="allPreviewUrls"
                  fit="cover"
                  class="cert-image"
                  preview-teleported
                />
              </div>
              <div v-if="detail.legalIdFront" class="cert-item">
                <p>身份证正面</p>
                <el-image
                  :src="detail.legalIdFront"
                  :preview-src-list="allPreviewUrls"
                  fit="cover"
                  class="cert-image"
                  preview-teleported
                />
              </div>
              <div v-if="detail.legalIdBack" class="cert-item">
                <p>身份证反面</p>
                <el-image
                  :src="detail.legalIdBack"
                  :preview-src-list="allPreviewUrls"
                  fit="cover"
                  class="cert-image"
                  preview-teleported
                />
              </div>
              <div v-for="item in qualImageList" :key="item.key" class="cert-item">
                <p>{{ item.label }}</p>
                <el-image
                  :src="item.url"
                  :preview-src-list="allPreviewUrls"
                  fit="cover"
                  class="cert-image"
                  preview-teleported
                />
              </div>
            </div>
            <el-empty v-if="!detail.licenseImage && !detail.legalIdFront && !detail.legalIdBack && !qualImageList.length" description="暂无证照图片" />
          </section>

          <div v-if="canAudit" class="drawer-actions">
            <el-text v-if="detail.auditStatus === 2" type="info" size="small">该申请已驳回，可重新审核</el-text>
            <el-button type="success" :loading="auditLoading" @click="handleApprove()">
              {{ detail.auditStatus === 2 ? '重新通过' : '通过' }}
            </el-button>
            <el-button type="danger" :loading="auditLoading" @click="handleReject()">
              {{ detail.auditStatus === 2 ? '更新驳回原因' : '驳回' }}
            </el-button>
          </div>
        </template>
      </div>
    </el-drawer>
  </el-card>
</template>

<style scoped lang="scss">
.audit-tabs {
  margin-bottom: 16px;
}

.detail-body {
  min-height: 200px;
}

.cert-section {
  margin-top: 20px;

  h4 {
    margin: 0 0 12px;
    font-size: 15px;
    font-weight: 600;
  }
}

.cert-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

.cert-item {
  p {
    margin: 0 0 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.cert-image {
  width: 140px;
  height: 140px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
}

.drawer-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
