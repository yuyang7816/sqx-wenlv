<template>
	<view class="mo-page" :style="pageStyle">
		<view class="mo-tabs-wrap">
			<scroll-view class="mo-tabs" scroll-x :show-scrollbar="false">
				<view class="mo-tabs__track">
					<view v-for="tab in tabs" :key="tab.key" class="mo-tabs__item"
						:class="{ 'mo-tabs__item--active': activeTab === tab.key }" @click="switchTab(tab.key)">
						<view class="mo-tabs__label-wrap">
							<text class="mo-tabs__label">{{ tab.label }}</text>
							<view v-if="activeTab === tab.key" class="mo-tabs__line" :style="{ background: theme.primary }" />
						</view>
						<view v-if="tab.key === 'pending' && pendingCount > 0" class="mo-tabs__badge">
							<text>{{ pendingCount > 99 ? '99+' : pendingCount }}</text>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>

		<view v-if="orders.length" class="mo-list">
			<view v-for="item in orders" :key="item.id" class="mo-card ry-card" @click="goDetail(item.id)">
				<view class="mo-card__head">
					<view class="mo-card__head-left">
						<text class="mo-card__type">{{ item.orderTypeLabel }}</text>
						<text class="mo-card__no">{{ item.orderNo }}</text>
					</view>
					<text class="mo-card__status" :class="`mo-card__status--${statusBadgeClass(item.status)}`">
						{{ item.statusLabel }}
					</text>
				</view>

				<view class="mo-card__body">
					<image :src="item.cover" mode="aspectFill" class="mo-card__cover ry-placeholder-img" />
					<view class="mo-card__info">
						<view class="mo-card__title-row">
							<text class="mo-card__title">{{ item.mainName }}</text>
							<text v-if="item.titleTag" class="mo-card__tag">{{ item.titleTag }}</text>
						</view>
						<!-- <text v-if="item.subSpec && activeTab !== 'completed'" class="mo-card__spec">{{ item.subSpec }}</text> -->
						<text v-if="item.highlightLine" class="mo-card__highlight">{{ item.highlightLine }}</text>
						<text v-if="item.extraLine" class="mo-card__extra">{{ item.extraLine }}</text>
					</view>
				</view>

				<view class="mo-card__foot">
					<text class="mo-card__time">{{ item.createdAt }}</text>
					<view class="mo-card__foot-right">
						<text class="mo-card__amount" :class="{ 'mo-card__amount--free': Number(item.amount) <= 0 }">
							{{ formatDisplayAmount(item) }}
						</text>
						<view v-if="merchantActions(item).length" class="mo-card__actions" @click.stop>
							<view v-for="action in merchantActions(item)" :key="action.key" class="mo-card__action"
								:class="action.tone === 'primary' ? 'mo-card__action--primary' : 'mo-card__action--outline'"
								:style="action.tone === 'primary' ? { background: theme.primary } : { borderColor: theme.primary, color: theme.primary }"
								@click="onMerchantAction(action.key, item)">
								<text>{{ action.label }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<view v-else-if="!loading" class="mo-empty">
			<u-icon name="file-text" color="#D8D8DE" :size="56" />
			<text class="mo-empty__text">{{ ui.emptyOrders }}</text>
		</view>

		<view v-if="loading && orders.length === 0" class="mo-loading">
			<u-loading-icon mode="circle" :color="theme.primary" />
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		onLoad,
		onShow,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		MERCHANT_BUSINESS_UI,
		requireMerchantDepositPaid
	} from '@/common/merchant-center.js';
	import {
		mapMerchantOrder,
		statusBadgeClass,
		formatDisplayAmount
	} from '@/common/merchant-order-ui.js';
	import {
		getMerchantOrderActions,
		runMerchantOrderAction
	} from '@/common/order-actions.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		fetchMerchantOrders,
		fetchMerchantPendingOrderCount
	} from '@/common/api/merchant.js';

	const PACKAGE_STORE_TYPE = 'hotel';
const {
		theme,
		pageStyle,
		initTheme,
		applyNavBarColor,
		applyStoreType
	} = useMerchantTheme();
	const ui = MERCHANT_BUSINESS_UI;
	const PAGE_SIZE = 10;
	const activeTab = ref('all');
	const orders = ref([]);
	const pendingCount = ref(0);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);

	const tabs = [{
			key: 'all',
			label: ui.tabAll
		},
		{
			key: 'pending',
			label: ui.tabPending
		},
		{
			key: 'processing',
			label: ui.tabProcessing
		},
		{
			key: 'completed',
			label: ui.tabCompleted
		}
	];

	async function loadPendingCount() {
		try {
			const res = await fetchMerchantPendingOrderCount();
			if (res.code === 0) pendingCount.value = Number(res.data ?? 0);
		} catch (e) {
			/* ignore */ }
	}

	async function loadOrders(reset = false) {
		if (loading.value) return;
		if (!reset && !hasMore.value) return;
		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}
		loading.value = true;
		try {
			const res = await fetchMerchantOrders({
				tab: activeTab.value,
				page: page.value,
				limit: PAGE_SIZE
			});
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapMerchantOrder).filter(Boolean);
				orders.value = reset ? records : [...orders.value, ...records];
				const total = Number(res.data.total ?? 0);
				hasMore.value = records.length >= PAGE_SIZE && orders.value.length < total;
			} else {
				if (reset) orders.value = [];
				uni.showToast({
					title: res.msg || ui.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) orders.value = [];
			uni.showToast({
				title: ui.networkError,
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	async function reload(reset = true) {
		await Promise.all([loadOrders(reset), loadPendingCount()]);
	}

	function switchTab(key) {
		if (activeTab.value === key) return;
		activeTab.value = key;
		reload(true);
	}

	function goDetail(id) {
		uni.navigateTo({
			url: `/merchant-hotel/order-detail?id=${id}`
		});
	}

	function merchantActions(order) {
		return getMerchantOrderActions(order);
	}

	async function onMerchantAction(actionKey, order) {
		await runMerchantOrderAction(actionKey, order, {
			onChanged: () => reload(true)
		});
	}

	onLoad(async (options) => {
		applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
		uni.setNavigationBarTitle({
			title: ui.ordersTitle
		});
		applyNavBarColor();
		if (options?.tab) activeTab.value = options.tab;
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		applyStoreType(PACKAGE_STORE_TYPE);
	await initTheme();
		applyNavBarColor();
		await reload(true);
	});

	onPullDownRefresh(async () => {
		await reload(true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(async () => {
		if (!hasMore.value || loading.value) return;
		page.value += 1;
		await loadOrders(false);
	});
</script>

<style lang="scss" scoped>
	.mo-page {
		min-height: 100vh;
		background: #F4F6FA;
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
	}

	.mo-tabs-wrap {
		padding: 16rpx var(--ry-page-padding-x) 0;
		position: sticky;
		top: 0;
		z-index: 10;
		background: #F4F6FA;
	}

	.mo-tabs {
		background: #FFFFFF;
		border-radius: 20rpx;
		box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.06);
		white-space: nowrap;

		&__track {
			display: inline-flex;
			padding: 8rpx 12rpx;
			gap: 4rpx;
		}

		&__item {
			position: relative;
			flex-shrink: 0;
			display: flex;
			align-items: flex-start;
			justify-content: center;
			gap: 6rpx;
			padding: 20rpx 24rpx 24rpx;
		}

		&__label-wrap {
			position: relative;
			display: flex;
			flex-direction: column;
			align-items: center;
			padding-bottom: 10rpx;
		}

		&__label {
			font-size: 26rpx;
			color: #8A8A96;
			line-height: 1;
		}

		&__item--active .mo-tabs__label {
			font-weight: 700;
			color: #1A1A22;
		}

		&__badge {
			min-width: 32rpx;
			height: 32rpx;
			margin-top: 2rpx;
			padding: 0 8rpx;
			border-radius: 999rpx;
			background: #FF4D4F;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;

			text {
				font-size: 18rpx;
				font-weight: 600;
				color: #FFF;
			}
		}

		&__line {
			position: absolute;
			left: 50%;
			bottom: 0;
			width: 36rpx;
			height: 6rpx;
			border-radius: 999rpx;
			transform: translateX(-50%);
		}
	}

	.mo-list {
		padding: 20rpx var(--ry-page-padding-x) 0;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.mo-card {
		padding: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 18rpx;

		&__head {
			display: flex;
			align-items: flex-start;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__head-left {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__type {
			font-size: 24rpx;
			font-weight: 600;
			color: #6B6B78;
			line-height: 1.3;
		}

		&__no {
			font-size: 22rpx;
			color: #B0B0B8;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__status {
			flex-shrink: 0;
			padding: 6rpx 16rpx;
			border-radius: 999rpx;
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1.2;

			&--is-pending {
				background: #FFF3EB;
				color: #FF6600;
			}

			&--is-ship {
				background: #EAF2FF;
				color: #2979FF;
			}

			&--is-done {
				background: #E8F8EF;
				color: #2EAF6A;
			}

			&--is-cancel {
				background: #FFF0F0;
				color: #FF4D4F;
			}

			&--is-default {
				background: #F2F3F7;
				color: #8A8A96;
			}
		}

		&__body {
			display: flex;
			align-items: flex-start;
			gap: 20rpx;
		}

		&__cover {
			width: 128rpx;
			height: 128rpx;
			border-radius: 16rpx;
			flex-shrink: 0;
			background: #EEE;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__title-row {
			display: flex;
			align-items: flex-start;
			gap: 10rpx;
		}

		&__title {
			flex: 1;
			min-width: 0;
			font-size: 28rpx;
			font-weight: 700;
			color: #1A1A22;
			line-height: 1.45;
		}

		&__tag {
			flex-shrink: 0;
			padding: 2rpx 10rpx;
			border-radius: 8rpx;
			background: #F2F3F7;
			font-size: 20rpx;
			color: #8A8A96;
			line-height: 1.6;
		}

		&__spec {
			font-size: 24rpx;
			color: #9A9AA6;
			line-height: 1.4;
		}

		&__highlight {
			font-size: 24rpx;
			color: #2979FF;
			font-weight: 600;
			line-height: 1.4;
		}

		&__extra {
			font-size: 22rpx;
			color: #8A8A96;
			line-height: 1.45;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__foot {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			padding-top: 16rpx;
			border-top: 1rpx solid #F0F1F5;
		}

		&__time {
			flex-shrink: 0;
			font-size: 22rpx;
			color: #B0B0B8;
		}

		&__foot-right {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 16rpx;
		}

		&__amount {
			font-size: 30rpx;
			font-weight: 800;
			color: #FF6600;
			line-height: 1;

			&--free {
				font-size: 24rpx;
				font-weight: 600;
				color: #FF6600;
			}
		}

		&__actions {
			display: flex;
			gap: 12rpx;
		}

		&__action {
			min-width: 140rpx;
			height: 56rpx;
			padding: 0 24rpx;
			border-radius: 999rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			box-sizing: border-box;

			text {
				font-size: 24rpx;
				font-weight: 600;
				line-height: 1;
			}

			&--primary text {
				color: #FFF;
			}

			&--outline {
				background: #FFF;
				border: 2rpx solid;
			}
		}
	}

	.mo-empty,
	.mo-loading {
		padding: 120rpx 48rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
	}

	.mo-empty__text {
		font-size: 28rpx;
		color: #8A8A96;
	}
</style>
