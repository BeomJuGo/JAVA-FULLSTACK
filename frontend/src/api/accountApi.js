import api from '../utils/api'

// 현재 사용자의 프로필 정보 가져오기 (accountId 포함)
export const getCurrentAccount = async () => {
  // 백엔드에서 현재 사용자 정보를 반환하는 엔드포인트가 있다면 사용
  // 없으면 프로필 목록에서 찾아야 함
  try {
    // 임시로 매칭 목록에서 사용자 정보를 추론
    const response = await api.get('/matches')
    return response.data
  } catch (err) {
    console.error('계정 정보 가져오기 실패:', err)
    return null
  }
}

