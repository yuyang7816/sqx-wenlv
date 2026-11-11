<template>
	<view class="result-page">
		<view class="result-card ry-card">
			<u-icon :name="iconName" :color="iconColor" :size="64" />
			<text class="result-card__title">{{ title }}</text>
			<text class="result-card__desc">{{ desc }}</text>
			<view v-if="isRejected && profile.auditRemark" class="result-card__reject">
				<text class="result-card__reject-label">{{ ui.rejectedReasonTitle }}</text>
				<text class="result-card__reject-text">{{ profile.auditRemark }}</text>
			</view>
			<view v-if="profile.storeName" class="result-card__info">
				<text>门店：{{ profile.storeName }}</text>
				<text>类型：{{ typeLabel }}</text>
			</view>
		</view>

		<view class="result-actions">
			<u-button v-if="isApproved" :text="ui.goCenter" block color="#FF6600" :custom-style="btnStyle" @click="goCenter" />
			<u-button v-else-if="isRejected" :text="ui.reapply" block color="#FF6600" :custom-style="btnStyle" @click="goApply" />
			<u-button :text="ui.backMy" block plain color="#FF6600" :custom-style="btnStylePlain" @click="goMy" />
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { MERCHANT_UI, STORE_TYPES, MERCHANT_STATUS } from '@/common/merchant-onboarding.js';
import { fetchMerchantStatus, requireMerchantLogin } from '@/common/api/merchant.js';

const ui = MERCHANT_UI;
const profile = ref({ status: MERCHANT_STATUS.NONE });
const loading = ref(false);
const btnStyle = { height: '44px', borderRadius: '22px' };
const btnStylePlain = { height: '44px', borderRadius: '22px', marginTop: '20rpx' };

const status = computed(() => profile.value.status || MERCHANT_STATUS.NONE);
const isApproved = computed(() => status.value === MERCHANT_STATUS.APPROVED);
const isRejected = computed(() => status.value === MERCHANT_STATUS.REJECTED);

const typeLabel = computed(() => {
	if (profile.value.storeTypeLabel) return profile.value.storeTypeLabel;
	return STORE_TYPES.find((t) => t.key === profile.value.storeType)?.label || '';
});

const title = computed(() => {
	if (isApproved.value) return '入驻成功';
	if (isRejected.value) return '审核未通过';
	return '审核中';
});

const desc = computed(() => {
	if (isApproved.value) return ui.approvedDesc;
	if (isRejected.value) return ui.rejectedDesc;
	return ui.pendingDesc;
});

const iconName = computed(() => {
	if (isApproved.value) return 'checkmark-circle-fill';
	if (isRejected.value) return 'close-circle-fill';
	return 'clock-fill';
});

const iconColor = computed(() => {
	if (isApproved.value) return '#2EAF6A';
	if (isRejected.value) return '#FF4D4F';
	return '#FFB800';
});

async function reload() {
	if (loading.value) return;
	loading.value = true;
	try {
		const res = await fetchMerchantStatus();
		if (res.code === 0 && res.data) {
			profile.value = {
				status: res.data.status || MERCHANT_STATUS.NONE,
				storeName: res.data.storeName || '',
				storeType: res.data.storeType || '',
				storeTypeLabel: res.data.storeTypeLabel || '',
				auditRemark: res.data.auditRemark || ''
			};
			return;
		}
		profile.value = { status: MERCHANT_STATUS.NONE };
	} catch (e) {
		profile.value = { status: MERCHANT_STATUS.NONE };
	} finally {
		loading.value = false;
	}
}

function goCenter() {
	uni.redirectTo({ url: '/merchant-portal/center/index' });
}

function goApply() {
	uni.redirectTo({ url: '/merchant-portal/onboarding/apply' });
}

function goMy() {
	uni.switchTab({ url: '/pages/my/my' });
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.resultTitle });
});

onShow(async () => {
	if (!requireMerchantLogin()) return;
	await reload();
	if (status.value === MERCHANT_STATUS.NONE) {
		uni.redirectTo({ url: '/merchant-portal/onboarding/apply' });
	}
});
</script>

<style lang="scss" scoped>
.result-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding: 48rpx var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	gap: 40rpx;
}

.result-card {
	padding: 60rpx 32rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20rpx;
	text-align: center;

	&__title {
		font-size: 36rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__desc {
		font-size: 26rpx;
		color: var(--ry-text-secondary);
		line-height: 1.6;
	}

	&__reject {
		width: 100%;
		margin-top: 8rpx;
		padding: 20rpx 24rpx;
		border-radius: 16rpx;
		background: rgba(255, 77, 79, 0.06);
		text-align: left;
	}

	&__reject-label {
		display: block;
		font-size: 22rpx;
		color: #FF7875;
		margin-bottom: 8rpx;
	}

	&__reject-text {
		display: block;
		font-size: 26rpx;
		color: #CF1322;
		line-height: 1.6;
		word-break: break-all;
	}

	&__info {
		margin-top: 12rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #F5F5F5;
		width: 100%;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}
}

.result-actions {
	display: flex;
	flex-direction: column;
}
</style>
