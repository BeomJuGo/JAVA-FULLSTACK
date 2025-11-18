import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAllTrainers } from '../api/profileApi'
import { getReviewsByTrainer } from '../api/reviewApi'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'

function Reviews() {
  const navigate = useNavigate()
  const [trainers, setTrainers] = useState([])
  const [selectedTrainerId, setSelectedTrainerId] = useState(null)
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadTrainers()
  }, [])

  useEffect(() => {
    if (selectedTrainerId) {
      loadReviews()
    }
  }, [selectedTrainerId])

  const loadTrainers = async () => {
    try {
      setLoading(true)
      const data = await getAllTrainers()
      setTrainers(data)
      if (data.length > 0) {
        setSelectedTrainerId(data[0].id)
      }
    } catch (err) {
      console.error('트레이너 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const loadReviews = async () => {
    try {
      const data = await getReviewsByTrainer(selectedTrainerId)
      setReviews(data.content || [])
    } catch (err) {
      console.error('리뷰 로드 실패:', err)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-600">
        <Card className="w-full max-w-md border border-white/80 bg-white px-6 py-6 shadow-lg">
          <CardHeader>
            <CardTitle className="text-slate-900">리뷰 데이터를 불러오는 중</CardTitle>
            <CardDescription className="text-slate-500">잠시만 기다려 주세요.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="py-6 text-center text-slate-500">로딩 중...</div>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-12 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 left-[15%] h-72 w-72 rounded-full bg-indigo-200/45 blur-[120px]" />
        <div className="absolute right-[10%] top-[18%] h-64 w-64 rounded-full bg-emerald-200/35 blur-[120px]" />
      </div>

      <div className="relative mx-auto flex max-w-5xl flex-col gap-6">
        <div className="flex flex-wrap items-center justify-between rounded-[28px] border border-white/80 bg-white/95 px-6 py-5 shadow-[0_18px_60px_-40px_rgba(15,23,42,0.45)]">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.35em] text-indigo-400">Reviews</p>
            <h1 className="mt-2 text-3xl font-bold text-slate-900">리뷰</h1>
            <p className="text-sm text-slate-500">트레이너별 사용자 리뷰를 확인할 수 있습니다.</p>
          </div>
          <Button
            variant="outline"
            className="rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
            onClick={() => navigate(-1)}
          >
            뒤로가기
          </Button>
        </div>

        <Card className="border border-slate-200 bg-white/95">
          <CardHeader>
            <CardTitle className="text-lg text-slate-900">트레이너 선택</CardTitle>
            <CardDescription className="text-slate-500">리뷰를 확인할 트레이너를 선택하세요.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2 text-left">
              <Label htmlFor="trainer-select" className="text-slate-600">
                트레이너
              </Label>
              <select
                id="trainer-select"
                value={selectedTrainerId || ''}
                onChange={(e) => setSelectedTrainerId(parseInt(e.target.value, 10))}
                className="w-full rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-200"
              >
                {trainers.map((trainer) => (
                  <option key={trainer.id} value={trainer.id}>
                    트레이너 {trainer.displayName || trainer.username || `#${trainer.id}`}
                  </option>
                ))}
              </select>
            </div>
          </CardContent>
        </Card>

        {selectedTrainerId && (
          <Card className="border border-slate-200 bg-white/95 shadow-[0_18px_60px_-45px_rgba(15,23,42,0.45)]">
            <CardHeader>
              <CardTitle className="text-slate-900">리뷰 목록</CardTitle>
              <CardDescription className="text-slate-500">사용자 경험을 통해 트레이너의 강점을 확인하세요.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
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
                <div className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 py-10 text-center text-sm text-slate-500">
                  아직 작성된 리뷰가 없습니다.
                </div>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}

export default Reviews

