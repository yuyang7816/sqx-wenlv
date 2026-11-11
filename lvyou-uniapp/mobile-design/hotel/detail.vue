<template>
	<view class="hotel-detail-page">
		<view class="hotel-cover">
			<swiper class="hotel-cover__swiper" :indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)" indicator-active-color="#FFFFFF" :autoplay="bannerList.length > 1"
				:circular="true" :interval="4000" duration="300" @change="onBannerChange">
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="hotel-cover__item">
					<image class="hotel-cover__img ry-placeholder-img" :src="item" mode="aspectFill" />
				</swiper-item>
			</swiper>
			<view class="hotel-cover__mask" />
			<view v-if="hotel.starLabel" class="hotel-cover__badge">
				<text class="hotel-cover__badge-text">{{ hotel.starLabel }}</text>
			</view>
			<view class="hotel-cover__favorite" @click="onFavorite">
				<u-icon :name="favorited ? 'heart-fill' : 'heart'" :color="favorited ? '#FF4757' : '#FFFFFF'" :size="22" />
			</view>
			<view v-if="bannerList.length > 1" class="hotel-cover__count">
				<text class="hotel-cover__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text>
			</view>
		</view>

		<view class="hotel-detail-body">
			<view class="hotel-info ry-card">
				<view class="hotel-info__head">
					<text class="hotel-info__name">{{ hotel.name }}</text>
					<view class="hotel-info__price">
						<text class="hotel-info__price-symbol">¥</text>
						<text class="hotel-info__price-value">{{ priceFrom }}</text>
						<text class="hotel-info__price-suffix">起</text>
					</view>
				</view>

				<view class="hotel-info__rating">
					<u-rate :model-value="hotel.starLevel" :count="5" :readonly="true" :allow-half="false" active-color="#FFB800"
						inactive-color="#E8E8E8" :size="14" :gutter="3" />
					<text class="hotel-info__rating-label">{{ hotel.starLabel }}</text>
				</view>

				<view class="hotel-info__addr" @click="onAddressClick">
					<view class="hotel-info__addr-icon">
						<u-icon name="map-fill" :color="accentColor" :size="14" />
					</view>
					<text class="hotel-info__addr-text">{{ hotel.address }}</text>
					<u-icon name="arrow-right" :size="12" color="#CCCCCC" />
				</view>

				<view class="hotel-info__meta">
					<view class="hotel-info__meta-item">
						<u-icon name="clock-fill" :color="accentColor" :size="15" />
						<text class="hotel-info__meta-label">入住时间</text>
						<text class="hotel-info__meta-value">{{ hotel.checkInTime }}</text>
					</view>
					<view class="hotel-info__meta-divider" />
					<view class="hotel-info__meta-item">
						<u-icon name="clock-fill" :color="accentColor" :size="15" />
						<text class="hotel-info__meta-label">退房时间</text>
						<text class="hotel-info__meta-value">{{ hotel.checkOutTime }}</text>
					</view>
					<view class="hotel-info__meta-divider" />
					<view class="hotel-info__meta-item">
						<u-icon name="phone-fill" :color="accentColor" :size="15" />
						<text class="hotel-info__meta-label">联系电话</text>
						<text class="hotel-info__meta-value">{{ hotel.phone }}</text>
					</view>
				</view>
			</view>

			<view v-if="hotel.intro" class="hotel-section ry-card">
				<view class="hotel-section__head">
					<view class="hotel-section__icon">
						<u-icon name="file-text-fill" :color="accentColor" :size="16" />
					</view>
					<text class="hotel-section__title">酒店介绍</text>
				</view>
				<text class="hotel-section__para">{{ hotel.intro }}</text>
			</view>

			<view class="hotel-section ry-card">
				<view class="hotel-section__head">
					<view class="hotel-section__icon">
						<u-icon name="grid-fill" :color="accentColor" :size="16" />
					</view>
					<text class="hotel-section__title">酒店设施</text>
				</view>
				<view class="hotel-facilities">
					<view v-for="item in hotel.facilities" :key="item.id" class="hotel-facilities__item">
						<view class="hotel-facilities__icon-wrap">
							<u-icon :name="item.icon" :color="accentColor" :size="22" />
						</view>
						<text class="hotel-facilities__label">{{ item.label }}</text>
					</view>
				</view>
			</view>

			<view v-if="hasHotelPolicy" class="hotel-section ry-card">
				<view class="hotel-section__head">
					<view class="hotel-section__icon">
						<u-icon name="info-circle-fill" :color="accentColor" :size="16" />
					</view>
					<text class="hotel-section__title">酒店政策</text>
				</view>
				<view class="hotel-policy">
					<view v-if="hotel.policy.bookingNotice" class="hotel-policy__block">
						<text class="hotel-policy__label">预订提示</text>
						<text class="hotel-policy__text">{{ hotel.policy.bookingNotice }}</text>
					</view>
					<view v-if="hotel.policy.checkInNotice" class="hotel-policy__block">
						<text class="hotel-policy__label">入住提示</text>
						<text class="hotel-policy__text">{{ hotel.policy.checkInNotice }}</text>
					</view>
					<view v-if="hotel.policy.depositPolicy" class="hotel-policy__row">
						<text class="hotel-policy__row-label">押金</text>
						<text class="hotel-policy__row-value">{{ hotel.policy.depositPolicy }}</text>
					</view>
					<view v-if="hotel.policy.breakfastInfo" class="hotel-policy__row">
						<text class="hotel-policy__row-label">早餐</text>
						<text class="hotel-policy__row-value">{{ hotel.policy.breakfastInfo }}</text>
					</view>
				</view>
			</view>

			<view class="hotel-section ry-card">
				<view class="hotel-section__head hotel-section__head--row">
					<view class="hotel-section__head-left">
						<view class="hotel-section__icon">
							<u-icon name="chat-fill" :color="accentColor" :size="16" />
						</view>
						<text class="hotel-section__title">{{ ui.reviewSection }}</text>
					</view>
					<view v-if="reviewList.length" class="hotel-section__more" @click="goReviews">
						<text class="hotel-section__more-text">{{ ui.reviewAll }}</text>
						<u-icon name="arrow-right" :color="accentColor" :size="12" />
					</view>
				</view>
				<view v-if="!reviewList.length" class="hotel-review-empty">
					<text class="hotel-review-empty__text">{{ ui.reviewEmpty }}</text>
				</view>
				<view v-for="review in previewReviews" :key="review.id" class="hotel-review">
					<view class="hotel-review__head">
						<view class="hotel-review__user">
							<image class="hotel-review__avatar" :src="resolveAvatar(review.avatar)" mode="aspectFill" />
							<text class="hotel-review__name">{{ review.user }}</text>
						</view>
						<view class="hotel-review__stars">
							<u-icon v-for="index in 5" :key="index" :name="index <= review.rating ? 'star-fill' : 'star'"
								:color="index <= review.rating ? accentColor : '#E5E7EB'" :size="12" />
						</view>
					</view>
					<text class="hotel-review__content">{{ review.content }}</text>
					<view v-if="review.images && review.images.length" class="hotel-review__images">
						<image v-for="(img, idx) in review.images" :key="idx" class="hotel-review__image" :src="img"
							mode="aspectFill" @click="previewImages(review.images, idx)" />
					</view>
					<text class="hotel-review__time">{{ review.time }}</text>
				</view>
			</view>

			<view class="hotel-section ry-card">
				<view class="hotel-section__head">
					<view class="hotel-section__icon">
						<u-icon name="home-fill" :color="accentColor" :size="16" />
					</view>
					<text class="hotel-section__title">房型推荐</text>
				</view>
				<view class="hotel-rooms">
					<view v-for="room in hotel.roomList" :key="room.id" class="hotel-room" @click="onRoomDetail(room)">
						<view class="hotel-room__thumb-wrap">
							<image class="hotel-room__thumb ry-placeholder-img" :src="room.coverImage || PLACEHOLDER_LOGO"
								mode="aspectFill" />
						</view>
						<view class="hotel-room__info">
							<text class="hotel-room__name">{{ room.name }}</text>
							<text class="hotel-room__spec">{{ room.spec }}</text>
							<view class="hotel-room__foot">
								<view class="hotel-room__price-row">
									<text class="hotel-room__symbol">¥</text>
									<text class="hotel-room__price">{{ room.price }}</text>
									<text class="hotel-room__unit">/晚</text>
								</view>
								<view class="hotel-room__btn" @click.stop="onRoomBook(room)">
									<text class="hotel-room__btn-text">预订</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<text class="hotel-footer">品质住宿 · 舒心之选</text>
		</view>

		<view class="hotel-bar">
			<view class="hotel-bar__btn hotel-bar__btn--outline" @click="onContactFrontDesk">
				<u-icon name="phone-fill" :color="accentColor" :size="18" />
				<text class="hotel-bar__btn-text hotel-bar__btn-text--outline">联系前台</text>
			</view>
			<view class="hotel-bar__btn hotel-bar__btn--primary" @click="onBookNow">
				<u-icon name="calendar-fill" color="#FFFFFF" :size="16" />
				<text class="hotel-bar__btn-text">立即预定</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onLoad,
		onShow
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO,
		getModuleColor
	} from '@/common/theme.js';
	import {
		resolveAvatar
	} from '@/common/avatar.js';
	import {
		checkFavorited,
		toggleFavoriteItem,
		logFootprint
	} from '@/common/asset-detail.js';
	import {
		fetchHotelDetail,
		fetchHotelRooms,
		fetchHotelReviews,
		mapReviewItem
	} from '@/common/api/hotel.js';
	import {
		ORDER_UI
	} from '@/common/order-ui.js';

	const accentColor = getModuleColor('hotel');
	const ui = ORDER_UI;

	const BOOKING_CONFIRM_PATH = '/mobile-design/booking/hotel-confirm';

	const emptyPolicy = () => ({
		bookingNotice: '',
		checkInNotice: '',
		petPolicy: '',
		depositPolicy: '',
		breakfastInfo: ''
	});

	const emptyHotel = () => ({
		id: '',
		name: '',
		starLevel: 0,
		starLabel: '',
		address: '',
		latitude: 0,
		longitude: 0,
		phone: '',
		checkInTime: '',
		checkOutTime: '',
		bannerImage: '',
		bannerImages: [],
		facilities: [],
		intro: '',
		policy: emptyPolicy(),
		roomList: [],
		priceFrom: null
	});

	const hotelId = ref('');
	const hotel = ref(emptyHotel());
	const reviewList = ref([]);
	const favorited = ref(false);
	const bannerCurrent = ref(0);
	const pageLoading = ref(false);

	const previewReviews = computed(() => reviewList.value.slice(0, 2));

	const bannerList = computed(() => {
		const images = (hotel.value.bannerImages || []).filter(Boolean);
		if (images.length) {
			return images;
		}
		if (hotel.value.bannerImage) {
			return [hotel.value.bannerImage];
		}
		return [PLACEHOLDER_LOGO];
	});

	const hasHotelPolicy = computed(() => {
		const p = hotel.value.policy || {};
		return Boolean(
			p.bookingNotice || p.checkInNotice || p.depositPolicy || p.breakfastInfo
		);
	});

	const priceFrom = computed(() => {
		if (hotel.value.priceFrom != null) {
			return Number(hotel.value.priceFrom);
		}
		const prices = (hotel.value.roomList || []).map((room) => Number(room.price)).filter(Boolean);
		if (!prices.length) {
			return '--';
		}
		return Math.min(...prices);
	});

	const navigateSafe = (url) => {
		uni.navigateTo({
			url
		});
	};

	function goReviews() {
		if (!hotel.value.id) return;
		uni.navigateTo({
			url: `/mobile-design/hotel/review-list?hotelId=${hotel.value.id}&title=${encodeURIComponent(hotel.value.name || '酒店')}`
		});
	}

	function mapRoomItem(room) {
		return {
			id: room.id,
			name: room.name || '',
			spec: room.spec || '',
			price: room.price != null ? String(room.price) : '0',
			coverImage: room.coverImage || PLACEHOLDER_LOGO
		};
	}

	function mapDetailData(data) {
		return {
			id: data.id,
			name: data.name || '',
			starLevel: data.starLevel || 0,
			starLabel: data.starLabel || '',
			address: data.address || '',
			latitude: data.latitude != null ? Number(data.latitude) : 0,
			longitude: data.longitude != null ? Number(data.longitude) : 0,
			phone: data.phone || '',
			checkInTime: data.checkInTime || '',
			checkOutTime: data.checkOutTime || '',
			bannerImage: data.bannerImage || '',
			bannerImages: data.bannerImages || [],
			facilities: data.facilities || [],
			intro: data.intro || '',
			policy: {
				bookingNotice: data.policy?.bookingNotice || '',
				checkInNotice: data.policy?.checkInNotice || '',
				petPolicy: data.policy?.petPolicy || '',
				depositPolicy: data.policy?.depositPolicy || '',
				breakfastInfo: data.policy?.breakfastInfo || ''
			},
			roomList: [],
			priceFrom: data.priceFrom
		};
	}

	/** onShow 并行拉取详情与房型（公开接口） */
	async function loadHotelData() {
		if (!hotelId.value || pageLoading.value) {
			return;
		}
		pageLoading.value = true;
		try {
			const [detailRes, roomsRes, reviewRes] = await Promise.all([
				fetchHotelDetail(hotelId.value),
				fetchHotelRooms(hotelId.value),
				fetchHotelReviews(hotelId.value, {
					page: 1,
					limit: 2
				})
			]);

			if (detailRes.code !== 0 || !detailRes.data) {
				uni.showToast({
					title: detailRes.msg || '酒店详情加载失败',
					icon: 'none'
				});
				return;
			}

			hotel.value = mapDetailData(detailRes.data);

			if (roomsRes.code === 0 && Array.isArray(roomsRes.data)) {
				hotel.value.roomList = roomsRes.data.map(mapRoomItem);
			} else if (Array.isArray(detailRes.data.roomList)) {
				hotel.value.roomList = detailRes.data.roomList.map(mapRoomItem);
			}

			if (reviewRes.code === 0 && reviewRes.data) {
				reviewList.value = (reviewRes.data.records || []).map(mapReviewItem);
			} else {
				reviewList.value = [];
			}

			favorited.value = await checkFavorited('hotel', hotel.value.id);
			logFootprint();
		} catch (e) {
			uni.showToast({
				title: '加载失败，请稍后重试',
				icon: 'none'
			});
		} finally {
			pageLoading.value = false;
		}
	}

	const favoriteItem = () => ({
		moduleType: 'hotel',
		id: hotel.value.id,
		name: hotel.value.name,
		address: hotel.value.address,
		path: `/mobile-design/hotel/detail?id=${hotel.value.id}`,
		price: Number(hotel.value.roomList?.[0]?.price) || Number(hotel.value.priceFrom) || 0,
		desc: hotel.value.starLabel || '',
		cover: hotel.value.bannerImage || hotel.value.bannerImages?.[0] || ''
	});

	onLoad((options) => {
		hotelId.value = options?.id ? String(options.id) : '';
		if (options?.name) {
			uni.setNavigationBarTitle({
				title: decodeURIComponent(options.name)
			});
		}
	});

	onShow(() => {
		if (hotelId.value) {
			loadHotelData();
		}
	});

	function onBannerChange(e) {
		bannerCurrent.value = e.detail.current;
	}

	async function onFavorite() {
		const next = await toggleFavoriteItem(favoriteItem());
		if (next == null) return;
		favorited.value = next;
		uni.showToast({
			title: favorited.value ? '已收藏' : '已取消收藏',
			icon: 'none'
		});
	}

	function onAddressClick() {
		const {
			latitude,
			longitude,
			address,
			name
		} = hotel.value;
		if (!latitude || !longitude) {
			uni.showToast({
				title: address,
				icon: 'none'
			});
			return;
		}
		uni.openLocation({
			latitude,
			longitude,
			name,
			address,
			fail: () => {
				uni.showToast({
					title: address,
					icon: 'none'
				});
			}
		});
	}

	function onContactFrontDesk() {
		const phoneNumber = String(hotel.value.phone || '').trim();
		if (!phoneNumber) {
			uni.showToast({
				title: '暂无联系电话',
				icon: 'none'
			});
			return;
		}
		// #ifdef H5
		window.location.href = `tel:${phoneNumber}`;
		return;
		// #endif
		uni.makePhoneCall({
			phoneNumber,
			fail: () => {
				uni.showToast({
					title: `请拨打 ${phoneNumber}`,
					icon: 'none'
				});
			}
		});
	}

	function buildBookingUrl(room) {
		const {
			id,
			name
		} = hotel.value;
		const targetRoom = room || hotel.value.roomList[0];
		const roomName = targetRoom?.name || '';
		const roomSpec = targetRoom?.spec || '';
		const roomPrice = targetRoom?.price || '398';
		const cover = targetRoom?.coverImage || hotel.value.bannerImage || '';
		const query = [
			`id=${id}`,
			`roomId=${targetRoom?.id || ''}`,
			`title=${encodeURIComponent(name)}`,
			`room=${encodeURIComponent(roomName)}`,
			`subtitle=${encodeURIComponent(roomSpec)}`,
			`price=${roomPrice}`,
			cover ? `cover=${encodeURIComponent(cover)}` : ''
		].filter(Boolean).join('&');
		return `${BOOKING_CONFIRM_PATH}?${query}`;
	}

	function onRoomDetail(room) {
		if (!hotelId.value || !room?.id) return;
		navigateSafe(`/mobile-design/hotel/room-detail?hotelId=${hotelId.value}&roomId=${room.id}`);
	}

	function onRoomBook(room) {
		navigateSafe(buildBookingUrl(room));
	}

	function onBookNow() {
		navigateSafe(buildBookingUrl());
	}

	function previewImages(images, currentIndex) {
		if (!images || !images.length) return;
		uni.previewImage({
			urls: images,
			current: currentIndex
		});
	}
</script>

<style lang="scss" scoped>
	.hotel-detail-page {
		min-height: 100vh;
		background-color: #F0F6FF;
		padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
	}

	.hotel-cover {
		position: relative;
		width: 100%;
		height: 420rpx;
		overflow: hidden;
		background-color: #E8F0FE;

		&__swiper {
			width: 100%;
			height: 420rpx;
		}

		&__item {
			width: 100%;
			height: 420rpx;
			overflow: hidden;
		}

		&__img {
			width: 100%;
			height: 420rpx;
			display: block;
			transform: scale(2.2);
			transform-origin: center center;
		}

		&__mask {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 140rpx;
			background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.32));
			pointer-events: none;
			z-index: 2;
		}

		&__badge {
			position: absolute;
			top: 20rpx;
			left: 24rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 44rpx;
			padding: 0 22rpx;
			box-sizing: border-box;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(255, 102, 0, 0.9);
			z-index: 3;
		}

		&__badge-text {
			font-size: 22rpx;
			line-height: 1;
			color: #FFFFFF;
		}

		&__favorite {
			position: absolute;
			top: 20rpx;
			right: 24rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 72rpx;
			height: 72rpx;
			border-radius: 50%;
			background-color: rgba(0, 0, 0, 0.35);
			z-index: 3;

			&:active {
				opacity: 0.88;
			}
		}

		&__count {
			position: absolute;
			right: 24rpx;
			bottom: 20rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 40rpx;
			padding: 0 16rpx;
			box-sizing: border-box;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(0, 0, 0, 0.38);
			z-index: 3;
		}

		&__count-text {
			font-size: 20rpx;
			line-height: 1;
			color: #FFFFFF;
		}
	}

	.hotel-detail-body {
		padding: 0 var(--ry-page-padding-x) calc(24rpx + env(safe-area-inset-bottom));
		margin-top: -48rpx;
		position: relative;
		z-index: 4;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.hotel-info {
		padding: 28rpx 28rpx 24rpx;
		border-radius: 28rpx;
		box-shadow: 0 8rpx 28rpx rgba(41, 121, 255, 0.1);

		&__head {
			display: flex;
			align-items: flex-start;
			justify-content: space-between;
			gap: 20rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 36rpx;
			font-weight: 700;
			line-height: 1.35;
			color: #1A1A1A;
		}

		&__price {
			flex-shrink: 0;
			display: flex;
			align-items: baseline;
		}

		&__price-symbol {
			font-size: 22rpx;
			font-weight: 700;
			line-height: 1;
			color: #FF4757;
		}

		&__price-value {
			font-size: 36rpx;
			font-weight: 700;
			line-height: 1;
			color: #FF4757;
		}

		&__price-suffix {
			margin-left: 2rpx;
			font-size: 20rpx;
			line-height: 1;
			color: #AAAAAA;
		}

		&__rating {
			display: flex;
			align-items: center;
			gap: 12rpx;
			margin-top: 14rpx;
		}

		&__rating-label {
			font-size: 24rpx;
			line-height: 1;
			color: #999999;
		}

		&__addr {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-top: 20rpx;
			padding: 16rpx 18rpx;
			border-radius: 16rpx;
			background-color: #F7FAFF;
		}

		&__addr-icon {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__addr-text {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			line-height: 1.5;
			color: #666666;
		}

		&__meta {
			display: flex;
			align-items: stretch;
			margin-top: 20rpx;
			padding-top: 20rpx;
			border-top: 1rpx solid #F0F0F0;
		}

		&__meta-item {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 8rpx;
		}

		&__meta-label {
			font-size: 20rpx;
			line-height: 1;
			color: #AAAAAA;
		}

		&__meta-value {
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1.3;
			color: #333333;
			text-align: center;
		}

		&__meta-divider {
			width: 1rpx;
			background-color: #F0F0F0;
			margin: 4rpx 0;
		}
	}

	.hotel-section {
		padding: 28rpx;
		border-radius: 28rpx;
		box-shadow: 0 8rpx 28rpx rgba(41, 121, 255, 0.08);

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 24rpx;
		}

		&__icon {
			display: flex;
			align-items: center;
			justify-content: center;
			width: 44rpx;
			height: 44rpx;
			border-radius: 12rpx;
			background-color: #E8F0FE;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 700;
			line-height: 1.4;
			color: #1A1A1A;
		}

		&__para {
			display: block;
			font-size: 26rpx;
			line-height: 1.7;
			color: #666666;
		}

		&__head--row {
			justify-content: space-between;
		}

		&__head-left {
			display: flex;
			align-items: center;
			gap: 10rpx;
		}

		&__more {
			display: flex;
			align-items: center;
			gap: 4rpx;
		}

		&__more-text {
			font-size: 24rpx;
			color: #FF6600;
		}
	}

	.hotel-review-empty {
		padding: 24rpx 0 8rpx;
		text-align: center;

		&__text {
			font-size: 26rpx;
			color: #999999;
		}
	}

	.hotel-review {
		padding: 20rpx 0;
		border-top: 1rpx solid #F0F0F0;

		&:first-of-type {
			border-top: none;
			padding-top: 0;
		}

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 10rpx;
		}

		&__user {
			display: flex;
			align-items: center;
			gap: 12rpx;
			min-width: 0;
			flex: 1;
		}

		&__avatar {
			width: 56rpx;
			height: 56rpx;
			border-radius: 50%;
			background: #f0f0f0;
			flex-shrink: 0;
		}

		&__name {
			font-size: 26rpx;
			font-weight: 600;
			color: #1A1A1A;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__stars {
			display: flex;
			gap: 4rpx;
			flex-shrink: 0;
		}

		&__content {
			display: block;
			font-size: 26rpx;
			line-height: 1.65;
			color: #333333;
		}

		&__images {
			display: flex;
			flex-wrap: wrap;
			gap: 12rpx;
			margin-top: 12rpx;
		}

		&__image {
			width: 160rpx;
			height: 160rpx;
			border-radius: 12rpx;
			background: #f5f5f5;
			object-fit: cover;
		}

		&__time {
			display: block;
			margin-top: 8rpx;
			font-size: 22rpx;
			color: #AAAAAA;
		}
	}

	.hotel-policy {
		display: flex;
		flex-direction: column;
		gap: 20rpx;

		&__block {
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__label {
			font-size: 26rpx;
			font-weight: 600;
			color: #1A1A1A;
		}

		&__text {
			font-size: 24rpx;
			line-height: 1.65;
			color: #666666;
		}

		&__row {
			display: flex;
			align-items: flex-start;
			gap: 16rpx;
			padding: 16rpx 0;
			border-bottom: 1rpx solid #F0F0F0;

			&:last-child {
				border-bottom: none;
				padding-bottom: 0;
			}
		}

		&__row-label {
			flex-shrink: 0;
			width: 88rpx;
			font-size: 24rpx;
			color: #999999;
		}

		&__row-value {
			flex: 1;
			font-size: 24rpx;
			line-height: 1.6;
			color: #333333;
		}
	}

	.hotel-facilities {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 8rpx;

		&__item {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 12rpx;
			min-width: 0;
		}

		&__icon-wrap {
			display: flex;
			align-items: center;
			justify-content: center;
			width: 72rpx;
			height: 72rpx;
			border-radius: 20rpx;
			background-color: #F7FAFF;
		}

		&__label {
			font-size: 22rpx;
			color: #666666;
			line-height: 1.4;
			text-align: center;
		}
	}

	.hotel-rooms {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.hotel-room {
		display: flex;
		align-items: stretch;
		padding: 20rpx;
		border-radius: 20rpx;
		background-color: #F7FAFF;
		gap: 20rpx;

		&__thumb-wrap {
			flex-shrink: 0;
			width: 148rpx;
			height: 148rpx;
			border-radius: 16rpx;
			overflow: hidden;
			background-color: #E8F0FE;
		}

		&__thumb {
			width: 100%;
			height: 100%;
			display: block;
			transform: scale(2.2);
			transform-origin: center center;
		}

		&__info {
			flex: 1;
			display: flex;
			flex-direction: column;
			min-height: 148rpx;
			min-width: 0;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 600;
			color: #333333;
			line-height: 1.4;
		}

		&__spec {
			margin-top: 8rpx;
			font-size: 22rpx;
			color: #999999;
			line-height: 1.5;
		}

		&__foot {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-top: auto;
			padding-top: 14rpx;
			gap: 16rpx;
		}

		&__price-row {
			display: flex;
			align-items: baseline;
			flex-shrink: 0;
		}

		&__symbol {
			font-size: 24rpx;
			font-weight: 600;
			color: #FF4757;
			line-height: 1;
		}

		&__price {
			font-size: 34rpx;
			font-weight: 700;
			color: #FF4757;
			line-height: 1;
		}

		&__unit {
			margin-left: 2rpx;
			font-size: 20rpx;
			color: #AAAAAA;
			line-height: 1;
		}

		&__btn {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			min-width: 128rpx;
			height: 56rpx;
			padding: 0 36rpx;
			border-radius: 999rpx;
			background: #FF6600;
			// box-shadow: 0 4rpx 14rpx rgba(41, 121, 255, 0.25);

			&:active {
				opacity: 0.88;
			}
		}

		&__btn-text {
			font-size: 24rpx;
			font-weight: 400;
			line-height: 1;
			color: #FFFFFF;
		}
	}

	.hotel-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding: 8rpx 0 4rpx;
	}

	.hotel-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
		background-color: #FFFFFF;
		box-shadow: 0 -4rpx 20rpx rgba(41, 121, 255, 0.08);

		&__btn {
			flex: 1;
			min-width: 0;
			box-sizing: border-box;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 88rpx;
			padding: 0 16rpx;
			border-radius: 999rpx;
			border: 2rpx solid transparent;

			&:active {
				opacity: 0.88;
			}

			&--outline {
				border-color: #FF6600;
				background-color: #FFFFFF;
			}

			&--primary {
				border-color: transparent;
				background: #FF6600;
			}
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 600;
			line-height: 1;
			color: #FFFFFF;

			&--outline {
				color: #FF6600;
			}
		}
	}
</style>
