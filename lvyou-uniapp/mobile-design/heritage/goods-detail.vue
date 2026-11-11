<template>
	<view class="hgd-page">
		<!-- 商品轮播 -->
		<view class="hgd-cover">
			<swiper
				class="hgd-cover__swiper"
				:indicator-dots="false"
				circular
				@change="onSwiperChange"
			>
				<swiper-item v-for="(banner, index) in goods.banners" :key="index" class="hgd-cover__item">
					<image
						class="hgd-cover__img ry-placeholder-img"
						:src="banner.image || PLACEHOLDER_LOGO"
						mode="aspectFill"
						@click="previewBannerImages(index)"
					/>
				</swiper-item>
			</swiper>
			<view class="hgd-cover__mask" />
			<view class="hgd-cover__favorite" @click="onFavorite">
				<u-icon
					:name="favorited ? 'heart-fill' : 'heart'"
					:color="favorited ? '#FF4757' : '#FFFFFF'"
					:size="22"
				/>
			</view>
			<view v-if="bannerCount > 1" class="hgd-cover__count">
				<text class="hgd-cover__count-text">{{ swiperIndex + 1 }}/{{ bannerCount }}</text>
			</view>
		</view>

		<view class="hgd-body">
			<!-- 价格与标题 -->
			<view class="hgd-info">
				<view class="hgd-info__price">
					<text class="hgd-info__price-symbol">{{ ui.currencySymbol || '¥' }}</text>
					<text class="hgd-info__price-value">{{ displayPrice }}</text>
					<text v-if="goods.priceFrom" class="hgd-info__price-from">起</text>
				</view>
				<text class="hgd-info__name">{{ goods.name }}</text>
				<text v-if="goods.desc" class="hgd-info__desc">{{ goods.desc }}</text>
				<view class="hgd-info__meta">
					<text class="hgd-info__meta-text">{{ ui.labelSales }} {{ goods.sales }}</text>
					<view class="hgd-info__meta-divider" />
					<text class="hgd-info__meta-text">{{ ui.labelStock }} {{ goods.stock }}</text>
					<template v-if="hasRating">
						<view class="hgd-info__meta-divider" />
						<view class="hgd-info__rating">
							<u-icon name="star-fill" color="#FF9800" :size="12" />
							<text class="hgd-info__rating-text">{{ goods.rating }}</text>
						</view>
					</template>
				</view>
			</view>

			<!-- 所属门店 -->
			<view v-if="showShopBlock" class="hgd-shop" @click="goShop">
				<view class="hgd-shop__cover-wrap" @click.stop>
					<swiper
						v-if="shopBannerList.length > 1"
						class="hgd-shop__swiper"
						:indicator-dots="false"
						circular
						@change="onShopSwiperChange"
					>
						<swiper-item v-for="(img, index) in shopBannerList" :key="index" class="hgd-shop__swiper-item">
							<image
								class="hgd-shop__cover ry-placeholder-img"
								:src="img || PLACEHOLDER_LOGO"
								mode="aspectFill"
								@click="previewShopBannerImages(index)"
							/>
						</swiper-item>
					</swiper>
					<image
						v-else
						class="hgd-shop__cover ry-placeholder-img"
						:src="shopBannerList[0] || PLACEHOLDER_LOGO"
						mode="aspectFill"
					/>
					<view v-if="shopBannerList.length > 1" class="hgd-shop__cover-count">
						<text class="hgd-shop__cover-count-text">{{ shopSwiperIndex + 1 }}/{{ shopBannerList.length }}</text>
					</view>
				</view>
				<view class="hgd-shop__content">
					<text class="hgd-shop__label">{{ ui.goodsDetailShop }}</text>
					<text class="hgd-shop__name">{{ goods.shopName }}</text>
					<text v-if="goods.shopAddress" class="hgd-shop__addr">{{ goods.shopAddress }}</text>
				</view>
				<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
			</view>

			<!-- 商品详情 -->
			<view v-if="goods.detail || detailImageList.length" class="hgd-section">
				<view class="hgd-section__head">
					<view class="hgd-section__deco" />
					<text class="hgd-section__title">{{ ui.goodsDetailInfo }}</text>
					<text class="hgd-section__quote">"</text>
				</view>
				<text v-if="goods.detail" class="hgd-section__content">{{ goods.detail }}</text>
				<view v-if="detailImageList.length" class="hgd-detail-images">
					<image
						v-for="(img, index) in detailImageList"
						:key="index"
						class="hgd-detail-images__img"
						:src="img"
						mode="widthFix"
						@click="previewDetailImage(index)"
					/>
				</view>
			</view>

			<!-- 用户评价 -->
			<view class="hgd-section">
				<view class="hgd-section__head hgd-section__head--row">
					<view class="hgd-section__head-left">
						<view class="hgd-section__deco" />
						<text class="hgd-section__title">{{ ui.goodsDetailReview }}</text>
					</view>
					<view v-if="reviewList.length" class="hgd-section__more" @click="goReviews">
						<text class="hgd-section__more-text">{{ ui.goodsDetailAllReview }}</text>
						<u-icon name="arrow-right" color="#8D6E63" :size="12" />
					</view>
				</view>
				<view v-if="!reviewList.length" class="hgd-review-empty">
					<text class="hgd-review-empty__text">暂无评价</text>
				</view>
				<view v-for="review in previewReviews" :key="review.id" class="hgd-review">
					<view class="hgd-review__head">
						<view class="hgd-review__avatar" />
						<view class="hgd-review__user">
							<text class="hgd-review__name">{{ review.user }}</text>
							<text class="hgd-review__time">{{ review.time }}</text>
						</view>
					</view>
					<text class="hgd-review__content">{{ review.content }}</text>
				</view>
			</view>

			<text class="hgd-footer">{{ ui.footerGoodsDetail }}</text>
		</view>

		<!-- 底部操作栏 -->
		<view class="hgd-bar">
			<view class="hgd-bar__icon" @click="onFavorite">
				<u-icon :name="favorited ? 'heart-fill' : 'heart'" :color="favorited ? '#FF4757' : '#8D6E63'" :size="22" />
				<text class="hgd-bar__icon-text">{{ favorited ? '已收藏' : '收藏' }}</text>
			</view>
			<view class="hgd-bar__icon" @click="onCallShop">
				<u-icon name="phone-fill" color="#8D6E63" :size="22" />
				<text class="hgd-bar__icon-text">电话</text>
			</view>
			<view class="hgd-bar__btn" @click="onBuy">
				<text class="hgd-bar__btn-text">{{ ui.btnBuyNow }}</text>
			</view>
		</view>

		<u-popup
			:show="buyPopupVisible"
			mode="bottom"
			:round="24"
			bg-color="transparent"
			:close-on-click-overlay="true"
			@close="closeBuyPopup"
		>
			<view class="hgd-buy-popup">
				<view class="hgd-buy-popup__head">
					<image class="hgd-buy-popup__thumb" :src="goods.cover || PLACEHOLDER_LOGO" mode="aspectFill" />
					<view class="hgd-buy-popup__meta">
						<view class="hgd-buy-popup__price">
							<text class="hgd-buy-popup__price-symbol">{{ ui.currencySymbol || '¥' }}</text>
							<text class="hgd-buy-popup__price-value">{{ displayPrice }}</text>
							<text v-if="goods.priceFrom" class="hgd-buy-popup__price-from">起</text>
						</view>
						<text v-if="goods.specs?.length" class="hgd-buy-popup__picked">
							{{ selectedSpec ? `已选：${selectedSpec}` : `请选择${ui.goodsDetailSpec}` }}
						</text>
						<text class="hgd-buy-popup__stock">{{ ui.labelStock }} {{ currentStock }}</text>
					</view>
					<view class="hgd-buy-popup__close" @click="closeBuyPopup">
						<u-icon name="close" color="#999999" :size="18" />
					</view>
				</view>

				<view v-if="goods.specs?.length" class="hgd-buy-popup__section">
					<text class="hgd-buy-popup__label">{{ ui.goodsDetailSpec }}</text>
					<view class="hgd-spec__list">
						<view
							v-for="spec in goods.specs"
							:key="spec.name"
							class="hgd-spec__item"
							:class="{ 'hgd-spec__item--active': selectedSpec === spec.name, 'hgd-spec__item--disabled': spec.stock <= 0 }"
							@click="selectSpec(spec)"
						>
							<text class="hgd-spec__text">{{ spec.name }}</text>
							<text v-if="goods.specMode === 'multi'" class="hgd-spec__price">¥{{ spec.price }}</text>
						</view>
					</view>
				</view>

				<view class="hgd-buy-popup__section hgd-buy-popup__section--qty">
					<text class="hgd-buy-popup__label">{{ ui.goodsDetailQty }}</text>
					<view class="hgd-stepper">
						<view class="hgd-stepper__btn" :class="{ 'hgd-stepper__btn--disabled': quantity <= 1 }" @click="changeQty(-1)">
							<u-icon name="minus" :color="quantity <= 1 ? '#CCCCCC' : '#8D6E63'" :size="14" />
						</view>
						<text class="hgd-stepper__value">{{ quantity }}</text>
						<view class="hgd-stepper__btn" :class="{ 'hgd-stepper__btn--disabled': quantity >= currentStock }" @click="changeQty(1)">
							<u-icon name="plus" :color="quantity >= currentStock ? '#CCCCCC' : '#8D6E63'" :size="14" />
						</view>
					</view>
				</view>

				<view class="hgd-buy-popup__btn" @click="confirmBuy">
					<text class="hgd-buy-popup__btn-text">{{ ui.btnBuyNow }}</text>
				</view>
			</view>
		</u-popup>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { fetchGoodsDetail, fetchGoodsReviews, mapGoodsDetail, mapReviewItem, findSkuByName } from '@/common/api/heritage.js';
import { HERITAGE_UI } from '@/common/heritage-ui.js';
import { checkFavorited, toggleFavoriteItem, logFootprint } from '@/common/asset-detail.js';

const ui = HERITAGE_UI;
const emptyGoods = {
	id: '',
	name: '',
	desc: '',
	cover: PLACEHOLDER_LOGO,
	price: '0',
	sales: 0,
	stock: 0,
	rating: '',
	specMode: 'single',
	specs: [],
	priceFrom: false,
	detail: '',
	banners: [{ image: PLACEHOLDER_LOGO }],
	detailImages: [],
	shopId: '',
	shopName: '',
	shopCover: PLACEHOLDER_LOGO,
	shopBannerImages: [],
	shopAddress: '',
	shopPhone: ''
};
const goods = ref({ ...emptyGoods });
const reviewList = ref([]);
const loading = ref(false);
const favorited = ref(false);
const swiperIndex = ref(0);
const shopSwiperIndex = ref(0);
const selectedSpec = ref('');
const quantity = ref(1);
const buyPopupVisible = ref(false);

const bannerCount = computed(() => goods.value.banners?.length || 1);
const detailImageList = computed(() => (goods.value.detailImages || []).filter(Boolean));
const previewReviews = computed(() => reviewList.value.slice(0, 2));
const hasRating = computed(() => {
	const rating = goods.value.rating;
	return rating != null && rating !== '' && Number(rating) > 0;
});
const showShopBlock = computed(() => Boolean(goods.value.shopId || goods.value.shopName));
const shopBannerList = computed(() => {
	const images = (goods.value.shopBannerImages || []).filter(Boolean);
	if (images.length) return images;
	if (goods.value.shopCover) return [goods.value.shopCover];
	return [];
});
const selectedSku = computed(() => findSkuByName(goods.value.specs, selectedSpec.value));
const currentStock = computed(() => {
	if (goods.value.specMode === 'multi' && selectedSku.value) {
		return Number(selectedSku.value.stock) || 0;
	}
	return goods.value.stock || 0;
});
const displayPrice = computed(() => {
	if (goods.value.specMode === 'multi' && selectedSku.value) {
		return String(selectedSku.value.price ?? goods.value.price);
	}
	return goods.value.price;
});

function selectSpec(spec) {
	if (!spec || Number(spec.stock) <= 0) {
		uni.showToast({ title: '该规格暂无库存', icon: 'none' });
		return;
	}
	selectedSpec.value = spec.name;
	if (quantity.value > currentStock.value) {
		quantity.value = Math.max(1, currentStock.value);
	}
}

async function loadGoodsDetail(id) {
	if (!id) return;
	loading.value = true;
	try {
		const [detailRes, reviewRes] = await Promise.all([
			fetchGoodsDetail(id),
			fetchGoodsReviews(id, { page: 1, limit: 2 })
		]);
		if (detailRes.code === 0 && detailRes.data) {
			goods.value = mapGoodsDetail(detailRes.data);
			shopSwiperIndex.value = 0;
			const firstAvailable = goods.value.specs?.find((item) => Number(item.stock) > 0) || goods.value.specs?.[0];
			selectedSpec.value = firstAvailable?.name || '';
			uni.setNavigationBarTitle({ title: goods.value.name });
			syncFavoriteState();
		} else {
			uni.showToast({ title: detailRes.msg || '加载失败', icon: 'none' });
		}
		if (reviewRes.code === 0 && reviewRes.data) {
			reviewList.value = (reviewRes.data.records || []).map(mapReviewItem);
		} else {
			reviewList.value = [];
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function onSwiperChange(e) {
	swiperIndex.value = e.detail?.current || 0;
}

function onShopSwiperChange(e) {
	shopSwiperIndex.value = e.detail?.current || 0;
}

function previewShopBannerImages(index) {
	const urls = shopBannerList.value;
	if (!urls.length) return;
	uni.previewImage({
		current: urls[index] || urls[0],
		urls
	});
}

function previewBannerImages(index) {
	const urls = (goods.value.banners || []).map((item) => item.image).filter(Boolean);
	if (!urls.length) return;
	uni.previewImage({
		current: urls[index] || urls[0],
		urls
	});
}

function previewDetailImage(index) {
	const urls = detailImageList.value;
	if (!urls.length) return;
	uni.previewImage({
		current: urls[index] || urls[0],
		urls
	});
}

function changeQty(delta) {
	const next = quantity.value + delta;
	if (next < 1 || next > currentStock.value) return;
	quantity.value = next;
}

function goShop() {
	if (!goods.value.shopId) return;
	uni.navigateTo({ url: `/mobile-design/heritage/shop-detail?id=${goods.value.shopId}` });
}

function goReviews() {
	uni.navigateTo({
		url: `/mobile-design/heritage/review-list?goodsId=${goods.value.id}&title=${encodeURIComponent(goods.value.name)}`
	});
}

function favoriteItem() {
	return {
		moduleType: 'goods',
		id: goods.value.id,
		name: goods.value.name,
		address: goods.value.shopName || goods.value.desc || '',
		path: `/mobile-design/heritage/goods-detail?id=${goods.value.id}`,
		price: goods.value.price || 0,
		cover: goods.value.cover || '',
		desc: goods.value.desc || ''
	};
}

async function syncFavoriteState() {
	favorited.value = await checkFavorited('goods', goods.value.id);
	logFootprint();
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

function onCallShop() {
	const phoneNumber = String(goods.value.shopPhone || '').trim();
	if (!phoneNumber) {
		uni.showToast({ title: '暂无联系电话', icon: 'none' });
		return;
	}
	uni.makePhoneCall({
		phoneNumber,
		fail: () => {
			uni.showToast({ title: `请拨打：${phoneNumber}`, icon: 'none' });
		}
	});
}

function openBuyPopup() {
	const firstAvailable = goods.value.specs?.find((item) => Number(item.stock) > 0) || goods.value.specs?.[0];
	if (!selectedSpec.value && firstAvailable?.name) {
		selectedSpec.value = firstAvailable.name;
	}
	if (quantity.value > currentStock.value) {
		quantity.value = Math.max(1, currentStock.value);
	}
	buyPopupVisible.value = true;
}

function closeBuyPopup() {
	buyPopupVisible.value = false;
}

function onBuy() {
	openBuyPopup();
}

function confirmBuy() {
	if (goods.value.specs?.length && !selectedSpec.value) {
		uni.showToast({ title: `请选择${ui.goodsDetailSpec}`, icon: 'none' });
		return;
	}
	if (currentStock.value <= 0) {
		uni.showToast({ title: '暂无库存', icon: 'none' });
		return;
	}
	const spec = selectedSpec.value || goods.value.specs?.[0]?.name || '';
	const query = [
		`id=${goods.value.id}`,
		`qty=${quantity.value}`,
		spec ? `spec=${encodeURIComponent(spec)}` : '',
		displayPrice.value ? `unitPrice=${encodeURIComponent(displayPrice.value)}` : ''
	].filter(Boolean).join('&');
	closeBuyPopup();
	uni.navigateTo({ url: `/mobile-design/booking/goods-confirm?${query}` });
}

onLoad((options) => {
	loadGoodsDetail(options?.id);
});
</script>

<style lang="scss" scoped>
.hgd-page {
	min-height: 100vh;
	background-color: #faf6f3;
}

.hgd-cover {
	position: relative;
	width: 100%;
	height: 520rpx;
	background-color: #f5f0eb;

	&__swiper {
		width: 100%;
		height: 520rpx;
	}

	&__item {
		width: 100%;
		height: 520rpx;
	}

	&__img {
		width: 100%;
		height: 520rpx;
		display: block;
	}

	&__mask {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		height: 120rpx;
		background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.25));
		pointer-events: none;
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
		z-index: 2;

		&:active {
			opacity: 0.88;
		}
	}

	&__count {
		position: absolute;
		right: 24rpx;
		bottom: 72rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		min-height: 36rpx;
		padding: 0 16rpx;
		border-radius: 999rpx;
		background-color: rgba(0, 0, 0, 0.35);
		z-index: 2;
	}

	&__count-text {
		font-size: 20rpx;
		line-height: 1;
		color: rgba(255, 255, 255, 0.9);
	}
}

.hgd-body {
	position: relative;
	margin-top: -48rpx;
	padding: 0 var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	z-index: 3;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.hgd-info {
	padding: 28rpx 28rpx 24rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 12rpx 40rpx rgba(74, 55, 40, 0.12);

	&__price {
		display: flex;
		align-items: baseline;
		margin-bottom: 12rpx;
	}

	&__price-symbol {
		font-size: 28rpx;
		font-weight: 600;
		color: #b45d2d;
	}

	&__price-value {
		margin-left: 4rpx;
		font-size: 48rpx;
		font-weight: 800;
		color: #b45d2d;
	}

	&__price-from {
		margin-left: 6rpx;
		font-size: 24rpx;
		font-weight: 600;
		color: #b45d2d;
	}

	&__name {
		display: block;
		font-size: 34rpx;
		font-weight: 800;
		color: #4a3728;
		line-height: 1.5;
	}

	&__desc {
		display: block;
		margin-top: 10rpx;
		font-size: 26rpx;
		color: #999999;
		line-height: 1.6;
	}

	&__meta {
		display: flex;
		align-items: center;
		gap: 16rpx;
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0ebe6;
	}

	&__meta-text {
		font-size: 24rpx;
		color: #bbbbbb;
	}

	&__meta-divider {
		width: 1rpx;
		height: 20rpx;
		background-color: #e8ddd4;
	}

	&__rating {
		display: flex;
		align-items: center;
		gap: 4rpx;
	}

	&__rating-text {
		font-size: 24rpx;
		font-weight: 600;
		color: #ff9800;
	}
}

.hgd-shop {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 24rpx 28rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

	&:active {
		opacity: 0.92;
	}

	&__cover-wrap {
		position: relative;
		width: 96rpx;
		height: 96rpx;
		border-radius: 12rpx;
		flex-shrink: 0;
		overflow: hidden;
		background-color: #f5f0eb;
	}

	&__swiper,
	&__swiper-item {
		width: 96rpx;
		height: 96rpx;
	}

	&__cover {
		width: 96rpx;
		height: 96rpx;
		display: block;
	}

	&__cover-count {
		position: absolute;
		right: 4rpx;
		bottom: 4rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		min-width: 36rpx;
		padding: 0 6rpx;
		height: 24rpx;
		border-radius: 999rpx;
		background-color: rgba(0, 0, 0, 0.45);
	}

	&__cover-count-text {
		font-size: 16rpx;
		line-height: 1;
		color: rgba(255, 255, 255, 0.92);
	}

	&__content {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	&__label {
		font-size: 22rpx;
		color: #999999;
	}

	&__name {
		font-size: 28rpx;
		font-weight: 600;
		color: #4a3728;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__addr {
		font-size: 22rpx;
		color: #bbbbbb;
		line-height: 1.4;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}
}

.hgd-spec {
	&__label {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: #4a3728;
		margin-bottom: 16rpx;
	}

	&__list {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
	}

	&__item {
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 4rpx;
		padding: 12rpx 24rpx;
		border-radius: 12rpx;
		border: 2rpx solid #f0ebe6;
		background-color: #faf6f3;

		&--active {
			border-color: #b45d2d;
			background-color: #fdf8f5;
		}

		&--disabled {
			opacity: 0.45;
		}
	}

	&__price {
		font-size: 22rpx;
		color: #b45d2d;
	}

	&__text {
		font-size: 26rpx;
		color: #666666;

		.hgd-spec__item--active & {
			color: #b45d2d;
			font-weight: 600;
		}
	}
}

.hgd-stepper {
	display: flex;
	align-items: center;
	gap: 20rpx;

	&__btn {
		width: 56rpx;
		height: 56rpx;
		border-radius: 12rpx;
		border: 1rpx solid #e8ddd4;
		background-color: #faf6f3;
		display: flex;
		align-items: center;
		justify-content: center;

		&--disabled {
			opacity: 0.5;
		}
	}

	&__value {
		min-width: 48rpx;
		text-align: center;
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3728;
	}
}

.hgd-section {
	padding: 24rpx 28rpx 28rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

	&__head {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 20rpx;

		&--row {
			justify-content: space-between;
		}
	}

	&__head-left {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	&__deco {
		width: 6rpx;
		height: 28rpx;
		border-radius: 3rpx;
		background: linear-gradient(180deg, #b45d2d, #5d4037);
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3728;
	}

	&__quote {
		font-size: 30rpx;
		font-weight: 700;
		color: #b45d2d;
	}

	&__more {
		display: flex;
		align-items: center;
		gap: 4rpx;
	}

	&__more-text {
		font-size: 24rpx;
		color: #8d6e63;
	}

	&__content {
		display: block;
		font-size: 26rpx;
		color: #666666;
		line-height: 1.9;
		text-align: justify;
	}
}

.hgd-detail-images {
	display: flex;
	flex-direction: column;
	gap: 0;
	margin-top: 20rpx;

	&__img {
		display: block;
		width: 100%;
	}
}

.hgd-review-empty {
	display: flex;
	justify-content: center;
	padding: 40rpx 0;

	&__text {
		font-size: 26rpx;
		color: #cccccc;
	}
}

.hgd-review {
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f0ebe6;

	&:last-child {
		border-bottom: none;
		padding-bottom: 0;
	}

	&__head {
		display: flex;
		align-items: center;
		gap: 16rpx;
		margin-bottom: 12rpx;
	}

	&__avatar {
		width: 64rpx;
		height: 64rpx;
		border-radius: 50%;
		background-color: #f5f0eb;
		flex-shrink: 0;
	}

	&__name {
		display: block;
		font-size: 26rpx;
		font-weight: 600;
		color: #4a3728;
	}

	&__time {
		display: block;
		margin-top: 4rpx;
		font-size: 22rpx;
		color: #cccccc;
	}

	&__content {
		display: block;
		font-size: 26rpx;
		color: #666666;
		line-height: 1.7;
	}
}

.hgd-footer {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #cccccc;
	padding: 8rpx 0;
}

.hgd-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 99;
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 20rpx var(--ry-page-padding-x);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	background-color: #ffffff;
	border-top: 1rpx solid #f0ebe6;
	box-shadow: 0 -4rpx 16rpx rgba(74, 55, 40, 0.06);

	&__icon {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 4rpx;
		width: 96rpx;
		flex-shrink: 0;

		&:active {
			opacity: 0.7;
		}
	}

	&__icon-text {
		font-size: 20rpx;
		color: #8d6e63;
	}

	&__btn {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 80rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #8d6e63, #5d4037);
		box-shadow: 0 8rpx 24rpx rgba(93, 64, 55, 0.3);

		&:active {
			opacity: 0.9;
		}
	}

	&__btn-text {
		font-size: 28rpx;
		font-weight: 600;
		color: #ffffff;
	}
}

.hgd-buy-popup {
	padding: 28rpx var(--ry-page-padding-x) calc(24rpx + env(safe-area-inset-bottom));
	border-radius: 24rpx 24rpx 0 0;
	background-color: #ffffff;

	&__head {
		display: flex;
		align-items: flex-start;
		gap: 20rpx;
		margin-bottom: 28rpx;
	}

	&__thumb {
		width: 160rpx;
		height: 160rpx;
		border-radius: 16rpx;
		flex-shrink: 0;
		background-color: #f5f0eb;
	}

	&__meta {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		padding-top: 8rpx;
	}

	&__price {
		display: flex;
		align-items: baseline;
	}

	&__price-symbol {
		font-size: 24rpx;
		font-weight: 600;
		color: #b45d2d;
	}

	&__price-value {
		margin-left: 4rpx;
		font-size: 40rpx;
		font-weight: 800;
		color: #b45d2d;
	}

	&__price-from {
		margin-left: 6rpx;
		font-size: 22rpx;
		font-weight: 600;
		color: #b45d2d;
	}

	&__picked,
	&__stock {
		font-size: 24rpx;
		color: #999999;
		line-height: 1.4;
	}

	&__close {
		width: 48rpx;
		height: 48rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	&__section {
		margin-bottom: 24rpx;

		&--qty {
			display: flex;
			align-items: center;
			justify-content: space-between;
		}
	}

	&__label {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: #4a3728;
		margin-bottom: 16rpx;
	}

	&__section--qty &__label {
		margin-bottom: 0;
	}

	&__btn {
		margin-top: 8rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 88rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #8d6e63, #5d4037);
		box-shadow: 0 8rpx 24rpx rgba(93, 64, 55, 0.28);

		&:active {
			opacity: 0.9;
		}
	}

	&__btn-text {
		font-size: 30rpx;
		font-weight: 600;
		color: #ffffff;
	}
}
</style>
