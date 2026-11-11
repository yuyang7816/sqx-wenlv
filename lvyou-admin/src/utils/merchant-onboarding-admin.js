/**
 * 管理端商户入驻表单（与 C 端 common/merchant-onboarding.js 对齐）
 */

export const QUALIFICATIONS_BY_TYPE = {
  hotel: {
    title: '酒店入驻资质',
    required: [
      { key: 'storeFront', label: '门店门头照', desc: '含店招完整外观', required: true },
      { key: 'specialPermit', label: '特种行业许可证', desc: '如有请上传', required: false },
      { key: 'healthPermit', label: '卫生许可证', desc: '如有请上传', required: false },
    ],
  },
  restaurant: {
    title: '餐饮入驻资质',
    note: '餐饮商户仅开放预约服务，不支持在线购买下单',
    required: [
      { key: 'storeFront', label: '门店门头照', desc: '含店招完整外观', required: true },
      { key: 'foodPermit', label: '食品经营许可证', desc: '如有请上传', required: false },
      { key: 'diningEnv', label: '就餐环境照片', desc: '可上传多张，展示就餐区域', required: false, multiple: true, minCount: 0, maxCount: 6 },
    ],
  },
  culture: {
    title: '非遗文创入驻资质',
    required: [
      { key: 'storeFront', label: '门店/网店展示照', desc: '实体门店或线上店铺', required: true },
      { key: 'productSample', label: '产品样品照片', desc: '可上传多张，展示主营产品', required: false, multiple: true, minCount: 0, maxCount: 9 },
    ],
  },
  travel: {
    title: '旅游入驻资质',
    required: [
      { key: 'officePhoto', label: '经营场所照片', desc: '可上传多张，展示门店或办公环境', required: true, multiple: true, minCount: 1, maxCount: 6 },
      { key: 'travelPermit', label: '旅行社经营许可证', desc: '如有请上传', required: false },
    ],
  },
}

const STORE_TYPE_ALIASES = { goods: 'culture' }

export function normalizeStoreType(key) {
  return STORE_TYPE_ALIASES[key] || key
}

export function getQualificationsByType(typeKey) {
  const normalized = normalizeStoreType(typeKey)
  return QUALIFICATIONS_BY_TYPE[normalized] || QUALIFICATIONS_BY_TYPE.hotel
}

export function isQualMultiItem(item) {
  return Boolean(item?.multiple)
}

export function getQualMaxCount(item) {
  if (!item) return 1
  return isQualMultiItem(item) ? Number(item.maxCount || 9) : 1
}

export function getQualMinCount(item) {
  if (!item) return 1
  if (!isQualMultiItem(item)) return item.required ? 1 : 0
  return Number(item.minCount ?? (item.required ? 1 : 0))
}

export function parseQualImageUrls(raw) {
  if (!raw) return []
  if (Array.isArray(raw)) return raw.map((url) => String(url || '').trim()).filter(Boolean)
  const text = String(raw).trim()
  if (!text) return []
  if (text.startsWith('[')) {
    try {
      const parsed = JSON.parse(text)
      return Array.isArray(parsed) ? parsed.map((url) => String(url || '').trim()).filter(Boolean) : [text]
    } catch (e) {
      return [text]
    }
  }
  return [text]
}

export function getQualImageList(qualImages, key) {
  return parseQualImageUrls(qualImages?.[key])
}

export function findQualItem(storeType, key) {
  return getQualificationsByType(storeType).required.find((item) => item.key === key) || null
}

export function isQualItemFilled(qualImages, item) {
  const count = getQualImageList(qualImages, item.key).length
  const minCount = getQualMinCount(item)
  if (item.required) return count >= minCount
  return count >= minCount
}

export function normalizeQualImagesForForm(qualImages, storeType) {
  const result = {}
  Object.entries(qualImages || {}).forEach(([key, value]) => {
    const item = findQualItem(storeType, key)
    const urls = parseQualImageUrls(value).filter(Boolean)
    if (!urls.length) return
    result[key] = item && isQualMultiItem(item) ? urls : urls[0]
  })
  return result
}

export function serializeQualImagesForSubmit(qualImages, storeType) {
  const result = {}
  Object.entries(qualImages || {}).forEach(([key, value]) => {
    const item = findQualItem(storeType, key)
    const urls = parseQualImageUrls(value).filter(Boolean)
    if (!urls.length) return
    if (item && isQualMultiItem(item)) {
      result[key] = JSON.stringify(urls)
    } else {
      result[key] = urls[0]
    }
  })
  return result
}

export function formatStoreAddress(form) {
  const region = String(form?.storeRegion || '').trim()
  const detail = String(form?.storeAddressDetail || '').trim()
  if (region || detail) return `${region}${detail}`
  return String(form?.storeAddress || '').trim()
}

export function getEmptyApplyForm() {
  return {
    userId: null,
    contactName: '',
    contactPhone: '',
    storeName: '',
    storeRegion: '',
    storeAddressDetail: '',
    storeAddress: '',
    latitude: null,
    longitude: null,
    storeHours: '09:00-21:00',
    storeType: '',
    shopCategory: '',
    licenseNo: '',
    licenseImage: '',
    legalName: '',
    legalIdNo: '',
    legalIdFront: '',
    legalIdBack: '',
    qualImages: {},
    status: 1,
    isOpen: 1,
  }
}

export function validateApplyForm(form) {
  if (!form.userId) return { ok: false, message: '请选择关联用户' }
  if (!String(form.contactName || '').trim()) return { ok: false, message: '请填写商户联系人' }
  if (!String(form.contactPhone || '').trim()) return { ok: false, message: '请填写联系方式' }
  if (!String(form.storeName || '').trim()) return { ok: false, message: '请填写门店名称' }
  if (!String(form.storeRegion || '').trim()) return { ok: false, message: '请选择所在地区' }
  if (!String(form.storeAddressDetail || '').trim()) return { ok: false, message: '请填写详细地址' }
  if (form.latitude == null || form.longitude == null) {
    return { ok: false, message: '请在地图上选择门店位置' }
  }
  if (!form.storeType) return { ok: false, message: '请选择门店类型' }
  if (form.storeType === 'culture' && !form.shopCategory) {
    return { ok: false, message: '请选择好店分类' }
  }
  if (!String(form.licenseNo || '').trim()) return { ok: false, message: '请填写统一社会信用代码' }
  if (!form.licenseImage) return { ok: false, message: '请上传营业执照' }
  if (!String(form.legalName || '').trim()) return { ok: false, message: '请填写法人姓名' }
  if (!String(form.legalIdNo || '').trim()) return { ok: false, message: '请填写法人身份证号' }
  if (!form.legalIdFront) return { ok: false, message: '请上传身份证人像面' }
  if (!form.legalIdBack) return { ok: false, message: '请上传身份证国徽面' }

  const quals = getQualificationsByType(form.storeType).required.filter((item) => item.required)
  for (const item of quals) {
    if (!isQualItemFilled(form.qualImages, item)) {
      return { ok: false, message: `请上传${item.label}` }
    }
  }
  return { ok: true }
}
