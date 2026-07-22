<script setup>
import { onMounted, ref, computed } from 'vue'
import {
  getAvailableBatches,
  getSubsidyApplies,
  getSubsidyApplyDetail,
  submitSubsidyReview,
  submitTutorSubsidyApply,
  searchStudents
} from '../../api'

const props = defineProps({ userType: Number, menuName: String, user: Object })

const activeTab = ref('review') // 'review' | 'proxy'

// --- Review tab state ---
const applies = ref([])
const loading = ref(false)
const error = ref('')
const totalElements = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const filterBatchId = ref('')
const filterStatus = ref('1') // default: pending counselor
const filterStudentName = ref('')
const batches = ref([])

// Review modal
const reviewTarget = ref(null)
const reviewForm = ref({ action: 1, comment: '', suggestAmount: '' })
const reviewing = ref(false)

// Detail view
const detailTarget = ref(null)

// --- Proxy apply tab state ---
const keyword = ref('')
const studentResults = ref([])
const searching = ref(false)
const selectedStudent = ref(null)
const proxyForm = ref({ batchId: '', applyAmount: '', applyReason: '' })
const proxySaving = ref(false)

const statusLabel = (s) => ({ 1: '待辅导员审核', 2: '待学院审核', 3: '待学校审核', 4: '已通过', 5: '已退回', 6: '不通过' }[s] || '未知')
const statusClass = (s) => {
  if (s >= 1 && s <= 3) return 'status-1'
  if (s === 4) return 'status-0'
  return 'status-2'
}
const applicantLabel = (t) => t === 1 ? '学生自主' : '辅导员代申请'
const actionLabel = (a) => ({ 1: '通过', 2: '退回', 3: '不通过' }[a] || '未知')
const formatMoney = (v) => new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(v)
const formatTime = (t) => t ? t.replace('T', ' ') : '-'

const totalPages = computed(() => Math.max(1, Math.ceil(totalElements.value / pageSize.value)))

onMounted(() => { loadBatches(); loadApplies() })

async function loadBatches() {
  try { batches.value = await getAvailableBatches() } catch (e) { /* ok */ }
}

async function loadApplies() {
  loading.value = true; error.value = ''
  try {
    const data = await getSubsidyApplies({
      batchId: filterBatchId.value || undefined,
      status: filterStatus.value || undefined,
      studentName: filterStudentName.value || undefined,
      page: currentPage.value, size: pageSize.value
    })
    applies.value = data.items || []
    totalElements.value = data.total || 0
  } catch (e) { error.value = e.message } finally { loading.value = false }
}

function prevPage() { if (currentPage.value > 1) { currentPage.value--; loadApplies() } }
function nextPage() { if (currentPage.value < totalPages.value) { currentPage.value++; loadApplies() } }

// --- Review ---

function openReview(apply) {
  reviewTarget.value = apply
  reviewForm.value = { action: 1, comment: '', suggestAmount: apply.applyAmount || '' }
}

async function submitReview() {
  if (reviewForm.value.action === 2 || reviewForm.value.action === 3) {
    if (!reviewForm.value.comment.trim()) { alert('驳回/不通过时必须填写审核意见'); return }
  }
  reviewing.value = true
  try {
    await submitSubsidyReview({
      applyId: reviewTarget.value.id,
      action: reviewForm.value.action,
      comment: reviewForm.value.comment || null,
      suggestAmount: reviewForm.value.action === 1 ? Number(reviewForm.value.suggestAmount) : null
    })
    reviewTarget.value = null
    await loadApplies()
  } catch (e) { alert('操作失败：' + e.message) } finally { reviewing.value = false }
}

async function openDetail(apply) {
  try { detailTarget.value = await getSubsidyApplyDetail(apply.id) } catch (e) { alert('加载详情失败：' + e.message) }
}

// --- Proxy apply ---

async function doSearch() {
  if (!keyword.value.trim()) return
  searching.value = true; selectedStudent.value = null
  try { studentResults.value = await searchStudents(keyword.value.trim()) } catch (e) { alert('搜索失败：' + e.message) } finally { searching.value = false }
}

function selectStudent(s) {
  selectedStudent.value = s
  studentResults.value = []
  keyword.value = s.name + ' (' + s.studentNo + ')'
  proxyForm.value = { batchId: batches.value.length > 0 ? batches.value[0].id : '', applyAmount: '', applyReason: '' }
}

async function submitProxy() {
  if (!selectedStudent.value) { alert('请先搜索并选择一个学生'); return }
  if (!proxyForm.value.applyAmount || Number(proxyForm.value.applyAmount) <= 0) { alert('请输入有效金额'); return }
  if (!proxyForm.value.applyReason.trim()) { alert('请填写申请理由'); return }
  proxySaving.value = true
  try {
    await submitTutorSubsidyApply({
      batchId: Number(proxyForm.value.batchId),
      studentId: selectedStudent.value.studentId,
      applyAmount: Number(proxyForm.value.applyAmount),
      applyReason: proxyForm.value.applyReason
    })
    selectedStudent.value = null
    keyword.value = ''
    proxyForm.value = { batchId: '', applyAmount: '', applyReason: '' }
    alert('代申请提交成功，已直接进入学院审核')
    activeTab.value = 'review'
    await loadApplies()
  } catch (e) { alert('提交失败：' + e.message) } finally { proxySaving.value = false }
}
</script>

<template>
  <div class="view-container">
    <header class="view-header">
      <div>
        <h2 class="view-title">资助审核</h2>
        <p class="subtitle">审核学生补助申请，或为所管学生代发起补助申请</p>
      </div>
    </header>

    <!-- Tabs -->
    <div class="tab-bar">
      <button :class="['tab-btn', { active: activeTab === 'review' }]" @click="activeTab = 'review'">审核学生申请</button>
      <button :class="['tab-btn', { active: activeTab === 'proxy' }]" @click="activeTab = 'proxy'">代学生申请</button>
    </div>

    <!-- === REVIEW TAB === -->
    <template v-if="activeTab === 'review'">
      <div class="filter-bar">
        <select v-model="filterBatchId" @change="currentPage = 1; loadApplies()">
          <option value="">全部批次</option>
          <option v-for="b in batches" :key="b.id" :value="b.id">{{ b.batchName }}</option>
        </select>
        <select v-model="filterStatus" @change="currentPage = 1; loadApplies()">
          <option value="">全部状态</option>
          <option :value="1">待辅导员审核</option>
          <option :value="2">待学院审核</option>
          <option :value="3">待学校审核</option>
          <option :value="4">已通过</option>
          <option :value="5">已退回</option>
          <option :value="6">不通过</option>
        </select>
        <input v-model.trim="filterStudentName" placeholder="搜索学生姓名/学号…" @keyup.enter="currentPage=1;loadApplies()" style="min-width:180px;" />
        <button class="secondary" @click="currentPage=1;loadApplies()">查询</button>
      </div>
      <p v-if="error" class="error-message">{{ error }}</p>
      <div class="table-card">
        <table class="data-table">
          <thead><tr><th>申请编号</th><th>学生</th><th>学号</th><th>批次</th><th>金额</th><th>发起方</th><th>状态</th><th>申请时间</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-if="loading"><td colspan="9" class="center-text">加载中…</td></tr>
            <tr v-else-if="!applies.length"><td colspan="9" class="center-text">暂无申请记录</td></tr>
            <tr v-for="a in applies" :key="a.id" v-else>
              <td>{{ a.applyNo }}</td>
              <td>{{ a.studentName }}</td>
              <td>{{ a.studentNo }}</td>
              <td>{{ a.batchName }}</td>
              <td>{{ formatMoney(a.applyAmount) }}</td>
              <td>{{ applicantLabel(a.applicantType) }}</td>
              <td><span class="status-badge" :class="statusClass(a.status)">{{ statusLabel(a.status) }}</span></td>
              <td>{{ formatTime(a.applyTime) }}</td>
              <td>
                <button class="text-btn" @click="openDetail(a)">详情</button>
                <button v-if="a.status === 1 && a.applicantType !== 2" class="text-btn edit-btn" @click="openReview(a)">审核</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="totalElements > 0" class="pagination">
        <button :disabled="currentPage <= 1" @click="prevPage">上一页</button>
        <span>第 {{ currentPage }} / {{ totalPages }} 页（{{ totalElements }} 条）</span>
        <button :disabled="currentPage >= totalPages" @click="nextPage">下一页</button>
      </div>
    </template>

    <!-- === PROXY TAB === -->
    <template v-if="activeTab === 'proxy'">
      <div class="proxy-panel">
        <div class="search-section">
          <h3>选择学生</h3>
          <div class="search-row">
            <input v-model.trim="keyword" placeholder="输入学生学号或姓名搜索…" @keyup.enter="doSearch" style="flex:1;" />
            <button :disabled="searching" @click="doSearch">{{ searching ? '搜索中…' : '搜索' }}</button>
          </div>
          <div v-if="studentResults.length" class="student-results">
            <div v-for="s in studentResults" :key="s.studentId" class="student-card" @click="selectStudent(s)">
              <strong>{{ s.name }}</strong> {{ s.studentNo }} · {{ s.collegeName }} {{ s.grade ? s.grade + '级' : '' }}
            </div>
          </div>
          <p v-if="selectedStudent" class="selected-info">
            已选择：<strong>{{ selectedStudent.name }}</strong>（{{ selectedStudent.studentNo }}）
            <button class="text-btn" @click="selectedStudent = null; keyword = ''">清除</button>
          </p>
        </div>

        <div v-if="selectedStudent" class="proxy-form-section">
          <h3>填写申请信息</h3>
          <form @submit.prevent="submitProxy">
            <label>补助批次
              <select v-model="proxyForm.batchId" required>
                <option value="">请选择批次</option>
                <option v-for="b in batches" :key="b.id" :value="b.id">{{ b.batchName }}</option>
              </select>
            </label>
            <label>申请金额 (元)
              <input type="number" v-model="proxyForm.applyAmount" min="0.01" step="0.01" required placeholder="请输入金额" />
            </label>
            <label>申请理由
              <textarea v-model="proxyForm.applyReason" required placeholder="详细说明申请理由…" rows="4"></textarea>
            </label>
            <p class="hint">提交后将直接进入学院审核，无需辅导员再次审批。</p>
            <button type="submit" :disabled="proxySaving">{{ proxySaving ? '提交中…' : '确认代学生提交' }}</button>
          </form>
        </div>
      </div>
    </template>

    <!-- Review modal -->
    <div v-if="reviewTarget" class="modal-overlay" @click.self="reviewTarget = null">
      <div class="modal-card">
        <h3>审核申请</h3>
        <p><strong>学生：</strong>{{ reviewTarget.studentName }}（{{ reviewTarget.studentNo }}）</p>
        <p><strong>批次：</strong>{{ reviewTarget.batchName }}</p>
        <p><strong>申请金额：</strong>{{ formatMoney(reviewTarget.applyAmount) }}</p>
        <p><strong>理由：</strong>{{ reviewTarget.applyReason }}</p>
        <form @submit.prevent="submitReview">
          <label>审核动作
            <select v-model="reviewForm.action" required>
              <option :value="1">通过</option>
              <option :value="2">退回修改</option>
              <option :value="3">不通过</option>
            </select>
          </label>
          <label>建议金额 (元)
            <input type="number" v-model="reviewForm.suggestAmount" min="0.01" step="0.01" placeholder="填写建议补助金额" />
          </label>
          <label>审核意见
            <textarea v-model="reviewForm.comment" placeholder="审核意见…" rows="3"></textarea>
          </label>
          <div class="modal-actions">
            <button type="button" class="secondary" @click="reviewTarget = null">取消</button>
            <button type="submit" :disabled="reviewing">{{ reviewing ? '提交中…' : '确认' }}</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Detail modal -->
    <div v-if="detailTarget" class="modal-overlay" @click.self="detailTarget = null">
      <div class="modal-card" style="max-width: 620px;">
        <h3>申请详情</h3>
        <div class="detail-grid">
          <div><strong>编号：</strong>{{ detailTarget.applyNo }}</div>
          <div><strong>批次：</strong>{{ detailTarget.batchName }}</div>
          <div><strong>学生：</strong>{{ detailTarget.studentName }}</div>
          <div><strong>学院：</strong>{{ detailTarget.collegeName }}</div>
          <div><strong>金额：</strong>{{ formatMoney(detailTarget.applyAmount) }}</div>
          <div><strong>发起：</strong>{{ applicantLabel(detailTarget.applicantType) }}</div>
        </div>
        <h4 style="margin: 20px 0 12px;">审核时间线</h4>
        <div v-if="!detailTarget.reviews || !detailTarget.reviews.length" style="color:#999;text-align:center;padding:16px;">暂无</div>
        <div v-else class="timeline">
          <div v-for="r in detailTarget.reviews" :key="r.id" class="timeline-item">
            <div class="timeline-dot" :class="'action-' + r.action"></div>
            <div class="timeline-body">
              <strong>{{ r.reviewerRoleName }} · {{ r.reviewerName }}</strong>
              <span class="timeline-action" :class="'act-' + r.action">{{ r.actionName }}</span>
              <p v-if="r.suggestAmount" style="margin:4px 0 0;">金额：{{ formatMoney(r.suggestAmount) }}</p>
              <p v-if="r.comment" style="margin:4px 0 0;color:#668077;">{{ r.comment }}</p>
              <small style="color:#999;">{{ formatTime(r.reviewTime) }}</small>
            </div>
          </div>
        </div>
        <div class="modal-actions" style="margin-top:20px;">
          <button class="secondary" @click="detailTarget = null">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { display: flex; flex-direction: column; gap: 24px; }
.view-header { display: flex; justify-content: space-between; align-items: flex-end; }
.view-title { margin: 0 0 8px; font-size: 24px; }
.subtitle { margin: 0; color: #668077; font-size: 14px; }
.tab-bar { display: flex; gap: 0; border-bottom: 2px solid #edf4ef; }
.tab-btn { padding: 12px 24px; border: none; background: transparent; color: #668077; font-weight: 600; font-size: 15px; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: .18s; }
.tab-btn.active { color: #217a58; border-bottom-color: #217a58; }
.tab-btn:hover:not(.active) { color: #16352c; }

.filter-bar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.filter-bar input, .filter-bar select { border: 1px solid #cbdad0; border-radius: 12px; padding: 10px 14px; outline: none; background: #fbfdfb; font: inherit; }
.filter-bar input:focus, .filter-bar select:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33,122,88,.12); }
.error-message { color: #b53f3f; margin: 0; font-size: 14px; }
.center-text { text-align: center; color: #999; padding: 32px !important; }
.table-card { background: #fff; border-radius: 18px; padding: 20px; box-shadow: 0 4px 12px rgba(27,74,57,.04); overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 14px; }
.data-table th { padding: 14px 16px; color: #50816d; font-weight: 600; border-bottom: 2px solid #edf4ef; white-space: nowrap; }
.data-table td { padding: 16px; border-bottom: 1px solid #edf4ef; color: #16352c; }
.data-table tbody tr:hover { background: #fbfdfb; }
.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.status-0 { background: #e0e0e0; color: #555; }
.status-1 { background: #e7f1eb; color: #217a58; }
.status-2 { background: #fce8e8; color: #b53f3f; }
.text-btn { background: transparent; border: none; color: #217a58; padding: 0 8px; font-weight: 600; font-size: 13px; cursor: pointer; }
.text-btn:hover { text-decoration: underline; }
.edit-btn { color: #668077; }
.pagination { display: flex; justify-content: center; align-items: center; gap: 16px; font-size: 14px; color: #50816d; }
.pagination button { padding: 8px 16px; border-radius: 10px; border: 1px solid #cbdad0; background: #fff; cursor: pointer; font: inherit; }
.pagination button:disabled { opacity: .4; cursor: not-allowed; }

.proxy-panel { display: grid; gap: 24px; }
.search-section h3, .proxy-form-section h3 { margin: 0 0 16px; font-size: 18px; color: #16352c; }
.search-row { display: flex; gap: 12px; }
.search-row input, .search-row button { border: 1px solid #cbdad0; border-radius: 12px; padding: 12px 14px; outline: none; background: #fbfdfb; font: inherit; }
.search-row input:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33,122,88,.12); }
.student-results { margin-top: 12px; display: flex; flex-direction: column; gap: 8px; }
.student-card { padding: 12px 16px; border: 1px solid #cbdad0; border-radius: 12px; cursor: pointer; background: #fbfdfb; font-size: 14px; transition: .15s; }
.student-card:hover { border-color: #217a58; background: #e7f1eb; }
.selected-info { margin-top: 12px; padding: 12px; background: #e7f1eb; border-radius: 12px; font-size: 14px; }
.proxy-form-section form { display: grid; gap: 16px; }
.proxy-form-section label { display: block; color: #50816d; font-size: 13px; font-weight: 600; }
.proxy-form-section input, .proxy-form-section select, .proxy-form-section textarea { width: 100%; border: 1px solid #cbdad0; border-radius: 12px; padding: 12px 14px; outline: none; background: #fbfdfb; font: inherit; margin-top: 6px; box-sizing: border-box; }
.proxy-form-section input:focus, .proxy-form-section select:focus, .proxy-form-section textarea:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33,122,88,.12); }
.proxy-form-section textarea { resize: vertical; }
.hint { padding: 10px 14px; background: #fef5e7; border-radius: 10px; color: #b07828; font-size: 13px; margin: 0; }
.proxy-form-section button[type="submit"] { border: 0; border-radius: 12px; padding: 14px 20px; color: white; background: #217a58; cursor: pointer; font-weight: 700; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); display: grid; place-items: center; z-index: 100; backdrop-filter: blur(4px); }
.modal-card { background: #fff; width: 100%; max-width: 520px; padding: 32px; border-radius: 24px; box-shadow: 0 24px 80px rgba(0,0,0,.2); max-height: 90vh; overflow-y: auto; }
.modal-card h3 { margin: 0 0 16px; font-size: 20px; }
.modal-card p { margin: 4px 0; font-size: 14px; }
.modal-card label { display: block; margin-bottom: 16px; color: #50816d; font-size: 13px; font-weight: 600; }
.modal-card input, .modal-card select, .modal-card textarea { width: 100%; border: 1px solid #cbdad0; border-radius: 12px; padding: 12px 14px; outline: none; background: #fbfdfb; font: inherit; margin-top: 6px; box-sizing: border-box; }
.modal-card input:focus, .modal-card select:focus, .modal-card textarea:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33,122,88,.12); }
.modal-card textarea { resize: vertical; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; font-size: 14px; }
h4 { margin: 0; font-size: 16px; color: #50816d; }
.timeline { position: relative; padding-left: 24px; }
.timeline::before { content: ''; position: absolute; left: 7px; top: 0; bottom: 0; width: 2px; background: #dcebe0; }
.timeline-item { position: relative; margin-bottom: 16px; display: flex; gap: 12px; }
.timeline-dot { width: 16px; height: 16px; border-radius: 50%; flex-shrink: 0; margin-top: 2px; background: #217a58; }
.timeline-dot.action-2 { background: #f0a040; }
.timeline-dot.action-3 { background: #b53f3f; }
.timeline-body { font-size: 14px; }
.timeline-action { padding: 2px 8px; border-radius: 10px; font-size: 12px; font-weight: 600; margin-left: 8px; }
.act-1 { background: #e7f1eb; color: #217a58; }
.act-2 { background: #fef5e7; color: #b07828; }
.act-3 { background: #fce8e8; color: #b53f3f; }
input[type='number']::-webkit-inner-spin-button,
input[type='number']::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
input[type='number'] { -moz-appearance: textfield; }
</style>
