<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import HomeBannerPanel from '@/components/business/HomeBannerPanel.vue'
import HomeGridPanel from '@/components/business/HomeGridPanel.vue'
import HomeTabPanel from '@/components/business/HomeTabPanel.vue'

const route = useRoute()
const router = useRouter()
const activeTab = ref('grid')

function syncTabFromRoute() {
  const tab = route.query.tab
  activeTab.value = tab === 'banner' || tab === 'recommend-tab' ? tab : 'grid'
}

onMounted(syncTabFromRoute)

watch(
  () => route.query.tab,
  () => syncTabFromRoute(),
)

watch(activeTab, (tab) => {
  const nextTab = tab === 'banner' || tab === 'recommend-tab' ? tab : undefined
  if ((route.query.tab || undefined) === nextTab) return
  router.replace({
    path: route.path,
    query: nextTab ? { tab: nextTab } : {},
  })
})
</script>

<template>
  <el-card shadow="never" class="page-card home-config">
    <template #header>
      <div class="card-header">
        <div>
          <strong>首页配置</strong>
          <p class="card-desc">维护用户端首页轮播图、金刚区与推荐 Tab，保存后 C 端首页实时生效</p>
        </div>
      </div>
    </template>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="金刚区" name="grid">
        <HomeGridPanel />
      </el-tab-pane>
      <el-tab-pane label="推荐 Tab" name="recommend-tab">
        <HomeTabPanel />
      </el-tab-pane>
      <el-tab-pane label="轮播图" name="banner">
        <HomeBannerPanel />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<style scoped>
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.card-desc {
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.home-config :deep(.embedded-crud) {
  padding-top: 4px;
}

.home-config :deep(.embedded-crud__header) {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.home-config :deep(.filter-form) {
  margin-bottom: 12px;
}
</style>
