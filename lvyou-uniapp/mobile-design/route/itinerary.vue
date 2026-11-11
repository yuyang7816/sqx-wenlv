<template>
	<view class="route-itinerary-page">
		<view class="route-itinerary-summary ry-card">
			<text class="route-itinerary-summary__title">{{ route.name }}</text>
			<view class="route-itinerary-summary__meta">
				<view v-if="route.travelCity" class="route-itinerary-summary__chip">
					<u-icon name="map-fill" :color="routeColor" :size="14" />
					<text class="route-itinerary-summary__chip-text">{{ route.travelCity }}</text>
				</view>
				<view v-if="route.duration" class="route-itinerary-summary__chip">
					<u-icon name="clock-fill" :color="routeColor" :size="14" />
					<text class="route-itinerary-summary__chip-text">{{ route.duration }}</text>
				</view>
				<view class="route-itinerary-summary__chip">
					<u-icon name="file-text-fill" :color="routeColor" :size="14" />
					<text class="route-itinerary-summary__chip-text">{{ summaryText }}</text>
				</view>
			</view>
			<view class="route-itinerary-summary__meet">
				<u-icon name="pushpin-fill" :color="routeColor" :size="14" />
				<text class="route-itinerary-summary__meet-text">{{ route.meetPoint || ui.meetPointDefault }}</text>
			</view>
			<text class="route-itinerary-summary__tip">{{ ui.itineraryMeetTip }}</text>
		</view>

		<view class="route-itinerary-block ry-card">
			<view class="route-itinerary-block__head">
				<view class="route-itinerary-block__icon">
					<u-icon name="file-text-fill" :color="routeColor" :size="16" />
				</view>
				<text class="route-itinerary-block__title">{{ ui.sectionItinerary }}</text>
			</view>

			<up-collapse
				v-if="route.itineraryDays.length"
				class="route-itinerary-collapse"
				accordion
				:border="false"
				:value="activeDayName"
			>
				<up-collapse-item
					v-for="(day, index) in route.itineraryDays"
					:key="day.dayNo || index"
					:name="String(index)"
					:border="false"
					:clickable="false"
					:isLink="true"
					:cell-custom-style="{ backgroundColor: '#ffffff' }"
				>
					<template #title>
						<view class="route-itinerary-day-title">
							<text class="route-itinerary-day-title__text">{{ collapseDayTitle(day, index) }}</text>
						</view>
					</template>
					<view class="route-itinerary-day-panel">
						<text v-if="day.content" class="route-itinerary-day-panel__content">{{ day.content }}</text>
						<view v-if="day.images && day.images.length" class="route-itinerary-day-panel__images">
							<image
								v-for="(img, imgIndex) in day.images"
								:key="imgIndex"
								class="route-itinerary-day-panel__img ry-placeholder-img"
								:src="img"
								mode="aspectFill"
								@click="previewImages(day.images, imgIndex)"
							/>
						</view>
						<text v-if="!day.content && !(day.images && day.images.length)" class="route-itinerary-day-panel__empty">暂无当天详情</text>
					</view>
				</up-collapse-item>
			</up-collapse>
			<view v-else-if="!pageLoading" class="route-itinerary-empty">
				<u-empty mode="list" text="暂无行程安排" />
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { ROUTE_UI } from '@/common/route-ui.js';
import { fetchRouteItinerary } from '@/common/api/route.js';

const ui = ROUTE_UI;
const routeColor = getModuleColor('route');

const emptyRoute = () => ({
	id: '',
	name: '',
	duration: '',
	dayCount: 0,
	travelCity: '',
	meetPoint: '',
	itineraryDays: []
});

const routeId = ref('');
const route = ref(emptyRoute());
const pageLoading = ref(false);
const activeDayName = ref('0');

function collapseDayTitle(day, index) {
	const dayLabel = day.dayLabel || `第${index + 1}天`;
	const title = String(day.title || '').trim();
	return title ? `${dayLabel} · ${title}` : dayLabel;
}

const summaryText = computed(() => {
	const count = route.value.dayCount || route.value.itineraryDays?.length || 0;
	return ui.itinerarySummary.replace('{count}', String(count));
});

function mapDay(day, index = 0) {
	const images = Array.isArray(day.images)
		? day.images.filter(Boolean)
		: (day.cover ? [day.cover] : []);
	return {
		dayNo: day.dayNo || index + 1,
		dayLabel: day.dayLabel || `第${day.dayNo || index + 1}天`,
		title: day.title || day.name || '',
		content: day.content || day.desc || '',
		images
	};
}

function mapItineraryToDays(list) {
	if (!Array.isArray(list) || !list.length) return [];
	const dayMap = new Map();
	list.forEach((node, index) => {
		const dayNo = Number(node.dayNo) > 0 ? Number(node.dayNo) : index + 1;
		const images = Array.isArray(node.images)
			? node.images.filter(Boolean)
			: (node.cover ? [node.cover] : []);
		if (!dayMap.has(dayNo)) {
			dayMap.set(dayNo, {
				dayNo,
				dayLabel: node.dayLabel || `第${dayNo}天`,
				title: node.name || node.title || '',
				content: node.desc || node.content || '',
				images: [...images]
			});
			return;
		}
		const day = dayMap.get(dayNo);
		if (!day.title && (node.name || node.title)) day.title = node.name || node.title;
		const extra = node.desc || node.content || '';
		if (extra) day.content = day.content ? `${day.content}\n${extra}` : extra;
		images.forEach((img) => {
			if (img && !day.images.includes(img)) day.images.push(img);
		});
	});
	return Array.from(dayMap.values());
}

function mapItineraryData(data) {
	const dayCount = Number(data.dayCount) || 0;
	const duration = data.duration || data.days || (dayCount > 0 ? `${dayCount}天` : '');
	const itineraryDays = Array.isArray(data.itineraryDays) && data.itineraryDays.length
		? data.itineraryDays.map(mapDay)
		: mapItineraryToDays(data.itinerary);
	return {
		id: data.id,
		name: data.name || '',
		duration,
		dayCount: dayCount || itineraryDays.length,
		travelCity: data.travelCity || '',
		meetPoint: data.meetPoint || '',
		itineraryDays
	};
}

function previewImages(urls, current = 0) {
	const list = (urls || []).filter(Boolean);
	if (!list.length) return;
	uni.previewImage({ urls: list, current: list[current] || list[0] });
}

async function loadRouteItinerary() {
	if (!routeId.value || pageLoading.value) {
		return;
	}
	pageLoading.value = true;
	try {
		const res = await fetchRouteItinerary(routeId.value);
		if (res.code !== 0 || !res.data) {
			uni.showToast({ title: res.msg || '行程加载失败', icon: 'none' });
			return;
		}
		route.value = mapItineraryData(res.data);
		activeDayName.value = '0';
		if (route.value.name) {
			uni.setNavigationBarTitle({ title: ui.itineraryPageTitle });
		}
	} catch (e) {
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

onLoad((options) => {
	routeId.value = options?.id ? String(options.id) : '';
	uni.setNavigationBarTitle({ title: ui.itineraryPageTitle });
});

onShow(() => {
	if (routeId.value) {
		loadRouteItinerary();
	}
});
</script>

<style lang="scss" scoped>
.route-itinerary-page {
	min-height: 100vh;
	background: #fff9f0;
	padding: 20rpx var(--ry-page-padding-x) calc(40rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.route-itinerary-summary {
	padding: 28rpx 24rpx;

	&__title {
		display: block;
		font-size: 34rpx;
		font-weight: 700;
		color: #1a1a1a;
		line-height: 1.4;
	}

	&__meta {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 16rpx;
	}

	&__chip {
		display: inline-flex;
		align-items: center;
		gap: 6rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: #fff3eb;
	}

	&__chip-text {
		font-size: 22rpx;
		color: var(--ry-module-route, #ff6600);
	}

	&__meet {
		display: flex;
		align-items: flex-start;
		gap: 8rpx;
		margin-top: 18rpx;
	}

	&__meet-text {
		flex: 1;
		font-size: 24rpx;
		color: #888888;
		line-height: 1.5;
	}

	&__tip {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		color: #888888;
		line-height: 1.5;
	}
}

.route-itinerary-block {
	padding: 24rpx;

	&__head {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 8rpx;
		padding-bottom: 16rpx;
		border-bottom: 1rpx solid #f3f4f8;
	}

	&__icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 12rpx;
		background: rgba(255, 102, 0, 0.1);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: #1a1a1a;
	}
}

.route-itinerary-day-title {
	width: 100%;
	min-width: 0;
	overflow: hidden;

	&__text {
		display: block;
		width: 100%;
		font-size: 28rpx;
		font-weight: 700;
		color: #1a1a1a;
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
}

:deep(.route-itinerary-collapse) {
	.u-collapse-item,
	.u-cell,
	.u-cell__body,
	.u-cell--clickable {
		background: #ffffff !important;
		background-color: #ffffff !important;
	}

	.u-cell__body {
		overflow: hidden;
		padding: 22rpx 8rpx !important;
	}

	.u-cell__body__content,
	.u-cell__title {
		flex: 1;
		min-width: 0;
		overflow: hidden;
	}

	.u-cell__title {
		width: 0;
	}

	.u-cell__label {
		display: none !important;
	}

	.u-cell__right-icon-wrap {
		flex-shrink: 0;
	}
}

.route-itinerary-day-panel {
	padding: 8rpx 4rpx 12rpx;

	&__content {
		display: block;
		font-size: 26rpx;
		color: #888888;
		line-height: 1.7;
		white-space: pre-wrap;
	}

	&__images {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
		margin-top: 16rpx;
	}

	&__img {
		width: calc((100% - 16rpx) / 2);
		height: 220rpx;
		border-radius: 12rpx;
		background: #f0f0f0;
	}

	&__empty {
		display: block;
		font-size: 24rpx;
		color: #888888;
	}
}

.route-itinerary-empty {
	padding: 48rpx 0;
}
</style>
