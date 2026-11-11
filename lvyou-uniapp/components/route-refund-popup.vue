<template>
	<u-popup
		:show="show"
		mode="bottom"
		:round="24"
		bg-color="transparent"
		:close-on-click-overlay="!submitting"
		@close="$emit('close')"
	>
		<view class="rrp">
			<view class="rrp__header">
				<text class="rrp__title">{{ ui.refundPopupTitle }}</text>
				<view class="rrp__close" @click="$emit('close')">
					<u-icon name="close" :size="18" color="#999" />
				</view>
			</view>

			<scroll-view class="rrp__scroll" scroll-y :show-scrollbar="false">
				<view class="rrp__section">
					<text class="rrp__section-title">{{ ui.refundPopupCalcTitle }}</text>
					<view class="rrp__calc">
						<view
							v-for="(row, index) in calcRows"
							:key="`${row.label}-${index}`"
							class="rrp__row"
							:class="{ 'rrp__row--highlight': row.highlight }"
						>
							<text class="rrp__label">{{ row.label }}</text>
							<text class="rrp__value" :class="{ 'rrp__value--accent': row.highlight }">{{ row.value }}</text>
						</view>
					</view>
				</view>

				<view v-if="policyText" class="rrp__section">
					<text class="rrp__section-title">{{ ui.sectionCancelPolicy }}</text>
					<view class="rrp__policy">
						<text class="rrp__policy-text">{{ policyText }}</text>
					</view>
				</view>
			</scroll-view>

			<view class="rrp__actions">
				<view class="rrp__btn rrp__btn--ghost" :class="{ 'rrp__btn--disabled': submitting }" @click="$emit('close')">
					<text>{{ ui.refundPopupCancel }}</text>
				</view>
				<view
					class="rrp__btn rrp__btn--primary"
					:class="{ 'rrp__btn--disabled': submitting }"
					@click="$emit('confirm')"
				>
					<text>{{ submitting ? ui.refundPopupSubmitting : ui.refundPopupConfirm }}</text>
				</view>
			</view>
		</view>
	</u-popup>
</template>

<script setup>
defineProps({
	show: { type: Boolean, default: false },
	ui: { type: Object, required: true },
	calcRows: { type: Array, default: () => [] },
	policyText: { type: String, default: '' },
	submitting: { type: Boolean, default: false }
});

defineEmits(['close', 'confirm']);
</script>

<style lang="scss" scoped>
.rrp {
	max-height: 78vh;
	display: flex;
	flex-direction: column;
	padding: 32rpx var(--ry-page-padding-x) calc(24rpx + env(safe-area-inset-bottom));
	background: #fff;
	border-radius: 24rpx 24rpx 0 0;
	box-sizing: border-box;

	&__header {
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		margin-bottom: 20rpx;
		flex-shrink: 0;
	}

	&__title {
		font-size: 32rpx;
		font-weight: 600;
		color: #1a1a1a;
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
		background: #f5f5f5;
	}

	&__scroll {
		flex: 1;
		min-height: 0;
		max-height: calc(78vh - 220rpx);
	}

	&__section {
		margin-bottom: 24rpx;

		&:last-child {
			margin-bottom: 8rpx;
		}
	}

	&__section-title {
		display: block;
		font-size: 26rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 16rpx;
	}

	&__calc {
		background: #f8f9fb;
		border-radius: 16rpx;
		padding: 8rpx 24rpx;
	}

	&__row {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 24rpx;
		padding: 18rpx 0;
		border-bottom: 1rpx solid #eef1f5;

		&:last-child {
			border-bottom: none;
		}

		&--highlight {
			padding-top: 20rpx;
			padding-bottom: 20rpx;
		}
	}

	&__label {
		flex-shrink: 0;
		width: 168rpx;
		font-size: 26rpx;
		color: #888;
		line-height: 1.5;
	}

	&__value {
		flex: 1;
		font-size: 26rpx;
		color: #333;
		text-align: right;
		line-height: 1.5;
		word-break: break-all;

		&--accent {
			font-size: 32rpx;
			font-weight: 700;
			color: #ff6600;
		}
	}

	&__policy {
		background: #fff8f3;
		border-radius: 16rpx;
		padding: 24rpx;
		border: 1rpx solid rgba(255, 102, 0, 0.08);
	}

	&__policy-text {
		display: block;
		font-size: 24rpx;
		color: #666;
		line-height: 1.75;
		white-space: pre-wrap;
	}

	&__actions {
		display: flex;
		gap: 20rpx;
		margin-top: 20rpx;
		flex-shrink: 0;
	}

	&__btn {
		flex: 1;
		height: 88rpx;
		border-radius: 999rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		text {
			font-size: 28rpx;
			font-weight: 600;
			line-height: 1;
		}

		&--ghost {
			background: #fff;
			border: 2rpx solid #e5e7eb;

			text {
				color: #666;
			}
		}

		&--primary {
			background: linear-gradient(135deg, #ff8a3d, #ff6600);
			border: none;

			text {
				color: #fff;
			}
		}

		&--disabled {
			opacity: 0.65;
		}

		&:active:not(&--disabled) {
			opacity: 0.88;
		}
	}
}
</style>
