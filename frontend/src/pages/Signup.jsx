import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { signup, login } from '../api/authApi'
import { useAuth } from '../context/AuthContext'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import FormLayout from '@/components/layout/FormLayout'

function Signup() {
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [role, setRole] = useState('USER')
  const [gender, setGender] = useState('')
  const [heightCm, setHeightCm] = useState('')
  const [weightKg, setWeightKg] = useState('')
  const [age, setAge] = useState('')
  const [activityLevel, setActivityLevel] = useState('')
  const [intro, setIntro] = useState('')
  const [isPublic, setIsPublic] = useState(true)
  const [careerYears, setCareerYears] = useState('')
  const [specialty, setSpecialty] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { login: setAuth } = useAuth()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const payload = {
        username: userId,
        password,
        role,
        displayName: displayName || undefined,
      }

      if (role === 'USER') {
        payload.userProfile = {
          gender: gender || null,
          heightCm: heightCm ? Number(heightCm) : null,
          weightKg: weightKg ? Number(weightKg) : null,
          age: age ? Number(age) : null,
          activityLevel: activityLevel || null,
          intro: intro || null,
          isPublic,
        }
      } else if (role === 'TRAINER') {
        payload.trainerProfile = {
          gender: gender || null,
          heightCm: heightCm ? Number(heightCm) : null,
          weightKg: weightKg ? Number(weightKg) : null,
          age: age ? Number(age) : null,
          careerYears: careerYears ? Number(careerYears) : null,
          specialty: specialty || null,
          intro: intro || null,
        }
      }

      await signup(payload)
      // 회원가입 성공 시 자동 로그인
      const data = await login(userId, password)
      setAuth({ username: data.username, role: data.role, displayName: data.displayName })
      navigate('/')
    } catch (err) {
      setError(err.response?.data?.message || '회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <FormLayout
      eyebrow="CREATE ACCOUNT"
      title="나만의 건강 여정을 지금 시작하세요"
      description="간단한 회원가입 후 개인화된 헬스케어 서비스, 트레이너 매칭, 커뮤니티 활동까지 한 번에 이용할 수 있습니다."
      side={
        <div className="space-y-6 text-sm text-slate-600">
          <div className="rounded-2xl border border-slate-200 bg-white/80 p-5 shadow-lg">
            <p className="text-xs uppercase tracking-[0.35em] text-indigo-600">Benefits</p>
            <ul className="mt-3 space-y-3">
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-emerald-500" />
                트레이너와의 실시간 소통 및 인증 시스템 제공
              </li>
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-sky-500" />
                맞춤형 주간 플랜과 목표 관리 기능
              </li>
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-rose-500" />
                커뮤니티에서 노하우와 동기부여 콘텐츠 공유
              </li>
            </ul>
          </div>
        </div>
      }
      footer={
        <p className="text-center text-sm text-slate-600">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className="font-semibold text-indigo-600 hover:text-indigo-700">
            로그인
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
          <p className="text-base font-semibold text-slate-900">회원가입</p>
        </div>
      </div>

      {error && (
        <div className="mt-6 rounded-lg border border-red-300 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-8 space-y-6 text-left">
        <div className="space-y-2">
          <Label htmlFor="userId" className="text-slate-700">
            아이디
          </Label>
          <Input
            id="userId"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            placeholder="아이디를 입력하세요"
            className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="displayName" className="text-slate-700">
            표시 이름
          </Label>
          <Input
            id="displayName"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            placeholder="표시 이름을 입력하세요 (미입력 시 아이디와 동일)"
            className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="password" className="text-slate-700">
            비밀번호
          </Label>
          <Input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호를 입력하세요"
            className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
            required
          />
        </div>

        <div className="space-y-2">
          <Label className="text-slate-700">역할 선택</Label>
          <div className="flex gap-3">
            <button
              type="button"
              onClick={() => setRole('USER')}
              className={`flex-1 rounded-lg border px-4 py-2 text-sm font-semibold transition ${role === 'USER'
                  ? 'border-indigo-500 bg-indigo-500 text-white'
                  : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                }`}
            >
              일반 사용자
            </button>
            <button
              type="button"
              onClick={() => setRole('TRAINER')}
              className={`flex-1 rounded-lg border px-4 py-2 text-sm font-semibold transition ${role === 'TRAINER'
                  ? 'border-indigo-500 bg-indigo-500 text-white'
                  : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50'
                }`}
            >
              트레이너
            </button>
          </div>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-slate-50/50 p-5">
          <h3 className="text-lg font-semibold text-slate-900">프로필 정보</h3>
          <p className="mt-1 text-xs text-slate-600">가입 시 입력한 정보는 마이페이지에서 확인할 수 있습니다.</p>

          <div className="mt-5 grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label className="text-slate-700">성별</Label>
              <select
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                className="h-11 w-full rounded-lg border border-slate-200 bg-white px-3 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              >
                <option value="" className="bg-white">
                  선택 없음
                </option>
                <option value="M" className="bg-white">
                  남성
                </option>
                <option value="F" className="bg-white">
                  여성
                </option>
                <option value="O" className="bg-white">
                  기타
                </option>
              </select>
            </div>

            <div className="space-y-2">
              <Label className="text-slate-700">나이</Label>
              <Input
                type="number"
                min="0"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                placeholder="나이 (숫자)"
                className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-700">키 (cm)</Label>
              <Input
                type="number"
                min="0"
                step="0.1"
                value={heightCm}
                onChange={(e) => setHeightCm(e.target.value)}
                placeholder="예: 170.5"
                className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
              />
            </div>

            <div className="space-y-2">
              <Label className="text-slate-700">몸무게 (kg)</Label>
              <Input
                type="number"
                min="0"
                step="0.1"
                value={weightKg}
                onChange={(e) => setWeightKg(e.target.value)}
                placeholder="예: 65.3"
                className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
              />
            </div>

            {role === 'USER' && (
              <div className="space-y-2">
                <Label className="text-slate-700">활동량</Label>
                <select
                  value={activityLevel}
                  onChange={(e) => setActivityLevel(e.target.value)}
                  className="h-11 w-full rounded-lg border border-slate-200 bg-white px-3 text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-400"
                >
                  <option value="" className="bg-white">
                    선택 없음
                  </option>
                  <option value="LOW" className="bg-white">
                    낮음
                  </option>
                  <option value="MID" className="bg-white">
                    보통
                  </option>
                  <option value="HIGH" className="bg-white">
                    높음
                  </option>
                </select>
              </div>
            )}

            {role === 'TRAINER' && (
              <>
                <div className="space-y-2">
                  <Label className="text-slate-700">경력 (년)</Label>
                  <Input
                    type="number"
                    min="0"
                    value={careerYears}
                    onChange={(e) => setCareerYears(e.target.value)}
                    placeholder="예: 5"
                    className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
                  />
                </div>
                <div className="space-y-2 md:col-span-2">
                  <Label className="text-slate-700">전문 분야</Label>
                  <Input
                    value={specialty}
                    onChange={(e) => setSpecialty(e.target.value)}
                    placeholder="예: 다이어트, 재활, 기능성 트레이닝 등"
                    className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
                  />
                </div>
              </>
            )}
          </div>

          <div className="mt-4 space-y-2">
            <Label className="text-slate-700">자기소개</Label>
            <Textarea
              value={intro}
              onChange={(e) => setIntro(e.target.value)}
              rows={4}
              placeholder="간단한 자기소개를 입력하세요."
              className="border-slate-200 bg-white text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-400"
            />
          </div>

          {role === 'USER' && (
            <label className="mt-4 flex items-center gap-3 text-sm text-slate-700">
              <input
                type="checkbox"
                checked={isPublic}
                onChange={(e) => setIsPublic(e.target.checked)}
                className="h-4 w-4 rounded border-slate-300 bg-white text-indigo-500 focus:ring-indigo-400"
              />
              프로필을 다른 사용자에게 공개합니다.
            </label>
          )}
        </div>

        <Button type="submit" disabled={loading} className="w-full bg-indigo-500 hover:bg-indigo-400">
          {loading ? '가입 중...' : '회원가입 완료'}
        </Button>
      </form>
    </FormLayout>
  )
}

export default Signup


