<template>
	<u-popup
		:show="show"
		mode="bottom"
		:round="24"
		bg-color="transparent"
		:close-on-click-overlay="true"
		@close="$emit('close')"
	>
		<view class="bpp">
			<view class="bpp__header">
				<text class="bpp__title">{{ ui.payTitle }}</text>
				<view class="bpp__close" @click="$emit('close')">
					<u-icon name="close" :size="18" color="#999" />
				</view>
			</view>

			<view class="bpp__amount">
				<text class="bpp__amount-label">{{ ui.payAmountLabel }}</text>
				<view class="bpp__amount-row">
					<text class="bpp__amount-symbol">{{ ui.currencySymbol }}</text>
					<text class="bpp__amount-value" :style="{ color: priceColor }">{{ payAmount }}</text>
				</view>
			</view>

			<view class="bpp__list">
				<view
					v-for="method in paymentMethods"
					:key="method.key"
					class="bpp-item"
					:class="{ 'bpp-item--active': selectedPayMethod === method.key, 'bpp-item--disabled': method.disabled }"
					:style="selectedPayMethod === method.key ? { borderColor: method.color } : {}"
					@click="$emit('select', method)"
				>
					<view class="bpp-item__icon" :style="{ backgroundColor: `${method.color}14` }">
						<u-icon :name="method.icon" :size="20" :color="method.color" />
					</view>
					<view class="bpp-item__main">
						<view class="bpp-item__title-row">
							<text class="bpp-item__title">{{ method.label }}</text>
							<view v-if="method.recommend" class="bpp-item__tag">
								<text>{{ ui.payRecommend }}</text>
							</view>
						</view>
						<text v-if="method.desc" class="bpp-item__desc" :class="{ 'bpp-item__desc--warn': method.disabled }">{{ method.desc }}</text>
					</view>
					<view
						class="bpp-item__radio"
						:class="{ 'bpp-item__radio--checked': selectedPayMethod === method.key }"
						:style="selectedPayMethod === method.key ? { borderColor: method.color, backgroundColor: method.color } : {}"
					>
						<u-icon v-if="selectedPayMethod === method.key" name="checkmark" :size="12" color="#fff" />
					</view>
				</view>
			</view>

			<view
				class="bpp__btn"
				:style="{ backgroundColor: accentColor }"
				:class="{ 'bpp__btn--disabled': paying }"
				@click="$emit('confirm')"
			>
				<text class="bpp__btn-text">{{ paying ? ui.payProcessing : ui.payConfirm }}</text>
			</view>
		</view>
	</u-popup>
</template>

<script setup>
defineProps({
	show: { type: Boolean, default: false },
	ui: { type: Object, required: true },
	payAmount: { type: [Number, String], default: 0 },
	paymentMethods: { type: Array, default: () => [] },
	selectedPayMethod: { type: String, default: '' },
	paying: { type: Boolean, default: false },
	accentColor: { type: String, default: '#2979FF' },
	priceColor: { type: String, default: '#FF4757' }
});

defineEmits(['close', 'select', 'confirm']);
</script>

<style lang="scss" scoped>
.bpp {
	padding: 32rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	background: #fff;
	border-radius: 24rpx 24rpx 0 0;
	box-sizing: border-box;

	&__header {
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		margin-bottom: 8rpx;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 500;
		color: #333;
	}

	&__close {
		position: absolute;
		right: 0;
		top: 50%;
		transform: translateY(-50%);
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		border-radius: 50%;
		background: #F5F5F5;
	}

	&__amount {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
		padding: 24rpx 0 28rpx;
		margin-bottom: 24rpx;
		background: #FAFAFA;
		border-radius: 16rpx;
	}

	&__amount-label {
		font-size: 24rpx;
		color: #999;
	}

	&__amount-row {
		display: flex;
		align-items: baseline;
		gap: 4rpx;
	}

	&__amount-symbol {
		font-size: 28rpx;
		font-weight: 500;
		line-height: 1;
	}

	&__amount-value {
		font-size: 56rpx;
		font-weight: 700;
		line-height: 1;
	}

	&__list {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
		margin-bottom: 28rpx;
	}

	&__btn {
		height: 88rpx;
		border-radius: 44rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		&--disabled {
			opacity: 0.6;
			pointer-events: none;
		}
	}

	&__btn-text {
		font-size: 30rpx;
		font-weight: 400;
		color: #fff;
	}
}

.bpp-item {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 18rpx 20rpx;
	min-height: 88rpx;
	border-radius: 16rpx;
	background: #FAFAFA;
	border: 2rpx solid transparent;
	box-sizing: border-box;

	&--active {
		background: #fff;
	}

	&--disabled {
		opacity: 0.45;
	}

	&__icon {
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	&__title-row {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	&__title {
		font-size: 28rpx;
		font-weight: 400;
		color: #333;
	}

	&__tag {
		padding: 2rpx 10rpx;
		border-radius: 6rpx;
		background: #FFF3EB;

		text {
			font-size: 18rpx;
			color: #FF6600;
		}
	}

	&__desc {
		font-size: 22rpx;
		color: #999;

		&--warn {
			color: #EF4444;
		}
	}

	&__radio {
		flex-shrink: 0;
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		border: 2rpx solid #D9D9D9;
		display: flex;
		align-items: center;
		justify-content: center;
		box-sizing: border-box;
	}
}
</style>
