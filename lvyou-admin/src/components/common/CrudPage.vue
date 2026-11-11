<script setup>
import { ref } from 'vue'
import AppDialog from '@/components/common/AppDialog.vue'
import BasePagination from '@/components/common/BasePagination.vue'
import TableImageCell from '@/components/common/TableImageCell.vue'
import { useCrudPage } from '@/composables/useCrudPage'
import { validateForm } from '@/utils/form'
import { showError } from '@/utils/modal'
import {
  formatColumnCell,
  getColumnBind,
  shouldRenderImageCell,
} from '@/utils/table-column'

const props = defineProps({
  title: { type: String, required: true },
  api: { type: Object, required: true },
  columns: { type: Array, required: true },
  idKey: { type: String, default: 'id' },
  createDefaults: { type: Object, default: () => ({}) },
  showCreate: { type: Boolean, default: true },
  showEdit: { type: Boolean, default: true },
  showDelete: { type: Boolean, default: true },
  /** 无 info 接口时直接用列表行数据填充编辑表单 */
  editFromRow: { type: Boolean, default: false },
  /** 列表默认查询参数（如房型须带 hotelId） */
  defaultQuery: { type: Object, default: () => ({}) },
  /** 编辑弹窗宽度（行程等复杂表单可加大） */
  dialogWidth: { type: String, default: '680px' },
  /** 提交前组装 payload（如剥离 UI 字段、转换嵌套结构） */
  buildPayload: { type: Function, default: null },
  /** 嵌入 Tab 等容器时使用，不渲染外层 el-card */
  embedded: { type: Boolean, default: false },
})

const formRef = ref()
const {
  loading,
  dialogVisible,
  dialogLoading,
  tableData,
  total,
  pageNum,
  pageSize,
  editingId,
  form,
  loadData,
  searchData,
  openCreate,
  openEdit,
  submitForm,
  removeRow,
} = useCrudPage(props.api, {
  idKey: props.idKey,
  editFromRow: props.editFromRow,
  defaultQuery: props.defaultQuery,
})

function handleCreate() {
  openCreate({ ...props.createDefaults })
}

async function handleSubmit() {
  const valid = await validateForm(formRef)
  if (!valid) return
  let extra = {}
  try {
    if (props.buildPayload) {
      const payload = await props.buildPayload(form, editingId.value)
      extra = { replacePayload: true, payload }
    }
  } catch (error) {
    showError(error?.msg || error?.message || '请完善表单后再提交')
    return
  }
  await submitForm(extra)
}

defineExpose({ loadData, searchData, handleCreate })
</script>

<template>
  <component :is="embedded ? 'div' : 'el-card'" shadow="never" :class="embedded ? 'embedded-crud' : 'page-card'">
    <template v-if="!embedded" #header>
      <div class="card-header">
        <strong>{{ title }}</strong>
        <el-button v-if="showCreate" type="primary" @click="handleCreate">新增</el-button>
      </div>
    </template>
    <div v-if="embedded" class="embedded-crud__header">
      <strong>{{ title }}</strong>
      <el-button v-if="showCreate" type="primary" @click="handleCreate">新增</el-button>
    </div>
    <slot name="toolbar" :load-data="loadData" />
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column
        v-for="column in columns"
        :key="column.prop || column.label"
        v-bind="getColumnBind(column)"
      >
        <template #default="{ row }">
          <slot v-if="$slots[`column-${column.prop}`]" :name="`column-${column.prop}`" :row="row" :column="column" />
          <TableImageCell
            v-else-if="shouldRenderImageCell(column, row[column.prop])"
            :value="row[column.prop]"
            :multiple="column.type === 'images'"
          />
          <span v-else>{{ formatColumnCell(column, row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200" fixed="right">
        <template #default="{ row }">
          <div class="table-actions">
            <slot name="actions" :row="row" :open-edit="() => openEdit(row)" :remove-row="() => removeRow(row)" />
            <el-button v-if="showEdit" size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="showDelete" size="small" type="danger" @click="removeRow(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <BasePagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      @update:current-page="pageNum = $event; loadData()"
      @update:page-size="pageSize = $event; pageNum = 1; loadData()"
    />
    <AppDialog
      v-model="dialogVisible"
      :title="editingId ? `编辑${title}` : `新增${title}`"
      :width="dialogWidth"
      :loading="dialogLoading"
      @confirm="handleSubmit"
    >
      <el-form ref="formRef" :model="form" label-width="110px" class="crud-dialog-form">
        <slot name="form" :form="form" :editing-id="editingId" />
      </el-form>
    </AppDialog>
  </component>
</template>
