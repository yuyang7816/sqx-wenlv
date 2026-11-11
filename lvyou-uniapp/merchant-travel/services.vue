<template>
	<view class="ms-page" :style="pageStyle">
		<view class="ms-header">
			<view class="ms-tabs">
				<view
					v-for="tab in tabs"
					:key="tab.key"
					class="ms-tabs__item"
					:class="{ 'ms-tabs__item--active': activeTab === tab.key }"
					@click="switchTab(tab.key)"
				>
					<text>{{ tab.label }}</text>
				</view>
			</view>
		</view>

		<view v-if="services.length" class="ms-list">
			<view v-for="item in services" :key="item.id" class="ms-item ry-card">
				<view class="ms-item__left">
					<image :src="item.cover" mode="aspectFill" class="ms-item__cover" />
				</view>
				<view class="ms-item__main">
					<view class="ms-item__head">
						<text class="ms-item__name">{{ item.name }}</text>
						<text class="ms-item__status" :class="'ms-item__status--' + item.status">{{ item.statusLabel }}</text>
					</view>
					<text class="ms-item__desc">{{ item.desc }}</text>
					<text class="ms-item__price">{{ Number(item.price) > 0 ? formatMoney(item.price) : '免费预约' }}</text>
					<view class="ms-item__actions">
						<view v-if="item.status !== 'on_sale'" class="ms-item__btn ms-item__btn--danger" @click="onDelete(item)">
							<text>{{ ui.delete }}</text>
						</view>
						<view v-if="item.status === 'off_sale' || item.status === 'draft'" class="ms-item__btn ms-item__btn--publish" @click="onPublish(item)">
							<text>{{ ui.publish }}</text>
						</view>
						<view v-if="item.status === 'on_sale'" class="ms-item__btn ms-item__btn--unpublish" @click="onUnpublish(item)">
							<text>{{ ui.unpublish }}</text>
						</view>
						<view class="ms-item__btn ms-item__btn--edit" @click="goEdit(item.id)">
							<text>{{ ui.edit }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
		<view v-else-if="!loading" class="ms-empty">
			<u-empty mode="list" :text="ui.emptyServices" />
			<u-button :text="ui.publishService" :color="theme.primary" :custom-style="emptyBtnStyle" @click="goPublish" />
		</view>
		<view v-if="loading" class="ms-loading">
			<u-loading-icon mode="circle" :color="theme.primary" />
		</view>

		<view class="ms-fab" @click="goPublish">
			<u-icon name="plus" color="#FFF" :size="24" />
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import {
	SERVICE_STATUS,
	formatMoney,
	requireMerchantDepositPaid
} from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import {
	deleteMerchantService,
	fetchMerchantServices,
	publishMerchantService
} from '@/common/api/merchant.js';

const PACKAGE_STORE_TYPE = 'travel';
const { theme, pageStyle, businessUi, initTheme, applyNavBarColor , applyStoreType } = useMerchantTheme();
const ui = businessUi;
const activeTab = ref('all');
const services = ref([]);
const loading = ref(false);
const emptyBtnStyle = { marginTop: '32rpx', borderRadius: '24px', padding: '0 48rpx' };

const tabs = computed(() => [
	{ key: 'all', label: ui.value.tabAll },
	{ key: SERVICE_STATUS.ON_SALE, label: ui.value.tabOnSale },
	{ key: SERVICE_STATUS.OFF_SALE, label: ui.value.tabOffSale },
	{ key: SERVICE_STATUS.DRAFT, label: ui.value.tabDraft }
]);

function mapService(item) {
	return {
		...item,
		price: Number(item.price ?? 0),
		cover: item.cover || PLACEHOLDER_LOGO,
		desc: item.desc || ''
	};
}

async function reload() {
	loading.value = true;
	try {
		const tab = activeTab.value === 'all' ? 'all' : activeTab.value;
		const res = await fetchMerchantServices(tab);
		if (res.code === 0) {
			services.value = (res.data || []).map(mapService);
		} else {
			services.value = [];
			uni.showToast({ title: res.msg || ui.value.loadFail, icon: 'none' });
		}
	} catch (e) {
		services.value = [];
		uni.showToast({ title: ui.value.networkError, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function switchTab(key) {
	if (activeTab.value === key) return;
	activeTab.value = key;
	reload();
}

function goPublish() {
	uni.navigateTo({ url: '/merchant-travel/service-edit' });
}

function goEdit(id) {
	uni.navigateTo({ url: `/merchant-travel/service-edit?id=${id}` });
}

async function onPublish(item) {
	try {
		const res = await publishMerchantService(item.id, SERVICE_STATUS.ON_SALE);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '操作失败', icon: 'none' });
			return;
		}
		uni.showToast({ title: ui.value.publishSuccess, icon: 'success' });
		await reload();
	} catch (e) {
		uni.showToast({ title: ui.value.networkError, icon: 'none' });
	}
}

function onUnpublish(item) {
	uni.showModal({
		title: '确认下架',
		content: `确定下架「${item.name}」？`,
		success: async (res) => {
			if (!res.confirm) return;
			try {
				const apiRes = await publishMerchantService(item.id, SERVICE_STATUS.OFF_SALE);
				if (apiRes.code !== 0) {
					uni.showToast({ title: apiRes.msg || '操作失败', icon: 'none' });
					return;
				}
				uni.showToast({ title: ui.value.unpublishSuccess, icon: 'success' });
				await reload();
			} catch (e) {
				uni.showToast({ title: ui.value.networkError, icon: 'none' });
			}
		}
	});
}

function onDelete(item) {
	uni.showModal({
		title: '确认删除',
		content: ui.value.confirmDelete,
		success: async (res) => {
			if (!res.confirm) return;
			try {
				const apiRes = await deleteMerchantService(item.id);
				if (apiRes.code !== 0) {
					uni.showToast({ title: apiRes.msg || '操作失败', icon: 'none' });
					return;
				}
				uni.showToast({ title: ui.value.deleteSuccess, icon: 'success' });
				await reload();
			} catch (e) {
				uni.showToast({ title: ui.value.networkError, icon: 'none' });
			}
		}
	});
}

onLoad(async () => {
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.value.servicesTitle });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantDepositPaid())) return;
	applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.value.servicesTitle });
	applyNavBarColor();
	await reload();
});
</script>

<style lang="scss" scoped>
.ms-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.ms-header {
	padding: 16rpx var(--ry-page-padding-x);
	background: #FFF;
	position: sticky;
	top: 0;
	z-index: 10;
}

.ms-tabs {
	display: flex;
	gap: 12rpx;

	&__item {
		flex: 1;
		padding: 14rpx 8rpx;
		border-radius: 999rpx;
		text-align: center;
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		background: #F3F2F0;

		&--active {
			background: var(--mc-primary-light, #E8F8EF);
			color: var(--mc-primary, #2EAF6A);
			font-weight: 600;
		}
	}
}

.ms-list {
	padding: 16rpx var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.ms-item {
	display: flex;
	gap: 20rpx;
	padding: 24rpx;

	&__left {
		width: 140rpx;
		flex-shrink: 0;
		display: flex;
		flex-direction: column;
		align-items: stretch;
	}

	&__cover {
		width: 140rpx;
		height: 140rpx;
		border-radius: 12rpx;
		flex-shrink: 0;
		background: #F5F5F5;
	}

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	&__head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 12rpx;
	}

	&__name {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		word-break: break-all;
	}

	&__status {
		flex-shrink: 0;
		font-size: 20rpx;
		padding: 4rpx 12rpx;
		border-radius: 999rpx;

		&--on_sale { background: #E8F8EF; color: #2EAF6A; }
		&--off_sale { background: #F3F2F0; color: var(--ry-text-hint); }
		&--draft { background: #FFF8E6; color: #FFB800; }
	}

	&__desc {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__price {
		font-size: 28rpx;
		font-weight: 700;
		color: #FF4D4F;
	}

	&__actions {
		display: flex;
		flex-wrap: wrap;
		justify-content: flex-end;
		gap: 12rpx;
		margin-top: auto;
		padding-top: 12rpx;
	}

	&__btn {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 10rpx 24rpx;
		border-radius: 999rpx;
		font-size: 26rpx;
		font-weight: 400;
		line-height: 1;

		&:active { opacity: 0.88; }

		&--edit {
			background: var(--mc-primary-light, #FFF3EB);
			color: var(--mc-primary, #FF6600);
		}

		&--unpublish {
			background: #FFF8E6;
			color: #D48806;
		}

		&--publish {
			background: #E8F8EF;
			color: #2EAF6A;
		}

		&--danger {
			background: #FFF0F0;
			color: #FF4D4F;
		}
	}
}

.ms-fab {
	position: fixed;
	right: 32rpx;
	bottom: calc(32rpx + env(safe-area-inset-bottom));
	width: 96rpx;
	height: 96rpx;
	border-radius: 50%;
	background: var(--mc-primary, #2EAF6A);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
	z-index: 20;

	&:active { opacity: 0.88; }
}

.ms-empty {
	padding: 80rpx var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	align-items: center;
}

.ms-loading {
	padding: 80rpx 0;
	display: flex;
	justify-content: center;
}
</style>
