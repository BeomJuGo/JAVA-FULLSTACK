import React, { useState, useEffect, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getWeekView, changeItemStatus } from '../api/planApi'
import { getMyMatches } from '../api/matchApi'
import { Card, CardHeader, CardContent, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'

function Certification() {
  const { date } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [matches, setMatches] = useState([])
  const [selectedMatchId, setSelectedMatchId] = useState(null)
  const [plan, setPlan] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadMatches()
  }, [])

  const loadPlan = useCallback(async () => {
    if (!selectedMatchId || !date) {
      setPlan(null)
      setLoading(false)
      return
    }

    try {
      setLoading(true)
      // 매칭 변경 시 이전 플랜 데이터 즉시 초기화
      setPlan(null)

      // 주간 시작일 계산 (월요일)
      const dateObj = new Date(date)
      const day = dateObj.getDay()
      const diff = day === 0 ? -6 : 1 - day
      const monday = new Date(dateObj)
      monday.setDate(dateObj.getDate() + diff)
      const weekStart = monday.toISOString().split('T')[0]

      const data = await getWeekView(selectedMatchId, weekStart)
      setPlan(data)
    } catch (err) {
      console.error('플랜 로드 실패:', err)
      setPlan(null)
    } finally {
      setLoading(false)
    }
  }, [selectedMatchId, date])

  useEffect(() => {
    if (selectedMatchId && date) {
      loadPlan()
    } else {
      // 매칭이 선택되지 않았거나 날짜가 없으면 플랜 초기화
      setPlan(null)
      setLoading(false)
    }
  }, [selectedMatchId, date, loadPlan])

  const loadMatches = async () => {
    try {
      const matchData = await getMyMatches()
      setMatches(matchData)
      const activeMatch = matchData.find(m => m.status === 'IN_PROGRESS' || m.status === 'ACCEPTED')
      if (activeMatch) {
        setSelectedMatchId(activeMatch.id)
      } else if (matchData.length > 0) {
        setSelectedMatchId(matchData[0].id)
      }
    } catch (err) {
      console.error('매칭 로드 실패:', err)
    }
  }

  const handleStatusChange = async (itemId, statusMark) => {
    try {
      await changeItemStatus(itemId, statusMark)
      await loadPlan()
    } catch (err) {
      alert(err.response?.data?.message || '상태 변경에 실패했습니다.')
    }
  }

  // 선택한 날짜의 아이템 찾기
  const getDayItems = () => {
    if (!plan || !date) return []

    const dateObj = new Date(date)
    const weekStart = new Date(plan.weekStart)
    const dayIndex = Math.floor((dateObj - weekStart) / (1000 * 60 * 60 * 24))

    if (plan.days && plan.days[dayIndex]) {
      return plan.days[dayIndex].items || []
    }
    return []
  }

  const items = getDayItems()
  const workoutItems = items.filter(item => item.itemType === 'WORKOUT')
  const dietItems = items.filter(item => item.itemType === 'DIET')

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50/30 px-6 py-12">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 left-[18%] h-72 w-72 rounded-full bg-indigo-200/40 blur-[120px]" />
        <div className="absolute right-[8%] top-[16%] h-64 w-64 rounded-full bg-purple-200/30 blur-[120px]" />
        <div className="absolute bottom-[-150px] left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full bg-blue-200/30 blur-[140px]" />
      </div>

      <div className="relative mx-auto flex w-full max-w-5xl flex-col gap-8">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h1 className="text-4xl font-bold text-slate-900">{date} 인증</h1>
            <p className="text-sm text-slate-600">하루 플랜 진행 상황을 확인하고 인증하세요.</p>
          </div>
          <Button
            variant="outline"
            className="border-slate-200 bg-white/80 text-slate-700 hover:bg-white hover:border-slate-300 shadow-sm"
            onClick={() => navigate(-1)}
          >
            뒤로가기
          </Button>
        </div>

        {matches.length > 0 && (
          <Card className="rounded-[26px] border border-slate-200 bg-white/95 p-6 shadow-[0_20px_60px_-45px_rgba(15,23,42,0.45)]">
            <CardHeader className="pb-3">
              <CardTitle className="text-lg text-slate-900">매칭 선택</CardTitle>
              <CardDescription className="text-slate-600">인증할 매칭을 선택하세요.</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2 text-left">
                <Label htmlFor="match-select" className="text-sm font-medium text-slate-700">
                  매칭
                </Label>
                <select
                  id="match-select"
                  value={selectedMatchId || ''}
                  onChange={(e) => setSelectedMatchId(e.target.value ? parseInt(e.target.value, 10) : null)}
                  className="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400 focus-visible:border-indigo-400"
                >
                  {matches.map((match) => {
                    const partnerLabel = match.isAiTrainer
                      ? '🤖 AI 트레이너'
                      : (match.partnerName || match.partnerUsername || `트레이너 #${match.trainerId}`);
                    const statusLabel = match.status === 'IN_PROGRESS' ? ' (진행중)'
                      : match.status === 'ACCEPTED' ? ' (승인됨)'
                        : match.status === 'REQUESTED' ? ' (요청됨)'
                          : '';
                    return (
                      <option key={match.id} value={match.id} className="bg-white text-slate-900">
                        매칭 {match.id} - {partnerLabel}{statusLabel}
                      </option>
                    );
                  })}
                </select>
              </div>
            </CardContent>
          </Card>
        )}

        {loading ? (
          <div className="rounded-[26px] border border-slate-200 bg-white/95 py-24 text-center text-sm text-slate-600 shadow-[0_20px_60px_-45px_rgba(15,23,42,0.45)]">
            로딩 중...
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
            <Card className="rounded-[26px] border border-slate-200 bg-white/95 p-6 shadow-[0_20px_60px_-45px_rgba(15,23,42,0.45)]">
              <CardHeader>
                <CardTitle className="text-slate-900">💪 운동 인증</CardTitle>
                <CardDescription className="text-slate-600">오늘의 운동 계획 및 인증 상태입니다.</CardDescription>
              </CardHeader>
              <CardContent>
                {workoutItems.length > 0 ? (
                  <div className="space-y-4">
                    {workoutItems.map((item) => (
                      <div key={item.id} className="rounded-2xl border border-indigo-200 bg-indigo-50/50 p-4 shadow-sm">
                        <div className="flex items-center justify-between text-xs text-slate-600">
                          <span className="uppercase tracking-wide font-medium">운동 항목</span>
                          {item.locked && <span className="text-rose-500">🔒 잠김</span>}
                        </div>
                        <h3 className="mt-2 text-lg font-semibold text-slate-900">{item.title}</h3>
                        {item.description && <p className="mt-1 text-sm text-slate-700">{item.description}</p>}
                        {item.targetMin && (
                          <p className="mt-2 text-xs text-slate-600">목표 시간: {item.targetMin}분</p>
                        )}
                        <div className="mt-4 flex flex-wrap gap-2">
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.id, 'O')}
                            disabled={item.locked}
                            className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide transition ${item.statusMark === 'O'
                                ? 'bg-emerald-500 text-white shadow-md'
                                : 'border border-emerald-300 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 hover:border-emerald-400'
                              } ${item.locked ? 'opacity-40 cursor-not-allowed' : ''}`}
                          >
                            완료
                          </button>
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.id, 'D')}
                            disabled={item.locked}
                            className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide transition ${item.statusMark === 'D'
                                ? 'bg-amber-500 text-white shadow-md'
                                : 'border border-amber-300 bg-amber-50 text-amber-700 hover:bg-amber-100 hover:border-amber-400'
                              } ${item.locked ? 'opacity-40 cursor-not-allowed' : ''}`}
                          >
                            부분
                          </button>
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.id, 'X')}
                            disabled={item.locked}
                            className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide transition ${item.statusMark === 'X'
                                ? 'bg-rose-500 text-white shadow-md'
                                : 'border border-rose-300 bg-rose-50 text-rose-700 hover:bg-rose-100 hover:border-rose-400'
                              } ${item.locked ? 'opacity-40 cursor-not-allowed' : ''}`}
                          >
                            미완료
                          </button>
                        </div>
                        {item.statusMark && (
                          <p className="mt-3 text-xs text-slate-600">
                            상태: {item.statusMark === 'O' ? '✅ 완료' : item.statusMark === 'D' ? '⚠️ 부분' : '❌ 미완료'}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="py-10 text-center text-sm text-slate-600">오늘 운동 계획이 없습니다.</p>
                )}
              </CardContent>
            </Card>

            <Card className="rounded-[26px] border border-slate-200 bg-white/95 p-6 shadow-[0_20px_60px_-45px_rgba(15,23,42,0.45)]">
              <CardHeader>
                <CardTitle className="text-slate-900">🍱 식단 인증</CardTitle>
                <CardDescription className="text-slate-600">식단 목표를 확인하고 인증하세요.</CardDescription>
              </CardHeader>
              <CardContent>
                {dietItems.length > 0 ? (
                  <div className="space-y-4">
                    {dietItems.map((item) => (
                      <div key={item.id} className="rounded-2xl border border-emerald-200 bg-emerald-50/50 p-4 shadow-sm">
                        <div className="flex items-center justify-between text-xs text-slate-600">
                          <span className="uppercase tracking-wide font-medium">식단 항목</span>
                          {item.locked && <span className="text-rose-500">🔒 잠김</span>}
                        </div>
                        <h3 className="mt-2 text-lg font-semibold text-slate-900">{item.title}</h3>
                        {item.description && <p className="mt-1 text-sm text-slate-700">{item.description}</p>}
                        {item.targetKcal && (
                          <p className="mt-2 text-xs text-slate-600">목표 칼로리: {item.targetKcal} kcal</p>
                        )}
                        <div className="mt-4 flex flex-wrap gap-2">
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.id, 'O')}
                            disabled={item.locked}
                            className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide transition ${item.statusMark === 'O'
                                ? 'bg-emerald-500 text-white shadow-md'
                                : 'border border-emerald-300 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 hover:border-emerald-400'
                              } ${item.locked ? 'opacity-40 cursor-not-allowed' : ''}`}
                          >
                            완료
                          </button>
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.id, 'D')}
                            disabled={item.locked}
                            className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide transition ${item.statusMark === 'D'
                                ? 'bg-amber-500 text-white shadow-md'
                                : 'border border-amber-300 bg-amber-50 text-amber-700 hover:bg-amber-100 hover:border-amber-400'
                              } ${item.locked ? 'opacity-40 cursor-not-allowed' : ''}`}
                          >
                            부분
                          </button>
                          <button
                            type="button"
                            onClick={() => handleStatusChange(item.id, 'X')}
                            disabled={item.locked}
                            className={`rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide transition ${item.statusMark === 'X'
                                ? 'bg-rose-500 text-white shadow-md'
                                : 'border border-rose-300 bg-rose-50 text-rose-700 hover:bg-rose-100 hover:border-rose-400'
                              } ${item.locked ? 'opacity-40 cursor-not-allowed' : ''}`}
                          >
                            미완료
                          </button>
                        </div>
                        {item.statusMark && (
                          <p className="mt-3 text-xs text-slate-600">
                            상태: {item.statusMark === 'O' ? '✅ 완료' : item.statusMark === 'D' ? '⚠️ 부분' : '❌ 미완료'}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="py-10 text-center text-sm text-slate-600">오늘 식단 계획이 없습니다.</p>
                )}
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </div>
  )
}

export default Certification

