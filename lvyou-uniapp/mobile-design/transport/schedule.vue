<template>
	<view class="transport-schedule-page">
		<view class="transport-schedule-body">
			<view v-if="loading && !scheduleData.name" class="transport-schedule-loading">
				<text class="transport-schedule-loading__text">加载中...</text>
			</view>

			<template v-else>
				<view v-if="routeTabs.length" class="transport-schedule-tabs">
					<view
						v-for="route in routeTabs"
						:key="route.id"
						class="transport-schedule-tabs__item"
						:class="{ 'transport-schedule-tabs__item--active': activeRouteId === route.id }"
						:style="activeRouteId === route.id ? { '--tab-accent': accentColor } : {}"
						@click="onTabChange(route.id)"
					>
						<text class="transport-schedule-tabs__text">{{ route.name }}</text>
					</view>
				</view>

				<view v-if="scheduleData.directions?.length" class="transport-schedule-directions">
					<view
						v-for="direction in scheduleData.directions"
						:key="direction.label"
						class="transport-schedule-direction ry-card"
					>
						<text class="transport-schedule-direction__label">{{ direction.label }}</text>
						<text class="transport-schedule-direction__route">
							{{ direction.from }} → {{ direction.to }}
						</text>
						<view class="transport-schedule-direction__meta">
							<text>首班 {{ direction.firstBus }}</text>
							<text>末班 {{ direction.lastBus }}</text>
							<text>间隔 {{ direction.interval }}</text>
						</view>
					</view>
				</view>

				<view v-if="scheduleData.scheduleRows?.length" class="transport-schedule-table ry-card">
					<view class="transport-schedule-table__head">
						<text class="transport-schedule-table__title">{{ scheduleData.name }} 时刻表</text>
					</view>
					<view
						v-for="row in scheduleData.scheduleRows"
						:key="row.station"
						class="transport-schedule-table__row"
					>
						<text class="transport-schedule-table__station">{{ row.station }}</text>
						<view class="transport-schedule-table__times">
							<text
								v-for="(time, index) in row.times"
								:key="`${row.station}-${index}`"
								class="transport-schedule-table__time"
							>{{ time }}</text>
						</view>
					</view>
				</view>

				<view v-else-if="!loading" class="transport-schedule-empty">
					<text class="transport-schedule-empty__text">暂无时刻表数据</text>
				</view>
			</template>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { fetchTransportRouteList, fetchTransportSchedule } from '@/common/api/transport.js';
import { parseTransportOptions } from '@/common/transport-context.js';

const accentColor = getModuleColor('transport');

const routeId = ref('');
const routeContext = ref({ city: '', station: '', direction: 1 });
const activeRouteId = ref('');
const routeTabs = ref([]);
const scheduleData = ref({
	id: '',
	name: '',
	directions: [],
	scheduleRows: []
});
const loading = ref(false);

async function ensureRouteId() {
	if (routeId.value) {
		return routeId.value;
	}
	try {
		const res = await fetchTransportRouteList({ page: 1, limit: 10 });
		if (res.code === 0 && res.data?.records?.length) {
			const first = res.data.records[0];
			routeId.value = String(first.id);
			routeContext.value = {
				city: first.city || '',
				station: first.referenceStation || '',
				direction: first.direction || 1
			};
			return routeId.value;
		}
	} catch (e) {
		// 兜底由 loadSchedule 提示
	}
	return '';
}

async function loadSchedule(id) {
	if (!id || loading.value) {
		return;
	}

	loading.value = true;
	try {
		const res = await fetchTransportSchedule(id, routeContext.value);
		if (res.code === 0 && res.data) {
			activeRouteId.value = String(res.data.id || id);
			routeTabs.value = res.data.routes || [];
			scheduleData.value = {
				id: res.data.id || id,
				name: res.data.name || '',
				directions: res.data.directions || [],
				scheduleRows: res.data.scheduleRows || []
			};
			uni.setNavigationBarTitle({ title: `${scheduleData.value.name || '公交'} · 时刻表` });
		} else {
			scheduleData.value = { id: '', name: '', directions: [], scheduleRows: [] };
			routeTabs.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		scheduleData.value = { id: '', name: '', directions: [], scheduleRows: [] };
		routeTabs.value = [];
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function onTabChange(id) {
	if (activeRouteId.value === id) {
		return;
	}
	routeId.value = String(id);
	loadSchedule(id);
}

function syncRouteContextFromTab(id) {
	const matched = routeTabs.value.find((item) => String(item.id) === String(id));
	if (matched) {
		routeContext.value = {
			...routeContext.value,
			city: routeContext.value.city || matched.city || '',
			station: routeContext.value.station || matched.referenceStation || ''
		};
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

onShow(async () => {
	const id = routeId.value || await ensureRouteId();
	if (!id) {
		uni.showToast({ title: '暂无公交线路', icon: 'none' });
		return;
	}
	syncRouteContextFromTab(id);
	await loadSchedule(id);
});
</script>

<style lang="scss" scoped>
@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes bgFlow {
	0%, 100% {
		background-position: 0% 50%;
	}
	50% {
		background-position: 100% 50%;
	}
}

.transport-schedule-page {
	min-height: 100vh;
	background: linear-gradient(135deg, #fff8f3 0%, #fffbf7 50%, #fff5eb 100%);
	background-size: 200% 200%;
	animation: bgFlow 15s ease infinite;
	position: relative;
	
	&::before {
		content: '';
		position: fixed;
		top: -50%;
		left: -50%;
		width: 100%;
		height: 100%;
		background: radial-gradient(circle, rgba(255, 102, 0, 0.06) 0%, transparent 60%);
		pointer-events: none;
	}
}

.transport-schedule-body {
	padding: 24rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.transport-schedule-loading,
.transport-schedule-empty {
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 60vh;
}

.transport-schedule-loading__text,
.transport-schedule-empty__text {
	font-size: 28rpx;
	color: #9ca3af;
}

/* 标签页 */
.transport-schedule-tabs {
	display: flex;
	flex-direction: row;
	gap: 12rpx;
	padding: 16rpx 0 8rpx;
	overflow-x: auto;
	-webkit-overflow-scrolling: touch;
	
	&::-webkit-scrollbar {
		display: none;
	}
}

.transport-schedule-tabs__item {
	flex-shrink: 0;
	min-width: 140rpx;
	height: 68rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #ffffff;
	border-radius: 34rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.06);
	transition: transform 0.2s ease, box-shadow 0.2s ease;
	position: relative;
	overflow: hidden;
}

.transport-schedule-tabs__item--active {
	background: linear-gradient(135deg, #ff6600 0%, #ff8533 100%);
	box-shadow: 0 6rpx 20rpx rgba(255, 102, 0, 0.4),
	            0 3rpx 10rpx rgba(255, 102, 0, 0.3);
	transform: scale(1.02);
	
	&::after {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 50%;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.3), transparent);
		border-radius: 34rpx 34rpx 0 0;
		pointer-events: none;
	}
}

.transport-schedule-tabs__text {
	font-size: 28rpx;
	font-weight: normal;
	color: #666666;
	transition: color 0.2s;
	position: relative;
	z-index: 1;
	pointer-events: none;
}

.transport-schedule-tabs__item--active .transport-schedule-tabs__text {
	color: #ffffff;
	font-weight: 600;
	text-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.15);
}

/* 方向卡片 */
.transport-schedule-directions {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.transport-schedule-direction {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 32rpx 28rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06),
	            0 2rpx 10rpx rgba(0, 0, 0, 0.04);
	display: flex;
	flex-direction: column;
	gap: 14rpx;
	position: relative;
	overflow: hidden;
	animation: fadeIn 0.5s ease-out;
	
	&::before {
		content: '';
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		height: 4rpx;
		background: linear-gradient(90deg, #ff6600 0%, #ff8533 100%);
	}
}

.transport-schedule-direction__label {
	font-size: 24rpx;
	font-weight: 600;
	color: #ff6600;
	letter-spacing: 0.5rpx;
}

.transport-schedule-direction__route {
	font-size: 32rpx;
	font-weight: 600;
	color: #1a1a1a;
	line-height: 1.5;
	letter-spacing: -0.5rpx;
}

.transport-schedule-direction__meta {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	font-size: 24rpx;
	color: #666666;
	
	text {
		display: flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 16rpx;
		background: rgba(255, 102, 0, 0.08);
		border-radius: 12rpx;
		font-weight: normal;
		
		&::before {
			content: '';
			width: 4rpx;
			height: 4rpx;
			background: #ff6600;
			border-radius: 50%;
		}
	}
}

/* 时刻表 */
.transport-schedule-table {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 32rpx 28rpx;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06),
	            0 2rpx 10rpx rgba(0, 0, 0, 0.04);
	animation: fadeIn 0.6s ease-out;
}

.transport-schedule-table__head {
	display: flex;
	flex-direction: column;
	margin-bottom: 28rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid rgba(255, 102, 0, 0.1);
	position: relative;
	
	&::after {
		content: '';
		position: absolute;
		bottom: -2rpx;
		left: 0;
		width: 80rpx;
		height: 2rpx;
		background: #ff6600;
	}
}

.transport-schedule-table__title {
	font-size: 30rpx;
	font-weight: 600;
	color: #1a1a1a;
	letter-spacing: -0.5rpx;
}

.transport-schedule-table__row {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
	
	&:last-child {
		border-bottom: none;
		padding-bottom: 0;
	}
}

.transport-schedule-table__station {
	font-size: 28rpx;
	font-weight: normal;
	color: #1a1a1a;
	display: flex;
	align-items: center;
	gap: 10rpx;
	
	&::before {
		content: '';
		width: 8rpx;
		height: 8rpx;
		background: #ff6600;
		border-radius: 50%;
		box-shadow: 0 0 0 3rpx rgba(255, 102, 0, 0.15);
	}
}

.transport-schedule-table__times {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	padding-left: 18rpx;
}

.transport-schedule-table__time {
	min-width: 96rpx;
	padding: 10rpx 18rpx;
	border-radius: 14rpx;
	background: linear-gradient(135deg, rgba(255, 102, 0, 0.08), rgba(255, 133, 51, 0.06));
	font-size: 26rpx;
	color: #ff6600;
	text-align: center;
	font-weight: normal;
	font-variant-numeric: tabular-nums;
	box-shadow: 0 2rpx 8rpx rgba(255, 102, 0, 0.08);
	transition: all 0.2s ease;
	
	&:active {
		transform: scale(0.95);
		background: linear-gradient(135deg, #ff6600, #ff8533);
		color: #ffffff;
		box-shadow: 0 4rpx 12rpx rgba(255, 102, 0, 0.3);
		font-weight: 600;
	}
}
</style>
