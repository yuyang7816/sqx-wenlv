<template>
	<view class="rs-page" :style="pageStyle">
		<view class="rs-header">
			<view class="rs-tabs">
				<view
					v-for="tab in statusTabs"
					:key="tab.key"
					class="rs-tabs__item"
					:class="{ 'rs-tabs__item--active': activeTab === tab.key }"
					@click="switchTab(tab.key)"
				>
					<text>{{ tab.label }}</text>
				</view>
			</view>
			<view class="rs-header__add" @click="goEdit()">
				<u-icon name="plus" :color="theme.primary" :size="18" />
				<text>新增</text>
			</view>
		</view>

		<view v-if="loading" class="rs-loading">
			<u-loading-icon mode="circle" :color="theme.primary" size="36" />
			<text class="rs-loading__text">加载中...</text>
		</view>

		<template v-else>
			<view v-if="displayList.length" class="rs-list">
				<view
					v-for="item in displayList"
					:key="item.id"
					class="rs-item ry-card"
					@click="goEdit(item.id)"
				>
					<view class="rs-item__top">
						<text class="rs-item__time">{{ item.updatedAt || '—' }}</text>
						<view class="rs-item__status-wrap" @click.stop>
							<text
								class="rs-item__status-text"
								:class="item.status === 1 ? 'rs-item__status-text--on' : 'rs-item__status-text--off'"
							>
								{{ item.status === 1 ? '启用' : '停用' }}
							</text>
							<u-switch
								:model-value="item.status === 1"
								size="18"
								:active-color="theme.primary"
								:disabled="statusLoadingId === item.id"
								@change="(enabled) => onToggleStatus(item.id, enabled)"
							/>
						</view>
					</view>

					<view class="rs-item__body">
						<image class="rs-item__cover" :src="item.cover" mode="aspectFill" />
						<view class="rs-item__main">
							<text class="rs-item__name">{{ item.name }}</text>
							<view v-if="item.chips.length || item.tags.length" class="rs-item__tags">
								<text v-for="chip in item.chips" :key="chip" class="rs-item__chip">{{ chip }}</text>
								<text v-for="tag in item.tags" :key="tag" class="rs-item__chip rs-item__chip--tag">{{ tag }}</text>
							</view>
							<text v-else class="rs-item__desc">暂无补充信息</text>
						</view>
					</view>

					<view class="rs-item__footer" @click.stop>
						<text class="rs-item__linked">关联 {{ item.linkedRoomCount || 0 }} 房型</text>
						<view class="rs-item__actions">
							<view class="rs-item__btn rs-item__btn--edit" @click="goEdit(item.id)">
								<text>编辑</text>
							</view>
							<view
								class="rs-item__btn rs-item__btn--del"
								:class="{ 'rs-item__btn--disabled': item.linkedRoomCount > 0 }"
								@click="onDelete(item)"
							>
								<text>删除</text>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view v-else class="rs-empty">
				<u-empty mode="list" :text="emptyText" />
				<text v-if="!standards.length" class="rs-empty__desc">创建标准模板后，新增房型时可一键继承设施与默认参数</text>
				<u-button
					v-if="!standards.length"
					text="创建客房标准"
					:color="theme.primary"
					:custom-style="emptyBtnStyle"
					@click="goEdit()"
				/>
			</view>
		</template>
	</view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { requireMerchantDepositPaid } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import {
	buildRoomStandardPayload,
	standardToRoomDetail
} from '@/common/merchant-hotel-room.js';
import {
	fetchMerchantRoomStandards,
	deleteMerchantRoomStandard,
	saveMerchantRoomStandard
} from '@/common/api/merchant.js';

const { theme, pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const standards = ref([]);
const loading = ref(false);
const statusLoadingId = ref(null);
const activeTab = ref('all');

const statusTabs = [
	{ key: 'all', label: '全部' },
	{ key: 'enabled', label: '启用' },
	{ key: 'disabled', label: '停用' }
];

const emptyBtnStyle = {
	marginTop: '32rpx',
	borderRadius: '24px',
	padding: '0 48rpx'
};

const emptyText = computed(() => {
	if (!standards.value.length) return '还没有客房标准';
	if (activeTab.value === 'enabled') return '暂无启用的客房标准';
	if (activeTab.value === 'disabled') return '暂无停用的客房标准';
	return '暂无客房标准';
});

const displayList = computed(() => {
	let list = standards.value;
	if (activeTab.value === 'enabled') {
		list = list.filter((item) => (item.status == null ? 1 : item.status) === 1);
	} else if (activeTab.value === 'disabled') {
		list = list.filter((item) => (item.status == null ? 1 : item.status) === 0);
	}
	return list.map(mapStandardItem);
});

function switchTab(key) {
	if (activeTab.value === key) return;
	activeTab.value = key;
}

function formatFloorHint(value) {
	const text = String(value || '').trim();
	if (!text) return '';
	return text.endsWith('层') ? text : `${text}层`;
}

function buildDisplayMeta(item) {
	const chips = [];
	const defaults = item.defaults || {};
	const facilities = item.facilities || [];
	const floorText = formatFloorHint(item.floorHint);
	if (floorText) chips.push(floorText);
	if (facilities.length) chips.push(`${facilities.length} 项设施`);

	const petPolicy = defaults.petPolicy || item.petPolicy || '';
	if (petPolicy) chips.push(petPolicy);

	const windowType = defaults.windowType || item.windowType || '';
	if (windowType) chips.push(windowType);

	const smokingPolicy = defaults.smokingPolicy || item.smokingPolicy || '';
	if (smokingPolicy) chips.push(smokingPolicy);

	const tags = (item.tags || []).slice(0, 3);
	return { chips, tags };
}

function mapStandardItem(item) {
	const gallery = (item.gallery || []).filter(Boolean);
	const { chips, tags } = buildDisplayMeta(item);
	return {
		id: item.id,
		name: item.name || '未命名标准',
		cover: gallery[0] || PLACEHOLDER_LOGO,
		chips,
		tags,
		linkedRoomCount: Number(item.linkedRoomCount || 0),
		status: item.status == null ? 1 : item.status,
		updatedAt: formatUpdatedAt(item.updatedAt)
	};
}

function formatUpdatedAt(value) {
	if (!value) return '';
	const text = String(value);
	return text.length >= 10 ? text.slice(0, 10) : text;
}

async function reload() {
	loading.value = true;
	try {
		const res = await fetchMerchantRoomStandards();
		if (res.code === 0) {
			standards.value = res.data || [];
		} else {
			standards.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		standards.value = [];
		uni.showToast({ title: '网络异常', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function goEdit(id) {
	const url = id
		? `/merchant-hotel/room-standard-edit?id=${id}`
		: '/merchant-hotel/room-standard-edit';
	uni.navigateTo({ url });
}

function buildPayloadFromStandard(item, status) {
	const detail = standardToRoomDetail(item);
	return buildRoomStandardPayload({
		id: item.id,
		name: item.name,
		floorHint: item.floorHint,
		sort: item.sort,
		status,
		...detail
	});
}

async function onToggleStatus(id, enabled) {
	const raw = standards.value.find((item) => item.id === id);
	if (!raw || statusLoadingId.value === id) return;

	const nextStatus = enabled ? 1 : 0;
	const prevStatus = raw.status == null ? 1 : raw.status;
	if (nextStatus === prevStatus) return;

	statusLoadingId.value = id;
	raw.status = nextStatus;

	try {
		const res = await saveMerchantRoomStandard(buildPayloadFromStandard(raw, nextStatus));
		if (res.code !== 0) {
			raw.status = prevStatus;
			uni.showToast({ title: res.msg || '操作失败', icon: 'none' });
			return;
		}
		if (res.data) {
			const index = standards.value.findIndex((item) => item.id === id);
			if (index >= 0) standards.value[index] = res.data;
		}
		uni.showToast({ title: nextStatus === 1 ? '已启用' : '已停用', icon: 'success' });
	} catch (e) {
		raw.status = prevStatus;
		uni.showToast({ title: '网络异常', icon: 'none' });
	} finally {
		statusLoadingId.value = null;
	}
}

function onDelete(item) {
	if (item.linkedRoomCount > 0) {
		uni.showToast({ title: '仍有关联房型，无法删除', icon: 'none' });
		return;
	}
	uni.showModal({
		title: '确认删除',
		content: `确定删除「${item.name}」？删除后不可恢复。`,
		confirmColor: '#FF4D4F',
		success: async (res) => {
			if (!res.confirm) return;
			try {
				const apiRes = await deleteMerchantRoomStandard(item.id);
				if (apiRes.code !== 0) {
					uni.showToast({ title: apiRes.msg || '删除失败', icon: 'none' });
					return;
				}
				uni.showToast({ title: '已删除', icon: 'success' });
				await reload();
			} catch (e) {
				uni.showToast({ title: '网络异常', icon: 'none' });
			}
		}
	});
}

onLoad(async () => {
	await initTheme();
	uni.setNavigationBarTitle({ title: '客房标准' });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantDepositPaid())) return;
	await initTheme();
	applyNavBarColor();
	await reload();
});
</script>

<style lang="scss" scoped>
.rs-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.rs-header {
	padding: 16rpx var(--ry-page-padding-x);
	background: #FFF;
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	position: sticky;
	top: 0;
	z-index: 10;

	&__add {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 18rpx 24rpx;
		border-radius: 12rpx;
		background: var(--mc-primary-light, rgba(41, 121, 255, 0.08));
		font-size: 26rpx;
		font-weight: 400;
		color: var(--mc-primary, #2979FF);

		&:active {
			opacity: 0.88;
		}
	}
}

.rs-tabs {
	flex: 1;
	min-width: 0;
	display: flex;
	gap: 12rpx;

	&__item {
		flex: 1;
		padding: 14rpx 8rpx;
		border-radius: 999rpx;
		text-align: center;
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		background: #F3F2F0;

		&--active {
			background: var(--mc-primary-light, rgba(41, 121, 255, 0.08));
			color: var(--mc-primary, #2979FF);
			font-weight: 600;
		}
	}
}

.rs-list {
	padding: 20rpx var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.rs-item {
	display: flex;
	flex-direction: column;
	gap: 24rpx;
	padding: 28rpx;

	&__top {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding-bottom: 4rpx;
	}

	&__body {
		display: flex;
		gap: 24rpx;
	}

	&__cover {
		width: 120rpx;
		height: 120rpx;
		border-radius: 12rpx;
		flex-shrink: 0;
		background: #F5F5F5;
	}

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	&__name {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
	}

	&__status-wrap {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	&__status-text {
		font-size: 22rpx;

		&--on {
			color: #2EAF6A;
		}

		&--off {
			color: var(--ry-text-hint);
		}
	}

	&__desc {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.45;
	}

	&__tags {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	&__chip {
		display: inline-flex;
		align-items: center;
		padding: 6rpx 14rpx;
		border-radius: 8rpx;
		background: #F7F8FC;
		font-size: 22rpx;
		color: var(--ry-text-secondary);
		line-height: 1.4;

		&--tag {
			background: rgba(41, 121, 255, 0.08);
			color: var(--mc-primary, #2979FF);
		}
	}

	&__time {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	&__footer {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding-top: 22rpx;
		border-top: 1rpx solid #F0F0F0;
	}

	&__linked {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		line-height: 1.4;
		color: var(--mc-primary, #2979FF);
	}

	&__actions {
		flex-shrink: 0;
		display: flex;
		gap: 16rpx;
	}

	&__btn {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 12rpx 32rpx;
		border-radius: 12rpx;
		font-size: 26rpx;
		font-weight: 400;
		line-height: 1;

		&:active {
			opacity: 0.88;
		}

		&--edit {
			background: var(--mc-primary-light, rgba(41, 121, 255, 0.08));
			color: var(--mc-primary, #2979FF);
		}

		&--del {
			background: #FFF0F0;
			color: #FF4D4F;
		}

		&--disabled {
			opacity: 0.45;
		}
	}
}

.rs-empty {
	padding: 80rpx var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	align-items: center;
	text-align: center;

	&__desc {
		margin-top: 12rpx;
		font-size: 26rpx;
		line-height: 1.6;
		color: var(--ry-text-hint);
		max-width: 520rpx;
	}
}

.rs-loading {
	min-height: 50vh;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 16rpx;

	&__text {
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}
}
</style>
