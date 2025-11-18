import React, { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { createOrGetThread, sendMessage, getMessages, sendAiMessage } from '../api/chatApi'
import { getMyProfile } from '../api/profileApi'
import { getMyMatches } from '../api/matchApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

function Chat() {
  const { matchId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [threadId, setThreadId] = useState(null)
  const [messages, setMessages] = useState([])
  const [messageText, setMessageText] = useState('')
  const [loading, setLoading] = useState(true)
  const [currentAccountId, setCurrentAccountId] = useState(null)
  const [isAiChat, setIsAiChat] = useState(false)
  const [sending, setSending] = useState(false)
  const messagesContainerRef = useRef(null)
  const [autoScrollEnabled, setAutoScrollEnabled] = useState(true)

  // localStorage 키 생성
  const getStorageKey = () => `ai_chat_${matchId}`

  // localStorage에서 AI 채팅 기록 복원
  const loadAiChatHistory = () => {
    try {
      const storageKey = getStorageKey()
      const saved = localStorage.getItem(storageKey)
      if (saved) {
        const parsed = JSON.parse(saved)
        return parsed || []
      }
    } catch (err) {
      console.error('AI 채팅 기록 로드 실패:', err)
    }
    return []
  }

  // localStorage에 AI 채팅 기록 저장
  const saveAiChatHistory = (msgs) => {
    try {
      const storageKey = getStorageKey()
      localStorage.setItem(storageKey, JSON.stringify(msgs))
    } catch (err) {
      console.error('AI 채팅 기록 저장 실패:', err)
    }
  }

  useEffect(() => {
    const init = async () => {
      await loadCurrentUser()
      await checkIfAiChat()
      await initializeChat()
    }
    init()
  }, [matchId])

  const loadCurrentUser = async () => {
    try {
      const profile = await getMyProfile()
      setCurrentAccountId(profile.accountId)
    } catch (err) {
      console.error('사용자 정보 로드 실패:', err)
    }
  }

  const checkIfAiChat = async () => {
    try {
      const matches = await getMyMatches()
      const match = matches.find(m => m.id === parseInt(matchId))
      if (match && match.isAiTrainer) {
        setIsAiChat(true)
        return true
      }
      return false
    } catch (err) {
      console.error('매칭 정보 확인 실패:', err)
      return false
    }
  }

  useEffect(() => {
    if (threadId && !isAiChat) {
      loadMessages()
      // 주기적으로 메시지 새로고침 (실제로는 WebSocket 사용 권장)
      const interval = setInterval(loadMessages, 3000)
      return () => clearInterval(interval)
    } else if (isAiChat) {
      // AI 채팅은 DB 저장 없이 localStorage에서 관리
      // currentAccountId가 로드된 후에만 복원
      if (currentAccountId) {
        const savedMessages = loadAiChatHistory()
        if (savedMessages.length > 0) {
          // 복원된 메시지들의 isOwn 속성 확인 및 설정
          const restoredMessages = savedMessages.map(msg => {
            if (msg.isOwn === undefined) {
              // isOwn 속성이 없으면 senderAcc를 기준으로 설정
              msg.isOwn = currentAccountId && msg.senderAcc === currentAccountId
            }
            return msg
          })
          setMessages(restoredMessages)
        }
      }
      setLoading(false)
    }
  }, [threadId, isAiChat, currentAccountId])

  // AI 채팅인 경우 messages 변경 시 localStorage에 저장
  useEffect(() => {
    if (isAiChat && messages.length > 0) {
      saveAiChatHistory(messages)
    }
  }, [messages, isAiChat])

  useEffect(() => {
    if (autoScrollEnabled) {
      scrollToBottom(messages.length <= 1 ? 'auto' : 'smooth')
    }
  }, [messages, autoScrollEnabled])

  const initializeChat = async () => {
    try {
      setLoading(true)
      // isAiChat 상태를 다시 확인 (비동기로 인해 아직 설정되지 않았을 수 있음)
      const matches = await getMyMatches()
      const match = matches.find(m => m.id === parseInt(matchId))
      const isAi = match && match.isAiTrainer

      if (isAi) {
        setIsAiChat(true)
        // AI 채팅은 스레드 생성 불필요
        setThreadId(null)
      } else {
        setIsAiChat(false)
        const thread = await createOrGetThread(parseInt(matchId))
        setThreadId(thread.threadId)
      }
    } catch (err) {
      console.error('채팅 초기화 실패:', err)
      alert('채팅을 불러올 수 없습니다.')
    } finally {
      setLoading(false)
    }
  }

  const loadMessages = async () => {
    if (!threadId || isAiChat) return

    try {
      const data = await getMessages(threadId, 0, 50)
      setMessages(data.content || [])
    } catch (err) {
      console.error('메시지 로드 실패:', err)
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!messageText.trim()) return

    if (isAiChat) {
      // AI 채팅: GPT와 실시간 대화
      await handleAiMessage()
    } else {
      // 일반 채팅: DB에 저장
      if (!threadId) return
      try {
        await sendMessage(threadId, messageText)
        setMessageText('')
        setAutoScrollEnabled(true)
        await loadMessages()
      } catch (err) {
        alert(err.response?.data?.message || '메시지 전송에 실패했습니다.')
      }
    }
  }

  const handleAiMessage = async () => {
    if (!messageText.trim() || sending) return

    const userMessage = messageText.trim()
    setMessageText('')
    setSending(true)
    setAutoScrollEnabled(true)

    // 사용자 메시지를 즉시 표시
    const userMsg = {
      id: Date.now(),
      senderAcc: currentAccountId,
      content: userMessage,
      createdAt: new Date().toISOString(),
      isOwn: true,
    }

    // 현재 메시지 목록에 새 사용자 메시지를 포함한 전체 대화 내역 생성
    const allMessages = [...messages, userMsg]
    setMessages(allMessages)

    try {
      // 대화 내역을 GPT API 형식으로 변환 (이전 대화 기록 + 현재 사용자 메시지 포함)
      const conversationHistory = allMessages.map(msg => ({
        role: msg.isOwn !== undefined
          ? (msg.isOwn ? 'user' : 'assistant')
          : (currentAccountId && msg.senderAcc === currentAccountId) ? 'user' : 'assistant',
        content: msg.content,
      }))

      // GPT API 호출 (이전 대화 기록을 포함하여 컨텍스트 유지)
      const response = await sendAiMessage(parseInt(matchId), userMessage, conversationHistory)

      // AI 응답을 표시
      const aiMsg = {
        id: Date.now() + 1,
        senderAcc: null, // AI는 senderAcc가 없음
        content: response.message,
        createdAt: new Date().toISOString(),
        isOwn: false,
      }
      setMessages(prev => [...prev, aiMsg])
    } catch (err) {
      console.error('AI 메시지 전송 실패:', err)
      alert(err.response?.data?.message || 'AI 응답을 받는 중 오류가 발생했습니다.')
      // 실패한 사용자 메시지 제거 (이전 상태로 복원)
      setMessages(messages)
    } finally {
      setSending(false)
    }
  }

  const scrollToBottom = (behavior = 'smooth') => {
    const container = messagesContainerRef.current
    if (!container) return
    container.scrollTo({
      top: container.scrollHeight,
      behavior,
    })
  }

  const handleScroll = () => {
    const container = messagesContainerRef.current
    if (!container) return
    const isAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 40
    setAutoScrollEnabled(isAtBottom)
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-600">
        <div className="rounded-3xl border border-white/80 bg-white px-6 py-4 shadow-lg">로딩 중...</div>
      </div>
    )
  }

  return (
    <div className="relative isolate flex h-screen flex-col overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-800">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-28 left-[10%] h-72 w-72 rounded-full bg-indigo-200/45 blur-[120px]" />
        <div className="absolute right-[12%] top-[20%] h-64 w-64 rounded-full bg-sky-200/35 blur-[120px]" />
        <div className="absolute bottom-[-160px] left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full bg-purple-100/60 blur-[140px]" />
      </div>

      <div className="sticky top-0 z-10 flex-shrink-0 border-b border-white/60 bg-white/90 backdrop-blur-xl">
        <div className="mx-auto flex w-full max-w-4xl items-center justify-between px-4 py-3">
          <div className="flex items-center gap-4">
            <Button
              variant="outline"
              onClick={() => navigate(-1)}
              className="rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
            >
              ← 뒤로가기
            </Button>
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.35em] text-indigo-400">Chat</p>
              <h1 className="text-xl font-semibold text-slate-900">
                {isAiChat ? '🤖 AI 트레이너' : '매칭'} #{matchId} 채팅
              </h1>
            </div>
          </div>
        </div>
      </div>

      <div className="mx-auto flex w-full max-w-4xl flex-1 min-h-0 flex-col px-4 py-3">
        <div
          ref={messagesContainerRef}
          onScroll={handleScroll}
          className="flex-1 min-h-0 space-y-3 overflow-y-auto rounded-2xl border border-slate-200 bg-white/90 p-4 shadow-inner hide-scrollbar"
        >
          {messages.length === 0 && !loading && (
            <div className="flex h-full items-center justify-center">
              <p className="text-center text-sm text-slate-500">
                {isAiChat
                  ? '🤖 AI 트레이너와 대화를 시작하세요!'
                  : '메시지를 입력하여 대화를 시작하세요.'}
              </p>
            </div>
          )}
          {messages.map((message) => {
            const isOwn = message.isOwn !== undefined
              ? message.isOwn
              : (currentAccountId && message.senderAcc === currentAccountId)
            return (
              <div key={message.id} className={`flex ${isOwn ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-xs rounded-2xl px-4 py-3 text-sm shadow-lg lg:max-w-md ${isOwn
                    ? 'bg-gradient-to-r from-blue-500 to-indigo-500 text-white'
                    : isAiChat && !isOwn
                      ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
                      : 'border border-slate-200 bg-white text-slate-700'
                    }`}
                >
                  <p className="whitespace-pre-wrap leading-relaxed">{message.content}</p>
                  <p
                    className={`mt-2 text-[10px] uppercase tracking-wide ${isOwn ? 'text-white/80' : 'text-slate-400'
                      }`}
                  >
                    {formatDate(message.createdAt)}
                  </p>
                </div>
              </div>
            )
          })}
          {sending && (
            <div className="flex justify-start">
              <div className="rounded-2xl border border-indigo-200 bg-indigo-50 px-4 py-3 text-sm text-slate-600">
                <p className="text-indigo-500">AI가 응답을 생성하는 중...</p>
              </div>
            </div>
          )}
        </div>

        <div className="flex-shrink-0 mt-3 rounded-2xl border border-slate-200 bg-white p-3 shadow-[0_12px_40px_-25px_rgba(15,23,42,0.35)]">
          <form onSubmit={handleSendMessage} className="flex gap-3">
            <Input
              value={messageText}
              onChange={(e) => setMessageText(e.target.value)}
              placeholder="메시지를 입력하세요..."
              className="flex-1 rounded-xl border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
            />
            <Button type="submit" className="rounded-xl bg-gradient-to-r from-blue-500 to-indigo-500 px-6 text-white shadow-md hover:from-blue-600 hover:to-indigo-600" disabled={!messageText.trim() || sending}>
              {sending ? '전송 중...' : '전송'}
            </Button>
          </form>
        </div>
      </div>
    </div>
  )
}

export default Chat

