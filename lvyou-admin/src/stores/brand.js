import { defineStore } from 'pinia'
import { fetchAppConfigInfoApi } from '@/api/modules/app-config'
import {
  DEFAULT_BRAND_NAME,
  buildAdminSuffix,
  buildAdminTitle,
  buildLogoText,
} from '@/constants/brand'

export const useBrandStore = defineStore('brand', {
  state: () => ({
    productName: DEFAULT_BRAND_NAME,
    loaded: false,
  }),
  getters: {
    brandName: (state) => state.productName,
    adminTitle: (state) => buildAdminTitle(state.productName),
    adminSuffix: (state) => buildAdminSuffix(state.productName),
    logoText: (state) => buildLogoText(state.productName),
  },
  actions: {
    setProductName(name) {
      const next = String(name || '').trim()
      if (next) this.productName = next
    },
    async load() {
      try {
        const data = await fetchAppConfigInfoApi()
        if (data?.productName) {
          this.setProductName(data.productName)
        }
      } catch {
        // 保留本地默认值，登录页仍可展示
      } finally {
        this.loaded = true
      }
    },
  },
})
