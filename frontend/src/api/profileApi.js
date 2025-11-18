import api from '../utils/api'

// 모든 유저 프로필 조회
export const getAllUsers = async () => {
  const response = await api.get('/profiles/users')
  return response.data
}

// 유저 프로필 조회
export const getUserProfile = async (id) => {
  const response = await api.get(`/profiles/users/${id}`)
  return response.data
}

// 모든 트레이너 프로필 조회
export const getAllTrainers = async () => {
  const response = await api.get('/profiles/trainers')
  return response.data
}

// 트레이너 프로필 조회
export const getTrainerProfile = async (id) => {
  const response = await api.get(`/profiles/trainers/${id}`)
  return response.data
}

// 현재 사용자 프로필 조회
export const getMyProfile = async () => {
  const response = await api.get('/profiles/me')
  return response.data
}

// 현재 사용자 프로필 정보 업데이트
export const updateMyProfileDetails = async (payload) => {
  const response = await api.patch('/profiles/me/profile', payload)
  return response.data
}

// 표시 이름 업데이트
export const updateDisplayName = async (displayName) => {
  const response = await api.patch('/profiles/me/display-name', { displayName })
  return response.data
}

// 프로필 이미지 업데이트
export const updateProfileImage = async (imageUrl, imagePublicId) => {
  const response = await api.patch('/profiles/me/image', { imageUrl, imagePublicId })
  return response.data
}