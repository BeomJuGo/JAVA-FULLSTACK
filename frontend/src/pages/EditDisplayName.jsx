import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getMyProfile, updateDisplayName } from '../api/profileApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '../context/AuthContext'
import FormLayout from '@/components/layout/FormLayout'

function EditDisplayName() {
  const navigate = useNavigate()
  const { user, login: setAuth } = useAuth()
  const [displayName, setDisplayName] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    const loadProfile = async () => {
      try {
        setLoading(true)
        const data = await getMyProfile()
        setDisplayName(data.displayName || '')
      } catch (err) {
        console.error('프로필 로드 실패:', err)
        setError('프로필을 불러오지 못했습니다.')
      } finally {
        setLoading(false)
      }
    }
    loadProfile()
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      setSaving(true)
      await updateDisplayName(displayName)
      const stored = localStorage.getItem('user')
      if (stored) {
        const parsed = JSON.parse(stored)
        localStorage.setItem('user', JSON.stringify({ ...parsed, displayName }))
      }
      if (user) {
        setAuth({ ...user, displayName })
      }
      navigate('/mypage')
    } catch (err) {
      setError(err.response?.data?.message || '표시 이름 저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <FormLayout
        eyebrow="PROFILE"
        title="표시 이름을 불러오는 중"
        description="개인화된 경험을 위해 현재 프로필 데이터를 준비하고 있습니다. 잠시만 기다려 주세요."
      >
        <div className="flex min-h-[160px] items-center justify-center text-sm text-slate-200/70">
          로딩 중...
        </div>
      </FormLayout>
    )
  }

  return (
    <FormLayout
      eyebrow="PROFILE SETTINGS"
      title="나를 표현하는 이름을 설정하세요"
      description="마이페이지, 커뮤니티, 리뷰 등에서 노출되는 이름입니다. 언제든지 다시 변경할 수 있어요."
      side={
        <div className="space-y-4 text-sm text-slate-600">
          <div className="rounded-2xl border border-slate-200 bg-white/90 p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.35em] text-indigo-400">Tip</p>
            <ul className="mt-3 space-y-2 text-slate-600">
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-emerald-400" />
                커뮤니티와 리뷰에 표시될 이름을 자유롭게 설정하세요.
              </li>
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-sky-400" />
                브랜드나 닉네임을 사용하면 기억에 더 오래 남습니다.
              </li>
            </ul>
          </div>
          <Button variant="ghost" className="text-sm text-indigo-500 hover:text-indigo-700" onClick={() => navigate(-1)}>
            ← 마이페이지로 돌아가기
          </Button>
        </div>
      }
      footer={<p className="text-xs text-slate-400">길이 제한 100자 · 공백만 입력할 수 없습니다.</p>}
    >
      {error && (
        <div className="mb-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6 text-left">
        <div className="space-y-2">
          <Label htmlFor="displayName" className="text-slate-600">
            표시 이름
          </Label>
          <Input
            id="displayName"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            maxLength={100}
            placeholder="예: 헬스매니아, 스파르타 트레이너"
            className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
            required
          />
          <p className="text-xs text-slate-500">입력한 이름은 커뮤니티와 인증 피드에 표시됩니다.</p>
        </div>

        <Button type="submit" disabled={saving} className="w-full rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 text-white shadow hover:from-blue-600 hover:to-indigo-600 disabled:opacity-70">
          {saving ? '저장 중...' : '저장하기'}
        </Button>
      </form>
    </FormLayout>
  )
}

export default EditDisplayName
