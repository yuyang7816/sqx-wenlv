<template>
	<view class="gc-page">
		<view class="gc-ambient" />

		<scroll-view scroll-y class="gc-scroll" :show-scrollbar="false">
			<view class="gc-inner">
				<!-- 收货地址 -->
				<view class="gc-card gc-card--tap gc-card--address" @click="goAddress">
					<view class="gc-card__head">
						<view class="gc-card__icon">
							<u-icon name="map-fill" :color="accentColor" :size="16" />
						</view>
						<view class="gc-card__titles">
							<text class="gc-card__title">{{ ui.sectionAddress }}</text>
							<text class="gc-card__sub">{{ address ? '点击可更换收货地址' : '请选择收货地址' }}</text>
						</view>
						<u-icon name="arrow-right" :size="14" color="#C7C7CC" />
					</view>

					<view v-if="address" class="gc-address">
						<view class="gc-address__user">
							<text class="gc-address__name">{{ address.name }}</text>
							<text class="gc-address__phone">{{ address.phone }}</text>
							<view v-if="address.isDefault" class="gc-address__tag">
								<text class="gc-address__tag-text">{{ ui.addrDefault }}</text>
							</view>
						</view>
						<text class="gc-address__text">{{ addressText }}</text>
					</view>
					<view v-else class="gc-address gc-address--empty">
						<text class="gc-address__placeholder">{{ ui.addrEmpty }}</text>
					</view>
				</view>

				<!-- 商品信息 -->
				<view class="gc-card">
					<view class="gc-card__head">
						<view class="gc-card__icon">
							<u-icon name="bag-fill" :color="accentColor" :size="16" />
						</view>
						<view class="gc-card__titles">
							<text class="gc-card__title">{{ ui.sectionGoods }}</text>
							<text class="gc-card__sub">{{ goods.shopName || ui.shopName }}</text>
						</view>
					</view>

					<view class="gc-goods">
						<image class="gc-goods__cover ry-placeholder-img" :src="goods.cover" mode="aspectFill" />
						<view class="gc-goods__info">
							<text class="gc-goods__name">{{ goods.name }}</text>
							<view v-if="selectedSpec" class="gc-goods__spec">
								<text class="gc-goods__spec-text">{{ selectedSpec }}</text>
							</view>
							<view class="gc-goods__foot">
								<view class="gc-goods__price">
									<text class="gc-goods__symbol">{{ ui.currencySymbol }}</text>
									<text class="gc-goods__value">{{ unitPrice }}</text>
								</view>
								<text class="gc-goods__qty">x{{ quantity }}</text>
							</view>
						</view>
					</view>
				</view>

				<!-- 订单备注 -->
				<view class="gc-card">
					<view class="gc-card__head">
						<view class="gc-card__icon">
							<u-icon name="edit-pen-fill" :color="accentColor" :size="16" />
						</view>
						<text class="gc-card__title">{{ ui.sectionRemark }}</text>
					</view>
					<textarea
						class="gc-remark"
						v-model="remark"
						:placeholder="ui.remarkPlaceholder"
						:maxlength="100"
						auto-height
					/>
				</view>

				<!-- 优惠券 -->
				<view v-if="goodsAmount > 0" class="gc-card gc-card--tap" @click="openCouponSelect">
					<view class="gc-coupon">
						<view class="gc-coupon__left">
							<view class="gc-coupon__icon">
								<u-icon name="coupon-fill" :color="accentColor" :size="16" />
							</view>
							<text class="gc-coupon__label">{{ ui.labelCoupon }}</text>
						</view>
						<view class="gc-coupon__right">
							<text v-if="selectedCoupon" class="gc-coupon__discount">- {{ formatHeritageAmount(couponDiscount) }}</text>
							<text v-else-if="availableCouponCount" class="gc-coupon__hint">{{ availableCouponCount }}{{ ui.couponSelectHint }}</text>
							<text v-else class="gc-coupon__none">{{ ui.couponNone }}</text>
							<u-icon name="arrow-right" :size="14" color="#C7C7CC" />
						</view>
					</view>
				</view>

				<!-- 费用明细 -->
				<view class="gc-card gc-bill">
					<view class="gc-card__head">
						<view class="gc-card__icon">
							<u-icon name="rmb-circle-fill" :color="accentColor" :size="16" />
						</view>
						<text class="gc-card__title">{{ ui.sectionFee }}</text>
					</view>
					<view class="gc-bill__rows">
						<view class="gc-bill__row">
							<text class="gc-bill__label">{{ ui.labelGoods }}</text>
							<text class="gc-bill__value">{{ formatHeritageAmount(goodsAmount) }}</text>
						</view>
						<view class="gc-bill__row">
							<text class="gc-bill__label">{{ ui.labelShip }}</text>
							<text class="gc-bill__value" :class="{ 'gc-bill__value--free': shippingFee <= 0 }">{{ shippingDisplayText }}</text>
						</view>
						<view v-if="couponDiscount > 0" class="gc-bill__row">
							<text class="gc-bill__label">{{ ui.labelDiscount }}</text>
							<text class="gc-bill__value gc-bill__value--off">- {{ formatHeritageAmount(couponDiscount) }}</text>
						</view>
					</view>
					<view class="gc-bill__divider" />
					<view class="gc-bill__row gc-bill__row--total">
						<text class="gc-bill__label">{{ ui.labelTotal }}</text>
						<view class="gc-bill__total">
							<text class="gc-bill__symbol">{{ ui.currencySymbol }}</text>
							<text class="gc-bill__amount">{{ totalAmount }}</text>
						</view>
					</view>
				</view>

				<text class="gc-footer">{{ ui.footer }}</text>
			</view>
		</scroll-view>

		<view class="gc-bar">
			<view class="gc-bar__glass" />
			<view class="gc-bar__inner">
				<view class="gc-bar__info">
					<text class="gc-bar__label">{{ ui.labelTotal }}</text>
					<view class="gc-bar__price">
						<text class="gc-bar__symbol">{{ ui.currencySymbol }}</text>
						<text class="gc-bar__amount">{{ totalAmount }}</text>
					</view>
				</view>
				<view class="gc-bar__btn" @click="onSubmit">
					<text class="gc-bar__btn-text">{{ ui.btnSubmit }}</text>
					<u-icon name="arrow-right" color="#FFFFFF" :size="12" />
				</view>
			</view>
		</view>

		<GlobalPayHost />
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import GlobalPayHost from '@/components/global-pay-host.vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { fetchGoodsDetail, mapGoodsDetail, findSkuByName } from '@/common/api/heritage.js';
import { fetchAddressList, mapAddressItem } from '@/common/api/address.js';
import { resolveSelectedAddress, formatAddress } from '@/common/address-store.js';
import { hasToken } from '@/common/api/auth.js';
import { useBookingGoodsPay } from '@/common/booking-goods-pay.js';
import { BOOKING_UI } from '@/common/booking-ui.js';
import { calcHeritageShippingFee } from '@/common/heritage-shipping.js';

const accentColor = getModuleColor('heritageShop');
const payUi = { ...BOOKING_UI, payTitle: '选择支付方式', payAmountLabel: '实付金额', payConfirm: '确认支付', payProcessing: '支付处理中...', payRecommend: '推荐' };

const emptyGoods = {
	id: '',
	name: '',
	cover: PLACEHOLDER_LOGO,
	price: '0',
	shopName: '',
	desc: '',
	specs: [],
	shopShipping: {
		freeShipping: true,
		fixedFee: 0,
		freeAmountThreshold: 0,
		shippingNote: ''
	}
};
const goods = ref({ ...emptyGoods });
const quantity = ref(1);
const selectedSpec = ref('');
const remark = ref('');
const address = ref(null);
const goodsIdRef = ref('');

const addressText = computed(() => (address.value ? formatAddress(address.value) : ''));
const unitPrice = computed(() => {
	if (selectedSpec.value) {
		const sku = findSkuByName(goods.value.specs, selectedSpec.value);
		if (sku?.price != null) {
			return Number(sku.price) || 0;
		}
	}
	return Number(goods.value.price || 0);
});
const goodsAmount = computed(() => unitPrice.value * quantity.value);
const shippingFee = computed(() => calcHeritageShippingFee(goods.value.shopShipping, goodsAmount.value));

const {
	ui,
	selectedCoupon,
	availableCouponCount,
	couponDiscount,
	totalAmount,
	requireLogin,
	openCouponSelect,
	onSubmit,
	onBookingShow,
	formatHeritageAmount
} = useBookingGoodsPay({
	goodsAmount,
	shippingFee,
	validateForm,
	buildCreateOrderPayload,
	accentColor,
	priceColor: '#B45D2D',
	payUi
});

const shippingDisplayText = computed(() => (
	shippingFee.value > 0 ? formatHeritageAmount(shippingFee.value) : ui.shipFree
));

async function loadGoodsDetail(id) {
	if (!id) return;
	try {
		const res = await fetchGoodsDetail(id);
		if (res.code === 0 && res.data) {
			goods.value = mapGoodsDetail(res.data);
		} else {
			uni.showToast({ title: res.msg || '加载商品失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常', icon: 'none' });
	}
}

async function loadAddress() {
	if (!hasToken()) {
		address.value = null;
		return;
	}
	try {
		const res = await fetchAddressList();
		if (res.code === 0 && Array.isArray(res.data)) {
			const list = res.data.map(mapAddressItem);
			address.value = resolveSelectedAddress(list);
		} else {
			address.value = null;
		}
	} catch (e) {
		address.value = null;
	}
}

function goAddress() {
	if (!requireLogin()) return;
	uni.navigateTo({ url: '/mobile-design/heritage/address-list' });
}

function validateForm() {
	if (!address.value) {
		uni.showToast({ title: ui.toastNeedAddress, icon: 'none' });
		return false;
	}
	if (goods.value.specs?.length > 1 && !selectedSpec.value) {
		uni.showToast({ title: '请选择规格', icon: 'none' });
		return false;
	}
	return true;
}

function buildCreateOrderPayload() {
	const payload = {
		goodsId: goods.value.id,
		addressId: address.value.id,
		quantity: quantity.value,
		itemSpec: selectedSpec.value || '',
		remark: remark.value.trim(),
		unitPrice: unitPrice.value
	};
	if (selectedCoupon.value?.id) {
		payload.couponId = selectedCoupon.value.id;
	}
	return payload;
}

onLoad((options) => {
	goodsIdRef.value = String(options?.id || '');
	quantity.value = Math.max(1, Number(options?.qty) || 1);
	if (options?.spec) selectedSpec.value = decodeURIComponent(options.spec);
	if (options?.unitPrice) goods.value.price = decodeURIComponent(options.unitPrice);
	uni.setNavigationBarTitle({ title: ui.navTitle });
	if (hasToken()) {
		loadGoodsDetail(goodsIdRef.value);
	}
});

onShow(() => {
	if (!hasToken()) return;
	if (goodsIdRef.value) {
		loadGoodsDetail(goodsIdRef.value);
	}
	loadAddress();
	onBookingShow();
});
</script>

<style lang="scss" scoped>
.gc-page {
	min-height: 100vh;
	background-color: #faf6f3;
}

.gc-ambient {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	height: 360rpx;
	background: linear-gradient(180deg, rgba(180, 93, 45, 0.1) 0%, rgba(250, 246, 243, 0) 100%);
	pointer-events: none;
	z-index: 0;
}

.gc-scroll {
	position: relative;
	z-index: 1;
	height: 100vh;
	box-sizing: border-box;
	padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
}

.gc-inner {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	padding: 20rpx var(--ry-page-padding-x) 24rpx;
}

.gc-card {
	padding: 28rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 8rpx 32rpx rgba(74, 55, 40, 0.06);

	&--tap:active {
		opacity: 0.92;
	}

	&--address {
		border: 2rpx solid rgba(180, 93, 45, 0.08);
	}

	&__head {
		display: flex;
		align-items: center;
		gap: 16rpx;
		margin-bottom: 20rpx;
	}

	&__icon {
		width: 56rpx;
		height: 56rpx;
		border-radius: 16rpx;
		background-color: #f5e8df;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	&__titles {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3728;
	}

	&__sub {
		font-size: 22rpx;
		color: #bbbbbb;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}
}

.gc-address {
	&__user {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-bottom: 12rpx;
	}

	&__name {
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3728;
	}

	&__phone {
		font-size: 28rpx;
		color: #666666;
	}

	&__tag {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 4rpx 12rpx;
		border-radius: 999rpx;
		background-color: #f5e8df;
	}

	&__tag-text {
		font-size: 20rpx;
		color: #b45d2d;
		font-weight: 600;
	}

	&__text {
		display: block;
		font-size: 26rpx;
		color: #666666;
		line-height: 1.6;
	}

	&--empty {
		padding: 12rpx 0 4rpx;
	}

	&__placeholder {
		font-size: 28rpx;
		color: #cccccc;
	}
}

.gc-goods {
	display: flex;
	gap: 20rpx;
	padding-top: 4rpx;

	&__cover {
		width: 168rpx;
		height: 168rpx;
		border-radius: 16rpx;
		flex-shrink: 0;
		background-color: #f5f0eb;
	}

	&__info {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}

	&__name {
		font-size: 28rpx;
		font-weight: 700;
		color: #4a3728;
		line-height: 1.5;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	&__spec {
		display: flex;
		align-items: center;
		justify-content: center;
		align-self: flex-start;
		padding: 4rpx 14rpx;
		border-radius: 8rpx;
		background-color: #faf6f3;
	}

	&__spec-text {
		font-size: 22rpx;
		line-height: 1;
		color: #8d6e63;
	}

	&__foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: auto;
		padding-top: 8rpx;
	}

	&__price {
		display: flex;
		align-items: baseline;
		color: #b45d2d;
	}

	&__symbol {
		font-size: 24rpx;
		font-weight: 600;
	}

	&__value {
		font-size: 36rpx;
		font-weight: 800;
	}

	&__qty {
		font-size: 26rpx;
		color: #bbbbbb;
	}
}

.gc-remark {
	width: 100%;
	min-height: 120rpx;
	padding: 20rpx 24rpx;
	border-radius: 16rpx;
	background-color: #faf6f3;
	font-size: 28rpx;
	color: #4a3728;
	line-height: 1.6;
	box-sizing: border-box;
}

.gc-coupon {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;

	&__left {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	&__icon {
		width: 48rpx;
		height: 48rpx;
		border-radius: 12rpx;
		background-color: #f5e8df;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__label {
		font-size: 28rpx;
		font-weight: 600;
		color: #4a3728;
	}

	&__right {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: flex-end;
		gap: 8rpx;
	}

	&__discount {
		font-size: 26rpx;
		font-weight: 600;
		color: #b45d2d;
	}

	&__hint {
		font-size: 26rpx;
		color: #8d6e63;
	}

	&__none {
		font-size: 26rpx;
		color: #cccccc;
	}
}

.gc-bill {
	&__rows {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	&__row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;

		&--total {
			padding-top: 4rpx;
		}
	}

	&__label {
		font-size: 26rpx;
		color: #666666;
	}

	&__value {
		font-size: 26rpx;
		color: #4a3728;

		&--free {
			color: #2eaf6a;
			font-weight: 600;
		}

		&--off {
			color: #b45d2d;
			font-weight: 600;
		}
	}

	&__divider {
		height: 1rpx;
		margin: 20rpx 0 16rpx;
		background-color: #f0ebe6;
	}

	&__total {
		display: flex;
		align-items: baseline;
		color: #b45d2d;
	}

	&__symbol {
		font-size: 26rpx;
		font-weight: 600;
	}

	&__amount {
		font-size: 40rpx;
		font-weight: 800;
	}
}

.gc-footer {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #cccccc;
	padding: 8rpx 0 16rpx;
}

.gc-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 100;

	&__glass {
		position: absolute;
		inset: 0;
		background-color: rgba(255, 255, 255, 0.92);
		border-top: 1rpx solid #f0ebe6;
		box-shadow: 0 -8rpx 32rpx rgba(74, 55, 40, 0.08);
		backdrop-filter: blur(12px);
	}

	&__inner {
		position: relative;
		display: flex;
		align-items: center;
		gap: 24rpx;
		padding: 20rpx var(--ry-page-padding-x);
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	}

	&__info {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: row;
		align-items: baseline;
		gap: 8rpx;
	}

	&__label {
		flex-shrink: 0;
		font-size: 24rpx;
		color: #999999;
	}

	&__price {
		display: flex;
		align-items: baseline;
		color: #b45d2d;
		min-width: 0;
	}

	&__symbol {
		font-size: 24rpx;
		font-weight: 600;
	}

	&__amount {
		font-size: 36rpx;
		font-weight: 800;
	}

	&__btn {
		flex-shrink: 0;
		height: 72rpx;
		padding: 0 36rpx;
		border-radius: 999rpx;
		background: linear-gradient(135deg, #8d6e63, #5d4037);
		box-shadow: 0 6rpx 20rpx rgba(93, 64, 55, 0.24);
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 6rpx;

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
</style>
