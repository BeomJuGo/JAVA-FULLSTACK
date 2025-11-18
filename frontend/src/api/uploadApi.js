import api from '../utils/api'

// 업로드 서명 발급
export const getUploadSign = async () => {
  const response = await api.post('/uploads/sign')
  return response.data
}

// 업로드 메타 저장
export const saveUploadMeta = async (publicId, mediaType, url, width, height, bytes) => {
  const response = await api.post('/uploads', {
    publicId,
    mediaType,
    url,
    width,
    height,
    bytes,
  })
  return response.data
}

// 업로드 숨김/해제
export const setUploadHidden = async (id, hidden) => {
  const response = await api.post(`/uploads/${id}/hidden`, null, {
    params: { hidden },
  })
  return response.data
}

// 업로드 삭제
export const deleteUpload = async (id) => {
  const response = await api.delete(`/uploads/${id}`)
  return response.data
}


