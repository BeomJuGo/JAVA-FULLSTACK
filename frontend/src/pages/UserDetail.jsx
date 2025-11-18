import React, { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getUserProfile } from '../api/profileApi'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

function UserDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)

  const genderLabel = {
    M: '남성',
    F: '여성',
    O: '기타',
  }

  const activityLabel = {
    LOW: '낮음',
    MID: '보통',
    HIGH: '높음',
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
    if (!profile?.profile) return []
    const detail = profile.profile
    const isPublic = profile?.isPublic ?? false
    return [
      { label: '성별', value: genderLabel[detail.gender] || '미입력' },
      { label: '나이', value: formatNumberWithSuffix(detail.age, '세') },
      { label: '키', value: formatMeasurement(detail.heightCm, 'cm') },
      { label: '몸무게', value: formatMeasurement(detail.weightKg, 'kg') },
      { label: '활동량', value: activityLabel[detail.activityLevel] || '미입력' },
      { label: '프로필 공개 여부', value: isPublic ? '공개' : '비공개' },
    ]
  }, [profile])

  useEffect(() => {
    const loadProfile = async () => {
      try {
        setLoading(true)
        const data = await getUserProfile(id)
        setProfile(data)
      } catch (err) {
        console.error('사용자 프로필 로드 실패:', err)
      } finally {
        setLoading(false)
      }
    }
    loadProfile()
  }, [id])

  const isPrivateBlocked = !profile?.visible && !profile?.isOwner

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
        <Card className="w-full max-w-md border border-white/80 bg-white px-6 py-6 shadow-lg">
          <CardHeader>
            <CardTitle className="text-slate-900">사용자 정보를 불러오는 중</CardTitle>
            <CardDescription className="text-slate-500">잠시만 기다려 주세요.</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="py-6 text-center text-slate-500">로딩 중...</div>
          </CardContent>
        </Card>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50">
        <Card className="w-full max-w-md border border-white/80 bg-white px-6 py-6 shadow-lg">
          <CardHeader>
            <CardTitle className="text-slate-900">사용자 정보를 찾을 수 없습니다</CardTitle>
            <CardDescription className="text-slate-500">요청하신 사용자가 존재하지 않습니다.</CardDescription>
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
        <div className="absolute right-[12%] top-[18%] h-64 w-64 rounded-full bg-cyan-200/35 blur-[120px]" />
      </div>

      <div className="relative mx-auto flex max-w-3xl flex-col gap-6">
        <div className="flex items-center justify-between">
          <Button
            variant="outline"
            className="w-fit rounded-2xl border-slate-200 bg-white text-slate-700 shadow hover:bg-slate-50"
            onClick={() => navigate(-1)}
          >
            ← 뒤로가기
          </Button>
        </div>

        <Card className="border border-slate-200 bg-white/95 shadow-[0_20px_60px_-45px_rgba(15,23,42,0.35)]">
          <CardHeader className="flex flex-col gap-6 md:flex-row md:items-center">
            <div className="relative">
              {profile.imageUrl ? (
                <img
                  src={profile.imageUrl}
                  alt="프로필"
                  className="h-24 w-24 rounded-full object-cover ring-2 ring-indigo-200"
                />
              ) : (
                <div className="flex h-24 w-24 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-indigo-500 text-4xl font-bold text-white">
                  {(profile.displayName || profile.username || 'U').charAt(0).toUpperCase()}
                </div>
              )}
            </div>
            <div>
              <CardTitle className="text-3xl font-bold text-slate-900">
                {profile.displayName || profile.username || `사용자 #${profile.id}`}
              </CardTitle>
              <CardDescription className="mt-1 text-base text-slate-500">
                @{profile.username || profile.id}
              </CardDescription>
              <p className="mt-2 text-sm text-slate-500">
                프로필 공개 상태: <span className="font-semibold text-slate-900">{profile.isPublic ? '공개' : '비공개'}</span>
              </p>
            </div>
          </CardHeader>
        </Card>

        {isPrivateBlocked ? (
          <Card className="border border-slate-200 bg-white/95">
            <CardContent className="py-12 text-center text-sm text-slate-500">
              이 사용자는 프로필을 비공개로 설정했습니다.
            </CardContent>
          </Card>
        ) : (
          <>
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
                <CardDescription className="text-slate-500">
                  사용자가 작성한 자기소개를 확인해 보세요.
                </CardDescription>
              </CardHeader>
              <CardContent>
                <p className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
                  {profile.profile?.intro && profile.profile.intro.trim()
                    ? profile.profile.intro
                    : '등록된 자기소개가 없습니다.'}
                </p>
              </CardContent>
            </Card>
          </>
        )}
      </div>
    </div>
  )
}

export default UserDetail
