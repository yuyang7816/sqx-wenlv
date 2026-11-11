<template>
	<view class="rr-page" :style="pageStyle">
		<!-- 顶栏：分段 Tab -->
		<view class="rr-head">
			<view class="rr-seg">
				<view v-for="tab in tabs" :key="tab.key" class="rr-seg__item"
					:class="{ 'rr-seg__item--active': activeTab === tab.key }"
					:style="activeTab === tab.key ? { background: theme.primary, boxShadow: `0 6rpx 20rpx ${theme.primary}40` } : {}"
					@click="switchTab(tab.key)">
					<text class="rr-seg__text">{{ tab.label }}</text>
					<view v-if="tab.key === 'pending' && pendingCount > 0" class="rr-seg__dot" />
				</view>
			</view>

			<view v-if="activeTab === 'pending' && pendingCount > 0" class="rr-tip">
				<view class="rr-tip__icon">
					<u-icon name="info-circle-fill" :color="theme.primary" :size="14" />
				</view>
				<text class="rr-tip__text">{{ pendingCount }} 条预约待确认，请尽快处理</text>
			</view>
		</view>

		<!-- 列表 -->
		<view v-if="orders.length" class="rr-list">
			<view v-for="item in orders" :key="item.id" class="rr-item" @click="goDetail(item.id)">
				<view class="rr-item__inner">
					<view class="rr-item__top">
						<view class="rr-item__top-left">
							<text class="rr-item__status" :class="`rr-item__status--${statusBadgeClass(item.status)}`">
								{{ item.statusLabel }}
							</text>
							<text v-if="item.orderNo" class="rr-item__no">{{ item.orderNo }}</text>
						</view>
						<text class="rr-item__time">{{ formatListTime(item.createdAt) }}</text>
					</view>

					<view class="rr-item__core">
						<view v-if="item.reservationDate" class="rr-item__date" :style="{ background: theme.primaryLight }">
							<text class="rr-item__date-day" :style="{ color: theme.primary }">{{ formatRestaurantResDay(item.reservationDate) }}</text>
							<text v-if="formatRestaurantArrivalTime(item.reservationTime)" class="rr-item__date-time"
								:style="{ color: theme.primary }">{{ formatRestaurantArrivalTime(item.reservationTime) }}</text>
							<text class="rr-item__date-sub">{{ formatRestaurantResMonthWeek(item.reservationDate) }}</text>
						</view>
						<view v-else class="rr-item__date rr-item__date--placeholder" :style="{ background: theme.primaryLight }">
							<u-icon name="calendar-fill" :color="theme.primary" :size="22" />
							<text class="rr-item__date-sub">待安排</text>
						</view>

						<view class="rr-item__main">
							<view class="rr-item__title-row">
								<text class="rr-item__title">{{ item.mainName }}</text>
								<text v-if="item.guestCount" class="rr-item__guest-tag">{{ item.guestCount }}人</text>
							</view>
							<view v-if="metaRows(item).length" class="rr-item__meta">
								<view v-for="row in metaRows(item)" :key="row.key" class="rr-item__meta-row">
									<view class="rr-item__meta-icon">
										<u-icon :name="row.icon" :color="theme.primary" :size="14" />
									</view>
									<text class="rr-item__meta-text">{{ row.text }}</text>
								</view>
							</view>
							<view v-if="item.userRemark" class="rr-item__remark">
								<text class="rr-item__remark-label">备注</text>
								<text class="rr-item__remark-text">{{ item.userRemark }}</text>
							</view>
						</view>
					</view>

					<view class="rr-item__foot">
						<view v-if="!isFreeBooking(item)" class="rr-item__amount-wrap">
							<text class="rr-item__amount" :style="{ color: theme.primary }">{{ formatMoney(orderAmount(item)) }}</text>
						</view>
						<view v-if="merchantActions(item).length" class="rr-item__actions" @click.stop>
							<view v-for="action in merchantActions(item)" :key="action.key" class="rr-item__btn"
								:style="{ background: theme.primary }" @click="onMerchantAction(action.key, item)">
								<text>{{ action.label }}</text>
							</view>
						</view>
						<view v-else class="rr-item__more">
							<text>查看详情</text>
							<view class="rr-item__more-icon">
								<u-icon name="arrow-right" color="#C0C0C8" :size="14" />
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-if="loading" class="rr-foot-loading">
				<u-loading-icon mode="circle" :color="theme.primary" :size="20" />
			</view>
			<view v-else-if="!hasMore" class="rr-foot-end">
				<text>— 没有更多了 —</text>
			</view>
		</view>

		<!-- 空态 -->
		<view v-else-if="!loading" class="rr-empty">
			<view class="rr-empty__icon" :style="{ background: theme.primaryLight }">
				<u-icon name="file-text-fill" :color="theme.primary" :size="48" />
			</view>
			<text class="rr-empty__title">{{ emptyTitle }}</text>
			<text class="rr-empty__desc">{{ emptyDesc }}</text>
		</view>

		<!-- 首屏加载 -->
		<view v-if="loading && orders.length === 0" class="rr-loading">
			<u-loading-icon mode="circle" :color="theme.primary" />
		</view>
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		onLoad,
		onShow,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		formatMoney,
		maskPhone,
		requireMerchantDepositPaid
	} from '@/common/merchant-center.js';
	import {
		formatRestaurantArrivalTime,
		formatRestaurantResDay,
		formatRestaurantResMonthWeek,
		mapRestaurantMerchantListOrder,
		statusBadgeClass
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

	const PACKAGE_STORE_TYPE = 'restaurant';
	const {
		theme,
		pageStyle,
		centerUi,
		businessUi,
		initTheme,
		applyNavBarColor,
		applyStoreType
	} = useMerchantTheme();
	const PAGE_SIZE = 10;
	const activeTab = ref('all');
	const orders = ref([]);
	const pendingCount = ref(0);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);

	const tabs = computed(() => [{
			key: 'all',
			label: centerUi.value.panelAllOrders || businessUi.value.tabAll
		},
		{
			key: 'pending',
			label: centerUi.value.panelPending || businessUi.value.tabPending
		},
		{
			key: 'processing',
			label: centerUi.value.panelProcessing || businessUi.value.tabProcessing
		},
		{
			key: 'completed',
			label: centerUi.value.panelClosed || businessUi.value.tabCompleted
		}
	]);

	const emptyTitle = computed(() => {
		if (activeTab.value === 'pending') return '暂无待确认预约';
		if (activeTab.value === 'processing') return '暂无待到店预约';
		if (activeTab.value === 'completed') return '暂无已到店预约';
		return businessUi.value.emptyOrders || '暂无预约';
	});

	const emptyDesc = computed(() => {
		if (activeTab.value === 'pending') return '用户提交的预约会出现在这里';
		return '切换其他分类看看';
	});

	async function loadPendingCount() {
		try {
			const res = await fetchMerchantPendingOrderCount();
			if (res.code === 0) pendingCount.value = Number(res.data ?? 0);
		} catch (e) {
			/* ignore */
		}
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
				const records = (res.data.records || []).map(mapRestaurantMerchantListOrder).filter(Boolean);
				orders.value = reset ? records : [...orders.value, ...records];
				const total = Number(res.data.total ?? 0);
				hasMore.value = records.length >= PAGE_SIZE && orders.value.length < total;
			} else {
				if (reset) orders.value = [];
				uni.showToast({
					title: res.msg || businessUi.value.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) orders.value = [];
			uni.showToast({
				title: businessUi.value.networkError,
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
			url: `/merchant-restaurant/order-detail?id=${id}`
		});
	}

	function merchantActions(order) {
		return getMerchantOrderActions(order);
	}

	function metaRows(item) {
		if (!item) return [];
		const rows = [];
		if (item.customerPhone) {
			rows.push({
				key: 'phone',
				icon: 'phone-fill',
				text: maskPhone(item.customerPhone)
			});
		}
		if (item.highlightLine && !item.reservationDate) {
			rows.push({
				key: 'reserve',
				icon: 'calendar-fill',
				text: item.highlightLine.replace(/^预约\s*/, '')
			});
		}
		return rows;
	}

	function orderAmount(item) {
		return Number(item?.amount) || 0;
	}

	function isFreeBooking(item) {
		return orderAmount(item) <= 0;
	}

	function formatListTime(dateTime) {
		if (!dateTime) return '';
		const text = String(dateTime);
		if (text.includes(' ')) {
			const [date, time] = text.split(' ');
			const today = new Date();
			const y = today.getFullYear();
			const m = String(today.getMonth() + 1).padStart(2, '0');
			const d = String(today.getDate()).padStart(2, '0');
			const todayStr = `${y}-${m}-${d}`;
			if (date === todayStr) return `今天 ${time?.slice(0, 5) || ''}`;
			return `${date.slice(5)} ${time?.slice(0, 5) || ''}`;
		}
		return text;
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
			title: businessUi.value.ordersTitle || '预约管理'
		});
		applyNavBarColor();
		if (options?.tab) activeTab.value = options.tab;
	});

	onShow(async () => {
		if (!(await requireMerchantDepositPaid())) return;
		applyStoreType(PACKAGE_STORE_TYPE);
		await initTheme();
		applyNavBarColor();
		uni.setNavigationBarTitle({
			title: businessUi.value.ordersTitle || '预约管理'
		});
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
	.rr-page {
		min-height: 100vh;
		background: #F5F6F8;
		padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
	}

	.rr-head {
		position: sticky;
		top: 0;
		z-index: 20;
		padding: 20rpx var(--ry-page-padding-x) 16rpx;
		background: #F5F6F8;
	}

	.rr-seg {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx;
		border-radius: 20rpx;
		background: #EBECF0;

		&__item {
			position: relative;
			flex: 1;
			height: 68rpx;
			border-radius: 14rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			transition: all 0.22s ease;
		}

		&__item--active .rr-seg__text {
			color: #FFFFFF;
			font-weight: 700;
		}

		&__text {
			font-size: 26rpx;
			color: #6B6B78;
			line-height: 1;
		}

		&__dot {
			position: absolute;
			top: 10rpx;
			right: 18rpx;
			width: 14rpx;
			height: 14rpx;
			border-radius: 50%;
			background: #FF4D4F;
			border: 2rpx solid #FFFFFF;
			box-sizing: border-box;
		}
	}

	.rr-tip {
		display: flex;
		align-items: center;
		gap: 10rpx;
		margin-top: 16rpx;
		padding: 16rpx 20rpx;
		border-radius: 14rpx;
		background: #FFFFFF;

		&__icon {
			width: 28rpx;
			height: 28rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__text {
			font-size: 24rpx;
			color: #4A4A56;
			line-height: 36rpx;
		}
	}

	.rr-list {
		padding: 8rpx var(--ry-page-padding-x) 0;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.rr-item {
		border-radius: 20rpx;
		background: #FFFFFF;
		overflow: hidden;
		box-shadow: 0 8rpx 32rpx rgba(15, 23, 42, 0.05);

		&__inner {
			padding: 24rpx;
		}

		&__top {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 20rpx;
		}

		&__top-left {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__no {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: #B0B0B8;
			line-height: 1.3;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__status {
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

		&__time {
			font-size: 22rpx;
			color: #B0B0B8;
			flex-shrink: 0;
		}

		&__core {
			display: flex;
			align-items: stretch;
			gap: 20rpx;
		}

		&__date {
			width: 132rpx;
			flex-shrink: 0;
			border-radius: 16rpx;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 6rpx;
			padding: 24rpx 8rpx;
			box-sizing: border-box;

			&--placeholder {
				gap: 10rpx;
			}
		}

		&__date-day {
			font-size: 44rpx;
			font-weight: 800;
			line-height: 1;
		}

		&__date-time {
			font-size: 26rpx;
			font-weight: 700;
			line-height: 1.2;
		}

		&__date-sub {
			font-size: 20rpx;
			color: #6B6B78;
			font-weight: 600;
			line-height: 1.3;
			text-align: center;
		}

		&__main {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			justify-content: center;
			gap: 12rpx;
		}

		&__title-row {
			display: flex;
			align-items: center;
			gap: 10rpx;
		}

		&__title {
			flex: 1;
			min-width: 0;
			font-size: 30rpx;
			font-weight: 700;
			color: #1A1A22;
			line-height: 1.45;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__guest-tag {
			flex-shrink: 0;
			padding: 4rpx 12rpx;
			border-radius: 8rpx;
			background: #F2F3F7;
			font-size: 20rpx;
			font-weight: 600;
			color: #6B6B78;
			line-height: 1.4;
		}

		&__meta {
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__meta-row {
			display: flex;
			align-items: center;
			gap: 10rpx;
			min-height: 36rpx;
		}

		&__meta-icon {
			width: 28rpx;
			height: 28rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__meta-text {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			color: #6B6B78;
			line-height: 36rpx;
		}

		&__remark {
			display: flex;
			align-items: flex-start;
			gap: 8rpx;
			padding: 10rpx 14rpx;
			border-radius: 10rpx;
			background: #FAFAFC;
		}

		&__remark-label {
			flex-shrink: 0;
			font-size: 20rpx;
			font-weight: 600;
			color: #B0B0B8;
			line-height: 32rpx;
		}

		&__remark-text {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: #6B6B78;
			line-height: 32rpx;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
		}

		&__foot {
			margin-top: 20rpx;
			padding-top: 20rpx;
			border-top: 1rpx solid #F0F1F5;
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__amount-wrap {
			flex-shrink: 0;
		}

		&__amount {
			font-size: 28rpx;
			font-weight: 700;
			line-height: 1.2;
		}

		&__actions {
			flex-shrink: 0;
			margin-left: auto;
		}

		&__btn {
			min-width: 168rpx;
			height: 64rpx;
			padding: 0 28rpx;
			border-radius: 999rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			box-sizing: border-box;

			text {
				font-size: 26rpx;
				font-weight: 600;
				color: #FFFFFF;
				line-height: 1;
			}

			&:active {
				opacity: 0.88;
			}
		}

		&__more {
			flex-shrink: 0;
			margin-left: auto;
			display: flex;
			align-items: center;
			gap: 4rpx;

			text {
				font-size: 24rpx;
				color: #B0B0B8;
				line-height: 36rpx;
			}
		}

		&__more-icon {
			width: 28rpx;
			height: 28rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}
	}

	.rr-foot-loading,
	.rr-foot-end {
		padding: 24rpx 0 8rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.rr-foot-end text {
		font-size: 22rpx;
		color: #C8C8D0;
	}

	.rr-empty {
		padding: 160rpx 48rpx 80rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;

		&__icon {
			width: 128rpx;
			height: 128rpx;
			border-radius: 50%;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 8rpx;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 600;
			color: #1A1A22;
		}

		&__desc {
			font-size: 24rpx;
			color: #B0B0B8;
		}
	}

	.rr-loading {
		padding: 200rpx 0;
		display: flex;
		justify-content: center;
	}
</style>
