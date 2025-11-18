import api from '../utils/api'

// 주간 플랜 조회
export const getWeekView = async (matchId, weekStart) => {
  const response = await api.get('/plans/weeks', {
    params: {
      matchId,
      weekStart, // yyyy-MM-dd 형식
    },
  })
  return response.data
}

// 주간 플랜 생성 (트레이너 전용)
export const createWeek = async (matchId, weekStart, title, note, createdBy) => {
  const response = await api.post('/plans/weeks', {
    matchId,
    weekStart,
    title,
    note,
    createdBy,
  })
  return response.data
}

// Day 메모 수정 (트레이너 전용)
export const updateDayNote = async (dayId, note) => {
  const response = await api.patch(`/plans/days/${dayId}`, { note })
  return response.data
}

// Item 생성 (트레이너 전용)
export const createItem = async (dayId, itemType, title, description, targetKcal, targetMin) => {
  const response = await api.post(`/plans/days/${dayId}/items`, {
    itemType, // WORKOUT, DIET, NOTE
    title,
    description,
    targetKcal,
    targetMin,
  })
  return response.data
}

// Item 수정 (트레이너 전용)
export const updateItem = async (itemId, data) => {
  const response = await api.patch(`/plans/items/${itemId}`, data)
  return response.data
}

// Item 상태 변경 (유저 전용) - O/D/X
export const changeItemStatus = async (itemId, statusMark, lockAfterComplete = false) => {
  const response = await api.post(`/plans/items/${itemId}/status`, {
    statusMark, // O, D, X
    lockAfterComplete,
  })
  return response.data
}

// Item 잠금/해제 (트레이너 전용)
export const lockItem = async (itemId, locked) => {
  const response = await api.post(`/plans/items/${itemId}/lock`, { locked })
  return response.data
}

// Item 삭제 (트레이너 전용)
export const deleteItem = async (itemId) => {
  const response = await api.delete(`/plans/items/${itemId}`)
  return response.data
}


