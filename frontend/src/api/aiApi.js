import api from '../utils/api'

/**
 * AI 추천 플랜 생성 요청
 * @param {string} weekStart - 주간 시작일 (yyyy-MM-dd 형식)
 * @param {string} goal - 목표 (선택사항)
 * @param {string} specialRequests - 특별 요청사항 (선택사항)
 * @returns {Promise<{matchId: number, message: string}>}
 */
export const createAiRecommendation = async (weekStart, goal = null, specialRequests = null) => {
  const response = await api.post('/ai/recommendations', {
    weekStart,
    goal,
    specialRequests,
  })
  return response.data
}

