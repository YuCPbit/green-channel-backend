const TOKEN_KEY = 'green_channel_token'

async function request(path, options = {}) {
  const token = localStorage.getItem(TOKEN_KEY)
  const response = await fetch(path, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  })
  const payload = await response.json()
  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || '请求失败')
  }
  return payload.data
}

export async function login(username, password) {
  const result = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
  localStorage.setItem(TOKEN_KEY, result.token)
  return result.user
}

export async function logout() {
  try {
    await request('/api/auth/logout', { method: 'POST' })
  } finally {
    localStorage.removeItem(TOKEN_KEY)
  }
}

export async function currentUser() {
  if (!localStorage.getItem(TOKEN_KEY)) return null
  try {
    return await request('/api/auth/me')
  } catch {
    localStorage.removeItem(TOKEN_KEY)
    return null
  }
}

// ==========================================
// 补助批次 API
// ==========================================

/**
 * 分页查询批次列表
 * @param {Object} params - { batchName, status, page, size }
 */
export async function getSubsidyBatches(params = {}) {
  const query = new URLSearchParams()
  if (params.batchName) query.set('batchName', params.batchName)
  if (params.status !== undefined && params.status !== null && params.status !== '') query.set('status', params.status)
  query.set('page', params.page || 0)
  query.set('size', params.size || 10)
  return request(`/api/subsidy/batches?${query.toString()}`)
}

/**
 * 创建补助批次
 * @param {Object} data - { batchName, academicYear, applyStartTime, applyEndTime, collegeSubmitEndTime }
 */
export async function createSubsidyBatch(data) {
  return request('/api/subsidy/batches', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

/**
 * 更新补助批次（含状态变更）
 * @param {number} id - 批次ID
 * @param {Object} data - 批次字段
 */
export async function updateSubsidyBatch(id, data) {
  return request(`/api/subsidy/batches/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

/**
 * 开始批次（DRAFT → ACTIVE）
 */
export async function startSubsidyBatch(id) {
  return request(`/api/subsidy/batches/${id}/start`, { method: 'POST' })
}

/**
 * 提前结束批次（ACTIVE → ENDED）
 */
export async function endSubsidyBatch(id) {
  return request(`/api/subsidy/batches/${id}/end`, { method: 'POST' })
}

// ==========================================
// 额度分配 API
// ==========================================

/**
 * 下发额度（学校→学院 或 学院→年级）
 * @param {Object} data - { batchId, targetType, targetId, amount }
 */
export async function allocateQuota(data) {
  return request('/api/subsidy/allocations', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

/**
 * 获取额度看板汇总
 * @param {number} batchId - 批次ID
 */
export async function getAllocationSummary(batchId) {
  return request(`/api/subsidy/allocations/summary?batchId=${batchId}`)
}

/**
 * 查询分配明细列表
 * @param {number} batchId - 批次ID
 * @param {number} targetType - 目标类型：1=学院 2=年级（可选）
 */
export async function getAllocationList(batchId, targetType) {
  let url = `/api/subsidy/allocations?batchId=${batchId}`
  if (targetType != null) url += `&targetType=${targetType}`
  return request(url)
}

/**
 * 获取学院列表
 */
export async function getColleges() {
  return request('/api/subsidy/allocations/colleges')
}

/**
 * 获取年级列表
 */
export async function getGrades() {
  return request('/api/subsidy/allocations/grades')
}

// ==========================================
// 补助申请与审核 API
// ==========================================

/**
 * 获取当前可申请的活跃批次列表
 */
export async function getAvailableBatches() {
  return request('/api/subsidy/batches/available')
}

/**
 * 学生提交困难补助申请
 * @param {Object} data - { batchId, applyAmount, applyReason }
 */
export async function submitSubsidyApply(data) {
  return request('/api/subsidy/applies', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

/**
 * 辅导员代学生发起补助申请
 * @param {Object} data - { batchId, studentId, applyAmount, applyReason }
 */
export async function submitTutorSubsidyApply(data) {
  return request('/api/subsidy/applies/tutor', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

/**
 * 重新提交被退回的申请
 * @param {number} id
 * @param {Object} data - { applyAmount, applyReason, batchId }
 */
export async function resubmitSubsidyApply(id, data) {
  return request(`/api/subsidy/applies/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

/**
 * 查询补助申请列表（角色过滤）
 * @param {Object} params - { batchId, status, studentName, page, size }
 */
export async function getSubsidyApplies(params = {}) {
  const query = new URLSearchParams()
  if (params.batchId) query.set('batchId', params.batchId)
  if (params.status !== undefined && params.status !== null && params.status !== '') query.set('status', params.status)
  if (params.studentName) query.set('studentName', params.studentName)
  query.set('page', params.page || 1)
  query.set('size', params.size || 20)
  return request(`/api/subsidy/applies?${query.toString()}`)
}

/**
 * 获取申请详情（含审核时间线）
 * @param {number} id
 */
export async function getSubsidyApplyDetail(id) {
  return request(`/api/subsidy/applies/${id}`)
}

/**
 * 提交审核
 * @param {Object} data - { applyId, action, comment, suggestAmount }
 */
export async function submitSubsidyReview(data) {
  return request('/api/subsidy/reviews', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

/**
 * 搜索学生（供辅导员代申请用）
 * @param {string} keyword - 学号或姓名
 */
export async function searchStudents(keyword) {
  return request(`/api/subsidy/students/search?keyword=${encodeURIComponent(keyword)}`)
}
