import api from '../utils/api'

// 게시글 생성
export const createPost = async (title, content, hashtags = [], mediaList = []) => {
  const response = await api.post('/community/posts', {
    title,
    content,
    hashtags,
    mediaList,
  })
  return response.data
}

// 게시글 수정
export const updatePost = async (postId, title, content, hashtags = [], mediaList = []) => {
  const response = await api.patch(`/community/posts/${postId}`, {
    title,
    content,
    hashtags,
    mediaList,
  })
  return response.data
}

// 게시글 목록 조회
export const getPosts = async (page = 0, size = 10, hashtag = null, authorAcc = null, sort = 'latest') => {
  const params = { page, size, sort }
  if (hashtag) params.hashtag = hashtag
  if (authorAcc) params.authorAcc = authorAcc
  
  const response = await api.get('/community/posts', { params })
  return response.data
}

// 게시글 단건 조회
export const getPost = async (postId) => {
  const response = await api.get(`/community/posts/${postId}`)
  return response.data
}

// 조회수 등록
export const registerView = async (postId) => {
  const response = await api.post(`/community/posts/${postId}/view`)
  return response.data
}

// 좋아요 토글
export const toggleLike = async (postId) => {
  const response = await api.post(`/community/posts/${postId}/like`)
  return response.data
}

// 댓글 작성
export const addComment = async (postId, content) => {
  const response = await api.post(`/community/posts/${postId}/comments`, { content })
  return response.data
}

// 댓글 목록 조회
export const getComments = async (postId, page = 0, size = 20) => {
  const response = await api.get(`/community/posts/${postId}/comments`, {
    params: { page, size },
  })
  return response.data
}

// 신고 작성
export const createReport = async (targetType, targetId, reason, reporterAcc = null) => {
  const response = await api.post('/community/reports', {
    targetType,
    targetId,
    reason,
    reporterAcc,
  })
  return response.data
}


