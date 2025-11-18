import React, { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getWeekView, createWeek, createItem, deleteItem, lockItem } from '../api/planApi'
import { getMyProfile } from '../api/profileApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Card, CardHeader, CardContent, CardTitle, CardDescription } from '@/components/ui/card'

const INITIAL_ITEM_STATE = {
  itemType: 'WORKOUT',
  title: '',
  description: '',
  targetKcal: '',
  targetMin: '',
}

function TrainerPlan() {
  const { matchId, weekStart } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [plan, setPlan] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [profile, setProfile] = useState(null)
  const [showItemForm, setShowItemForm] = useState(null) // dayId
  const creationAttemptRef = useRef(false)
  const [newItem, setNewItem] = useState(INITIAL_ITEM_STATE)

  useEffect(() => {
    if (user?.role === 'TRAINER') {
      preloadProfile()
    }
  }, [user])

  useEffect(() => {
    creationAttemptRef.current = false
    if (matchId && weekStart) {
      loadPlan()
    }
  }, [matchId, weekStart])

  const preloadProfile = async () => {
    try {
      const data = await getMyProfile()
      setProfile(data)
    } catch (err) {
      console.error('프로필 로드 실패:', err)
    }
  }

  const ensureTrainerAccountId = async () => {
    if (profile?.accountId) return profile.accountId
    try {
      const data = await getMyProfile()
      setProfile(data)
      return data.accountId
    } catch (err) {
      console.error('트레이너 계정 정보 조회 실패:', err)
      alert('계정 정보를 불러오지 못했습니다. 다시 시도해주세요.')
      return null
    }
  }

  const createWeekForTrainer = async () => {
    if (user?.role !== 'TRAINER') return false
    const confirmed = window.confirm('해당 주차 플랜이 없습니다. 새 플랜을 생성할까요?')
    if (!confirmed) {
      setError('해당 주차 플랜이 아직 생성되지 않았습니다.')
      return false
    }

    const accountId = await ensureTrainerAccountId()
    if (!accountId) {
      return false
    }

    try {
      await createWeek(
        parseInt(matchId, 10),
        weekStart,
        `${weekStart} 주간 플랜`,
        '',
        accountId
      )
      return true
    } catch (createErr) {
      const createMessage =
        createErr.response?.data?.message || '주간 플랜 생성에 실패했습니다. 잠시 후 다시 시도해주세요.'
      alert(createMessage)
      setError(createMessage)
      console.error('주간 플랜 생성 실패:', createErr)
      return false
    }
  }

  const loadPlan = async (options = { allowCreate: true }) => {
    try {
      setLoading(true)
      setError('')
      const data = await getWeekView(parseInt(matchId, 10), weekStart)
      setPlan(data)
    } catch (err) {
      if (
        err.response?.status === 404 &&
        options.allowCreate &&
        user?.role === 'TRAINER' &&
        !creationAttemptRef.current
      ) {
        creationAttemptRef.current = true
        const created = await createWeekForTrainer()
        if (created) {
          return loadPlan({ allowCreate: false })
        }
        creationAttemptRef.current = false
      }
      const errorMessage = err.response?.data?.message || err.message || '플랜을 불러오는데 실패했습니다.'
      setError(errorMessage)
      console.error('플랜 로드 실패:', err)
      creationAttemptRef.current = false
    } finally {
      setLoading(false)
    }
  }

  const toggleItemForm = (dayId) => {
    if (showItemForm === dayId) {
      setShowItemForm(null)
      setNewItem({ ...INITIAL_ITEM_STATE })
    } else {
      setShowItemForm(dayId)
      setNewItem({ ...INITIAL_ITEM_STATE })
    }
  }

  const handleCreateItem = async (dayId, e) => {
    e.preventDefault()
    try {
      await createItem(
        dayId,
        newItem.itemType,
        newItem.title,
        newItem.description,
        newItem.targetKcal ? parseInt(newItem.targetKcal) : null,
        newItem.targetMin ? parseInt(newItem.targetMin) : null
      )
      setShowItemForm(null)
      setNewItem({ ...INITIAL_ITEM_STATE })
      await loadPlan()
    } catch (err) {
      alert(err.response?.data?.message || '아이템 생성에 실패했습니다.')
    }
  }

  const handleDeleteItem = async (itemId) => {
    if (!confirm('이 아이템을 삭제하시겠습니까?')) return
    try {
      await deleteItem(itemId)
      await loadPlan()
    } catch (err) {
      alert(err.response?.data?.message || '아이템 삭제에 실패했습니다.')
    }
  }

  const handleLockItem = async (itemId, locked) => {
    try {
      await lockItem(itemId, locked)
      await loadPlan()
    } catch (err) {
      alert(err.response?.data?.message || '잠금 상태 변경에 실패했습니다.')
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950">
        <Card className="w-full max-w-md border-white/15 bg-white/10 text-slate-100 backdrop-blur-xl">
          <CardHeader>
            <CardTitle className="text-white">주간 플랜을 불러오는 중</CardTitle>
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
            <CardDescription className="text-slate-200/80">플랜 정보를 불러오던 중 문제가 발생했습니다.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-sm text-rose-200/80">{error}</p>
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
        <div className="absolute -top-32 left-[15%] h-80 w-80 rounded-full bg-indigo-500/25 blur-[120px]" />
        <div className="absolute right-[10%] top-[10%] h-64 w-64 rounded-full bg-cyan-500/20 blur-[110px]" />
        <div className="absolute bottom-[-160px] left-1/2 h-96 w-96 -translate-x-1/2 rounded-full bg-violet-500/20 blur-[140px]" />
        <div className="absolute left-[5%] top-1/2 h-40 w-40 animate-[pulse-glow_6s_ease-in-out_infinite] rounded-full bg-white/10 blur-[90px]" />
      </div>

      <div className="relative mx-auto flex w-full max-w-6xl flex-col gap-8">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <span className="inline-flex items-center rounded-full border border-white/10 bg-white/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.25em] text-indigo-100">
              Trainer Plan
            </span>
            <h1 className="mt-4 text-4xl font-bold tracking-tight text-white md:text-5xl">{plan.title}</h1>
            <p className="mt-3 text-sm text-slate-200/80">{weekStart} 주간 플랜 · 트레이너 전용 관리 화면</p>
          </div>
          <Button
            variant="outline"
            className="border-white/20 bg-white/5 text-slate-100 hover:bg-white/10"
            onClick={() => navigate(-1)}
          >
            뒤로가기
          </Button>
        </div>

        {plan.note && (
          <Card className="border-white/15 bg-white/5 text-slate-100 backdrop-blur-xl">
            <CardHeader>
              <CardTitle className="text-xl text-white">플랜 노트</CardTitle>
              <CardDescription className="text-slate-200/70">
                주간 공지사항이나 메모를 확인하세요.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="whitespace-pre-wrap rounded-xl border border-white/15 bg-white/10 p-5 text-sm text-slate-100/90 shadow-inner">
                {plan.note}
              </div>
            </CardContent>
          </Card>
        )}

        <div className="flex flex-col gap-8">
          {plan.days?.map((day, dayIndex) => {
            const dayDate = new Date(plan.weekStart)
            dayDate.setDate(dayDate.getDate() + dayIndex)
            const dateLabel = dayDate.toLocaleDateString('ko-KR', {
              month: 'long',
              day: 'numeric',
              weekday: 'short',
            })
            return (
              <Card
                key={day.id}
                className="border-white/15 bg-white/5 text-slate-100 shadow-[0_20px_60px_-25px_rgba(15,23,42,0.7)] backdrop-blur-xl"
              >
                <CardHeader className="pb-4">
                  <CardTitle className="text-2xl text-white">{dateLabel}</CardTitle>
                  {day.note && (
                    <CardDescription className="whitespace-pre-wrap text-slate-200/70">
                      {day.note}
                    </CardDescription>
                  )}
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="space-y-4">
                    {day.items?.length ? (
                      day.items.map((item) => {
                        const baseClasses =
                          item.itemType === 'WORKOUT'
                            ? 'border-indigo-400/40 bg-indigo-400/10'
                            : item.itemType === 'DIET'
                            ? 'border-emerald-400/35 bg-emerald-400/10'
                            : 'border-slate-200/30 bg-slate-500/10'
                        return (
                          <div key={item.id} className={`rounded-2xl border p-5 shadow-inner ${baseClasses}`}>
                            <div className="flex flex-wrap items-center justify-between gap-3">
                              <span className="text-xs font-semibold uppercase tracking-wide text-slate-100/80">
                                {item.itemType === 'WORKOUT'
                                  ? '💪 운동'
                                  : item.itemType === 'DIET'
                                  ? '🍱 식단'
                                  : '📝 메모'}
                              </span>
                              <div className="flex flex-wrap gap-2">
                                <Button
                                  type="button"
                                  size="sm"
                                  variant="ghost"
                                  className="border border-white/10 bg-white/10 text-xs text-slate-100 hover:bg-white/20"
                                  onClick={() => handleLockItem(item.id, !item.locked)}
                                >
                                  {item.locked ? '잠금 해제' : '잠금'}
                                </Button>
                                <Button
                                  type="button"
                                  size="sm"
                                  variant="destructive"
                                  className="text-xs"
                                  onClick={() => handleDeleteItem(item.id)}
                                >
                                  삭제
                                </Button>
                              </div>
                            </div>
                            <div className="mt-3 space-y-2 text-left">
                              <h4 className="text-lg font-semibold text-white">{item.title}</h4>
                              {item.description && (
                                <p className="whitespace-pre-wrap text-sm text-slate-100/90">{item.description}</p>
                              )}
                              {item.targetKcal && (
                                <p className="text-xs text-slate-100/70">목표 칼로리: {item.targetKcal} kcal</p>
                              )}
                              {item.targetMin && (
                                <p className="text-xs text-slate-100/70">목표 시간: {item.targetMin} 분</p>
                              )}
                              {item.statusMark && (
                                <p className="text-xs font-semibold text-slate-100/80">
                                  상태:{' '}
                                  {item.statusMark === 'O'
                                    ? '✅ 완료'
                                    : item.statusMark === 'D'
                                    ? '⚠️ 부분'
                                    : '❌ 미완료'}
                                </p>
                              )}
                            </div>
                          </div>
                        )
                      })
                    ) : (
                      <div className="rounded-2xl border border-dashed border-white/20 bg-white/5 py-10 text-center text-sm text-slate-200/70">
                        아직 등록된 항목이 없습니다.
                      </div>
                    )}
                  </div>

                  <div className="space-y-4">
                    <Button
                      type="button"
                      variant={showItemForm === day.id ? 'secondary' : 'outline'}
                      className="w-full border-white/20 bg-white/10 text-slate-100 hover:bg-white/20"
                      onClick={() => toggleItemForm(day.id)}
                    >
                      {showItemForm === day.id ? '아이템 추가 닫기' : '+ 아이템 추가'}
                    </Button>

                    {showItemForm === day.id && (
                      <form
                        onSubmit={(e) => handleCreateItem(day.id, e)}
                        className="space-y-5 rounded-2xl border border-white/15 bg-white/10 p-5 text-left shadow-inner backdrop-blur"
                      >
                        <div className="grid gap-2">
                          <Label className="text-sm font-medium text-slate-200">항목 종류</Label>
                          <select
                            value={newItem.itemType}
                            onChange={(e) => setNewItem({ ...newItem, itemType: e.target.value })}
                            className="w-full rounded-md border border-white/15 bg-slate-900/60 px-3 py-2 text-sm text-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400"
                            required
                          >
                            <option value="WORKOUT" className="bg-slate-900">운동</option>
                            <option value="DIET" className="bg-slate-900">식단</option>
                            <option value="NOTE" className="bg-slate-900">메모</option>
                          </select>
                        </div>

                        <div className="grid gap-2">
                          <Label htmlFor={`title-${day.id}`} className="text-sm font-medium text-slate-200">
                            제목
                          </Label>
                          <Input
                            id={`title-${day.id}`}
                            value={newItem.title}
                            onChange={(e) => setNewItem({ ...newItem, title: e.target.value })}
                            placeholder="예: 상체 근력 운동"
                            className="border-white/15 bg-white/10 text-slate-100 placeholder:text-slate-200/50 focus-visible:ring-indigo-400"
                            required
                          />
                        </div>

                        <div className="grid gap-2">
                          <Label htmlFor={`desc-${day.id}`} className="text-sm font-medium text-slate-200">
                            설명
                          </Label>
                          <Textarea
                            id={`desc-${day.id}`}
                            value={newItem.description}
                            onChange={(e) => setNewItem({ ...newItem, description: e.target.value })}
                            placeholder="세부 계획 또는 메모를 입력하세요."
                            className="border-white/15 bg-white/10 text-slate-100 placeholder:text-slate-200/50 focus-visible:ring-indigo-400"
                            rows={3}
                          />
                        </div>

                        {newItem.itemType === 'DIET' && (
                          <div className="grid gap-2">
                            <Label htmlFor={`kcal-${day.id}`} className="text-sm font-medium text-slate-200">
                              목표 칼로리
                            </Label>
                            <Input
                              id={`kcal-${day.id}`}
                              type="number"
                              value={newItem.targetKcal}
                              onChange={(e) => setNewItem({ ...newItem, targetKcal: e.target.value })}
                              placeholder="예: 1800"
                              className="border-white/15 bg-white/10 text-slate-100 placeholder:text-slate-200/50 focus-visible:ring-indigo-400"
                            />
                          </div>
                        )}

                        {newItem.itemType === 'WORKOUT' && (
                          <div className="grid gap-2">
                            <Label htmlFor={`minutes-${day.id}`} className="text-sm font-medium text-slate-200">
                              목표 시간 (분)
                            </Label>
                            <Input
                              id={`minutes-${day.id}`}
                              type="number"
                              value={newItem.targetMin}
                              onChange={(e) => setNewItem({ ...newItem, targetMin: e.target.value })}
                              placeholder="예: 45"
                              className="border-white/15 bg-white/10 text-slate-100 placeholder:text-slate-200/50 focus-visible:ring-indigo-400"
                            />
                          </div>
                        )}

                        <div className="flex justify-end gap-2">
                          <Button
                            type="button"
                            variant="ghost"
                            className="border border-white/15 bg-white/5 text-slate-100 hover:bg-white/15"
                            onClick={() => {
                              setShowItemForm(null)
                              setNewItem({ ...INITIAL_ITEM_STATE })
                            }}
                          >
                            취소
                          </Button>
                          <Button type="submit" className="bg-indigo-500 hover:bg-indigo-400">
                            추가
                          </Button>
                        </div>
                      </form>
                    )}
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </div>
      </div>
    </div>
  )
}

export default TrainerPlan

