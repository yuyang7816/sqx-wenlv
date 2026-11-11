/**
 * 商户中心主题 composable：按 storeType 提供主题色与业务文案
 */
import { computed, ref } from 'vue';
import { fetchMerchantProfile } from '@/common/api/merchant.js';
import {
	buildMerchantThemeStyle,
	cacheMerchantStoreType,
	getCachedMerchantStoreType,
	getMerchantStoreTheme,
	resolveMerchantBusinessUi,
	resolveMerchantCenterUi
} from '@/common/merchant-theme.js';
import { MERCHANT_BUSINESS_UI, MERCHANT_CENTER_UI } from '@/common/merchant-center.js';

export function useMerchantTheme() {
	const storeType = ref(getCachedMerchantStoreType());

	const theme = computed(() => getMerchantStoreTheme(storeType.value));
	const pageStyle = computed(() => buildMerchantThemeStyle(storeType.value));
	const centerUi = computed(() => resolveMerchantCenterUi(storeType.value, MERCHANT_CENTER_UI));
	const businessUi = computed(() => resolveMerchantBusinessUi(storeType.value, MERCHANT_BUSINESS_UI));

	function applyStoreType(type) {
		const normalized = type ? String(type).trim() : '';
		if (!normalized) return;
		storeType.value = normalized;
		cacheMerchantStoreType(normalized);
	}

	function applyNavBarColor() {
		try {
			uni.setNavigationBarColor({
				frontColor: '#ffffff',
				backgroundColor: theme.value.primary
			});
		} catch (e) {
			// H5/部分端可能不支持
		}
	}

	async function initTheme(force = false) {
		if (!force && storeType.value) {
			return theme.value;
		}
		const cached = getCachedMerchantStoreType();
		if (!force && cached) {
			storeType.value = cached;
			return theme.value;
		}
		try {
			const res = await fetchMerchantProfile();
			if (res.code === 0 && res.data?.storeType) {
				applyStoreType(res.data.storeType);
			}
		} catch (e) {
			// 使用缓存或默认
		}
		return theme.value;
	}

	return {
		storeType,
		theme,
		pageStyle,
		centerUi,
		businessUi,
		applyStoreType,
		initTheme,
		applyNavBarColor
	};
}
