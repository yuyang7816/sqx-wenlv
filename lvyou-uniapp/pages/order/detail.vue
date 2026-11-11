<template>
	<view class="od-page" :class="`od-page--${category}`">
		<!-- 顶部状态 -->
		<view class="od-hero" :style="heroStyle">
			<text class="od-hero__status">{{ displayStatus }}</text>
			<text class="od-hero__tip">{{ statusTip }}</text>
			<text v-if="!isHotel" class="od-hero__no">{{ orderNoLabel }} {{ record.orderNo || record.id }}</text>
		</view>

		<view class="od-body">
			<!-- 酒店：商家信息（主） -->
			<view v-if="isHotel" class="od-card od-merchant">
				<view class="od-merchant__main" @click="openMerchantDetail">
					<image class="od-merchant__cover ry-placeholder-img" :src="merchantCover" mode="aspectFill" />
					<view class="od-merchant__info">
						<view class="od-merchant__name-row">
							<text class="od-merchant__name">{{ merchantName }}</text>
						</view>
						<view v-if="merchantAddressLine" class="od-merchant__addr-row" @click.stop="onNavigateMerchant">
							<text class="od-merchant__addr">{{ merchantAddressLine }}</text>
							<view class="od-merchant__guide">
								<text>{{ ui.btnStoreGuide }}</text>
								<u-icon name="arrow-right" :size="12" :color="accentColor" />
							</view>
						</view>
					</view>
				</view>
				<view class="od-merchant__toolbar">
					<view class="od-merchant__btn" @click="onNavigateMerchant">
						<u-icon name="map-fill" :size="16" :color="accentColor" />
						<text>{{ ui.btnNavigate }}</text>
					</view>
					<view class="od-merchant__btn" @click="onCallMerchant">
						<u-icon name="phone-fill" :size="16" :color="accentColor" />
						<text>{{ ui.btnCallMerchant }}</text>
					</view>
				</view>
			</view>

			<!-- 酒店：预订信息（次） -->
			<view v-if="isHotel" class="od-card od-booking">
				<view v-if="record.roomName" class="od-booking__room-row" @click="openRoomDetail">
					<u-icon name="home-fill" :size="16" :color="accentColor" />
					<text class="od-booking__room-name">{{ record.roomName }}</text>
					<text class="od-booking__detail-link">{{ ui.btnRoomDetail }}</text>
					<u-icon name="arrow-right" :size="12" color="#ccc" />
				</view>
				<text v-if="record.roomSpec" class="od-booking__spec">{{ record.roomSpec }}</text>

				<view v-if="record.checkIn" class="od-card__divider" />

				<view v-if="record.checkIn" class="od-stay-timeline">
					<view class="od-stay-timeline__item">
						<view class="od-stay-timeline__badge">住</view>
						<text class="od-stay-timeline__text">{{ displayCheckInFull }}</text>
					</view>
					<view class="od-stay-timeline__item">
						<view class="od-stay-timeline__badge od-stay-timeline__badge--out">离</view>
						<text class="od-stay-timeline__text">{{ displayCheckOutFull }}</text>
					</view>
				</view>

				<view v-if="record.guestNames || record.phone" class="od-card__divider" />

				<view v-if="record.guestNames || record.phone" class="od-guest">
					<view v-if="record.guestNames" class="od-guest__item">
						<u-icon name="account-fill" :size="14" :color="accentColor" />
						<text>{{ record.guestNames }}</text>
					</view>
					<view v-if="record.phone" class="od-guest__item">
						<u-icon name="phone-fill" :size="14" :color="accentColor" />
						<text>{{ record.phone }}</text>
					</view>
				</view>

				<view v-if="showVerifyStatus" class="od-card__divider" />

				<view v-if="showVerifyStatus" class="od-kv-list">
					<view class="od-kv">
						<text class="od-kv__label">{{ ui.labelVerifyStatus }}</text>
						<text class="od-kv__value od-kv__value--pending">{{ verifyStatusText }}</text>
					</view>
				</view>
			</view>

			<!-- 线路：商家信息 -->
			<view v-if="isRoute" class="od-card od-merchant od-merchant--route">
				<text class="od-section-title">{{ ui.sectionMerchant }}</text>
				<view class="od-merchant__main" @click="openRouteStore">
					<image class="od-merchant__cover ry-placeholder-img" :src="merchantCover" mode="aspectFill" />
					<view class="od-merchant__info">
						<view class="od-merchant__name-row">
							<text class="od-merchant__name">{{ merchantName }}</text>
							<view v-if="record.routeId" class="od-merchant__guide">
								<text>{{ ui.btnRouteStore }}</text>
								<u-icon name="arrow-right" :size="12" :color="accentColor" />
							</view>
						</view>
						<view v-if="merchantAddressLine" class="od-merchant__addr-row" @click.stop="onNavigateMerchant">
							<text class="od-merchant__addr">{{ merchantAddressLine }}</text>
						</view>
						<text v-if="merchantPhone" class="od-merchant__phone">{{ merchantPhone }}</text>
					</view>
				</view>
				<view class="od-merchant__toolbar">
					<view class="od-merchant__btn" @click="onNavigateMerchant">
						<u-icon name="map-fill" :size="16" :color="accentColor" />
						<text>{{ ui.btnNavigate }}</text>
					</view>
					<view class="od-merchant__btn" @click="onCallMerchant">
						<u-icon name="phone-fill" :size="16" :color="accentColor" />
						<text>{{ ui.btnCallMerchant }}</text>
					</view>
				</view>
			</view>

			<!-- 线路：线路概要 -->
			<view v-if="isRoute" class="od-card od-route">
				<text class="od-section-title">{{ ui.sectionRoute }}</text>
				<view class="od-route__product" @click="openRouteDetail">
					<image class="od-route__cover ry-placeholder-img" :src="coverImage" mode="aspectFill" />
					<view class="od-route__main">
						<text class="od-route__name">{{ record.routeName || mainTitle }}</text>
						<text v-if="record.duration" class="od-route__meta">{{ record.duration }}</text>
						<view v-if="feeBaseAmount > 0" class="od-route__price">
							<text class="od-route__price-label">{{ ui.labelAmountRoute }}</text>
							<text class="od-route__price-value">{{ formatOrderAmount(feeBaseAmount) }}</text>
						</view>
					</view>
					<u-icon v-if="record.routeId" name="arrow-right" :size="14" color="#C0C4CC" />
				</view>
			</view>

			<!-- 线路：核销码（待出行） -->
			<view v-if="showVerifyCodeCard && isRoute" class="od-card od-verify">
				<text class="od-verify__label">{{ ui.labelVerifyCode }}</text>
				<text class="od-verify__code">{{ verifyCodeText }}</text>
				<view class="od-verify__qr">
					<up-qrcode v-if="verifyCodeText" :val="verifyCodeText" :size="140" />
				</view>
				<text class="od-verify__tip">{{ verifyCodeTipText }}</text>
			</view>

			<!-- 线路：出行信息 -->
			<view v-if="isRoute && routeBookingRows.length" class="od-card od-route-booking">
				<text class="od-section-title">{{ ui.sectionTrip }}</text>
				<view class="od-route-booking__list">
					<view v-for="row in routeBookingRows" :key="row.label" class="od-route-booking__row">
						<text class="od-route-booking__label">{{ row.label }}</text>
						<text class="od-route-booking__value">{{ row.value }}</text>
					</view>
				</view>
			</view>

			<!-- 线路：出行人 -->
			<view v-if="showRouteGuestList" class="od-card od-route-guests">
				<text class="od-section-title">{{ ui.sectionGuestRoute }}</text>
				<view class="od-route-guests__list">
					<view v-for="(guest, index) in routeGuestList" :key="`${guest.name}-${index}`" class="od-route-guests__item">
						<view class="od-route-guests__head">
							<text class="od-route-guests__role">{{ guest.roleLabel || (index === 0 ? ui.labelBooker : ui.labelCompanion) }}</text>
							<text class="od-route-guests__name">{{ guest.name }}</text>
						</view>
						<text v-if="guest.idCard" class="od-route-guests__meta">{{ ui.labelIdCard }} {{ guest.idCard }}</text>
						<text v-if="guest.phone" class="od-route-guests__meta">{{ ui.labelPhone }} {{ guest.phone }}</text>
					</view>
				</view>
			</view>

			<!-- 线路：退改规则 -->
			<view v-if="showCancelPolicy" class="od-card od-route-policy">
				<text class="od-section-title">{{ ui.sectionCancelPolicy }}</text>
				<text class="od-route-policy__text">{{ cancelPolicyText }}</text>
			</view>

			<!-- 商品主卡片 -->
			<view v-if="isGoods" class="od-card">
				<view class="od-card__product">
					<image class="od-card__cover ry-placeholder-img" :src="coverImage" mode="aspectFill" />
					<view class="od-card__info">
						<view class="od-card__shop">
							<u-icon :name="productIcon" :size="13" :color="accentColor" />
							<text>{{ shopLabel }}</text>
						</view>
						<text class="od-card__title">{{ mainTitle }}</text>
						<text v-if="subTitle" class="od-card__sub">{{ subTitle }}</text>
						<text v-if="record.quantity" class="od-card__tag">{{ ui.labelQty }}{{ record.quantity }}</text>
					</view>
				</view>

				<view v-if="record.addressText" class="od-card__divider" />

				<view v-if="record.addressText" class="od-kv-list">
					<view class="od-kv">
						<text class="od-kv__label">{{ ui.sectionAddress }}</text>
						<text class="od-kv__value">{{ record.contactName }} {{ record.phone }}</text>
					</view>
					<view class="od-kv">
						<text class="od-kv__label"></text>
						<text class="od-kv__value od-kv__value--muted">{{ record.addressText }}</text>
					</view>
				</view>
			</view>

			<!-- 好物：物流信息 -->
			<view v-if="showLogisticsCard" class="od-card od-logistics">
				<view class="od-logistics__head">
					<text class="od-section-title od-logistics__title">{{ ui.sectionLogistics }}</text>
					<text class="od-logistics__link" @click="openLogisticsPage">{{ ui.btnViewLogistics }}</text>
				</view>
				<view class="od-kv-list">
					<view v-if="logisticsInfo.company" class="od-kv">
						<text class="od-kv__label">{{ ui.labelLogisticsCompany }}</text>
						<text class="od-kv__value">{{ logisticsInfo.company }}</text>
					</view>
					<view v-if="logisticsInfo.trackingNo" class="od-kv od-logistics__no-row">
						<text class="od-kv__label">{{ ui.labelLogisticsNo }}</text>
						<view class="od-logistics__no">
							<text class="od-kv__value">{{ logisticsInfo.trackingNo }}</text>
							<text class="od-logistics__copy" @click="copyLogisticsNo">{{ ui.btnCopyLogisticsNo }}</text>
						</view>
					</view>
				</view>
			</view>
			<view v-else-if="isGoods && canViewLogistics" class="od-card od-logistics od-logistics--entry" @click="openLogisticsPage">
				<text class="od-section-title od-logistics__title">{{ ui.sectionLogistics }}</text>
				<view class="od-logistics__entry-row">
					<text class="od-logistics__entry-tip">{{ statusTip }}</text>
					<text class="od-logistics__link">{{ ui.btnViewLogistics }}</text>
				</view>
			</view>

			<!-- 核销码（酒店待入住） -->
			<view v-if="showVerifyCodeCard && isHotel" class="od-card od-verify">
				<text class="od-verify__label">{{ ui.labelVerifyCode }}</text>
				<text class="od-verify__code">{{ verifyCodeText }}</text>
				<view class="od-verify__qr">
					<up-qrcode v-if="verifyCodeText" :val="verifyCodeText" :size="140" />
				</view>
				<text class="od-verify__tip">{{ verifyCodeTipText }}</text>
			</view>

			<!-- 订单/预约信息 -->
			<view class="od-card od-order-meta">
				<text class="od-section-title">{{ infoSectionTitle }}</text>
				<view class="od-order-meta__row">
					<text class="od-order-meta__label">{{ orderNoLabel }}</text>
					<view class="od-order-meta__right">
						<text class="od-order-meta__no">{{ record.orderNo || record.id }}</text>
						<text class="od-order-meta__copy" @click="copyOrderNo">{{ ui.btnCopyOrderNo }}</text>
					</view>
				</view>
				<view v-if="orderMetaRows.length" class="od-card__divider" />
				<view v-if="orderMetaRows.length" class="od-kv-list">
					<view v-for="row in orderMetaRows" :key="row.label" class="od-kv">
						<text class="od-kv__label">{{ row.label }}</text>
						<text class="od-kv__value">{{ row.value }}</text>
					</view>
				</view>
			</view>

			<!-- 费用明细 -->
			<view v-if="showFeeCard" class="od-card od-fee">
				<text class="od-fee__title">{{ ui.sectionFee }}</text>
				<view class="od-fee__row">
					<text class="od-fee__label">{{ feeLabel }}</text>
					<text class="od-fee__value">{{ formatOrderAmount(feeBaseAmount) }}</text>
				</view>
				<view v-if="hasDiscount" class="od-fee__row">
					<text class="od-fee__label">{{ ui.labelDiscount }}</text>
					<text class="od-fee__value od-fee__value--discount">-{{ ui.currencySymbol }}{{ record.discountAmount }}</text>
				</view>
				<view v-if="isGoods" class="od-fee__row">
					<text class="od-fee__label">{{ ui.labelFeeShip }}</text>
					<text class="od-fee__value" :class="{ 'od-fee__value--free': displayShippingFee <= 0 }">{{ shippingFeeText }}</text>
				</view>
				<view class="od-fee__total">
					<text class="od-fee__total-label">{{ feeTotalLabel }}</text>
					<text class="od-fee__total-value" :style="{ color: priceColor }">{{ formatOrderAmount(displayTotalAmount) }}</text>
				</view>
			</view>
		</view>

		<!-- 底部操作栏 -->
		<view v-if="hasBottomBar" class="od-bar">
			<view class="od-bar__actions">
				<view v-for="action in secondaryActions" :key="action.key" class="od-bar__btn od-bar__btn--ghost"
					:style="{ borderColor: accentColor, color: accentColor }" @click="onDetailAction(action.key)">
					<text>{{ action.label }}</text>
				</view>
				<view v-if="primaryAction" class="od-bar__btn od-bar__btn--primary" :style="primaryBtnStyle"
					@click="onDetailAction(primaryAction.key)">
					<text>{{ primaryAction.label }}</text>
				</view>
			</view>
		</view>

		<GlobalPayHost />
		<GlobalRouteRefundHost />
	</view>
</template>

<script setup>
	import {
		computed,
		nextTick,
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
		hasToken
	} from '@/common/api/auth.js';
	import {
		fetchOrderDetail
	} from '@/common/api/order.js';
	import {
		getUserOrderActions,
		runUserOrderAction
	} from '@/common/order-actions.js';
	import GlobalPayHost from '@/components/global-pay-host.vue';
	import GlobalRouteRefundHost from '@/components/global-route-refund-host.vue';
	import {
		ORDER_UI,
		ORDER_CATEGORY_HOTEL,
		ORDER_CATEGORY_GOODS,
		ORDER_CATEGORY_ROUTE,
		formatOrderAmount,
		formatRouteTripLine,
		inferOrderCategory,
		mapOrderVo,
		resolveOrderDisplayStatus,
		resolvePayMethodLabel,
		isHotelPendingCheckin,
		isRoutePendingVerify,
		resolveOrderVerifyCode,
		extractLogisticsInfo,
		resolveGoodsItemsAmount,
		resolveDisplayShippingFee,
		canViewOrderLogistics,
		openOrderLogistics
	} from '@/common/order-ui.js';

	const ui = ORDER_UI;
	const record = ref({});
	const orderId = ref('');
	const autoPay = ref(false);
	const payTriggered = ref(false);
	const loading = ref(false);

	const category = computed(() => inferOrderCategory(record.value) || ORDER_CATEGORY_HOTEL);
	const isHotel = computed(() => category.value === ORDER_CATEGORY_HOTEL);
	const isGoods = computed(() => category.value === ORDER_CATEGORY_GOODS);
	const isRoute = computed(() => category.value === ORDER_CATEGORY_ROUTE);

	const accentColor = computed(() => {
		if (isRoute.value) return getModuleColor('route');
		if (isHotel.value) return getModuleColor('hotel');
		return getModuleColor('heritageShop');
	});

	const priceColor = computed(() => {
		if (isRoute.value) return '#FF6600';
		if (isHotel.value) return '#FF6600';
		return '#B45D2D';
	});

	const heroStyle = computed(() => {
		if (isRoute.value) {
			return {
				background: `linear-gradient(135deg, ${getModuleColor('route')} 0%, #FF8A3D 100%)`
			};
		}
		if (isHotel.value) {
			return {
				background: `linear-gradient(135deg, ${accentColor.value} 0%, #FF8A3D 100%)`
			};
		}
		return {
			background: 'linear-gradient(135deg, #8D6E63 0%, #B45D2D 100%)'
		};
	});

	const primaryBtnStyle = computed(() => {
		if (isRoute.value) {
			return {
				background: 'linear-gradient(135deg, #FF8A3D, #FF6600)'
			};
		}
		if (isHotel.value) {
			return {
				background: 'linear-gradient(135deg, #FF8A3D, #FF6600)'
			};
		}
		return {
			background: 'linear-gradient(135deg, #8D6E63, #5D4037)'
		};
	});

	const productIcon = computed(() => {
		if (isRoute.value) return 'list-dot';
		if (isHotel.value) return 'home-fill';
		return 'bag-fill';
	});

	const statusTip = computed(() => {
		if (isRoute.value) {
			if (record.value.status === '已完成') return ui.routeTipDone;
			if (record.value.status === '待确认' || record.value.statusCode === 'pending_confirm') {
				return ui.routeTipConfirm;
			}
			return ui.routeTipPending;
		}
		if (isHotel.value) {
			if (record.value.verified || record.value.status === '已完成') {
				return ui.hotelTipDone;
			}
			return ui.hotelTipPending;
		}
		if (record.value.status === '待收货') return ui.goodsTipReceive;
		return ui.goodsTipPending;
	});

	const orderNoLabel = computed(() => (isRoute.value ? ui.labelBookingNo : ui.orderNo));
	const infoSectionTitle = computed(() => (isRoute.value ? ui.sectionBookingInfo : ui.sectionInfo));
	const feeTotalLabel = computed(() => (isRoute.value ? ui.labelFeeTotalRoute : ui.labelFeeTotal));

	const shopLabel = computed(() => record.value.merchantName || ui.shopName);

	const coverImage = computed(() => record.value.goodsCover || PLACEHOLDER_LOGO);

	const merchantCover = computed(() => record.value.merchantCover || record.value.hotelCover || PLACEHOLDER_LOGO);

	const mainTitle = computed(() => {
		if (isHotel.value) return record.value.roomName || record.value.merchantName;
		return record.value.goodsName || record.value.merchantName;
	});

	const subTitle = computed(() => record.value.goodsSpec || '');

	const merchantName = computed(() => {
		const name = String(record.value.merchantName || '').trim();
		const routeTitle = String(record.value.routeName || '').trim();
		if (isRoute.value && name && name !== routeTitle) return name;
		if (isRoute.value) return ui.labelRouteStore;
		return record.value.merchantName || ui.shopName;
	});

	const merchantAddressLine = computed(() => {
		const address = String(record.value.merchantAddress || '').trim();
		if (isRoute.value) return address;
		const region = record.value.merchantRegion || record.value.destinationName || '';
		if (address) return address;
		return region;
	});

	const merchantPhone = computed(() => record.value.merchantPhone || '');

	const merchantLatitude = computed(() => Number(record.value.latitude) || 0);

	const merchantLongitude = computed(() => Number(record.value.longitude) || 0);

	const feeLabel = computed(() => {
		if (isRoute.value) return ui.labelFeeRoute;
		if (isHotel.value) return ui.labelFeeRoom;
		return ui.labelFeeGoods;
	});

	const hasDiscount = computed(() => Number(record.value.discountAmount) > 0);

	const feeBaseAmount = computed(() => {
		if (isGoods.value) {
			return resolveGoodsItemsAmount(record.value);
		}
		const total = Number(record.value.totalAmount);
		if (total > 0) return total;
		const amount = Number(record.value.amount) || 0;
		const discount = Number(record.value.discountAmount) || 0;
		return amount + discount;
	});

	const displayShippingFee = computed(() => resolveDisplayShippingFee(record.value));

	const shippingFeeText = computed(() => (
		displayShippingFee.value > 0 ? formatOrderAmount(displayShippingFee.value) : ui.shipFree
	));

	const canViewLogistics = computed(() => canViewOrderLogistics(record.value));

	const payAmount = computed(() => Number(record.value.amount) || 0);

	const displayTotalAmount = computed(() => payAmount.value || feeBaseAmount.value);

	const showFeeCard = computed(() => !isRoute.value || feeBaseAmount.value > 0);

	const verifyCodeText = computed(() => resolveOrderVerifyCode(record.value));

	const showVerifyCodeCard = computed(() => {
		if (!verifyCodeText.value) return false;
		if (isHotel.value) return isHotelPendingCheckin(record.value);
		if (isRoute.value) return isRoutePendingVerify(record.value);
		return false;
	});

	const verifyCodeTipText = computed(() => {
		if (isRoute.value) return ui.routeVerifyCodeTip;
		return ui.verifyCodeTip;
	});

	const displayStatus = computed(() => resolveOrderDisplayStatus(record.value));

	const logisticsInfo = computed(() => record.value.logisticsInfo || extractLogisticsInfo(record.value.remark));

	const showLogisticsCard = computed(() => {
		if (!isGoods.value || !logisticsInfo.value) return false;
		const code = record.value.statusCode || '';
		const status = displayStatus.value;
		return code === 'pending_receive'
			|| code === 'completed'
			|| status === ui.statusPendingReceive
			|| status === ui.statusCompleted;
	});

	const orderTimeLine = computed(() => {
		if (isRoute.value) return formatRouteTripLine(record.value);
		return record.value.date || '';
	});

	const displayCheckIn = computed(() => formatShortDate(record.value.checkIn));
	const displayCheckOut = computed(() => formatShortDate(record.value.checkOut));
	const displayTripDate = computed(() => formatShortDate(record.value.tripDate));

	const displayTripDateFull = computed(() => {
		if (!record.value.tripDate) return '';
		const week = formatWeekday(record.value.tripDate);
		return week ? `${record.value.tripDate} ${week}` : record.value.tripDate;
	});

	const displayCheckInFull = computed(() => {
		if (!record.value.checkIn) return '--';
		const week = formatWeekday(record.value.checkIn);
		return `${displayCheckIn.value}(${week})`;
	});

	const displayCheckOutFull = computed(() => {
		if (!record.value.checkOut) return '--';
		const week = formatWeekday(record.value.checkOut);
		return `${displayCheckOut.value}(${week})`;
	});

	const routeGuestList = computed(() => (Array.isArray(record.value.guestList) ? record.value.guestList : []));
	const showRouteGuestList = computed(() => isRoute.value && routeGuestList.value.length > 0);
	const cancelPolicyText = computed(() => String(record.value.cancelPolicySummary || '').trim());
	const showCancelPolicy = computed(() => isRoute.value && !!cancelPolicyText.value);

	const routeBookingRows = computed(() => {
		const rows = [];
		if (record.value.tripDate) {
			rows.push({
				label: ui.labelTripDate,
				value: displayTripDateFull.value
			});
		}
		if (record.value.guestCount) {
			rows.push({
				label: ui.labelGuestCount,
				value: `${record.value.guestCount}${ui.guestCountSuffix}`
			});
		}
		if (record.value.contactName) {
			rows.push({
				label: ui.labelContactPerson,
				value: record.value.contactName
			});
		}
		if (record.value.phone) {
			rows.push({
				label: ui.labelPhone,
				value: record.value.phone
			});
		}
		if (record.value.meetPoint) {
			rows.push({
				label: ui.labelMeetPoint,
				value: record.value.meetPoint
			});
		}
		if (record.value.remark) {
			rows.push({
				label: ui.labelRemark,
				value: record.value.remark
			});
		}
		return rows;
	});

	const orderMetaRows = computed(() => {
		const rows = [];
		if (record.value.date) {
			rows.push({
				label: isRoute.value ? '预约时间' : ui.labelOrderTime,
				value: record.value.date
			});
		}
		if (record.value.payMethod) {
			rows.push({
				label: ui.labelPay,
				value: resolvePayMethodLabel(record.value.payMethod)
			});
		}
		if (record.value.verified && record.value.verifiedAt) {
			rows.push({
				label: ui.labelVerifyTime,
				value: record.value.verifiedAt
			});
		}
		return rows;
	});

	const showVerifyStatus = computed(() => {
		if (!isHotel.value) return false;
		if (record.value.verified) return false;
		const status = record.value.status || '';
		const code = record.value.statusCode || '';
		if (['待支付', '已取消', '退款中', '已退款'].includes(status)) return false;
		if (['pending_pay', 'cancelled', 'refunding', 'refunded'].includes(code)) return false;
		return !showVerifyCodeCard.value;
	});

	const verifyStatusText = computed(() => {
		if (record.value.verified) return ui.statusVerified;
		if (record.value.status === '已完成') return ui.statusVerified;
		return ui.statusPendingVerify;
	});

	const detailActions = computed(() => {
		return getUserOrderActions(record.value).filter(
			(action) => action.key !== 'verifyCode' && action.key !== 'map'
		);
	});

	const primaryAction = computed(() => {
		const list = detailActions.value;
		return list.find((item) => item.tone === 'primary') || null;
	});

	const secondaryActions = computed(() => {
		const primary = primaryAction.value;
		return detailActions.value.filter((item) => item !== primary);
	});

	const hasBottomBar = computed(() => detailActions.value.length > 0);

	function formatShortDate(dateStr) {
		if (!dateStr) return '--';
		const parts = String(dateStr).split('-');
		if (parts.length < 3) return dateStr;
		return `${Number(parts[1])}月${Number(parts[2])}日`;
	}

	function formatWeekday(dateStr) {
		if (!dateStr) return '';
		const date = new Date(String(dateStr).replace(/-/g, '/'));
		if (Number.isNaN(date.getTime())) return '';
		const labels = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
		return labels[date.getDay()] || '';
	}

	function requireLogin() {
		if (hasToken()) return true;
		uni.showToast({
			title: ui.loginTip,
			icon: 'none'
		});
		setTimeout(() => {
			uni.navigateTo({
				url: '/pages/my/login',
				fail: () => {}
			});
		}, 400);
		return false;
	}

	async function loadDetail() {
		if (!orderId.value || !hasToken()) return;
		if (loading.value) return;
		loading.value = true;
		try {
			const res = await fetchOrderDetail(orderId.value);
			if (res.code === 0 && res.data) {
				record.value = mapOrderVo(res.data);
				uni.setNavigationBarTitle({
					title: inferOrderCategory(record.value) === ORDER_CATEGORY_ROUTE
						? ui.detailTitleRoute
						: ui.detailTitle
				});
			} else {
				record.value = {};
				uni.showToast({
					title: res.msg || ui.loadFail,
					icon: 'none'
				});
			}
		} catch (e) {
			record.value = {};
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function tryAutoPay() {
		if (!autoPay.value || payTriggered.value || !record.value?.id) return;
		if ((record.value.statusCode || '') !== 'pending_pay') {
			autoPay.value = false;
			return;
		}
		payTriggered.value = true;
		nextTick(() => {
			runUserOrderAction('pay', record.value, {
				onChanged: () => loadDetail()
			});
		});
	}

	function openMerchantDetail() {
		const hotelId = record.value.hotelId;
		if (hotelId) {
			uni.navigateTo({
				url: `/mobile-design/hotel/detail?id=${hotelId}`
			});
			return;
		}
		uni.showToast({
			title: '暂无商家详情',
			icon: 'none'
		});
	}

	function openRouteStore() {
		const routeId = record.value.routeId;
		if (!routeId) {
			uni.showToast({
				title: '暂无门店信息',
				icon: 'none'
			});
			return;
		}
		uni.navigateTo({
			url: `/mobile-design/route/store?id=${routeId}`
		});
	}

	function openRouteDetail() {
		const routeId = record.value.routeId;
		if (!routeId) return;
		uni.navigateTo({
			url: `/mobile-design/route/detail?id=${routeId}`
		});
	}

	function openRoomDetail() {
		const hotelId = record.value.hotelId;
		const roomId = record.value.roomId;
		if (hotelId && roomId) {
			uni.navigateTo({
				url: `/mobile-design/hotel/room-detail?hotelId=${hotelId}&roomId=${roomId}`
			});
			return;
		}
		openMerchantDetail();
	}

	function copyOrderNo() {
		const orderNo = record.value.orderNo || record.value.id;
		if (!orderNo) return;
		uni.setClipboardData({
			data: String(orderNo),
			success: () => {
				uni.showToast({
					title: ui.copyOrderNoSuccess,
					icon: 'none'
				});
			}
		});
	}

	function copyLogisticsNo() {
		const trackingNo = logisticsInfo.value?.trackingNo;
		if (!trackingNo) return;
		uni.setClipboardData({
			data: String(trackingNo),
			success: () => {
				uni.showToast({
					title: ui.copyLogisticsNoSuccess,
					icon: 'none'
				});
			}
		});
	}

	function openLogisticsPage() {
		openOrderLogistics(record.value);
	}

	function onCallMerchant() {
		const phone = String(merchantPhone.value || '').trim();
		if (!phone) {
			uni.showToast({
				title: '暂无联系电话',
				icon: 'none'
			});
			return;
		}
		/* #ifdef H5 */
		window.location.href = `tel:${phone}`;
		return;
		/* #endif */
		uni.makePhoneCall({
			phoneNumber: phone,
			fail: () => {
				uni.showToast({
					title: `请拨打 ${phone}`,
					icon: 'none'
				});
			}
		});
	}

	function onNavigateMerchant() {
		const latitude = merchantLatitude.value;
		const longitude = merchantLongitude.value;
		const address = merchantAddressLine.value;
		const name = merchantName.value;
		if (!latitude || !longitude) {
			if (address) {
				uni.showToast({
					title: address,
					icon: 'none'
				});
				return;
			}
			uni.showToast({
				title: '暂无位置信息',
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
					title: address || '打开导航失败',
					icon: 'none'
				});
			}
		});
	}

	async function onDetailAction(actionKey) {
		await runUserOrderAction(actionKey, record.value, {
			onChanged: () => loadDetail()
		});
	}

	onLoad((options) => {
		orderId.value = options?.id ? String(options.id) : '';
		autoPay.value = String(options?.pay || '') === '1';
		payTriggered.value = false;
		uni.setNavigationBarTitle({
			title: ui.detailTitle
		});
	});

	onShow(async () => {
		if (!requireLogin()) return;
		await loadDetail();
		tryAutoPay();
	});
</script>

<style lang="scss" scoped>
	.od-page {
		min-height: 100vh;
		padding-bottom: calc(130rpx + env(safe-area-inset-bottom));
		box-sizing: border-box;
	}

	.od-page--hotel {
		background: #faf7f2;

		.od-merchant__guide text {
			color: #ff6600;
		}

		.od-merchant__btn {
			background: #fff8f3;
			border-color: rgba(255, 102, 0, 0.12);

			text {
				color: #ff6600;
			}
		}

		.od-stay-timeline__badge {
			background: #ff6600;

			&--out {
				background: #ff8a3d;
			}
		}

		.od-order-meta__copy {
			color: #ff6600;
		}

		.od-verify__code {
			color: #ff6600;
		}
	}

	.od-page--goods {
		background: #f8f5f2;
	}

	.od-page--route {
		background: #faf7f2;
	}

	.od-hero {
		padding: 48rpx var(--ry-page-padding-x) 56rpx;
		display: flex;
		flex-direction: column;
		gap: 10rpx;

		&__status {
			font-size: 40rpx;
			font-weight: 700;
			color: #fff;
			line-height: 1.2;
		}

		&__tip {
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.88);
			line-height: 1.5;
		}

		&__no {
			margin-top: 4rpx;
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.72);
		}
	}

	.od-body {
		padding: 0 var(--ry-page-padding-x) 24rpx;
		margin-top: -28rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
		position: relative;
		z-index: 2;
	}

	.od-section-title {
		display: block;
		font-size: 28rpx;
		font-weight: 700;
		color: #1a1a1a;
		margin-bottom: 20rpx;
	}

	.od-merchant {
		&__icon {
			width: 88rpx;
			height: 88rpx;
			border-radius: 16rpx;
			background: #fff8f3;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__phone {
			font-size: 24rpx;
			color: #666;
			line-height: 1.4;
		}

		&--route {
			.od-merchant__btn {
				background: #fff8f3;
				border-color: rgba(255, 102, 0, 0.12);

				text {
					color: #ff6600;
				}
			}
		}

		&__main {
			display: flex;
			gap: 20rpx;
			align-items: flex-start;

			&:active {
				opacity: 0.85;
			}
		}

		&__cover {
			width: 120rpx;
			height: 120rpx;
			border-radius: 14rpx;
			flex-shrink: 0;
			background: #f0f0f0;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 10rpx;
		}

		&__name-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 12rpx;
		}

		&__name {
			flex: 1;
			font-size: 30rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.35;
		}

		&__addr-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 12rpx;
		}

		&__addr {
			flex: 1;
			font-size: 24rpx;
			color: #666;
			line-height: 1.5;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		&__guide {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 4rpx;
			line-height: 1;

			text {
				font-size: 22rpx;
				color: #2979ff;
				line-height: 1;
			}
		}

		&__toolbar {
			display: flex;
			gap: 16rpx;
			margin-top: 20rpx;
			padding-top: 16rpx;
			border-top: 1rpx solid #f0f2f5;
		}

		&__btn {
			flex: 1;
			height: 64rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			border-radius: 12rpx;
			background: #f5f8ff;
			border: 1rpx solid rgba(41, 121, 255, 0.12);

			text {
				font-size: 24rpx;
				color: #2979ff;
				font-weight: 500;
				line-height: 1;
			}

			&:active {
				opacity: 0.85;
			}
		}
	}

	.od-booking {
		&__room-row {
			display: flex;
			align-items: center;
			gap: 10rpx;

			&:active {
				opacity: 0.85;
			}
		}

		&__room-name {
			flex: 1;
			font-size: 28rpx;
			font-weight: 600;
			color: #333;
			line-height: 1.4;
		}

		&__detail-link {
			font-size: 24rpx;
			color: #999;
		}

		&__spec {
			display: block;
			margin-top: 12rpx;
			font-size: 22rpx;
			color: #999;
			line-height: 1.5;
		}
	}

	.od-stay-timeline {
		display: flex;
		flex-direction: column;
		gap: 16rpx;

		&__item {
			display: flex;
			align-items: center;
			gap: 16rpx;
		}

		&__badge {
			width: 40rpx;
			height: 40rpx;
			border-radius: 50%;
			background: #2979ff;
			color: #fff;
			font-size: 20rpx;
			text-align: center;
			line-height: 40rpx;
			// font-weight: 700;
			// display: flex;
			// align-items: center;
			// justify-content: center;
			// flex-shrink: 0;

			&--out {
				background: #5b9bf5;
			}
		}

		&__text {
			font-size: 26rpx;
			font-weight: 400;
			color: #333;
			line-height: 1.4;
		}
	}

	.od-guest {
		display: flex;
		flex-direction: column;
		gap: 12rpx;

		&__item {
			display: flex;
			align-items: center;
			gap: 10rpx;

			text {
				font-size: 26rpx;
				color: #333;
			}
		}
	}

	.od-order-meta {
		&__row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 24rpx;
		}

		&__label {
			flex-shrink: 0;
			font-size: 26rpx;
			color: #999;
		}

		&__right {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 24rpx;
		}

		&__no {
			flex: 0 1 auto;
			font-size: 24rpx;
			color: #666;
			text-align: right;
			word-break: break-all;
		}

		&__copy {
			flex-shrink: 0;
			font-size: 24rpx;
			color: #2979ff;
		}
	}

	.od-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 24rpx;
		box-shadow: 0 4rpx 20rpx rgba(15, 23, 42, 0.05);

		&__product {
			display: flex;
			gap: 20rpx;
		}

		&__cover {
			width: 144rpx;
			height: 144rpx;
			border-radius: 14rpx;
			flex-shrink: 0;
			background: #f0f0f0;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__shop {
			display: flex;
			align-items: center;
			gap: 6rpx;

			text {
				font-size: 22rpx;
				color: #999;
			}
		}

		&__title {
			font-size: 30rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.35;
		}

		&__sub {
			font-size: 24rpx;
			color: #888;
			line-height: 1.45;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
			overflow: hidden;
		}

		&__tag {
			align-self: flex-start;
			margin-top: 4rpx;
			padding: 2rpx 12rpx;
			border-radius: 6rpx;
			background: #f5f0ec;
			font-size: 22rpx;
			color: #8d6e63;
		}

		&__divider {
			height: 1rpx;
			background: #f0f2f5;
			margin: 20rpx 0;
		}
	}

	.od-route {
		&__product {
			display: flex;
			align-items: center;
			gap: 20rpx;

			&:active {
				opacity: 0.88;
			}
		}

		&__cover {
			width: 160rpx;
			height: 140rpx;
			border-radius: 14rpx;
			flex-shrink: 0;
			background: #f0f0f0;
		}

		&__main {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
			line-height: 1.4;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 2;
			overflow: hidden;
		}

		&__meta {
			font-size: 24rpx;
			color: #999;
			line-height: 1.4;
		}

		&__price {
			display: flex;
			align-items: baseline;
			gap: 8rpx;
			margin-top: 4rpx;
		}

		&__price-label {
			font-size: 22rpx;
			color: #999;
			line-height: 1;
		}

		&__price-value {
			font-size: 32rpx;
			font-weight: 800;
			color: #ff6600;
			line-height: 1;
		}
	}

	.od-route-guests {
		&__list {
			display: flex;
			flex-direction: column;
			gap: 16rpx;
		}

		&__item {
			padding: 20rpx 24rpx;
			background: #f8f9fb;
			border-radius: 12rpx;
		}

		&__head {
			display: flex;
			align-items: center;
			gap: 12rpx;
			margin-bottom: 8rpx;
		}

		&__role {
			font-size: 22rpx;
			color: #fff;
			background: #ff6600;
			padding: 2rpx 12rpx;
			border-radius: 6rpx;
			line-height: 1.6;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 600;
			color: #333;
		}

		&__meta {
			display: block;
			font-size: 24rpx;
			color: #666;
			line-height: 1.6;
		}
	}

	.od-route-policy {
		&__text {
			display: block;
			font-size: 24rpx;
			color: #666;
			line-height: 1.7;
			white-space: pre-wrap;
		}
	}

	.od-route-booking {
		&__list {
			display: flex;
			flex-direction: column;
			gap: 0;
		}

		&__row {
			display: flex;
			align-items: flex-start;
			justify-content: space-between;
			gap: 24rpx;
			padding: 18rpx 0;
			border-bottom: 1rpx solid #f5f6f8;

			&:last-child {
				border-bottom: none;
				padding-bottom: 0;
			}

			&:first-child {
				padding-top: 0;
			}
		}

		&__label {
			flex-shrink: 0;
			width: 140rpx;
			font-size: 26rpx;
			color: #999;
			line-height: 1.5;
		}

		&__value {
			flex: 1;
			font-size: 26rpx;
			color: #333;
			text-align: right;
			line-height: 1.5;
			word-break: break-all;
		}
	}

	.od-date-row {
		display: flex;
		align-items: center;

		&__item {
			flex: 1;
			display: flex;
			flex-direction: column;
			gap: 6rpx;

			&--right {
				align-items: flex-end;
				text-align: right;
			}
		}

		&__label {
			font-size: 22rpx;
			color: #aaa;
		}

		&__value {
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
		}

		&__mid {
			flex-shrink: 0;
			padding: 0 16rpx;

			text {
				font-size: 22rpx;
				font-weight: 600;
				color: #666;
				padding: 4rpx 14rpx;
				border-radius: 999rpx;
				background: #f5f6f8;
			}
		}
	}

	.od-kv-list {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.od-kv {
		display: flex;
		justify-content: space-between;
		gap: 24rpx;

		&__label {
			flex-shrink: 0;
			width: 140rpx;
			font-size: 26rpx;
			color: #999;
		}

		&__value {
			flex: 1;
			font-size: 26rpx;
			color: #333;
			text-align: right;
			line-height: 1.5;
			word-break: break-all;

			&--muted {
				color: #666;
			}

			&--success {
				color: #2eaf6a;
				font-weight: 600;
			}

			&--pending {
				color: #ff6600;
				font-weight: 600;
			}
		}
	}

	.od-logistics {
		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 20rpx;
		}

		&__title {
			margin-bottom: 0;
		}

		&__link {
			flex-shrink: 0;
			font-size: 24rpx;
			font-weight: 600;
			color: #b45d2d;
		}

		&--entry {
			&:active {
				opacity: 0.88;
			}
		}

		&__entry-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__entry-tip {
			flex: 1;
			font-size: 24rpx;
			color: #888;
			line-height: 1.5;
		}

		&__no-row {
			align-items: center;
		}

		&__no {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 12rpx;
		}

		&__copy {
			flex-shrink: 0;
			font-size: 22rpx;
			font-weight: 600;
			color: #b45d2d;
			padding: 4rpx 12rpx;
			border-radius: 999rpx;
			background: #fff6f0;
		}
	}

	.od-verify {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		padding: 28rpx 24rpx;

		&__label {
			font-size: 26rpx;
			font-weight: 600;
			color: #666;
		}

		&__code {
			font-size: 40rpx;
			font-weight: 700;
			color: #2979ff;
			letter-spacing: 6rpx;
			line-height: 1.2;
		}

		&__qr {
			padding: 12rpx;
			border-radius: 12rpx;
			background: #fafbfc;
			border: 1rpx solid #eef1f5;
		}

		&__tip {
			font-size: 22rpx;
			color: #aaa;
			text-align: center;
			line-height: 1.5;
		}
	}

	.od-fee {
		&__title {
			display: block;
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
			margin-bottom: 16rpx;
		}

		&__row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 10rpx 0;
		}

		&__label {
			font-size: 26rpx;
			color: #888;
		}

		&__value {
			font-size: 26rpx;
			color: #333;

			&--free {
				color: #2eaf6a;
			}

			&--discount {
				color: #ff6600;
				font-weight: 600;
			}
		}

		&__total {
			display: flex;
			align-items: baseline;
			justify-content: space-between;
			margin-top: 12rpx;
			padding-top: 16rpx;
			border-top: 1rpx solid #f0f2f5;
		}

		&__total-left {
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__total-note {
			font-size: 22rpx;
			color: #999;
			line-height: 1.2;
		}

		&__total-right {
			display: flex;
			align-items: baseline;
			gap: 8rpx;
		}

		&__ref-label {
			font-size: 22rpx;
			color: #999;
			line-height: 1;
		}

		&__total-label {
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a1a;
		}

		&__total-value {
			font-size: 36rpx;
			font-weight: 800;
		}

		&__time {
			display: block;
			margin-top: 12rpx;
			font-size: 22rpx;
			color: #ccc;
		}
	}

	.od-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding: 16rpx var(--ry-page-padding-x);
		padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
		background: #fff;
		box-shadow: 0 -2rpx 16rpx rgba(0, 0, 0, 0.05);

		&__actions {
			flex: 1;
			width: 100%;
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 16rpx;
			min-width: 0;
		}

		&__btn {
			flex: 1;
			min-width: 0;
			height: 80rpx;
			padding: 0 24rpx;
			border-radius: 999rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			box-sizing: border-box;

			text {
				font-size: 28rpx;
				font-weight: 600;
				line-height: 1;
				white-space: nowrap;
			}

			&:active {
				opacity: 0.88;
			}

			&--ghost {
				background: #fff;
				border: 2rpx solid;
			}

			&--primary text {
				color: #fff;
			}
		}
	}
</style>
