<script setup>
import { reactive, ref } from 'vue'
import CrudPage from '@/components/common/CrudPage.vue'
import { messagesApi } from '@/api/modules/messages'

const crudRef = ref()

/** 筛选：接收用户、消息类型 */
const filters = reactive({
  userId: '',
  messageType: '',
})

const TYPE_OPTIONS = [
  { label: '系统公告', value: 1 },
  { label: '用户消息', value: 2 },
]

function formatType(_row, _col, val) {
  const item = TYPE_OPTIONS.find((opt) => opt.value === val)
  return item?.label ?? val ?? '-'
}

function formatUser(row) {
  if (row.userName) {
    return row.phone ? `${row.userName}（${row.phone}）` : row.userName
  }
  return row.userId ?? '-'
}

const columns = [
  { prop: 'title', label: '标题', minWidth: 160, showOverflowTooltip: true },
  { prop: 'content', label: '内容', minWidth: 200, showOverflowTooltip: true },
  { prop: 'type', label: '类型', width: 100, formatter: formatType },
  {
    prop: 'userName',
    label: '用户',
    minWidth: 140,
    showOverflowTooltip: true,
    formatter: (row) => formatUser(row),
  },
  { prop: 'createTime', label: '创建时间', minWidth: 170 },
]

function buildQuery() {
  return {
    userId: filters.userId === '' ? undefined : Number(filters.userId),
    type: filters.messageType === '' ? undefined : Number(filters.messageType),
  }
}

function handleSearch() {
  crudRef.value?.searchData(buildQuery())
}

function handleReset() {
  Object.assign(filters, { userId: '', messageType: '' })
  crudRef.value?.searchData({})
}
</script>

<template>
  <CrudPage
    ref="crudRef"
    title="消息管理"
    :api="messagesApi"
    :columns="columns"
    :create-defaults="{ type: 1, isRead: 0 }"
  >
    <template #toolbar>
      <el-form :inline="true" class="filter-form" @submit.prevent="handleSearch">
        <el-form-item label="用户ID">
          <el-input
            v-model="filters.userId"
            clearable
            placeholder="接收用户ID"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="消息类型">
          <el-select v-model="filters.messageType" clearable placeholder="全部" style="width: 140px">
            <el-option v-for="item in TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </template>

    <template #form="{ form }">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="消息标题" />
      </el-form-item>
      <el-form-item label="内容">
        <el-input v-model="form.content" type="textarea" :rows="4" placeholder="消息正文" />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="form.type" placeholder="选择类型" style="width: 100%">
          <el-option v-for="item in TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="用户ID">
        <el-input-number v-model="form.userId" :min="0" controls-position="right" style="width: 100%" />
      </el-form-item>
    </template>
  </CrudPage>
</template>

<style scoped>
.filter-form {
  margin-bottom: 16px;
}
</style>
