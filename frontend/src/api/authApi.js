import api from '../utils/api'

// 회원가입
export const signup = async (payload) => {
  const response = await api.post('/auth/signup', payload)
  return response.data
}

// 로그인
export const login = async (username, password) => {
  const response = await api.post('/auth/login', {
    username,
    password,
  })
  const { token, username: user, role, displayName } = response.data
  // 토큰과 사용자 정보 저장
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify({ username: user, role, displayName }))
  return response.data
}

// 로그아웃
export const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

// 현재 사용자 정보 가져오기
export const getCurrentUser = () => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
}

// 토큰 가져오기
export const getToken = () => {
  return localStorage.getItem('token')
}

// 인증 여부 확인
export const isAuthenticated = () => {
  return !!getToken()
}


