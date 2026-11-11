<template>
	<view class="page">
		<!-- 第一屏：一张构图 —— 封面 + 状态 + 标题 -->
		<view class="hero">
			<image class="hero__img ry-placeholder-img" :src="activity.cover" mode="aspectFill" />
			<view class="hero__mask" />
			<view class="hero__foot">
				<text v-if="activity.statusLabel" class="hero__status">{{ activity.statusLabel }}</text>
				<text class="hero__title">{{ activity.name || '活动详情' }}</text>
				<view v-if="activity.tags?.length" class="hero__tags">
					<text v-for="tag in activity.tags" :key="tag" class="hero__tag">{{ tag }}</text>
				</view>
			</view>
		</view>

		<view class="body">
			<!-- 关键信息：一行一意，无卡片嵌套 -->
			<view class="meta">
				<view class="meta__row">
					<text class="meta__k">{{ ui.labelTime }}</text>
					<text class="meta__v">{{ activity.time || '时间待定' }}</text>
				</view>
				<view class="meta__row">
					<text class="meta__k">{{ ui.labelPrice }}</text>
					<view class="meta__stack">
						<text class="meta__v">{{ priceView.label }}</text>
						<text v-if="priceView.label !== ui.priceFree && priceView.tip" class="meta__tip">{{ priceView.tip }}</text>
					</view>
				</view>
				<view class="meta__row">
					<text class="meta__k">{{ ui.labelEnroll }}</text>
					<text class="meta__v">{{ enrollText }}</text>
				</view>
				<view class="meta__row meta__row--link" @click.stop="onNavigate">
					<text class="meta__k">{{ ui.labelLocation }}</text>
					<text class="meta__v meta__v--flex">{{ activity.address || '暂无地址' }}</text>
					<u-icon name="map-fill" color="#9C27B0" :size="16" />
				</view>
			</view>

			<view class="sec">
				<text class="sec__h">{{ ui.sectionIntro }}</text>
				<text class="sec__p">{{ activity.desc || '暂无介绍' }}</text>
			</view>

			<view v-if="activity.highlights?.length" class="sec">
				<text class="sec__h">{{ ui.sectionHighlights }}</text>
				<view
					v-for="(item, index) in activity.highlights"
					:key="index"
					class="hl"
				>
					<view class="hl__rail">
						<view class="hl__dot" />
						<view v-if="index < activity.highlights.length - 1" class="hl__line" />
					</view>
					<view class="hl__main">
						<text class="hl__t">{{ item.label }}</text>
						<text v-if="item.desc" class="hl__d">{{ item.desc }}</text>
					</view>
				</view>
			</view>

			<view v-if="activity.notices?.length" class="sec sec--end">
				<text class="sec__h">{{ ui.sectionNotice }}</text>
				<text
					v-for="(notice, index) in activity.notices"
					:key="index"
					class="note"
				>{{ notice }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { ACTIVITY_UI, formatActivityPrice } from '@/common/activity-ui.js';
import { fetchActivityDetail } from '@/common/api/activity.js';
import { fetchMapGeocode } from '@/common/api/map.js';

const ui = ACTIVITY_UI;

const emptyActivity = () => ({
	id: '',
	name: '',
	address: '',
	latitude: 0,
	longitude: 0,
	time: '',
	status: '',
	statusLabel: '',
	cover: PLACEHOLDER_LOGO,
	tags: [],
	desc: '',
	highlights: [],
	notices: [],
	price: null,
	priceTip: '',
	participants: 0,
	enrollLimit: 0
});

const activityId = ref('');
const activity = ref(emptyActivity());
const pageLoading = ref(false);

const priceView = computed(() => formatActivityPrice(activity.value.price, activity.value.priceTip));
const enrollText = computed(() => {
	const count = Number(activity.value.participants) || 0;
	const limit = Number(activity.value.enrollLimit) || 0;
	const joined = `${count}${ui.labelParticipants}`;
	if (limit > 0) {
		return `${joined} / 上限${limit}人`;
	}
	return `${joined} / ${ui.enrollUnlimited}`;
});

function mapDetailData(data) {
	return {
		id: data.id,
		name: data.name || '',
		address: data.address || '',
		latitude: data.latitude != null ? Number(data.latitude) : 0,
		longitude: data.longitude != null ? Number(data.longitude) : 0,
		time: data.time || '',
		status: data.status || '',
		statusLabel: data.statusLabel || '',
		cover: data.cover || PLACEHOLDER_LOGO,
		tags: Array.isArray(data.tags) ? data.tags : [],
		desc: data.desc || data.description || '',
		highlights: Array.isArray(data.highlights) ? data.highlights : [],
		notices: Array.isArray(data.notices) ? data.notices : [],
		price: data.price != null ? data.price : data.priceFrom,
		priceTip: data.priceTip || '',
		participants: data.participants != null ? Number(data.participants) : Number(data.enrollCount || 0),
		enrollLimit: data.enrollLimit != null ? Number(data.enrollLimit) : 0
	};
}

function hasValidCoordinate(lat, lng) {
	const latitude = Number(lat);
	const longitude = Number(lng);
	return Number.isFinite(latitude)
		&& Number.isFinite(longitude)
		&& Math.abs(latitude) > 0.000001
		&& Math.abs(longitude) > 0.000001;
}

async function resolveNavigatePoint() {
	const { latitude, longitude, address } = activity.value;
	if (hasValidCoordinate(latitude, longitude)) {
		return {
			latitude: Number(latitude),
			longitude: Number(longitude)
		};
	}
	const keyword = String(address || '').trim();
	if (!keyword) {
		return null;
	}
	const res = await fetchMapGeocode({ address: keyword });
	if (res.code !== 0 || !res.data) {
		throw new Error(res.msg || '地址解析失败');
	}
	const lat = Number(res.data.latitude);
	const lng = Number(res.data.longitude);
	if (!hasValidCoordinate(lat, lng)) {
		throw new Error('未解析到有效坐标');
	}
	activity.value.latitude = lat;
	activity.value.longitude = lng;
	return { latitude: lat, longitude: lng };
}

async function onNavigate() {
	const { address, name } = activity.value;
	try {
		uni.showLoading({ title: '打开导航…', mask: true });
		const point = await resolveNavigatePoint();
		if (!point) {
			uni.showToast({ title: '暂无位置信息', icon: 'none' });
			return;
		}
		uni.openLocation({
			latitude: point.latitude,
			longitude: point.longitude,
			name: name || '活动地点',
			address: address || '',
			scale: 16,
			fail: () => {
				uni.showToast({ title: '打开地图失败', icon: 'none' });
			}
		});
	} catch (e) {
		uni.showToast({ title: e?.message || '打开导航失败', icon: 'none' });
	} finally {
		uni.hideLoading();
	}
}

async function loadActivityDetail() {
	if (!activityId.value || pageLoading.value) {
		return;
	}
	pageLoading.value = true;
	try {
		const res = await fetchActivityDetail(activityId.value);
		if (res.code === 0 && res.data) {
			activity.value = mapDetailData(res.data);
			uni.setNavigationBarTitle({ title: activity.value.name || '活动详情' });
		} else {
			uni.showToast({ title: res.msg || '活动详情加载失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

onLoad((options) => {
	activityId.value = options?.id || '';
});

onShow(() => {
	if (activityId.value) {
		loadActivityDetail();
	}
});
</script>

<style lang="scss" scoped>
	$purple: #9c27b0;
	$purple-dim: rgba(156, 39, 176, 0.12);
	$ink: #16141a;
	$soft: #6f6a78;
	$line: rgba(22, 20, 26, 0.06);
	$page: #faf9fb;

	.page {
		min-height: 100vh;
		background: $page;
		padding-bottom: calc(48rpx + env(safe-area-inset-bottom));
	}

	/* —— Hero：全幅，标题即品牌 —— */
	.hero {
		position: relative;
		height: 72vh;
		min-height: 640rpx;
		max-height: 880rpx;
		overflow: hidden;
		background: #1a1220;
	}

	.hero__img {
		display: block;
		width: 100%;
		height: 100%;
	}

	.hero__mask {
		position: absolute;
		inset: 0;
		background: linear-gradient(
			180deg,
			rgba(22, 20, 26, 0.05) 0%,
			rgba(22, 20, 26, 0.15) 42%,
			rgba(22, 20, 26, 0.88) 100%
		);
		pointer-events: none;
	}

	.hero__foot {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 2;
		padding: 48rpx 40rpx 56rpx;
	}

	.hero__status {
		display: inline-block;
		margin-bottom: 20rpx;
		padding: 8rpx 20rpx;
		border-radius: 8rpx;
		background: $purple;
		font-size: 22rpx;
		font-weight: 600;
		color: #fff;
		line-height: 1.3;
		letter-spacing: 1rpx;
	}

	.hero__title {
		display: block;
		font-size: 48rpx;
		font-weight: 800;
		line-height: 1.28;
		color: #fff;
		letter-spacing: -0.5rpx;
		max-width: 640rpx;
	}

	.hero__tags {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 24rpx;
	}

	.hero__tag {
		padding: 6rpx 16rpx;
		border-radius: 6rpx;
		background: rgba(255, 255, 255, 0.14);
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.88);
		line-height: 1.4;
	}

	/* —— Body —— */
	.body {
		padding: 8rpx 0 0;
	}

	.meta {
		padding: 36rpx 40rpx 12rpx;
	}

	.meta__row {
		display: flex;
		align-items: flex-start;
		gap: 28rpx;
		padding: 22rpx 0;

		& + & {
			border-top: 1rpx solid $line;
		}
	}

	.meta__row--link:active {
		opacity: 0.65;
	}

	.meta__k {
		flex-shrink: 0;
		width: 120rpx;
		padding-top: 2rpx;
		font-size: 24rpx;
		color: $soft;
		line-height: 1.5;
	}

	.meta__v {
		flex: 1;
		min-width: 0;
		font-size: 28rpx;
		font-weight: 600;
		color: $ink;
		line-height: 1.5;
	}

	.meta__v--flex {
		padding-right: 8rpx;
	}

	.meta__stack {
		flex: 1;
		min-width: 0;
	}

	.meta__tip {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		font-weight: 400;
		color: $soft;
		line-height: 1.5;
	}

	.sec {
		padding: 40rpx 40rpx 8rpx;

		&--end {
			padding-bottom: 24rpx;
		}
	}

	.sec__h {
		display: block;
		margin-bottom: 20rpx;
		font-size: 22rpx;
		font-weight: 700;
		letter-spacing: 4rpx;
		color: $purple;
		line-height: 1.3;
	}

	.sec__p {
		display: block;
		font-size: 28rpx;
		line-height: 1.8;
		color: #3d3a44;
		text-align: justify;
	}

	/* 亮点：细时间轴，紫色点 */
	.hl {
		display: flex;
		gap: 24rpx;
		min-height: 88rpx;
	}

	.hl__rail {
		width: 16rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		flex-shrink: 0;
		padding-top: 12rpx;
	}

	.hl__dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		background: $purple;
		box-shadow: 0 0 0 6rpx $purple-dim;
		flex-shrink: 0;
	}

	.hl__line {
		flex: 1;
		width: 2rpx;
		min-height: 40rpx;
		margin-top: 10rpx;
		background: $purple-dim;
	}

	.hl__main {
		flex: 1;
		min-width: 0;
		padding: 4rpx 0 36rpx;
	}

	.hl__t {
		display: block;
		font-size: 30rpx;
		font-weight: 700;
		color: $ink;
		line-height: 1.4;
	}

	.hl__d {
		display: block;
		margin-top: 10rpx;
		font-size: 26rpx;
		color: $soft;
		line-height: 1.65;
	}

	.note {
		display: block;
		padding: 8rpx 0 8rpx 24rpx;
		position: relative;
		font-size: 26rpx;
		line-height: 1.7;
		color: #3d3a44;

		&::before {
			content: '';
			position: absolute;
			left: 0;
			top: 22rpx;
			width: 8rpx;
			height: 8rpx;
			border-radius: 50%;
			background: $purple;
			opacity: 0.55;
		}

		& + & {
			margin-top: 4rpx;
		}
	}
</style>
