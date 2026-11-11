<template>
	<view class="route-detail-page">
		<view class="route-detail-banner-wrap">
			<swiper v-if="bannerList.length" class="route-detail-banner-swiper" style="height: 400rpx;" circular
				:autoplay="bannerList.length > 1" :indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)" indicator-active-color="#FFFFFF">
				<swiper-item v-for="(img, index) in bannerList" :key="index" class="route-detail-banner-item">
					<image class="route-detail-banner ry-placeholder-img" :src="img" mode="aspectFill"
						@click="previewImages(bannerList, index)" />
				</swiper-item>
			</swiper>
			<image v-else class="route-detail-banner ry-placeholder-img" :src="route.cover" mode="aspectFill" />
			<view class="route-detail-banner__mask" />
		</view>

		<view class="route-detail-body">
			<view class="route-detail-card ry-card">
				<view class="route-detail-card__head">
					<text class="route-detail-card__title">{{ route.name }}</text>
					<view class="route-detail-card__price">
						<text class="route-detail-card__symbol">¥</text>
						<text class="route-detail-card__amount">{{ route.price }}</text>
						<text class="route-detail-card__suffix">起</text>
					</view>
				</view>
				<view v-if="route.tags.length" class="route-detail-card__tags">
					<view v-for="tag in route.tags" :key="tag.text" class="route-detail-card__tag">
						<text class="route-detail-card__tag-text">{{ tag.text }}</text>
					</view>
				</view>
				<text v-if="route.intro" class="route-detail-card__desc">{{ route.intro }}</text>

				<view class="route-detail-meta">
					<view class="route-detail-meta__row">
						<text class="route-detail-meta__label">{{ ui.labelCity }}</text>
						<text class="route-detail-meta__value">{{ route.travelCity || '--' }}</text>
					</view>
					<view class="route-detail-meta__row">
						<text class="route-detail-meta__label">{{ ui.labelDuration }}</text>
						<text class="route-detail-meta__value">{{ route.duration || '--' }}</text>
					</view>
					<view class="route-detail-meta__row route-detail-meta__row--top">
						<text class="route-detail-meta__label">{{ ui.labelMeetPoint }}</text>
						<text class="route-detail-meta__value">{{ route.meetPoint || ui.meetPointDefault }}</text>
					</view>
				</view>
			</view>

			<view class="route-detail-block ry-card">
				<view class="route-detail-block__head">
					<view class="route-detail-block__head-left">
						<view class="route-detail-block__icon">
							<u-icon name="map-fill" :color="routeColor" :size="16" />
						</view>
						<text class="route-detail-block__title">{{ ui.labelSpots }}</text>
					</view>
				</view>
				<view v-if="spotList.length" class="route-detail-spots__list">
					<view v-for="(spot, index) in spotList" :key="index" class="route-detail-spots__chip">
						<text class="route-detail-spots__chip-text">{{ spot }}</text>
					</view>
				</view>
				<text v-else class="route-detail-spots__empty">--</text>
			</view>

			<view class="route-detail-block ry-card">
				<view class="route-detail-block__head">
					<view class="route-detail-block__head-left">
						<view class="route-detail-block__icon">
							<u-icon name="file-text-fill" :color="routeColor" :size="16" />
						</view>
						<text class="route-detail-block__title">{{ ui.sectionItinerary }}</text>
					</view>
					<view v-if="itineraryDays.length" class="route-detail-block__more" @click="goItinerary">
						<text class="route-detail-block__more-text">{{ ui.viewAllItinerary }}</text>
						<u-icon name="arrow-right" :color="routeColor" :size="12" />
					</view>
				</view>

				<up-collapse v-if="itineraryDays.length" class="route-detail-itinerary" accordion :border="false"
					:value="activeItineraryName">
					<up-collapse-item v-for="(day, index) in itineraryDays" :key="day.dayNo || index" :name="String(index)"
						:border="false" :clickable="false" :isLink="true" :cell-custom-style="itineraryCellStyle"
						class="route-detail-itinerary-item">
						<template #title>
							<view class="route-detail-day-title">
								<text class="route-detail-day-title__text">{{ collapseDayTitle(day, index) }}</text>
							</view>
						</template>
						<view class="route-detail-day-panel">
							<text v-if="day.content" class="route-detail-day-panel__content">{{ day.content }}</text>
							<scroll-view v-if="day.images && day.images.length" scroll-x class="route-detail-day-panel__images"
								:show-scrollbar="false">
								<view class="route-detail-day-panel__images-row">
									<image v-for="(img, imgIndex) in day.images" :key="imgIndex"
										class="route-detail-day-panel__img ry-placeholder-img" :src="img" mode="aspectFill"
										@click="previewImages(day.images, imgIndex)" />
								</view>
							</scroll-view>
							<text v-if="!day.content && !(day.images && day.images.length)"
								class="route-detail-day-panel__empty">暂无当天详情</text>
						</view>
					</up-collapse-item>
				</up-collapse>
				<view v-else-if="!pageLoading" class="route-detail-timeline-empty">
					<text class="route-detail-timeline-empty__text">暂无行程安排</text>
				</view>
			</view>

			<view v-if="hasMerchantBlock" class="route-detail-block ry-card" @click="goStore">
				<view class="route-detail-block__head">
					<view class="route-detail-block__head-left">
						<view class="route-detail-block__icon">
							<u-icon name="home-fill" :color="routeColor" :size="16" />
						</view>
						<text class="route-detail-block__title">{{ ui.sectionContact }}</text>
					</view>
					<u-icon name="arrow-right" color="#C0C4CC" :size="14" />
				</view>
				<view class="route-store-entry">
					<image class="route-store-entry__cover ry-placeholder-img" :src="storeCover" mode="aspectFill" />
					<view class="route-store-entry__main">
						<text v-if="route.storeName" class="route-store-entry__name">{{ route.storeName }}</text>
						<scroll-view v-if="storeTagList.length" class="route-store-entry__tags" scroll-x :show-scrollbar="false">
							<view class="route-store-entry__tags-row">
								<text v-for="(tag, index) in storeTagList" :key="index" class="route-store-entry__tag">{{ tag }}</text>
							</view>
						</scroll-view>
						<view v-if="route.storeAddress" class="route-store-entry__addr-row">
							<u-icon name="map-fill" :color="routeColor" :size="13" />
							<text class="route-store-entry__addr">{{ route.storeAddress }}</text>
						</view>
					</view>
				</view>
				<view v-if="guideList.length" class="route-detail-merchant__guides" @click.stop>
					<text class="route-detail-merchant__guides-title">{{ ui.sectionGuides }}</text>
					<scroll-view scroll-x class="route-detail-guides" :show-scrollbar="false">
						<view class="route-detail-guides__row">
							<view v-for="(guide, index) in guideList" :key="index" class="route-detail-guide">
								<image class="route-detail-guide__avatar ry-placeholder-img" :src="guide.avatar || placeholderLogo"
									mode="aspectFill" @click="guide.avatar && previewImages([guide.avatar], 0)" />
								<text class="route-detail-guide__name">{{ guide.name || '导游' }}</text>
							</view>
						</view>
					</scroll-view>
				</view>
			</view>
		</view>

		<view class="route-detail-bar">
			<view class="route-detail-bar__inner">
				<view class="route-detail-bar__collect" @click="onFavorite">
					<u-icon name="heart-fill" :color="favorited ? '#FF4757' : '#C0C4CC'" :size="22" />
					<text class="route-detail-bar__collect-text">{{ favorited ? '已收藏' : '收藏' }}</text>
				</view>
				<view class="route-detail-bar__actions">
					<view v-if="route.wechatQrCode" class="route-detail-bar__wx" @click="openWechat">
						<u-icon name="weixin-fill" color="#07C160" :size="18" />
						<text class="route-detail-bar__wx-text">添加微信</text>
					</view>
					<view class="route-detail-bar__action" @click="onBook">
						<text class="route-detail-bar__btn-text">{{ ui.detailBook }}</text>
					</view>
				</view>
			</view>
		</view>

		<u-popup :show="wechatVisible" mode="center" :round="28" :close-on-click-overlay="true" bg-color="transparent"
			@close="closeWechat">
			<view class="route-wechat-popup">
				<!-- <view class="route-wechat-popup__hero">
					<view class="route-wechat-popup__icon">
						<u-icon name="weixin-fill" color="#FFFFFF" :size="28" />
					</view>
					<text class="route-wechat-popup__title">添加微信</text>
					<text class="route-wechat-popup__subtitle">{{ route.storeName || ui.wechatPopupSubtitle }}</text>
				</view> -->
				<view class="route-wechat-popup__body">
					<!-- <view class="route-wechat-popup__icon">
						<u-icon name="weixin-fill" color="#FFFFFF" :size="28" />
					</view> -->
					<text class="route-wechat-popup__subtitle">{{ route.storeName || ui.wechatPopupSubtitle }}</text>
					<view class="route-wechat-popup__qr-wrap">
						<image v-if="route.wechatQrCode" class="route-wechat-popup__img" :src="route.wechatQrCode" mode="aspectFill"
							show-menu-by-longpress @click="previewImages([route.wechatQrCode], 0)" />
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
		ref
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
		normalizeMediaList,
		normalizeMediaUrl
	} from '@/common/media-url.js';
	import {
		ROUTE_UI
	} from '@/common/route-ui.js';
	import {
		fetchRouteDetail
	} from '@/common/api/route.js';
	import {
		checkFavorited,
		toggleFavoriteItem,
		logFootprint
	} from '@/common/asset-detail.js';

	const ui = ROUTE_UI;
	const routeColor = getModuleColor('route');

	const emptyRoute = () => ({
		id: '',
		name: '',
		desc: '',
		intro: '',
		priceFrom: 0,
		price: 0,
		duration: '',
		dayCount: 0,
		travelCity: '',
		meetPoint: '',
		tags: [],
		spots: [],
		cover: PLACEHOLDER_LOGO,
		banners: [],
		storeCover: '',
		storeBanners: [],
		guides: [],
		wechatQrCode: '',
		contactPhone: '',
		storeName: '',
		storeTags: [],
		storeAddress: '',
		storeHours: '',
		storeIntro: '',
		merchantId: '',
		itineraryDays: []
	});

	const routeId = ref('');
	const route = ref(emptyRoute());
	const favorited = ref(false);
	const pageLoading = ref(false);
	const wechatVisible = ref(false);

	const spotsText = computed(() => {
		const spots = route.value.spots || [];
		return spots.length ? spots.join(' · ') : '--';
	});
	const spotList = computed(() => {
		const raw = route.value.spots || [];
		const list = [];
		raw.forEach((item) => {
			String(item || '')
				.split(/[,，、·]/)
				.map((part) => part.trim())
				.filter(Boolean)
				.forEach((part) => {
					if (!list.includes(part)) list.push(part);
				});
		});
		return list;
	});

	const itineraryDays = computed(() => route.value.itineraryDays || []);
	const activeItineraryName = ref('0');
	const bannerList = computed(() => {
		const banners = Array.isArray(route.value.banners) ? route.value.banners.filter(Boolean) : [];
		if (banners.length) return banners;
		return route.value.cover ? [route.value.cover] : [];
	});
	const guideList = computed(() => Array.isArray(route.value.guides) ? route.value.guides.filter((g) => g && (g.name || g
		.avatar)) : []);
	const storeBannerList = computed(() => Array.isArray(route.value.storeBanners) ? route.value.storeBanners.filter(
		Boolean) : []);
	const storeCover = computed(() => route.value.storeCover || storeBannerList.value[0] || route.value.cover ||
		placeholderLogo);
	const storeTagList = computed(() => {
		const raw = Array.isArray(route.value.storeTags) ? route.value.storeTags : [];
		return raw
			.map((tag) => (typeof tag === 'string' ? tag : tag?.text || ''))
			.map((text) => String(text || '').trim())
			.filter(Boolean);
	});
	const placeholderLogo = PLACEHOLDER_LOGO;
	const itineraryCellStyle = {
		backgroundColor: '#ffffff'
	};
	const hasMerchantBlock = computed(() => !!(
		route.value.storeName ||
		storeTagList.value.length ||
		route.value.storeAddress ||
		route.value.storeHours ||
		route.value.contactPhone ||
		route.value.storeIntro ||
		route.value.wechatQrCode ||
		guideList.value.length
	));

	function collapseDayTitle(day, index) {
		const dayLabel = day.dayLabel || `第${index + 1}天`;
		const title = String(day.title || '').trim();
		return title ? `${dayLabel} · ${title}` : dayLabel;
	}

	function mapDay(day, index = 0) {
		const images = normalizeMediaList(Array.isArray(day.images) ? day.images : (day.cover ? [day.cover] : []));
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
			const images = normalizeMediaList(Array.isArray(node.images) ? node.images : (node.cover ? [node.cover] : []));
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

	function mapDetailData(data) {
		const tags = Array.isArray(data.tags) ?
			data.tags.map((tag) => ({
				text: tag.text || tag,
				type: tag.type || ''
			})) : [];
		const price = data.price != null ? Number(data.price) : Number(data.priceFrom) || 0;
		const dayCount = Number(data.dayCount) || 0;
		const duration = data.duration || data.days || (dayCount > 0 ? `${dayCount}天` : '');
		let itineraryDays = Array.isArray(data.itineraryDays) && data.itineraryDays.length ?
			data.itineraryDays.map(mapDay) :
			mapItineraryToDays(data.itinerary);
		return {
			id: data.id,
			name: data.name || '',
			desc: data.desc || '',
			intro: data.intro || data.desc || '',
			priceFrom: data.priceFrom != null ? Number(data.priceFrom) : price,
			price,
			duration,
			dayCount,
			travelCity: data.travelCity || '',
			meetPoint: data.meetPoint || '',
			tags,
			spots: Array.isArray(data.spots) ? data.spots : [],
			cover: normalizeMediaUrl(data.cover) || PLACEHOLDER_LOGO,
			banners: normalizeMediaList(data.banners),
			storeCover: normalizeMediaUrl(data.storeCover),
			storeBanners: normalizeMediaList(data.storeBanners),
			guides: Array.isArray(data.guides) ?
				data.guides.map((g) => ({
					name: g.name || '',
					avatar: normalizeMediaUrl(g.avatar)
				})).filter((g) => g.name || g.avatar) : [],
			wechatQrCode: normalizeMediaUrl(data.wechatQrCode || data.wechatQr),
			contactPhone: data.contactPhone || '',
			storeName: data.storeName || '',
			storeTags: Array.isArray(data.storeTags) ? data.storeTags : [],
			storeAddress: data.storeAddress || '',
			storeHours: data.storeHours || '',
			storeIntro: data.storeIntro || '',
			merchantId: data.merchantId || '',
			itineraryDays
		};
	}

	function favoriteItem() {
		return {
			moduleType: 'route',
			id: route.value.id,
			name: route.value.name,
			address: route.value.meetPoint || (route.value.spots || []).join(' · '),
			path: `/mobile-design/route/detail?id=${route.value.id}`,
			price: route.value.price || route.value.priceFrom || 0,
			cover: route.value.cover || '',
			desc: route.value.desc || route.value.intro || ''
		};
	}

	async function syncFavoriteState() {
		if (!route.value.id) return;
		favorited.value = await checkFavorited('route', route.value.id);
		logFootprint();
	}

	async function loadRouteDetail() {
		if (!routeId.value || pageLoading.value) {
			return;
		}
		pageLoading.value = true;
		try {
			const res = await fetchRouteDetail(routeId.value);
			if (res.code !== 0 || !res.data) {
				uni.showToast({
					title: res.msg || '线路详情加载失败',
					icon: 'none'
				});
				return;
			}
			route.value = mapDetailData(res.data);
			uni.setNavigationBarTitle({
				title: route.value.name || ui.listTitle
			});
			syncFavoriteState();
		} catch (e) {
			uni.showToast({
				title: '加载失败，请稍后重试',
				icon: 'none'
			});
		} finally {
			pageLoading.value = false;
		}
	}

	function previewImages(urls, current = 0) {
		const list = (urls || []).filter(Boolean);
		wechatVisible.value = false;
		if (!list.length) return;
		uni.previewImage({
			urls: list,
			current: list[current] || list[0]
		});
	}

	function callMerchant() {
		const phone = String(route.value.contactPhone || '').trim();
		if (!phone) return;
		uni.makePhoneCall({
			phoneNumber: phone
		});
	}

	function goItinerary() {
		if (!route.value.id) return;
		uni.navigateTo({
			url: `/mobile-design/route/itinerary?id=${route.value.id}`
		});
	}

	function goStore() {
		if (!route.value.id) return;
		uni.navigateTo({
			url: `/mobile-design/route/store?id=${route.value.id}`
		});
	}

	function openWechat() {
		const qr = String(route.value.wechatQrCode || '').trim();
		if (!qr) return;
		wechatVisible.value = true;
	}

	function closeWechat() {
		wechatVisible.value = false;
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

	onLoad((options) => {
		routeId.value = options?.id ? String(options.id) : '';
	});

	onShow(() => {
		if (routeId.value) {
			loadRouteDetail();
		}
	});

	function onBook() {
		const {
			id,
			name,
			price,
			duration,
			cover,
			desc,
			intro
		} = route.value;
		const query = [
			`id=${id}`,
			`title=${encodeURIComponent(name)}`,
			`price=${price}`,
			`duration=${encodeURIComponent(duration)}`,
			`cover=${encodeURIComponent(cover || '')}`,
			`desc=${encodeURIComponent(desc || intro || '')}`
		].join('&');
		uni.navigateTo({
			url: `/mobile-design/booking/route-confirm?${query}`
		});
	}
</script>

<style lang="scss" scoped>
	.route-detail-page {
		min-height: 100vh;
		background: #fff9f0;
		padding-bottom: calc(128rpx + env(safe-area-inset-bottom));
	}

	.route-detail-banner-wrap {
		position: relative;
		height: 400rpx;
		overflow: hidden;
		background: #f0f0f0;
	}

	.route-detail-banner-swiper,
	.route-detail-banner-item,
	.route-detail-banner {
		width: 100%;
		height: 400rpx;
		display: block;
	}

	.route-detail-banner__mask {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		height: 120rpx;
		background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.25));
		pointer-events: none;
	}

	.route-detail-body {
		padding: 0 var(--ry-page-padding-x);
		margin-top: -40rpx;
		position: relative;
		z-index: 2;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.route-detail-card {
		padding: 28rpx 24rpx 24rpx;
	}

	.route-detail-card__head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 20rpx;
	}

	.route-detail-card__title {
		flex: 1;
		min-width: 0;
		font-size: 34rpx;
		font-weight: 700;
		color: #1a1a1a;
		line-height: 1.4;
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		text-overflow: ellipsis;
	}

	.route-detail-card__price {
		display: flex;
		align-items: baseline;
		flex-shrink: 0;
		padding-top: 2rpx;
	}

	.route-detail-card__symbol {
		font-size: 22rpx;
		font-weight: 600;
		color: #ff6600;
	}

	.route-detail-card__amount {
		font-size: 38rpx;
		font-weight: 800;
		color: #ff6600;
		line-height: 1;
	}

	.route-detail-card__suffix {
		margin-left: 4rpx;
		font-size: 22rpx;
		color: #999999;
	}

	.route-detail-card__tags {
		display: flex;
		flex-wrap: nowrap;
		overflow: hidden;
		gap: 10rpx;
		margin-top: 14rpx;
	}

	.route-detail-card__tag {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		height: 40rpx;
		padding: 0 18rpx;
		border-radius: 999rpx;
		background: #fff3eb;
		box-sizing: border-box;
		flex-shrink: 0;
	}

	.route-detail-card__tag-text {
		font-size: 22rpx;
		font-weight: 400;
		color: #ff6600;
		line-height: 40rpx;
		height: 40rpx;
	}

	.route-detail-card__desc {
		margin-top: 16rpx;
		font-size: 26rpx;
		color: #666666;
		line-height: 1.6;
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 3;
		line-clamp: 3;
		text-overflow: ellipsis;
	}

	.route-detail-meta {
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0f0f0;
		display: flex;
		flex-direction: column;
		gap: 14rpx;
	}

	.route-detail-meta__row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 24rpx;

		&--top {
			align-items: flex-start;
		}
	}

	.route-detail-meta__label {
		flex-shrink: 0;
		width: 140rpx;
		font-size: 24rpx;
		color: #999999;
		line-height: 1.4;
	}

	.route-detail-meta__value {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		font-weight: 500;
		color: #333333;
		line-height: 1.45;
		text-align: right;
	}

	.route-detail-spots {
		&__list {
			display: flex;
			flex-wrap: wrap;
			gap: 10rpx;
		}

		&__chip {
			display: inline-flex;
			align-items: center;
			justify-content: center;
			height: 44rpx;
			padding: 0 18rpx;
			border-radius: 8rpx;
			background: #f7f7f7;
			box-sizing: border-box;
		}

		&__chip-text {
			font-size: 24rpx;
			color: #666666;
			line-height: 44rpx;
			height: 44rpx;
		}

		&__empty {
			font-size: 24rpx;
			color: #999999;
		}
	}

	.route-store-entry {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
		padding: 16rpx;
		border-radius: 16rpx;
		background: #fafafa;

		&__cover {
			width: 132rpx;
			height: 132rpx;
			border-radius: 14rpx;
			flex-shrink: 0;
			background: #f0f0f0;
		}

		&__main {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 10rpx;
			padding-top: 2rpx;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.35;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__tags {
			width: 100%;
			white-space: nowrap;
		}

		&__tags-row {
			display: inline-flex;
			align-items: center;
			gap: 8rpx;
		}

		&__tag {
			display: inline-flex;
			align-items: center;
			justify-content: center;
			height: 36rpx;
			padding: 0 14rpx;
			border-radius: 999rpx;
			background: #fff3eb;
			font-size: 20rpx;
			color: #ff6600;
			line-height: 1;
			flex-shrink: 0;
		}

		&__addr-row {
			display: flex;
			align-items: center;
			gap: 6rpx;
		}

		&__addr {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.45;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			line-clamp: 2;
		}
	}

	.route-detail-block {
		padding: 24rpx;
	}

	.route-detail-block__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		margin-bottom: 20rpx;
		padding-bottom: 14rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.route-detail-block__head-left {
		display: flex;
		align-items: center;
		gap: 12rpx;
		min-width: 0;
	}

	.route-detail-block__more {
		display: flex;
		align-items: center;
		gap: 4rpx;
		flex-shrink: 0;
		padding: 8rpx 0 8rpx 12rpx;
	}

	.route-detail-block__more-text {
		font-size: 24rpx;
		color: var(--ry-module-route, #ff6600);
		line-height: 1;
	}

	.route-detail-block__icon {
		width: 44rpx;
		height: 44rpx;
		border-radius: 12rpx;
		background: rgba(255, 102, 0, 0.1);
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.route-detail-block__title {
		font-size: 30rpx;
		font-weight: 700;
		color: #1a1a1a;
	}

	.route-detail-day-panel {
		padding: 8rpx 4rpx 12rpx;

		&__content {
			display: block;
			font-size: 26rpx;
			color: #888888;
			line-height: 1.7;
			white-space: pre-wrap;
		}

		&__images {
			margin-top: 16rpx;
			width: 100%;
			white-space: nowrap;
		}

		&__images-row {
			display: inline-flex;
			gap: 12rpx;
			padding: 2rpx 0;
		}

		&__img {
			width: 180rpx;
			height: 136rpx;
			border-radius: 12rpx;
			background: #f0f0f0;
			flex-shrink: 0;
		}

		&__empty {
			display: block;
			font-size: 24rpx;
			color: #888888;
			line-height: 1.5;
		}
	}

	.route-detail-day-title {
		width: 100%;
		min-width: 0;
		overflow: hidden;
		padding-right: 8rpx;

		&__text {
			display: block;
			width: 100%;
			font-size: 28rpx;
			font-weight: 500;
			color: #1a1a1a;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}
	}

	:deep(.route-detail-itinerary) {
		.u-collapse-item {
			background: #ffffff !important;
		}

		.u-cell,
		.u-cell__body {
			background: #ffffff !important;
		}

		.u-cell--clickable {
			background-color: #ffffff !important;
		}

		.u-cell:active,
		.u-cell--clickable:active {
			background-color: #fff9f4 !important;
		}

		.u-cell__body {
			padding: 22rpx 8rpx !important;
			overflow: hidden;
		}

		.u-cell__body__content {
			flex: 1;
			min-width: 0;
			overflow: hidden;
		}

		.u-cell__title {
			flex: 1;
			min-width: 0;
			overflow: hidden;
			width: 0;
		}

		.u-cell__label {
			display: none !important;
		}

		.u-cell__right-icon-wrap {
			flex-shrink: 0;
		}

		.u-icon__icon,
		.u-icon__icon span {
			color: #999999 !important;
		}
	}

	.route-detail-merchant {
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	.route-detail-merchant__row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 20rpx;
		padding: 16rpx 0;
		border-bottom: 1rpx solid #f3f4f8;

		&--top {
			align-items: flex-start;
		}

		&:last-of-type {
			border-bottom: none;
		}
	}

	.route-detail-merchant__label {
		flex-shrink: 0;
		width: 140rpx;
		font-size: 26rpx;
		color: #888888;
	}

	.route-detail-merchant__value {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		font-weight: 500;
		color: #1a1a1a;
		line-height: 1.5;
		text-align: right;
	}

	.route-detail-merchant__phone {
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 8rpx 16rpx;
		border-radius: 999rpx;
		background: rgba(255, 102, 0, 0.08);
	}

	.route-detail-merchant__phone-text {
		font-size: 26rpx;
		font-weight: 600;
		color: var(--ry-module-route, #ff6600);
	}

	.route-detail-merchant__intro {
		margin-top: 12rpx;
		padding: 18rpx 20rpx;
		border-radius: 14rpx;
		background: #fff9f4;
	}

	.route-detail-merchant__intro-label {
		display: block;
		font-size: 24rpx;
		font-weight: 600;
		color: #b87333;
		margin-bottom: 8rpx;
	}

	.route-detail-merchant__intro-text {
		display: block;
		font-size: 26rpx;
		color: #666666;
		line-height: 1.65;
		white-space: pre-wrap;
	}

	.route-detail-merchant__guides {
		margin-top: 20rpx;
		padding-top: 8rpx;
	}

	.route-detail-merchant__guides-title {
		display: block;
		font-size: 24rpx;
		font-weight: 600;
		color: #666666;
		margin-bottom: 14rpx;
	}

	.route-detail-timeline-empty {
		padding: 24rpx 0;
		text-align: center;
	}

	.route-detail-timeline-empty__text {
		font-size: 24rpx;
		color: #888888;
	}

	.route-detail-guides {
		width: 100%;
		white-space: nowrap;
	}

	.route-detail-guides__row {
		display: inline-flex;
		gap: 24rpx;
		padding: 4rpx 0;
	}

	.route-detail-guide {
		display: inline-flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;
		width: 128rpx;
	}

	.route-detail-guide__avatar {
		width: 112rpx;
		height: 112rpx;
		border-radius: 50%;
		background: #f0f0f0;
	}

	.route-detail-guide__name {
		font-size: 22rpx;
		color: #666666;
		text-align: center;
		width: 100%;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.route-detail-store-banners {
		width: 100%;
		white-space: nowrap;
	}

	.route-detail-store-banners__row {
		display: inline-flex;
		gap: 16rpx;
		padding: 4rpx 0;
	}

	.route-detail-store-banners__img {
		width: 240rpx;
		height: 160rpx;
		border-radius: 12rpx;
		background: #f0f0f0;
		flex-shrink: 0;
	}

	.route-detail-contact-phone {
		display: flex;
		align-items: center;
		gap: 10rpx;
		margin-bottom: 20rpx;
		padding: 16rpx 18rpx;
		border-radius: 12rpx;
		background: #fffafc;
	}

	.route-detail-contact-phone__text {
		font-size: 26rpx;
		color: #1a1a1a;
		font-weight: 500;
	}

	.route-detail-qr {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		padding: 12rpx 0 4rpx;
	}

	.route-detail-qr__img {
		width: 280rpx;
		height: 280rpx;
		border-radius: 16rpx;
		background: #f7f8fc;
	}

	.route-detail-qr__tip {
		font-size: 24rpx;
		color: #999999;
		line-height: 1.4;
	}

	.route-wechat-popup {
		position: relative;
		width: 600rpx;
		padding-bottom: 72rpx;

		&__hero {
			padding: 48rpx 36rpx 36rpx;
			border-radius: 28rpx 28rpx 0 0;
			background: linear-gradient(145deg, #1aad19 0%, #07c160 55%, #06ad56 100%);
			display: flex;
			flex-direction: column;
			align-items: center;
		}

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

	.route-detail-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
		background: #ffffff;
		box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.06);
	}

	.route-detail-bar__inner {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.route-detail-bar__collect {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 4rpx;
		width: 88rpx;
		flex-shrink: 0;

		&:active {
			opacity: 0.7;
		}
	}

	.route-detail-bar__collect-text {
		font-size: 20rpx;
		color: #888888;
		line-height: 1;
	}

	.route-detail-bar__actions {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.route-detail-bar__wx {
		flex-shrink: 0;
		height: 88rpx;
		padding: 0 28rpx;
		border-radius: 999rpx;
		background: #e8f8ef;
		border: 2rpx solid rgba(7, 193, 96, 0.25);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		box-sizing: border-box;
	}

	.route-detail-bar__wx-text {
		font-size: 26rpx;
		font-weight: 600;
		color: #07c160;
		line-height: 1;
	}

	.route-detail-bar__action {
		flex: 1;
		min-width: 0;
		height: 88rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #ff8a3d, var(--ry-module-route, #ff6600));
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 8rpx 24rpx rgba(255, 102, 0, 0.28);
	}

	.route-detail-bar__btn-text {
		font-size: 30rpx;
		font-weight: 600;
		color: #ffffff;
	}
</style>
