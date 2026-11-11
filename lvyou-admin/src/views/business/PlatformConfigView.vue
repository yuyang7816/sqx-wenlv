<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import ImageUpload from '@/components/common/ImageUpload.vue'
import RichTextEditor from '@/components/common/RichTextEditor.vue'
import {
  batchUpdatePlatformConfig,
  indexConfigByKey,
  loadPlatformConfigGroups,
} from '@/api/modules/platform-config'
import { useBrandStore } from '@/stores/brand'
import { BRAND_CONFIG_KEY } from '@/api/modules/platform-config'
import { isAuth } from '@/utils/tool'
import { showError, showSuccess } from '@/utils/modal'

const brandStore = useBrandStore()
const activeTab = ref('brand')
const loading = ref(false)
const saving = ref(false)

/** paramKey → 原始配置实体（含 id，用于提交 update） */
const configMap = reactive({})

const canUpdate = computed(() => isAuth('sys:config:update'))

/** 品牌表单（对齐 C 端 GET /app/config/info → productName） */
const brandForm = reactive({
  productName: '',
})

/** 客服表单（对齐 C 端 GET /app/config/info → kefu） */
const kefuForm = reactive({
  phone: '',
  wechat: '',
  wechatQrCode: '',
  weworkUrl: '',
  weworkCorpId: '',
  type: 'wework',
  wechatMpEnabled: '1',
})

/** 协议表单（对齐 GET /app/agreement/{key}） */
const agreementForm = reactive({
  user: '',
  privacy: '',
  about: '',
})

/** 邀请表单（对齐 C 端 invite 配置与佣金比例） */
const inviteForm = reactive({
  posterImage: '',
  copyText: '',
  rebateLevel1: '',
  rebateLevel2: '',
  shareLink: '',
  commissionRateLevel1: '',
  commissionRateLevel2: '',
})

/** 提现规则（用户/商户共用） */
const withdrawForm = reactive({
  minAmount: '',
  maxAmount: '',
  feeRate: '',
})

/** 实时公交（apizero） */
const transportForm = reactive({
  enabled: '0',
  apiKey: '',
})

/** 订单结算 / 平台抽成 */
const orderSettlementForm = reactive({
  commissionRateHotel: '',
  commissionRateRoute: '',
})

/** 线路退改规则 */
const routeRefundForm = reactive({
  ratioBeforeTrip: '',
  ratioOnTripDay: '',
  ratioAfterTrip: '',
  ratioNoshow: '',
  meetHoldHour: '',
  ruleSummary: '',
})

/** 物流查询（阿里云快递 API） */
const logisticsForm = reactive({
  expressAppCode: '',
  expressApiUrl: '',
})

/** 微信支付（运行时键 + 兼容键，对齐 aiPhoto_server） */
const payForm = reactive({
  wechatMchId: '',
  wechatMchKey: '',
  wxKeyPath: '',
  jsapiAppId: '',
  h5AppId: '',
  maAppId: '',
  h5SysAppId: '',
})

const KEFU_TYPE_OPTIONS = [
  { label: '电话咨询', value: 'phone' },
  { label: '微信客服', value: 'wechat' },
  { label: '企业微信', value: 'wework' },
  { label: '在线客服', value: 'im' },
]

const BRAND_FIELDS = [
  { key: BRAND_CONFIG_KEY, formKey: 'productName', label: '产品名称', paramType: 'sys', paramTypeDesc: '系统配置' },
]

const KEFU_PARAM_META = { paramType: 'kefu', paramTypeDesc: '客服配置' }

const KEFU_FIELDS = [
  { key: 'sys_kefu_phone', formKey: 'phone', label: '客服电话', ...KEFU_PARAM_META },
  { key: 'sys_kefu_wechat', formKey: 'wechat', label: '客服微信号', ...KEFU_PARAM_META },
  { key: 'sys_kefu_wechat_qrcode', formKey: 'wechatQrCode', label: '客服微信二维码', ...KEFU_PARAM_META },
  { key: 'sys_kefu_wework_url', formKey: 'weworkUrl', label: '企业微信客服链接', ...KEFU_PARAM_META },
  { key: 'sys_kefu_wework_corp_id', formKey: 'weworkCorpId', label: '企业微信 corpId', ...KEFU_PARAM_META },
  { key: 'sys_kefu_type', formKey: 'type', label: '默认客服类型', ...KEFU_PARAM_META },
  { key: 'sys_wechat_mp_enabled', formKey: 'wechatMpEnabled', label: '启用微信公众号支付', ...KEFU_PARAM_META },
]

const AGREEMENT_PARAM_META = { paramType: 'agreement', paramTypeDesc: '协议配置' }
const INVITE_PARAM_META = { paramType: 'invite', paramTypeDesc: '邀请配置' }

const AGREEMENT_FIELDS = [
  { key: 'agreement_user', formKey: 'user', label: '用户服务协议', ...AGREEMENT_PARAM_META },
  { key: 'agreement_privacy', formKey: 'privacy', label: '隐私政策', ...AGREEMENT_PARAM_META },
  { key: 'agreement_about', formKey: 'about', label: '关于我们', ...AGREEMENT_PARAM_META },
]

const WITHDRAW_PARAM_META = { paramType: 'wallet', paramTypeDesc: '提现配置' }

const WITHDRAW_FIELDS = [
  { key: 'sys_withdraw_min', formKey: 'minAmount', label: '最低提现金额', ...WITHDRAW_PARAM_META },
  { key: 'sys_withdraw_max', formKey: 'maxAmount', label: '最高提现金额', ...WITHDRAW_PARAM_META },
  { key: 'sys_withdraw_rate', formKey: 'feeRate', label: '手续费比例', ...WITHDRAW_PARAM_META },
]

const TRANSPORT_PARAM_META = { paramType: 'transport', paramTypeDesc: '公共交通配置' }

const TRANSPORT_FIELDS = [
  { key: 'sys_bus_realtime_enabled', formKey: 'enabled', label: '启用实时公交', ...TRANSPORT_PARAM_META },
  { key: 'sys_bus_realtime_api_key', formKey: 'apiKey', label: 'apizero API Key', ...TRANSPORT_PARAM_META },
]

const ORDER_SETTLEMENT_PARAM_META = { paramType: 'order', paramTypeDesc: '订单结算配置' }

const ORDER_SETTLEMENT_FIELDS = [
  { key: 'sys_platform_commission_rate_hotel', formKey: 'commissionRateHotel', label: '酒店订单平台抽成比例', ...ORDER_SETTLEMENT_PARAM_META },
  { key: 'sys_platform_commission_rate_route', formKey: 'commissionRateRoute', label: '线路订单平台抽成比例', ...ORDER_SETTLEMENT_PARAM_META },
]

const ROUTE_REFUND_PARAM_META = { paramType: 'route_order', paramTypeDesc: '线路退改规则' }

const ROUTE_REFUND_FIELDS = [
  { key: 'sys_route_refund_ratio_before_trip', formKey: 'ratioBeforeTrip', label: '出行日前取消扣款比例', ...ROUTE_REFUND_PARAM_META },
  { key: 'sys_route_refund_ratio_on_trip_day', formKey: 'ratioOnTripDay', label: '出行日当天取消扣款比例', ...ROUTE_REFUND_PARAM_META },
  { key: 'sys_route_refund_ratio_after_trip', formKey: 'ratioAfterTrip', label: '出行日已过扣款比例', ...ROUTE_REFUND_PARAM_META },
  { key: 'sys_route_refund_ratio_noshow', formKey: 'ratioNoshow', label: 'No-show 扣款比例', ...ROUTE_REFUND_PARAM_META },
  { key: 'sys_route_meet_hold_hour', formKey: 'meetHoldHour', label: '集合保留截止小时', ...ROUTE_REFUND_PARAM_META },
  { key: 'sys_route_refund_rule_summary', formKey: 'ruleSummary', label: 'C 端退改规则摘要', ...ROUTE_REFUND_PARAM_META },
]

const LOGISTICS_PARAM_META = { paramType: 'logistics', paramTypeDesc: '物流配置' }

const LOGISTICS_FIELDS = [
  { key: 'lvyou_aliyun_express_appcode', formKey: 'expressAppCode', label: '阿里云快递 AppCode', ...LOGISTICS_PARAM_META },
  { key: 'lvyou_aliyun_express_api_url', formKey: 'expressApiUrl', label: '阿里云快递接口地址', ...LOGISTICS_PARAM_META },
]

const PAY_PARAM_META = { paramType: 'pay', paramTypeDesc: '支付配置' }

const PAY_FIELDS = [
  { key: 'pay_wechat_mch_id', formKey: 'wechatMchId', label: '微信支付商户号', ...PAY_PARAM_META },
  { key: 'pay_wechat_mch_key', formKey: 'wechatMchKey', label: '微信支付商户秘钥', ...PAY_PARAM_META },
  { key: 'sys_wx_key_path', formKey: 'wxKeyPath', label: '微信支付 p12 证书路径', ...PAY_PARAM_META },
  { key: 'pay_wx_jsapi_app_id', formKey: 'jsapiAppId', label: '小程序支付 AppId', ...PAY_PARAM_META },
  { key: 'pay_wx_h5_app_id', formKey: 'h5AppId', label: 'H5/公众号支付 AppId', ...PAY_PARAM_META },
  { key: 'sys_wechat_jsapi_appid', formKey: 'maAppId', label: '小程序 AppId（登录/支付）', paramType: 'sys', paramTypeDesc: '系统配置' },
  { key: 'sys_wechat_h5_appid', formKey: 'h5SysAppId', label: 'H5 AppId（支付 SDK）', paramType: 'sys', paramTypeDesc: '系统配置' },
]

const INVITE_FIELDS = [
  { key: 'invite_poster_image', formKey: 'posterImage', label: '邀请海报', ...INVITE_PARAM_META },
  { key: 'invite_copy_text', formKey: 'copyText', label: '邀请复制文案', ...INVITE_PARAM_META },
  { key: 'invite_rebate_level1', formKey: 'rebateLevel1', label: '一级返佣说明', ...INVITE_PARAM_META },
  { key: 'invite_rebate_level2', formKey: 'rebateLevel2', label: '二级返佣说明', ...INVITE_PARAM_META },
  { key: 'invite_share_link', formKey: 'shareLink', label: '邀请分享链接', ...INVITE_PARAM_META },
  { key: 'invite_commission_rate_level1', formKey: 'commissionRateLevel1', label: '一级佣金比例', ...INVITE_PARAM_META },
  { key: 'invite_commission_rate_level2', formKey: 'commissionRateLevel2', label: '二级佣金比例', ...INVITE_PARAM_META },
]

function applyGroupToForm(groupItems, fieldDefs, formTarget) {
  const indexed = indexConfigByKey(groupItems)
  fieldDefs.forEach(({ key, formKey }) => {
    const entity = indexed[key]
    if (entity) {
      configMap[key] = entity
      formTarget[formKey] = entity.paramValue || ''
    }
  })
}

async function loadData() {
  loading.value = true
  try {
    const groups = await loadPlatformConfigGroups()
    applyGroupToForm(groups.sys, BRAND_FIELDS, brandForm)
    applyGroupToForm(groups.kefu, KEFU_FIELDS, kefuForm)
    if (!kefuForm.type) kefuForm.type = 'phone'
    if (!kefuForm.wechatMpEnabled) kefuForm.wechatMpEnabled = '1'
    applyGroupToForm(groups.agreement, AGREEMENT_FIELDS, agreementForm)
    applyGroupToForm(groups.invite, INVITE_FIELDS, inviteForm)
    applyGroupToForm(groups.wallet, WITHDRAW_FIELDS, withdrawForm)
    applyGroupToForm(groups.transport, TRANSPORT_FIELDS, transportForm)
    if (!transportForm.enabled) transportForm.enabled = '0'
    applyGroupToForm(groups.order, ORDER_SETTLEMENT_FIELDS, orderSettlementForm)
    applyGroupToForm(groups.route_order, ROUTE_REFUND_FIELDS, routeRefundForm)
    applyGroupToForm(groups.logistics, LOGISTICS_FIELDS, logisticsForm)
    applyGroupToForm(groups.pay, PAY_FIELDS, payForm)
    if (!logisticsForm.expressApiUrl) {
      logisticsForm.expressApiUrl = 'https://wuliu.market.alicloudapi.com/kdi'
    }
  } catch (error) {
    showError(error?.msg || error?.message || '加载平台配置失败')
  } finally {
    loading.value = false
  }
}

function buildUpdates(fieldDefs, formTarget) {
  const updates = []
  const creates = []
  fieldDefs.forEach(({ key, formKey, label, paramType, paramTypeDesc }) => {
    const entity = configMap[key]
    const nextValue = formTarget[formKey] ?? ''
    if (entity?.id) {
      if (String(entity.paramValue || '') !== String(nextValue)) {
        updates.push({
          ...entity,
          paramValue: nextValue,
        })
      }
      return
    }
    if (String(nextValue).trim()) {
      creates.push({
        paramKey: key,
        paramValue: nextValue,
        status: '1',
        remark: label || key,
        paramType: paramType || 'kefu',
        paramTypeDesc: paramTypeDesc || '客服配置',
      })
    }
  })
  return { updates, creates }
}

async function handleSaveBrand() {
  if (!canUpdate.value) return
  const name = String(brandForm.productName || '').trim()
  if (!name) {
    showError('请输入产品名称')
    return
  }
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(BRAND_FIELDS, brandForm)
    if (!updates.length && !creates.length) {
      showSuccess('品牌配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    brandStore.setProductName(name)
    showSuccess('品牌配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存品牌配置失败')
  } finally {
    saving.value = false
  }
}

function clearInactiveKefuFields() {
  const type = kefuForm.type || 'phone'
  if (type !== 'phone') kefuForm.phone = ''
  if (type !== 'wechat') {
    kefuForm.wechat = ''
    kefuForm.wechatQrCode = ''
  }
  if (type !== 'wework') {
    kefuForm.weworkUrl = ''
    kefuForm.weworkCorpId = ''
  }
}

function validateKefuForm() {
  const type = kefuForm.type || 'phone'
  if (type === 'phone' && !String(kefuForm.phone || '').trim()) {
    showError('请填写客服电话')
    return false
  }
  if (type === 'wechat') {
    if (!String(kefuForm.wechat || '').trim()) {
      showError('请填写客服微信号')
      return false
    }
    if (!String(kefuForm.wechatQrCode || '').trim()) {
      showError('请上传客服微信二维码')
      return false
    }
  }
  if (type === 'wework' && !String(kefuForm.weworkUrl || '').trim()) {
    showError('请填写企业微信客服链接')
    return false
  }
  return true
}

watch(
  () => kefuForm.type,
  () => {
    clearInactiveKefuFields()
  },
)

async function handleSaveKefu() {
  if (!canUpdate.value) return
  if (!validateKefuForm()) return
  clearInactiveKefuFields()
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(KEFU_FIELDS, kefuForm)
    if (!updates.length && !creates.length) {
      showSuccess('客服配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('客服配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存客服配置失败')
  } finally {
    saving.value = false
  }
}

async function handleSaveAgreement() {
  if (!canUpdate.value) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(AGREEMENT_FIELDS, agreementForm)
    if (!updates.length && !creates.length) {
      showSuccess('协议配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('协议配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存协议配置失败')
  } finally {
    saving.value = false
  }
}

async function handleSaveInvite() {
  if (!canUpdate.value) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(INVITE_FIELDS, inviteForm)
    if (!updates.length && !creates.length) {
      showSuccess('邀请配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('邀请配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存邀请配置失败')
  } finally {
    saving.value = false
  }
}

function validateWithdrawForm() {
  const min = Number(withdrawForm.minAmount)
  const max = Number(withdrawForm.maxAmount)
  const rate = Number(withdrawForm.feeRate)
  if (!Number.isFinite(min) || min <= 0) {
    showError('请填写有效的最低提现金额')
    return false
  }
  if (!Number.isFinite(max) || max <= 0) {
    showError('请填写有效的最高提现金额')
    return false
  }
  if (min > max) {
    showError('最低提现金额不能大于最高提现金额')
    return false
  }
  if (!Number.isFinite(rate) || rate < 0 || rate > 100) {
    showError('手续费比例需在 0-100 之间')
    return false
  }
  return true
}

async function handleSaveWithdraw() {
  if (!canUpdate.value) return
  if (!validateWithdrawForm()) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(WITHDRAW_FIELDS, withdrawForm)
    if (!updates.length && !creates.length) {
      showSuccess('提现配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('提现配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存提现配置失败')
  } finally {
    saving.value = false
  }
}

function validateTransportForm() {
  if (transportForm.enabled === '1' && !String(transportForm.apiKey || '').trim()) {
    showError('启用实时公交时请填写 apizero API Key')
    return false
  }
  return true
}

async function handleSaveTransport() {
  if (!canUpdate.value) return
  if (!validateTransportForm()) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(TRANSPORT_FIELDS, transportForm)
    if (!updates.length && !creates.length) {
      showSuccess('公共交通配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('公共交通配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存公共交通配置失败')
  } finally {
    saving.value = false
  }
}

function validateRateField(value, label, { allowEmpty = false } = {}) {
  const text = String(value ?? '').trim()
  if (!text) return allowEmpty ? true : (showError(`请填写${label}`), false)
  const num = Number(text)
  if (!Number.isInteger(num) || num < 0 || num > 100) {
    showError(`${label}需为 0-100 的整数`)
    return false
  }
  return true
}

function validateOrderSettlementForm() {
  return validateRateField(orderSettlementForm.commissionRateHotel, '酒店平台抽成比例')
    && validateRateField(orderSettlementForm.commissionRateRoute, '线路平台抽成比例')
}

function validateRouteRefundForm() {
  if (!validateRateField(routeRefundForm.ratioBeforeTrip, '出行日前扣款比例')) return false
  if (!validateRateField(routeRefundForm.ratioOnTripDay, '出行日当天扣款比例')) return false
  if (!validateRateField(routeRefundForm.ratioAfterTrip, '出行日已过扣款比例')) return false
  if (!validateRateField(routeRefundForm.ratioNoshow, 'No-show 扣款比例', { allowEmpty: true })) return false
  const hourText = String(routeRefundForm.meetHoldHour ?? '').trim()
  if (hourText) {
    const hour = Number(hourText)
    if (!Number.isInteger(hour) || hour < 0 || hour > 23) {
      showError('集合保留截止小时需为 0-23 的整数')
      return false
    }
  }
  return true
}

async function handleSaveOrderSettlement() {
  if (!canUpdate.value) return
  if (!validateOrderSettlementForm()) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(ORDER_SETTLEMENT_FIELDS, orderSettlementForm)
    if (!updates.length && !creates.length) {
      showSuccess('订单结算配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('订单结算配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存订单结算配置失败')
  } finally {
    saving.value = false
  }
}

async function handleSaveRouteRefund() {
  if (!canUpdate.value) return
  if (!validateRouteRefundForm()) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(ROUTE_REFUND_FIELDS, routeRefundForm)
    if (!updates.length && !creates.length) {
      showSuccess('线路退改配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('线路退改配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存线路退改配置失败')
  } finally {
    saving.value = false
  }
}

async function handleSaveLogistics() {
  if (!canUpdate.value) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(LOGISTICS_FIELDS, logisticsForm)
    if (!updates.length && !creates.length) {
      showSuccess('物流配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('物流配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存物流配置失败')
  } finally {
    saving.value = false
  }
}

async function handleSavePay() {
  if (!canUpdate.value) return
  saving.value = true
  try {
    const { updates, creates } = buildUpdates(PAY_FIELDS, payForm)
    const mirrorFields = [
      { key: 'pay_wx_mch_id', value: payForm.wechatMchId, label: '微信支付商户号（兼容键）', ...PAY_PARAM_META },
      { key: 'pay_wx_mch_key', value: payForm.wechatMchKey, label: '微信支付商户秘钥（兼容键）', ...PAY_PARAM_META },
      { key: 'pay_wx_key_path', value: payForm.wxKeyPath, label: '微信支付 p12 证书路径（兼容键）', ...PAY_PARAM_META },
    ]
    mirrorFields.forEach(({ key, value, label, paramType, paramTypeDesc }) => {
      const entity = configMap[key]
      const nextValue = value ?? ''
      if (entity?.id) {
        if (String(entity.paramValue || '') !== String(nextValue)) {
          updates.push({ ...entity, paramValue: nextValue })
        }
        return
      }
      if (String(nextValue).trim()) {
        creates.push({
          paramKey: key,
          paramValue: nextValue,
          status: '1',
          remark: label,
          paramType,
          paramTypeDesc,
        })
      }
    })
    if (!updates.length && !creates.length) {
      showSuccess('支付配置无变更')
      return
    }
    await batchUpdatePlatformConfig(updates, creates)
    showSuccess('支付配置已保存')
    await loadData()
  } catch (error) {
    showError(error?.msg || error?.message || '保存支付配置失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <el-card v-loading="loading" shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <div>
          <strong>平台配置</strong>
          <p class="card-desc">维护产品名称、客服、协议、物流、微信支付、邀请分销、实时公交与提现规则</p>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="品牌配置" name="brand">
        <el-form label-width="140px" class="config-form">
          <el-form-item label="产品名称">
            <el-input
              v-model="brandForm.productName"
              placeholder="用户端与管理端展示名称，如：省钱兄文旅"
              maxlength="32"
              show-word-limit
              clearable
            />
            <p class="field-tip">亦用于短信/邮件签名；修改后用户端与管理端将同步展示新名称</p>
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveBrand">保存品牌配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="客服配置" name="kefu">
        <el-form label-width="140px" class="config-form">
          <el-form-item label="客服形式">
            <el-radio-group v-model="kefuForm.type">
              <el-radio v-for="item in KEFU_TYPE_OPTIONS" :key="item.value" :value="item.value">
                {{ item.label }}
              </el-radio>
            </el-radio-group>
            <p class="field-tip">仅启用一种人工客服方式，C 端按所选方式展示对应入口</p>
          </el-form-item>
          <el-form-item v-if="kefuForm.type === 'phone'" label="客服电话">
            <el-input v-model="kefuForm.phone" placeholder="如 023-46789012" clearable />
          </el-form-item>
          <template v-if="kefuForm.type === 'wechat'">
            <el-form-item label="客服微信号">
              <el-input v-model="kefuForm.wechat" placeholder="微信号，C 端可复制添加" clearable />
            </el-form-item>
            <el-form-item label="微信二维码">
              <ImageUpload v-model="kefuForm.wechatQrCode" />
              <p class="field-tip">上传客服微信二维码，C 端展示供用户扫码添加</p>
            </el-form-item>
          </template>
          <el-form-item v-if="kefuForm.type === 'im'">
            <p class="field-tip">启用后 C 端进入平台内在线 IM 会话页（/pages/my/im-chat），无需额外配置</p>
          </el-form-item>
          <template v-if="kefuForm.type === 'wework'">
            <el-form-item label="企业微信链接">
              <el-input v-model="kefuForm.weworkUrl" placeholder="企业微信客服 URL" clearable />
            </el-form-item>
            <el-form-item label="企业微信 corpId">
              <el-input v-model="kefuForm.weworkCorpId" placeholder="小程序 openCustomerServiceChat 用" clearable />
            </el-form-item>
          </template>
          <el-divider content-position="left">微信公众号</el-divider>
          <el-form-item label="启用公众号支付">
            <el-switch
              v-model="kefuForm.wechatMpEnabled"
              active-value="1"
              inactive-value="0"
            />
            <p class="field-tip">开启后，H5 端在微信内置浏览器内且用户通过公众号登录时，预订/商品支付可展示微信支付；关闭后即使在微信内也不展示</p>
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveKefu">保存客服配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="协议配置" name="agreement">
        <el-form label-width="140px" class="config-form">
          <el-form-item label="用户服务协议">
            <RichTextEditor v-model="agreementForm.user" placeholder="用户服务协议 HTML 正文" min-height="220px" />
          </el-form-item>
          <el-form-item label="隐私政策">
            <RichTextEditor v-model="agreementForm.privacy" placeholder="隐私政策 HTML 正文" min-height="220px" />
          </el-form-item>
          <el-form-item label="关于我们">
            <RichTextEditor v-model="agreementForm.about" placeholder="关于我们 HTML 正文" min-height="220px" />
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveAgreement">保存协议配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="提现配置" name="withdraw">
        <el-form label-width="140px" class="config-form">
          <el-form-item label="最低提现金额">
            <el-input v-model="withdrawForm.minAmount" placeholder="如 10" clearable>
              <template #append>元</template>
            </el-input>
            <p class="field-tip">用户与商户提现共用，单笔不得低于该金额</p>
          </el-form-item>
          <el-form-item label="最高提现金额">
            <el-input v-model="withdrawForm.maxAmount" placeholder="如 50000" clearable>
              <template #append>元</template>
            </el-input>
            <p class="field-tip">用户与商户提现共用，单笔不得超过该金额</p>
          </el-form-item>
          <el-form-item label="手续费比例">
            <el-input v-model="withdrawForm.feeRate" placeholder="如 1 表示 1%" clearable>
              <template #append>%</template>
            </el-input>
            <p class="field-tip">按提现金额计算手续费，实际到账 = 提现金额 - 手续费</p>
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveWithdraw">保存提现配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="公共交通" name="transport">
        <el-form label-width="160px" class="config-form">
          <el-form-item label="启用实时公交">
            <el-switch
              v-model="transportForm.enabled"
              active-value="1"
              inactive-value="0"
            />
            <p class="field-tip">开启后 C 端公共交通页走 apizero 实时到站；关闭则回退数据库静态线路</p>
          </el-form-item>
          <el-form-item label="apizero API Key">
            <el-input
              v-model="transportForm.apiKey"
              type="password"
              show-password
              placeholder="sk_test_... 或 sk_live_..."
              clearable
            />
            <p class="field-tip">在 apizero.cn 获取，仅服务端代理调用，不会暴露给前端</p>
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveTransport">保存公共交通配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="订单结算" name="orderSettlement">
        <el-form label-width="180px" class="config-form">
          <el-form-item label="酒店平台抽成比例">
            <el-input v-model="orderSettlementForm.commissionRateHotel" placeholder="0 表示不抽成" clearable>
              <template #append>%</template>
            </el-input>
            <p class="field-tip">核销完成时从商户应得基数扣除；0 不抽，1-100 按比例抽成</p>
          </el-form-item>
          <el-form-item label="线路平台抽成比例">
            <el-input v-model="orderSettlementForm.commissionRateRoute" placeholder="0 表示不抽成" clearable>
              <template #append>%</template>
            </el-input>
            <p class="field-tip">线路核销完成及退款扣留结算时使用；与酒店配置互不影响</p>
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveOrderSettlement">保存订单结算配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="线路退改" name="routeRefund">
        <el-form label-width="200px" class="config-form">
          <el-form-item label="出行日前取消扣款比例">
            <el-input v-model="routeRefundForm.ratioBeforeTrip" placeholder="0 表示全额退用户" clearable>
              <template #append>%</template>
            </el-input>
            <p class="field-tip">从实付中扣留的比例，余款退用户；0 表示不扣款、全额退款</p>
          </el-form-item>
          <el-form-item label="出行日当天取消扣款比例">
            <el-input v-model="routeRefundForm.ratioOnTripDay" placeholder="如 2 表示扣除实付 2%" clearable>
              <template #append>%</template>
            </el-input>
            <p class="field-tip">扣留部分结算给商户，平台再从扣留金额中抽成</p>
          </el-form-item>
          <el-form-item label="出行日已过扣款比例">
            <el-input v-model="routeRefundForm.ratioAfterTrip" placeholder="如 30 表示扣除实付 30%" clearable>
              <template #append>%</template>
            </el-input>
          </el-form-item>
          <el-form-item label="No-show 扣款比例">
            <el-input v-model="routeRefundForm.ratioNoshow" placeholder="留空则沿用出行日已过比例" clearable>
              <template #append>%</template>
            </el-input>
          </el-form-item>
          <el-form-item label="集合保留截止小时">
            <el-input v-model="routeRefundForm.meetHoldHour" placeholder="默认 10，即出行日 10:00" clearable>
              <template #append>时</template>
            </el-input>
          </el-form-item>
          <el-form-item label="C 端退改规则摘要">
            <el-input v-model="routeRefundForm.ruleSummary" type="textarea" :rows="5" placeholder="展示在预约确认页与订单详情" />
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveRouteRefund">保存线路退改配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="物流配置" name="logistics">
        <el-form label-width="180px" class="config-form">
          <el-form-item label="阿里云快递 AppCode">
            <el-input
              v-model="logisticsForm.expressAppCode"
              type="password"
              show-password
              placeholder="阿里云市场快递查询 AppCode"
              clearable
            />
            <p class="field-tip">在阿里云云市场购买「全国快递物流查询」后，于买家中心获取 AppCode；未配置时仅展示运单号</p>
          </el-form-item>
          <el-form-item label="阿里云快递接口地址">
            <el-input
              v-model="logisticsForm.expressApiUrl"
              placeholder="https://wuliu.market.alicloudapi.com/kdi"
              clearable
            />
            <p class="field-tip">阿里云云市场快递查询接口，默认 https://wuliu.market.alicloudapi.com/kdi，一般无需修改</p>
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveLogistics">保存物流配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="微信支付" name="pay">
        <el-form label-width="200px" class="config-form">
          <el-form-item label="微信支付商户号">
            <el-input v-model="payForm.wechatMchId" placeholder="如 1564357541" clearable />
            <p class="field-tip">运行时读取键：pay_wechat_mch_id（保存时同步写入 pay_wx_mch_id）</p>
          </el-form-item>
          <el-form-item label="微信支付商户秘钥">
            <el-input v-model="payForm.wechatMchKey" type="password" show-password placeholder="32 位 API 密钥" clearable />
            <p class="field-tip">运行时读取键：pay_wechat_mch_key</p>
          </el-form-item>
          <el-form-item label="p12 证书路径">
            <el-input v-model="payForm.wxKeyPath" placeholder="服务端可访问的 apiclient_cert.p12 绝对路径" clearable />
            <p class="field-tip">运行时读取键：sys_wx_key_path；需将证书文件部署到该路径</p>
          </el-form-item>
          <el-divider content-position="left">AppId 配置</el-divider>
          <el-form-item label="小程序 AppId（登录/支付）">
            <el-input v-model="payForm.maAppId" placeholder="sys_wechat_jsapi_appid" clearable />
          </el-form-item>
          <el-form-item label="小程序支付 AppId">
            <el-input v-model="payForm.jsapiAppId" placeholder="pay_wx_jsapi_app_id" clearable />
          </el-form-item>
          <el-form-item label="H5/公众号支付 AppId">
            <el-input v-model="payForm.h5AppId" placeholder="pay_wx_h5_app_id" clearable />
          </el-form-item>
          <el-form-item label="H5 AppId（支付 SDK）">
            <el-input v-model="payForm.h5SysAppId" placeholder="sys_wechat_h5_appid" clearable />
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSavePay">保存支付配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="邀请配置" name="invite">
        <el-form label-width="140px" class="config-form">
          <el-form-item label="邀请海报">
            <ImageUpload v-model="inviteForm.posterImage" />
            <p class="field-tip">点击上传海报图片，禁止手填外链 URL</p>
          </el-form-item>
          <el-form-item label="邀请复制文案">
            <el-input
              v-model="inviteForm.copyText"
              type="textarea"
              :rows="3"
              placeholder="支持 {code}、{link} 占位符；未写 {link} 时 C 端会自动追加注册链接"
            />
          </el-form-item>
          <el-form-item label="一级返佣说明">
            <el-input v-model="inviteForm.rebateLevel1" type="textarea" :rows="2" placeholder="团队页展示文案" />
          </el-form-item>
          <el-form-item label="二级返佣说明">
            <el-input v-model="inviteForm.rebateLevel2" type="textarea" :rows="2" placeholder="团队页展示文案" />
          </el-form-item>
          <el-form-item label="邀请分享链接">
            <el-input v-model="inviteForm.shareLink" placeholder="如 http://127.0.0.1:5588/pages/my/register（勿带邀请码，系统自动追加 ?code=）" clearable />
          </el-form-item>
          <el-form-item label="一级佣金比例">
            <el-input v-model="inviteForm.commissionRateLevel1" placeholder="如 0.05 表示 5%" clearable />
          </el-form-item>
          <el-form-item label="二级佣金比例">
            <el-input v-model="inviteForm.commissionRateLevel2" placeholder="如 0.02 表示 2%" clearable />
          </el-form-item>
          <el-form-item>
            <el-button v-if="canUpdate" type="primary" :loading="saving" @click="handleSaveInvite">保存邀请配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<style scoped>
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-desc {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.config-form {
  max-width: 920px;
  padding-top: 8px;
}

.field-tip {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.55;
}

.card-desc {
  color: var(--el-text-color-secondary);
}

.config-form :deep(.el-radio__label) {
  color: var(--el-text-color-regular);
}

.config-form :deep(.el-radio.is-checked .el-radio__label) {
  color: var(--el-text-color-primary);
}

.config-form :deep(.el-divider__text) {
  color: var(--el-text-color-secondary);
  background: var(--el-bg-color);
}
</style>
