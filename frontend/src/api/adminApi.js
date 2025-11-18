import api from '../utils/api'

// 게시글 숨기기/보이기
export const hidePost = async (postId, hidden, reason) => {
  const response = await api.post(`/admin/posts/${postId}/hide`, {
    hidden,
    reason,
  })
  return response.data
}

// 댓글 숨기기/보이기
export const hideComment = async (commentId, hidden, reason) => {
  const response = await api.post(`/admin/comments/${commentId}/hide`, {
    hidden,
    reason,
  })
  return response.data
}

// 계정 정지
export const suspendAccount = async (accountId, reason) => {
  const response = await api.post(`/admin/accounts/${accountId}/suspend`, {
    reason,
  })
  return response.data
}

// 계정 복구
export const restoreAccount = async (accountId, reason) => {
  const response = await api.post(`/admin/accounts/${accountId}/restore`, {
    reason,
  })
  return response.data
}

// 매칭 강제 종료
export const forceEndMatch = async (matchId, reason) => {
  const response = await api.post(`/admin/matches/${matchId}/force-end`, {
    reason,
  })
  return response.data
}

// 관리자 로그 조회
export const getAdminLogs = async (page = 0, size = 20) => {
  const response = await api.get('/admin/logs', {
    params: { page, size },
  })
  return response.data
}

