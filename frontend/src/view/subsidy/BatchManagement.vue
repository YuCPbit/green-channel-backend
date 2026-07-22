<script setup>
import { computed, onMounted, ref } from 'vue'
import { getSubsidyBatches, createSubsidyBatch, updateSubsidyBatch, startSubsidyBatch, endSubsidyBatch } from '../../api'

// 批次列表与分页
const batches = ref([])
const loading = ref(false)
const error = ref('')
const totalElements = ref(0)
const currentPage = ref(0)
const pageSize = ref(10)

// 筛选条件
const filterName = ref('')
const filterStatus = ref('')

// 创建/编辑弹窗
const showModal = ref(false)
const editTarget = ref(null) // null = 创建模式；否则为编辑的批次对象
const form = ref({ batchName: '', academicYear: '', subsidyType: 1, totalAmount: '', applyStartTime: '', applyEndTime: '', collegeSubmitEndTime: '' })
const saving = ref(false)

const statusLabel = (status) => {
  if (status === 0) return '未开始'
  if (status === 1) return '进行中'
  if (status === 2) return '已结束'
  return '未知'
}

const subsidyTypeLabel = (type) => {
  if (type === 1) return '集中批次'
  if (type === 2) return '动态-大病补助'
  if (type === 3) return '动态-受灾补助'
  if (type === 4) return '动态-其他补助'
  return '未知'
}

const isDynamicType = (type) => type === 2 || type === 3 || type === 4

const totalPages = computed(() => Math.max(1, Math.ceil(totalElements.value / pageSize.value)))

async function loadBatches() {
  loading.value = true
  error.value = ''
  try {
    const data = await getSubsidyBatches({
      batchName: filterName.value || undefined,
      status: filterStatus.value,
      page: currentPage.value,
      size: pageSize.value
    })
    // Spring Data Page 结构: { content, totalElements, totalPages, number, size }
    batches.value = data.content || []
    totalElements.value = data.totalElements || 0
  } catch (e) {
    error.value = e.message
    batches.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.value = { batchName: '', academicYear: '', subsidyType: 1, totalAmount: '', applyStartTime: '', applyEndTime: '', collegeSubmitEndTime: '' }
  editTarget.value = null
}

function openCreate() {
  resetForm()
  showModal.value = true
}

function openEdit(batch) {
  editTarget.value = batch
  form.value = {
    batchName: batch.batchName || '',
    academicYear: batch.academicYear || '',
    subsidyType: batch.subsidyType || 1,
    totalAmount: batch.totalAmount != null ? String(batch.totalAmount) : '',
    applyStartTime: batch.applyStartTime ? batch.applyStartTime.slice(0, 16) : '',
    applyEndTime: batch.applyEndTime ? batch.applyEndTime.slice(0, 16) : '',
    collegeSubmitEndTime: batch.collegeSubmitEndTime ? batch.collegeSubmitEndTime.slice(0, 16) : ''
  }
  showModal.value = true
}

function toLocalDateTime(dateStr) {
  if (!dateStr) return dateStr
  // 如果是 "yyyy-MM-ddTHH:mm" 格式，补充秒
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(dateStr)) {
    return dateStr + ':00'
  }
  return dateStr
}

async function submitBatch() {
  saving.value = true
  try {
    const payload = {
      batchName: form.value.batchName,
      academicYear: form.value.academicYear,
      subsidyType: form.value.subsidyType,
      totalAmount: form.value.totalAmount ? Number(form.value.totalAmount) : 0,
      applyStartTime: toLocalDateTime(form.value.applyStartTime),
      applyEndTime: toLocalDateTime(form.value.applyEndTime),
      collegeSubmitEndTime: toLocalDateTime(form.value.collegeSubmitEndTime)
    }
    if (editTarget.value) {
      await updateSubsidyBatch(editTarget.value.id, payload)
    } else {
      await createSubsidyBatch(payload)
    }
    showModal.value = false
    resetForm()
    await loadBatches()
  } catch (e) {
    alert('操作失败：' + e.message)
  } finally {
    saving.value = false
  }
}

async function handleStart(batch) {
  if (!confirm(`确定要开始批次「${batch.batchName}」吗？开始后学院即可向年级分配额度。`)) return
  try {
    await startSubsidyBatch(batch.id)
    await loadBatches()
  } catch (e) {
    alert('操作失败：' + e.message)
  }
}

async function handleEnd(batch) {
  if (!confirm(`确定要提前结束批次「${batch.batchName}」吗？结束后将无法再进行额度分配。`)) return
  try {
    await endSubsidyBatch(batch.id)
    await loadBatches()
  } catch (e) {
    alert('操作失败：' + e.message)
  }
}

function prevPage() {
  if (currentPage.value > 0) {
    currentPage.value--
    loadBatches()
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadBatches()
  }
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ')
}

const formatMoney = (val) => {
  const num = Number(val) || 0
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(num)
}

onMounted(() => {
  loadBatches()
})
</script>

<template>
  <div class="view-container">
    <header class="view-header">
      <div>
        <h2 class="view-title">补助批次管理</h2>
        <p class="subtitle">统一配置全校补助批次的时间与规则</p>
      </div>
      <button class="primary-btn" @click="openCreate">+ 创建批次</button>
    </header>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <input
        v-model.trim="filterName"
        placeholder="批次名称搜索…"
        @keyup.enter="currentPage = 0; loadBatches()"
      />
      <select v-model="filterStatus" @change="currentPage = 0; loadBatches()">
        <option value="">全部状态</option>
        <option value="0">未开始</option>
        <option value="1">进行中</option>
        <option value="2">已结束</option>
      </select>
      <button class="secondary" @click="currentPage = 0; loadBatches()">查询</button>
    </div>

    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="table-card">
      <table class="data-table">
        <thead>
          <tr>
            <th>批次名称</th>
            <th>学年</th>
            <th>批次类型</th>
            <th>总金额</th>
            <th>申请开始时间</th>
            <th>申请截止时间</th>
            <th>学院提交截止</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="9" class="center-text">加载中…</td>
          </tr>
          <tr v-else-if="!batches.length">
            <td colspan="9" class="center-text">暂无批次数据</td>
          </tr>
          <tr v-for="batch in batches" :key="batch.id" v-else>
            <td>{{ batch.batchName }}</td>
            <td>{{ batch.academicYear }}</td>
            <td>{{ subsidyTypeLabel(batch.subsidyType) }}</td>
            <td>{{ formatMoney(batch.totalAmount) }}</td>
            <td>{{ formatTime(batch.applyStartTime) }}</td>
            <td>{{ formatTime(batch.applyEndTime) }}</td>
            <td>{{ formatTime(batch.collegeSubmitEndTime) }}</td>
            <td>
              <span class="status-badge" :class="'status-' + batch.status">
                {{ statusLabel(batch.status) }}
              </span>
            </td>
            <td>
              <button v-if="batch.status === 0" class="text-btn" @click="handleStart(batch)">开始</button>
              <button v-if="batch.status === 1" class="text-btn" @click="handleEnd(batch)">提前结束</button>
              <button v-if="batch.status !== 2" class="text-btn edit-btn" @click="openEdit(batch)">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 分页 -->
    <div v-if="totalElements > 0" class="pagination">
      <button :disabled="currentPage === 0" @click="prevPage">上一页</button>
      <span>第 {{ currentPage + 1 }} / {{ totalPages }} 页（{{ totalElements }} 条）</span>
      <button :disabled="currentPage >= totalPages - 1" @click="nextPage">下一页</button>
    </div>

    <!-- 创建/编辑弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <h3>{{ editTarget ? '编辑补助批次' : '创建补助批次' }}</h3>
        <form @submit.prevent="submitBatch">
          <label>批次名称
            <input v-model="form.batchName" required placeholder="如：2026年秋季困难补助" />
          </label>
          <label>学年
            <input v-model="form.academicYear" required placeholder="如：2026-2027" />
          </label>
          <div class="form-row">
            <label>批次类型
              <select v-model="form.subsidyType" required>
                <option :value="1">集中批次（每学年1次）</option>
                <option :value="2">动态批次 - 大病补助</option>
                <option :value="3">动态批次 - 受灾补助</option>
                <option :value="4">动态批次 - 其他补助</option>
              </select>
            </label>
            <label>总金额 (元)
              <input type="number" v-model="form.totalAmount" min="0" step="0.01" placeholder="全校总资金盘" />
            </label>
          </div>
          <div class="form-row">
            <label>申请开始时间
              <input type="datetime-local" v-model="form.applyStartTime" required />
            </label>
            <label>申请截止时间
              <input type="datetime-local" v-model="form.applyEndTime" required />
            </label>
          </div>
          <label>学院提交截止时间
            <input type="datetime-local" v-model="form.collegeSubmitEndTime" required />
          </label>
          <div class="modal-actions">
            <button type="button" class="secondary" @click="showModal = false">取消</button>
            <button type="submit" :disabled="saving">{{ saving ? '保存中…' : '确认保存' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { display: flex; flex-direction: column; gap: 24px; }
.view-header { display: flex; justify-content: space-between; align-items: flex-end; }
.view-title { margin: 0 0 8px; font-size: 24px; }
.subtitle { margin: 0; color: #668077; font-size: 14px; }

/* 筛选栏 */
.filter-bar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.filter-bar input { flex: 1; min-width: 180px; border: 1px solid #cbdad0; border-radius: 12px; padding: 10px 14px; outline: none; background: #fbfdfb; font: inherit; }
.filter-bar input:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33, 122, 88, .12); }
.filter-bar select { border: 1px solid #cbdad0; border-radius: 12px; padding: 10px 14px; outline: none; background: #fbfdfb; font: inherit; }
.filter-bar select:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33, 122, 88, .12); }

.error-message { color: #b53f3f; margin: 0; font-size: 14px; }
.center-text { text-align: center; color: #999; padding: 32px !important; }

/* 分页 */
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; font-size: 14px; color: #50816d; }
.pagination button { padding: 8px 16px; border-radius: 10px; border: 1px solid #cbdad0; background: #fff; cursor: pointer; font: inherit; }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }

/* 表格卡片样式 */
.table-card { background: #fff; border-radius: 18px; padding: 20px; box-shadow: 0 4px 12px rgba(27,74,57,.04); overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 14px; }
.data-table th { padding: 14px 16px; color: #50816d; font-weight: 600; border-bottom: 2px solid #edf4ef; white-space: nowrap; }
.data-table td { padding: 16px; border-bottom: 1px solid #edf4ef; color: #16352c; }
.data-table tbody tr:hover { background: #fbfdfb; }

/* 状态标签 */
.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.status-0 { background: #e0e0e0; color: #555; }
.status-1 { background: #e7f1eb; color: #217a58; }
.status-2 { background: #fce8e8; color: #b53f3f; }

/* 按钮 */
.text-btn { background: transparent; border: none; color: #217a58; padding: 0 8px; font-weight: 600; font-size: 13px; cursor: pointer; }
.text-btn:hover { text-decoration: underline; }
.edit-btn { color: #668077; }

/* 弹窗样式 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); display: grid; place-items: center; z-index: 100; backdrop-filter: blur(4px); }
.modal-card { background: #fff; width: 100%; max-width: 520px; padding: 32px; border-radius: 24px; box-shadow: 0 24px 80px rgba(0,0,0,.2); max-height: 90vh; overflow-y: auto; }
.modal-card h3 { margin: 0 0 24px; font-size: 20px; }
.modal-card label { display: block; margin-bottom: 16px; color: #50816d; font-size: 13px; font-weight: 600; }
.modal-card input,
.modal-card select { width: 100%; border: 1px solid #cbdad0; border-radius: 12px; padding: 12px 14px; outline: none; background: #fbfdfb; font: inherit; margin-top: 6px; box-sizing: border-box; }
.modal-card input:focus,
.modal-card select:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33, 122, 88, .12); }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }

/* 隐藏 number 输入框的上下箭头 */
input[type='number']::-webkit-inner-spin-button,
input[type='number']::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
input[type='number'] { -moz-appearance: textfield; }
</style>
