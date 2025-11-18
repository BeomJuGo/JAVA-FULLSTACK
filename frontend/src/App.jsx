import React, { useState, useEffect, useMemo, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { getMyMatches } from './api/matchApi'
import { getWeekView } from './api/planApi'
import FullCalendar from '@fullcalendar/react'
import dayGridPlugin from '@fullcalendar/daygrid'
import interactionPlugin from '@fullcalendar/interaction'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Avatar, AvatarFallback } from '@/components/ui/avatar'
import { Separator } from '@/components/ui/separator'
import StaggeredMenu from '@/components/StaggeredMenu'
import GradientText from '@/components/GradientText'
import './App.css'
import './index.css'
import healthwebLogo from './assets/healthweb logo.png'

function App() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [matches, setMatches] = useState([])
  const [selectedMatchId, setSelectedMatchId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [todayPlan, setTodayPlan] = useState(null)
  const [calendarPlans, setCalendarPlans] = useState({}) // 날짜별 플랜 데이터
  const calendarPlansRef = useRef({}) // dayCellDidMount에서 최신 값 참조용
  const calendarRef = useRef(null) // FullCalendar 인스턴스 참조

  const toDateKey = (date) => {
    const base = new Date(date)
    const tzOffset = base.getTimezoneOffset() * 60000
    return new Date(base.getTime() - tzOffset).toISOString().split('T')[0]
  }

  useEffect(() => {
    if (user) {
      loadData()
    }
  }, [user])

  useEffect(() => {
    if (selectedMatchId && user?.role === 'USER') {
      loadTodayPlan()
    }
  }, [selectedMatchId, user])

  // calendarPlans가 변경될 때 캘린더 인디케이터 업데이트
  useEffect(() => {
    if (!calendarRef.current) return

    // 약간의 지연을 두어 DOM이 완전히 렌더링된 후 업데이트
    const timer = setTimeout(() => {
      document.querySelectorAll('.fc-daygrid-day').forEach((cell) => {
        const dateStr = cell.getAttribute('data-date')
        if (dateStr) {
          const dayData = calendarPlans[dateStr] || null
          renderIndicatorsForCell(cell, dayData)
        }
      })
    }, 100)

    return () => clearTimeout(timer)
  }, [calendarPlans])

  // 캘린더 뷰 변경 시 플랜 다시 로드
  const handleDatesSet = (arg) => {
    if (selectedMatchId && user?.role === 'USER') {
      loadCalendarPlansForMonth(arg.start, arg.end)
    }
  }

  // 특정 월의 플랜 데이터 로드
  const loadCalendarPlansForMonth = async (start, end) => {
    if (!selectedMatchId) return
    try {
      const rangeInfo = {
        start: start?.toISOString?.() ?? String(start),
        end: end?.toISOString?.() ?? String(end),
        matchId: selectedMatchId,
      }
      console.log('[Calendar] loadCalendarPlansForMonth start', rangeInfo)

      const plans = {}

      // 시작일의 월요일 찾기
      const firstDay = new Date(start)
      const firstDayOfWeek = firstDay.getDay()
      const diffToMonday = firstDayOfWeek === 0 ? -6 : 1 - firstDayOfWeek
      const firstMonday = new Date(firstDay)
      firstMonday.setDate(firstDay.getDate() + diffToMonday)

      // 종료일의 월요일 찾기
      const lastDay = new Date(end)
      const lastDayOfWeek = lastDay.getDay()
      const diffToLastMonday = lastDayOfWeek === 0 ? -6 : 1 - lastDayOfWeek
      const lastMonday = new Date(lastDay)
      lastMonday.setDate(lastDay.getDate() + diffToLastMonday)

      // 각 주차별로 플랜 로드
      const currentWeek = new Date(firstMonday)
      while (currentWeek <= lastMonday) {
        const weekStart = toDateKey(currentWeek)
        try {
          const plan = await getWeekView(selectedMatchId, weekStart)
          console.log('[Calendar] fetched week', {
            weekStart,
            matchId: selectedMatchId,
            dayCount: plan.days?.length ?? 0,
            raw: plan,
          })
          plan.days?.forEach((day, dayIndex) => {
            const date = new Date(currentWeek)
            date.setDate(currentWeek.getDate() + dayIndex)
            const dateStr = toDateKey(date)
            plans[dateStr] = day
          })
        } catch (err) {
          // 해당 주차 플랜이 없으면 무시
          console.warn('[Calendar] week plan missing', {
            weekStart,
            matchId: selectedMatchId,
            error: err?.response?.data ?? err?.message ?? err,
          })
        }
        currentWeek.setDate(currentWeek.getDate() + 7)
      }

      setCalendarPlans(plans)
      calendarPlansRef.current = plans // ref도 업데이트
      window.__calendarPlans = plans
      window.__calendarDebug = {
        ...(window.__calendarDebug || {}),
        lastLoadedRange: rangeInfo,
        lastLoadedAt: new Date().toISOString(),
      }

      // 캘린더 인디케이터 업데이트 (약간의 지연을 두어 상태 업데이트 후 실행)
      setTimeout(() => {
        if (calendarRef.current) {
          document.querySelectorAll('.fc-daygrid-day').forEach((cell) => {
            const dateStr = cell.getAttribute('data-date')
            if (dateStr) {
              const dayData = plans[dateStr] || null
              renderIndicatorsForCell(cell, dayData)
            }
          })
        }
      }, 200)
    } catch (err) {
      console.error('캘린더 플랜 로드 실패:', err)
      window.__calendarDebug = {
        ...(window.__calendarDebug || {}),
        lastError: err?.response?.data ?? err?.message ?? err,
      }
    }
  }

  const loadData = async () => {
    try {
      setLoading(true)
      const matchData = await getMyMatches().catch(() => [])
      setMatches(matchData)
      const activeMatch = matchData.find((m) => m.status === 'IN_PROGRESS' || m.status === 'ACCEPTED')
      if (activeMatch && !selectedMatchId) {
        setSelectedMatchId(activeMatch.id)
      }
    } catch (err) {
      console.error('데이터 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  // 오늘 날짜의 플랜 로드
  const loadTodayPlan = async () => {
    if (!selectedMatchId) return
    try {
      const today = new Date()
      const day = today.getDay()
      const diff = day === 0 ? -6 : 1 - day
      const monday = new Date(today)
      monday.setDate(today.getDate() + diff)
      const weekStart = toDateKey(monday)

      const plan = await getWeekView(selectedMatchId, weekStart)
      const todayIndex = today.getDay() === 0 ? 6 : today.getDay() - 1
      console.log('[Calendar] loadTodayPlan result', {
        matchId: selectedMatchId,
        weekStart,
        todayIndex,
        plan,
      })
      const todayDay = plan.days?.[todayIndex]
      setTodayPlan(todayDay)
      window.__calendarDebug = {
        ...(window.__calendarDebug || {}),
        lastTodayPlan: {
          matchId: selectedMatchId,
          weekStart,
          todayIndex,
          items: todayDay?.items?.length ?? 0,
        },
      }
    } catch (err) {
      console.error('오늘 플랜 로드 실패:', err)
      setTodayPlan(null)
      window.__calendarDebug = {
        ...(window.__calendarDebug || {}),
        lastTodayPlanError: err?.response?.data ?? err?.message ?? err,
      }
    }
  }


  const handleDateClick = async (info) => {
    const dateStr = info.dateStr

    if (user?.role === 'USER') {
      navigate(`/certification/${dateStr}`)
    } else if (user?.role === 'TRAINER') {
      const date = new Date(dateStr)
      const day = date.getDay()
      const diff = day === 0 ? -6 : 1 - day
      const monday = new Date(date)
      monday.setDate(date.getDate() + diff)
      const weekStart = toDateKey(monday)

      if (selectedMatchId) {
        navigate(`/trainer-plan/${selectedMatchId}/${weekStart}`)
      } else {
        alert('먼저 매칭을 선택해주세요.')
      }
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  // 운동 수행 상태에 따른 색상 (인라인 스타일용)
  const getWorkoutStatusColor = (items) => {
    if (!items || items.length === 0) return 'rgba(51, 65, 85, 0.5)'
    const workoutItems = items.filter((item) => item.itemType === 'WORKOUT')
    if (workoutItems.length === 0) return 'rgba(51, 65, 85, 0.5)'

    const allCompleted = workoutItems.every((item) => item.statusMark === 'O')
    const someCompleted = workoutItems.some((item) => item.statusMark === 'O' || item.statusMark === 'D')

    if (allCompleted) return 'rgba(34, 197, 94, 0.85)'
    if (someCompleted) return 'rgba(234, 179, 8, 0.85)'
    return 'rgba(239, 68, 68, 0.85)'
  }

  const getDietStatusColor = (items) => {
    if (!items || items.length === 0) return 'rgba(51, 65, 85, 0.35)'
    const dietItems = items.filter((item) => item.itemType === 'DIET')
    if (dietItems.length === 0) return 'rgba(51, 65, 85, 0.35)'

    const allCompleted = dietItems.every((item) => item.statusMark === 'O')
    const someCompleted = dietItems.some((item) => item.statusMark === 'O' || item.statusMark === 'D')

    if (allCompleted) return 'rgba(34, 197, 94, 0.85)'
    if (someCompleted) return 'rgba(234, 179, 8, 0.85)'
    return 'rgba(239, 68, 68, 0.85)'
  }

  const renderIndicatorsForCell = (cell, dayData) => {
    if (!cell) return
    const frame = cell.querySelector('.fc-daygrid-day-frame')
    if (!frame) return

    // frame이 relative positioning을 가지도록 설정
    if (getComputedStyle(frame).position === 'static') {
      frame.style.position = 'relative'
    }

    // 기존 인디케이터 제거
    frame.querySelectorAll('.custom-day-indicator').forEach((indicator) => indicator.remove())

    const workoutItems = dayData?.items?.filter((item) => item.itemType === 'WORKOUT') || []
    const dietItems = dayData?.items?.filter((item) => item.itemType === 'DIET') || []

    const workoutColor = getWorkoutStatusColor(dayData?.items)
    const dietColor = dietItems.length > 0 ? getDietStatusColor(dayData?.items) : 'rgba(51, 65, 85, 0.35)'

    const indicatorContainer = document.createElement('div')
    indicatorContainer.className = 'custom-day-indicator'
    indicatorContainer.style.cssText = `
      position: absolute;
      top: 30px;
      left: 4px;
      right: 4px;
      display: flex;
      flex-direction: column;
      gap: 6px;
      pointer-events: none;
      z-index: 1;
    `

    const workoutIndicator = document.createElement('div')
    workoutIndicator.style.cssText = `
      height: 16px;
      width: 100%;
      border-radius: 6px;
      background-color: ${workoutColor};
      opacity: ${workoutItems.length > 0 ? '1' : '0.3'};
      min-width: 0;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
      font-weight: 600;
      color: rgba(15, 23, 42, 0.9);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    `
    workoutIndicator.textContent = '운동'
    workoutIndicator.title = `운동: ${workoutItems.length > 0 ? workoutItems.map((i) => i.title).join(', ') : '없음'}`

    const dietIndicator = document.createElement('div')
    dietIndicator.style.cssText = `
      height: 16px;
      width: 100%;
      border-radius: 6px;
      background-color: ${dietColor};
      opacity: ${dietItems.length > 0 ? '1' : '0.3'};
      min-width: 0;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
      font-weight: 600;
      color: rgba(15, 23, 42, 0.9);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    `
    dietIndicator.textContent = '식단'
    dietIndicator.title = `식단: ${dietItems.length > 0 ? dietItems.map((i) => i.title).join(', ') : '없음'}`

    indicatorContainer.appendChild(workoutIndicator)
    indicatorContainer.appendChild(dietIndicator)

    frame.appendChild(indicatorContainer)
  }


  // summaryCards 동적 생성
  const summaryCards = useMemo(() => {
    const workoutItems = todayPlan?.items?.filter(item => item.itemType === 'WORKOUT') || []
    const dietItems = todayPlan?.items?.filter(item => item.itemType === 'DIET') || []

    return [
      {
        label: '오늘의 운동',
        icon: '🏋️‍♀️',
        gradient: 'from-indigo-500/60 to-indigo-400/20',
        content: workoutItems.length > 0
          ? workoutItems.map(item => item.title).join(', ')
          : '운동 계획이 없습니다'
      },
      {
        label: '오늘의 식단',
        icon: '🥗',
        gradient: 'from-emerald-500/60 to-emerald-400/20',
        content: dietItems.length > 0
          ? dietItems.map(item => item.title).join(', ')
          : '식단 계획이 없습니다'
      },
      {
        label: '진행 상황',
        icon: '📊',
        gradient: 'from-sky-500/60 to-sky-400/20',
        content: todayPlan
          ? `운동 ${workoutItems.length}개, 식단 ${dietItems.length}개`
          : '플랜을 선택해주세요'
      },
    ]
  }, [todayPlan])

  const quickActions = useMemo(
    () =>
      [
        { path: '/ai-recommendation', label: 'AI 추천', icon: '🤖', role: 'USER' },
        { path: '/community', label: '커뮤니티', icon: '💬' },
        { path: '/profiles/trainers', label: '트레이너', icon: '👨‍🏫' },
        { path: '/matches', label: '매칭', icon: '🤝' },
        { path: '/reviews', label: '리뷰', icon: '⭐' },
        { path: '/admin', label: '관리자', icon: '⚙️', role: 'ADMIN' },
      ].filter((action) => !action.role || user?.role === action.role),
    [user]
  )

  const staggeredMenuItems = useMemo(
    () =>
      quickActions.map((action) => ({
        label: `${action.icon} ${action.label}`,
        ariaLabel: `${action.label} 페이지로 이동`,
        onClick: () => navigate(action.path),
      })),
    [navigate, quickActions]
  )

  const socialMenuItems = useMemo(
    () => [
      { label: 'GitHub', link: 'https://github.com/BeomJuGo/JAVA-FULLSTACK' },

    ],
    []
  )

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-600">
        <div className="rounded-3xl border border-white/70 bg-white px-6 py-4 shadow-lg">로딩 중...</div>
      </div>
    )
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-10 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-20 left-[10%] h-64 w-64 rounded-full bg-blue-200/40 blur-[100px]" />
        <div className="absolute right-[8%] top-[10%] h-72 w-72 rounded-full bg-purple-200/35 blur-[110px]" />
        <div className="absolute bottom-[-120px] left-1/2 h-[360px] w-[360px] -translate-x-1/2 rounded-full bg-indigo-100/60 blur-[140px]" />
      </div>
      {user && (
        <div className="pointer-events-none absolute right-4 top-4 z-20 sm:right-8 sm:top-6">
          <div className="pointer-events-auto">
            <StaggeredMenu
              className="inline-header-menu"
              items={staggeredMenuItems}
              socialItems={socialMenuItems}
              displaySocials={false}
              displayItemNumbering
              menuButtonColor="#0f172a"
              openMenuButtonColor="#ffffff"
              changeMenuColorOnOpen
              colors={['#1E1B4B', '#4C1D95', '#6D28D9']}
              logoUrl={healthwebLogo}
              accentColor="#8B5CF6"
              position="right"
              showLogo={false}
              menuLabel="Menu"
              closeLabel="Close"
            />
          </div>
        </div>
      )}

      <div className="relative mx-auto flex w-full max-w-6xl flex-col gap-8">
        <header className="rounded-3xl border border-white/70 bg-white/80 p-6 shadow-[0_25px_80px_-40px_rgba(15,23,42,0.45)] backdrop-blur-xl">
          <div className="flex flex-col gap-6 md:flex-row md:items-center md:justify-between">
            <div className="flex items-center gap-4">
              <img
                src={healthwebLogo}
                alt="HealthWeb 로고"
                className="h-16 w-40 rounded-2xl bg-gradient-to-br from-blue-500/15 to-purple-500/25 p-2 shadow-lg object-contain"
              />
              <div>
                <GradientText
                  colors={['#6366f1', '#8b5cf6', '#6366f1', '#8b5cf6', '#6366f1']}
                  animationSpeed={5}
                  showBorder={false}
                  className="text-xs font-semibold uppercase tracking-[0.3em]"
                >
                  Healthy Lifestyle Platform
                </GradientText>
                <GradientText
                  colors={['#3b82f6', '#8b5cf6', '#ec4899', '#8b5cf6', '#3b82f6']}
                  animationSpeed={8}
                  showBorder={false}
                  className="text-4xl font-bold tracking-tight mt-1"
                >
                  HealthWeb Dashboard
                </GradientText>
                <p className="mt-1 text-sm text-slate-500">밝고 직관적인 인터페이스에서 플랜을 관리하세요.</p>
              </div>
            </div>
            {user && (
              <div className="flex flex-wrap items-center justify-end gap-3">
                <div className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white/80 px-4 py-2 shadow-sm">
                  <Avatar className="h-8 w-8">
                    <AvatarFallback className="bg-gradient-to-br from-indigo-500 to-purple-500 text-white text-xs font-semibold">
                      {user.username?.charAt(0).toUpperCase() || 'U'}
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium text-slate-900">{user.username}</span>
                    <Badge
                      variant={user.role === 'ADMIN' ? 'destructive' : user.role === 'TRAINER' ? 'default' : 'secondary'}
                      className="text-xs"
                    >
                      {user.role === 'ADMIN' ? '관리자' : user.role === 'TRAINER' ? '트레이너' : '사용자'}
                    </Badge>
                  </div>
                </div>
                <Button
                  variant="outline"
                  className="border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                  onClick={() => navigate('/mypage')}
                >
                  마이페이지
                </Button>
                <Button
                  className="bg-gradient-to-r from-blue-500 to-indigo-500 shadow-lg hover:from-blue-600 hover:to-indigo-600"
                  onClick={handleLogout}
                >
                  로그아웃
                </Button>
              </div>
            )}
          </div>
        </header>

        {user && (
          <section className="rounded-[28px] border border-slate-200/80 bg-white/90 p-6 shadow-[0_22px_70px_-45px_rgba(15,23,42,0.65)] backdrop-blur">
            <div className="flex flex-col gap-1">
              <p className="text-xs font-semibold uppercase tracking-[0.3em] text-slate-400">MATCHING</p>
              <div className="flex flex-col gap-2 lg:flex-row lg:items-center lg:justify-between">
                <h2 className="text-2xl font-semibold text-slate-900">매칭 선택</h2>
                <p className="text-sm text-slate-500">플랜을 관리할 매칭을 선택하고 실시간으로 확인하세요.</p>
              </div>
            </div>
            <Separator className="my-4" />
            <div className="mt-5 flex flex-col gap-3 lg:flex-row">
              <div className="flex-1 rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-inner">
                <select
                  value={selectedMatchId || ''}
                  onChange={(e) => setSelectedMatchId(e.target.value ? parseInt(e.target.value, 10) : null)}
                  className="w-full bg-transparent text-sm text-slate-800 focus-visible:outline-none"
                >
                  <option value="">매칭을 선택하세요</option>
                  {matches.map((match) => {
                    const partnerLabel = match.isAiTrainer
                      ? '🤖 AI 트레이너'
                      : (match.partnerName || match.partnerUsername || `트레이너 #${match.trainerId}`);
                    const statusLabel = match.status === 'IN_PROGRESS' ? ' (진행중)'
                      : match.status === 'ACCEPTED' ? ' (승인됨)'
                        : match.status === 'REQUESTED' ? ' (요청됨)'
                          : '';
                    return (
                      <option key={match.id} value={match.id}>
                        매칭 {match.id} - {partnerLabel}{statusLabel}
                      </option>
                    );
                  })}
                </select>
              </div>
              <Button
                variant="outline"
                className="h-12 rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                onClick={loadData}
              >
                새로고침
              </Button>
            </div>
            {selectedMatchId && matches.length > 0 && (
              <div className="mt-4 flex items-center gap-2">
                {(() => {
                  const selectedMatch = matches.find(m => m.id === selectedMatchId)
                  if (!selectedMatch) return null
                  const statusConfig = {
                    'IN_PROGRESS': { label: '진행중', variant: 'default', className: 'bg-emerald-500 hover:bg-emerald-600' },
                    'ACCEPTED': { label: '승인됨', variant: 'secondary', className: 'bg-blue-500 hover:bg-blue-600' },
                    'REQUESTED': { label: '요청됨', variant: 'outline', className: 'bg-amber-500 hover:bg-amber-600' },
                  }
                  const config = statusConfig[selectedMatch.status] || { label: selectedMatch.status, variant: 'outline' }
                  return (
                    <div className="flex items-center gap-2">
                      <span className="text-sm text-slate-600">상태:</span>
                      <Badge variant={config.variant} className={`${config.className || ''} text-white border-0`}>
                        {config.label}
                      </Badge>
                    </div>
                  )
                })()}
              </div>
            )}
            {matches.length === 0 && (
              <p className="mt-2 text-sm text-slate-500">매칭이 없습니다. 매칭을 생성해주세요.</p>
            )}
            {!selectedMatchId && matches.length > 0 && (
              <p className="mt-2 text-sm text-amber-500">플랜을 보려면 먼저 매칭을 선택해주세요.</p>
            )}
          </section>
        )}

        {user?.role !== 'TRAINER' && (
          <section className="grid grid-cols-1 gap-6 md:grid-cols-3">
            {summaryCards.map(({ label, icon, gradient, content }, index) => {
              const cardConfigs = [
                {
                  bgGradient: 'from-indigo-50 via-indigo-50/50 to-white',
                  borderColor: 'border-indigo-200/60',
                  iconBg: 'from-indigo-500 to-purple-500',
                  shadow: 'shadow-[0_20px_60px_-30px_rgba(99,102,241,0.4)]',
                  hoverShadow: 'hover:shadow-[0_25px_70px_-25px_rgba(99,102,241,0.5)]',
                },
                {
                  bgGradient: 'from-emerald-50 via-emerald-50/50 to-white',
                  borderColor: 'border-emerald-200/60',
                  iconBg: 'from-emerald-500 to-teal-500',
                  shadow: 'shadow-[0_20px_60px_-30px_rgba(16,185,129,0.4)]',
                  hoverShadow: 'hover:shadow-[0_25px_70px_-25px_rgba(16,185,129,0.5)]',
                },
                {
                  bgGradient: 'from-sky-50 via-sky-50/50 to-white',
                  borderColor: 'border-sky-200/60',
                  iconBg: 'from-sky-500 to-blue-500',
                  shadow: 'shadow-[0_20px_60px_-30px_rgba(14,165,233,0.4)]',
                  hoverShadow: 'hover:shadow-[0_25px_70px_-25px_rgba(14,165,233,0.5)]',
                },
              ]
              const config = cardConfigs[index] || cardConfigs[0]

              return (
                <div
                  key={label}
                  className={`group relative overflow-hidden rounded-[28px] border-2 ${config.borderColor} bg-gradient-to-br ${config.bgGradient} p-6 ${config.shadow} transition-all duration-500 hover:-translate-y-2 ${config.hoverShadow}`}
                >
                  {/* 배경 장식 요소 */}
                  <div className="absolute -right-8 -top-8 h-32 w-32 rounded-full bg-gradient-to-br from-white/40 to-transparent blur-2xl" />
                  <div className="absolute -bottom-4 -left-4 h-24 w-24 rounded-full bg-gradient-to-tr from-white/30 to-transparent blur-xl" />

                  <div className="relative z-10">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <p className="mb-2 text-xs font-bold uppercase tracking-[0.2em] text-slate-500">{label}</p>
                        <p className="text-base font-semibold leading-relaxed text-slate-800 line-clamp-2">{content}</p>
                      </div>
                      <div className={`flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br ${config.iconBg} text-3xl shadow-lg transition-transform duration-300 group-hover:scale-110 group-hover:rotate-3`}>
                        {icon}
                      </div>
                    </div>

                    {/* 하단 장식 라인 */}
                    <div className={`mt-4 h-1 w-full rounded-full bg-gradient-to-r ${config.iconBg} opacity-30`} />
                  </div>
                </div>
              )
            })}
          </section>
        )}

        <section className="rounded-[28px] border border-slate-200 bg-white/95 p-6 shadow-[0_30px_90px_-60px_rgba(15,23,42,0.9)]">
          <div className="mb-5 flex flex-col gap-2">
            <p className="text-xs font-semibold tracking-[0.35em] text-slate-400">SCHEDULE</p>
            <h3 className="text-2xl font-semibold text-slate-900">월간 캘린더</h3>
            <p className="text-sm text-slate-500">일간 운동 / 식단 플랜을 한눈에 확인하세요.</p>
          </div>
          <Separator className="mb-5" />
          <FullCalendar
            ref={calendarRef}
            plugins={[dayGridPlugin, interactionPlugin]}
            initialView="dayGridMonth"
            locale="ko"
            height="auto"
            dateClick={handleDateClick}
            headerToolbar={{
              left: 'prev,next today',
              center: 'title',
              right: '',
            }}
            buttonText={{ today: '오늘' }}
            datesSet={handleDatesSet}
            dayCellContent={(arg) => <span className="text-slate-700">{arg.dayNumberText}</span>}
            dayCellDidMount={(arg) => {
              const dateStr = toDateKey(arg.date)
              arg.el.setAttribute('data-date', dateStr)
              const dayData = calendarPlansRef.current[dateStr] || null
              setTimeout(() => {
                renderIndicatorsForCell(arg.el, dayData)
              }, 0)
            }}
          />
        </section>

        <footer className="pb-6 pt-2 text-center text-sm text-slate-400">
          © 2025 HealthWeb — Stay healthy and motivated!
        </footer>
      </div>
    </div>
  )
}

export default App
