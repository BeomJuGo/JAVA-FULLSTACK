import api from '../utils/api'

// 매칭 요청 생성
export const createMatchRequest = async (userId, trainerId, requestedBy = 'USER') => {
  const response = await api.post('/matches/request', {
    userId,
    trainerId,
    requestedBy,
  })
  return response.data
}

// 매칭 승인
export const acceptMatch = async (matchId) => {
  const response = await api.post(`/matches/${matchId}/accept`)
  return response.data
}

// 매칭 시작
export const startMatch = async (matchId) => {
  const response = await api.post(`/matches/${matchId}/start`)
  return response.data
}

// 매칭 종료
export const endMatch = async (matchId, reason) => {
  const response = await api.post(`/matches/${matchId}/end`, { reason })
  return response.data
}

// 매칭 거절
export const rejectMatch = async (matchId, reason) => {
  const response = await api.post(`/matches/${matchId}/reject`, { reason })
  return response.data
}

// 매칭 조회
export const getMatch = async (matchId) => {
  const response = await api.get(`/matches/${matchId}`)
  return response.data
}

// 현재 사용자의 매칭 목록 조회
export const getMyMatches = async (status = null) => {
  const params = status ? { status } : {}
  const response = await api.get('/matches', { params })
  return response.data
}

