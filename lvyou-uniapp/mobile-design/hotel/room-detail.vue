<template>
	<view class="rd-page">
		<view v-if="loading" class="rd-loading">
			<u-loading-icon mode="circle" :color="accentColor" />
		</view>
		<template v-else>
			<view class="rd-gallery">
				<swiper
					class="rd-gallery__swiper"
					:indicator-dots="false"
					:autoplay="gallery.length > 1"
					:circular="gallery.length > 1"
					:interval="5000"
					duration="380"
					:current="bannerCurrent"
					@change="onSwiperChange"
				>
					<swiper-item v-for="(item, index) in gallery" :key="index">
						<image
							class="rd-gallery__img"
							:src="item"
							mode="aspectFill"
							@click="previewGallery(index)"
						/>
					</swiper-item>
				</swiper>
				<view v-if="gallery.length > 1" class="rd-gallery__pager">
					<text>{{ bannerCurrent + 1 }}/{{ gallery.length }}</text>
				</view>
				<scroll-view
					v-if="gallery.length > 1"
					scroll-x
					class="rd-gallery__thumbs"
					:show-scrollbar="false"
					:scroll-into-view="'thumb-' + bannerCurrent"
					scroll-with-animation
				>
					<view class="rd-gallery__thumbs-inner">
						<view
							v-for="(item, index) in gallery"
							:id="'thumb-' + index"
							:key="index"
							class="rd-gallery__thumb"
							:class="{ 'rd-gallery__thumb--active': bannerCurrent === index }"
							@click="bannerCurrent = index"
						>
							<image :src="item" mode="aspectFill" class="rd-gallery__thumb-img" />
						</view>
					</view>
				</scroll-view>
			</view>

			<view class="rd-body">
				<text v-if="room.hotelName" class="rd-hotel">{{ room.hotelName }}</text>
				<text class="rd-title">{{ room.name }}</text>

				<view v-if="room.tags?.length" class="rd-tags">
					<template v-for="(tag, index) in room.tags" :key="tag">
						<text v-if="index > 0" class="rd-tags__dot">·</text>
						<text class="rd-tag">{{ tag }}</text>
					</template>
				</view>

				<view v-if="quickStats.length" class="rd-stats">
					<view v-for="item in quickStats" :key="item.label" class="rd-stats__cell">
						<view class="rd-stats__icon">
							<u-icon :name="item.icon" :color="accentColor" :size="15" />
						</view>
						<view class="rd-stats__text">
							<text class="rd-stats__value">{{ item.value }}</text>
							<text class="rd-stats__label">{{ item.label }}</text>
						</view>
					</view>
				</view>

				<view v-if="infoRows.length" class="rd-section">
					<text class="rd-section__title">房型信息</text>
					<view class="rd-info-grid">
						<view v-for="row in infoRows" :key="row.label" class="rd-info-grid__cell">
							<text class="rd-info-grid__value">{{ row.value }}</text>
							<text class="rd-info-grid__label">{{ row.label }}</text>
						</view>
					</view>
				</view>

				<view v-if="room.desc" class="rd-section">
					<text class="rd-section__title">房型说明</text>
					<text class="rd-desc">{{ room.desc }}</text>
				</view>

				<view v-if="room.facilities?.length" class="rd-section rd-section--last">
					<text class="rd-section__title">房间设施</text>
					<view class="rd-facilities">
						<view v-for="item in room.facilities" :key="item.id" class="rd-facilities__pill">
							<u-icon :name="item.icon" :color="accentColor" :size="14" />
							<text>{{ item.label }}</text>
						</view>
					</view>
				</view>
			</view>

			<view class="rd-bar">
				<view class="rd-bar__price-wrap">
					<text class="rd-bar__price-line">
						每晚<text class="rd-bar__symbol">¥</text><text class="rd-bar__value">{{ room.price }}</text>起
					</text>
				</view>
				<view class="rd-bar__btn" @click="onBook">
					<text class="rd-bar__btn-text">立即预订</text>
					<u-icon name="arrow-right" color="#FFF" :size="14" />
				</view>
			</view>
		</template>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { fetchHotelRoomDetail } from '@/common/api/hotel.js';

const accentColor = getModuleColor('hotel');
const BOOKING_CONFIRM_PATH = '/mobile-design/booking/confirm';

const hotelId = ref('');
const roomId = ref('');
const loading = ref(true);
const bannerCurrent = ref(0);
const room = ref({});

const gallery = computed(() => {
	const images = (room.value.gallery || []).filter(Boolean);
	if (images.length) return images;
	if (room.value.coverImage) return [room.value.coverImage];
	return [PLACEHOLDER_LOGO];
});

const bedSummary = computed(() => {
	const parts = [];
	if (room.value.bedCount) parts.push(`${room.value.bedCount}张`);
	if (room.value.bedType) parts.push(room.value.bedType);
	if (room.value.bedSize) parts.push(`${room.value.bedSize}米`);
	return parts.join('');
});

const quickStats = computed(() => {
	const items = [];
	const push = (icon, label, value) => {
		const text = value == null ? '' : String(value).trim();
		if (!text) return;
		items.push({ icon, label, value: text });
	};
	push('grid-fill', '面积', room.value.area ? `${room.value.area}㎡` : '');
	push('account-fill', '可住', room.value.occupancy ? `${room.value.occupancy}人` : '');
	push('home-fill', '床型', bedSummary.value);
	push('bag-fill', '库存', room.value.stock != null ? `${room.value.stock}间` : '');
	return items;
});

const detailRows = computed(() => {
	const rows = [];
	const pushRow = (label, value) => {
		const text = value == null ? '' : String(value).trim();
		if (!text) return;
		rows.push({ label, value: text });
	};
	pushRow('楼层', room.value.floor);
	pushRow('窗户', room.value.windowType);
	pushRow('吸烟政策', room.value.smokingPolicy);
	pushRow('宠物政策', room.value.petPolicy);
	pushRow('加床政策', room.value.extraBedPolicy);
	return rows;
});

const infoRows = computed(() => {
	const shown = new Set(quickStats.value.map((item) => item.label));
	const extra = [];
	const pushRow = (label, value) => {
		const text = value == null ? '' : String(value).trim();
		if (!text) return;
		extra.push({ label, value: text });
	};
	if (!shown.has('面积')) pushRow('面积', room.value.area ? `${room.value.area}㎡` : '');
	if (!shown.has('可住')) pushRow('可住人数', room.value.occupancy ? `${room.value.occupancy}人` : '');
	if (!shown.has('床型')) pushRow('床型', bedSummary.value);
	if (!shown.has('库存')) pushRow('可售库存', room.value.stock != null ? `${room.value.stock}间` : '');
	return [...extra, ...detailRows.value];
});

function onSwiperChange(e) {
	bannerCurrent.value = Number(e?.detail?.current ?? 0);
}

function previewGallery(index) {
	const urls = gallery.value.filter((url) => url && url !== PLACEHOLDER_LOGO);
	if (!urls.length) return;
	const current = Math.min(Math.max(index, 0), urls.length - 1);
	uni.previewImage({ current: urls[current], urls });
}

async function loadRoom() {
	if (!hotelId.value || !roomId.value) return;
	loading.value = true;
	try {
		const res = await fetchHotelRoomDetail(hotelId.value, roomId.value);
		if (res.code === 0 && res.data) {
			room.value = res.data;
		} else {
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function buildBookingUrl() {
	const cover = room.value.coverImage || room.value.gallery?.[0] || '';
	const query = [
		`id=${hotelId.value}`,
		`roomId=${roomId.value}`,
		`title=${encodeURIComponent(room.value.hotelName || '')}`,
		`room=${encodeURIComponent(room.value.name || '')}`,
		`subtitle=${encodeURIComponent(room.value.spec || '')}`,
		`price=${room.value.price || ''}`,
		cover ? `cover=${encodeURIComponent(cover)}` : ''
	].filter(Boolean).join('&');
	return `${BOOKING_CONFIRM_PATH}?${query}`;
}

function onBook() {
	uni.navigateTo({ url: buildBookingUrl() });
}

onLoad((options) => {
	hotelId.value = options?.hotelId || options?.id || '';
	roomId.value = options?.roomId || '';
	uni.setNavigationBarTitle({ title: '房型详情' });
	loadRoom();
});
</script>

<style lang="scss" scoped>
.rd-page {
	min-height: 100vh;
	background: #FFF;
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.rd-loading {
	min-height: 60vh;
	display: flex;
	align-items: center;
	justify-content: center;
}

.rd-gallery {
	position: relative;
	background: #F5F5F5;

	&__swiper,
	&__img {
		width: 100%;
		height: 480rpx;
	}

	&__pager {
		position: absolute;
		top: 24rpx;
		right: 24rpx;
		padding: 8rpx 18rpx;
		border-radius: 999rpx;
		background: rgba(0, 0, 0, 0.45);
		font-size: 22rpx;
		color: #FFF;
		line-height: 1.4;
	}

	&__thumbs {
		width: 100%;
		background: #1A1A1A;
		white-space: nowrap;
	}

	&__thumbs-inner {
		display: inline-flex;
		gap: 12rpx;
		padding: 14rpx 24rpx;
	}

	&__thumb {
		flex-shrink: 0;
		width: 96rpx;
		height: 72rpx;
		border-radius: 8rpx;
		overflow: hidden;
		opacity: 0.45;
		border: 2rpx solid transparent;

		&--active {
			opacity: 1;
			border-color: #FFF;
		}
	}

	&__thumb-img {
		width: 100%;
		height: 100%;
	}
}

.rd-body {
	padding: 32rpx var(--ry-page-padding-x) 24rpx;
	background: #FFF;
}

.rd-hotel {
	display: block;
	font-size: 24rpx;
	color: #999;
	line-height: 1.4;
	margin-bottom: 8rpx;
}

.rd-title {
	display: block;
	font-size: 40rpx;
	font-weight: 700;
	color: #1A1A1A;
	line-height: 1.35;
}

.rd-tags {
	display: flex;
	flex-wrap: wrap;
	align-items: center;
	gap: 8rpx;
	margin-top: 16rpx;

	&__dot {
		font-size: 24rpx;
		color: #D9D9D9;
		line-height: 1;
	}
}

.rd-tag {
	font-size: 24rpx;
	color: #FF6600;
	line-height: 1.4;
}

/* 2×2 网格，左对齐，长文本可换行 */
.rd-stats {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16rpx;
	margin-top: 28rpx;

	&__cell {
		display: flex;
		align-items: flex-start;
		gap: 14rpx;
		min-height: 108rpx;
		padding: 20rpx;
		border-radius: 16rpx;
		background: #F8FAFC;
		box-sizing: border-box;
	}

	&__icon {
		flex-shrink: 0;
		width: 48rpx;
		height: 48rpx;
		border-radius: 12rpx;
		background: #FFF3EB;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__text {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__value {
		font-size: 28rpx;
		font-weight: 400;
		color: #333;
		line-height: 1.45;
		word-break: break-all;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	&__label {
		font-size: 22rpx;
		font-weight: 400;
		color: #999;
		line-height: 1.3;
	}
}

.rd-section {
	margin-top: 40rpx;
	padding-top: 40rpx;
	border-top: 1rpx solid #F0F0F0;

	&--last {
		padding-bottom: 8rpx;
	}

	&__title {
		display: block;
		font-size: 30rpx;
		font-weight: 500;
		color: #333;
		line-height: 1.4;
		margin-bottom: 24rpx;
	}
}

/* 信息卡片网格，非表单 */
.rd-info-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16rpx;

	&__cell {
		padding: 24rpx 20rpx;
		border-radius: 16rpx;
		background: #F8FAFC;
		min-height: 100rpx;
		box-sizing: border-box;
	}

	&__value {
		font-size: 28rpx;
		font-weight: 400;
		color: #333;
		line-height: 1.45;
		word-break: break-all;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	&__label {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		font-weight: 400;
		color: #999;
		line-height: 1.3;
	}
}

.rd-desc {
	display: block;
	font-size: 28rpx;
	line-height: 1.75;
	color: #666;
}

.rd-facilities {
	display: flex;
	flex-wrap: wrap;
	gap: 14rpx;

	&__pill {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 12rpx 20rpx;
		border-radius: 999rpx;
		background: #FFF3EB;
		font-size: 24rpx;
		color: #475569;
	}
}

.rd-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 100;
	display: flex;
	align-items: center;
	gap: 24rpx;
	padding: 16rpx var(--ry-page-padding-x);
	padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	background: #FFF;
	box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.06);

	&__price-wrap {
		flex: 1;
		min-width: 0;
	}

	&__price-line {
		font-size: 26rpx;
		color: #666666;
		line-height: 1.2;
	}

	&__symbol {
		font-size: 28rpx;
		font-weight: 600;
		color: #FF6600;
	}

	&__value {
		font-size: 40rpx;
		font-weight: 700;
		color: #FF6600;
		line-height: 1;
	}

	&__btn {
		flex: 0 0 auto;
		height: 88rpx;
		padding: 0 36rpx;
		border-radius: 44rpx;
		background: #FF6600;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;

		&:active {
			opacity: 0.9;
		}
	}

	&__btn-text {
		font-size: 30rpx;
		font-weight: 400;
		color: #FFF;
	}
}
</style>
