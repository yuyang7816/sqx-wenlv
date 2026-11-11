<template>
	<view class="ol-page" :class="`ol-page--${currentCategory}`">
		<view class="ol-head">
			<view class="ol-category">
				<view class="ol-category__item ol-category__item--goods"
					:class="{ 'ol-category__item--active': currentCategory === ORDER_CATEGORY_GOODS }"
					@click="switchCategory(ORDER_CATEGORY_GOODS)">
					<text class="ol-category__text">{{ ui.categoryGoods }}</text>
				</view>
				<view class="ol-category__item ol-category__item--hotel"
					:class="{ 'ol-category__item--active': currentCategory === ORDER_CATEGORY_HOTEL }"
					@click="switchCategory(ORDER_CATEGORY_HOTEL)">
					<text class="ol-category__text">{{ ui.categoryHotel }}</text>
				</view>
				<view class="ol-category__item ol-category__item--route"
					:class="{ 'ol-category__item--active': currentCategory === ORDER_CATEGORY_ROUTE }"
					@click="switchCategory(ORDER_CATEGORY_ROUTE)">
					<text class="ol-category__text">{{ ui.categoryRoute }}</text>
				</view>
			</view>

			<scroll-view class="ol-status-scroll" scroll-x :show-scrollbar="false">
				<view class="ol-status-row">
					<view v-for="(tab, index) in statusTabs" :key="tab" class="ol-status-pill"
						:class="{ 'ol-status-pill--active': currentStatusTab === index }" @click="switchStatusTab(index)">
						<text class="ol-status-pill__text">{{ tab }}</text>
					</view>
				</view>
			</scroll-view>
		</view>

		<view class="ol-head-spacer" />

		<view class="ol-body">
			<view v-if="displayRecords.length" class="ol-list">
				<view v-for="record in displayRecords" :key="record.id" class="ol-card ry-card" @click="openDetail(record)">
					<view class="ol-card__head">
						<view class="ol-card__shop">
							<u-icon :name="shopIcon(record)" :size="15" :color="accentColor" />
							<text class="ol-card__shop-name">{{ shopName(record) }}</text>
						</view>
						<text class="ol-card__status" :class="statusBadgeClass(record)">{{ displayStatus(record) }}</text>
					</view>

					<view class="ol-card__body">
						<image class="ol-card__cover ry-placeholder-img" :src="coverImage(record)" mode="aspectFill" />
						<view v-if="isRoute(record)" class="ol-card__content ol-card__content--route">
							<text class="ol-card__title ol-card__title--route">{{ mainTitle(record) }}</text>
							<view class="ol-card__route-info">
								<view v-if="routeTagList(record).length" class="ol-card__route-tags">
									<text
										v-for="(tag, tagIndex) in routeTagList(record)"
										:key="tagIndex"
										class="ol-card__route-tag"
									>{{ tag }}</text>
								</view>
								<view v-if="record.tripDate" class="ol-card__route-date">
									<u-icon name="calendar" color="#999999" :size="13" />
									<text class="ol-card__route-date-text">{{ routeTripDateText(record) }}</text>
								</view>
								<text v-if="routeContactLine(record)" class="ol-card__route-contact">{{ routeContactLine(record) }}</text>
							</view>
						</view>
						<view v-else class="ol-card__content">
							<view class="ol-card__title-row">
								<text class="ol-card__title">{{ mainTitle(record) }}</text>
								<text v-if="titleTag(record)" class="ol-card__title-tag">{{ titleTag(record) }}</text>
							</view>
							<text v-if="descLine(record)" class="ol-card__desc">{{ descLine(record) }}</text>
							<text v-if="highlightLine(record)" class="ol-card__highlight">{{ highlightLine(record) }}</text>
							<text v-if="extraLine(record)" class="ol-card__extra">{{ extraLine(record) }}</text>
						</view>
					</view>

					<view class="ol-card__foot">
						<view v-if="isRoute(record)" class="ol-card__amount-row">
							<text class="ol-card__amount-label">{{ ui.labelAmountRoute }}</text>
							<text class="ol-card__amount" :style="{ color: priceColor }">{{ routeRefAmount(record) }}</text>
						</view>
						<text v-else class="ol-card__amount" :style="{ color: priceColor }">{{ displayAmount(record) }}</text>
						<view v-if="userActions(record).length" class="ol-card__actions" @click.stop>
							<view
								v-for="action in userActions(record)"
								:key="action.key"
								class="ol-card__action"
								:class="action.tone === 'primary' ? 'ol-card__action--primary' : 'ol-card__action--ghost'"
								:style="actionBtnStyle(action)"
								@click="onUserAction(action.key, record)"
							>
								<u-icon v-if="action.key === 'verifyCode'" name="scan" :size="14"
									:color="action.tone === 'primary' ? '#fff' : accentColor" />
								<text>{{ action.label }}</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-else class="ol-empty">
				<u-empty mode="list" :text="emptyText" />
				<view class="ol-empty__btn" :style="{ background: accentColor }" @click="goExplore">
					<text>{{ emptyActionText }}</text>
				</view>
			</view>
		</view>
		<GlobalRouteRefundHost />
	</view>
</template>

<script setup>
	import {
		computed,
		ref
	} from 'vue';
	import {
		onShow,
		onLoad,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO,
		getModuleColor
	} from '@/common/theme.js';
	import { hasToken } from '@/common/api/auth.js';
	import { fetchOrderList } from '@/common/api/order.js';
	import {
		ORDER_UI,
		ORDER_CATEGORY_HOTEL,
		ORDER_CATEGORY_GOODS,
		ORDER_CATEGORY_ROUTE,
		formatOrderAmount,
		inferOrderCategory,
		consumeOrderTabCategory,
		mapOrderVo,
		resolveOrderDisplayStatus
	} from '@/common/order-ui.js';
	import {
		dictItemsToTabLabels,
		getOrderFilterDictType,
		loadDictItems,
		resolveStatusFromDictItems
	} from '@/common/dict-util.js';
	import { goLogin } from '@/common/navigation.js';
	import {
		getUserOrderActions,
		runUserOrderAction
	} from '@/common/order-actions.js';
	import GlobalRouteRefundHost from '@/components/global-route-refund-host.vue';

	const PAGE_SIZE = 10;
	const ui = ORDER_UI;
	const currentCategory = ref(ORDER_CATEGORY_HOTEL);
	const currentStatusTab = ref(0);
	const records = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);
	const loadSeq = ref(0);
	const filterDictItems = ref([]);

	const filterDictItemsForTabs = computed(() => {
		if (currentCategory.value !== ORDER_CATEGORY_HOTEL
			&& currentCategory.value !== ORDER_CATEGORY_GOODS) {
			return filterDictItems.value;
		}
		return filterDictItems.value.filter(
			(item) => item.label !== '待确认' && item.code !== 'pending_confirm'
		);
	});

	const statusTabs = computed(() => {
		let labels = dictItemsToTabLabels(filterDictItemsForTabs.value);
		if (!labels.length) {
			if (currentCategory.value === ORDER_CATEGORY_GOODS) labels = [...ui.goodsTabs];
			else if (currentCategory.value === ORDER_CATEGORY_ROUTE) labels = [...ui.routeTabs];
			else labels = [...ui.hotelTabs];
		}
		return labels;
	});

	async function loadFilterDict() {
		const type = getOrderFilterDictType(currentCategory.value);
		filterDictItems.value = await loadDictItems(type);
	}

	const accentColor = computed(() => {
		if (currentCategory.value === ORDER_CATEGORY_GOODS) return getModuleColor('heritageShop');
		if (currentCategory.value === ORDER_CATEGORY_ROUTE) return getModuleColor('route');
		return getModuleColor('hotel');
	});

	const priceColor = computed(() => {
		if (currentCategory.value === ORDER_CATEGORY_GOODS) return '#B45D2D';
		return '#FF6600';
	});

	const displayRecords = computed(() => records.value);

	const emptyText = computed(() => {
		if (currentCategory.value === ORDER_CATEGORY_GOODS) return ui.emptyGoods;
		if (currentCategory.value === ORDER_CATEGORY_ROUTE) return ui.emptyRoute;
		return ui.emptyHotel;
	});

	const emptyActionText = computed(() => {
		if (currentCategory.value === ORDER_CATEGORY_GOODS) return ui.goShop;
		if (currentCategory.value === ORDER_CATEGORY_ROUTE) return ui.goRoute;
		return ui.goHotel;
	});

	function isRoute(record) {
		return inferOrderCategory(record) === ORDER_CATEGORY_ROUTE;
	}

	function isHotel(record) {
		return inferOrderCategory(record) === ORDER_CATEGORY_HOTEL;
	}

	function requireLogin() {
		if (hasToken()) return true;
		records.value = [];
		uni.showToast({ title: ui.loginTip, icon: 'none' });
		goLogin({ delay: 400 });
		return false;
	}

	function resolveListStatus(category, statusTab, dictItems) {
		let status = resolveStatusFromDictItems(dictItems, statusTab);
		if (!status && !dictItems.length) {
			const fallbackTabs = category === ORDER_CATEGORY_GOODS
				? ui.goodsTabs
				: category === ORDER_CATEGORY_ROUTE
					? ui.routeTabs
					: ui.hotelTabs;
			const tabLabel = fallbackTabs[statusTab];
			const statusMap = {
				待入住: 'pending_use',
				待出行: 'pending_use',
				待发货: 'pending_use',
				待收货: 'pending_receive',
				已完成: 'completed',
				已取消: 'cancelled',
				已退款: 'refunded'
			};
			status = statusMap[tabLabel];
		}
		return status;
	}

	async function loadRecords(reset = false) {
		if (!hasToken()) {
			records.value = [];
			return;
		}
		if (!reset && (loading.value || !hasMore.value)) return;

		const requestSeq = ++loadSeq.value;
		const requestCategory = currentCategory.value;
		const requestStatusTab = currentStatusTab.value;
		const requestDictItems = filterDictItemsForTabs.value;

		if (reset) {
			page.value = 1;
			hasMore.value = true;
			records.value = [];
		}

		const requestPage = page.value;
		loading.value = true;
		const params = {
			page: requestPage,
			limit: PAGE_SIZE,
			category: requestCategory
		};
		const status = resolveListStatus(requestCategory, requestStatusTab, requestDictItems);
		if (status) params.status = status;

		try {
			const res = await fetchOrderList(params);
			if (requestSeq !== loadSeq.value) return;
			if (res.code === 0 && res.data) {
				const list = (res.data.records || []).map(mapOrderVo);
				records.value = reset ? list : [...records.value, ...list];
				const total = Number(res.data.total ?? 0);
				hasMore.value = list.length >= PAGE_SIZE && records.value.length < total;
			} else {
				if (reset) records.value = [];
				uni.showToast({ title: res.msg || ui.loadFail, icon: 'none' });
			}
		} catch (e) {
			if (requestSeq !== loadSeq.value) return;
			if (reset) records.value = [];
			uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
		} finally {
			if (requestSeq === loadSeq.value) {
				loading.value = false;
			}
		}
	}

	async function switchCategory(category) {
		if (currentCategory.value === category) return;
		currentCategory.value = category;
		currentStatusTab.value = 0;
		records.value = [];
		await loadFilterDict();
		loadRecords(true);
	}

	function switchStatusTab(index) {
		if (currentStatusTab.value === index) return;
		currentStatusTab.value = index;
		records.value = [];
		loadRecords(true);
	}

	function shopIcon(record) {
		if (isRoute(record)) return 'list-dot';
		if (isHotel(record)) return 'home-fill';
		return 'bag-fill';
	}

	function statusBadgeClass(record) {
		const code = record.statusCode || '';
		if (code === 'completed') return 'ol-card__status--done';
		if (code === 'cancelled' || code === 'refunding') return 'ol-card__status--cancel';
		if (code === 'pending_receive' || code === 'pending_ship') return 'ol-card__status--ship';
		if (code === 'pending_use' || code === 'pending_confirm' || code === 'pending_pay') {
			return 'ol-card__status--pending';
		}
		return 'ol-card__status--default';
	}

	function shopName(record) {
		if (isRoute(record)) {
			const name = String(record.merchantName || '').trim();
			const routeTitle = String(record.routeName || '').trim();
			if (name && name !== routeTitle) return name;
			return ui.shopName;
		}
		return record.merchantName || ui.shopName;
	}

	function coverImage(record) {
		if (isHotel(record) && record.hotelCover) return record.hotelCover;
		return record.goodsCover || PLACEHOLDER_LOGO;
	}

	function mainTitle(record) {
		if (isRoute(record)) return record.routeName || '';
		if (isHotel(record)) return record.roomName || record.merchantName;
		return record.goodsName || record.merchantName;
	}

	function titleTag(record) {
		if (isHotel(record) && record.roomCount > 1) return `${record.roomCount}间`;
		if (!isHotel(record) && !isRoute(record) && record.quantity > 1) {
			return `${ui.labelQty}${record.quantity}`;
		}
		return '';
	}

	/** 规格 / 时长等次要描述 */
	function descLine(record) {
		if (isHotel(record)) return record.roomSpec || '';
		return record.goodsSpec || '';
	}

	function routeTagList(record) {
		if (!isRoute(record)) return [];
		const tags = [];
		if (record.duration) tags.push(record.duration);
		if (record.guestCount) tags.push(`${record.guestCount}${ui.guestCountSuffix}`);
		return tags;
	}

	function routeTripDateText(record) {
		if (!record.tripDate) return '';
		const parts = String(record.tripDate).split('-');
		if (parts.length >= 3) {
			return `${Number(parts[1])}月${Number(parts[2])}日 出行`;
		}
		return `${record.tripDate} 出行`;
	}

	function routeContactLine(record) {
		if (!isRoute(record)) return '';
		const name = String(record.contactName || record.guestNames || '').trim();
		if (!name) return '';
		return `联系人 ${name}`;
	}

	/** 核心信息一行：日期 / 行程 / 地址摘要 */
	function highlightLine(record) {
		if (isHotel(record) && record.checkIn) return hotelDateLine(record);
		if (isRoute(record) && record.tripDate) {
			return `${ui.labelTripDate} ${record.tripDate}`;
		}
		return '';
	}

	function hotelDateLine(record) {
		const checkIn = formatShortDate(record.checkIn);
		const checkOut = formatShortDate(record.checkOut);
		const nights = record.nights ? `${record.nights}${ui.nightSuffix}` : '';
		const range = `${checkIn} 至 ${checkOut}`;
		return nights ? `${range} · ${nights}` : range;
	}

	/** 补充一行：预定人 / 集合点 / 收货信息 */
	function extraLine(record) {
		if (isHotel(record)) {
			return record.guestNames ? `预定 ${record.guestNames}` : '';
		}
		if (isRoute(record)) {
			return record.meetPoint || record.guestNames || '';
		}
		if (record.addressText) {
			const contact = record.contactName ? `${record.contactName} ` : '';
			return `${contact}${record.addressText}`.trim();
		}
		return '';
	}

	function formatShortDate(dateStr) {
		if (!dateStr) return '--';
		const parts = String(dateStr).split('-');
		if (parts.length < 3) return dateStr;
		return `${Number(parts[1])}/${Number(parts[2])}`;
	}

	function openDetail(record) {
		uni.navigateTo({
			url: `/pages/order/detail?id=${record.id}`
		});
	}

	function routeRefAmount(record) {
		const refAmount = Number(record.totalAmount) > 0 ? Number(record.totalAmount) : Number(record.amount) || 0;
		return formatOrderAmount(refAmount);
	}

	function displayAmount(record) {
		return formatOrderAmount(record.amount);
	}

	function displayStatus(record) {
		return resolveOrderDisplayStatus(record);
	}

	function userActions(record) {
		return getUserOrderActions(record);
	}

	function actionBtnStyle(action) {
		if (action.tone !== 'primary') {
			return { borderColor: '#e5e7eb', color: '#666666' };
		}
		if (currentCategory.value === ORDER_CATEGORY_GOODS) {
			return { background: 'linear-gradient(135deg, #A86B3A, #B45D2D)' };
		}
		if (currentCategory.value === ORDER_CATEGORY_ROUTE) {
			return { background: 'linear-gradient(135deg, #FF8A3D, #FF6600)' };
		}
		return { background: 'linear-gradient(135deg, #FF8A3D, #FF6600)' };
	}

	async function onUserAction(actionKey, record) {
		if (actionKey === 'pay') {
			uni.navigateTo({
				url: `/pages/order/detail?id=${record.id}&pay=1`
			});
			return;
		}
		await runUserOrderAction(actionKey, record, {
			onChanged: () => loadRecords(true)
		});
	}

	function goExplore() {
		if (currentCategory.value === ORDER_CATEGORY_GOODS) {
			uni.navigateTo({ url: '/mobile-design/heritage/goods-list' });
			return;
		}
		if (currentCategory.value === ORDER_CATEGORY_ROUTE) {
			uni.navigateTo({ url: '/mobile-design/route/list' });
			return;
		}
		uni.navigateTo({ url: '/mobile-design/hotel/list' });
	}

	onLoad((options) => {
		if (options?.category === ORDER_CATEGORY_ROUTE) {
			currentCategory.value = ORDER_CATEGORY_ROUTE;
		}
	});

	onShow(async () => {
		if (!requireLogin()) return;
		const tabCategory = consumeOrderTabCategory();
		if (tabCategory) {
			currentCategory.value = tabCategory;
			currentStatusTab.value = 0;
		}
		await loadFilterDict();
		loadRecords(true);
		uni.setNavigationBarTitle({ title: ui.navTitle });
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
		if (!hasToken() || !hasMore.value || loading.value) return;
		page.value += 1;
		loadRecords(false);
	});
</script>

<style lang="scss" scoped>
	.ol-page {
		min-height: 100vh;
		background: #f5f6fa;
		padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
	}

	.ol-page--hotel {
		background: #fff9f0;
	}

	.ol-page--route {
		background: #fff9f0;
	}

	.ol-page--goods {
		background: #faf6f3;
	}

	.ol-head {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 100;
		padding: 20rpx var(--ry-page-padding-x) 0;
		background-color: #ffffff;
	}

	.ol-head-spacer {
		height: 200rpx;
		flex-shrink: 0;
	}

	.ol-category {
		display: flex;
		gap: 12rpx;
		padding-bottom: 16rpx;
	}

	.ol-category__item {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 72rpx;
		border-radius: 16rpx;
		background-color: #f5f6f8;
		box-sizing: border-box;
		transition: all 0.2s ease;
	}

	.ol-category__text {
		font-size: 28rpx;
		font-weight: 500;
		color: #666666;
		line-height: 1;
	}

	.ol-category__item--active.ol-category__item--goods {
		background-color: #fff6f0;

		.ol-category__text {
			color: #b45d2d;
			font-weight: 600;
		}
	}

	.ol-category__item--active.ol-category__item--hotel {
		background-color: #fff3eb;

		.ol-category__text {
			color: #ff6600;
			font-weight: 600;
		}
	}

	.ol-category__item--active.ol-category__item--route {
		background-color: #fff3eb;

		.ol-category__text {
			color: #ff6600;
			font-weight: 600;
		}
	}

	.ol-status-scroll {
		width: 100%;
		white-space: nowrap;
	}

	.ol-status-row {
		display: inline-flex;
		gap: 12rpx;
		padding-bottom: 20rpx;
	}

	.ol-status-pill {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		height: 56rpx;
		padding: 0 28rpx;
		border-radius: 999rpx;
		background-color: #f5f6f8;
		box-sizing: border-box;
		transition: all 0.2s ease;
	}

	.ol-status-pill__text {
		font-size: 24rpx;
		font-weight: 500;
		color: #666666;
		line-height: 1;
	}

	.ol-page--goods .ol-status-pill--active {
		background-color: #fff6f0;

		.ol-status-pill__text {
			color: #b45d2d;
			font-weight: 600;
		}
	}

	.ol-page--hotel .ol-status-pill--active {
		background-color: #fff3eb;

		.ol-status-pill__text {
			color: #ff6600;
			font-weight: 600;
		}
	}

	.ol-page--route .ol-status-pill--active {
		background-color: #fff3eb;

		.ol-status-pill__text {
			color: #ff6600;
			font-weight: 600;
		}
	}

	.ol-page--hotel .ol-card__status--ship {
		background: #fff3eb;
		color: #ff6600;
	}

	.ol-page--hotel .ol-card__title-tag {
		color: #ff6600;
		background: #fff3eb;
	}

	.ol-body {
		padding: 0 var(--ry-page-padding-x) 24rpx;
	}

	.ol-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.ol-card {
		padding: 0;
		overflow: hidden;
		border-radius: 20rpx;
		background: #fff;
		box-shadow: 0 4rpx 24rpx rgba(15, 23, 42, 0.06);

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			padding: 20rpx 24rpx 16rpx;
		}

		&__shop {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			gap: 8rpx;
		}

		&__shop-name {
			font-size: 26rpx;
			font-weight: 600;
			color: #333;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__status {
			flex-shrink: 0;
			font-size: 22rpx;
			font-weight: 600;
			padding: 6rpx 14rpx;
			border-radius: 999rpx;
			line-height: 1.2;

			&--pending {
				background: #fff3eb;
				color: #ff6600;
			}

			&--ship {
				background: #f0f6ff;
				color: #2979ff;
			}

			&--done {
				background: #e8f8ef;
				color: #2eaf6a;
			}

			&--cancel {
				background: #fff0f0;
				color: #ff4d4f;
			}

			&--default {
				background: #f5f6f8;
				color: #666;
			}
		}

		&__body {
			display: flex;
			align-items: stretch;
			gap: 20rpx;
			padding: 0 24rpx 20rpx;
		}

		&__cover {
			width: 156rpx;
			height: 156rpx;
			border-radius: 14rpx;
			flex-shrink: 0;
			background: #f0f0f0;
		}

		&__content {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			justify-content: center;
			gap: 8rpx;

			&--route {
				justify-content: space-between;
				min-height: 156rpx;
				box-sizing: border-box;
				padding: 2rpx 0 4rpx;
			}
		}

		&__title--route {
			display: -webkit-box;
			overflow: hidden;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			line-clamp: 2;
			white-space: normal;
			text-overflow: ellipsis;
		}

		&__route-info {
			display: flex;
			flex-direction: column;
			gap: 10rpx;
			margin-top: 8rpx;
		}

		&__route-tags {
			display: flex;
			flex-wrap: wrap;
			gap: 8rpx;
		}

		&__route-tag {
			display: inline-flex;
			align-items: center;
			height: 36rpx;
			padding: 0 14rpx;
			border-radius: 999rpx;
			background: #fff3eb;
			font-size: 22rpx;
			color: #ff6600;
			line-height: 1;
		}

		&__route-date {
			display: flex;
			align-items: center;
			gap: 6rpx;
			min-width: 0;
		}

		&__route-date-text {
			font-size: 24rpx;
			color: #666666;
			line-height: 1.3;
		}

		&__route-contact {
			font-size: 22rpx;
			color: #999999;
			line-height: 1.3;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__title-row {
			display: flex;
			align-items: center;
			gap: 10rpx;
			min-width: 0;
		}

		&__title {
			flex: 1;
			min-width: 0;
			font-size: 28rpx;
			font-weight: 600;
			color: #1a1a1a;
			line-height: 1.35;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__title-tag {
			flex-shrink: 0;
			font-size: 20rpx;
			color: #2979ff;
			background: #f0f6ff;
			padding: 4rpx 10rpx;
			border-radius: 6rpx;
			line-height: 1.2;
		}

		&__desc {
			font-size: 24rpx;
			color: #888;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__highlight {
			font-size: 24rpx;
			color: #444;
			line-height: 1.4;
			margin-top: 16rpx;
		}

		&__extra {
			font-size: 22rpx;
			color: #999;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__foot {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			padding: 16rpx 24rpx 20rpx;
			border-top: 1rpx solid #f3f4f6;
		}

		&__amount-row {
			display: flex;
			align-items: baseline;
			gap: 8rpx;
			flex-shrink: 0;
		}

		&__amount-label {
			font-size: 22rpx;
			font-weight: 500;
			color: #999999;
			line-height: 1;
		}

		&__amount {
			font-size: 34rpx;
			font-weight: 700;
			line-height: 1;
			flex-shrink: 0;
		}

		&__actions {
			flex: 1;
			display: flex;
			justify-content: flex-end;
			flex-wrap: wrap;
			gap: 12rpx;
			min-width: 0;
		}

		&__action {
			height: 56rpx;
			padding: 0 22rpx;
			border-radius: 999rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 6rpx;
			box-sizing: border-box;
			border: 2rpx solid transparent;
			flex-shrink: 0;

			text {
				font-size: 24rpx;
				font-weight: 600;
				line-height: 1;
			}

			&--primary {
				border: none;
				box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);

				text {
					color: #fff;
				}
			}

			&--ghost {
				background: #fff;
				border-color: #e5e7eb;

				text {
					color: #666;
				}
			}

			&:active {
				opacity: 0.85;
				transform: scale(0.98);
			}
		}
	}

	.ol-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 120rpx;
		gap: 28rpx;

		&__btn {
			min-width: 240rpx;
			height: 80rpx;
			padding: 0 40rpx;
			border-radius: 999rpx;
			display: flex;
			align-items: center;
			justify-content: center;

			text {
				font-size: 28rpx;
				font-weight: 600;
				color: #fff;
			}

			&:active {
				opacity: 0.9;
			}
		}
	}
</style>
