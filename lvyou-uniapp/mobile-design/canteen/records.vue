<template>
	<view class="cr-page">
		<view class="cr-body">
			<scroll-view class="cr-tabs" scroll-x :show-scrollbar="false">
				<view class="cr-tabs__row">
					<view v-for="(tab, index) in filterTabs" :key="tab.key" class="cr-tab"
						:class="{ 'cr-tab--active': currentFilter === index }" @click="onFilterTap(index)">
						<text class="cr-tab__text">{{ tab.name }}</text>
						<view v-if="tab.count > 0" class="cr-tab__badge">
							<text class="cr-tab__badge-text">{{ tab.count > 99 ? '99+' : tab.count }}</text>
						</view>
					</view>
				</view>
			</scroll-view>

			<view v-if="loading && recordList.length === 0" class="cr-loading">
				<view class="cr-loading__spinner" />
				<text class="cr-loading__text">加载预约记录…</text>
			</view>

			<view v-else-if="!loading && recordList.length === 0" class="cr-empty">
				<view class="cr-empty__circle">
					<u-icon name="calendar" :color="accentColor" :size="44" />
				</view>
				<text class="cr-empty__title">暂无预约记录</text>
				<text class="cr-empty__desc">您还没有餐饮预约，选一家好店开始吧</text>
				<view class="cr-empty__btn" @click="goReserve">
					<u-icon name="plus-circle-fill" color="#FFFFFF" :size="18" />
					<text class="cr-empty__btn-text">立即预约</text>
				</view>
			</view>

			<view v-else class="cr-list">
				<view v-for="record in recordList" :key="record.id" class="cr-card ry-card" @click="onCardTap(record)">
					<view class="cr-card__head">
						<view class="cr-card__status" :class="`cr-card__status--${record.status}`">
							<view class="cr-card__status-dot" />
							<text class="cr-card__status-text">{{ record.statusLabel }}</text>
						</view>
						<text class="cr-card__no">{{ record.reservationNo || record.id }}</text>
					</view>



					<view class="cr-card__shop">
						<view class="cr-card__shop-body">
							<view class="cr-card__shop-cover">
								<image class="cr-card__shop-cover-img ry-placeholder-img" :src="record.shopCover || PLACEHOLDER_LOGO"
									mode="aspectFill" />
							</view>
							<view class="cr-card__shop-main">
								<text class="cr-card__shop-name">{{ getShopName(record) }}</text>
								<text class="cr-card__contact">
									{{ record.contactName }} · {{ record.guestCount }} 人 · {{ record.phone }}
								</text>
								<view v-if="record.address" class="cr-card__shop-addr">
									<view class="cr-card__shop-addr-icon">
										<u-icon name="map" color="#C8C8C8" :size="12" />
									</view>
									<text class="cr-card__shop-addr-text">{{ record.address }}</text>
								</view>
								<view v-if="record.dishName" class="cr-card__dish">
									<u-icon name="bag-fill" :color="accentColor" :size="11" />
									<text class="cr-card__dish-text">预约菜品：{{ record.dishName }}</text>
								</view>
							</view>
						</view>
					</view>
					<view class="cr-card__time" style="margin-top: 20rpx;margin-bottom: 0;">
						<view class="cr-card__time-left">
							<text class="cr-card__date">{{ getReserveDate(record.reserveTime) }}</text>
							<text v-if="getReserveWeekday(record.reserveTime)" class="cr-card__week">
								{{ getReserveWeekday(record.reserveTime) }}
							</text>
						</view>
						<view class="cr-card__time-divider" />
						<view class="cr-card__time-right">
							<text class="cr-card__clock">{{ getReserveClock(record.reserveTime) || '--:--' }}</text>
							<text class="cr-card__guest">{{ record.guestCount }} 人</text>
						</view>
					</view>
					<view v-if="isActionable(record)" class="cr-card__actions" @click.stop>
						<view class="cr-card__btn cr-card__btn--ghost" @click="onCancel(record)">
							<u-icon name="close-circle" :color="accentColor" :size="16" />
							<text class="cr-card__btn-text cr-card__btn-text--ghost">取消</text>
						</view>
						<view class="cr-card__btn cr-card__btn--primary" @click="onModify(record)">
							<u-icon name="edit-pen-fill" color="#FFFFFF" :size="16" />
							<text class="cr-card__btn-text">修改</text>
						</view>
					</view>
				</view>

				<view v-if="loading && recordList.length > 0" class="cr-list-foot">
					<view class="cr-list-foot__spinner" />
					<text class="cr-list-foot__text">加载更多…</text>
				</view>
				<view v-else-if="!hasMore && recordList.length > 0" class="cr-list-foot">
					<text class="cr-list-foot__text cr-list-foot__text--muted">— 已加载全部 —</text>
				</view>
			</view>

			<text class="cr-footer">非遗美食 · 地道卤味</text>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onShow,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO,
		getModuleColor
	} from '@/common/theme.js';
	import {
		hasToken
	} from '@/common/api/auth.js';
	import {
		cancelCanteenReservation,
		fetchCanteenList,
		mapCanteenRecord
	} from '@/common/api/canteen.js';
	import {
		DICT_TYPES,
		loadDictItems,
		resolveStatusFromDictItems
	} from '@/common/dict-util.js';

	const accentColor = getModuleColor('goose');
	const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';
	const CANTEEN_DETAIL_PATH = '/mobile-design/canteen/detail';
	const PAGE_SIZE = 10;
	const filterDictItems = ref([]);

	const currentFilter = ref(0);
	const recordList = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);
	const total = ref(0);

	const pendingCount = computed(() => recordList.value.filter(item => item.status === 'pending' || item.status ===
		'confirmed').length);
	const completedCount = computed(() => recordList.value.filter(item => item.status === 'completed').length);
	const filterTabs = computed(() => {
		if (filterDictItems.value.length) {
			return filterDictItems.value.map((item) => ({
				key: item.code,
				name: item.label,
				count: item.code === 'all' ?
					(total.value || recordList.value.length) : item.code === 'confirmed' ?
					pendingCount.value : item.code === 'completed' ?
					completedCount.value : recordList.value.filter((row) => row.status === item.code).length
			}));
		}
		return [{
				key: 'all',
				name: '全部',
				count: total.value || recordList.value.length
			},
			{
				key: 'confirmed',
				name: '待到店',
				count: pendingCount.value
			},
			{
				key: 'completed',
				name: '已完成',
				count: completedCount.value
			},
			{
				key: 'cancelled',
				name: '已取消',
				count: recordList.value.filter(item => item.status === 'cancelled').length
			}
		];
	});

	function getShopName(record = {}) {
		return record.shopName || record.merchantName || '餐饮门店';
	}

	function getReserveDate(time = '') {
		const parts = String(time).trim().split(' ');
		return parts[0] || time;
	}

	function getReserveClock(time = '') {
		const parts = String(time).trim().split(' ');
		const clock = parts[1] || '';
		return clock ? clock.slice(0, 5) : '';
	}

	function getReserveWeekday(time = '') {
		const dateStr = getReserveDate(time);
		if (!dateStr) return '';
		const d = new Date(`${dateStr}T00:00:00`);
		if (Number.isNaN(d.getTime())) return '';
		const labels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
		return labels[d.getDay()] || '';
	}

	function isActionable(record) {
		return record.status === 'pending' || record.status === 'confirmed';
	}

	function requireLogin() {
		if (hasToken()) return true;
		uni.navigateTo({
			url: '/pages/my/login'
		});
		return false;
	}

	function getStatusParam() {
		const status = resolveStatusFromDictItems(filterDictItems.value, currentFilter.value);
		if (status) return status;
		const tab = filterTabs.value[currentFilter.value];
		if (!tab || tab.key === 'all') return undefined;
		return tab.key;
	}

	async function loadFilterDict() {
		filterDictItems.value = await loadDictItems(DICT_TYPES.RESERVATION_FILTER);
	}

	async function loadRecords(reset = false) {
		if (!hasToken()) {
			recordList.value = [];
			total.value = 0;
			hasMore.value = false;
			return;
		}
		if (loading.value) return;
		if (!reset && !hasMore.value) return;
		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}
		loading.value = true;
		try {
			const res = await fetchCanteenList({
				page: page.value,
				limit: PAGE_SIZE,
				status: getStatusParam()
			});
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapCanteenRecord);
				recordList.value = reset ? records : [...recordList.value, ...records];
				total.value = Number(res.data.total ?? 0);
				hasMore.value = records.length >= PAGE_SIZE && recordList.value.length < total.value;
			} else {
				if (reset) {
					recordList.value = [];
					total.value = 0;
				}
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) {
				recordList.value = [];
				total.value = 0;
			}
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	const onFilterTap = (index) => {
		if (currentFilter.value === index) return;
		currentFilter.value = index;
		loadRecords(true);
	};

	const navigateSafe = (url) => uni.navigateTo({
		url
	});
	const GOOSE_LIST_PATH = '/mobile-design/goose/list';
	const goReserve = () => navigateSafe(GOOSE_LIST_PATH);
	const onCardTap = (record) => navigateSafe(`${CANTEEN_DETAIL_PATH}?id=${encodeURIComponent(record.id)}`);

	const onCancel = (record) => {
		uni.showModal({
			title: '取消预约',
			content: `确定取消 ${record.contactName} 的餐饮预约吗？`,
			confirmColor: accentColor,
			success: async (res) => {
				if (!res.confirm) return;
				try {
					const cancelRes = await cancelCanteenReservation(record.id);
					if (cancelRes.code === 0) {
						uni.showToast({
							title: '预约已取消',
							icon: 'success'
						});
						loadRecords(true);
						return;
					}
					uni.showToast({
						title: cancelRes.msg || '取消失败',
						icon: 'none'
					});
				} catch (e) {
					uni.showToast({
						title: '网络异常，请稍后重试',
						icon: 'none'
					});
				}
			}
		});
	};

	const onModify = (record) => navigateSafe(`${CANTEEN_RESERVE_PATH}?mode=edit&id=${encodeURIComponent(record.id)}`);

	onShow(async () => {
		if (!requireLogin()) return;
		await loadFilterDict();
		loadRecords(true);
		uni.setNavigationBarTitle({
			title: '预约记录'
		});
	});

	onPullDownRefresh(async () => {
		if (!hasToken()) {
			uni.stopPullDownRefresh();
			return;
		}
		await loadRecords(true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(() => {
		if (!hasMore.value || loading.value) return;
		page.value += 1;
		loadRecords(false);
	});
</script>

<style lang="scss" scoped>
	.cr-page {
		min-height: 100vh;
		background-color: #FFF9F2;
	}

	.cr-body {
		padding: 20rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.cr-tabs {
		width: 100%;
		white-space: nowrap;
		padding: 12rpx 16rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 20rpx rgba(255, 122, 0, 0.06);
		box-sizing: border-box;
	}

	.cr-tabs__row {
		display: inline-flex;
		gap: 12rpx;
	}

	.cr-tab {
		flex-shrink: 0;
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		height: 56rpx;
		padding: 0 24rpx;
		border-radius: 999rpx;
		background-color: #FFF9F2;
		box-sizing: border-box;

		&--active {
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			box-shadow: 0 6rpx 18rpx rgba(255, 122, 0, 0.22);

			.cr-tab__text {
				color: #FFFFFF;
				font-weight: 700;
			}

			.cr-tab__badge {
				background-color: rgba(255, 255, 255, 0.28);
			}

			.cr-tab__badge-text {
				color: #FFFFFF;
			}
		}

		&__text {
			font-size: 26rpx;
			font-weight: 500;
			color: #666666;
			line-height: 1;
		}

		&__badge {
			min-width: 32rpx;
			height: 32rpx;
			padding: 0 8rpx;
			border-radius: 999rpx;
			background-color: #FFF3EB;
			display: flex;
			align-items: center;
			justify-content: center;
			box-sizing: border-box;
		}

		&__badge-text {
			font-size: 20rpx;
			font-weight: 700;
			color: #FF7A00;
			line-height: 1;
		}
	}

	.cr-loading {
		padding: 80rpx 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 20rpx;

		&__spinner {
			width: 44rpx;
			height: 44rpx;
			border-radius: 50%;
			border: 4rpx solid #FFE8D6;
			border-top-color: #FF7A00;
			animation: cr-spin 0.8s linear infinite;
		}

		&__text {
			font-size: 26rpx;
			color: #999999;
		}
	}

	@keyframes cr-spin {
		to {
			transform: rotate(360deg);
		}
	}

	.cr-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 72rpx 40rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 20rpx rgba(255, 122, 0, 0.04);

		&__circle {
			width: 140rpx;
			height: 140rpx;
			border-radius: 50%;
			background: linear-gradient(145deg, #FFF6EF, #FFE8D6);
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__title {
			margin-top: 28rpx;
			font-size: 32rpx;
			font-weight: 700;
			color: #333333;
		}

		&__desc {
			margin-top: 12rpx;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.6;
			text-align: center;
		}

		&__btn {
			margin-top: 36rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 80rpx;
			padding: 0 48rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			box-shadow: 0 10rpx 28rpx rgba(255, 122, 0, 0.3);

			&:active {
				opacity: 0.9;
			}
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 700;
			color: #FFFFFF;
		}
	}

	.cr-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.cr-card {
		padding: 24rpx;
		overflow: hidden;

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 16rpx;
		}

		&__status {
			display: inline-flex;
			align-items: center;
			gap: 8rpx;
			height: 40rpx;
			padding: 0 14rpx;
			border-radius: 999rpx;
			box-sizing: border-box;

			&--pending,
			&--confirmed {
				background-color: #FFF6EF;

				.cr-card__status-dot {
					background-color: #FF7A00;
				}

				.cr-card__status-text {
					color: #FF7A00;
				}
			}

			&--completed {
				background-color: #E8F8EF;

				.cr-card__status-dot {
					background-color: #2EAF6A;
				}

				.cr-card__status-text {
					color: #2EAF6A;
				}
			}

			&--cancelled {
				background-color: #F5F5F5;

				.cr-card__status-dot {
					background-color: #BBBBBB;
				}

				.cr-card__status-text {
					color: #999999;
				}
			}
		}

		&__status-dot {
			width: 8rpx;
			height: 8rpx;
			border-radius: 50%;
			flex-shrink: 0;
		}

		&__status-text {
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1;
		}

		&__no {
			flex-shrink: 0;
			font-size: 22rpx;
			color: #CCCCCC;
			font-family: ui-monospace, monospace;
			line-height: 1;
		}

		&__time {
			display: flex;
			align-items: center;
			gap: 20rpx;
			padding: 20rpx;
			margin-bottom: 16rpx;
			border-radius: 16rpx;
			background-color: #FFF9F2;
		}

		&__time-left {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 4rpx;
		}

		&__date {
			font-size: 30rpx;
			font-weight: 800;
			color: #1A1A1A;
			line-height: 1.2;
		}

		&__week {
			font-size: 22rpx;
			font-weight: 600;
			color: #FF7A00;
			line-height: 1.2;
		}

		&__time-divider {
			flex-shrink: 0;
			width: 2rpx;
			height: 56rpx;
			background: linear-gradient(180deg, transparent, #FFD4B8, transparent);
		}

		&__time-right {
			flex-shrink: 0;
			display: flex;
			flex-direction: column;
			align-items: flex-end;
			gap: 4rpx;
		}

		&__clock {
			font-size: 36rpx;
			font-weight: 800;
			color: #FF7A00;
			line-height: 1;
			font-variant-numeric: tabular-nums;
		}

		&__guest {
			font-size: 20rpx;
			color: #BBBBBB;
			line-height: 1;
		}

		&__shop {
			margin-bottom: 4rpx;
		}

		&__shop-body {
			display: flex;
			align-items: flex-start;
			gap: 16rpx;
		}

		&__shop-cover {
			flex-shrink: 0;
			width: 120rpx;
			height: 120rpx;
			border-radius: 14rpx;
			overflow: hidden;
			background-color: #FFF3EB;
		}

		&__shop-cover-img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__shop-main {
			flex: 1;
			min-width: 0;
			min-height: 120rpx;
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			gap: 8rpx;
		}

		&__shop-name {
			font-size: 28rpx;
			font-weight: 700;
			color: #1A1A1A;
			line-height: 1.35;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
		}

		&__contact {
			font-size: 22rpx;
			color: #999999;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__shop-addr {
			display: flex;
			align-items: flex-start;
			gap: 6rpx;
		}

		&__shop-addr-icon {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 24rpx;
			height: 33rpx;
		}

		&__shop-addr-text {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: #999999;
			line-height: 1.5;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
		}

		&__dish {
			display: inline-flex;
			align-items: center;
			align-self: flex-start;
			gap: 4rpx;
			padding: 4rpx 12rpx;
			border-radius: 999rpx;
			background-color: #FFF6EF;
			max-width: 100%;
		}

		&__dish-text {
			font-size: 20rpx;
			color: #E86A00;
			line-height: 1.3;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__actions {
			display: flex;
			gap: 16rpx;
			margin-top: 20rpx;
			padding-top: 16rpx;
		}

		&__btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 72rpx;
			border-radius: 999rpx;
			box-sizing: border-box;

			&--ghost {
				background-color: #FFF9F2;
			}

			&--primary {
				background: linear-gradient(135deg, #FF9A40, #FF7A00);
				box-shadow: 0 6rpx 20rpx rgba(255, 122, 0, 0.22);
			}

			&:active {
				opacity: 0.88;
			}
		}

		&__btn-text {
			font-size: 26rpx;
			font-weight: 600;
			color: #FFFFFF;
			line-height: 1;

			&--ghost {
				color: #FF7A00;
			}
		}
	}

	.cr-list-foot {
		padding: 16rpx 0 8rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 12rpx;

		&__spinner {
			width: 28rpx;
			height: 28rpx;
			border-radius: 50%;
			border: 3rpx solid #FFE8D6;
			border-top-color: #FF7A00;
			animation: cr-spin 0.8s linear infinite;
		}

		&__text {
			font-size: 24rpx;
			color: #999999;

			&--muted {
				color: #CCCCCC;
			}
		}
	}

	.cr-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding-top: 8rpx;
	}
</style>
