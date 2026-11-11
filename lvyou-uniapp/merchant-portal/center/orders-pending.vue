<template>
	<view class="redirect-page" />
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app';
import { getCachedMerchantStoreType } from '@/common/merchant-theme.js';
import { getMerchantBizPath } from '@/common/merchant-biz-routes.js';

onLoad(() => {
	const storeType = getCachedMerchantStoreType() || 'hotel';
	uni.redirectTo({
		url: getMerchantBizPath(storeType, 'orders', { tab: 'pending' })
	});
});
</script>

<style lang="scss" scoped>
.redirect-page { min-height: 100vh; background: var(--ry-page-bg); }
</style>
