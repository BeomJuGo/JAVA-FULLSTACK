import api from '../utils/api'

// 리뷰 생성 (백엔드에서 userId 자동 설정)
export const createReview = async (matchId, trainerId, rating, content, anonymous = false) => {
  // 백엔드에서 accountId를 userId로 변환하므로 userId는 전달하지 않음
  const response = await api.post('/reviews', {
    matchId,
    trainerId,
    rating,
    content,
    anonymous,
  })
  return response.data
}

// 리뷰 수정
export const updateReview = async (reviewId, rating, content, anonymous) => {
  const response = await api.patch(`/reviews/${reviewId}`, {
    rating,
    content,
    anonymous,
  })
  return response.data
}

// 리뷰 삭제
export const deleteReview = async (reviewId) => {
  const response = await api.delete(`/reviews/${reviewId}`)
  return response.data
}

// 매칭별 리뷰 조회
export const getReviewByMatch = async (matchId) => {
  const response = await api.get(`/reviews/matches/${matchId}`)
  return response.data
}

// 트레이너별 리뷰 목록 조회
export const getReviewsByTrainer = async (trainerId, page = 0, size = 10) => {
  const response = await api.get(`/reviews/trainers/${trainerId}`, {
    params: { page, size },
  })
  return response.data
}

