<template>
	<view class="info-page" :style="pageStyle">
		<view v-if="loading" class="info-loading">
			<u-loading-icon mode="circle" :color="theme.primary" />
		</view>
		<template v-else>
			<view class="info-card ry-card">
				<view class="info-row"><text class="info-row__label">门店名称</text><text class="info-row__value">{{ info.storeName }}</text></view>
				<view class="info-row"><text class="info-row__label">门店类型</text><text class="info-row__value">{{ info.typeLabel }}</text></view>
				<view class="info-row"><text class="info-row__label">门店地址</text><text class="info-row__value">{{ info.storeAddress }}</text></view>
				<view class="info-row"><text class="info-row__label">营业时间</text><text class="info-row__value">{{ info.storeHours }}</text></view>
				<view class="info-row"><text class="info-row__label">经营状态</text><text class="info-row__value info-row__value--ok">{{ info.statusLabel }}</text></view>
			</view>
			<view class="info-card ry-card">
				<view class="info-row"><text class="info-row__label">联系人</text><text class="info-row__value">{{ info.contactName }}</text></view>
				<view class="info-row"><text class="info-row__label">联系方式</text><text class="info-row__value">{{ maskPhone(info.contactPhone) }}</text></view>
			</view>
			<view class="info-card ry-card">
				<view class="info-row"><text class="info-row__label">统一社会信用代码</text><text class="info-row__value">{{ info.licenseNo }}</text></view>
				<view class="info-row"><text class="info-row__label">法人姓名</text><text class="info-row__value">{{ info.legalName }}</text></view>
				<view class="info-row info-row--last"><text class="info-row__label">商户编号</text><text class="info-row__value">{{ info.merchantId }}</text></view>
			</view>
			<view v-if="info.storeType === 'restaurant'" class="info-tip ry-card">
				<text>本店为餐饮预约商户，仅提供到店预约服务，不支持在线购买下单。</text>
			</view>
		</template>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { MERCHANT_CENTER_UI, MERCHANT_BUSINESS_UI, enrichMerchantProfile, maskPhone, requireMerchantApproved } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { cacheMerchantStoreType } from '@/common/merchant-theme.js';
import { fetchMerchantProfile } from '@/common/api/merchant.js';

const { theme, pageStyle, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();
const info = ref({});
const loading = ref(false);

async function reload() {
	loading.value = true;
	try {
		const res = await fetchMerchantProfile();
		if (res.code === 0 && res.data) {
			info.value = enrichMerchantProfile(res.data) || res.data;
			if (res.data.storeType) {
				applyStoreType(res.data.storeType);
				cacheMerchantStoreType(res.data.storeType);
			}
		} else {
			uni.showToast({ title: res.msg || MERCHANT_BUSINESS_UI.loadFail, icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: MERCHANT_BUSINESS_UI.networkError, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

onLoad(async () => {
	await initTheme();
	uni.setNavigationBarTitle({ title: MERCHANT_CENTER_UI.infoTitle });
	applyNavBarColor();
});
onShow(async () => {
	if (!(await requireMerchantApproved())) return;
	await initTheme();
	applyNavBarColor();
	await reload();
});
</script>

<style lang="scss" scoped>
.info-page { min-height: 100vh; background: var(--ry-page-bg); padding: 24rpx var(--ry-page-padding-x); display: flex; flex-direction: column; gap: 20rpx; }
.info-loading { padding: 120rpx 0; display: flex; justify-content: center; }
.info-card { padding: 0 28rpx; }
.info-row { display: flex; gap: 20rpx; padding: 28rpx 0; border-bottom: 1rpx solid #F5F5F5; &--last { border-bottom: none; }
	&__label { width: 200rpx; flex-shrink: 0; font-size: 26rpx; color: var(--ry-text-hint); }
	&__value { flex: 1; font-size: 28rpx; color: var(--ry-text-primary); line-height: 1.5; &--ok { color: #2EAF6A; font-weight: 600; } }
}
.info-tip { padding: 24rpx 28rpx; font-size: 26rpx; color: var(--mc-primary, #FF6600); line-height: 1.6; background: var(--mc-primary-light, #FFF8F3); }
</style>
