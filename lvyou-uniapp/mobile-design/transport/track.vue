<template>
	<view class="track-page">
		<!-- 背景装饰 -->
		<view class="track-bg">
			<view class="track-bg__gradient"></view>
			<view class="track-bg__circles">
				<view class="track-bg__circle track-bg__circle--1"></view>
				<view class="track-bg__circle track-bg__circle--2"></view>
				<view class="track-bg__circle track-bg__circle--3"></view>
			</view>
		</view>

		<!-- 加载状态 -->
		<view v-if="loading && !routeName" class="track-loading">
			<view class="track-loading__spinner"></view>
			<text class="track-loading__text">正在追踪车辆...</text>
		</view>

		<!-- 主要内容 -->
		<template v-else>
			<!-- 地图区域 - 扩大并增强视觉效果 -->
			<view class="track-map-section">
				<view class="track-map-container">
					<!-- #ifdef H5 -->
					<view class="track-map-placeholder">
						<!-- 背景动态网格 -->
						<view class="track-map__grid">
							<view class="track-map__grid-line" v-for="i in 6" :key="'v'+i"></view>
							<view class="track-map__grid-line track-map__grid-line--h" v-for="i in 4" :key="'h'+i"></view>
						</view>
						
						<!-- 中心车辆图标 -->
						<view class="track-map__vehicle">
							<view class="track-map__vehicle-pulse"></view>
							<view class="track-map__vehicle-icon">
								<u-icon name="car-fill" :color="accentColor" :size="32" />
							</view>
						</view>
						
						<!-- 轨迹路径装饰 -->
						<view class="track-map__path">
							<view class="track-map__path-dot" v-for="i in 5" :key="i"></view>
						</view>
					</view>
					<!-- #endif -->

					<!-- #ifndef H5 -->
					<map
						class="track-map"
						:latitude="mapCenter.latitude"
						:longitude="mapCenter.longitude"
						:scale="14"
						:markers="mapMarkers"
						:polyline="mapPolyline"
						show-location
					/>
					<!-- #endif -->
				</view>

				<!-- 地图上的浮动信息卡片 -->
				<view class="track-float-card">
					<view class="track-float-card__header">
						<view class="track-float-card__icon">
							<u-icon name="map-fill" color="#FFFFFF" :size="20" />
						</view>
						<text class="track-float-card__text">实时位置追踪</text>
					</view>
				</view>
			</view>

			<!-- 车辆信息卡片组 -->
			<view class="track-info-section">
				<!-- 主信息卡 - 玻璃态设计 -->
				<view class="track-main-card">
					<view class="track-main-card__deco">
						<view class="track-main-card__deco-circle"></view>
					</view>
					
					<view class="track-main-card__header">
						<view class="track-main-card__route">
							<view class="track-main-card__route-icon">
								<u-icon name="car-fill" :color="accentColor" :size="24" />
							</view>
							<text class="track-main-card__route-name">{{ routeName }}</text>
						</view>
						<view v-if="trackInfo.currentStation" class="track-status-badge">
							<view class="track-status-badge__dot"></view>
							<text class="track-status-badge__text">{{ statusLabel }}</text>
						</view>
					</view>

					<!-- 站点信息 - 时间轴设计 -->
					<view class="track-stations">
						<view v-if="currentStationDisplay" class="track-station">
							<view class="track-station__marker track-station__marker--current">
								<view class="track-station__marker-dot"></view>
							</view>
							<view class="track-station__content">
								<text class="track-station__label">当前站</text>
								<text class="track-station__name">{{ currentStationDisplay }}</text>
							</view>
						</view>

						<view v-if="nextStationDisplay" class="track-station">
							<view class="track-station__marker track-station__marker--next">
								<view class="track-station__marker-dot"></view>
							</view>
							<view class="track-station__content">
								<text class="track-station__label">下一站</text>
								<text class="track-station__name">{{ nextStationDisplay }}</text>
								<text v-if="remainingDisplay" class="track-station__eta">{{ remainingDisplay }}</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 线路站点列表 -->
				<view class="track-route-card">
					<view class="track-route-card__header">
						<view class="track-route-card__title">
							<u-icon name="list" :color="accentColor" :size="20" />
							<text class="track-route-card__title-text">途经站点</text>
						</view>
						<text class="track-route-card__count">共{{ allStations.length }}站</text>
					</view>

					<view class="track-route-stations">
						<view 
							v-for="(station, index) in displayedStations" 
							:key="index"
							class="track-route-station"
							:class="{
								'track-route-station--current': station.isCurrent,
								'track-route-station--passed': station.isPassed
							}"
						>
							<view class="track-route-station__line">
								<view class="track-route-station__dot"></view>
								<view v-if="index < displayedStations.length - 1" class="track-route-station__connector"></view>
							</view>
							<view class="track-route-station__content">
								<view class="track-route-station__row">
									<text class="track-route-station__name">{{ station.name }}</text>
									<text v-if="station.time" class="track-route-station__time">{{ station.time }}</text>
								</view>
								<text v-if="station.isCurrent" class="track-route-station__label">车辆当前位置</text>
							</view>
						</view>
					</view>

					<!-- 展开/收起按钮 -->
					<view class="track-route-toggle" @click="toggleAllStations">
						<text class="track-route-toggle__text">
							{{ showAllStations ? '收起站点' : `查看全部站点 (${allStations.length}站)` }}
						</text>
						<u-icon 
							:name="showAllStations ? 'arrow-up' : 'arrow-down'" 
							:color="accentColor" 
							:size="16" 
						/>
					</view>
				</view>

				<!-- 实时动态列表 -->
				<view v-if="realtimeUpdates.length" class="track-updates">
					<view class="track-updates__title">
						<view class="track-updates__title-line"></view>
						<text class="track-updates__title-text">实时动态</text>
						<view class="track-updates__title-line"></view>
					</view>

					<view
						v-for="(item, index) in realtimeUpdates"
						:key="item.id"
						class="track-update-item"
						:style="{ animationDelay: (index * 0.1) + 's' }"
					>
						<view class="track-update-item__timeline">
							<view class="track-update-item__timeline-dot"></view>
							<view v-if="index < realtimeUpdates.length - 1" class="track-update-item__timeline-line"></view>
						</view>
						<view class="track-update-item__card">
							<view class="track-update-item__header">
								<text class="track-update-item__label">{{ item.label }}</text>
								<text class="track-update-item__time">{{ item.time }}</text>
							</view>
							<text class="track-update-item__value">{{ item.value }}</text>
						</view>
					</view>
				</view>

				<!-- 空状态 - 增强视觉设计 -->
				<view v-else-if="!loading" class="track-empty">
					<view class="track-empty__icon">
						<u-icon name="bell" :color="accentColor" :size="48" />
						<view class="track-empty__icon-ring"></view>
					</view>
					<text class="track-empty__text">暂无实时动态</text>
					<text class="track-empty__hint">车辆运行信息将实时更新</text>
				</view>
			</view>
		</template>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { fetchTransportTrack } from '@/common/api/transport.js';
import { parseTransportOptions } from '@/common/transport-context.js';

const accentColor = getModuleColor('transport');

const routeId = ref('');
const routeContext = ref({ city: '', station: '', direction: 1 });
const routeName = ref('');
const statusLabel = ref('运行中');
const trackInfo = ref({});
const realtimeUpdates = ref([]);
const polylinePoints = ref([]);
const loading = ref(false);
const showAllStations = ref(false);

// 当前站显示(优先使用实际数据,否则使用mock)
const currentStationDisplay = computed(() => {
	const station = trackInfo.value.currentStation;
	// 确保返回的是字符串,且不是纯数字
	if (station && typeof station === 'string' && station.trim() && isNaN(station)) {
		return station;
	}
	// 没有有效数据时,使用mock的当前站
	const currentMock = allStations.value.find(s => s.isCurrent);
	return currentMock ? currentMock.name : '';
});

// 是否为「剩余站数」类文案(不能当作真实站名)
function isRemainingStatusText(text) {
	const value = String(text || '').trim();
	if (!value) return true;
	if (value === '暂无车辆') return true;
	if (/还有|剩余/.test(value)) return true;
	if (/^\d+\s*站$/.test(value)) return true;
	return false;
}

// 下一站显示真实站名(过滤剩余站数文案)
const nextStationDisplay = computed(() => {
	const station = trackInfo.value.nextStation;
	if (station && typeof station === 'string' && station.trim() && isNaN(station) && !isRemainingStatusText(station)) {
		return station.trim();
	}
	const currentIndex = allStations.value.findIndex(s => s.isCurrent);
	if (currentIndex >= 0 && currentIndex < allStations.value.length - 1) {
		return allStations.value[currentIndex + 1].name;
	}
	return '';
});

// 剩余站数 + ETA，例如「还有 7 站 · 约17分钟」
const remainingDisplay = computed(() => {
	const eta = String(trackInfo.value.eta || '').trim();
	if (eta) return eta;
	const next = String(trackInfo.value.nextStation || '').trim();
	if (isRemainingStatusText(next) && next !== '暂无车辆') {
		return next;
	}
	return '';
});

// 完整站点列表
const allStations = computed(() => {
	const current = currentStationDisplay.value;
	const next = nextStationDisplay.value;
	
	// 默认mock数据
	return [
		{ name: '火车站', isPassed: true, time: '已过站' },
		{ name: '五一广场', isPassed: true, time: '已过站' },
		{ name: '黄兴广场', isPassed: true, time: '已过站' },
		{ name: current || '橘子洲', isCurrent: true, time: '当前站' },
		{ name: next || '湖南大学', isPassed: false, time: '2分钟后' },
		{ name: '麓山南路', isPassed: false, time: '5分钟后' },
		{ name: '师大附中', isPassed: false, time: '8分钟后' },
		{ name: '岳麓山南', isPassed: false, time: '11分钟后' }
	];
});

// 显示的站点列表(折叠时只显示关键站点)
const displayedStations = computed(() => {
	if (showAllStations.value) {
		return allStations.value;
	}
	
	// 折叠状态:显示最后1个已过站、当前站、接下来2个站
	const stations = allStations.value;
	const currentIndex = stations.findIndex(s => s.isCurrent);
	
	if (currentIndex === -1) {
		// 如果没有当前站,显示前5个
		return stations.slice(0, 5);
	}
	
	// 显示逻辑: 当前站前1个 + 当前站 + 后2个
	const startIndex = Math.max(0, currentIndex - 1);
	const endIndex = Math.min(stations.length, currentIndex + 3);
	
	return stations.slice(startIndex, endIndex);
});

function toggleAllStations() {
	showAllStations.value = !showAllStations.value;
}

const mapCenter = computed(() => {
	const points = polylinePoints.value;
	if (points.length) {
		const last = points[points.length - 1];
		return {
			latitude: Number(last.latitude),
			longitude: Number(last.longitude)
		};
	}
	return { latitude: 29.4036, longitude: 105.5944 };
});

const mapMarkers = computed(() => {
	const points = polylinePoints.value;
	if (!points.length) {
		return [];
	}
	const last = points[points.length - 1];
	return [{
		id: 1,
		latitude: Number(last.latitude),
		longitude: Number(last.longitude),
		iconPath: PLACEHOLDER_LOGO,
		width: 32,
		height: 32,
		title: routeName.value
	}];
});

const mapPolyline = computed(() => {
	const points = polylinePoints.value;
	if (points.length < 2) {
		return [];
	}
	return [{
		points: points.map((p) => ({
			latitude: Number(p.latitude),
			longitude: Number(p.longitude)
		})),
		color: accentColor,
		width: 4
	}];
});

async function loadTrack() {
	if (!routeId.value || loading.value) {
		return;
	}

	loading.value = true;
	try {
		const res = await fetchTransportTrack(routeId.value, routeContext.value);
		if (res.code === 0 && res.data) {
			routeName.value = res.data.routeName || '';
			trackInfo.value = res.data.trackInfo || {};
			realtimeUpdates.value = res.data.realtimeUpdates || [];
			polylinePoints.value = res.data.polyline || [];
			statusLabel.value = trackInfo.value.currentStation ? '运行中' : '暂无车辆';
			uni.setNavigationBarTitle({ title: `${routeName.value || '公交'} · 实时` });
		} else {
			routeName.value = '';
			trackInfo.value = {};
			realtimeUpdates.value = [];
			polylinePoints.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		routeName.value = '';
		trackInfo.value = {};
		realtimeUpdates.value = [];
		polylinePoints.value = [];
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

onLoad((options) => {
	const parsed = parseTransportOptions(options);
	if (parsed.routeId) {
		routeId.value = parsed.routeId;
	}
	routeContext.value = {
		city: parsed.city,
		station: parsed.station,
		direction: parsed.direction
	};
});

onShow(() => {
	if (!routeId.value) {
		uni.showToast({ title: '缺少线路参数', icon: 'none' });
		return;
	}
	loadTrack();
});
</script>

<style lang="scss" scoped>
// ========== 页面容器 ==========
.track-page {
	position: relative;
	min-height: 100vh;
	background: linear-gradient(180deg, #F0F7FF 0%, #FFF9F2 100%);
	overflow: hidden;
}

// ========== 背景装饰 ==========
.track-bg {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	pointer-events: none;
	z-index: 0;
}

.track-bg__gradient {
	position: absolute;
	top: -50%;
	left: -20%;
	width: 140%;
	height: 100%;
	background: radial-gradient(ellipse at top, rgba(59, 130, 246, 0.08) 0%, transparent 70%);
	animation: gradientFloat 8s ease-in-out infinite;
}

@keyframes gradientFloat {
	0%, 100% { transform: translateY(0) scale(1); }
	50% { transform: translateY(30rpx) scale(1.05); }
}

.track-bg__circles {
	position: absolute;
	width: 100%;
	height: 100%;
}

.track-bg__circle {
	position: absolute;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(59, 130, 246, 0.12) 0%, transparent 70%);
	animation: circleFloat 12s ease-in-out infinite;
}

.track-bg__circle--1 {
	width: 400rpx;
	height: 400rpx;
	top: 10%;
	right: -100rpx;
	animation-delay: 0s;
}

.track-bg__circle--2 {
	width: 300rpx;
	height: 300rpx;
	bottom: 20%;
	left: -80rpx;
	animation-delay: 3s;
}

.track-bg__circle--3 {
	width: 250rpx;
	height: 250rpx;
	top: 50%;
	right: 20rpx;
	animation-delay: 6s;
}

@keyframes circleFloat {
	0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.3; }
	50% { transform: translate(-20rpx, 20rpx) scale(1.1); opacity: 0.5; }
}

// ========== 加载状态 ==========
.track-loading {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	min-height: 60vh;
	gap: 24rpx;
}

.track-loading__spinner {
	width: 80rpx;
	height: 80rpx;
	border: 6rpx solid rgba(59, 130, 246, 0.1);
	border-top-color: #3B82F6;
	border-radius: 50%;
	animation: spin 0.8s linear infinite;
}

@keyframes spin {
	to { transform: rotate(360deg); }
}

.track-loading__text {
	font-size: 28rpx;
	color: #64748B;
	font-weight: 500;
}

// ========== 地图区域 ==========
.track-map-section {
	position: relative;
	z-index: 1;
	margin: 8rpx 32rpx 0;
}

.track-map-container {
	position: relative;
	height: 340rpx;
	border-radius: 32rpx;
	overflow: hidden;
	box-shadow: 
		0 20rpx 60rpx -10rpx rgba(59, 130, 246, 0.25),
		0 0 0 1rpx rgba(255, 255, 255, 0.8);
}

.track-map {
	width: 100%;
	height: 100%;
}

// H5地图占位符
.track-map-placeholder {
	position: relative;
	width: 100%;
	height: 100%;
	background: linear-gradient(135deg, #E0F2FE 0%, #DBEAFE 50%, #E0E7FF 100%);
	overflow: hidden;
}

// 动态网格
.track-map__grid {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	display: grid;
	grid-template-columns: repeat(6, 1fr);
	grid-template-rows: repeat(4, 1fr);
	opacity: 0.2;
}

.track-map__grid-line {
	border-right: 1rpx solid #3B82F6;
	animation: gridPulse 3s ease-in-out infinite;
}

.track-map__grid-line--h {
	border-right: none;
	border-bottom: 1rpx solid #3B82F6;
}

@keyframes gridPulse {
	0%, 100% { opacity: 0.3; }
	50% { opacity: 0.6; }
}

// 中心车辆
.track-map__vehicle {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	z-index: 2;
}

.track-map__vehicle-pulse {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(59, 130, 246, 0.3) 0%, transparent 70%);
	animation: pulse 2s ease-out infinite;
}

@keyframes pulse {
	0% {
		transform: translate(-50%, -50%) scale(0.5);
		opacity: 0.8;
	}
	100% {
		transform: translate(-50%, -50%) scale(1.5);
		opacity: 0;
	}
}

.track-map__vehicle-icon {
	position: relative;
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 12rpx 32rpx rgba(59, 130, 246, 0.4);
	animation: vehicleBounce 2s ease-in-out infinite;
}

@keyframes vehicleBounce {
	0%, 100% { transform: translateY(0); }
	50% { transform: translateY(-8rpx); }
}

// 轨迹装饰
.track-map__path {
	position: absolute;
	bottom: 25%;
	left: 20%;
	display: flex;
	gap: 24rpx;
}

.track-map__path-dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background-color: #3B82F6;
	opacity: 0;
	animation: pathDot 2s ease-in-out infinite;
}

.track-map__path-dot:nth-child(1) { animation-delay: 0s; }
.track-map__path-dot:nth-child(2) { animation-delay: 0.2s; }
.track-map__path-dot:nth-child(3) { animation-delay: 0.4s; }
.track-map__path-dot:nth-child(4) { animation-delay: 0.6s; }
.track-map__path-dot:nth-child(5) { animation-delay: 0.8s; }

@keyframes pathDot {
	0%, 100% { opacity: 0; transform: scale(0); }
	50% { opacity: 0.8; transform: scale(1); }
}

// 浮动信息卡
.track-float-card {
	position: absolute;
	top: 24rpx;
	left: 24rpx;
	padding: 16rpx 24rpx;
	border-radius: 100rpx;
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(20rpx);
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.08);
	animation: fadeInDown 0.6s ease-out;
}

@keyframes fadeInDown {
	from {
		opacity: 0;
		transform: translateY(-20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.track-float-card__header {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.track-float-card__icon {
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
	display: flex;
	align-items: center;
	justify-content: center;
}

.track-float-card__text {
	font-size: 24rpx;
	font-weight: 600;
	color: #1E293B;
}

// ========== 信息区域 ==========
.track-info-section {
	position: relative;
	z-index: 1;
	padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
}

// 主信息卡
.track-main-card {
	position: relative;
	padding: 40rpx;
	border-radius: 32rpx;
	background: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(40rpx);
	box-shadow: 
		0 20rpx 60rpx -10rpx rgba(0, 0, 0, 0.1),
		0 0 0 1rpx rgba(255, 255, 255, 0.9);
	overflow: hidden;
	animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
	from {
		opacity: 0;
		transform: translateY(30rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.track-main-card__deco {
	position: absolute;
	top: -50rpx;
	right: -50rpx;
	width: 200rpx;
	height: 200rpx;
	pointer-events: none;
}

.track-main-card__deco-circle {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	background: radial-gradient(circle, rgba(59, 130, 246, 0.08) 0%, transparent 70%);
}

.track-main-card__header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 32rpx;
	padding-bottom: 24rpx;
	border-bottom: 2rpx solid rgba(226, 232, 240, 0.8);
}

.track-main-card__route {
	display: flex;
	align-items: center;
	gap: 16rpx;
	flex: 1;
}

.track-main-card__route-icon {
	width: 56rpx;
	height: 56rpx;
	border-radius: 16rpx;
	background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.1) 100%);
	display: flex;
	align-items: center;
	justify-content: center;
}

.track-main-card__route-name {
	font-size: 38rpx;
	font-weight: 700;
	color: #0F172A;
	letter-spacing: 0.5rpx;
}

.track-status-badge {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 10rpx 20rpx;
	border-radius: 100rpx;
	background: linear-gradient(135deg, #ECFDF5 0%, #D1FAE5 100%);
}

.track-status-badge__dot {
	width: 12rpx;
	height: 12rpx;
	border-radius: 50%;
	background-color: #10B981;
	animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
	0%, 100% { opacity: 1; transform: scale(1); }
	50% { opacity: 0.6; transform: scale(1.2); }
}

.track-status-badge__text {
	font-size: 24rpx;
	font-weight: 500;
	color: #059669;
}

// 站点信息 - 时间轴
.track-stations {
	display: flex;
	flex-direction: column;
	gap: 32rpx;
	margin-bottom: 0;
}

.track-station {
	display: flex;
	align-items: flex-start;
	gap: 24rpx;
}

.track-station__marker {
	position: relative;
	flex-shrink: 0;
	width: 48rpx;
	height: 48rpx;
	margin-top: 4rpx;
}

.track-station__marker-dot {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	width: 24rpx;
	height: 24rpx;
	border-radius: 50%;
	border: 4rpx solid #FFFFFF;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.track-station__marker--current .track-station__marker-dot {
	width: 32rpx;
	height: 32rpx;
	background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
	animation: markerPulse 2s ease-in-out infinite;
}

@keyframes markerPulse {
	0%, 100% { box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.3); }
	50% { box-shadow: 0 4rpx 24rpx rgba(59, 130, 246, 0.6); }
}

.track-station__marker--next .track-station__marker-dot {
	background: linear-gradient(135deg, #E2E8F0 0%, #CBD5E1 100%);
}

.track-station__content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	padding-top: 2rpx;
}

.track-station__label {
	font-size: 24rpx;
	font-weight: 400;
	color: #94A3B8;
	letter-spacing: 0.5rpx;
}

.track-station__name {
	font-size: 34rpx;
	font-weight: 600;
	color: #0F172A;
	line-height: 1.4;
}

.track-station__eta {
	margin-top: 4rpx;
	padding: 6rpx 16rpx;
	border-radius: 8rpx;
	background-color: #FEF3C7;
	font-size: 24rpx;
	font-weight: 500;
	color: #D97706;
	align-self: flex-start;
}

// 车辆详情网格
// ========== 线路站点列表 ==========
.track-route-card {
	margin-top: 24rpx;
	padding: 32rpx;
	border-radius: 32rpx;
	background: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(40rpx);
	box-shadow: 0 20rpx 60rpx -10rpx rgba(0, 0, 0, 0.1);
	animation: fadeInUp 0.6s ease-out 0.1s backwards;
}

.track-route-card__header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 28rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid rgba(226, 232, 240, 0.6);
}

.track-route-card__title {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.track-route-card__title-text {
	font-size: 32rpx;
	font-weight: 600;
	color: #0F172A;
}

.track-route-card__count {
	font-size: 24rpx;
	font-weight: 400;
	color: #94A3B8;
}

.track-route-stations {
	display: flex;
	flex-direction: column;
	gap: 0;
}

.track-route-station {
	display: flex;
	gap: 20rpx;
	padding: 16rpx 0;
}

.track-route-station__line {
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
	width: 32rpx;
}

.track-route-station__dot {
	flex-shrink: 0;
	width: 16rpx;
	height: 16rpx;
	border-radius: 50%;
	background-color: #CBD5E1;
	border: 3rpx solid #FFFFFF;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
	z-index: 1;
}

.track-route-station--passed .track-route-station__dot {
	background-color: #94A3B8;
}

.track-route-station--current .track-route-station__dot {
	width: 24rpx;
	height: 24rpx;
	background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
	box-shadow: 0 4rpx 12rpx rgba(59, 130, 246, 0.4);
}

.track-route-station__connector {
	position: absolute;
	top: 24rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 2rpx;
	height: calc(100% + 16rpx);
	background-color: #E2E8F0;
}

.track-route-station--current .track-route-station__connector {
	background: linear-gradient(180deg, #3B82F6 0%, #E2E8F0 100%);
}

.track-route-station__content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	padding-top: 2rpx;
}

.track-route-station__row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
}

.track-route-station__name {
	font-size: 28rpx;
	font-weight: 500;
	color: #475569;
}

.track-route-station--current .track-route-station__name {
	font-size: 30rpx;
	font-weight: 600;
	color: #0F172A;
}

.track-route-station--passed .track-route-station__name {
	color: #94A3B8;
}

.track-route-station__time {
	font-size: 24rpx;
	font-weight: 400;
	color: #94A3B8;
}

.track-route-station--current .track-route-station__time {
	color: #3B82F6;
	font-weight: 500;
}

.track-route-station__label {
	font-size: 22rpx;
	font-weight: 400;
	color: #3B82F6;
	padding: 4rpx 12rpx;
	border-radius: 6rpx;
	background-color: rgba(59, 130, 246, 0.1);
	align-self: flex-start;
}

// 展开/收起按钮
.track-route-toggle {
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;
	margin-top: 20rpx;
	padding: 16rpx 24rpx;
	border-radius: 100rpx;
	background: linear-gradient(135deg, rgba(59, 130, 246, 0.05) 0%, rgba(37, 99, 235, 0.05) 100%);
	border: 1rpx solid rgba(59, 130, 246, 0.15);
	transition: all 0.3s ease;
}

.track-route-toggle__text {
	font-size: 26rpx;
	font-weight: 500;
	color: #3B82F6;
}

// ========== 实时动态 ==========
.track-updates {
	margin-top: 32rpx;
	animation: fadeInUp 0.6s ease-out 0.2s backwards;
}

.track-updates__title {
	display: flex;
	align-items: center;
	gap: 16rpx;
	margin-bottom: 24rpx;
}

.track-updates__title-line {
	flex: 1;
	height: 2rpx;
	background: linear-gradient(90deg, transparent 0%, #E2E8F0 50%, transparent 100%);
}

.track-updates__title-text {
	font-size: 28rpx;
	font-weight: 600;
	color: #64748B;
	letter-spacing: 1rpx;
}

.track-update-item {
	display: flex;
	gap: 20rpx;
	animation: fadeInLeft 0.5s ease-out backwards;
}

@keyframes fadeInLeft {
	from {
		opacity: 0;
		transform: translateX(-20rpx);
	}
	to {
		opacity: 1;
		transform: translateX(0);
	}
}

.track-update-item__timeline {
	position: relative;
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-top: 12rpx;
}

.track-update-item__timeline-dot {
	flex-shrink: 0;
	width: 20rpx;
	height: 20rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #3B82F6 0%, #2563EB 100%);
	box-shadow: 0 0 0 6rpx rgba(59, 130, 246, 0.15);
}

.track-update-item__timeline-line {
	flex: 1;
	width: 2rpx;
	min-height: 60rpx;
	margin-top: 8rpx;
	background: linear-gradient(180deg, #3B82F6 0%, #E2E8F0 100%);
}

.track-update-item__card {
	flex: 1;
	padding: 24rpx 28rpx;
	margin-bottom: 16rpx;
	border-radius: 20rpx;
	background: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(20rpx);
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
}

.track-update-item__header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 12rpx;
}

.track-update-item__label {
	font-size: 24rpx;
	font-weight: 400;
	color: #94A3B8;
}

.track-update-item__time {
	font-size: 22rpx;
	font-weight: 400;
	color: #CBD5E1;
}

.track-update-item__value {
	font-size: 28rpx;
	font-weight: 500;
	color: #1E293B;
	line-height: 1.5;
}

// ========== 空状态 ==========
.track-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 80rpx 40rpx;
	margin-top: 32rpx;
	border-radius: 32rpx;
	background: rgba(255, 255, 255, 0.6);
	backdrop-filter: blur(20rpx);
	animation: fadeInUp 0.6s ease-out 0.2s backwards;
}

.track-empty__icon {
	position: relative;
	width: 120rpx;
	height: 120rpx;
	margin-bottom: 24rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, rgba(59, 130, 246, 0.1) 0%, rgba(37, 99, 235, 0.1) 100%);
	display: flex;
	align-items: center;
	justify-content: center;
}

.track-empty__icon-ring {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	width: 140rpx;
	height: 140rpx;
	border: 3rpx solid rgba(59, 130, 246, 0.2);
	border-radius: 50%;
	animation: ringPulse 2s ease-out infinite;
}

@keyframes ringPulse {
	0% {
		transform: translate(-50%, -50%) scale(0.8);
		opacity: 0.8;
	}
	100% {
		transform: translate(-50%, -50%) scale(1.3);
		opacity: 0;
	}
}

.track-empty__text {
	font-size: 32rpx;
	font-weight: 600;
	color: #64748B;
	margin-bottom: 12rpx;
}

.track-empty__hint {
	font-size: 26rpx;
	font-weight: 400;
	color: #CBD5E1;
	text-align: center;
}
</style>
