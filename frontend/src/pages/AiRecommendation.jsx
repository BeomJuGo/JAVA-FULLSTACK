import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { createAiRecommendation } from '../api/aiApi'
import FormLayout from '@/components/layout/FormLayout'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

function AiRecommendation() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  
  // 오늘 날짜 기준 다음 주 월요일 계산
  const getNextMonday = () => {
    const today = new Date()
    const day = today.getDay()
    const diff = day === 0 ? 1 : 8 - day // 다음 주 월요일까지의 일수
    const nextMonday = new Date(today)
    nextMonday.setDate(today.getDate() + diff)
    return nextMonday.toISOString().split('T')[0] // yyyy-MM-dd 형식
  }

  const [formData, setFormData] = useState({
    weekStart: getNextMonday(),
    goal: '',
    specialRequests: '',
  })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    setSuccess('')

    try {
      const result = await createAiRecommendation(
        formData.weekStart,
        formData.goal || null,
        formData.specialRequests || null
      )

      setSuccess(result.message || 'AI 추천 플랜이 성공적으로 생성되었습니다!')
      
      // 2초 후 메인 페이지로 이동
      setTimeout(() => {
        navigate('/')
      }, 2000)
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'AI 추천 플랜 생성 중 오류가 발생했습니다.')
    } finally {
      setLoading(false)
    }
  }

  if (user?.role !== 'USER') {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-800">
        <Card className="border border-slate-200 bg-white/95 px-6 py-6 shadow-lg">
          <CardHeader>
            <CardTitle className="text-slate-900">접근 권한 없음</CardTitle>
            <CardDescription className="text-slate-500">
              AI 추천 기능은 일반 사용자만 이용할 수 있습니다.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button onClick={() => navigate(-1)} className="rounded-2xl border border-slate-200 bg-white text-slate-700 hover:bg-slate-50">
              돌아가기
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <FormLayout
      eyebrow="AI 추천"
      title="AI 맞춤형 플랜 생성"
      description="GPT를 활용하여 당신의 개인 정보를 바탕으로 최적화된 운동 및 식단 플랜을 자동으로 생성해드립니다."
      footer={
        <Button variant="ghost" onClick={() => navigate(-1)} className="w-fit px-0 text-indigo-500 hover:text-indigo-700">
          ← 메인으로 돌아가기
        </Button>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-6 text-left">
        {error && (
          <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
            {error}
          </div>
        )}
        {success && (
          <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
            {success}
          </div>
        )}

        <div className="space-y-2">
          <Label htmlFor="weekStart" className="text-slate-600">플랜 시작일 (주간 시작 월요일)</Label>
          <Input
            id="weekStart"
            type="date"
            value={formData.weekStart}
            onChange={(e) => setFormData({ ...formData, weekStart: e.target.value })}
            required
            className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
          />
          <p className="text-xs text-slate-500">
            주간 플랜은 월요일부터 시작됩니다. 선택한 날짜의 해당 주 월요일이 시작일이 됩니다.
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="goal" className="text-slate-600">목표 (선택사항)</Label>
          <Input
            id="goal"
            value={formData.goal}
            onChange={(e) => setFormData({ ...formData, goal: e.target.value })}
            placeholder="예: 체중 감량, 근육 증가, 체력 향상 등"
            className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
          />
          <p className="text-xs text-slate-500">
            달성하고자 하는 목표를 입력하시면 더 맞춤형 플랜을 생성할 수 있습니다.
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="specialRequests" className="text-slate-600">특별 요청사항 (선택사항)</Label>
          <Textarea
            id="specialRequests"
            value={formData.specialRequests}
            onChange={(e) => setFormData({ ...formData, specialRequests: e.target.value })}
            placeholder="예: 무릎 부상으로 인해 하체 운동 제한, 채식 위주 식단, 특정 시간대 운동 선호 등"
            rows={4}
            className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
          />
          <p className="text-xs text-slate-500">
            건강 상태, 식단 제한, 운동 선호도 등 특별한 요청사항을 입력해주세요.
          </p>
        </div>

        <Button type="submit" disabled={loading} className="w-full rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow hover:from-blue-600 hover:to-indigo-600 disabled:opacity-70">
          {loading ? 'AI 플랜 생성 중...' : 'AI 추천 플랜 생성하기'}
        </Button>

        <div className="rounded-2xl border border-indigo-200 bg-indigo-50 p-4 text-sm text-slate-600">
          <p className="mb-2 font-semibold text-indigo-500">💡 AI 추천 기능 안내</p>
          <ul className="list-disc list-inside space-y-1 text-slate-500">
            <li>마이페이지에 등록된 개인 정보(나이, 키, 체중, 활동 수준)를 기반으로 플랜이 생성됩니다.</li>
            <li>생성된 플랜은 AI 트레이너와의 매칭으로 자동 설정되며, 바로 사용할 수 있습니다.</li>
            <li>플랜 생성에는 약 10-30초 정도 소요될 수 있습니다.</li>
            <li>생성된 플랜은 메인 페이지의 캘린더에서 확인할 수 있습니다.</li>
          </ul>
        </div>
      </form>
    </FormLayout>
  )
}

export default AiRecommendation

