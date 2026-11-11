<template>
	<view class="mse-page" :style="pageStyle">
		<!-- 封面 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="camera-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">{{ ui.serviceCover }}</text>
					<text class="mse-module__desc">{{ coverHint }}</text>
				</view>
			</view>
			<view class="mse-cover" @click="pickCover">
				<image v-if="form.cover" :src="form.cover" mode="aspectFill" class="mse-cover__img" />
				<view v-else class="mse-cover__empty">
					<u-icon name="camera-fill" :color="theme.primary" :size="32" />
					<text>上传封面</text>
				</view>
			</view>
		</view>

		<!-- 基本信息 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="edit-pen-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">基本信息</text>
					<text class="mse-module__desc">名称、价格与库存等核心信息</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field">
					<text class="mse-field__label"><text class="mse-field__req">*</text>{{ ui.serviceName }}</text>
					<input v-model="form.name" class="mse-field__input" :placeholder="'请输入' + ui.serviceName" placeholder-class="mse-field__placeholder" />
				</view>
				<view class="mse-field">
					<text class="mse-field__label">{{ ui.servicePrice }}</text>
					<view class="mse-field__input-wrap">
						<text class="mse-field__prefix">¥</text>
						<input v-model="form.price" class="mse-field__input mse-field__input--plain" type="digit" placeholder="0 表示免费预约" placeholder-class="mse-field__placeholder" />
					</view>
				</view>
				<view class="mse-field mse-field--last">
					<text class="mse-field__label">{{ ui.serviceStock }}</text>
					<input v-model="form.stock" class="mse-field__input" type="number" placeholder="请输入库存或名额" placeholder-class="mse-field__placeholder" />
				</view>
			</view>
		</view>

		<!-- 客房标准（酒店） -->
		<view v-if="storeType === 'hotel'" class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="file-text-fill" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">客房标准</text>
					<text class="mse-module__desc">关联后可继承设施、标签与默认参数，本页只需填写差异项</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field">
					<text class="mse-field__label">选择标准</text>
					<picker mode="selector" :range="roomStandardLabels" :value="roomStandardIndex" @change="onRoomStandardChange">
						<view class="mse-field__picker">
							<text :class="{ 'mse-field__picker-placeholder': !selectedRoomStandardLabel }">
								{{ selectedRoomStandardLabel || '不关联标准（完整填写）' }}
							</text>
							<u-icon name="arrow-down" color="#999" :size="14" />
						</view>
					</picker>
					<text class="mse-field__hint">
						<navigator url="/merchant-hotel/room-standards" class="mse-field__link">管理客房标准</navigator>
					</text>
				</view>
				<view v-if="roomStandardId" class="mse-field mse-field--last">
					<view class="mse-switch-row">
						<view class="mse-switch-row__text">
							<text class="mse-field__label">继承客房标准</text>
							<text class="mse-field__hint">开启后自动带入标准中的设施、标签与默认参数</text>
						</view>
						<u-switch v-model="inheritStandard" size="20" :active-color="theme.primary" @change="onInheritStandardChange" />
					</view>
				</view>
			</view>
		</view>

		<!-- 继承提示 -->
		<view v-if="storeType === 'hotel' && inheritStandard && selectedStandard" class="mse-inherit-tip ry-card">
			<u-icon name="info-circle-fill" :color="theme.primary" :size="16" />
			<text>已关联「{{ selectedStandard.name }}」，下方内容已自动带入标准默认值，您可针对本房型自由修改、删除或补充</text>
		</view>

		<!-- 房型详情（酒店） -->
		<HotelRoomFields
			v-if="storeType === 'hotel'"
			v-model="displayRoomDetail"
			:theme="theme"
			variant="full"
			layout="split"
		/>

		<!-- 扩展字段 -->
		<view v-if="extraFields.length" class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="list-dot" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">补充信息</text>
					<text class="mse-module__desc">按业态填写额外展示字段</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view v-for="(field, index) in extraFields" :key="field.key" class="mse-field" :class="{ 'mse-field--last': index === extraFields.length - 1 }">
					<text class="mse-field__label">{{ field.label }}</text>
					<textarea
						v-if="field.type === 'textarea'"
						v-model="form[field.key]"
						class="mse-field__textarea"
						:placeholder="field.placeholder"
						placeholder-class="mse-field__placeholder"
						maxlength="500"
					/>
					<input
						v-else
						v-model="form[field.key]"
						class="mse-field__input"
						:type="field.type === 'number' ? 'number' : 'text'"
						:placeholder="field.placeholder"
						placeholder-class="mse-field__placeholder"
					/>
					<text v-if="field.hint" class="mse-field__hint">{{ field.hint }}</text>
				</view>
			</view>
		</view>

		<!-- 简介 -->
		<view class="mse-module ry-card">
			<view class="mse-module__head">
				<view class="mse-module__icon" :style="{ background: theme.primaryLight || 'rgba(41,121,255,0.1)' }">
					<u-icon name="file-text" :color="theme.primary" :size="18" />
				</view>
				<view class="mse-module__meta">
					<text class="mse-module__title">{{ descLabel }}</text>
					<text class="mse-module__desc">{{ descHint }}</text>
				</view>
			</view>
			<view class="mse-module__body">
				<view class="mse-field mse-field--last">
					<textarea v-model="form.desc" class="mse-field__textarea" :placeholder="'请输入' + descLabel" placeholder-class="mse-field__placeholder" maxlength="500" />
				</view>
			</view>
		</view>

		<view class="mse-footer">
			<u-button text="保存草稿" plain :color="theme.primary" :custom-style="btnPlain" :loading="submitting" @click="onSaveDraft" />
			<u-button :text="ui.saveAndPublish" :color="theme.primary" :custom-style="btnPrimary" :loading="submitting" @click="onPublish" />
		</view>
	</view>
</template>

<script setup>
import { computed, reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import {
	SERVICE_STATUS,
	requireMerchantDepositPaid
} from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { getServiceExtraFields, getServiceDescLabel } from '@/common/merchant-service-fields.js';
import {
	emptyRoomDetail,
	emptyRoomOverride,
	normalizeRoomDetail,
	buildRoomDetailPayload,
	mergeRoomDetailWithStandard,
	extractRoomOverridesFromStandard,
	normalizeRoomOverride
} from '@/common/merchant-hotel-room.js';
import HotelRoomFields from '@/merchant-hotel/components/hotel-room-fields.vue';
import {
	fetchMerchantServiceDetail,
	fetchMerchantRoomStandards,
	saveMerchantService,
	ensureUploadedUrl
} from '@/common/api/merchant.js';

/** 门店类型 → 首页推荐 Tab（与后端 MerchantHomeTabSupport 一致） */
function resolveHomeTabKey(storeType) {
	const key = String(storeType || '').toLowerCase();
	if (key === 'hotel') return 'hotel';
	if (key === 'travel') return 'route';
	if (key === 'culture') return 'heritage';
	if (key === 'restaurant' || key === 'goose') return 'activity';
	return 'activity';
}

const PACKAGE_STORE_TYPE = 'hotel';
const { theme, pageStyle, businessUi, storeType, initTheme, applyNavBarColor, applyStoreType } = useMerchantTheme();
const extraFields = computed(() => getServiceExtraFields(storeType.value));
const descLabel = computed(() => getServiceDescLabel(storeType.value));
const descHint = computed(() => {
	const map = {
		hotel: '用户端房型卡片展示简介（可选）',
		restaurant: '用户端招牌菜品列表展示此介绍',
		travel: '用户端线路详情页「线路介绍」展示',
		culture: '用户端商品详情页标题下方展示'
	};
	return map[storeType.value] || '用户端详情页展示';
});
const coverHint = computed(() => {
	const map = {
		hotel: '用户端酒店详情「房型推荐」卡片封面',
		restaurant: '用户端招牌菜品列表封面图',
		travel: '用户端线路详情页顶部封面',
		culture: '用户端商品详情页封面与轮播首图'
	};
	return map[storeType.value] || '用户端详情页封面';
});
const ui = businessUi;
const serviceId = ref('');
const submitting = ref(false);
const formInitialized = ref(false);
const emptyForm = () => ({
	id: '',
	name: '',
	price: '',
	stock: '',
	spec: '',
	meetPoint: '',
	spots: '',
	goodsSpecs: '',
	detail: '',
	monthlySales: '',
	desc: '',
	cover: '',
	homeTabKey: ''
});

const form = reactive(emptyForm());
const roomDetail = ref(emptyRoomDetail());
const roomStandards = ref([]);
const roomStandardId = ref('');
const inheritStandard = ref(false);

const selectedStandard = computed(() => {
	if (!roomStandardId.value) return null;
	return roomStandards.value.find((item) => String(item.id) === String(roomStandardId.value)) || null;
});

const displayRoomDetail = computed({
	get() {
		if (inheritStandard.value && selectedStandard.value) {
			return mergeRoomDetailWithStandard(roomDetail.value, selectedStandard.value);
		}
		return roomDetail.value;
	},
	set(value) {
		if (inheritStandard.value && selectedStandard.value) {
			roomDetail.value = extractRoomOverridesFromStandard(value, selectedStandard.value);
			return;
		}
		roomDetail.value = normalizeRoomDetail(value);
	}
});

const roomStandardLabels = computed(() => ['不关联标准', ...roomStandards.value.map((item) => item.name)]);
const roomStandardIndex = computed(() => {
	if (!roomStandardId.value) return 0;
	const index = roomStandards.value.findIndex((item) => String(item.id) === String(roomStandardId.value));
	return index >= 0 ? index + 1 : 0;
});
const selectedRoomStandardLabel = computed(() => selectedStandard.value?.name || '');

const btnPrimary = { flex: 1, height: '48px', borderRadius: '24px', fontWeight: '600' };
const btnPlain = { flex: 1, height: '48px', borderRadius: '24px' };

async function loadRoomStandards() {
	if (storeType.value !== 'hotel') return;
	try {
		const res = await fetchMerchantRoomStandards();
		if (res.code === 0 && Array.isArray(res.data)) {
			roomStandards.value = res.data;
		}
	} catch (e) {
		// 标准列表加载失败不阻塞编辑
	}
}

function onRoomStandardChange(event) {
	const index = Number(event?.detail?.value ?? 0);
	if (index <= 0) {
		roomStandardId.value = '';
		inheritStandard.value = false;
		return;
	}
	const option = roomStandards.value[index - 1];
	roomStandardId.value = option?.id ? String(option.id) : '';
	if (roomStandardId.value && option) {
		inheritStandard.value = true;
		// 绑定标准时重置差异项，避免 emptyRoomDetail 默认值阻断继承
		roomDetail.value = emptyRoomOverride();
	}
}

function onInheritStandardChange(enabled) {
	if (!selectedStandard.value) return;
	if (enabled) {
		roomDetail.value = extractRoomOverridesFromStandard(
			displayRoomDetail.value,
			selectedStandard.value
		);
		return;
	}
	roomDetail.value = normalizeRoomDetail(
		mergeRoomDetailWithStandard(roomDetail.value, selectedStandard.value)
	);
}

async function loadForm() {
	if (!serviceId.value) {
		Object.assign(form, emptyForm());
		form.homeTabKey = resolveHomeTabKey(storeType.value);
		roomDetail.value = emptyRoomOverride();
		roomStandardId.value = '';
		inheritStandard.value = false;
		return;
	}
	try {
		const res = await fetchMerchantServiceDetail(serviceId.value);
		if (res.code === 0 && res.data) {
			const service = res.data;
			Object.assign(form, {
				id: service.id,
				name: service.name || '',
				price: String(service.price ?? ''),
				stock: String(service.stock ?? ''),
				desc: service.desc || '',
				spec: service.spec || '',
				meetPoint: service.meetPoint || '',
				spots: service.spots || '',
				goodsSpecs: service.goodsSpecs || '',
				detail: service.detail || '',
				monthlySales: String(service.monthlySales ?? ''),
				cover: service.cover || '',
				homeTabKey: service.homeTabKey || ''
			});
			roomStandardId.value = service.roomStandardId ? String(service.roomStandardId) : '';
			inheritStandard.value = Boolean(service.inheritStandard);
			const linkedStandard = roomStandards.value.find(
				(item) => String(item.id) === String(service.roomStandardId)
			);
			if (inheritStandard.value && linkedStandard) {
				roomDetail.value = normalizeRoomOverride(service.roomDetail || {});
			} else {
				roomDetail.value = normalizeRoomDetail(service.roomDetail || {});
			}
		}
	} catch (e) {
		uni.showToast({ title: ui.value.networkError, icon: 'none' });
	}
}

function pickCover() {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => { form.cover = res.tempFilePaths[0]; }
	});
}

async function doSave(status) {
	if (!String(form.name || '').trim()) {
		uni.showToast({ title: `请填写${ui.value.serviceName}`, icon: 'none' });
		return;
	}
	const homeTabKey = String(form.homeTabKey || resolveHomeTabKey(storeType.value)).trim();
	submitting.value = true;
	try {
		let coverUrl = form.cover;
		if (coverUrl) {
			coverUrl = await ensureUploadedUrl(coverUrl);
		}
		let roomDetailPayload;
		if (storeType.value === 'hotel') {
			const detail = normalizeRoomDetail(
				inheritStandard.value && selectedStandard.value
					? extractRoomOverridesFromStandard(displayRoomDetail.value, selectedStandard.value)
					: displayRoomDetail.value
			);
			const uploadedGallery = [];
			for (const path of detail.gallery || []) {
				uploadedGallery.push(await ensureUploadedUrl(path));
			}
			detail.gallery = uploadedGallery;
			if (!coverUrl && uploadedGallery.length) {
				coverUrl = uploadedGallery[0];
			}
			roomDetailPayload = buildRoomDetailPayload(detail, {
				inheritStandard: inheritStandard.value && !!roomStandardId.value,
				standard: inheritStandard.value ? selectedStandard.value : null
			});
		}
		const payload = {
			id: form.id || undefined,
			name: String(form.name).trim(),
			price: Number(form.price) || 0,
			stock: Number(form.stock) || 0,
			desc: String(form.desc || '').trim(),
			spec: storeType.value === 'hotel' ? undefined : (String(form.spec || '').trim() || undefined),
			meetPoint: String(form.meetPoint || '').trim() || undefined,
			spots: String(form.spots || '').trim() || undefined,
			goodsSpecs: String(form.goodsSpecs || '').trim() || undefined,
			detail: String(form.detail || '').trim() || undefined,
			roomDetail: roomDetailPayload,
			roomStandardId: roomStandardId.value ? Number(roomStandardId.value) : undefined,
			inheritStandard: inheritStandard.value && !!roomStandardId.value,
			monthlySales: Number(form.monthlySales) || 0,
			cover: coverUrl,
			homeTabKey: homeTabKey || undefined,
			status
		};
		const res = await saveMerchantService(payload);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '保存失败', icon: 'none' });
			return;
		}
		uni.showToast({ title: ui.value.saveSuccess, icon: 'success' });
		setTimeout(() => uni.navigateBack(), 500);
	} catch (e) {
		uni.showToast({ title: e.message || ui.value.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}

function onSaveDraft() {
	doSave(SERVICE_STATUS.DRAFT);
}

function onPublish() {
	doSave(SERVICE_STATUS.ON_SALE);
}

onLoad(async (options) => {
	serviceId.value = options?.id || '';
	await initTheme();
	const title = serviceId.value ? ui.value.serviceEditTitle : ui.value.servicePublishTitle;
	uni.setNavigationBarTitle({ title });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantDepositPaid())) return;
	await initTheme();
	applyNavBarColor();
	await loadRoomStandards();
	if (!formInitialized.value) {
		await loadForm();
		formInitialized.value = true;
	}
});
</script>

<style lang="scss" scoped>
.mse-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding: 20rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.mse-module {
	padding: 28rpx;
	overflow: hidden;

	&__head {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
		margin-bottom: 24rpx;
	}

	&__icon {
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__meta {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		line-height: 1.35;
	}

	&__desc {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.55;
	}

	&__body {
		display: flex;
		flex-direction: column;
	}
}

.mse-inherit-tip {
	display: flex;
	align-items: flex-start;
	gap: 12rpx;
	padding: 20rpx 24rpx;
	font-size: 22rpx;
	line-height: 1.55;
	color: var(--ry-text-secondary);
	background: rgba(41, 121, 255, 0.06);
}

.mse-cover {
	width: 100%;
	height: 280rpx;
	border-radius: 16rpx;
	overflow: hidden;
	background: #F7F8FC;

	&__img { width: 100%; height: 100%; }

	&__empty {
		width: 100%;
		height: 100%;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}
}

.mse-field {
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	margin-bottom: 24rpx;

	&--last { margin-bottom: 0; }

	&__label {
		font-size: 26rpx;
		color: var(--ry-text-primary);
		font-weight: 600;
	}

	&__req { color: #FF4D4F; margin-right: 4rpx; }

	&__input {
		padding: 0 24rpx;
		height: 88rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);

		&--plain { flex: 1; padding-left: 0; background: transparent; height: auto; }
	}

	&__input-wrap {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 0 24rpx;
		height: 88rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
	}

	&__prefix {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__textarea {
		width: 100%;
		min-height: 160rpx;
		padding: 20rpx 24rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);
		box-sizing: border-box;
	}

	&__placeholder { color: #B0B8C8; }

	&__picker {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 24rpx;
		height: 88rpx;
		border-radius: 16rpx;
		background: #F7F8FC;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	&__picker-placeholder {
		color: #B0B8C8;
	}

	&__hint {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	&__link {
		color: #2979FF;
	}
}

.mse-switch-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 24rpx;
	padding: 20rpx 24rpx;
	border-radius: 16rpx;
	background: #F7F8FC;

	&__text {
		flex: 1;
		min-width: 0;
	}
}

.mse-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.96);
	box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);
	display: flex;
	gap: 16rpx;
}
</style>
