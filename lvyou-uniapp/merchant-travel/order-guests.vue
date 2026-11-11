<template>
	<view class="og-page" :style="pageStyle">
		<view v-if="loading" class="og-loading">
			<u-loading-icon mode="circle" :color="theme.primary" />
		</view>
		<view v-else-if="!guestList.length" class="og-empty">
			<text>暂无出行人信息</text>
		</view>
		<view v-else class="og-sheet">
			<view v-for="(guest, index) in guestList" :key="index" class="og-guest" :class="{ 'og-guest--split': index > 0 }">
				<text class="og-guest__role">{{ guestRoleLabel(guest) }}</text>
				<view class="og-rows">
					<view class="og-row">
						<text class="og-row__label">姓名</text>
						<text class="og-row__value">{{ guest.name || '—' }}</text>
					</view>
					<view class="og-row">
						<text class="og-row__label">身份证号</text>
						<text class="og-row__value">{{ guest.idCard || '—' }}</text>
					</view>
					<view v-if="guest.phone" class="og-row">
						<text class="og-row__label">联系电话</text>
						<text class="og-row__value">{{ guest.phone }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="guestList.length" class="og-footer">
			<view class="og-footer__btn og-footer__btn--ghost" @click="copyAll">
				<text>复制全部</text>
			</view>
			<view class="og-footer__btn" :style="{ background: theme.primary }" @click="exportAll">
				<text>导出 </text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
	} from 'vue';
	import {
		onLoad
	} from '@dcloudio/uni-app';
	import {
		fetchMerchantOrderDetail
	} from '@/common/api/merchant.js';
	import {
		mapMerchantOrder
	} from '@/common/merchant-order-ui.js';
	import {
		useMerchantTheme
	} from '@/common/useMerchantTheme.js';

	const {
		theme,
		pageStyle,
		initTheme,
		applyNavBarColor
	} = useMerchantTheme();
	const orderId = ref('');
	const orderNo = ref('');
	const guestList = ref([]);
	const loading = ref(false);

	function guestRoleLabel(guest) {
		return guest.roleLabel || (guest.role === 'booker' ? '预约人' : '同行人');
	}

	function formatGuestLine(guest) {
		const role = guestRoleLabel(guest);
		let line = `${role}：姓名 ${guest.name || ''}  身份证号 ${guest.idCard || ''}`;
		if (guest.phone) {
			line += `  联系电话 ${guest.phone}`;
		}
		return line;
	}

	function escapeCsvCell(value) {
		const text = String(value ?? '');
		if (/[",\n\r]/.test(text)) {
			return `"${text.replace(/"/g, '""')}"`;
		}
		return text;
	}

	function buildCopyText() {
		return guestList.value.map(formatGuestLine).join('\n\n');
	}

	function buildExportCsv() {
		const header = ['角色', '姓名', '身份证号', '联系电话'];
		const rows = guestList.value.map((guest) => [
			guestRoleLabel(guest),
			guest.name || '',
			guest.idCard || '',
			guest.phone || ''
		]);
		return [header, ...rows]
			.map((row) => row.map(escapeCsvCell).join(','))
			.join('\n');
	}

	function buildExportFilename() {
		const suffix = orderNo.value || orderId.value || 'order';
		return `出行人-${suffix}.csv`;
	}

	function exportTextFileH5(content, filename) {
		const blob = new Blob([`\ufeff${content}`], {
			type: 'text/csv;charset=utf-8'
		});
		const url = URL.createObjectURL(blob);
		const link = document.createElement('a');
		link.href = url;
		link.download = filename;
		link.click();
		URL.revokeObjectURL(url);
		uni.showToast({
			title: '文件已开始下载',
			icon: 'success'
		});
	}

	function exportTextFileMp(content, filename) {
		const fs = uni.getFileSystemManager();
		const basePath = typeof wx !== 'undefined' && wx.env?.USER_DATA_PATH ?
			wx.env.USER_DATA_PATH :
			`${uni.env.USER_DATA_PATH}`;
		const filePath = `${basePath}/${filename}`;
		fs.writeFile({
			filePath,
			data: `\ufeff${content}`,
			encoding: 'utf8',
			success: () => {
				if (typeof wx !== 'undefined' && wx.shareFileMessage) {
					wx.shareFileMessage({
						filePath,
						success: () => {},
						fail: () => {
							uni.showToast({
								title: '请在分享面板保存文件',
								icon: 'none'
							});
						}
					});
					return;
				}
				uni.showToast({
					title: '文件已保存到本地',
					icon: 'success'
				});
			},
			fail: () => {
				uni.showToast({
					title: '导出失败',
					icon: 'none'
				});
			}
		});
	}

	function copyAll() {
		const text = buildCopyText();
		if (!text) return;
		uni.setClipboardData({
			data: text,
			success: () => uni.showToast({
				title: '已复制全部出行人',
				icon: 'success'
			})
		});
	}

	function exportAll() {
		const content = buildExportCsv();
		if (!content) return;
		const filename = buildExportFilename();

		// #ifdef H5
		exportTextFileH5(content, filename);
		return;
		// #endif

		// #ifdef MP-WEIXIN
		exportTextFileMp(content, filename);
		return;
		// #endif

		uni.showModal({
			title: '导出 CSV',
			content: '当前环境暂不支持直接下载，是否复制为表格文本？',
			success: (res) => {
				if (res.confirm) copyAll();
			}
		});
	}

	async function loadOrder() {
		if (!orderId.value) return;
		loading.value = true;
		try {
			const res = await fetchMerchantOrderDetail(orderId.value);
			if (res.code === 0 && res.data) {
				const order = mapMerchantOrder(res.data);
				orderNo.value = order.orderNo || '';
				guestList.value = Array.isArray(order.guestList) ? order.guestList : [];
			} else {
				guestList.value = [];
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			guestList.value = [];
			uni.showToast({
				title: '网络异常',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	onLoad(async (options) => {
		orderId.value = options?.id ? String(options.id) : '';
		await initTheme();
		applyNavBarColor();
		uni.setNavigationBarTitle({
			title: '出行人信息'
		});
		loadOrder();
	});
</script>

<style lang="scss" scoped>
	.og-page {
		min-height: 100vh;
		padding: 24rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
		background: #f0f2f5;
		box-sizing: border-box;
	}

	.og-loading,
	.og-empty {
		padding: 120rpx 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
		font-size: 28rpx;
		color: #999;
	}

	.og-sheet {
		background: #fff;
		border-radius: 20rpx;
		padding: 8rpx 28rpx;
	}

	.og-guest {
		padding: 24rpx 0;

		&--split {
			border-top: 1rpx solid #f2f3f5;
		}

		&__role {
			display: block;
			margin-bottom: 16rpx;
			font-size: 28rpx;
			font-weight: 700;
			color: #1a1a22;
			line-height: 1.3;
		}
	}

	.og-rows {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	.og-row {
		display: flex;
		align-items: flex-start;
		gap: 24rpx;
		min-width: 0;

		&__label {
			width: 148rpx;
			flex-shrink: 0;
			font-size: 26rpx;
			color: #9a9aa6;
			line-height: 1.5;
		}

		&__value {
			flex: 1;
			min-width: 0;
			font-size: 26rpx;
			color: #1a1a22;
			line-height: 1.5;
			word-break: break-all;
		}
	}

	.og-footer {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		display: flex;
		gap: 16rpx;
		padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
		background: #fff;
		border-top: 1rpx solid #f0f0f0;
		box-sizing: border-box;

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
				color: #fff;
				line-height: 1;
			}

			&--ghost {
				background: #fff;
				border: 2rpx solid #e5e7eb;

				text {
					color: #1a1a22;
				}
			}

			&:active {
				opacity: 0.88;
			}
		}
	}
</style>
