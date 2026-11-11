<template>
	<view v-if="summary && dashboard" class="mc-page" :class="{ 'mc-page--verify': isHotelMerchant }" :style="pageStyle">
		<view class="mc-hero">
			<view class="mc-hero__top" @click="goInfo">
				<view class="mc-hero__circle mc-hero__circle--1" />
				<view class="mc-hero__circle mc-hero__circle--2" />
				<view class="mc-hero__main">
					<view class="mc-hero__head">
						<view class="mc-hero__badge"
							:style="{ backgroundColor: summary.merchant.typeBgColor, color: summary.merchant.typeColor }">
							<u-icon :name="summary.merchant.typeIcon" :color="summary.merchant.typeColor" :size="14" />
							<text>{{ summary.merchant.typeLabel }}</text>
						</view>
						<view class="mc-hero__link">
							<text>门店详情</text>
							<u-icon name="arrow-right" color="rgba(255,255,255,0.85)" :size="14" />
						</view>
					</view>
					<text class="mc-hero__name">{{ summary.merchant.storeName }}</text>
					<view class="mc-hero__status-row" @click.stop>
						<view class="mc-hero__status-left">
							<view class="mc-hero__status-dot" :class="{ 'mc-hero__status-dot--closed': !isOpen }" />
							<text class="mc-hero__status">{{ summary.openStatusLabel }}</text>
						</view>
						<u-switch v-model="isOpen" size="20" active-color="#2EAF6A" inactive-color="rgba(255,255,255,0.35)"
							:disabled="!depositPaid" @change="onToggleOpen" />
					</view>
				</view>
				<view class="mc-hero__actions" @click.stop>
					<view class="mc-hero__action" @click="goMessages">
						<view class="mc-hero__action-icon">
							<u-icon name="bell-fill" color="#FFFFFF" :size="20" />
							<view v-if="summary.messageCount > 0" class="mc-hero__action-badge">
								<text>{{ summary.messageCount > 99 ? '99+' : summary.messageCount }}</text>
							</view>
						</view>
						<text class="mc-hero__action-label">{{ ui.heroMessage }}</text>
					</view>
					<view class="mc-hero__action" @click="goWallet">
						<view class="mc-hero__action-icon">
							<u-icon name="rmb-circle-fill" color="#FFFFFF" :size="20" />
						</view>
						<text class="mc-hero__action-label">{{ ui.heroWallet }}</text>
					</view>
					<view class="mc-hero__action" :class="{ 'mc-hero__action--disabled': !depositPaid }"
						@click="toggleOpenFromAction">
						<view class="mc-hero__action-icon">
							<u-icon :name="isOpen ? 'checkmark-circle-fill' : 'pause-circle-fill'" color="#FFFFFF" :size="20" />
						</view>
						<text class="mc-hero__action-label">{{ ui.heroOpen }}</text>
					</view>
					<view class="mc-hero__action" @click="goStoreDecor">
						<view class="mc-hero__action-icon">
							<u-icon name="edit-pen-fill" color="#FFFFFF" :size="20" />
						</view>
						<text class="mc-hero__action-label">{{ ui.heroDecor }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="!depositPaid" class="mc-deposit-alert ry-card" @click="goDeposit">
			<u-icon name="info-circle-fill" color="#FF8F1F" :size="18" />
			<view class="mc-deposit-alert__text">
				<text class="mc-deposit-alert__title">{{ businessUi.depositRequiredBanner }}</text>
				<text class="mc-deposit-alert__sub">{{ businessUi.depositRequiredTip }}</text>
			</view>
			<u-icon name="arrow-right" color="#FF8F1F" :size="14" />
		</view>

		<view v-if="isRestaurantMerchant && depositPaid" class="mc-panel ry-card mc-queue">
			<view class="mc-panel__head mc-queue__head">
				<view class="mc-panel__title-wrap">
					<view class="mc-panel__bar" />
					<text class="mc-panel__title">{{ ui.panelQueue }}</text>
				</view>
				<text class="mc-queue__status"
					:class="{ 'mc-queue__status--busy': queueEnabled && queueTableCount > 0 }">
					{{ queuePreviewText }}
				</text>
			</view>
			<view class="mc-queue__body">
				<view class="mc-queue__item">
					<text class="mc-queue__label">{{ ui.panelQueueEnabled }}</text>
					<u-switch v-model="queueEnabled" size="20" :active-color="theme.primary"
						inactive-color="#E8E8E8" :disabled="!isOpen" @change="onQueueToggleSave" />
				</view>
				<view v-if="queueEnabled" class="mc-queue__item">
					<text class="mc-queue__label">{{ ui.panelQueueCount }}</text>
					<view class="mc-queue__stepper">
						<view class="mc-queue__step-btn" :class="{ 'mc-queue__step-btn--disabled': queueTableCount <= 1 }"
							@click.stop="adjustQueueCount(-1)">
							<text>−</text>
						</view>
						<text class="mc-queue__step-value">{{ queueTableCount }}</text>
						<view class="mc-queue__step-btn" :class="{ 'mc-queue__step-btn--disabled': queueTableCount >= 99 }"
							@click.stop="adjustQueueCount(1)">
							<text>＋</text>
						</view>
						<text class="mc-queue__unit">{{ ui.panelQueueUnit }}</text>
					</view>
				</view>
			</view>
			<text class="mc-queue__hint">{{ ui.panelQueueHint }}</text>
			<text v-if="!isOpen" class="mc-queue__warn">{{ ui.panelQueueRequireOpen }}</text>
		</view>

		<view class="mc-panel ry-card">
			<view class="mc-panel__head">
				<view class="mc-panel__title-wrap">
					<view class="mc-panel__bar" />
					<text class="mc-panel__title">{{ ui.panelRevenue }}</text>
				</view>
				<picker mode="date" fields="month" :value="selectedMonth" :end="maxMonth" @change="onMonthChange">
					<view class="mc-panel__month">
						<u-icon name="calendar-fill" :color="theme.primary" :size="14" />
						<text class="mc-panel__month-text">{{ monthLabel }}</text>
						<u-icon name="arrow-down-fill" :color="theme.primary" :size="10" />
					</view>
				</picker>
			</view>
			<view class="mc-panel__stats mc-panel__stats--3">
				<view class="mc-panel__stat">
					<text
						class="mc-panel__value mc-panel__value--money">{{ formatRevenue(revenue.today, revenue.isMoney) }}</text>
					<text class="mc-panel__label">{{ ui.panelToday }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__stat">
					<text
						class="mc-panel__value mc-panel__value--money">{{ formatRevenue(revenue.month, revenue.isMoney) }}</text>
					<text class="mc-panel__label">{{ ui.panelMonth }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__stat">
					<text class="mc-panel__value mc-panel__value--money">{{ formatRevenue(revenue.total, revenue.isMoney) }}</text>
					<text class="mc-panel__label">{{ ui.panelTotal }}</text>
				</view>
			</view>
		</view>

		<view class="mc-panel ry-card">
			<view class="mc-panel__head">
				<view class="mc-panel__title-wrap">
					<view class="mc-panel__bar" />
					<text class="mc-panel__title">{{ ui.panelService }}</text>
				</view>
				<view v-if="storeType === 'hotel'" class="mc-panel__link" @click="goRoomStandards">
					<text>客房标准</text>
					<u-icon name="arrow-right" :color="theme.primary" :size="12" />
				</view>
			</view>
			<view class="mc-panel__stats mc-panel__stats--4">
				<view class="mc-panel__stat" @click="goServices">
					<text class="mc-panel__value">{{ dashboard.service.total }}</text>
					<text class="mc-panel__label">{{ ui.panelAll }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__stat" @click="goServices">
					<text class="mc-panel__value mc-panel__value--accent">{{ dashboard.service.onSale }}</text>
					<text class="mc-panel__label">{{ dashboard.service.onSaleLabel }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__action" @click="goPublish">
					<view class="mc-panel__action-icon mc-panel__action-icon--plus">
						<u-icon name="plus" :color="theme.primary" :size="22" />
					</view>
					<text class="mc-panel__label">{{ ui.panelPublishService }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__action" @click="goServices">
					<view class="mc-panel__action-icon">
						<u-icon name="grid-fill" :color="theme.primary" :size="18" />
					</view>
					<text class="mc-panel__label">{{ ui.panelManageService }}</text>
				</view>
			</view>
		</view>

		<view class="mc-panel ry-card">
			<view class="mc-panel__head">
				<view class="mc-panel__title-wrap">
					<view class="mc-panel__bar" />
					<text class="mc-panel__title">{{ ui.panelOrder }}</text>
				</view>
				<view class="mc-panel__link" @click="goOrders">
					<text>{{ ui.panelAllOrders }}</text>
					<u-icon name="arrow-right" :color="theme.primary" :size="12" />
				</view>
			</view>
			<view class="mc-panel__stats mc-panel__stats--4">
				<view class="mc-panel__stat" @click="goOrders">
					<text class="mc-panel__value">{{ dashboard.order.total }}</text>
					<text class="mc-panel__label">{{ ui.panelAll }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__stat" @click="goOrdersPending">
					<text class="mc-panel__value mc-panel__value--accent">{{ dashboard.order.pending }}</text>
					<text class="mc-panel__label">{{ ui.panelPending }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__stat" @click="goOrdersProcessing">
					<text class="mc-panel__value mc-panel__value--accent">{{ dashboard.order.processing }}</text>
					<text class="mc-panel__label">{{ ui.panelProcessing }}</text>
				</view>
				<view class="mc-panel__divider" />
				<view class="mc-panel__stat" @click="goOrdersCompleted">
					<text class="mc-panel__value mc-panel__value--accent">{{ dashboard.order.closed }}</text>
					<text class="mc-panel__label">{{ ui.panelClosed }}</text>
				</view>
			</view>
		</view>

		<view v-if="isCultureMerchant && pendingShipOrders.length" class="mc-panel ry-card">
			<view class="mc-panel__head">
				<view class="mc-panel__title-wrap">
					<view class="mc-panel__bar" />
					<text class="mc-panel__title">{{ ui.panelPendingShip }}</text>
					<view v-if="pendingShipCount > 0" class="mc-panel__badge">{{ pendingShipCount }}</view>
				</view>
				<view class="mc-panel__link" @click="goOrdersPending">
					<text>{{ ui.panelShipAll }}</text>
					<u-icon name="arrow-right" :color="theme.primary" :size="12" />
				</view>
			</view>
			<view class="mc-verify-list">
				<view v-for="item in pendingShipOrders" :key="item.id" class="mc-verify-item" @click="goOrderDetail(item)">
					<view class="mc-verify-item__main">
						<text class="mc-verify-item__title">{{ shipItemTitle(item) }}</text>
						<text class="mc-verify-item__sub">{{ shipItemSub(item) }}</text>
					</view>
					<view class="mc-verify-item__action" @click.stop="goShipOrder(item)">
						<u-icon name="car-fill" color="#FFFFFF" :size="18" />
						<text class="mc-verify-item__action-text">{{ businessUi.goShip }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="(isHotelMerchant || isTravelMerchant) && pendingVerifyOrders.length" class="mc-panel ry-card">
			<view class="mc-panel__head">
				<view class="mc-panel__title-wrap">
					<view class="mc-panel__bar" />
					<text class="mc-panel__title">{{ ui.panelVerify }}</text>
					<view v-if="pendingVerifyCount > 0" class="mc-panel__badge">{{ pendingVerifyCount }}</view>
				</view>
				<view class="mc-panel__link" @click="goVerify">
					<text>{{ businessUi.verifyAll }}</text>
					<u-icon name="arrow-right" :color="theme.primary" :size="12" />
				</view>
			</view>
			<view class="mc-verify-list">
				<view v-for="item in pendingVerifyOrders" :key="item.id" class="mc-verify-item" @click="goOrderDetail(item)">
					<view class="mc-verify-item__main">
						<text class="mc-verify-item__title">{{ verifyItemTitle(item) }}</text>
						<text class="mc-verify-item__sub">{{ verifyItemSub(item) }}</text>
					</view>
					<view class="mc-verify-item__action" @click.stop="goVerifyOrder(item)">
						<u-icon name="scan" color="#FFFFFF" :size="18" />
						<text class="mc-verify-item__action-text">{{ businessUi.confirmVerify }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="(isHotelMerchant || isTravelMerchant) && depositPaid" class="mc-verify-fab" :style="{ background: theme.primary }"
			@click="goVerify">
			<u-icon name="scan" color="#FFFFFF" :size="30" />
			<text class="mc-verify-fab__text">{{ businessUi.confirmVerify }}</text>
		</view>
	</view>
	<view v-else-if="loading" class="mc-loading" :style="pageStyle">
		<u-loading-icon mode="circle" :color="theme.primary" />
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
		MERCHANT_CENTER_UI,
		MERCHANT_BUSINESS_UI,
		enrichMerchantProfile,
		isMerchantDepositPaid,
		requireMerchantApproved
	} from '@/common/merchant-center.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';
	import {
		cacheMerchantStoreType
	} from '@/common/merchant-theme.js';
	import {
		fetchMerchantDashboard,
		fetchMessageUnreadCount,
		updateMerchantProfile,
		updateMerchantQueueStatus
	} from '@/common/api/merchant.js';
	import {
		getMerchantBizPath
	} from '@/common/merchant-biz-routes.js';

	const {
		theme,
		pageStyle,
		centerUi,
		storeType,
		applyStoreType,
		initTheme,
		applyNavBarColor
	} = useMerchantTheme();
	const ui = centerUi;
	const businessUi = MERCHANT_BUSINESS_UI;
	const summary = ref(null);
	const dashboard = ref(null);
	const revenue = ref({
		today: 0,
		month: 0,
		total: 0,
		isMoney: true
	});
	const pendingVerifyOrders = ref([]);
	const pendingVerifyCount = ref(0);
	const pendingShipOrders = ref([]);
	const pendingShipCount = ref(0);
	const isOpen = ref(true);
	const loading = ref(false);
	const dashboardRaw = ref(null);
	const depositPaid = ref(false);

	function getCurrentYearMonth() {
		const now = new Date();
		return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
	}

	const selectedMonth = ref(getCurrentYearMonth());
	const maxMonth = ref(getCurrentYearMonth());

	const monthLabel = computed(() => {
		const [y, m] = selectedMonth.value.split('-');
		return `${y}年${m}月`;
	});

	const isHotelMerchant = computed(() => storeType.value === 'hotel');
	const isTravelMerchant = computed(() => storeType.value === 'travel');
	const isCultureMerchant = computed(() => storeType.value === 'culture');
	const isRestaurantMerchant = computed(() => {
		const type = storeType.value;
		return type === 'restaurant' || type === 'goose';
	});
	const queueEnabled = ref(false);
	const queueTableCount = ref(1);
	const queueSubmitting = ref(false);
	let queueSaveTimer = null;
	let queueSavePromise = null;

	const queuePreviewText = computed(() => {
		if (!isOpen.value) {
			return summary.value?.openStatusLabel || MERCHANT_CENTER_UI.statusClosed;
		}
		if (!queueEnabled.value || queueTableCount.value <= 0) {
			return MERCHANT_CENTER_UI.panelQueueNoQueue;
		}
		return `前面约 ${queueTableCount.value} 桌`;
	});

	function syncQueueFromMerchant(merchant) {
		if (!merchant) return;
		queueEnabled.value = !!merchant.queueEnabled;
		const count = Number(merchant.queueTableCount ?? 0);
		queueTableCount.value = queueEnabled.value ? Math.max(1, count) : Math.max(1, count || 1);
	}

	function mapVerifyOrder(record) {
		return {
			...record,
			amount: Number(record.amount ?? 0)
		};
	}

	function mapShipOrder(record) {
		return {
			...record,
			amount: Number(record.amount ?? 0)
		};
	}

	function shipItemTitle(item) {
		return item.items?.[0]?.name || item.title || '待发货订单';
	}

	function shipItemSub(item) {
		const parts = [];
		const receiver = item.receiverName || item.customerName || '';
		if (receiver) parts.push(receiver);
		const phone = item.receiverPhone || item.customerPhone || '';
		if (phone) parts.push(phone);
		if (item.address) parts.push(item.address);
		return parts.join(' · ') || item.statusLabel || '';
	}

	function verifyItemTitle(item) {
		return item.items?.[0]?.name || item.title || '待核销订单';
	}

	function verifyItemSub(item) {
		const name = item.customerName || item.guestNames || '';
		if (isTravelMerchant.value) {
			const parts = [];
			if (item.tourDate) parts.push(`${item.tourDate} 出行`);
			if (item.guestCount) parts.push(`${item.guestCount}人`);
			if (name) parts.push(name);
			return parts.join(' · ') || item.statusLabel || '';
		}
		return `${item.checkIn || ''} 入住 · ${name}`;
	}

	function applyRevenueByMonth() {
		const base = dashboardRaw.value?.revenue;
		if (!base) return;
		const isCurrent = selectedMonth.value === getCurrentYearMonth();
		revenue.value = {
			today: isCurrent ? Number(base.today ?? 0) : 0,
			month: isCurrent ? Number(base.month ?? 0) : 0,
			total: Number(base.total ?? 0),
			isMoney: base.isMoney !== false
		};
	}

	function mapDashboard(data, messageCount = 0) {
		const merchant = enrichMerchantProfile(data.merchant);
		if (merchant?.storeType) {
			applyStoreType(merchant.storeType);
			cacheMerchantStoreType(merchant.storeType);
		}
		const paid = isMerchantDepositPaid(data.wallet) || isMerchantDepositPaid(merchant);
		return {
			summary: {
				merchant,
				isOpen: paid && merchant?.isOpen !== false,
				openStatusLabel: merchant?.statusLabel ||
					(paid ?
						(merchant?.isOpen !== false ? MERCHANT_CENTER_UI.statusOpen : MERCHANT_CENTER_UI.statusClosed) :
						MERCHANT_BUSINESS_UI.statusNotOperating),
				messageCount,
				depositPaid: paid
			},
			dashboard: {
				service: {
					total: Number(data.service?.total ?? 0),
					onSale: Number(data.service?.onSale ?? 0),
					onSaleLabel: data.service?.onSaleLabel || centerUi.value.panelOnSale
				},
				order: {
					total: Number(data.order?.total ?? 0),
					pending: Number(data.order?.pending ?? 0),
					processing: Number(data.order?.processing ?? 0),
					closed: Number(data.order?.closed ?? 0)
				}
			}
		};
	}

	async function reload() {
		loading.value = true;
		try {
			const [dashRes, msgRes] = await Promise.all([
				fetchMerchantDashboard(),
				fetchMessageUnreadCount().catch(() => null)
			]);
			if (dashRes.code !== 0 || !dashRes.data) {
				uni.showToast({
					title: dashRes.msg || '加载失败',
					icon: 'none'
				});
				return;
			}
			dashboardRaw.value = dashRes.data;
			const messageCount = msgRes?.code === 0 ? Number(msgRes.data?.count ?? 0) : 0;
			const mapped = mapDashboard(dashRes.data, messageCount);
			summary.value = mapped.summary;
			dashboard.value = mapped.dashboard;
			pendingVerifyOrders.value = (dashRes.data.pendingVerifyOrders || []).map(mapVerifyOrder);
			pendingVerifyCount.value = Number(dashRes.data.order?.pendingVerify ?? pendingVerifyOrders.value.length);
			pendingShipOrders.value = (dashRes.data.pendingShipOrders || []).map(mapShipOrder);
			pendingShipCount.value = Number(dashRes.data.order?.pendingShip ?? pendingShipOrders.value.length);
			isOpen.value = mapped.summary.isOpen;
			depositPaid.value = mapped.summary.depositPaid;
			if (isRestaurantMerchant.value) {
				syncQueueFromMerchant(mapped.summary.merchant || dashRes.data.merchant || {});
			}
			maxMonth.value = getCurrentYearMonth();
			applyRevenueByMonth();
		} catch (e) {
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function formatRevenue(value, isMoney) {
		const num = Number(value) || 0;
		if (isMoney) return `${MERCHANT_CENTER_UI.currencySymbol}${num.toFixed(1)}`;
		return String(num);
	}

	function onMonthChange(e) {
		const val = e.detail?.value || selectedMonth.value;
		selectedMonth.value = val.slice(0, 7);
		applyRevenueByMonth();
	}

	function goPage(path) {
		uni.navigateTo({
			url: path
		});
	}

	function goInfo() {
		goPage('/merchant-portal/center/info');
	}

	function goMessages() {
		goPage('/pages/msg/msg');
	}

	function goBiz(page, query) {
		goPage(getMerchantBizPath(storeType.value, page, query));
	}

	function goStoreDecor() {
		goBiz('store-decor');
	}

	function guardDepositAction() {
		if (depositPaid.value) return true;
		uni.showToast({
			title: MERCHANT_BUSINESS_UI.depositRequiredOpenTip,
			icon: 'none'
		});
		goDeposit();
		return false;
	}

	function toggleOpenFromAction() {
		if (!depositPaid.value) {
			guardDepositAction();
			return;
		}
		const next = !isOpen.value;
		isOpen.value = next;
		onToggleOpen(next);
	}

	async function onToggleOpen(value) {
		if (!depositPaid.value) {
			isOpen.value = false;
			guardDepositAction();
			return;
		}
		try {
			const res = await updateMerchantProfile({
				isOpen: !!value
			});
			if (res.code !== 0) {
				isOpen.value = !value;
				uni.showToast({
					title: res.msg || '更新失败',
					icon: 'none'
				});
				return;
			}
			if (summary.value) {
				summary.value.isOpen = !!value;
				summary.value.openStatusLabel = value ? MERCHANT_CENTER_UI.statusOpen : MERCHANT_CENTER_UI.statusClosed;
			}
			if (!value && isRestaurantMerchant.value) {
				queueEnabled.value = false;
				queueTableCount.value = 1;
				await saveQueueStatus({ silent: true });
			}
			uni.showToast({
				title: value ? MERCHANT_CENTER_UI.statusOpen : MERCHANT_CENTER_UI.statusClosed,
				icon: 'none'
			});
		} catch (e) {
			isOpen.value = !value;
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		}
	}

	async function onQueueToggleSave(value) {
		const previousEnabled = !value;
		if (!isOpen.value) {
			queueEnabled.value = false;
			uni.showToast({
				title: MERCHANT_CENTER_UI.panelQueueRequireOpen,
				icon: 'none'
			});
			return;
		}
		if (value && queueTableCount.value < 1) {
			queueTableCount.value = 1;
		}
		const ok = await saveQueueStatus({ silent: true });
		if (!ok) {
			queueEnabled.value = previousEnabled;
		}
	}

	function adjustQueueCount(delta) {
		if (!queueEnabled.value) return;
		const next = queueTableCount.value + delta;
		if (next < 1 || next > 99) return;
		queueTableCount.value = next;
		scheduleQueueSave({ silent: true });
	}

	function patchQueueMerchantState(merchant) {
		if (!merchant) return;
		const enabled = !!merchant.queueEnabled;
		const count = Number(merchant.queueTableCount ?? 0);
		const displayText = merchant.queueDisplayText || '';
		if (summary.value?.merchant) {
			summary.value.merchant.queueEnabled = enabled;
			summary.value.merchant.queueTableCount = count;
			summary.value.merchant.queueDisplayText = displayText;
		}
		if (dashboardRaw.value?.merchant) {
			dashboardRaw.value.merchant.queueEnabled = enabled;
			dashboardRaw.value.merchant.queueTableCount = count;
			dashboardRaw.value.merchant.queueDisplayText = displayText;
		}
	}

	function clearQueueSaveTimer() {
		if (!queueSaveTimer) return;
		clearTimeout(queueSaveTimer);
		queueSaveTimer = null;
	}

	function scheduleQueueSave(options = {}) {
		clearQueueSaveTimer();
		queueSaveTimer = setTimeout(() => {
			queueSaveTimer = null;
			saveQueueStatus(options);
		}, 400);
	}

	async function saveQueueStatus({ silent = false } = {}) {
		clearQueueSaveTimer();
		if (queueSubmitting.value && queueSavePromise) {
			await queueSavePromise;
		}
		if (!guardDepositAction()) return false;
		if (!isOpen.value) {
			if (!silent) {
				uni.showToast({
					title: MERCHANT_CENTER_UI.panelQueueRequireOpen,
					icon: 'none'
				});
			}
			return false;
		}
		if (queueEnabled.value && (queueTableCount.value < 1 || queueTableCount.value > 99)) {
			if (!silent) {
				uni.showToast({
					title: '排队桌数需在 1-99 之间',
					icon: 'none'
				});
			}
			return false;
		}
		if (queueSubmitting.value) {
			return queueSavePromise || false;
		}
		const snapshotEnabled = !!queueEnabled.value;
		const snapshotCount = queueEnabled.value ? queueTableCount.value : 0;
		queueSubmitting.value = true;
		queueSavePromise = (async () => {
			try {
				const res = await updateMerchantQueueStatus({
					queueEnabled: snapshotEnabled,
					queueTableCount: snapshotCount
				});
				if (res.code !== 0) {
					if (!silent) {
						uni.showToast({
							title: res.msg || '更新失败',
							icon: 'none'
						});
					}
					return false;
				}
				patchQueueMerchantState(res.data || {});
				if (!silent) {
					uni.showToast({
						title: MERCHANT_CENTER_UI.panelQueueSaveSuccess,
						icon: 'none'
					});
				}
				return true;
			} catch (e) {
				if (!silent) {
					uni.showToast({
						title: '网络异常，请稍后重试',
						icon: 'none'
					});
				}
				return false;
			} finally {
				queueSubmitting.value = false;
				queueSavePromise = null;
			}
		})();
		return queueSavePromise;
	}

	function goDeposit() {
		goPage('/merchant-portal/center/wallet-deposit');
	}

	function goWallet() {
		goPage('/merchant-portal/center/wallet');
	}

	function goOrders() {
		if (!guardDepositAction()) return;
		goBiz('orders');
	}

	function goOrdersPending() {
		if (!guardDepositAction()) return;
		if (isTravelMerchant.value) {
			goBiz('orders', { tab: 'verify' });
			return;
		}
		if (isCultureMerchant.value) {
			goBiz('orders', { tab: 'pending' });
			return;
		}
		goBiz('orders', { tab: 'pending' });
	}

	function goOrdersProcessing() {
		if (!guardDepositAction()) return;
		goBiz('orders', {
			tab: 'processing'
		});
	}

	function goOrdersCompleted() {
		if (!guardDepositAction()) return;
		goBiz('orders', {
			tab: 'completed'
		});
	}

	function goServices() {
		if (!guardDepositAction()) return;
		goBiz('services');
	}

	function goPublish() {
		if (!guardDepositAction()) return;
		goBiz('service-edit');
	}

	function goRoomStandards() {
		if (!guardDepositAction()) return;
		goBiz('room-standards');
	}

	function goVerify() {
		if (!guardDepositAction()) return;
		goPage('/merchant-portal/center/verify');
	}

	function goOrderDetail(item) {
		if (!item?.id) return;
		goBiz('order-detail', {
			id: item.id
		});
	}

	function goVerifyOrder(item) {
		const orderNo = item.orderNo || item.id;
		if (!orderNo) return;
		goPage(`/merchant-portal/center/verify?orderNo=${encodeURIComponent(orderNo)}`);
	}

	function goShipOrder(item) {
		if (!item?.id) return;
		goBiz('order-detail', {
			id: item.id,
			ship: '1'
		});
	}

	onLoad(async () => {
		await initTheme();
		uni.setNavigationBarTitle({
			title: MERCHANT_CENTER_UI.centerTitle
		});
		applyNavBarColor();
	});

	onShow(async () => {
		if (!(await requireMerchantApproved())) return;
		await initTheme();
		applyNavBarColor();
		await reload();
	});
</script>

<style lang="scss" scoped>
	.mc-page {
		min-height: 100vh;
		background: var(--ry-page-bg);
		padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;

		&--verify {
			padding-bottom: calc(160rpx + env(safe-area-inset-bottom));
		}
	}

	.mc-deposit-alert {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 24rpx 28rpx;
		background: #FFF7ED;
		border: 1rpx solid #FFE4C7;

		&__text {
			flex: 1;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 600;
			color: #D46B08;
		}

		&__sub {
			font-size: 24rpx;
			color: #AD6800;
		}
	}

	.mc-loading {
		min-height: 100vh;
		display: flex;
		align-items: center;
		justify-content: center;
		background: var(--ry-page-bg);
	}

	.mc-hero {
		position: relative;
		overflow: hidden;
		border-radius: var(--ry-radius-lg);
		background: var(--mc-hero-gradient, linear-gradient(135deg, #2979FF 0%, #5B9FFF 100%));

		&__top {
			padding: 32rpx 28rpx 24rpx;

			&:active {
				opacity: 0.96;
			}
		}

		&__circle {
			position: absolute;
			border-radius: 50%;
			background: rgba(255, 255, 255, 0.12);
			pointer-events: none;
		}

		&__circle--1 {
			width: 180rpx;
			height: 180rpx;
			top: -50rpx;
			right: -30rpx;
		}

		&__circle--2 {
			width: 100rpx;
			height: 100rpx;
			bottom: -20rpx;
			left: -20rpx;
		}

		&__main {
			position: relative;
			z-index: 1;
			display: flex;
			flex-direction: column;
			gap: 12rpx;
			margin-bottom: 24rpx;
		}

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__badge {
			display: flex;
			align-items: center;
			gap: 6rpx;
			padding: 6rpx 16rpx;
			border-radius: var(--ry-radius-pill);
			font-size: 22rpx;
			font-weight: 600;
			background: rgba(255, 255, 255, 0.9);
		}

		&__link {
			display: flex;
			align-items: center;
			gap: 4rpx;
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.85);
		}

		&__name {
			font-size: 36rpx;
			font-weight: 700;
			color: #FFF;
			line-height: 1.3;
		}

		&__status-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			padding: 12rpx 20rpx;
			border-radius: 12rpx;
			background: rgba(255, 255, 255, 0.15);
		}

		&__status-left {
			display: flex;
			align-items: center;
			gap: 8rpx;
			min-width: 0;
		}

		&__status-dot {
			width: 12rpx;
			height: 12rpx;
			border-radius: 50%;
			background: #2EAF6A;
			box-shadow: 0 0 8rpx rgba(46, 175, 106, 0.6);

			&--closed {
				background: #FFB800;
				box-shadow: 0 0 8rpx rgba(255, 184, 0, 0.5);
			}
		}

		&__status {
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.88);
		}

		&__actions {
			position: relative;
			z-index: 1;
			display: flex;
			gap: 10rpx;
			padding-top: 20rpx;
			border-top: 1rpx solid rgba(255, 255, 255, 0.18);
		}

		&__action {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 6rpx;
			padding: 14rpx 4rpx;
			border-radius: 16rpx;
			background: rgba(255, 255, 255, 0.12);

			&:active {
				background: rgba(255, 255, 255, 0.2);
			}

			&--disabled {
				opacity: 0.55;

				&:active {
					background: rgba(255, 255, 255, 0.12);
				}
			}
		}

		&__action-icon {
			position: relative;
			width: 40rpx;
			height: 40rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__action-badge {
			position: absolute;
			top: -8rpx;
			right: -12rpx;
			min-width: 28rpx;
			height: 28rpx;
			padding: 0 6rpx;
			border-radius: 999rpx;
			background: #FF4D4F;
			display: flex;
			align-items: center;
			justify-content: center;

			text {
				font-size: 18rpx;
				color: #FFF;
				line-height: 1;
			}
		}

		&__action-label {
			font-size: 20rpx;
			color: rgba(255, 255, 255, 0.92);
			text-align: center;
			line-height: 1.2;
		}
	}

	.mc-panel {
		padding: 24rpx 28rpx;

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			margin-bottom: 24rpx;
		}

		&__title-wrap {
			display: flex;
			align-items: center;
			gap: 12rpx;
			min-width: 0;
		}

		&__bar {
			width: 6rpx;
			height: 28rpx;
			border-radius: 3rpx;
			background: var(--mc-primary, #2979FF);
			flex-shrink: 0;
		}

		&__title {
			font-size: 30rpx;
			font-weight: 700;
			color: var(--ry-text-primary);
			line-height: 1;
		}

		&__month {
			display: flex;
			align-items: center;
			gap: 6rpx;
			flex-shrink: 0;
			padding: 8rpx 16rpx;
			border-radius: 999rpx;
			background: var(--mc-primary-light, #EAF2FF);

			&:active {
				opacity: 0.85;
			}
		}

		&__month-text {
			font-size: 24rpx;
			font-weight: 400;
			color: var(--mc-primary, #2979FF);
			line-height: 1;
		}

		&__link {
			display: flex;
			align-items: center;
			gap: 4rpx;
			flex-shrink: 0;
			font-size: 24rpx;
			color: var(--mc-primary, #2979FF);

			&:active {
				opacity: 0.75;
			}
		}

		&__stats {
			display: flex;
			align-items: stretch;

			&--3 .mc-panel__stat {
				flex: 1;
			}

			&--4 .mc-panel__stat,
			&--4 .mc-panel__action {
				flex: 1;
			}
		}

		&__stat {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 10rpx;
			padding: 8rpx 4rpx;
			min-width: 0;

			&:active {
				opacity: 0.75;
			}
		}

		&__value {
			font-size: 36rpx;
			font-weight: 800;
			color: #1A1A1A;
			line-height: 1;

			&--accent {
				color: var(--mc-primary, #2979FF);
			}

			&--money {
				font-size: 30rpx;
				font-weight: 700;
				color: var(--mc-primary, #2979FF);
			}
		}

		&__label {
			font-size: 22rpx;
			color: var(--ry-text-hint);
			text-align: center;
			line-height: 1.3;
		}

		&__divider {
			width: 1rpx;
			align-self: stretch;
			margin: 8rpx 0;
			background: #F0F0F0;
			flex-shrink: 0;
		}

		&__action {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 10rpx;
			padding: 8rpx 4rpx;
			min-width: 0;

			&:active {
				opacity: 0.75;
			}
		}

		&__action-icon {
			width: 56rpx;
			height: 56rpx;
			border-radius: 14rpx;
			background: var(--mc-primary-light, #EAF2FF);
			display: flex;
			align-items: center;
			justify-content: center;

			&--plus {
				background: var(--mc-primary-light, #F0F6FF);
			}
		}

		&__badge {
			min-width: 32rpx;
			height: 32rpx;
			padding: 0 10rpx;
			border-radius: 999rpx;
			background: #FF4D4F;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 20rpx;
			color: #fff;
			font-weight: 600;
		}
	}

	.mc-queue {
		&__head {
			align-items: center;
		}

		&__status {
			flex-shrink: 0;
			font-size: 24rpx;
			font-weight: 600;
			color: #2EAF6A;

			&--busy {
				color: #FF7A00;
			}
		}

		&__body {
			display: flex;
			flex-direction: column;
			border-top: 1rpx solid #F3F4F6;
		}

		&__item {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 24rpx;
			min-height: 96rpx;
			padding: 8rpx 0;
			border-bottom: 1rpx solid #F3F4F6;

			&:last-child {
				border-bottom: none;
			}
		}

		&__label {
			font-size: 28rpx;
			color: #333333;
		}

		&__stepper {
			display: flex;
			align-items: center;
			gap: 12rpx;
		}

		&__step-btn {
			width: 52rpx;
			height: 52rpx;
			border-radius: 12rpx;
			background: #F5F6F8;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 28rpx;
			color: #333333;

			&:active {
				opacity: 0.75;
			}

			&--disabled {
				opacity: 0.35;
			}
		}

		&__step-value {
			min-width: 40rpx;
			text-align: center;
			font-size: 32rpx;
			font-weight: 700;
			color: #1A1A1A;
		}

		&__unit {
			font-size: 24rpx;
			color: var(--ry-text-hint);
		}

		&__hint,
		&__warn {
			display: block;
			margin-top: 12rpx;
			font-size: 22rpx;
			line-height: 1.5;
			color: var(--ry-text-hint);
		}

		&__warn {
			color: #FF7A00;
		}
	}

	.mc-verify-list {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.mc-verify-item {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 20rpx;
		border-radius: 16rpx;
		background: #FAFAFA;

		&:active {
			opacity: 0.85;
		}

		&__main {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 6rpx;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 600;
			color: #1A1A1A;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__sub {
			font-size: 22rpx;
			color: var(--ry-text-hint);
		}

		&__phone {
			display: flex;
			align-items: center;
			gap: 8rpx;
			margin-top: 4rpx;
		}

		&__phone-text {
			font-size: 26rpx;
			font-weight: 700;
			color: var(--mc-primary, #2979FF);
			line-height: 1.3;
		}

		&__action {
			flex-shrink: 0;
			min-width: 72rpx;
			padding: 12rpx 16rpx;
			border-radius: 16rpx;
			background: var(--mc-primary, #2979FF);
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 4rpx;
		}

		&__action-text {
			font-size: 22rpx;
			color: #fff;
			line-height: 1.2;
		}

		&__action--confirm {
			min-width: 108rpx;
		}
	}

	.mc-verify-fab {
		position: fixed;
		right: 32rpx;
		bottom: calc(120rpx + env(safe-area-inset-bottom));
		width: 112rpx;
		height: 112rpx;
		border-radius: 50%;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		box-shadow: 0 12rpx 32rpx rgba(41, 121, 255, 0.35);
		z-index: 120;

		&:active {
			transform: scale(0.96);
			opacity: 0.92;
		}

		&__text {
			font-size: 22rpx;
			color: #fff;
			line-height: 1.2;
		}
	}
</style>
