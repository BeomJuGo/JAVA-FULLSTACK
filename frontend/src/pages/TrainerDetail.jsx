import React, { useState, useEffect, useMemo } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getTrainerProfile } from '../api/profileApi'
import { getReviewsByTrainer, createReview } from '../api/reviewApi'
import { useAuth } from '../context/AuthContext'
import { getMyMatches } from '../api/matchApi'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'

function TrainerDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [trainer, setTrainer] = useState(null)
  const [reviews, setReviews] = useState([])
  const [matches, setMatches] = useState([])
  const [loading, setLoading] = useState(true)
  const [showReviewForm, setShowReviewForm] = useState(false)
  const [rating, setRating] = useState(5)
  const [reviewContent, setReviewContent] = useState('')
  const [anonymous, setAnonymous] = useState(false)

  useEffect(() => {
    loadData()
  }, [id])

  const loadData = async () => {
    try {
      setLoading(true)
      const [trainerData, reviewsData, matchesData] = await Promise.all([
        getTrainerProfile(id),
        getReviewsByTrainer(id).catch(() => ({ content: [], summary: { avgRating: 0, totalReviews: 0 } })),
        getMyMatches().catch(() => []),
      ])
      setTrainer(trainerData)
      setReviews(reviewsData.content || [])
      setMatches(matchesData)
    } catch (err) {
      console.error('데이터 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleReviewSubmit = async (e) => {
    e.preventDefault()
    if (!reviewContent.trim()) {
      alert('리뷰 내용을 입력해주세요.')
      return
    }

    // 매칭이 완료된 경우에만 리뷰 작성 가능
    const completedMatch = matches.find(m => 
      m.status === 'ENDED' && m.trainerId === parseInt(id)
    )

    if (!completedMatch) {
      alert('이 트레이너와 완료된 매칭이 있어야 리뷰를 작성할 수 있습니다.')
      return
    }

    try {
      // 백엔드에서 accountId를 userId로 변환하므로 matchId와 trainerId만 전달
      await createReview(completedMatch.id, parseInt(id), rating, reviewContent, anonymous)
      setShowReviewForm(false)
      setReviewContent('')
      setRating(5)
      setAnonymous(false)
      await loadData()
    } catch (err) {
      alert(err.response?.data?.message || '리뷰 작성에 실패했습니다.')
    }
  }

  // Hook은 항상 같은 순서로 호출되어야 하므로 early return 전에 호출
  const genderLabel = {
    M: '남성',
    F: '여성',
    O: '기타',
  }

  const formatMeasurement = (value, unit) => {
    if (value === null || value === undefined || value === '') return '미입력'
    const num = Number(value)
    if (Number.isNaN(num)) return '미입력'
    const display = Number.isInteger(num) ? num.toString() : num.toFixed(1)
    return `${display}${unit}`
  }

  const formatNumberWithSuffix = (value, suffix) => {
    if (value === null || value === undefined || value === '') return '미입력'
    const num = Number(value)
    if (Number.isNaN(num)) return '미입력'
    return `${num}${suffix}`
  }

  const detailItems = useMemo(() => {
    if (!trainer?.profile) return []
    const detail = trainer.profile
    return [
      { label: '성별', value: genderLabel[detail.gender] || '미입력' },
      { label: '나이', value: formatNumberWithSuffix(detail.age, '세') },
      { label: '키', value: formatMeasurement(detail.heightCm, 'cm') },
      { label: '몸무게', value: formatMeasurement(detail.weightKg, 'kg') },
      { label: '경력', value: formatNumberWithSuffix(detail.careerYears, '년') },
      { label: '전문 분야', value: detail.specialty && detail.specialty.trim() ? detail.specialty : '미입력' },
    ]
  }, [trainer])

  const avgRating = reviews.length > 0 ? reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length : 0

  // Early return은 모든 Hook 호출 후에 수행
  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
        <Card className="w-full max-w-md border border-white/80 bg-white px-6 py-6 shadow-lg">
          <CardHeader>
            <CardTitle className="text-slate-900">트레이너 정보를 불러오는 중</CardTitle>
            <CardDescription className="text-slate-500">잠시만 기다려 주세요.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="py-6 text-center text-slate-500">로딩 중...</div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (!trainer) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
        <Card className="w-full max-w-md border border-white/80 bg-white px-6 py-6 shadow-lg">
          <CardHeader>
            <CardTitle className="text-slate-900">트레이너를 찾을 수 없습니다</CardTitle>
            <CardDescription className="text-slate-500">요청하신 트레이너 프로필이 존재하지 않습니다.</CardDescription>
          </CardHeader>
          <CardContent>
            <Button
              variant="outline"
              className="rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
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
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-12 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 left-[18%] h-72 w-72 rounded-full bg-indigo-200/45 blur-[120px]" />
        <div className="absolute right-[10%] top-[12%] h-64 w-64 rounded-full bg-cyan-200/40 blur-[120px]" />
        <div className="absolute bottom-[-150px] left-1/2 h-[420px] w-[420px] -translate-x-1/2 rounded-full bg-violet-100/80 blur-[140px]" />
      </div>

      <div className="relative mx-auto flex w-full max-w-5xl flex-col gap-8">
        <div className="flex items-center justify-between">
          <Button
            variant="outline"
            className="rounded-2xl border-slate-200 bg-white text-slate-700 shadow hover:bg-slate-50"
            onClick={() => navigate(-1)}
          >
            ← 목록으로 돌아가기
          </Button>
        </div>

        <Card className="border border-slate-200 bg-white/95 shadow-[0_20px_60px_-45px_rgba(15,23,42,0.35)]">
          <CardHeader className="flex flex-col gap-6 md:flex-row md:items-center">
            <div className="relative">
              {trainer.imageUrl ? (
                <img
                  src={trainer.imageUrl}
                  alt="트레이너 프로필"
                  className="h-24 w-24 rounded-full object-cover ring-2 ring-indigo-200"
                />
              ) : (
                <div className="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-500 text-4xl font-bold text-white">
                  {trainer.displayName ? trainer.displayName.charAt(0).toUpperCase() : trainer.username?.charAt(0)?.toUpperCase() || trainer.id}
                </div>
              )}
            </div>
            <div className="text-left">
              <CardTitle className="text-3xl text-slate-900">
                {trainer.displayName || trainer.username || `트레이너 #${trainer.id}`}
              </CardTitle>
              <CardDescription className="text-base text-slate-500">
                @{trainer.username || trainer.id}
              </CardDescription>
              <div className="mt-3 flex items-center gap-2 text-sm text-slate-500">
                <span className="text-xl text-yellow-500">⭐</span>
                <span className="text-lg font-semibold text-slate-900">{avgRating.toFixed(1)}</span>
                <span>({reviews.length}개 리뷰)</span>
              </div>
            </div>
          </CardHeader>
        </Card>

        <Card className="border border-slate-200 bg-white/95 shadow-sm">
          <CardHeader>
            <CardTitle className="text-xl font-semibold text-slate-900">프로필 상세 정보</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm text-slate-600">
            <div className="grid gap-3 md:grid-cols-2">
              {detailItems.map((item) => (
                <div key={item.label} className="flex items-center justify-between gap-4 rounded-2xl border border-slate-200 bg-white px-4 py-3">
                  <span className="text-slate-500">{item.label}</span>
                  <span className="text-right font-semibold text-slate-900">{item.value}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className="border border-slate-200 bg-white/95 shadow-sm">
          <CardHeader>
            <CardTitle className="text-xl font-semibold text-slate-900">자기소개</CardTitle>
            <CardDescription className="text-slate-500">트레이너가 작성한 소개 내용을 확인해 보세요.</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
              {trainer.profile?.intro && trainer.profile.intro.trim()
                ? trainer.profile.intro
                : '등록된 자기소개가 없습니다.'}
            </p>
          </CardContent>
        </Card>

        <Card className="border border-slate-200 bg-white/95 shadow-[0_18px_60px_-45px_rgba(15,23,42,0.35)]">
          <CardHeader className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <CardTitle className="text-slate-900">리뷰</CardTitle>
              <CardDescription className="text-slate-500">트레이너와 함께한 경험을 공유해 주세요.</CardDescription>
            </div>
            {user && user.role === 'USER' && (
              <Button className="rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow hover:from-blue-600 hover:to-indigo-600" onClick={() => setShowReviewForm((prev) => !prev)}>
                {showReviewForm ? '작성 취소' : '리뷰 작성'}
              </Button>
            )}
          </CardHeader>
          <CardContent className="space-y-6">
            {showReviewForm && (
              <form
                onSubmit={handleReviewSubmit}
                className="space-y-5 rounded-2xl border border-slate-200 bg-white/90 p-5 text-left shadow-sm"
              >
                <div className="grid gap-4">
                  <div className="grid gap-2">
                    <Label htmlFor="rating" className="text-slate-600">
                      평점
                    </Label>
                    <select
                      id="rating"
                      value={rating}
                      onChange={(e) => setRating(parseFloat(e.target.value))}
                      className="w-40 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-200"
                    >
                      <option value={5}>5점 (매우 만족)</option>
                      <option value={4}>4점</option>
                      <option value={3}>3점</option>
                      <option value={2}>2점</option>
                      <option value={1}>1점 (불만족)</option>
                    </select>
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="review-content" className="text-slate-600">
                      리뷰 내용
                    </Label>
                    <Textarea
                      id="review-content"
                      value={reviewContent}
                      onChange={(e) => setReviewContent(e.target.value)}
                      placeholder="트레이너와 함께한 경험을 자세히 남겨주세요."
                      rows={4}
                      className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
                      required
                    />
                  </div>
                  <label className="flex items-center gap-2 text-sm text-slate-600">
                    <input
                      type="checkbox"
                      checked={anonymous}
                      onChange={(e) => setAnonymous(e.target.checked)}
                      className="h-4 w-4 rounded border-slate-300 text-indigo-500 focus:ring-indigo-200"
                    />
                    익명으로 작성하기
                  </label>
                  <div className="flex justify-end gap-2">
                    <Button
                      type="button"
                      variant="ghost"
                      className="rounded-2xl border border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                      onClick={() => setShowReviewForm(false)}
                    >
                      취소
                    </Button>
                    <Button type="submit" className="rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 px-4 text-white shadow hover:from-blue-600 hover:to-indigo-600">
                      작성하기
                    </Button>
                  </div>
                </div>
              </form>
            )}

            <div className="space-y-4">
              {reviews.map((review) => (
                <div key={review.id} className="rounded-2xl border border-slate-200 bg-white/90 p-5 shadow-sm">
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <div className="flex items-center gap-2 text-sm text-slate-600">
                      <span className="text-base font-semibold text-yellow-500">⭐ {review.rating}</span>
                      <span>{review.anonymous ? '익명' : `사용자 ${review.authorUserId}`}</span>
                    </div>
                    <span className="text-xs text-slate-400">
                      {new Date(review.createdAt).toLocaleDateString('ko-KR')}
                    </span>
                  </div>
                  <p className="mt-3 whitespace-pre-wrap text-sm text-slate-700">{review.content}</p>
                </div>
              ))}
              {reviews.length === 0 && (
                <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 py-12 text-center text-sm text-slate-500">
                  아직 작성된 리뷰가 없습니다.
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

export default TrainerDetail

