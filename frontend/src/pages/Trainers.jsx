import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAllTrainers } from '../api/profileApi'
import { Card, CardHeader, CardContent, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

function Trainers() {
  const navigate = useNavigate()
  const [trainers, setTrainers] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadTrainers()
  }, [])

  const loadTrainers = async () => {
    try {
      setLoading(true)
      const data = await getAllTrainers()
      setTrainers(data)
    } catch (err) {
      console.error('트레이너 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
        <Card className="w-full max-w-md border border-white/80 bg-white px-6 py-6 shadow-lg">
          <CardHeader>
            <CardTitle className="text-slate-900">트레이너 목록을 불러오는 중</CardTitle>
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
        <div className="absolute -top-32 left-[12%] h-72 w-72 rounded-full bg-indigo-200/40 blur-[120px]" />
        <div className="absolute right-[10%] top-[18%] h-64 w-64 rounded-full bg-sky-200/35 blur-[120px]" />
      </div>

      <div className="relative mx-auto flex max-w-6xl flex-col gap-6">
        <div className="flex flex-wrap items-center justify-between rounded-[28px] border border-white/80 bg-white/95 px-6 py-5 shadow-[0_18px_60px_-40px_rgba(15,23,42,0.45)]">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.35em] text-indigo-400">Trainers</p>
            <h1 className="mt-2 text-3xl font-bold text-slate-900">트레이너 목록</h1>
            <p className="text-sm text-slate-500">트레이너 프로필을 둘러보고 원하는 트레이너와 매칭해 보세요.</p>
          </div>
          <Button
            variant="outline"
            className="rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
            onClick={() => navigate(-1)}
          >
            뒤로가기
          </Button>
        </div>

        <div className="grid gap-6 sm:grid-cols-2 xl:grid-cols-3">
          {trainers.map((trainer) => (
            <Card
              key={trainer.id}
              className="cursor-pointer border border-slate-200 bg-white/95 text-slate-800 transition shadow-[0_20px_60px_-45px_rgba(15,23,42,0.35)] hover:-translate-y-1 hover:shadow-xl"
              onClick={() => navigate(`/profiles/trainers/${trainer.id}`)}
            >
              <CardHeader className="flex flex-row items-center gap-4">
                <div className="flex h-16 w-16 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-500 text-2xl font-bold text-white">
                  {trainer.displayName ? trainer.displayName.charAt(0).toUpperCase() : trainer.username?.charAt(0)?.toUpperCase() || trainer.id}
                </div>
                <div className="text-left">
                  <CardTitle className="text-xl text-slate-900">
                    {trainer.displayName || trainer.username || `트레이너 #${trainer.id}`}
                  </CardTitle>
                  <CardDescription className="text-slate-500">@{trainer.username || trainer.id}</CardDescription>
                </div>
              </CardHeader>
              <CardContent>
                <Button
                  className="w-full rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow hover:from-blue-600 hover:to-indigo-600"
                  onClick={(e) => {
                    e.stopPropagation()
                    navigate(`/profiles/trainers/${trainer.id}`)
                  }}
                >
                  상세보기
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>

        {trainers.length === 0 && (
          <Card className="border border-slate-200 bg-white/95">
            <CardContent>
              <div className="py-8 text-center text-sm text-slate-500">등록된 트레이너가 없습니다.</div>
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}

export default Trainers

