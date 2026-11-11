const fs = require('fs');
const path = require('path');

const target = path.join(__dirname, '../mobile-design/canteen/detail.vue');

const content = `<template>
	<view class="canteen-detail-page">
		<view class="canteen-detail-body">
			<view v-if="!record.id" class="canteen-detail-empty ry-card">
				<view class="canteen-detail-empty__icon">
					<u-icon name="file-text" :color="accentColor" :size="48" />
				</view>
				<text class="canteen-detail-empty__title">预约不存在</text>
				<text class="canteen-detail-empty__desc">该预约可能已被删除或链接无效</text>
				<view class="canteen-detail-empty__btn" @click="goRecords">
					<text class="canteen-detail-empty__btn-text">查看预约记录</text>
				</view>
			</view>

			<template v-else>
				<view class="detail-ticket ry-card">
					<view class="detail-ticket__accent" :class="statusAccentClass" />

					<view class="detail-ticket__head">
						<view class="detail-ticket__avatar">
							<text class="detail-ticket__avatar-text">{{ avatarText }}</text>
						</view>
						<view class="detail-ticket__head-info">
							<text class="detail-ticket__name">{{ record.contactName }}</text>
							<text class="detail-ticket__sub">{{ record.guestCount }} 人用餐 · 餐饮预约</text>
						</view>
						<view class="detail-ticket__badge" :class="\`detail-ticket__badge--\${record.status}\`">
							<text class="detail-ticket__badge-text">{{ record.statusLabel }}</text>
						</view>
					</view>

					<view class="detail-ticket__time">
						<view class="detail-ticket__time-main">
							<u-icon name="clock-fill" :color="accentColor" :size="18" />
							<view class="detail-ticket__time-text">
								<text class="detail-ticket__date">{{ reserveDate }}</text>
								<text class="detail-ticket__clock">{{ reserveClock }}</text>
							</view>
						</view>
						<text class="detail-ticket__time-tip">{{ statusTip }}</text>
					</view>

					<view class="detail-ticket__tear">
						<view v-for="n in 12" :key="n" class="detail-ticket__tear-dot" />
					</view>

					<view class="detail-ticket__body">
						<view class="detail-ticket__row" @click="onCall">
							<text class="detail-ticket__label">联系电话</text>
							<view class="detail-ticket__value-wrap">
								<text class="detail-ticket__value detail-ticket__value--link">{{ record.phone }}</text>
								<u-icon name="phone-fill" :color="accentColor" :size="14" />
							</view>
						</view>
						<view class="detail-ticket__row">
							<text class="detail-ticket__label">用餐人数</text>
							<text class="detail-ticket__value">{{ record.guestCount }} 人</text>
						</view>
						<view class="detail-ticket__row detail-ticket__row--addr">
							<text class="detail-ticket__label">用餐地址</text>
							<text class="detail-ticket__value">{{ record.address }}</text>
						</view>
						<view class="detail-ticket__row detail-ticket__row--addr">
							<text class="detail-ticket__label">预约编号</text>
							<text class="detail-ticket__value detail-ticket__value--muted">{{ record.id }}</text>
						</view>
					</view>
				</view>

				<view class="detail-notice">
					<view class="detail-notice__head">
						<u-icon name="info-circle-fill" :color="accentColor" :size="16" />
						<text class="detail-notice__title">用餐须知</text>
					</view>
					<view class="detail-notice__list">
						<view v-for="(item, index) in noticeList" :key="index" class="detail-notice__item">
							<view class="detail-notice__bullet">
								<text class="detail-notice__bullet-text">{{ index + 1 }}</text>
							</view>
							<text class="detail-notice__text">{{ item }}</text>
						</view>
					</view>
				</view>

				<text class="detail-footer">荣昌非遗美食 · 地道卤味</text>
			</template>
		</view>

		<view v-if="record.id && record.status === 'pending'" class="detail-bar">
			<view class="detail-bar__btn detail-bar__btn--ghost" @click="onCancel">
				<text class="detail-bar__btn-text detail-bar__btn-text--ghost">取消预约</text>
			</view>
			<view class="detail-bar__btn detail-bar__btn--primary" @click="onModify">
				<text class="detail-bar__btn-text">修改预约</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import {
	getCanteenRecordById,
	getCanteenRecords,
	saveCanteenRecords
} from '@/common/canteen-mock.js';

const accentColor = getModuleColor('goose');
const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';
const CANTEEN_RECORDS_PATH = '/mobile-design/canteen/records';

const recordId = ref('');
const record = ref({});

const avatarText = computed(() => {
	const name = record.value.contactName || '';
	return name ? name.slice(0, 1) : '?';
});

const reserveDate = computed(() => {
	const time = record.value.reserveTime || '';
	const parts = time.split(' ');
	return parts[0] || time;
});

const reserveClock = computed(() => {
	const time = record.value.reserveTime || '';
	const parts = time.split(' ');
	return parts[1] || '';
});

const statusAccentClass = computed(() => {
	if (record.value.status === 'completed') return 'detail-ticket__accent--completed';
	if (record.value.status === 'cancelled') return 'detail-ticket__accent--cancelled';
	return '';
});

const statusTip = computed(() => {
	const tips = {
		pending: '请按时到店，向工作人员出示预约信息即可用餐',
		completed: '本次用餐已完成，感谢您的光临',
		cancelled: '预约已取消，欢迎再次预约'
	};
	return tips[record.value.status] || '请留意预约时间，按时到店用餐';
});

const noticeList = [
	'请提前 10 分钟到达用餐地点，超时 15 分钟预约可能失效',
	'如需变更时间或人数，可使用「修改预约」或提前取消后重新预约',
	'到店后向工作人员报联系人姓名及预约编号即可入座'
];

const loadRecord = () => {
	if (!recordId.value) {
		record.value = {};
		return;
	}
	const found = getCanteenRecordById(recordId.value);
	record.value = found ? { ...found } : {};
};

const navigateSafe = (url) => uni.navigateTo({ url });

const goRecords = () => navigateSafe(CANTEEN_RECORDS_PATH);

const onCall = () => {
	const phone = record.value.phone;
	if (!phone) return;
	uni.makePhoneCall({ phoneNumber: String(phone) });
};

const onCancel = () => {
	uni.showModal({
		title: '取消预约',
		content: \`确定取消 \${record.value.contactName} 的餐饮预约吗？\`,
		confirmColor: accentColor,
		success: (res) => {
			if (!res.confirm) return;
			const list = getCanteenRecords();
			const target = list.find(item => String(item.id) === String(recordId.value));
			if (target) {
				target.status = 'cancelled';
				target.statusLabel = '已取消';
				saveCanteenRecords(list);
				loadRecord();
			}
			uni.showToast({ title: '已取消', icon: 'success' });
		}
	});
};

const onModify = () => {
	navigateSafe(\`\${CANTEEN_RESERVE_PATH}?mode=edit&id=\${encodeURIComponent(recordId.value)}\`);
};

onLoad((options) => {
	recordId.value = String(options?.id || '');
	loadRecord();
	uni.setNavigationBarTitle({ title: '预约详情' });
});

onShow(() => {
	if (recordId.value) loadRecord();
});
</script>

<style lang="scss" scoped>
	.canteen-detail-page {
		min-height: 100vh;
		background-color: #FFF9F2;
	}

	.canteen-detail-body {
		padding: 20rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.canteen-detail-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 80rpx 40rpx;

		&__icon {
			width: 120rpx;
			height: 120rpx;
			border-radius: 50%;
			background-color: #FFF3EB;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__title {
			margin-top: 24rpx;
			font-size: 30rpx;
			font-weight: 700;
			color: #333333;
			line-height: 1.4;
		}

		&__desc {
			margin-top: 8rpx;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.5;
			text-align: center;
		}

		&__btn {
			margin-top: 32rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 72rpx;
			padding: 0 48rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.28);

			&:active {
				opacity: 0.9;
			}
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 700;
			line-height: 1;
			color: #FFFFFF;
		}
	}

	.detail-ticket {
		position: relative;
		padding: 0 0 8rpx;
		overflow: hidden;

		&__accent {
			height: 8rpx;
			background: linear-gradient(90deg, #FF9A40, #FF7A00);

			&--completed {
				background: linear-gradient(90deg, #5FD68A, #2EAF6A);
			}

			&--cancelled {
				background: linear-gradient(90deg, #CCCCCC, #999999);
			}
		}

		&__head {
			display: flex;
			align-items: center;
			gap: 16rpx;
			padding: 28rpx 28rpx 24rpx;
		}

		&__avatar {
			flex-shrink: 0;
			width: 80rpx;
			height: 80rpx;
			border-radius: 50%;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__avatar-text {
			font-size: 32rpx;
			font-weight: 700;
			line-height: 1;
			color: #FFFFFF;
		}

		&__head-info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__name {
			font-size: 32rpx;
			font-weight: 700;
			color: #333333;
			line-height: 1.3;
		}

		&__sub {
			font-size: 22rpx;
			color: #999999;
			line-height: 1.4;
		}

		&__badge {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 40rpx;
			padding: 0 16rpx;
			border-radius: var(--ry-radius-pill);

			&--pending {
				background-color: #FFF3EB;
			}

			&--completed {
				background-color: rgba(46, 175, 106, 0.12);
			}

			&--cancelled {
				background-color: #F5F5F5;
			}
		}

		&__badge-text {
			font-size: 22rpx;
			line-height: 1;
			font-weight: 600;
		}

		&__badge--pending &__badge-text {
			color: #FF7A00;
		}

		&__badge--completed &__badge-text {
			color: #2EAF6A;
		}

		&__badge--cancelled &__badge-text {
			color: #999999;
		}

		&__time {
			margin: 0 28rpx;
			padding: 24rpx;
			border-radius: var(--ry-radius-lg);
			background-color: #FFF9F2;
		}

		&__time-main {
			display: flex;
			align-items: center;
			gap: 16rpx;
		}

		&__time-text {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: baseline;
			flex-wrap: wrap;
			gap: 12rpx;
		}

		&__date {
			font-size: 34rpx;
			font-weight: 800;
			color: #333333;
			line-height: 1.3;
		}

		&__clock {
			font-size: 30rpx;
			font-weight: 700;
			color: #FF7A00;
			line-height: 1.3;
		}

		&__time-tip {
			margin-top: 14rpx;
			padding-top: 14rpx;
			font-size: 22rpx;
			color: #999999;
			line-height: 1.5;
			border-top: 1rpx dashed #FFE8D6;
		}

		&__tear {
			display: flex;
			align-items: center;
			justify-content: space-between;
			padding: 16rpx 20rpx;
			overflow: hidden;
		}

		&__tear-dot {
			flex-shrink: 0;
			width: 12rpx;
			height: 12rpx;
			border-radius: 50%;
			background-color: #FFF9F2;
			box-shadow: 0 0 0 2rpx #FFE8D6;
		}

		&__body {
			padding: 4rpx 28rpx 24rpx;
		}

		&__row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 24rpx;
			padding: 16rpx 0;

			&--addr {
				align-items: flex-start;
			}
		}

		&__label {
			flex-shrink: 0;
			font-size: 24rpx;
			color: #999999;
			line-height: 1.4;
		}

		&__value-wrap {
			flex: 1;
			min-width: 0;
			display: flex;
			align-items: center;
			justify-content: flex-end;
			gap: 8rpx;
		}

		&__value {
			flex: 1;
			min-width: 0;
			font-size: 26rpx;
			font-weight: 600;
			color: #333333;
			line-height: 1.5;
			text-align: right;

			&--link {
				color: #FF7A00;
			}

			&--muted {
				font-weight: 500;
				color: #BBBBBB;
				font-size: 24rpx;
			}
		}

		&__row--addr &__value {
			text-align: right;
		}
	}

	.detail-notice {
		padding: 24rpx 28rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 16rpx rgba(255, 122, 0, 0.06);

		&__head {
			display: flex;
			align-items: center;
			gap: 8rpx;
			margin-bottom: 16rpx;
		}

		&__title {
			font-size: 26rpx;
			font-weight: 700;
			color: #FF7A00;
			line-height: 1;
		}

		&__list {
			display: flex;
			flex-direction: column;
			gap: 14rpx;
		}

		&__item {
			display: flex;
			align-items: flex-start;
			gap: 12rpx;
		}

		&__bullet {
			flex-shrink: 0;
			width: 32rpx;
			height: 32rpx;
			border-radius: 50%;
			background-color: #FFF3EB;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__bullet-text {
			font-size: 18rpx;
			font-weight: 700;
			color: #FF7A00;
			line-height: 1;
		}

		&__text {
			flex: 1;
			font-size: 24rpx;
			color: #666666;
			line-height: 1.65;
		}
	}

	.detail-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding-top: 4rpx;
	}

	.detail-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		gap: 16rpx;
		padding: 20rpx var(--ry-page-padding-x);
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		background-color: #FFFFFF;
		box-shadow: 0 -8rpx 24rpx rgba(255, 122, 0, 0.08);
		box-sizing: border-box;

		&__btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 6rpx;
			height: 88rpx;
			border-radius: 999rpx;

			&--ghost {
				background-color: #FFF3EB;
			}

			&--primary {
				background: linear-gradient(135deg, #FF9A40, #FF7A00);
				box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.32);
			}

			&:active {
				opacity: 0.88;
			}
		}

		&__btn-text {
			font-size: 28rpx;
			font-weight: 700;
			line-height: 1;
			color: #FFFFFF;

			&--ghost {
				color: #FF7A00;
			}
		}
	}
</style>
`;

fs.writeFileSync(target, content, 'utf8');
console.log('written', target);
