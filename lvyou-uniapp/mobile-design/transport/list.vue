<template>
	<view class="bus-page">
		<view class="bus-top">
			<view class="bus-search">
				<u-search
					v-model="keyword"
					placeholder="搜索公交线路或站点"
					:show-action="false"
					bg-color="#FFFFFF"
					border-color="#E8EBF0"
					height="34"
					@search="onSearch"
					@clear="onSearch"
				/>
			</view>
			<view class="bus-locate" :class="{ 'bus-locate--loading': locating }">
				<u-icon name="map-fill" color="#FF6600" :size="13" />
				<text class="bus-locate__text">{{ locationLabel || (locating ? '正在定位…' : '') }}</text>
				<text v-if="stationGroups.length && !locating" class="bus-locate__count">{{ stationGroups.length }} 个附近站</text>
			</view>
		</view>

		<view v-if="initialLoading" class="bus-skeleton">
			<view v-for="n in 3" :key="n" class="bus-skeleton__card">
				<view class="bus-skeleton__head">
					<view class="bus-skeleton__icon bus-shimmer" />
					<view class="bus-skeleton__text">
						<view class="bus-skeleton__line bus-skeleton__line--lg bus-shimmer" />
						<view class="bus-skeleton__line bus-skeleton__line--sm bus-shimmer" />
					</view>
				</view>
				<view v-for="m in 2" :key="m" class="bus-skeleton__row bus-shimmer" />
			</view>
		</view>

		<view v-else-if="stationGroups.length" class="bus-list" :class="{ 'bus-list--refreshing': refreshing }">
			<view
				v-for="(group, index) in stationGroups"
				:key="group.name"
				class="bus-stop bus-stop--enter"
				:style="{ animationDelay: `${Math.min(index, 8) * 50}ms` }"
			>
				<view
					class="bus-stop__head"
					hover-class="bus-stop__head--press"
					:hover-stay-time="100"
					@click="openStationGroup(group)"
				>
					<view class="bus-stop__mark">
						<u-icon name="car-fill" color="#FFFFFF" :size="16" />
					</view>
					<view class="bus-stop__info">
						<text class="bus-stop__name">{{ group.name }}</text>
						<text class="bus-stop__meta">
							{{ group.routes.length }} 条线路
							<text v-if="group.distanceLabel"> · {{ group.distanceLabel }}</text>
						</text>
					</view>
					<u-icon name="arrow-right" color="#C8CDD4" :size="12" />
				</view>

				<view
					class="bus-stop__lines"
					:class="{ 'bus-stop__lines--expanded': expandedStations[group.name] }"
				>
					<view
						v-for="route in group.routes"
						:key="`${group.name}-${route.id}`"
						class="bus-line"
						hover-class="bus-line--press"
						:hover-stay-time="80"
						@click="openTrack(route)"
					>
						<view class="bus-line__badge" :style="{ background: route.accentColor || busAccent }">
							<text class="bus-line__badge-text">{{ route.name }}</text>
						</view>
						<view class="bus-line__body">
							<text class="bus-line__to">开往 {{ route.endStation || route.startStation }}</text>
							<text class="bus-line__sub">{{ formatRouteMeta(route) }}</text>
						</view>
						<view class="bus-line__eta">
							<template v-if="!arrivalParts(route).isText">
								<text class="bus-line__eta-num">{{ arrivalParts(route).value }}</text>
								<text class="bus-line__eta-unit">{{ arrivalParts(route).unit }}</text>
							</template>
							<text v-else class="bus-line__eta-muted">{{ arrivalParts(route).value }}</text>
						</view>
					</view>
				</view>

				<view
					v-if="group.routes.length > ROUTE_PREVIEW"
					class="bus-stop__more"
					hover-class="bus-stop__more--press"
					:hover-stay-time="80"
					@click="toggleExpand(group.name)"
				>
					<text>{{ expandedStations[group.name] ? '收起' : `查看全部 ${group.routes.length} 条线路` }}</text>
					<view
						class="bus-stop__more-arrow"
						:class="{ 'bus-stop__more-arrow--up': expandedStations[group.name] }"
					>
						<u-icon name="arrow-down" color="#A0A7B2" :size="12" />
					</view>
				</view>
			</view>

			<view v-if="loadingMore" class="bus-foot bus-foot--loading">
				<view class="bus-foot__dot" />
				<text class="bus-foot__text">加载更多站点</text>
			</view>
			<view v-else-if="!hasMore" class="bus-foot">
				<text class="bus-foot__text">— 已显示全部附近站点 —</text>
			</view>
		</view>

		<view v-else-if="!initialLoading" class="bus-state bus-state--empty">
			<u-empty mode="search" text="附近暂无公交线路" icon-size="120" />
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { fetchTransportRouteList } from '@/common/api/transport.js';
import { appendLocationParams, getAppLocationParams } from '@/common/location-params.js';
import { buildTransportRouteQuery, serializeTransportQuery } from '@/common/transport-context.js';

const PAGE_SIZE = 5;
const ROUTE_PREVIEW = 3;
const busAccent = getModuleColor('transport');
const defaultAccent = busAccent;

const keyword = ref('');
const searchKey = ref('');
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
const initialLoading = ref(true);
const refreshing = ref(false);
const loadingMore = ref(false);
const locating = ref(false);
const totalCount = ref(0);
const locationLabel = ref('');
const nearbyStations = ref([]);
const allRoutes = ref([]);
const expandedStations = ref({});

const stationGroups = computed(() => {
	const routeMap = {};
	for (const route of allRoutes.value) {
		const name = route.referenceStation || '附近站点';
		if (!routeMap[name]) routeMap[name] = [];
		routeMap[name].push(route);
	}

	if (nearbyStations.value.length) {
		return nearbyStations.value
			.map((station) => ({
				name: station.name,
				distanceLabel: station.distanceLabel,
				routes: routeMap[station.name] || []
			}))
			.filter((group) => group.routes.length > 0);
	}

	return Object.keys(routeMap).map((name) => ({
		name,
		distanceLabel: '',
		routes: routeMap[name]
	}));
});

function mapRouteItem(record) {
	return {
		id: record.id,
		city: record.city || '',
		referenceStation: record.referenceStation || '',
		direction: record.direction || 1,
		name: record.name || '',
		firstBus: record.firstBus || '--',
		lastBus: record.lastBus || '--',
		startStation: record.startStation || '',
		endStation: record.endStation || '',
		statusLabel: record.statusLabel || '运行中',
		accentColor: record.accentColor || defaultAccent,
		accentLight: record.accentLight || '#FFF2E8',
		showRealtimeStops: !!record.showRealtimeStops,
		currentStop: record.currentStop || '',
		nextStop: record.nextStop || '',
		nextEta: record.nextEta || '',
		expandable: !!record.expandable,
		stationCount: record.stationCount || (record.stations || []).length,
		currentStopIndex: record.currentStopIndex ?? 0,
		stations: record.stations || [],
		trackInfo: record.trackInfo || null
	};
}

function formatDistanceLabel(distanceKm) {
	const distance = Number(distanceKm);
	if (!Number.isFinite(distance)) return '';
	if (distance < 1) return `${Math.round(distance * 1000)}米`;
	return `${distance.toFixed(1)}千米`;
}

function mapNearbyStation(item) {
	return {
		name: item.name || '',
		distanceKm: item.distanceKm,
		distanceLabel: formatDistanceLabel(item.distanceKm)
	};
}

function toggleExpand(stationName) {
	expandedStations.value = {
		...expandedStations.value,
		[stationName]: !expandedStations.value[stationName]
	};
}

function arrivalParts(route) {
	const eta = String(route.nextEta || '').trim();
	const minuteMatch = eta.match(/(\d+)\s*分钟/);
	if (minuteMatch) {
		return { value: minuteMatch[1], unit: '分钟', isText: false };
	}
	const secondMatch = eta.match(/(\d+)\s*秒/);
	if (secondMatch) {
		return { value: secondMatch[1], unit: '秒', isText: false };
	}
	if (route.nextStop && route.nextStop !== '暂无车辆') {
		return { value: route.nextStop, unit: '', isText: true };
	}
	if (route.statusLabel === '暂无车辆') {
		return { value: '暂无', unit: '', isText: true };
	}
	return { value: '--', unit: '', isText: true };
}

function formatRouteMeta(route) {
	const stopText = String(route.nextStop || '').trim();
	if (stopText && stopText !== '暂无车辆') {
		return stopText.includes('站') ? stopText : stopText;
	}
	return route.statusLabel || '等待发车';
}

function onSearch() {
	searchKey.value = keyword.value.trim();
	loadRouteList(true);
}

async function loadRouteList(reset = false, forceRefreshLocation = false) {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	const hadContent = stationGroups.value.length > 0;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
		expandedStations.value = {};
		if (hadContent) {
			refreshing.value = true;
		}
	} else {
		loadingMore.value = true;
	}

	loading.value = true;
	const base = { page: page.value, limit: PAGE_SIZE };
	if (searchKey.value) base.keyword = searchKey.value;

	let params;
	try {
		locating.value = true;
		const location = await getAppLocationParams(forceRefreshLocation || reset);
		locating.value = false;
		console.log('[transport/list] uni.getLocation 成功', location.getLocationRaw || {
			latitude: Number(location.lat),
			longitude: Number(location.lng),
			accuracy: location.accuracy,
			highAccuracy: location.highAccuracy
		});
		console.log('[transport/list] 定位解析结果', {
			lat: location.lat,
			lng: location.lng,
			city: location.city,
			district: location.district,
			label: location.label,
			accuracy: location.accuracy,
			highAccuracy: location.highAccuracy
		});
		params = appendLocationParams(base, location);
		if (params.label) locationLabel.value = params.label;
	} catch (e) {
		locating.value = false;
		console.warn('[transport/list] uni.getLocation 失败', e);
		uni.showToast({ title: '定位失败，请开启定位权限', icon: 'none' });
		loading.value = false;
		initialLoading.value = false;
		refreshing.value = false;
		loadingMore.value = false;
		return;
	}

	try {
		const res = await fetchTransportRouteList(params);
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapRouteItem);
			if (reset) {
				allRoutes.value = records;
				nearbyStations.value = (res.data.nearbyStations || []).map(mapNearbyStation);
			} else {
				allRoutes.value = [...allRoutes.value, ...records];
			}
			const total = Number(res.data.total ?? 0);
			totalCount.value = total;
			if (searchKey.value) {
				hasMore.value = records.length > 0 && allRoutes.value.length < total;
			} else {
				hasMore.value = page.value * PAGE_SIZE < total;
			}
			if (records.length && records[0].city) {
				locationLabel.value = locationLabel.value || records[0].city;
			}
		} else {
			if (reset) {
				allRoutes.value = [];
				nearbyStations.value = [];
				totalCount.value = 0;
			}
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) {
			allRoutes.value = [];
			nearbyStations.value = [];
			totalCount.value = 0;
		}
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
		initialLoading.value = false;
		refreshing.value = false;
		loadingMore.value = false;
	}
}

function buildNavUrl(pagePath, route = {}) {
	const query = serializeTransportQuery(buildTransportRouteQuery(route));
	return query
		? `/mobile-design/transport/${pagePath}?${query}`
		: `/mobile-design/transport/${pagePath}`;
}

function openStationGroup(group) {
	const route = group.routes[0];
	if (route) openStations(route);
}

function openStations(route) {
	uni.navigateTo({ url: buildNavUrl('stations', route) });
}

function openTrack(route) {
	uni.navigateTo({ url: buildNavUrl('track', route) });
}

onLoad(async () => {
	uni.setNavigationBarTitle({ title: '附近公交' });
	await loadRouteList(true, true);
});

onPullDownRefresh(async () => {
	await loadRouteList(true, true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasMore.value || loading.value) return;
	page.value += 1;
	loadRouteList(false);
});
</script>

<style lang="scss" scoped>
@keyframes busSlideUp {
	from {
		opacity: 0;
		transform: translateY(20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes busShimmer {
	0% {
		background-position: 200% 0;
	}
	100% {
		background-position: -200% 0;
	}
}

@keyframes busPulse {
	0%,
	100% {
		opacity: 0.35;
	}
	50% {
		opacity: 1;
	}
}

@keyframes busGlow {
	0%,
	100% {
		box-shadow: 0 0 20rpx rgba(255, 102, 0, 0.15);
	}
	50% {
		box-shadow: 0 0 30rpx rgba(255, 102, 0, 0.25);
	}
}

.bus-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #f8f9fc 0%, #eef1f6 100%);
	position: relative;
	
	&::before {
		content: '';
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		height: 300rpx;
		background: linear-gradient(135deg, #ff6600 0%, #ff8533 50%, #ffaa66 100%);
		opacity: 0.06;
		pointer-events: none;
	}
}

/* ---- 顶部:搜索 + 定位一行 ---- */
.bus-top {
	padding: 16rpx 24rpx 12rpx;
	background: transparent;
	position: relative;
	z-index: 10;
}

.bus-search {
	margin-bottom: 12rpx;
	
	::v-deep .u-search {
		box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.06), 
		            0 2rpx 8rpx rgba(0, 0, 0, 0.04);
		border-radius: 20rpx !important;
		backdrop-filter: blur(10rpx);
		border: 1rpx solid rgba(255, 255, 255, 0.8) !important;
		transition: all 0.3s ease;
		
		&:active {
			transform: scale(0.995);
		}
	}
}

.bus-locate {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 8rpx;
	padding: 10rpx 16rpx;
	background: rgba(255, 255, 255, 0.7);
	backdrop-filter: blur(10rpx);
	border-radius: 16rpx;
	border: 1rpx solid rgba(255, 255, 255, 0.9);
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
	transition: all 0.3s ease;
}

.bus-locate--loading .bus-locate__text {
	color: #b0b7c3;
}

.bus-locate__text {
	flex: 1;
	min-width: 0;
	font-size: 23rpx;
	color: #4b5563;
	font-weight: normal;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.bus-locate__count {
	flex-shrink: 0;
	font-size: 20rpx;
	font-weight: normal;
	color: #ff6600;
	background: linear-gradient(135deg, rgba(255, 102, 0, 0.12), rgba(255, 133, 51, 0.08));
	padding: 4rpx 14rpx;
	border-radius: 999rpx;
	line-height: 1.6;
}

/* ---- 骨架屏 ---- */
.bus-skeleton {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	padding: 8rpx 24rpx;
}

.bus-skeleton__card {
	background: #ffffff;
	border-radius: 20rpx;
	padding: 24rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04),
	            0 2rpx 8rpx rgba(0, 0, 0, 0.02);
}

.bus-skeleton__head {
	display: flex;
	gap: 16rpx;
	margin-bottom: 24rpx;
}

.bus-skeleton__icon {
	width: 48rpx;
	height: 48rpx;
	border-radius: 14rpx;
	flex-shrink: 0;
}

.bus-skeleton__text {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.bus-skeleton__line {
	border-radius: 10rpx;
	height: 24rpx;

	&--lg {
		width: 55%;
		height: 30rpx;
	}

	&--sm {
		width: 35%;
	}
}

.bus-skeleton__row {
	height: 68rpx;
	border-radius: 12rpx;
	margin-top: 14rpx;
}

.bus-shimmer {
	background: linear-gradient(90deg, #eef0f3 25%, #f8f9fb 50%, #eef0f3 75%);
	background-size: 200% 100%;
	animation: busShimmer 1.4s ease-in-out infinite;
}

/* ---- 列表 ---- */
.bus-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	padding: 8rpx 24rpx calc(24rpx + env(safe-area-inset-bottom));
	transition: opacity 0.3s ease;
}

.bus-list--refreshing {
	opacity: 0.72;
}

.bus-stop {
	background: #ffffff;
	border-radius: 24rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 24rpx rgba(0, 0, 0, 0.06),
	            0 2rpx 12rpx rgba(0, 0, 0, 0.03);
	position: relative;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	
	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 3rpx;
		background: linear-gradient(90deg, transparent, rgba(255, 102, 0, 0.4), transparent);
	}
	
	&:active {
		transform: scale(0.985);
	}
}

.bus-stop--enter {
	animation: busSlideUp 0.45s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.bus-stop__head {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 16rpx;
	padding: 24rpx 24rpx 18rpx;
	transition: background-color 0.2s ease;
	position: relative;
}

.bus-stop__head--press {
	background: #fafbfc;
}

.bus-stop__mark {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	width: 52rpx;
	height: 52rpx;
	border-radius: 16rpx;
	background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
	box-shadow: 0 6rpx 20rpx rgba(255, 102, 0, 0.25),
	            0 2rpx 8rpx rgba(255, 102, 0, 0.15),
	            inset 0 -2rpx 4rpx rgba(0, 0, 0, 0.1);
	position: relative;
	
	&::after {
		content: '';
		position: absolute;
		top: 2rpx;
		left: 2rpx;
		right: 2rpx;
		height: 50%;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.3), transparent);
		border-radius: 14rpx 14rpx 0 0;
		pointer-events: none;
	}
}

.bus-stop__info {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.bus-stop__name {
	font-size: 32rpx;
	font-weight: 600;
	color: #1a202c;
	line-height: 1.35;
	letter-spacing: -0.3rpx;
}

.bus-stop__meta {
	font-size: 23rpx;
	color: #9ca3af;
	line-height: 1.3;
	font-weight: normal;
}

.bus-stop__lines {
	padding: 0 20rpx 6rpx;
	max-height: 300rpx;
	overflow: hidden;
	transition: max-height 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.bus-stop__lines--expanded {
	max-height: 2400rpx;
}

.bus-line {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 16rpx;
	padding: 18rpx 8rpx;
	border-top: 1rpx solid #f5f6f8;
	transition: all 0.2s ease;
	position: relative;
	
	&::before {
		content: '';
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		background: linear-gradient(90deg, rgba(255, 102, 0, 0.02), transparent);
		opacity: 0;
		transition: opacity 0.2s ease;
	}
	
	&:active::before {
		opacity: 1;
	}
}

.bus-line--press {
	background: rgba(255, 102, 0, 0.02);
}

.bus-line__badge {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	min-width: 76rpx;
	max-width: 128rpx;
	padding: 8rpx 14rpx;
	border-radius: 10rpx;
	box-shadow: 0 3rpx 12rpx rgba(0, 0, 0, 0.15),
	            inset 0 1rpx 2rpx rgba(255, 255, 255, 0.3);
	position: relative;
	
	&::after {
		content: '';
		position: absolute;
		top: 1rpx;
		left: 1rpx;
		right: 1rpx;
		height: 45%;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.25), transparent);
		border-radius: 9rpx 9rpx 0 0;
		pointer-events: none;
	}
}

.bus-line__badge-text {
	font-size: 26rpx;
	font-weight: 700;
	color: #ffffff;
	line-height: 1.2;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	letter-spacing: 0.5rpx;
	text-shadow: 0 1rpx 2rpx rgba(0, 0, 0, 0.15);
}

.bus-line__body {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.bus-line__to {
	font-size: 26rpx;
	color: #2d3748;
	line-height: 1.35;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	font-weight: normal;
}

.bus-line__sub {
	font-size: 22rpx;
	color: #a8afba;
	line-height: 1.3;
	font-weight: normal;
}

.bus-line__eta {
	flex-shrink: 0;
	display: flex;
	flex-direction: row;
	align-items: baseline;
	justify-content: flex-end;
	min-width: 88rpx;
	gap: 3rpx;
	padding: 8rpx 12rpx;
	background: linear-gradient(135deg, rgba(5, 150, 105, 0.08), rgba(5, 150, 105, 0.04));
	border-radius: 12rpx;
}

.bus-line__eta-num {
	font-size: 38rpx;
	font-weight: 700;
	color: #059669;
	line-height: 1;
	font-variant-numeric: tabular-nums;
	transition: color 0.2s ease;
}

.bus-line__eta-unit {
	font-size: 22rpx;
	font-weight: normal;
	color: #059669;
	line-height: 1.2;
}

.bus-line__eta-muted {
	font-size: 23rpx;
	color: #b0b7c3;
	line-height: 1.3;
	text-align: right;
	font-weight: normal;
}

.bus-stop__more {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	padding: 16rpx 0 20rpx;
	border-top: 1rpx solid #f5f6f8;
	transition: all 0.2s ease;
	position: relative;
	
	&::before {
		content: '';
		position: absolute;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		background: rgba(255, 102, 0, 0.02);
		opacity: 0;
		transition: opacity 0.2s ease;
	}
	
	&:active::before {
		opacity: 1;
	}
}

.bus-stop__more--press {
	background: rgba(255, 102, 0, 0.02);
}

.bus-stop__more text {
	font-size: 23rpx;
	color: #9ca3af;
	line-height: 1.3;
	font-weight: normal;
}

.bus-stop__more-arrow {
	display: flex;
	align-items: center;
	justify-content: center;
	transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.bus-stop__more-arrow--up {
	transform: rotate(180deg);
}

/* ---- 底部 / 状态 ---- */
.bus-foot {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
	padding: 48rpx 0 32rpx;
}

.bus-foot--loading .bus-foot__text {
	color: #9ca3af;
}

.bus-foot__dot {
	width: 10rpx;
	height: 10rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #ff6600, #ff8533);
	animation: busPulse 1s ease-in-out infinite, busGlow 2s ease-in-out infinite;
	box-shadow: 0 0 12rpx rgba(255, 102, 0, 0.4);
}

.bus-foot__text {
	font-size: 23rpx;
	color: #b0b7c3;
	line-height: 1.4;
	font-weight: normal;
}

.bus-state {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 60vh;
	padding: 48rpx 24rpx;
}

.bus-state--empty {
	min-height: 60vh;
	padding: 120rpx 24rpx;
}
</style>
