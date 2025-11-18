import api from '../utils/api'

// 스레드 생성/조회
export const createOrGetThread = async (matchId) => {
  const response = await api.post('/chat/threads', { matchId })
  return response.data
}

// 메시지 전송
export const sendMessage = async (threadId, content) => {
  // 백엔드에서 SecurityUtil로 자동으로 senderAcc를 가져오므로 content만 전송
  const response = await api.post(`/chat/threads/${threadId}/messages`, { content })
  return response.data
}

// 메시지 목록 조회
export const getMessages = async (threadId, page = 0, size = 50, includeHidden = false) => {
  const response = await api.get(`/chat/threads/${threadId}/messages`, {
    params: { page, size, includeHidden },
  })
  return response.data
}

// 메시지 숨김/해제
export const hideMessage = async (messageId, hidden) => {
  const response = await api.post(`/chat/messages/${messageId}/hide`, { hidden })
  return response.data
}

// AI 채팅: GPT와 실시간 대화 (DB 저장 없음)
export const sendAiMessage = async (matchId, message, conversationHistory = []) => {
  const response = await api.post(`/chat/ai/${matchId}/message`, {
    message,
    conversationHistory,
  })
  return response.data
}

