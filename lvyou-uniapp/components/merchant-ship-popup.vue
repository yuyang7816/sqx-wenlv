<template>
	<u-popup
		:show="show"
		mode="bottom"
		:round="24"
		bg-color="transparent"
		:close-on-click-overlay="!submitting"
		@close="handleClose"
	>
		<view class="msp-popup">
			<view class="msp-popup__header">
				<text class="msp-popup__title">{{ ui.shipPopupTitle }}</text>
				<view class="msp-popup__close" @click="handleClose">
					<u-icon name="close" :size="18" color="#999" />
				</view>
			</view>
			<view class="msp-popup__field">
				<text class="msp-popup__label">{{ ui.shipCompanyLabel }}</text>
				<input
					v-model="company"
					class="msp-popup__input"
					:placeholder="ui.shipCompanyPlaceholder"
					placeholder-class="msp-popup__placeholder"
				/>
			</view>
			<view class="msp-popup__field">
				<text class="msp-popup__label">{{ ui.shipNoLabel }}</text>
				<input
					v-model="trackingNo"
					class="msp-popup__input"
					:placeholder="ui.shipNoPlaceholder"
					placeholder-class="msp-popup__placeholder"
				/>
			</view>
			<view class="msp-popup__actions">
				<view class="msp-popup__btn msp-popup__btn--ghost" @click="handleClose">
					<text>{{ ui.shipPopupCancel }}</text>
				</view>
				<view
					class="msp-popup__btn msp-popup__btn--primary"
					:style="{ background: primaryColor }"
					@click="handleSubmit"
				>
					<text>{{ submitting ? ui.shipPopupSubmitting : ui.shipPopupConfirm }}</text>
				</view>
			</view>
		</view>
	</u-popup>
</template>

<script setup>
import { ref, watch } from 'vue';
import { MERCHANT_BUSINESS_UI } from '@/common/merchant-center.js';

const props = defineProps({
	show: {
		type: Boolean,
		default: false
	},
	submitting: {
		type: Boolean,
		default: false
	},
	primaryColor: {
		type: String,
		default: '#B45D2D'
	}
});

const emit = defineEmits(['update:show', 'submit', 'close']);

const ui = MERCHANT_BUSINESS_UI;
const company = ref('');
const trackingNo = ref('');

watch(() => props.show, (visible) => {
	if (visible) {
		company.value = '';
		trackingNo.value = '';
	}
});

function handleClose() {
	if (props.submitting) return;
	emit('update:show', false);
	emit('close');
}

function handleSubmit() {
	emit('submit', {
		company: String(company.value || '').trim(),
		trackingNo: String(trackingNo.value || '').trim()
	});
}
</script>

<style lang="scss" scoped>
.msp-popup {
	padding: 32rpx var(--ry-page-padding-x) calc(24rpx + env(safe-area-inset-bottom));
	background: #fff;
	border-radius: 24rpx 24rpx 0 0;
	box-sizing: border-box;

	&__header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-bottom: 28rpx;
	}

	&__title {
		font-size: 32rpx;
		font-weight: 700;
		color: #1A1A22;
	}

	&__close {
		width: 56rpx;
		height: 56rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__field {
		margin-bottom: 24rpx;
	}

	&__label {
		display: block;
		margin-bottom: 12rpx;
		font-size: 26rpx;
		font-weight: 600;
		color: #333;
	}

	&__input {
		width: 100%;
		height: 88rpx;
		padding: 0 24rpx;
		border-radius: 16rpx;
		background: #F5F6F8;
		font-size: 28rpx;
		color: #1A1A22;
		box-sizing: border-box;
	}

	&__placeholder {
		color: #B0B0B8;
	}

	&__actions {
		display: flex;
		gap: 16rpx;
		margin-top: 12rpx;
	}

	&__btn {
		flex: 1;
		height: 88rpx;
		border-radius: 44rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		text {
			font-size: 28rpx;
			font-weight: 600;
		}

		&--ghost {
			background: #F5F6F8;

			text {
				color: #666;
			}
		}

		&--primary text {
			color: #fff;
		}
	}
}
</style>
