import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getWeekView, changeItemStatus } from '../api/planApi'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

function PlanDetail() {
  const { matchId, weekStart } = useParams()
  const navigate = useNavigate()
  const [plan, setPlan] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (matchId && weekStart) {
      loadPlan()
    }
  }, [matchId, weekStart])

  const loadPlan = async () => {
    try {
      setLoading(true)
      setError('')
      const data = await getWeekView(parseInt(matchId), weekStart)
      setPlan(data)
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || '플랜을 불러오는데 실패했습니다.'
      setError(errorMessage)
      console.error('플랜 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleStatusChange = async (itemId, statusMark) => {
    try {
      await changeItemStatus(itemId, statusMark)
      loadPlan() // 플랜 다시 로드
    } catch (err) {
      alert(err.response?.data?.message || '상태 변경에 실패했습니다.')
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950">
        <Card className="w-full max-w-md border-white/15 bg-white/10 text-slate-100 backdrop-blur-xl">
          <CardHeader>
            <CardTitle className="text-white">주간 플랜 불러오는 중</CardTitle>
            <CardDescription className="text-slate-200/80">잠시만 기다려 주세요.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="py-6 text-center text-slate-200/70">로딩 중...</div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950">
        <Card className="w-full max-w-md border-white/15 bg-white/10 text-slate-100 backdrop-blur-xl">
          <CardHeader>
            <CardTitle className="text-white">플랜 로드 실패</CardTitle>
            <CardDescription className="text-slate-200/80">플랜 정보를 불러오는 중 오류가 발생했습니다.</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-rose-200/80">{error}</p>
            <Button
              className="mt-4 border-white/20 bg-white/10 text-slate-100 hover:bg-white/20"
              variant="outline"
              onClick={() => navigate(-1)}
            >
              뒤로가기
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (!plan) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950">
        <Card className="w-full max-w-md border-white/15 bg-white/10 text-slate-100 backdrop-blur-xl">
          <CardHeader>
            <CardTitle className="text-white">플랜이 없습니다</CardTitle>
            <CardDescription className="text-slate-200/80">선택한 주차의 플랜이 존재하지 않습니다.</CardDescription>
          </CardHeader>
          <CardContent>
            <Button
              variant="outline"
              className="border-white/20 bg-white/10 text-slate-100 hover:bg-white/20"
              onClick={() => navigate(-1)}
            >
              뒤로가기
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-slate-950 px-6 py-12 text-slate-100">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 left-[18%] h-72 w-72 rounded-full bg-indigo-500/25 blur-[120px]" />
        <div className="absolute right-[10%] top-[14%] h-64 w-64 rounded-full bg-cyan-500/20 blur-[120px]" />
        <div className="absolute bottom-[-160px] left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full bg-violet-500/20 blur-[140px]" />
      </div>

      <div className="relative mx-auto flex w-full max-w-6xl flex-col gap-6">
        <div className="flex flex-col gap-3">
          <Button
            variant="outline"
            className="w-fit border-white/20 bg-white/10 text-slate-100 hover:bg-white/20"
            onClick={() => navigate(-1)}
          >
            ← 이전 페이지로 돌아가기
          </Button>
          <Card className="border-white/15 bg-white/10 text-slate-100 backdrop-blur-xl">
            <CardHeader>
              <CardTitle className="text-3xl font-bold text-white">{plan.title}</CardTitle>
              <CardDescription className="text-slate-200/80">{weekStart} 주간 플랜</CardDescription>
            </CardHeader>
            {plan.note && (
              <CardContent>
                <div className="rounded-2xl border border-dashed border-indigo-400/40 bg-indigo-400/10 p-4 text-sm text-indigo-100">
                  {plan.note}
                </div>
              </CardContent>
            )}
          </Card>
        </div>

        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-4">
          {plan.days?.map((day, dayIndex) => {
            const currentDate = new Date(new Date(weekStart).getTime() + dayIndex * 24 * 60 * 60 * 1000)
            const dayLabel = currentDate.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric', weekday: 'short' })
            return (
              <Card
                key={day.id}
                className="flex flex-col border-white/15 bg-white/10 text-slate-100 shadow-[0_20px_60px_-25px_rgba(15,23,42,0.7)] backdrop-blur-xl"
              >
                <CardHeader className="pb-4">
                  <CardTitle className="text-lg text-indigo-200">{dayLabel}</CardTitle>
                  {day.note && <CardDescription className="whitespace-pre-wrap text-slate-200/70">{day.note}</CardDescription>}
                </CardHeader>
                <CardContent className="flex flex-1 flex-col gap-3">
                  {day.items?.length ? (
                    day.items.map((item) => (
                      <div
                        key={item.id}
                        className="space-y-3 rounded-2xl border border-white/15 bg-white/10 p-4 shadow-inner backdrop-blur"
                      >
                        <div className="flex items-center justify-between text-xs text-slate-200/70">
                          <span>
                            {item.itemType === 'WORKOUT'
                              ? '💪 운동'
                              : item.itemType === 'DIET'
                              ? '🍱 식단'
                              : '📝 메모'}
                          </span>
                          {item.locked && <span className="text-rose-200">🔒 잠김</span>}
                        </div>
                        <div>
                          <h4 className="text-base font-semibold text-white">{item.title}</h4>
                          {item.description && (
                            <p className="mt-1 text-sm text-slate-100/85 whitespace-pre-wrap">{item.description}</p>
                          )}
                          {item.targetKcal && (
                            <p className="mt-2 text-xs text-slate-200/70">목표 칼로리: {item.targetKcal} kcal</p>
                          )}
                          {item.targetMin && (
                            <p className="mt-2 text-xs text-slate-200/70">목표 시간: {item.targetMin} 분</p>
                          )}
                        </div>
                        {!item.locked && (
                          <div className="mt-3 flex flex-wrap gap-2">
                            <Button
                              type="button"
                              size="sm"
                              variant={item.statusMark === 'O' ? 'default' : 'ghost'}
                              className={
                                item.statusMark === 'O'
                                  ? 'bg-emerald-500 hover:bg-emerald-400'
                                  : 'border border-emerald-400/40 bg-emerald-400/10 text-emerald-100 hover:bg-emerald-400/20'
                              }
                              onClick={() => handleStatusChange(item.id, 'O')}
                            >
                              완료
                            </Button>
                            <Button
                              type="button"
                              size="sm"
                              variant={item.statusMark === 'D' ? 'default' : 'ghost'}
                              className={
                                item.statusMark === 'D'
                                  ? 'bg-amber-500 hover:bg-amber-400'
                                  : 'border border-amber-400/40 bg-amber-400/10 text-amber-100 hover:bg-amber-400/20'
                              }
                              onClick={() => handleStatusChange(item.id, 'D')}
                            >
                              부분
                            </Button>
                            <Button
                              type="button"
                              size="sm"
                              variant={item.statusMark === 'X' ? 'default' : 'ghost'}
                              className={
                                item.statusMark === 'X'
                                  ? 'bg-rose-500 hover:bg-rose-400'
                                  : 'border border-rose-400/40 bg-rose-400/10 text-rose-100 hover:bg-rose-400/20'
                              }
                              onClick={() => handleStatusChange(item.id, 'X')}
                            >
                              미완료
                            </Button>
                          </div>
                        )}
                        {item.statusMark && (
                          <p className="text-xs text-slate-200/70">
                            상태: {item.statusMark === 'O' ? '✅ 완료' : item.statusMark === 'D' ? '⚠️ 부분' : '❌ 미완료'}
                          </p>
                        )}
                      </div>
                    ))
                  ) : (
                    <div className="flex h-full items-center justify-center rounded-2xl border border-dashed border-white/20 bg-white/5 py-6 text-sm text-slate-200/70">
                      오늘은 등록된 항목이 없습니다.
                    </div>
                  )}
                </CardContent>
              </Card>
            )
          })}
        </div>
      </div>
    </div>
  )
}

export default PlanDetail

