<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { getSubsidyBatches, getAllocationSummary, getAllocationList, allocateQuota, getColleges, getGrades } from '../../api'

const props = defineProps({
  userType: { type: Number, default: 4 },
  menuName: { type: String, default: '' },
  user: { type: Object, default: () => ({}) }
})

// ---- 视图模式 ----
const isSchool = computed(() => props.userType === 4)   // 资金管理
const isCollege = computed(() => props.userType === 3)  // 额度管理

const viewTitle = computed(() => isSchool.value ? '资金管理看板' : '学院额度管理')
const viewSubtitle = computed(() =>
  isSchool.value
    ? '实时监控全校资金池，向各学院下发额度'
    : '查看本院可用额度并向各年级分配'
)

// ---- 批次选择 ----
const batches = ref([])
const selectedBatchId = ref(null)

// ---- 汇总数据 ----
const loading = ref(false)
const error = ref('')
const summary = ref({ totalAmount: 0, allocatedAmount: 0, availableAmount: 0 })

// ---- 分配明细 ----
const allocationItems = ref([])  // { targetId, amount } 的映射

// ---- 分配弹窗 ----
const showModal = ref(false)
const form = ref({ targetId: null, targetName: '', amount: '' })
const saving = ref(false)

// 学院/年级选项（从接口获取）
const collegeOptions = ref([])
const gradeOptions = ref([])

const currentBatchStatus = computed(() => {
  const batch = batches.value.find(b => b.id === selectedBatchId.value)
  return batch ? batch.status : null
})

const targetLabel = computed(() => (isSchool.value ? '目标学院' : '目标年级'))

// ---- 格式化 ----
const formatMoney = (val) => {
  const num = Number(val) || 0
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(num)
}

// ---- 分配金额上限 ----
const maxAllocationAmount = computed(() => {
  // 学校：新额度 ≤ 可用余额 + 当前已分配（未分配过则为可用余额）
  if (isSchool.value) {
    const currentAmount = getTargetAmount(form.value.targetId)
    return summary.value.availableAmount + currentAmount
  }
  // 学院在批次未开始(DRAFT=0)时：可分配至学院总额度，不受可用余额限制
  if (currentBatchStatus.value === 0) return summary.value.totalAmount
  // 学院在批次开始后：只能分配可用余额
  return summary.value.availableAmount
})

const minAllocationAmount = computed(() => {
  const currentAmount = getTargetAmount(form.value.targetId)
  // 学校：一旦分配不可减少
  if (isSchool.value) return Math.max(0.01, currentAmount)
  // 学院 DRAFT：可自由调整
  if (currentBatchStatus.value === 0) return 0.01
  // 学院 ACTIVE/ENDED：不可减少已下发额度
  return Math.max(0.01, currentAmount)
})

// ---- 加载批次列表 ----
async function loadBatches() {
  try {
    const data = await getSubsidyBatches({ size: 50 })
    batches.value = (data && data.content) ? data.content : []
    if (batches.value.length && !selectedBatchId.value) {
      selectedBatchId.value = batches.value[0].id
    }
  } catch (e) {
    console.error('加载批次列表失败', e)
  }
}

// ---- 加载学院和年级列表 ----
async function loadCollegeOptions() {
  try {
    const data = await getColleges()
    collegeOptions.value = (data || []).map(c => ({ id: c.id, name: c.collegeName }))
  } catch (e) {
    console.error('加载学院列表失败', e)
  }
}

async function loadGradeOptions() {
  try {
    const data = await getGrades()
    gradeOptions.value = (data || []).map(g => ({ id: g, name: g + '级' }))
  } catch (e) {
    console.error('加载年级列表失败', e)
  }
}

// ---- 加载汇总 ----
async function loadSummary() {
  if (!selectedBatchId.value) return
  loading.value = true
  error.value = ''
  try {
    const data = await getAllocationSummary(selectedBatchId.value, {
      userType: props.userType,
      collegeId: props.user?.collegeId
    })
    summary.value = {
      totalAmount: data.totalAmount || 0,
      allocatedAmount: data.allocatedAmount || 0,
      availableAmount: data.availableAmount || 0
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

// ---- 加载分配明细 ----
async function loadAllocations() {
  if (!selectedBatchId.value) return
  try {
    const targetType = isSchool.value ? 1 : 2
    allocationItems.value = await getAllocationList(selectedBatchId.value, targetType)
  } catch (e) {
    console.error('加载分配明细失败', e)
    allocationItems.value = []
  }
}

function getTargetAmount(targetId) {
  const item = allocationItems.value.find(a => a.targetId === targetId)
  return item ? item.amount : 0
}

// ---- 分配操作 ----
async function submitAllocation() {
  if (!form.value.targetId || !form.value.amount) {
    alert('请完整填写目标对象和金额')
    return
  }
  if (Number(form.value.amount) <= 0) {
    alert('金额必须大于0')
    return
  }
  saving.value = true
  try {
    await allocateQuota(
      {
        batchId: selectedBatchId.value,
        targetType: isSchool.value ? 1 : 2, // 1=学院 2=年级
        targetId: Number(form.value.targetId),
        amount: Number(form.value.amount)
      },
      {
        userType: props.userType,
        id: props.user?.id,
        collegeId: props.user?.collegeId
      }
    )
    showModal.value = false
    await Promise.all([loadSummary(), loadAllocations()])
  } catch (e) {
    alert('分配失败：' + e.message)
  } finally {
    saving.value = false
  }
}

// ---- 生命周期 ----
onMounted(async () => {
  await loadBatches()
  await Promise.all([loadSummary(), loadAllocations(), loadCollegeOptions(), loadGradeOptions()])
})

watch(selectedBatchId, () => {
  Promise.all([loadSummary(), loadAllocations()])
})
</script>

<template>
  <div class="view-container">
    <header class="view-header">
      <div>
        <h2 class="view-title">{{ viewTitle }}</h2>
        <p class="subtitle">{{ viewSubtitle }}</p>
      </div>
      <div class="header-right">
        <select v-model="selectedBatchId" class="batch-select">
          <option v-for="b in batches" :key="b.id" :value="b.id">{{ b.batchName }}</option>
        </select>
      </div>
    </header>

    <p v-if="error" class="error-message">{{ error }}</p>

    <!-- 核心指标 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <span class="eyebrow">{{ isSchool ? '全校总额度' : '学院总额度' }}</span>
        <div class="value">{{ formatMoney(summary.totalAmount) }}</div>
      </div>
      <div class="metric-card">
        <span class="eyebrow">已下发额度</span>
        <div class="value text-warning">{{ formatMoney(summary.allocatedAmount) }}</div>
      </div>
      <div class="metric-card highlight">
        <span class="eyebrow">当前可用余额</span>
        <div class="value text-success">{{ formatMoney(summary.availableAmount) }}</div>
      </div>
    </div>

    <!-- 分配明细表 -->
    <div class="table-card">
      <div v-if="loading" class="center-text">加载中…</div>
      <div v-else>
        <!-- 学校视图：列出各学院 -->
        <template v-if="isSchool">
          <table class="data-table">
            <thead>
              <tr>
                <th>学院</th>
                <th>分配总额</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!collegeOptions.length">
                <td colspan="3" class="center-text">暂无学院数据</td>
              </tr>
              <tr v-for="college in collegeOptions" :key="college.id">
                <td><strong>{{ college.name }}</strong></td>
                <td>{{ formatMoney(getTargetAmount(college.id)) }}</td>
                <td>
                  <button class="text-btn" @click="form.targetId = college.id; form.targetName = college.name; showModal = true">
                    下发额度
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </template>

        <!-- 学院视图：列出各年级 -->
        <template v-if="isCollege">
          <table class="data-table">
            <thead>
              <tr>
                <th>年级</th>
                <th>分配总额</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!gradeOptions.length">
                <td colspan="3" class="center-text">暂无年级数据</td>
              </tr>
              <tr v-for="grade in gradeOptions" :key="grade.id">
                <td><strong>{{ grade.name }}</strong></td>
                <td>{{ formatMoney(getTargetAmount(grade.id)) }}</td>
                <td>
                  <button class="text-btn" @click="form.targetId = grade.id; form.targetName = grade.name; showModal = true">
                    下发额度
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </template>
      </div>
    </div>

    <!-- 下发额度弹窗 -->
    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal-card">
        <h3>下发额度</h3>
        <p class="modal-subtitle">当前可用余额：<strong>{{ formatMoney(summary.availableAmount) }}</strong></p>
        <form @submit.prevent="submitAllocation">
          <p class="target-display">{{ targetLabel }}：<strong>{{ form.targetName }}</strong></p>
          <label>下拨金额 (元)
            <input
              type="number"
              v-model="form.amount"
              required
              :min="minAllocationAmount"
              step="0.01"
              :max="maxAllocationAmount"
              placeholder="请输入金额"
            />
          </label>
          <div class="modal-actions">
            <button type="button" class="secondary" @click="showModal = false">取消</button>
            <button type="submit" :disabled="saving">{{ saving ? '提交中…' : '确认下发' }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.view-container { display: flex; flex-direction: column; gap: 24px; }
.view-header { display: flex; justify-content: space-between; align-items: flex-end; flex-wrap: wrap; gap: 12px; }
.view-title { margin: 0 0 8px; font-size: 24px; }
.subtitle { margin: 0; color: #668077; font-size: 14px; }

.header-right { display: flex; gap: 12px; align-items: center; }
.batch-select {
  border: 1px solid #cbdad0; border-radius: 12px; padding: 10px 14px;
  outline: none; background: #fbfdfb; font: inherit;
}
.batch-select:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33, 122, 88, .12); }

.error-message { color: #b53f3f; margin: 0; font-size: 14px; }
.center-text { text-align: center; color: #999; padding: 32px !important; }

/* 核心指标卡片 */
.metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 16px; }
.metric-card { background: #fff; padding: 24px; border-radius: 18px; box-shadow: 0 4px 12px rgba(27,74,57,.04); border: 1px solid rgba(25, 89, 66, .05); }
.metric-card.highlight { background: #f1f7f3; border-color: #cbdad0; }
.metric-card .eyebrow { color: #50816d; font-size: 13px; font-weight: 700; letter-spacing: .1em; display: block; margin-bottom: 8px; }
.metric-card .value { font-size: 28px; font-weight: 700; color: #16352c; }

/* 表格 */
.table-card { background: #fff; border-radius: 18px; padding: 20px; box-shadow: 0 4px 12px rgba(27,74,57,.04); overflow-x: auto; }
.data-table { width: 100%; border-collapse: collapse; text-align: left; font-size: 14px; }
.data-table th { padding: 14px 16px; color: #50816d; font-weight: 600; border-bottom: 2px solid #edf4ef; }
.data-table td { padding: 16px; border-bottom: 1px solid #edf4ef; color: #16352c; }
.data-table tbody tr:hover { background: #fbfdfb; }

.text-success { color: #217a58; font-weight: 600; }
.text-warning { color: #d97706; }

/* 按钮 */
.text-btn { background: transparent; border: none; color: #217a58; padding: 0 8px; font-weight: 600; font-size: 13px; cursor: pointer; }
.text-btn:hover { text-decoration: underline; }

/* 弹窗 */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,.4); display: grid; place-items: center; z-index: 100; backdrop-filter: blur(4px); }
.modal-card { background: #fff; width: 100%; max-width: 440px; padding: 32px; border-radius: 24px; box-shadow: 0 24px 80px rgba(0,0,0,.2); }
.modal-card h3 { margin: 0 0 8px; font-size: 20px; }
.modal-subtitle { margin: 0 0 20px; color: #668077; font-size: 14px; }
.modal-card label { display: block; margin-bottom: 16px; color: #50816d; font-size: 13px; font-weight: 600; }
.modal-card .target-display { margin: 0 0 16px; color: #16352c; font-size: 14px; }
.modal-card input { width: 100%; border: 1px solid #cbdad0; border-radius: 12px; padding: 12px 14px; outline: none; background: #fbfdfb; font: inherit; margin-top: 6px; box-sizing: border-box; }
.modal-card select:focus,
.modal-card input:focus { border-color: #217a58; box-shadow: 0 0 0 3px rgba(33, 122, 88, .12); }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }

/* 隐藏 number 输入框的上下箭头 */
input[type='number']::-webkit-inner-spin-button,
input[type='number']::-webkit-outer-spin-button { -webkit-appearance: none; margin: 0; }
input[type='number'] { -moz-appearance: textfield; }
</style>
