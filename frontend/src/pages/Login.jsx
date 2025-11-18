import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { login } from '../api/authApi'
import { useAuth } from '../context/AuthContext'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import FormLayout from '@/components/layout/FormLayout'

function Login() {
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { login: setAuth } = useAuth()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const data = await login(userId, password)
      setAuth({ username: data.username, role: data.role, displayName: data.displayName })
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || '로그인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <FormLayout
      eyebrow="WELCOME BACK"
      title="다시 만나서 반가워요!"
      description="맞춤형 플랜과 실시간 커뮤니티 업데이트를 확인하려면 로그인하세요. 데이터 보안과 빠른 경험을 동시에 제공합니다."
      side={
        <ul className="space-y-4 text-sm text-slate-600">
          <li className="flex items-start gap-3">
            <span className="mt-1 size-2 rounded-full bg-emerald-500" />
            개인 맞춤 주간 계획과 알림을 한곳에서 관리합니다.
          </li>
          <li className="flex items-start gap-3">
            <span className="mt-1 size-2 rounded-full bg-sky-500" />
            트레이너와의 채팅, 리뷰, 커뮤니티 활동을 실시간으로 확인하세요.
          </li>
          <li className="flex items-start gap-3">
            <span className="mt-1 size-2 rounded-full bg-rose-500" />
            인증 시스템을 통해 목표 달성 현황을 바로 공유할 수 있습니다.
          </li>
        </ul>
      }
      footer={
        <p className="text-center text-sm text-slate-600">
          계정이 없으신가요?{' '}
          <Link to="/signup" className="font-semibold text-indigo-600 hover:text-indigo-700">
            회원가입
          </Link>
        </p>
      }
    >
      <div className="flex items-center gap-3 text-sm text-slate-700">
        <div className="flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 bg-indigo-50 text-lg font-bold uppercase text-indigo-600">
          hw
        </div>
        <div>
          <p className="text-xs uppercase tracking-[0.3em] text-indigo-600">HealthWeb</p>
          <p className="text-base font-semibold text-slate-900">로그인</p>
        </div>
      </div>

      {error && (
        <div className="mt-6 rounded-lg border border-red-300 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-8 space-y-6 text-left">
        <div className="space-y-2">
          <Label htmlFor="login-id" className="text-slate-700">
            아이디
          </Label>
          <Input
            id="login-id"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="아이디를 입력하세요"
            className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="login-password" className="text-slate-700">
            비밀번호
          </Label>
          <Input
            id="login-password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호를 입력하세요"
            className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
            required
          />
        </div>

        <Button type="submit" disabled={loading} className="w-full bg-indigo-500 hover:bg-indigo-400">
          {loading ? '로그인 중...' : '로그인'}
        </Button>
      </form>
    </FormLayout>
  )
}

export default Login


