import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getMyMatches, createMatchRequest, acceptMatch, startMatch, endMatch, rejectMatch } from '../api/matchApi'
import { getAllTrainers, getAllUsers } from '../api/profileApi'
import { Button } from '@/components/ui/button'

function Matches() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [matches, setMatches] = useState([])
  const [trainers, setTrainers] = useState([])
  const [users, setUsers] = useState([])
  const [currentUserProfile, setCurrentUserProfile] = useState(null)
  const [currentTrainerProfile, setCurrentTrainerProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [selectedTrainerId, setSelectedTrainerId] = useState('')
  const [selectedUserId, setSelectedUserId] = useState('')

  useEffect(() => {
    loadData()
  }, [user])

  const loadData = async () => {
    try {
      setLoading(true)
      const [matchData, trainerData, userData] = await Promise.all([
        getMyMatches().catch(() => []),
        getAllTrainers().catch(() => []),
        getAllUsers().catch(() => []),
      ])
      setMatches(matchData)
      setTrainers(trainerData)
      setUsers(userData)
      
      // 현재 사용자의 프로필 찾기
      // 매칭 데이터에서 현재 사용자의 프로필 ID 추론
      if (user?.role === 'USER' && userData.length > 0) {
        if (matchData.length > 0) {
          // 매칭 데이터의 userId를 사용하여 프로필 찾기
          const userMatch = matchData[0]
          if (userMatch.userId) {
            const userProfile = userData.find(u => u.id === userMatch.userId)
            if (userProfile) {
              setCurrentUserProfile(userProfile)
            }
          }
        }
        // 매칭이 없거나 프로필을 찾지 못한 경우 첫 번째 프로필 사용 (임시)
        // 실제로는 백엔드에서 현재 사용자의 프로필을 반환하는 API가 필요함
        if (userData.length > 0 && !matchData.find(m => m.userId)) {
          setCurrentUserProfile(userData[0])
        }
      } else if (user?.role === 'TRAINER' && trainerData.length > 0) {
        if (matchData.length > 0) {
          const trainerMatch = matchData[0]
          if (trainerMatch.trainerId) {
            const trainerProfile = trainerData.find(t => t.id === trainerMatch.trainerId)
            if (trainerProfile) {
              setCurrentTrainerProfile(trainerProfile)
            }
          }
        }
        if (trainerData.length > 0 && !matchData.find(m => m.trainerId)) {
          setCurrentTrainerProfile(trainerData[0])
        }
      }
    } catch (err) {
      console.error('데이터 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleCreateMatch = async (e) => {
    e.preventDefault()
    try {
      if (user?.role === 'USER' && selectedTrainerId && currentUserProfile) {
        await createMatchRequest(currentUserProfile.id, parseInt(selectedTrainerId), 'USER')
      } else if (user?.role === 'TRAINER' && selectedUserId && currentTrainerProfile) {
        await createMatchRequest(parseInt(selectedUserId), currentTrainerProfile.id, 'TRAINER')
      } else {
        alert('프로필 정보를 불러올 수 없습니다.')
        return
      }
      await loadData()
      setShowCreateForm(false)
      setSelectedTrainerId('')
      setSelectedUserId('')
    } catch (err) {
      alert(err.response?.data?.message || '매칭 생성에 실패했습니다.')
    }
  }

  const handleAccept = async (matchId) => {
    try {
      await acceptMatch(matchId)
      await loadData()
    } catch (err) {
      alert(err.response?.data?.message || '승인에 실패했습니다.')
    }
  }

  const handleStart = async (matchId) => {
    try {
      await startMatch(matchId)
      await loadData()
    } catch (err) {
      alert(err.response?.data?.message || '시작에 실패했습니다.')
    }
  }

  const handleEnd = async (matchId) => {
    const reason = prompt('종료 사유를 입력하세요:')
    if (reason) {
      try {
        await endMatch(matchId, reason)
        await loadData()
      } catch (err) {
        alert(err.response?.data?.message || '종료에 실패했습니다.')
      }
    }
  }

  const handleReject = async (matchId) => {
    const reason = prompt('거절 사유를 입력하세요:')
    if (reason) {
      try {
        await rejectMatch(matchId, reason)
        await loadData()
      } catch (err) {
        alert(err.response?.data?.message || '거절에 실패했습니다.')
      }
    }
  }

  const getStatusBadge = (status) => {
    const colors = {
      REQUESTED: 'border border-amber-400/40 bg-amber-400/10 text-amber-100',
      ACCEPTED: 'border border-sky-400/40 bg-sky-400/10 text-sky-100',
      IN_PROGRESS: 'border border-emerald-400/40 bg-emerald-400/10 text-emerald-100',
      ENDED: 'border border-slate-300/30 bg-slate-300/10 text-slate-200',
      REJECTED: 'border border-rose-400/40 bg-rose-400/10 text-rose-100',
    }
    return (
      <span
        className={`inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide ${
          colors[status] || 'border border-slate-400/30 bg-slate-400/10 text-slate-200'
        }`}
      >
        {status}
      </span>
    )
  }

  const getPartnerInfo = (match) => {
    if (user?.role === 'USER') {
      // 백엔드에서 제공하는 partnerName 사용
      if (match.isAiTrainer) {
        return {
          label: '🤖 AI 트레이너',
          navigateTo: `/chat/${match.id}`, // AI 트레이너는 프로필 페이지가 없으므로 채팅으로
        }
      }
      const label = match.partnerName || match.partnerUsername || `트레이너 #${match.trainerId}`
      return {
        label,
        navigateTo: `/profiles/trainers/${match.trainerId}`,
      }
    }
    if (user?.role === 'TRAINER') {
      // 백엔드에서 제공하는 partnerName 사용
      const label = match.partnerName || match.partnerUsername || `사용자 #${match.userId}`
      return {
        label,
        navigateTo: `/profiles/users/${match.userId}`,
      }
    }
    return {
      label: `매칭 ${match.id}`,
      navigateTo: `/chat/${match.id}`,
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-600">
        <div className="rounded-3xl border border-white/80 bg-white px-6 py-4 shadow-lg">
          로딩 중...
        </div>
      </div>
    )
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-10 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-24 left-[15%] h-64 w-64 rounded-full bg-indigo-200/45 blur-[110px]" />
        <div className="absolute right-[12%] top-[15%] h-72 w-72 rounded-full bg-sky-200/35 blur-[115px]" />
        <div className="absolute bottom-[-130px] left-1/2 h-[360px] w-[360px] -translate-x-1/2 rounded-full bg-purple-100/60 blur-[140px]" />
      </div>

      <div className="relative mx-auto flex w-full max-w-6xl flex-col gap-8">
        <div className="flex flex-wrap items-center justify-between gap-4 rounded-[28px] border border-white/70 bg-white/90 p-6 shadow-[0_25px_70px_-50px_rgba(15,23,42,0.65)] backdrop-blur">
          <div>
            <span className="inline-flex items-center rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.35em] text-indigo-500">
              Match Center
            </span>
            <h1 className="mt-4 text-4xl font-bold tracking-tight text-slate-900">매칭 관리</h1>
            <p className="mt-2 text-sm text-slate-500">매칭 요청과 상태 변화를 한눈에 관리하세요.</p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Button
              variant="outline"
              className="border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
              onClick={() => navigate(-1)}
            >
              뒤로가기
            </Button>
            <Button
              className="rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 shadow-lg hover:from-blue-600 hover:to-indigo-600"
              onClick={() => setShowCreateForm(!showCreateForm)}
            >
              {showCreateForm ? '취소' : '매칭 생성'}
            </Button>
          </div>
        </div>

        {showCreateForm && (
          <div className="rounded-[28px] border border-slate-200 bg-white/95 p-6 shadow-[0_22px_70px_-45px_rgba(15,23,42,0.55)]">
            <h2 className="text-xl font-semibold text-slate-900">새 매칭 생성</h2>
            <p className="mt-1 text-sm text-slate-500">파트너를 선택하고 새로운 매칭을 시작하세요.</p>
            <form onSubmit={handleCreateMatch} className="mt-6 grid gap-5 text-left">
              {user?.role === 'USER' && (
                <div className="grid gap-2">
                  <label className="text-sm font-medium text-slate-600">트레이너 선택</label>
                  <select
                    value={selectedTrainerId}
                    onChange={(e) => setSelectedTrainerId(e.target.value)}
                    className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-3 py-3 text-sm text-slate-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-300"
                    required
                  >
                    <option value="">트레이너를 선택하세요</option>
                    {trainers.map((trainer) => (
                      <option key={trainer.id} value={trainer.id}>
                        트레이너 {trainer.id}
                      </option>
                    ))}
                  </select>
                </div>
              )}
              {user?.role === 'TRAINER' && (
                <div className="grid gap-2">
                  <label className="text-sm font-medium text-slate-600">사용자 선택</label>
                  <select
                    value={selectedUserId}
                    onChange={(e) => setSelectedUserId(e.target.value)}
                    className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-3 py-3 text-sm text-slate-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-300"
                    required
                  >
                    <option value="">사용자를 선택하세요</option>
                    {users.map((user) => (
                      <option key={user.id} value={user.id}>
                        사용자 {user.id}
                      </option>
                    ))}
                  </select>
                </div>
              )}
              <Button
                type="submit"
                className="h-12 rounded-2xl bg-gradient-to-r from-blue-500 to-purple-500 text-white shadow-lg hover:from-blue-600 hover:to-purple-600"
              >
                매칭 요청
              </Button>
            </form>
          </div>
        )}

        <div className="rounded-[28px] border border-slate-200/80 bg-white/95 p-6 shadow-[0_26px_80px_-50px_rgba(15,23,42,0.6)]">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-slate-100">
              <thead className="bg-slate-50/80">
                <tr>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.3em] text-slate-400">상대</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.3em] text-slate-400">상태</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.3em] text-slate-400">요청일</th>
                  <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-[0.3em] text-slate-400">작업</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100/70">
                {matches.map((match) => {
                  const partner = getPartnerInfo(match)
                  return (
                    <tr key={match.id} className="transition hover:bg-slate-50/80">
                      <td className="px-6 py-4 text-sm">
                        <div className="space-y-1">
                          <button
                            type="button"
                            onClick={() => navigate(partner.navigateTo)}
                            className="text-sm font-semibold text-indigo-600 hover:text-indigo-800"
                          >
                            {partner.label}
                          </button>
                          <div className="text-xs text-slate-400">매칭 ID: {match.id}</div>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm">
                        {getStatusBadge(match.status)}
                      </td>
                      <td className="px-6 py-4 text-sm text-slate-500">
                        {new Date(match.requestedAt).toLocaleDateString('ko-KR')}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        <div className="flex flex-wrap gap-2">
                          {match.status === 'REQUESTED' && user?.role === 'TRAINER' && (
                            <>
                              <Button
                                type="button"
                                variant="outline"
                                className="rounded-xl border-emerald-200 bg-emerald-50 px-3 py-2 text-emerald-600 hover:bg-emerald-100"
                                onClick={() => handleAccept(match.id)}
                              >
                                승인
                              </Button>
                              <Button
                                type="button"
                                variant="outline"
                                className="rounded-xl border-rose-200 bg-rose-50 px-3 py-2 text-rose-600 hover:bg-rose-100"
                                onClick={() => handleReject(match.id)}
                              >
                                거절
                              </Button>
                            </>
                          )}
                          {match.status === 'ACCEPTED' && (
                            <Button
                              type="button"
                              variant="outline"
                              className="rounded-xl border-sky-200 bg-sky-50 px-3 py-2 text-sky-600 hover:bg-sky-100"
                              onClick={() => handleStart(match.id)}
                            >
                              시작
                            </Button>
                          )}
                          {(match.status === 'IN_PROGRESS' || match.status === 'ACCEPTED') && (
                            <Button
                              type="button"
                              variant="outline"
                              className="rounded-xl border-rose-200 bg-rose-50 px-3 py-2 text-rose-600 hover:bg-rose-100"
                              onClick={() => handleEnd(match.id)}
                            >
                              종료
                            </Button>
                          )}
                          <Button
                            type="button"
                            variant="outline"
                            className="rounded-xl border-indigo-200 bg-indigo-50 px-3 py-2 text-indigo-600 hover:bg-indigo-100"
                            onClick={() => navigate(`/chat/${match.id}`)}
                          >
                            채팅
                          </Button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </div>

        {matches.length === 0 && (
          <div className="rounded-[28px] border border-dashed border-slate-200 bg-white/80 py-12 text-center text-sm text-slate-500">
            매칭이 없습니다.
          </div>
        )}
      </div>
    </div>
  )
}

export default Matches

