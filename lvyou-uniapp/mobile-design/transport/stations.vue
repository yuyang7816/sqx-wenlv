<template>
	<view class="transport-stations-page">
		<view class="transport-stations-body">
			<view v-if="loading && !route.name" class="transport-stations-loading">
				<text class="transport-stations-loading__text">加载中...</text>
			</view>

			<template v-else-if="route.name">
				<view class="transport-stations-route ry-card">
					<text class="transport-stations-route__label">{{ route.name }}</text>
					<text class="transport-stations-route__text">{{ route.startStation }} → {{ route.endStation }}</text>
					<text class="transport-stations-route__meta">
						共 {{ route.stationCount }} 站
					</text>
				</view>

				<view v-if="route.stations.length" class="transport-stations-list ry-card">
					<view
						v-for="(station, index) in route.stations"
						:key="`${route.id}-${station}-${index}`"
						class="transport-stations-item"
					>
						<view class="transport-stations-item__rail">
							<view
								class="transport-stations-item__dot"
								:class="{
									'transport-stations-item__dot--current': index === route.currentStopIndex,
									'transport-stations-item__dot--passed': index < route.currentStopIndex
								}"
								:style="dotStyle(index)"
							/>
							<view v-if="index < route.stations.length - 1" class="transport-stations-item__line" />
						</view>
						<view
							class="transport-stations-item__content"
							:class="{ 'transport-stations-item__content--current': index === route.currentStopIndex }"
						>
							<text class="transport-stations-item__name">{{ station }}</text>
							<text v-if="index === route.currentStopIndex" class="transport-stations-item__badge">当前站</text>
						</view>
					</view>
				</view>

				<view v-else class="transport-stations-empty">
					<text class="transport-stations-empty__text">暂无站点数据</text>
				</view>
			</template>

			<view v-else class="transport-stations-empty">
				<text class="transport-stations-empty__text">线路不存在或已下架</text>
			</view>
		</view>

		<view v-if="routeId" class="transport-stations-bar">
			<view class="transport-stations-bar__btn transport-stations-bar__btn--outline" @click="openSchedule">
				<text>时刻表</text>
			</view>
			<view
				class="transport-stations-bar__btn transport-stations-bar__btn--primary"
				:style="{ background: `linear-gradient(135deg, ${accentColor}, ${accentColor}CC)` }"
				@click="openTrack"
			>
				<text>实时位置</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { fetchTransportStations } from '@/common/api/transport.js';
import { parseTransportOptions, serializeTransportQuery, buildTransportRouteQuery } from '@/common/transport-context.js';

const accentColor = getModuleColor('transport');
const accentLight = '#FFF2E8';

const route = ref({
	id: '',
	name: '',
	startStation: '',
	endStation: '',
	stationCount: 0,
	currentStopIndex: 0,
	stations: []
});
const routeId = ref('');
const routeContext = ref({ city: '', station: '', direction: 1 });
const loading = ref(false);

function dotStyle(index) {
	if (index === route.value.currentStopIndex) {
		return { backgroundColor: accentColor, boxShadow: `0 0 0 8rpx ${accentLight}` };
	}
	if (index < route.value.currentStopIndex) {
		return { backgroundColor: accentColor, opacity: 0.45 };
	}
	return {};
}

function mapRoute(data) {
	return {
		id: data.id || routeId.value,
		name: data.name || '',
		startStation: data.startStation || '',
		endStation: data.endStation || '',
		stationCount: data.stationCount || (data.stations || []).length,
		currentStopIndex: data.currentStopIndex ?? 0,
		stations: data.stations || []
	};
}

async function loadStations() {
	if (!routeId.value || loading.value) {
		return;
	}

	loading.value = true;
	try {
		const res = await fetchTransportStations(routeId.value, routeContext.value);
		if (res.code === 0 && res.data) {
			route.value = mapRoute(res.data);
			uni.setNavigationBarTitle({ title: `${route.value.name} · 站点` });
		} else {
			route.value = { id: '', name: '', startStation: '', endStation: '', stationCount: 0, currentStopIndex: 0, stations: [] };
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		route.value = { id: '', name: '', startStation: '', endStation: '', stationCount: 0, currentStopIndex: 0, stations: [] };
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function buildNavUrl(page) {
	const query = serializeTransportQuery(buildTransportRouteQuery({
		id: routeId.value,
		city: routeContext.value.city,
		referenceStation: routeContext.value.station,
		direction: routeContext.value.direction
	}));
	return `/mobile-design/transport/${page}?${query}`;
}

function openSchedule() {
	uni.navigateTo({ url: buildNavUrl('schedule') });
}

function openTrack() {
	uni.navigateTo({ url: buildNavUrl('track') });
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
	loadStations();
});
</script>

<style lang="scss" scoped>
@keyframes stationEnter {
	from {
		opacity: 0;
		transform: translateY(20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes dotGlow {
	0%, 100% {
		box-shadow: 0 0 0 0 rgba(255, 102, 0, 0.5);
	}
	50% {
		box-shadow: 0 0 0 20rpx rgba(255, 102, 0, 0);
	}
}

@keyframes gradientShift {
	0%, 100% {
		background-position: 0% 50%;
	}
	50% {
		background-position: 100% 50%;
	}
}

.transport-stations-page {
	min-height: 100vh;
	background: linear-gradient(135deg, #fff8f3 0%, #fffbf7 50%, #fff5eb 100%);
	background-size: 200% 200%;
	animation: gradientShift 15s ease infinite;
	padding-bottom: calc(110rpx + env(safe-area-inset-bottom));
	position: relative;
	
	&::before {
		content: '';
		position: fixed;
		top: -50%;
		right: -50%;
		width: 100%;
		height: 100%;
		background: radial-gradient(circle, rgba(255, 102, 0, 0.08) 0%, transparent 60%);
		pointer-events: none;
	}
}

.transport-stations-body {
	padding: 0;
	display: flex;
	flex-direction: column;
}

.transport-stations-loading,
.transport-stations-empty {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 60vh;
}

.transport-stations-loading__text,
.transport-stations-empty__text {
	font-size: 28rpx;
	color: #9ca3af;
}

/* 顶部线路信息 - 全新卡片设计 */
.transport-stations-route {
	margin: 24rpx 32rpx 0;
	background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
	padding: 0;
	border-radius: 28rpx;
	box-shadow: 0 12rpx 32rpx rgba(255, 102, 0, 0.35),
	            0 6rpx 16rpx rgba(255, 102, 0, 0.25);
	position: relative;
	overflow: hidden;
	
	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 50%;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.2), transparent);
	}
	
	&::after {
		content: '';
		position: absolute;
		bottom: -100rpx;
		right: -100rpx;
		width: 300rpx;
		height: 300rpx;
		background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
		border-radius: 50%;
	}
}

.transport-stations-route__label {
	padding: 28rpx 32rpx 0;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.9);
	font-weight: 600;
	margin-bottom: 8rpx;
	display: block;
	letter-spacing: 1rpx;
	position: relative;
	z-index: 1;
}

.transport-stations-route__text {
	padding: 0 32rpx;
	font-size: 40rpx;
	font-weight: 700;
	color: #ffffff;
	line-height: 1.4;
	margin-bottom: 12rpx;
	letter-spacing: -0.8rpx;
	text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.15);
	position: relative;
	z-index: 1;
}

.transport-stations-route__meta {
	padding: 0 32rpx 32rpx;
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.95);
	font-weight: 500;
	display: flex;
	align-items: center;
	gap: 12rpx;
	position: relative;
	z-index: 1;
	
	&::before {
		content: '';
		width: 4rpx;
		height: 4rpx;
		background: rgba(255, 255, 255, 0.6);
		border-radius: 50%;
	}
}

/* 站点列表 */
.transport-stations-list {
	padding: 64rpx 0 40rpx;
	background: transparent;
	position: relative;
}

.transport-stations-item {
	display: flex;
	flex-direction: row;
	gap: 0;
	position: relative;
	padding: 0 40rpx;
	animation: stationEnter 0.5s cubic-bezier(0.34, 1.56, 0.64, 1) both;
	animation-delay: calc(var(--station-index, 0) * 60ms);
	
	&:nth-child(1) { --station-index: 0; }
	&:nth-child(2) { --station-index: 1; }
	&:nth-child(3) { --station-index: 2; }
	&:nth-child(4) { --station-index: 3; }
	&:nth-child(5) { --station-index: 4; }
	&:nth-child(6) { --station-index: 5; }
	&:nth-child(7) { --station-index: 6; }
	&:nth-child(8) { --station-index: 7; }
	&:nth-child(9) { --station-index: 8; }
	&:nth-child(n+10) { --station-index: 9; }
}

.transport-stations-item__rail {
	width: 60rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	flex-shrink: 0;
	position: relative;
}

.transport-stations-item__dot {
	width: 18rpx;
	height: 18rpx;
	border-radius: 50%;
	background: #e0e0e0;
	flex-shrink: 0;
	z-index: 2;
	position: relative;
	margin-top: 6rpx;
	transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.transport-stations-item__dot--passed {
	background: linear-gradient(135deg, #ffb380 0%, #ffc599 100%);
	box-shadow: 0 2rpx 8rpx rgba(255, 179, 128, 0.4);
}

.transport-stations-item__dot--current {
	width: 36rpx;
	height: 36rpx;
	margin-top: -3rpx;
	background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
	box-shadow: 0 6rpx 20rpx rgba(255, 102, 0, 0.5),
	            0 0 0 6rpx rgba(255, 102, 0, 0.15);
	animation: dotGlow 2s ease-in-out infinite;
	
	&::before {
		content: '';
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		width: 14rpx;
		height: 14rpx;
		background: #ffffff;
		border-radius: 50%;
		box-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.15);
	}
}

.transport-stations-item__line {
	flex: 1;
	width: 3rpx;
	min-height: 56rpx;
	margin-top: 6rpx;
	background: linear-gradient(180deg, #e8e8e8 0%, #f0f0f0 100%);
	border-radius: 2rpx;
}

.transport-stations-item__content {
	flex: 1;
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	gap: 24rpx;
	padding-bottom: 44rpx;
}

.transport-stations-item__name {
	font-size: 30rpx;
	color: #4a4a4a;
	line-height: 1.6;
	font-weight: 500;
	transition: all 0.3s ease;
}

.transport-stations-item__content--current {
	.transport-stations-item__name {
		font-size: 36rpx;
		color: #1a1a1a;
		font-weight: 700;
	}
}

.transport-stations-item__badge {
	flex-shrink: 0;
	padding: 8rpx 20rpx;
	border-radius: 20rpx;
	background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
	font-size: 24rpx;
	color: #ffffff;
	font-weight: 700;
	box-shadow: 0 4rpx 16rpx rgba(255, 102, 0, 0.35),
	            0 2rpx 8rpx rgba(255, 102, 0, 0.25);
	position: relative;
	
	&::before {
		content: '';
		position: absolute;
		top: 2rpx;
		left: 2rpx;
		right: 2rpx;
		height: 45%;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.4), transparent);
		border-radius: 18rpx 18rpx 0 0;
	}
}

/* 底部操作栏 */
.transport-stations-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 100;
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0.95) 100%);
	backdrop-filter: blur(20rpx) saturate(180%);
	border-top: 1rpx solid rgba(255, 102, 0, 0.12);
	box-shadow: 0 -4rpx 20rpx rgba(255, 102, 0, 0.08);
}

.transport-stations-bar__btn {
	flex: 1;
	height: 88rpx;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	font-weight: 600;
	transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
	position: relative;
	overflow: hidden;

	&--outline {
		background: #ffffff;
		color: #ff6600;
		border: 2rpx solid #ff6600;
		box-shadow: 0 4rpx 16rpx rgba(255, 102, 0, 0.12);
		
		&::before {
			content: '';
			position: absolute;
			inset: 0;
			background: linear-gradient(135deg, rgba(255, 102, 0, 0.08), transparent);
			opacity: 0;
			transition: opacity 0.25s;
		}
		
		&:active {
			transform: scale(0.96);
			
			&::before {
				opacity: 1;
			}
		}
	}

	&--primary {
		background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
		color: #ffffff;
		box-shadow: 0 8rpx 24rpx rgba(255, 102, 0, 0.4),
		            0 4rpx 12rpx rgba(255, 102, 0, 0.3);
		
		&::before {
			content: '';
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			height: 50%;
			background: linear-gradient(180deg, rgba(255, 255, 255, 0.3), transparent);
			border-radius: 16rpx 16rpx 0 0;
		}
		
		&:active {
			transform: scale(0.96);
			box-shadow: 0 4rpx 16rpx rgba(255, 102, 0, 0.35),
			            0 2rpx 8rpx rgba(255, 102, 0, 0.25);
		}
	}
}
</style>
