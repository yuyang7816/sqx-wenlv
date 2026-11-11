<template>
	<view class="route-store-page">
		<view class="route-store-banner">
			<swiper v-if="bannerList.length" class="route-store-banner__swiper" style="height: 420rpx;" circular
				:autoplay="bannerList.length > 1" :indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)" indicator-active-color="#FFFFFF">
				<swiper-item v-for="(img, index) in bannerList" :key="index" class="route-store-banner__item">
					<image class="route-store-banner__img" :src="img" mode="aspectFill"
						@click="previewImages(bannerList, index)" />
				</swiper-item>
			</swiper>
			<image v-else class="route-store-banner__img" :src="store.cover" mode="aspectFill" />
		</view>

		<view class="route-store-body">
			<view class="route-store-card ry-card">
				<view class="route-store-card__head">
					<text class="route-store-card__name">{{ store.name || '门店' }}</text>
					<view v-if="store.wechatQrCode" class="route-store-card__wx" @click="openWechat">
						<u-icon name="weixin-fill" color="#07C160" :size="16" />
						<text>添加微信</text>
					</view>
				</view>
				<view v-if="store.tags.length" class="route-store-tags">
					<text v-for="(tag, index) in store.tags" :key="index" class="route-store-tags__item">{{ tag }}</text>
				</view>
				<view v-if="store.address" class="route-store-card__row" @click="openNavigate">
					<u-icon name="map-fill" :color="routeColor" :size="15" />
					<text class="route-store-card__text route-store-card__text--link"
						style="color: #333333;font-weight: 500;">{{ store.address }}</text>
					<!-- <u-icon name="arrow-right" color="#C0C4CC" :size="12" /> -->
					<view
						style="width: max-content;padding: 4rpx 16rpx;background-color: #ff6600;color: #fff;border-radius: 30rpx;font-size: 24rpx;text-align: center;">
						导航
					</view>
				</view>
				<view v-if="store.hours" class="route-store-card__row">
					<u-icon name="clock-fill" :color="routeColor" :size="15" />
					<text class="route-store-card__text" style="color: #333333;font-weight: 500;">{{ store.hours }}</text>
				</view>
				<view v-if="store.phone" class="route-store-card__row" @click="callPhone">
					<u-icon name="phone-fill" :color="routeColor" :size="15" />
					<text class="route-store-card__text route-store-card__text--link"
						style="color: #333333;font-weight: 500;">{{ store.phone }}</text>
				</view>
			</view>

			<view v-if="store.intro" class="route-store-card ry-card">
				<text class="route-store-card__title">{{ ui.labelStoreIntro }}</text>
				<text class="route-store-card__intro">{{ store.intro }}</text>
			</view>

			<view v-if="store.guides.length" class="route-store-card ry-card">
				<text class="route-store-card__title">{{ ui.sectionGuides }}</text>
				<view class="route-store-guides">
					<view v-for="(guide, index) in store.guides" :key="index" class="route-store-guide">
						<image class="route-store-guide__avatar" :src="guide.avatar || placeholderLogo" mode="aspectFill"
							@click="guide.avatar && previewImages([guide.avatar], 0)" />
						<text class="route-store-guide__name">{{ guide.name || '导游' }}</text>
					</view>
				</view>
			</view>

			<view class="route-store-card ry-card">
				<view class="route-store-card__section-head">
					<view class="route-store-card__section-left">
						<u-icon name="chat-fill" :color="routeColor" :size="16" />
						<text class="route-store-card__title route-store-card__title--inline">{{ reviewUi.reviewSection }}</text>
					</view>
					<view v-if="reviewList.length" class="route-store-card__more" @click="goReviews">
						<text class="route-store-card__more-text">{{ reviewUi.reviewAll }}</text>
						<u-icon name="arrow-right" :color="routeColor" :size="12" />
					</view>
				</view>
				<view v-if="!reviewList.length" class="route-store-review-empty">
					<text class="route-store-review-empty__text">{{ reviewUi.reviewEmpty }}</text>
				</view>
				<view v-for="review in previewReviews" :key="review.id" class="route-store-review">
					<view class="route-store-review__head">
						<view class="route-store-review__user">
							<image class="route-store-review__avatar" :src="resolveAvatar(review.avatar)" mode="aspectFill" />
							<text class="route-store-review__name">{{ review.user }}</text>
						</view>
						<view class="route-store-review__stars">
							<u-icon v-for="index in 5" :key="index" :name="index <= review.rating ? 'star-fill' : 'star'"
								:color="index <= review.rating ? routeColor : '#E5E7EB'" :size="12" />
						</view>
					</view>
					<text class="route-store-review__content">{{ review.content }}</text>
					<view v-if="review.images && review.images.length" class="route-store-review__images">
						<image v-for="(img, idx) in review.images" :key="idx" class="route-store-review__image" :src="img"
							mode="aspectFill" @click="previewImages(review.images, idx)" />
					</view>
					<text class="route-store-review__time">{{ review.time }}</text>
				</view>
			</view>

			<view class="route-store-card ry-card">
				<view class="route-store-card__section-head">
					<text class="route-store-card__title route-store-card__title--inline">{{ ui.sectionStoreRoutes }}</text>
					<view v-if="merchantId" class="route-store-card__more" @click="goAllRoutes">
						<text class="route-store-card__more-text">{{ ui.viewAllRoutes }}</text>
						<u-icon name="arrow-right" :color="routeColor" :size="12" />
					</view>
				</view>

				<view v-if="storeRoutes.length" class="route-store-routes">
					<view v-for="item in storeRoutes" :key="item.id" class="route-store-route" @click="openRouteDetail(item)">
						<image class="route-store-route__cover" :src="item.cover" mode="aspectFill" />
						<view class="route-store-route__main">
							<text class="route-store-route__name">{{ item.name }}</text>
							<text v-if="item.desc" class="route-store-route__desc">{{ item.desc }}</text>
							<view class="route-store-route__foot">
								<view class="route-store-route__price">
									<text class="route-store-route__symbol">¥</text>
									<text class="route-store-route__amount">{{ item.priceFrom }}</text>
									<text class="route-store-route__suffix">起</text>
								</view>
								<text v-if="item.duration" class="route-store-route__duration">{{ item.duration }}</text>
							</view>
						</view>
					</view>
				</view>
				<view v-else-if="!routesLoading" class="route-store-routes-empty">
					<text class="route-store-routes-empty__text">{{ ui.emptyStoreRoutes }}</text>
				</view>
			</view>
		</view>

		<u-popup :show="wechatVisible" mode="center" :round="28" :close-on-click-overlay="true" bg-color="transparent"
			@close="closeWechat">
			<view class="route-wechat-popup">
				<view class="route-wechat-popup__body">
					<text class="route-wechat-popup__subtitle">{{ store.name || ui.wechatPopupSubtitle }}</text>
					<view class="route-wechat-popup__qr-wrap">
						<image v-if="store.wechatQrCode" class="route-wechat-popup__img" :src="store.wechatQrCode" mode="aspectFill"
							show-menu-by-longpress @click="previewWechatQr" />
					</view>
					<text class="route-wechat-popup__tip">{{ ui.wechatQrTip }}</text>
					<view class="route-wechat-popup__btn" @click="closeWechat">
						<text class="route-wechat-popup__btn-text">我知道了</text>
					</view>
				</view>
				<view class="route-wechat-popup__close" @click="closeWechat">
					<u-icon name="close-circle-fill" color="rgba(255,255,255,0.85)" :size="28" />
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script setup>
	import {
		computed,
		nextTick,
		ref
	} from 'vue';
	import {
		onLoad
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO,
		getModuleColor
	} from '@/common/theme.js';
	import {
		normalizeMediaList,
		normalizeMediaUrl
	} from '@/common/media-url.js';
	import {
		ROUTE_UI
	} from '@/common/route-ui.js';
	import {
		fetchRouteDetail,
		fetchRouteList,
		fetchRouteStoreReviews,
		mapRouteReviewItem
	} from '@/common/api/route.js';
	import {
		ORDER_UI
	} from '@/common/order-ui.js';
	import {
		resolveAvatar
	} from '@/common/avatar.js';

	const ui = ROUTE_UI;
	const reviewUi = ORDER_UI;
	const routeColor = getModuleColor('route');
	const PREVIEW_LIMIT = 3;
	const routeId = ref('');
	const merchantId = ref('');
	const wechatVisible = ref(false);
	const routesLoading = ref(false);
	const storeRouteTotal = ref(0);
	const storeRoutes = ref([]);
	const reviewList = ref([]);
	const placeholderLogo = PLACEHOLDER_LOGO;
	const previewReviews = computed(() => reviewList.value.slice(0, 2));
	const store = ref({
		name: '',
		cover: PLACEHOLDER_LOGO,
		banners: [],
		tags: [],
		address: '',
		latitude: null,
		longitude: null,
		hours: '',
		phone: '',
		intro: '',
		guides: [],
		wechatQrCode: ''
	});

	const bannerList = computed(() => {
		const list = (store.value.banners || []).filter(Boolean);
		if (list.length) return list;
		return store.value.cover ? [store.value.cover] : [];
	});

	function previewImages(urls, current = 0) {
		const list = (urls || []).filter(Boolean);
		if (!list.length) return;
		uni.previewImage({
			urls: list,
			current: list[current] || list[0]
		});
	}

	function callPhone() {
		const phone = String(store.value.phone || '').trim();
		if (!phone) return;
		uni.makePhoneCall({
			phoneNumber: phone
		});
	}

	function openNavigate() {
		const address = String(store.value.address || '').trim();
		if (!address) return;
		const latitude = Number(store.value.latitude);
		const longitude = Number(store.value.longitude);
		if (!Number.isFinite(latitude) || !Number.isFinite(longitude) || (latitude === 0 && longitude === 0)) {
			uni.showToast({
				title: address,
				icon: 'none'
			});
			return;
		}
		uni.openLocation({
			latitude,
			longitude,
			name: store.value.name || '门店',
			address,
			fail: () => {
				uni.showToast({
					title: address,
					icon: 'none'
				});
			}
		});
	}

	function openWechat() {
		if (!store.value.wechatQrCode) return;
		wechatVisible.value = true;
	}

	function closeWechat() {
		wechatVisible.value = false;
	}

	function previewWechatQr() {
		const qr = String(store.value.wechatQrCode || '').trim();
		if (!qr) return;
		wechatVisible.value = false;
		nextTick(() => {
			setTimeout(() => {
				uni.previewImage({
					urls: [qr],
					current: qr
				});
			}, 220);
		});
	}

	function mapTags(tags) {
		if (!Array.isArray(tags)) return [];
		return tags
			.map((tag) => (typeof tag === 'string' ? tag : tag?.text || ''))
			.map((text) => String(text || '').trim())
			.filter(Boolean);
	}

	function mapRouteItem(record) {
		const tags = Array.isArray(record.tags) ?
			record.tags.map((tag) => ({
				text: tag.text || tag,
				type: tag.type || ''
			})) : [];
		return {
			id: record.id,
			name: record.name || '',
			desc: record.desc || '',
			priceFrom: record.priceFrom != null ? Number(record.priceFrom) : 0,
			duration: record.duration || record.days || '',
			tags,
			cover: normalizeMediaUrl(record.cover) || PLACEHOLDER_LOGO
		};
	}

	function goReviews() {
		if (!merchantId.value) return;
		const title = encodeURIComponent(store.value.name || '门店');
		uni.navigateTo({
			url: `/mobile-design/route/review-list?merchantId=${merchantId.value}&title=${title}`
		});
	}

	async function loadStoreReviews() {
		if (!merchantId.value) {
			reviewList.value = [];
			return;
		}
		try {
			const res = await fetchRouteStoreReviews(merchantId.value, { page: 1, limit: 2 });
			if (res.code === 0 && res.data) {
				reviewList.value = (res.data.records || []).map(mapRouteReviewItem);
			} else {
				reviewList.value = [];
			}
		} catch (e) {
			reviewList.value = [];
		}
	}

	async function loadStoreRoutes() {
		if (!merchantId.value) {
			storeRoutes.value = [];
			storeRouteTotal.value = 0;
			return;
		}
		routesLoading.value = true;
		try {
			const res = await fetchRouteList({
				merchantId: merchantId.value,
				page: 1,
				limit: PREVIEW_LIMIT
			});
			if (res.code === 0 && res.data) {
				storeRoutes.value = (res.data.records || []).map(mapRouteItem);
				storeRouteTotal.value = Number(res.data.total ?? storeRoutes.value.length);
			} else {
				storeRoutes.value = [];
				storeRouteTotal.value = 0;
			}
		} catch (e) {
			storeRoutes.value = [];
			storeRouteTotal.value = 0;
		} finally {
			routesLoading.value = false;
		}
	}

	function openRouteDetail(item) {
		if (!item?.id) return;
		uni.navigateTo({
			url: `/mobile-design/route/detail?id=${item.id}`
		});
	}

	function goAllRoutes() {
		if (!merchantId.value) {
			uni.showToast({
				title: '暂无门店信息',
				icon: 'none'
			});
			return;
		}
		const name = encodeURIComponent(store.value.name || '');
		uni.navigateTo({
			url: `/mobile-design/route/store-routes?merchantId=${merchantId.value}&storeName=${name}`
		});
	}

	async function loadStore() {
		if (!routeId.value) return;
		try {
			const res = await fetchRouteDetail(routeId.value);
			if (res.code !== 0 || !res.data) {
				uni.showToast({
					title: res.msg || '门店信息加载失败',
					icon: 'none'
				});
				return;
			}
			const data = res.data;
			const banners = normalizeMediaList(data.storeBanners);
			const cover = normalizeMediaUrl(data.storeCover) || banners[0] || PLACEHOLDER_LOGO;
			merchantId.value = data.merchantId ? String(data.merchantId) : '';
			store.value = {
				name: data.storeName || '',
				cover,
				banners: banners.length ? banners : (cover ? [cover] : []),
				tags: mapTags(data.storeTags?.length ? data.storeTags : data.tags),
				address: data.storeAddress || '',
				latitude: data.storeLatitude != null ? Number(data.storeLatitude) : null,
				longitude: data.storeLongitude != null ? Number(data.storeLongitude) : null,
				hours: data.storeHours || '',
				phone: data.contactPhone || '',
				intro: data.storeIntro || '',
				guides: Array.isArray(data.guides) ?
					data.guides.map((guide) => ({
						name: guide.name || '',
						avatar: normalizeMediaUrl(guide.avatar)
					})).filter((guide) => guide.name || guide.avatar) : [],
				wechatQrCode: normalizeMediaUrl(data.wechatQrCode || data.wechatQr)
			};
			if (store.value.name) {
				uni.setNavigationBarTitle({
					title: store.value.name
				});
			}
			await Promise.all([loadStoreRoutes(), loadStoreReviews()]);
		} catch (e) {
			uni.showToast({
				title: '加载失败，请稍后重试',
				icon: 'none'
			});
		}
	}

	onLoad((options) => {
		routeId.value = options?.id ? String(options.id) : '';
		loadStore();
	});
</script>

<style lang="scss" scoped>
	.route-store-page {
		min-height: 100vh;
		background: #fff9f0;
		padding-bottom: calc(40rpx + env(safe-area-inset-bottom));
	}

	.route-store-banner {
		height: 420rpx;
		background: #f0f0f0;

		&__swiper,
		&__item,
		&__img {
			width: 100%;
			height: 420rpx;
			display: block;
		}
	}

	.route-store-body {
		margin-top: -32rpx;
		padding: 0 var(--ry-page-padding-x);
		position: relative;
		z-index: 2;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.route-store-card {
		padding: 28rpx 24rpx;

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 16rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 36rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__wx {
			flex-shrink: 0;
			display: inline-flex;
			align-items: center;
			gap: 6rpx;
			padding: 10rpx 18rpx;
			border-radius: 999rpx;
			background: #e8f8ef;
			border: 2rpx solid rgba(7, 193, 96, 0.2);
			font-size: 22rpx;
			font-weight: 600;
			color: #07c160;
			line-height: 1;
		}

		&__title {
			display: block;
			font-size: 30rpx;
			font-weight: 700;
			color: #1a1a1a;
			margin-bottom: 16rpx;

			&--inline {
				margin-bottom: 0;
			}
		}

		&__section-head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 16rpx;
		}

		&__section-left {
			display: flex;
			align-items: center;
			gap: 10rpx;
		}

		&__more {
			display: flex;
			align-items: center;
			gap: 4rpx;
			flex-shrink: 0;
			padding: 8rpx 0 8rpx 12rpx;
		}

		&__more-text {
			font-size: 24rpx;
			color: var(--ry-module-route, #ff6600);
			line-height: 1;
		}

		&__row {
			display: flex;
			align-items: baseline;
			gap: 10rpx;
			margin-top: 14rpx;
		}

		&__text {
			flex: 1;
			min-width: 0;
			font-size: 26rpx;
			color: #333333;
			line-height: 1.5;

			&--link {
				color: var(--ry-module-route, #ff6600);
				font-weight: 600;
			}
		}

		&__intro {
			display: block;
			font-size: 26rpx;
			color: #666666;
			line-height: 1.7;
			white-space: pre-wrap;
		}
	}

	.route-store-tags {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-bottom: 8rpx;

		&__item {
			padding: 8rpx 18rpx;
			border-radius: 999rpx;
			background: #fff3eb;
			font-size: 22rpx;
			color: var(--ry-module-route, #ff6600);
			line-height: 1.4;
		}
	}

	.route-store-guides {
		display: flex;
		flex-wrap: wrap;
		gap: 24rpx;
	}

	.route-store-guide {
		width: 128rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;

		&__avatar {
			width: 112rpx;
			height: 112rpx;
			border-radius: 50%;
			background: #f0f0f0;
		}

		&__name {
			width: 100%;
			font-size: 24rpx;
			color: #1a1a1a;
			text-align: center;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}
	}

	.route-store-routes {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.route-store-route {
		display: flex;
		gap: 20rpx;

		&__cover {
			width: 180rpx;
			height: 160rpx;
			border-radius: 14rpx;
			background: #f0f0f0;
			flex-shrink: 0;
		}

		&__main {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			justify-content: space-between;
			padding: 4rpx 0;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.35;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			line-clamp: 2;
			overflow: hidden;
		}

		&__desc {
			margin-top: 8rpx;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__foot {
			margin-top: 10rpx;
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 12rpx;
		}

		&__price {
			display: flex;
			align-items: baseline;
		}

		&__symbol {
			font-size: 22rpx;
			font-weight: 600;
			color: #ff6600;
		}

		&__amount {
			font-size: 32rpx;
			font-weight: 800;
			color: #ff6600;
			line-height: 1;
		}

		&__suffix {
			margin-left: 2rpx;
			font-size: 20rpx;
			color: #999999;
		}

		&__duration {
			font-size: 22rpx;
			color: #999999;
			flex-shrink: 0;
		}
	}

	.route-store-routes-empty {
		padding: 24rpx 0 8rpx;
		text-align: center;

		&__text {
			font-size: 24rpx;
			color: #999999;
		}
	}

	.route-store-review-empty {
		padding: 24rpx 0;
		text-align: center;

		&__text {
			font-size: 26rpx;
			color: #999;
		}
	}

	.route-store-review {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f5f5f5;

		&:last-child {
			border-bottom: none;
		}

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 12rpx;
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
			color: #1a1a1a;
		}

		&__stars {
			display: flex;
			gap: 4rpx;
			flex-shrink: 0;
		}

		&__content {
			display: block;
			font-size: 26rpx;
			line-height: 1.6;
			color: #333;
		}

		&__images {
			display: flex;
			flex-wrap: wrap;
			gap: 12rpx;
			margin-top: 12rpx;
		}

		&__image {
			width: 140rpx;
			height: 140rpx;
			border-radius: 12rpx;
			background: #f5f5f5;
		}

		&__time {
			display: block;
			margin-top: 10rpx;
			font-size: 22rpx;
			color: #aaa;
		}
	}

	.route-wechat-popup {
		position: relative;
		width: 600rpx;
		padding-bottom: 72rpx;

		&__icon {
			width: 88rpx;
			height: 88rpx;
			border-radius: 50%;
			background: #07c160;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 16rpx;
		}

		&__title {
			font-size: 34rpx;
			font-weight: 700;
			color: #ffffff;
			line-height: 1.3;
		}

		&__subtitle {
			margin-top: 10rpx;
			font-size: 30rpx;
			color: #333;
			font-weight: 600;
			line-height: 1.4;
			text-align: center;
			max-width: 480rpx;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
			margin-bottom: 20rpx;
		}

		&__body {
			margin-top: -20rpx;
			padding: 36rpx 36rpx 40rpx;
			border-radius: 28rpx;
			background: #ffffff;
			display: flex;
			flex-direction: column;
			align-items: center;
			box-shadow: 0 16rpx 40rpx rgba(7, 193, 96, 0.12);
		}

		&__qr-wrap {
			width: 400rpx;
			height: 400rpx;
			padding: 0rpx;
			border-radius: 20rpx;
			background: #f7faf8;
			border: 2rpx solid #e8f5ee;
			box-sizing: border-box;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__img {
			width: 100%;
			height: 100%;
			border-radius: 20rpx;
		}

		&__tip {
			margin-top: 24rpx;
			font-size: 24rpx;
			color: #888888;
			line-height: 1.5;
			text-align: center;
		}

		&__btn {
			margin-top: 28rpx;
			width: 100%;
			height: 80rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #1aad19, #07c160);
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 600;
			color: #ffffff;
		}

		&__close {
			position: absolute;
			left: 50%;
			bottom: 0;
			transform: translateX(-50%);
			width: 56rpx;
			height: 56rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}
	}
</style>
