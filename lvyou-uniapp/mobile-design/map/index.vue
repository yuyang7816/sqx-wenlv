<template>
	<view class="map-service-page">
		<view class="map-service-body">
			<view class="map-service-tabs">
				<view class="map-service-tabs__track">
					<view
						v-for="cat in categories"
						:key="cat.key"
						class="map-service-tab"
						:class="{ 'map-service-tab--active': currentCategory === cat.key }"
						@click="switchCategory(cat.key)"
					>
						<text class="map-service-tab__text">{{ cat.label }}</text>
						<view v-if="currentCategory === cat.key" class="map-service-tab__line" />
					</view>
				</view>
			</view>

			<!-- 地图导航：起终点为主，结果区可滚动 -->
			<view v-if="isNavigation" class="map-nav-panel">
				<view class="map-nav-modes">
					<view
						v-for="m in routeModes"
						:key="m.key"
						class="map-nav-mode"
						:class="{ 'map-nav-mode--active': routeMode === m.key }"
						@click="onRouteModeChange(m.key)"
					>
						<text class="map-nav-mode__text">{{ m.label }}</text>
					</view>
				</view>

				<view class="map-nav-ends">
					<view class="map-nav-ends__main">
						<view class="map-nav-row">
							<view class="map-nav-row__dot map-nav-row__dot--start" />
							<input
								class="map-nav-row__input"
								v-model="fromText"
								placeholder="输入起点"
								placeholder-class="map-nav-row__ph"
								confirm-type="search"
								@input="onFromInput"
								@focus="suggestTarget = 'from'"
								@confirm="planRoute"
							/>
							<view class="map-nav-row__btn" @click="useMyLocationAsFrom">
								<u-icon name="map-fill" color="#2F6BFF" :size="18" />
							</view>
						</view>
						<view class="map-nav-row">
							<view class="map-nav-row__dot map-nav-row__dot--end" />
							<input
								class="map-nav-row__input"
								v-model="toText"
								placeholder="你要去哪里？"
								placeholder-class="map-nav-row__ph"
								confirm-type="search"
								@input="onToInput"
								@focus="suggestTarget = 'to'"
								@confirm="planRoute"
							/>
							<view class="map-nav-row__btn" @click="swapEnds">
								<view class="map-nav-swap-ico">
									<u-icon name="arrow-upward" color="#2F6BFF" :size="12" />
									<u-icon name="arrow-downward" color="#2F6BFF" :size="12" />
								</view>
							</view>
						</view>
						<view class="map-nav-ends__rail-line" />
					</view>
				</view>

				<view v-if="suggestList.length" class="map-nav-suggest">
					<view
						v-for="item in suggestList"
						:key="item.id"
						class="map-nav-suggest__item"
						@click="pickSuggest(item)"
					>
						<view class="map-nav-suggest__icon">
							<u-icon name="map-fill" color="#2F6BFF" :size="14" />
						</view>
						<view class="map-nav-suggest__main">
							<text class="map-nav-suggest__name">{{ item.name }}</text>
							<text class="map-nav-suggest__addr">{{ item.address }}</text>
						</view>
					</view>
				</view>

				<view
					class="map-nav-plan"
					:class="{ 'map-nav-plan--loading': routeLoading }"
					@click="planRoute"
				>
					<u-icon name="map" color="#fff" :size="18" />
					<text class="map-nav-plan__text">{{ routeLoading ? '规划中…' : '开始规划' }}</text>
				</view>
			</view>

			<!-- 美食 / 停车：单搜索 -->
			<view v-else class="map-service-search">
				<u-search
					v-model="keyword"
					:placeholder="poiSearchPlaceholder"
					:show-action="false"
					bg-color="#F5F5F5"
					height="36"
					@search="onSearch"
					@clear="onSearch"
				/>
			</view>

			<!-- 导航：可滚动路线列表 -->
			<view v-if="isNavigation" class="map-nav-results">
				<scroll-view scroll-y class="map-nav-results__scroll" :show-scrollbar="false">
					<view v-if="routeLoading" class="map-nav-results__state">
						<view class="map-nav-results__pulse" />
						<text class="map-nav-results__hint">正在规划最优路线…</text>
					</view>
					<view v-else-if="!routePlans.length" class="map-nav-results__state">
						<view class="map-nav-results__illus">
							<u-icon name="map" color="#2F6BFF" :size="28" />
						</view>
						<text class="map-nav-results__title">输入终点，开始出行</text>
						<text class="map-nav-results__hint">规划后点选方案可查看分步指引</text>
					</view>
					<template v-else>
						<view class="map-nav-results__head">
							<text class="map-nav-results__count">可选路线</text>
							<text class="map-nav-results__count-num">{{ routePlans.length }} 条</text>
						</view>
						<view class="map-route-list">
							<view
								v-for="(plan, idx) in routePlans"
								:key="idx"
								class="map-route-item"
								:class="{
									'map-route-item--best': idx === 0,
									'map-route-item--active': selectedRouteIndex === idx
								}"
								:style="{ animationDelay: `${idx * 45}ms` }"
								@click="selectRoute(idx)"
							>
								<view class="map-route-item__top">
									<text class="map-route-item__time">{{ plan.durationText || '-' }}</text>
									<text v-if="idx === 0" class="map-route-item__badge">推荐</text>
									<view class="map-route-item__spacer" />
									<u-icon name="arrow-right" color="#C5CDD8" :size="13" />
								</view>
								<text v-if="plan.summary" class="map-route-item__path">{{ plan.summary }}</text>
								<view class="map-route-item__meta">
									<text v-if="plan.distanceText" class="map-route-item__chip">{{ plan.distanceText }}</text>
									<text v-if="plan.walkDistanceText" class="map-route-item__chip">步行 {{ plan.walkDistanceText }}</text>
									<text v-if="plan.transferCount != null && plan.transferCount > 0" class="map-route-item__chip">换乘 {{ plan.transferCount }} 次</text>
									<text v-if="plan.priceText" class="map-route-item__chip map-route-item__chip--price">{{ plan.priceText }}</text>
									<text v-if="!plan.summary && plan.tags && plan.tags.length" class="map-route-item__chip">{{ formatRouteTags(plan.tags) }}</text>
								</view>
							</view>
						</view>
					</template>
				</scroll-view>
			</view>

			<!-- 美食 / 停车：地图 -->
			<view v-else class="map-service-map-wrap">
				<!-- #ifdef H5 -->
				<div id="ryTencentMap" class="map-service-map"></div>
				<view v-if="mapError" class="map-service-map-error">
					<text class="map-service-map-error__text">{{ mapError }}</text>
				</view>
				<!-- #endif -->

				<!-- #ifndef H5 -->
				<map
					id="mainMap"
					class="map-service-map"
					:latitude="mapCenter.latitude"
					:longitude="mapCenter.longitude"
					:scale="mapScale"
					:markers="mapMarkers"
					:polyline="nativePolylines"
					show-location
					@markertap="onMarkerTap"
					@regionchange="onRegionChange"
				/>
				<!-- #endif -->

				<view class="map-service-tools">
					<view class="map-service-tool" @click="locateMe">
						<u-icon name="map-fill" color="#666666" :size="18" />
						<text class="map-service-tool__text">{{ ui.locateLabel }}</text>
					</view>
					<view class="map-service-tool" @click="searchNearbyPois">
						<u-icon name="reload" color="#666666" :size="18" />
						<text class="map-service-tool__text">刷新</text>
					</view>
				</view>

				<view v-if="loading" class="map-service-empty">
					<text class="map-service-empty__text">搜索中…</text>
				</view>
				<view v-else-if="!displayPois.length" class="map-service-empty">
					<u-empty mode="search" :text="emptyHint" />
				</view>

				<view v-if="activePoi" class="map-service-callout">
					<view class="map-service-callout__main">
						<view class="map-service-callout__info">
							<text class="map-service-callout__name">{{ activePoi.name }}</text>
							<view class="map-service-callout__meta">
								<view class="map-service-callout__tag" :style="{ backgroundColor: `${categoryMeta.color}14` }">
									<text class="map-service-callout__tag-text" :style="{ color: categoryMeta.color }">{{ activePoi.tag }}</text>
								</view>
								<text v-if="activePoi.distance" class="map-service-callout__distance">{{ activePoi.distance }}</text>
							</view>
							<text class="map-service-callout__addr">{{ activePoi.address }}</text>
						</view>
					</view>
					<view class="map-service-callout__actions">
						<u-button type="primary" size="small" :text="ui.navigate" @click="openNavigate" />
					</view>
				</view>
			</view>
		</view>

		<!-- 路线详情抽屉 -->
		<view v-if="showRouteDetail && selectedRoute" class="map-route-detail">
			<view class="map-route-detail__mask" @click="showRouteDetail = false" />
			<view class="map-route-detail__sheet">
				<view class="map-route-detail__handle" />
				<view class="map-route-detail__header">
					<view class="map-route-detail__header-main">
						<text class="map-route-detail__title">{{ selectedRoute.durationText || '路线详情' }}</text>
						<text class="map-route-detail__sub">
							<template v-if="selectedRoute.distanceText">{{ selectedRoute.distanceText }}</template>
							<template v-if="selectedRoute.walkDistanceText"> · 步行{{ selectedRoute.walkDistanceText }}</template>
							<template v-if="selectedRoute.priceText"> · {{ selectedRoute.priceText }}</template>
						</text>
						<text v-if="selectedRoute.summary" class="map-route-detail__summary">{{ selectedRoute.summary }}</text>
					</view>
					<view class="map-route-detail__close" @click="showRouteDetail = false">
						<u-icon name="close" color="#999" :size="16" />
					</view>
				</view>
				<scroll-view scroll-y class="map-route-detail__list" :show-scrollbar="false">
					<view
						v-for="(step, i) in selectedRoute.steps || []"
						:key="i"
						class="map-route-step"
					>
						<view class="map-route-step__rail">
							<view
								class="map-route-step__dot"
								:class="{
									'map-route-step__dot--transit': step.mode === 'TRANSIT',
									'map-route-step__dot--subway': step.mode === 'TRANSIT' && step.vehicle === 'SUBWAY'
								}"
							>
								<text>{{ stepLabel(step, i) }}</text>
							</view>
							<view v-if="i < ((selectedRoute.steps || []).length - 1)" class="map-route-step__line" />
						</view>
						<view class="map-route-step__body">
							<text class="map-route-step__text">{{ step.instruction || step.actDesc || '继续前行' }}</text>
							<text v-if="formatStepMeta(step)" class="map-route-step__meta">{{ formatStepMeta(step) }}</text>
						</view>
					</view>
					<view v-if="!(selectedRoute.steps && selectedRoute.steps.length)" class="map-route-step__empty">
						暂无详细步骤
					</view>
				</scroll-view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue';
import { onLoad, onShow, onReady } from '@dcloudio/uni-app';
import {
	fetchMapPlaceSearch,
	fetchMapSuggest,
	fetchMapDirection,
	fetchReverseGeocode
} from '@/common/api/map.js';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import {
	MAP_UI,
	MAP_CATEGORIES,
	MAP_DEFAULT_CENTER,
	getMapCategoryMeta,
	resolveMapCategory,
	getCategorySearchConfig,
	filterPoisByCategory,
	mapPoiVoToDisplay
} from '@/common/map.js';

// #ifdef H5
import { createTencentMap, getMarkerIconPath } from '@/common/tencent-map-h5.js';
// #endif


const ui = MAP_UI;
const categories = MAP_CATEGORIES;
	const routeModes = [
		{ key: 'transit', label: '公交' },
		{ key: 'driving', label: '驾车' },
		{ key: 'walking', label: '步行' },
		{ key: 'bicycling', label: '骑行' }
	];

const keyword = ref('');
const currentCategory = ref('navigation');
const activePoi = ref(null);
const mapScale = ref(16);
const mapCenter = ref({ ...MAP_DEFAULT_CENTER });
const pois = ref([]);
const loading = ref(false);
const locatedOnce = ref(false);
const mapError = ref('');

const fromText = ref('我的位置');
const toText = ref('');
const fromPoint = ref(null);
const toPoint = ref(null);
const suggestTarget = ref('to');
const suggestList = ref([]);
const suggestTimer = ref(null);
const routeMode = ref('transit');
const routePlans = ref([]);
const selectedRouteIndex = ref(0);
const routeLoading = ref(false);
const showRouteDetail = ref(false);

const categoryMeta = computed(() => getMapCategoryMeta(currentCategory.value));
const isNavigation = computed(() => currentCategory.value === 'navigation');
const displayPois = computed(() => (Array.isArray(pois.value) ? pois.value : []));
const selectedRoute = computed(() => routePlans.value[selectedRouteIndex.value] || null);
const poiSearchPlaceholder = computed(() => {
	if (currentCategory.value === 'food') return '搜索附近美食餐厅…';
	if (currentCategory.value === 'parking') return '搜索附近停车场…';
	return ui.searchPlaceholder;
});
const emptyHint = computed(() => {
	if (currentCategory.value === 'food') return '附近暂无美食餐厅';
	if (currentCategory.value === 'parking') return '附近暂无停车场';
	return ui.emptyText;
});

const mapMarkers = computed(() => {
	if (isNavigation.value) {
		const list = [];
		if (fromPoint.value) {
			list.push({
				id: 1,
				latitude: Number(fromPoint.value.latitude),
				longitude: Number(fromPoint.value.longitude),
				iconPath: resolveMarkerIcon(false),
				width: 32,
				height: 42,
				title: fromText.value || '起点'
			});
		}
		if (toPoint.value) {
			list.push({
				id: 2,
				latitude: Number(toPoint.value.latitude),
				longitude: Number(toPoint.value.longitude),
				iconPath: resolveMarkerIcon(true),
				width: 40,
				height: 52,
				title: toText.value || '终点'
			});
		}
		return list;
	}
	return displayPois.value
		.filter((poi) => poi.latitude != null && poi.longitude != null)
		.map((poi, index) => {
			const isActive = poi.id === activePoi.value?.id;
			return {
				id: index + 1,
				latitude: Number(poi.latitude),
				longitude: Number(poi.longitude),
				iconPath: resolveMarkerIcon(isActive),
				width: isActive ? 40 : 32,
				height: isActive ? 52 : 42,
				title: poi.name || '',
				callout: {
					content: poi.name || '',
					display: isActive ? 'ALWAYS' : 'BYCLICK',
					padding: 8,
					borderRadius: 6,
					fontSize: 12
				}
			};
		});
});

const nativePolylines = computed(() => {
	const points = selectedRoute.value?.points || [];
	if (points.length < 2) return [];
	return [{
		points: points.map((p) => ({
			latitude: Number(p.latitude),
			longitude: Number(p.longitude)
		})),
		color: '#2979FF',
		width: 6
	}];
});

function resolveMarkerIcon(isActive) {
	// #ifdef H5
	return getMarkerIconPath(currentCategory.value === 'navigation' ? 'navigation' : currentCategory.value, isActive);
	// #endif
	// #ifndef H5
	if (isActive) return '/static/map/pin-active.svg';
	const map = {
		food: '/static/map/pin-food.svg',
		parking: '/static/map/pin-parking.svg',
		navigation: '/static/map/pin-scenic.svg'
	};
	return map[currentCategory.value] || map.navigation;
	// #endif
}

function formatRouteTags(tags) {
	const dict = {
		LEAST_TIME: '时间短',
		LEAST_FEE: '少收费',
		AVOID_HIGHWAY: '不走高速',
		HIGHWAY_FIRST: '高速优先',
		LEAST_LIGHT: '红绿灯少'
	};
	return (tags || []).map((t) => dict[t] || t).slice(0, 2).join('·');
}

function formatStepMeta(step) {
	if (!step) return '';
	const parts = [];
	if (step.mode === 'TRANSIT') {
		if (step.stationCount) parts.push(`${step.stationCount}站`);
		if (step.duration) parts.push(`约${step.duration}分钟`);
		if (step.distanceText) parts.push(step.distanceText);
	} else {
		if (step.roadName) parts.push(step.roadName);
		if (step.distanceText) parts.push(step.distanceText);
		if (step.duration) parts.push(`约${step.duration}分钟`);
	}
	return parts.join(' · ');
}

function stepLabel(step, index) {
	if (step?.mode === 'TRANSIT') {
		return step.vehicle === 'SUBWAY' ? '轨' : '公';
	}
	return String(index + 1);
}

function onRouteModeChange(key) {
	if (routeMode.value === key) return;
	routeMode.value = key;
	routePlans.value = [];
	selectedRouteIndex.value = 0;
	showRouteDetail.value = false;
	if (fromPoint.value && toPoint.value) {
		planRoute();
	}
}

// #ifdef H5
let h5Map = null;
let h5MapInitPromise = null;
let syncingFromMap = false;

async function ensureH5Map() {
	if (h5Map) return h5Map;
	if (h5MapInitPromise) return h5MapInitPromise;
	mapError.value = '';
	h5MapInitPromise = (async () => {
		await nextTick();
		const el = document.getElementById('ryTencentMap');
		if (!el) {
			mapError.value = '地图容器未就绪';
			return null;
		}
		try {
			h5Map = await createTencentMap(el, {
				latitude: mapCenter.value.latitude,
				longitude: mapCenter.value.longitude,
				zoom: mapScale.value,
				categoryKey: currentCategory.value === 'navigation' ? 'navigation' : currentCategory.value
			});
			h5Map.onMarkerClick(({ poiId, id }) => {
				if (isNavigation.value) return;
				const target = displayPois.value.find((item) => String(item.id) === String(poiId || id));
				if (target) activePoi.value = target;
			});
			h5Map.onCenterChange((center) => {
				syncingFromMap = true;
				mapCenter.value = { latitude: center.latitude, longitude: center.longitude };
				nextTick(() => { syncingFromMap = false; });
			});
			syncH5Overlay();
			return h5Map;
		} catch (e) {
			mapError.value = e?.message || '地图加载失败';
			h5Map = null;
			return null;
		} finally {
			h5MapInitPromise = null;
		}
	})();
	return h5MapInitPromise;
}

function syncH5Overlay() {
	if (!h5Map || isNavigation.value) return;
	h5Map.clearRoute();
	h5Map.setMarkers(displayPois.value, activePoi.value?.id || '', currentCategory.value);
}

function focusActivePoiOnH5Map() {
	if (!h5Map || !activePoi.value) return;
	syncingFromMap = true;
	h5Map.setCenter(activePoi.value.latitude, activePoi.value.longitude, mapScale.value);
	nextTick(() => { syncingFromMap = false; });
}

watch([displayPois, activePoi, selectedRoute, fromPoint, toPoint, currentCategory], () => {
	syncH5Overlay();
});
// #endif

watch(displayPois, (list) => {
	if (!list.length) {
		activePoi.value = null;
		return;
	}
	if (!list.some((item) => item.id === activePoi.value?.id)) {
		activePoi.value = null;
	}
});

function clearSuggest() {
	suggestList.value = [];
}

function onFromInput() {
	fromPoint.value = null;
	debounceSuggest('from', fromText.value);
}

function onToInput() {
	toPoint.value = null;
	debounceSuggest('to', toText.value);
}

function debounceSuggest(target, text) {
	suggestTarget.value = target;
	if (suggestTimer.value) clearTimeout(suggestTimer.value);
	const q = String(text || '').trim();
	if (!q || q === '我的位置') {
		clearSuggest();
		return;
	}
	suggestTimer.value = setTimeout(() => loadSuggest(q), 280);
}

async function loadSuggest(q) {
	try {
		const res = await fetchMapSuggest({
			keyword: q,
			lat: mapCenter.value.latitude,
			lng: mapCenter.value.longitude
		});
		if (res.code !== 0) {
			clearSuggest();
			return;
		}
		suggestList.value = (res.data || []).map((item) => ({
			id: item.id,
			name: item.name,
			address: item.address,
			latitude: Number(item.latitude),
			longitude: Number(item.longitude)
		}));
	} catch (e) {
		clearSuggest();
	}
}

function pickSuggest(item) {
	clearSuggest();
	const point = {
		latitude: Number(item.latitude),
		longitude: Number(item.longitude)
	};
	if (suggestTarget.value === 'from') {
		fromText.value = item.name;
		fromPoint.value = point;
	} else {
		toText.value = item.name;
		toPoint.value = point;
	}
}

async function useMyLocationAsFrom() {
	uni.getLocation({
		type: 'gcj02',
		success: async (res) => {
			fromPoint.value = { latitude: res.latitude, longitude: res.longitude };
			fromText.value = '我的位置';
			mapCenter.value = { latitude: res.latitude, longitude: res.longitude };
			try {
				const geo = await fetchReverseGeocode({ lat: res.latitude, lng: res.longitude });
				if (geo.code === 0 && geo.data?.address) {
					fromText.value = geo.data.address;
				}
			} catch (e) { /* keep 我的位置 */ }
		},
		fail: () => uni.showToast({ title: ui.locateFail, icon: 'none' })
	});
}

function swapEnds() {
	const tText = fromText.value;
	const tPoint = fromPoint.value;
	fromText.value = toText.value;
	fromPoint.value = toPoint.value;
	toText.value = tText;
	toPoint.value = tPoint;
}

async function resolveEndpoint(text, point, role) {
	if (point?.latitude != null && point?.longitude != null) {
		return point;
	}
	const q = String(text || '').trim();
	if (!q) {
		throw new Error(role === 'from' ? '请输入起点' : '请输入终点');
	}
	if (q === '我的位置') {
		return await new Promise((resolve, reject) => {
			uni.getLocation({
				type: 'gcj02',
				success: (res) => resolve({ latitude: res.latitude, longitude: res.longitude }),
				fail: () => reject(new Error('定位失败'))
			});
		});
	}
	const res = await fetchMapSuggest({
		keyword: q,
		lat: mapCenter.value.latitude,
		lng: mapCenter.value.longitude
	});
	if (res.code !== 0 || !res.data?.length) {
		throw new Error(`未找到${role === 'from' ? '起点' : '终点'}`);
	}
	const first = res.data[0];
	return {
		latitude: Number(first.latitude),
		longitude: Number(first.longitude)
	};
}

async function planRoute() {
	if (routeLoading.value) return;
	clearSuggest();
	routeLoading.value = true;
	try {
		const from = await resolveEndpoint(fromText.value, fromPoint.value, 'from');
		const to = await resolveEndpoint(toText.value, toPoint.value, 'to');
		fromPoint.value = from;
		toPoint.value = to;
		const res = await fetchMapDirection({
			fromLat: from.latitude,
			fromLng: from.longitude,
			toLat: to.latitude,
			toLng: to.longitude,
			mode: routeMode.value,
			multiPlan: true
		});
		if (res.code !== 0) {
			throw new Error(res.msg || '路线规划失败');
		}
		routePlans.value = Array.isArray(res.data) ? res.data : [];
		selectedRouteIndex.value = 0;
		showRouteDetail.value = false;
		if (!routePlans.value.length) {
			uni.showToast({ title: '暂无可用路线', icon: 'none' });
		}
	} catch (e) {
		routePlans.value = [];
		uni.showToast({ title: e?.message || '路线规划失败', icon: 'none' });
	} finally {
		routeLoading.value = false;
	}
}

function selectRoute(idx) {
	selectedRouteIndex.value = idx;
	// 公交：查看分步详情；驾车/步行/骑行：直接唤起地图导航
	if (routeMode.value === 'transit') {
		showRouteDetail.value = true;
		return;
	}
	openRouteNavigate();
}

function openRouteNavigate() {
	const dest = toPoint.value;
	if (dest?.latitude == null || dest?.longitude == null) {
		uni.showToast({ title: '缺少终点坐标', icon: 'none' });
		return;
	}
	uni.openLocation({
		latitude: Number(dest.latitude),
		longitude: Number(dest.longitude),
		name: String(toText.value || '目的地').trim() || '目的地',
		address: String(toText.value || '').trim(),
		fail: () => uni.showToast({ title: '导航失败', icon: 'none' })
	});
}

async function searchNearbyPois() {
	if (isNavigation.value || loading.value) return;
	const lat = mapCenter.value?.latitude;
	const lng = mapCenter.value?.longitude;
	if (lat == null || lng == null) {
		uni.showToast({ title: '请先定位', icon: 'none' });
		return;
	}
	loading.value = true;
	try {
		const searchConfig = getCategorySearchConfig(currentCategory.value);
		const customKeyword = keyword.value.trim();
		// 美食/停车强制 keyword 搜索，避免 explore 返回小区/行政地名
		const res = await fetchMapPlaceSearch({
			keyword: customKeyword || searchConfig.keyword,
			lat,
			lng,
			radius: 1000,
			filter: searchConfig.filter,
			mode: 'search',
			autoExtend: 0,
			page: 1,
			limit: 20
		});
		if (res.code !== 0) {
			throw new Error(res.msg || '搜索失败');
		}
		const mapped = (Array.isArray(res.data) ? res.data : [])
			.map(mapPoiVoToDisplay)
			.filter((item) => item.latitude != null && item.longitude != null);
		// 美食/停车：严格只保留对应分类
		pois.value = filterPoisByCategory(mapped, currentCategory.value);
		if (!pois.value.length) {
			activePoi.value = null;
			uni.showToast({ title: emptyHint.value, icon: 'none' });
		} else {
			activePoi.value = pois.value[0];
		}
		// #ifdef H5
		await ensureH5Map();
		syncH5Overlay();
		if (pois.value.length > 1) h5Map?.fitPois(pois.value.slice(0, 12));
		else focusActivePoiOnH5Map();
		// #endif
	} catch (e) {
		pois.value = [];
		uni.showToast({ title: e?.message || '搜索失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function switchCategory(key) {
	currentCategory.value = key;
	activePoi.value = null;
	keyword.value = '';
	clearSuggest();
	routePlans.value = [];
	showRouteDetail.value = false;
	uni.setNavigationBarTitle({
		title: getMapCategoryMeta(key).label,
		fail: () => {}
	});
	if (key === 'navigation') {
		pois.value = [];
		// #ifdef H5
		h5Map?.clearRoute();
		// #endif
		return;
	}
	// #ifdef H5
	nextTick(() => ensureH5Map().then(() => searchNearbyPois()));
	// #endif
	// #ifndef H5
	searchNearbyPois();
	// #endif
}

function onSearch() {
	activePoi.value = null;
	searchNearbyPois();
}

function onMarkerTap(event) {
	if (isNavigation.value) return;
	const markerId = event?.detail?.markerId;
	if (!markerId) return;
	const index = markerId - 1;
	if (index >= 0 && index < displayPois.value.length) {
		activePoi.value = displayPois.value[index];
	}
}

function onRegionChange(e) {
	const type = e?.type || e?.detail?.type;
	const causedBy = e?.causedBy || e?.detail?.causedBy;
	if (type !== 'end') return;
	if (causedBy !== 'drag' && causedBy !== 'scale' && causedBy !== 'update') return;
	const detail = e?.detail || {};
	const centerLocation = detail.centerLocation || detail.center;
	if (centerLocation?.latitude != null && centerLocation?.longitude != null) {
		mapCenter.value = {
			latitude: Number(centerLocation.latitude),
			longitude: Number(centerLocation.longitude)
		};
	}
}

function locateMe() {
	uni.showToast({ title: ui.locating, icon: 'none' });
	uni.getLocation({
		type: 'gcj02',
		success: async (res) => {
			mapCenter.value = { latitude: res.latitude, longitude: res.longitude };
			mapScale.value = 16;
			if (isNavigation.value && (!fromPoint.value || fromText.value === '我的位置')) {
				fromPoint.value = { latitude: res.latitude, longitude: res.longitude };
				fromText.value = '我的位置';
			}
			if (isNavigation.value) return;
			// #ifdef H5
			await ensureH5Map();
			h5Map?.setCenter(res.latitude, res.longitude, 16);
			// #endif
			searchNearbyPois();
		},
		fail: () => uni.showToast({ title: ui.locateFail, icon: 'none' })
	});
}

function openNavigate() {
	if (!activePoi.value) return;
	uni.openLocation({
		latitude: Number(activePoi.value.latitude),
		longitude: Number(activePoi.value.longitude),
		name: activePoi.value.name,
		address: activePoi.value.address,
		fail: () => uni.showToast({ title: '导航失败', icon: 'none' })
	});
}

onLoad((options) => {
	const type = resolveMapCategory(options?.type || options?.category);
	currentCategory.value = type;
	uni.setNavigationBarTitle({
		title: getMapCategoryMeta(type).label,
		fail: () => {}
	});
	if (options?.lat != null && options?.lng != null) {
		mapCenter.value = {
			latitude: Number(options.lat),
			longitude: Number(options.lng)
		};
	}
	if (options?.keyword) {
		keyword.value = decodeURIComponent(options.keyword);
	}
});

onReady(() => {
	// #ifdef H5
	if (!isNavigation.value) ensureH5Map();
	// #endif
});

onShow(() => {
	if (locatedOnce.value) {
		if (!isNavigation.value) searchNearbyPois();
		return;
	}
	locatedOnce.value = true;
	uni.getLocation({
		type: 'gcj02',
		success: async (res) => {
			mapCenter.value = { latitude: res.latitude, longitude: res.longitude };
			fromPoint.value = { latitude: res.latitude, longitude: res.longitude };
			fromText.value = '我的位置';
			// #ifdef H5
			if (!isNavigation.value) {
				await ensureH5Map();
				h5Map?.setCenter(res.latitude, res.longitude, 16);
			}
			// #endif
			if (!isNavigation.value) searchNearbyPois();
		},
		fail: () => {
			if (!isNavigation.value) searchNearbyPois();
		}
	});
});

onBeforeUnmount(() => {
	if (suggestTimer.value) clearTimeout(suggestTimer.value);
	// #ifdef H5
	h5MapInitPromise = null;
	if (h5Map) {
		h5Map.destroy();
		h5Map = null;
	}
	// #endif
});
</script>

<style lang="scss" scoped>
	/* 导航页主色：清爽蓝，替代压抑暖橙 */
	$nav-blue: #2f6bff;
	$nav-blue-soft: #eaf1ff;
	$nav-blue-mid: #6b93ff;
	$nav-page: #f4f7fb;
	$nav-text: #1a2332;
	$nav-muted: #8b95a5;

	.map-service-page {
		height: 100vh;
		height: 100dvh;
		display: flex;
		flex-direction: column;
		background-color: $nav-page;
		overflow: hidden;
	}

	.map-service-body {
		flex: 1;
		display: flex;
		flex-direction: column;
		min-height: 0;
		overflow: hidden;
	}

	.map-service-tabs {
		flex-shrink: 0;
		background-color: #fff;
	}

	.map-service-tabs__track {
		display: flex;
		flex-direction: row;
		align-items: center;
		padding: 0 var(--ry-page-padding-x);
	}

	.map-service-tab {
		position: relative;
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		height: 84rpx;

		&__text {
			font-size: 28rpx;
			color: #667085;
		}

		&__line {
			position: absolute;
			left: 50%;
			bottom: 10rpx;
			width: 36rpx;
			height: 5rpx;
			border-radius: 999rpx;
			background: $nav-blue;
			transform: translateX(-50%);
		}

		&--active .map-service-tab__text {
			font-size: 30rpx;
			font-weight: 700;
			color: $nav-text;
		}
	}

	.map-service-search {
		flex-shrink: 0;
		padding: 16rpx var(--ry-page-padding-x) 12rpx;
		background-color: #fff;
	}

	.map-nav-panel {
		flex-shrink: 0;
		padding: 20rpx 28rpx 16rpx;
		background: #fff;
		position: relative;
		z-index: 6;
	}

	/* 出行方式：纯文字分段 */
	.map-nav-modes {
		display: flex;
		padding: 6rpx;
		margin-bottom: 20rpx;
		border-radius: 18rpx;
		background: $nav-page;
	}

	.map-nav-mode {
		flex: 1;
		height: 64rpx;
		border-radius: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: background 0.2s ease, color 0.2s ease;

		&__text {
			font-size: 26rpx;
			color: $nav-muted;
			font-weight: 500;
		}

		&--active {
			background: #fff;
			box-shadow: 0 2rpx 12rpx rgba(47, 107, 255, 0.1);

			.map-nav-mode__text {
				color: $nav-blue;
				font-weight: 700;
			}
		}
	}

	/* 起终点：两行对齐，右侧统一图标按钮 */
	.map-nav-ends {
		padding: 8rpx 8rpx 8rpx 20rpx;
		border-radius: 24rpx;
		background: $nav-page;
	}

	.map-nav-ends__main {
		position: relative;
	}

	.map-nav-ends__rail-line {
		position: absolute;
		left: 7rpx;
		top: 52rpx;
		bottom: 52rpx;
		width: 2rpx;
		background: repeating-linear-gradient(
			180deg,
			#c5d0e0 0,
			#c5d0e0 5rpx,
			transparent 5rpx,
			transparent 10rpx
		);
		pointer-events: none;
	}

	.map-nav-row {
		position: relative;
		z-index: 1;
		display: flex;
		align-items: center;
		gap: 16rpx;
		min-height: 88rpx;
		padding: 4rpx 0;
	}

	.map-nav-row__dot {
		width: 16rpx;
		height: 16rpx;
		border-radius: 50%;
		flex-shrink: 0;

		&--start {
			background: #22c55e;
			box-shadow: 0 0 0 6rpx rgba(34, 197, 94, 0.16);
		}

		&--end {
			background: $nav-blue;
			box-shadow: 0 0 0 6rpx rgba(47, 107, 255, 0.16);
		}
	}

	.map-nav-row__input {
		flex: 1;
		min-width: 0;
		height: 52rpx;
		font-size: 30rpx;
		font-weight: 600;
		color: $nav-text;
		line-height: 52rpx;
	}

	.map-nav-row__ph {
		color: #a8b2c1;
		font-weight: 400;
	}

	.map-nav-row__btn {
		width: 64rpx;
		height: 64rpx;
		border-radius: 18rpx;
		background: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		box-shadow: 0 2rpx 10rpx rgba(47, 107, 255, 0.08);

		&:active {
			opacity: 0.75;
			transform: scale(0.94);
		}
	}

	.map-nav-swap-ico {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		height: 36rpx;
		margin-top: -4rpx;

		:deep(.u-icon) {
			line-height: 1 !important;
			height: 16rpx !important;
		}
	}

	.map-nav-suggest {
		margin-top: 12rpx;
		max-height: 320rpx;
		overflow: auto;
		border-radius: 20rpx;
		background: $nav-page;
	}

	.map-nav-suggest__item {
		display: flex;
		align-items: flex-start;
		gap: 14rpx;
		padding: 18rpx 20rpx;

		&:active {
			background: rgba(47, 107, 255, 0.04);
		}
	}

	.map-nav-suggest__icon {
		width: 40rpx;
		height: 40rpx;
		border-radius: 12rpx;
		background: $nav-blue-soft;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		margin-top: 2rpx;
	}

	.map-nav-suggest__main {
		flex: 1;
		min-width: 0;
	}

	.map-nav-suggest__name {
		display: block;
		font-size: 28rpx;
		color: $nav-text;
		font-weight: 500;
	}

	.map-nav-suggest__addr {
		display: block;
		margin-top: 4rpx;
		font-size: 22rpx;
		color: $nav-muted;
	}

	.map-nav-plan {
		margin-top: 20rpx;
		height: 88rpx;
		border-radius: 24rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		background: linear-gradient(135deg, #4d82ff 0%, $nav-blue 100%);
		box-shadow: 0 12rpx 28rpx rgba(47, 107, 255, 0.28);

		&:active {
			opacity: 0.92;
			transform: scale(0.985);
		}

		&--loading {
			opacity: 0.7;
			pointer-events: none;
		}

		&__text {
			font-size: 30rpx;
			font-weight: 600;
			color: #fff;
			letter-spacing: 2rpx;
		}
	}

	.map-nav-results {
		flex: 1;
		min-height: 0;
		display: flex;
		flex-direction: column;
		background: $nav-page;
	}

	.map-nav-results__scroll {
		flex: 1;
		height: 100%;
		padding: 8rpx 28rpx calc(env(safe-area-inset-bottom) + 28rpx);
		box-sizing: border-box;
	}

	.map-nav-results__state {
		padding: 72rpx 28rpx 40rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 10rpx;
	}

	.map-nav-results__illus {
		width: 96rpx;
		height: 96rpx;
		border-radius: 32rpx;
		background: #fff;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 8rpx;
		box-shadow: 0 8rpx 24rpx rgba(47, 107, 255, 0.08);
	}

	.map-nav-results__pulse {
		width: 40rpx;
		height: 40rpx;
		border-radius: 50%;
		border: 3rpx solid rgba(47, 107, 255, 0.18);
		border-top-color: $nav-blue;
		animation: map-nav-spin 0.75s linear infinite;
		margin-bottom: 12rpx;
	}

	@keyframes map-nav-spin {
		to { transform: rotate(360deg); }
	}

	.map-nav-results__title {
		font-size: 28rpx;
		font-weight: 600;
		color: $nav-text;
	}

	.map-nav-results__hint {
		font-size: 24rpx;
		color: $nav-muted;
	}

	.map-nav-results__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 8rpx 4rpx 20rpx;
	}

	.map-nav-results__count {
		font-size: 28rpx;
		font-weight: 700;
		color: $nav-text;
	}

	.map-nav-results__count-num {
		font-size: 24rpx;
		color: $nav-muted;
		font-weight: 500;
	}

	.map-route-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.map-route-item {
		position: relative;
		padding: 28rpx 28rpx 24rpx 32rpx;
		border-radius: 24rpx;
		background: #fff;
		overflow: hidden;
		animation: map-route-in 0.32s ease both;

		&:active {
			opacity: 0.92;
		}

		&--best {
			background: #fff;

			&::before {
				content: '';
				position: absolute;
				left: 0;
				top: 0;
				bottom: 0;
				width: 6rpx;
				background: $nav-blue;
				border-radius: 0 6rpx 6rpx 0;
			}
		}

		&--active {
			background: #f7f9ff;
		}

		&__top {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__time {
			font-size: 40rpx;
			font-weight: 700;
			color: $nav-text;
			letter-spacing: -1rpx;
			line-height: 1.1;
		}

		&__badge {
			padding: 4rpx 14rpx;
			border-radius: 8rpx;
			background: $nav-blue;
			color: #fff;
			font-size: 20rpx;
			font-weight: 600;
			line-height: 1.4;
		}

		&__spacer {
			flex: 1;
		}

		&__path {
			display: block;
			margin-top: 14rpx;
			font-size: 26rpx;
			color: #5a6578;
			line-height: 1.55;
			word-break: break-all;
		}

		&__meta {
			display: flex;
			flex-wrap: wrap;
			gap: 10rpx;
			margin-top: 18rpx;
		}

		&__chip {
			padding: 6rpx 14rpx;
			border-radius: 10rpx;
			background: $nav-page;
			font-size: 22rpx;
			color: $nav-muted;
			line-height: 1.4;

			&--price {
				background: $nav-blue-soft;
				color: $nav-blue;
				font-weight: 600;
			}
		}
	}

	@keyframes map-route-in {
		from {
			opacity: 0;
			transform: translateY(12rpx);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}

	.map-service-map-wrap {
		position: relative;
		flex: 1;
		width: 100%;
		min-height: 0;
		overflow: hidden;
		background-color: #e8eef5;
	}

	/* 绝对定位切断 canvas 与容器互相撑大，避免 GL 纹理超限 */
	.map-service-map {
		position: absolute;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		width: 100%;
		height: 100%;
	}

	.map-service-map-error {
		position: absolute;
		left: 24rpx;
		right: 24rpx;
		top: 40%;
		z-index: 2;
		padding: 24rpx;
		border-radius: 16rpx;
		background: rgba(255, 255, 255, 0.95);
	}

	.map-service-map-error__text {
		font-size: 24rpx;
		color: #c0392b;
		text-align: center;
	}

	.map-service-tools {
		position: absolute;
		right: 24rpx;
		top: 24rpx;
		z-index: 3;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.map-service-tool {
		width: 112rpx;
		padding: 16rpx 12rpx;
		border-radius: 16rpx;
		background-color: #ffffff;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.1);
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;

		&__text {
			font-size: 20rpx;
			color: #666666;
		}
	}

	.map-service-empty {
		position: absolute;
		left: var(--ry-page-padding-x);
		right: var(--ry-page-padding-x);
		bottom: calc(env(safe-area-inset-bottom) + 32rpx);
		z-index: 4;
		padding: 32rpx 24rpx;
		border-radius: var(--ry-radius-lg);
		background-color: rgba(255, 255, 255, 0.94);
		box-shadow: var(--ry-card-shadow);
		display: flex;
		justify-content: center;
	}

	.map-service-empty__text {
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}

	.map-service-callout {
		position: absolute;
		left: var(--ry-page-padding-x);
		right: var(--ry-page-padding-x);
		bottom: calc(env(safe-area-inset-bottom) + 24rpx);
		z-index: 4;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		padding: 24rpx;
		border-radius: var(--ry-radius-lg);
		background-color: var(--ry-card-bg);
		box-shadow: var(--ry-card-shadow);
	}

	.map-service-callout__name {
		font-size: 30rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	.map-service-callout__meta {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-top: 8rpx;
	}

	.map-service-callout__tag {
		padding: 4rpx 12rpx;
		border-radius: var(--ry-radius-pill);
	}

	.map-service-callout__tag-text {
		font-size: 22rpx;
	}

	.map-service-callout__distance {
		margin-left: auto;
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	.map-service-callout__addr {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.map-route-detail {
		position: fixed;
		inset: 0;
		z-index: 20;
	}

	.map-route-detail__mask {
		position: absolute;
		inset: 0;
		background: rgba(20, 28, 48, 0.4);
	}

	.map-route-detail__sheet {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		max-height: 78vh;
		background: #fff;
		border-radius: 32rpx 32rpx 0 0;
		padding: 12rpx 28rpx 24rpx;
		padding-bottom: calc(env(safe-area-inset-bottom) + 28rpx);
		display: flex;
		flex-direction: column;
		box-shadow: 0 -16rpx 48rpx rgba(26, 35, 50, 0.1);
		animation: map-sheet-up 0.28s ease;
	}

	@keyframes map-sheet-up {
		from { transform: translateY(40rpx); opacity: 0.6; }
		to { transform: translateY(0); opacity: 1; }
	}

	.map-route-detail__handle {
		width: 64rpx;
		height: 8rpx;
		border-radius: 999rpx;
		background: #d8dee8;
		margin: 4rpx auto 16rpx;
	}

	.map-route-detail__header {
		display: flex;
		justify-content: space-between;
		align-items: flex-start;
		gap: 16rpx;
		margin-bottom: 20rpx;
	}

	.map-route-detail__header-main {
		flex: 1;
		min-width: 0;
	}

	.map-route-detail__title {
		display: block;
		font-size: 40rpx;
		font-weight: 700;
		color: $nav-text;
		letter-spacing: -0.5rpx;
	}

	.map-route-detail__sub {
		display: block;
		margin-top: 8rpx;
		font-size: 24rpx;
		color: $nav-muted;
	}

	.map-route-detail__summary {
		display: block;
		margin-top: 12rpx;
		font-size: 26rpx;
		color: $nav-blue;
		line-height: 1.45;
	}

	.map-route-detail__close {
		width: 56rpx;
		height: 56rpx;
		border-radius: 50%;
		background: $nav-page;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.map-route-detail__list {
		flex: 1;
		max-height: 58vh;
	}

	.map-route-step {
		display: flex;
		gap: 18rpx;
		padding: 0 0 8rpx;
	}

	.map-route-step__rail {
		width: 44rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		flex-shrink: 0;
	}

	.map-route-step__dot {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		background: $nav-blue-soft;
		color: $nav-blue;
		font-size: 20rpx;
		font-weight: 600;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		z-index: 1;

		&--transit {
			background: #e8f8ef;
			color: #1fa85a;
		}

		&--subway {
			background: #eaf2ff;
			color: $nav-blue;
		}
	}

	.map-route-step__line {
		flex: 1;
		width: 2rpx;
		min-height: 28rpx;
		margin: 4rpx 0;
		background: #e2e8f0;
	}

	.map-route-step__body {
		flex: 1;
		min-width: 0;
		padding: 4rpx 0 20rpx;
	}

	.map-route-step__text {
		display: block;
		font-size: 28rpx;
		color: $nav-text;
		line-height: 1.45;
	}

	.map-route-step__meta {
		display: block;
		margin-top: 6rpx;
		font-size: 22rpx;
		color: $nav-muted;
	}

	.map-route-step__empty {
		padding: 48rpx;
		text-align: center;
		color: $nav-muted;
		font-size: 26rpx;
	}
</style>
