import React, { useState, useEffect, useRef, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getMyProfile, updateProfileImage, updateMyProfileDetails } from '../api/profileApi'
import { getMyMatches } from '../api/matchApi'
import { getUploadSign } from '../api/uploadApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'

function MyPage() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const [profile, setProfile] = useState(null)
  const [matches, setMatches] = useState([])
  const [loading, setLoading] = useState(true)
  const [uploading, setUploading] = useState(false)
  const [editMode, setEditMode] = useState(false)
  const [saving, setSaving] = useState(false)
  const [form, setForm] = useState({
    gender: '',
    age: '',
    heightCm: '',
    weightKg: '',
    activityLevel: '',
    intro: '',
    isPublic: true,
    careerYears: '',
    specialty: '',
  })
  const fileInputRef = useRef(null)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [profileData, matchData] = await Promise.all([
        getMyProfile(),
        getMyMatches().catch(() => []),
      ])
      setProfile(profileData)
      setMatches(matchData)
      if (profileData?.profile) {
        setForm(buildFormState(profileData.profile, profileData.role))
      }
    } catch (err) {
      console.error('데이터 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const handleImageUpload = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    // 이미지 파일만 허용
    if (!file.type.startsWith('image/')) {
      alert('이미지 파일만 업로드 가능합니다.')
      return
    }

    // 파일 크기 제한 (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('파일 크기는 5MB 이하여야 합니다.')
      return
    }

    try {
      setUploading(true)

      // 1. 서명 받기
      const signData = await getUploadSign()

      // 2. FormData 생성
      const formData = new FormData()
      formData.append('file', file)
      formData.append('api_key', signData.apiKey)
      formData.append('timestamp', signData.timestamp)
      formData.append('signature', signData.signature)
      formData.append('folder', signData.folder)

      // 3. Cloudinary에 업로드
      const uploadResponse = await fetch(
        `https://api.cloudinary.com/v1_1/${signData.cloudName}/image/upload`,
        {
          method: 'POST',
          body: formData,
        }
      )

      if (!uploadResponse.ok) {
        throw new Error('이미지 업로드 실패')
      }

      const uploadResult = await uploadResponse.json()

      // 4. 서버에 프로필 이미지 업데이트 요청
      await updateProfileImage(uploadResult.secure_url, uploadResult.public_id)

      // 5. 프로필 다시 로드
      await loadData()

      alert('프로필 이미지가 업데이트되었습니다.')
    } catch (err) {
      console.error('이미지 업로드 실패:', err)
      alert('이미지 업로드에 실패했습니다.')
    } finally {
      setUploading(false)
      // 파일 input 초기화
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleImageClick = () => {
    fileInputRef.current?.click()
  }

  const buildFormState = (profileData, role) => {
    const formatValue = (value) => (value !== null && value !== undefined ? String(value) : '')
    const detectPublic = typeof profileData.public === 'boolean' ? profileData.public : profileData.isPublic
    return {
      gender: profileData.gender || '',
      age: formatValue(profileData.age),
      heightCm: formatValue(profileData.heightCm),
      weightKg: formatValue(profileData.weightKg),
      activityLevel: profileData.activityLevel || '',
      intro: profileData.intro || '',
      isPublic: detectPublic !== undefined ? detectPublic : true,
      careerYears: formatValue(profileData.careerYears),
      specialty: profileData.specialty || '',
    }
  }

  useEffect(() => {
    if (profile?.profile) {
      setForm(buildFormState(profile.profile, profile.role))
    }
  }, [profile])

  const handleFormChange = (field, value) => {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleCancelEdit = () => {
    if (profile?.profile) {
      setForm(buildFormState(profile.profile, profile.role))
    }
    setEditMode(false)
  }

  const toNullableNumber = (value) => {
    if (value === '' || value === null || value === undefined) return null
    const num = Number(value)
    return Number.isNaN(num) ? null : num
  }

  const handleProfileSubmit = async (e) => {
    e.preventDefault()
    if (!profile?.role) return
    try {
      setSaving(true)
      const payload =
        profile.role === 'USER'
          ? {
            userProfile: {
              gender: form.gender || null,
              age: toNullableNumber(form.age),
              heightCm: toNullableNumber(form.heightCm),
              weightKg: toNullableNumber(form.weightKg),
              activityLevel: form.activityLevel || null,
              intro: form.intro || null,
              isPublic: form.isPublic,
            },
          }
          : {
            trainerProfile: {
              gender: form.gender || null,
              age: toNullableNumber(form.age),
              heightCm: toNullableNumber(form.heightCm),
              weightKg: toNullableNumber(form.weightKg),
              careerYears: toNullableNumber(form.careerYears),
              specialty: form.specialty || null,
              intro: form.intro || null,
            },
          }

      await updateMyProfileDetails(payload)
      await loadData()
      setEditMode(false)
      alert('프로필 정보가 업데이트되었습니다.')
    } catch (err) {
      console.error('프로필 업데이트 실패:', err)
      alert(err.response?.data?.message || '프로필 업데이트에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

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

  const profileInfoItems = useMemo(() => {
    if (!profile?.profile) return []
    const data = profile.profile
    const genderKey = typeof data.gender === 'string' ? data.gender.toUpperCase() : data.gender
    const activityKey = typeof data.activityLevel === 'string' ? data.activityLevel.toUpperCase() : data.activityLevel
    const items = [
      { label: '성별', value: genderLabel[genderKey] || '미입력' },
      { label: '나이', value: formatNumberWithSuffix(data.age, '세') },
      { label: '키', value: formatMeasurement(data.heightCm, 'cm') },
      { label: '몸무게', value: formatMeasurement(data.weightKg, 'kg') },
    ]

    if (profile.role === 'USER') {
      items.push({
        label: '활동량',
        value: activityLabel[activityKey] || '미입력',
      })
      const isPublicValue = data.public ?? data.isPublic ?? true
      items.push({
        label: '프로필 공개 여부',
        value: isPublicValue ? '공개' : '비공개',
      })
    } else if (profile.role === 'TRAINER') {
      items.push({
        label: '경력',
        value: formatNumberWithSuffix(data.careerYears, '년'),
      })
      items.push({
        label: '전문 분야',
        value: data.specialty && data.specialty.trim() ? data.specialty : '미입력',
      })
    }

    return items
  }, [profile])

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-600">
        <div className="rounded-3xl border border-white/80 bg-white px-6 py-4 shadow-lg">로딩 중...</div>
      </div>
    )
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-12 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 left-[15%] h-72 w-72 rounded-full bg-indigo-200/40 blur-[120px]" />
        <div className="absolute right-[12%] top-[18%] h-64 w-64 rounded-full bg-emerald-200/35 blur-[120px]" />
      </div>

      <div className="relative mx-auto w-full max-w-4xl space-y-6">
        <div className="flex flex-wrap items-center justify-between gap-3 rounded-[28px] border border-white/80 bg-white/95 px-6 py-5 shadow-[0_18px_60px_-40px_rgba(15,23,42,0.45)]">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.35em] text-indigo-400">My Page</p>
            <h1 className="mt-2 text-3xl font-bold text-slate-900">마이페이지</h1>
          </div>
          <div className="flex flex-1 flex-wrap items-center justify-end gap-3">
            <Button
              className="rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 shadow-md hover:from-blue-600 hover:to-indigo-600"
              onClick={() => navigate('/mypage/edit-display-name')}
            >
              표시 이름 설정
            </Button>
            <Button
              variant="outline"
              className="rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
              onClick={() => navigate(-1)}
            >
              뒤로가기
            </Button>
            <Button variant="destructive" className="rounded-2xl" onClick={handleLogout}>
              로그아웃
            </Button>
          </div>
        </div>

        <div className="rounded-[26px] border border-slate-200 bg-white/95 p-6 shadow-[0_20px_60px_-45px_rgba(15,23,42,0.45)]">
          {/* 프로필 이미지 섹션 */}
          <div className="mb-6 flex flex-col items-center gap-4">
            <div className="relative">
              {profile?.profile?.imageUrl ? (
                <img
                  src={profile.profile.imageUrl}
                  alt="프로필 이미지"
                  className="h-32 w-32 cursor-pointer rounded-full border-4 border-slate-200 object-cover shadow-lg transition-all hover:scale-105 hover:border-indigo-400"
                  onClick={handleImageClick}
                />
              ) : (
                <div
                  className="flex h-32 w-32 cursor-pointer items-center justify-center rounded-full border-4 border-slate-200 bg-gradient-to-br from-indigo-400 to-purple-500 text-4xl font-bold text-white shadow-lg transition-all hover:scale-105 hover:border-indigo-400"
                  onClick={handleImageClick}
                >
                  {profile?.username?.charAt(0).toUpperCase() || 'U'}
                </div>
              )}
              {uploading && (
                <div className="absolute inset-0 flex items-center justify-center rounded-full bg-black/50 text-white">
                  <span className="text-sm">업로드 중...</span>
                </div>
              )}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleImageUpload}
                className="hidden"
              />
            </div>
            <Button
              variant="outline"
              size="sm"
              className="rounded-xl border-slate-200 bg-white text-xs text-slate-700 hover:bg-slate-50"
              onClick={handleImageClick}
              disabled={uploading}
            >
              {uploading ? '업로드 중...' : '프로필 사진 변경'}
            </Button>
          </div>

          <div className="flex items-center justify-between gap-3">
            <h3 className="text-xl font-semibold text-slate-900">프로필 상세 정보</h3>
            {profile?.profile && (
              <Button
                variant="outline"
                className="rounded-2xl border-slate-200 bg-white text-xs text-slate-700 hover:bg-slate-50"
                onClick={() => {
                  if (editMode) {
                    handleCancelEdit()
                  } else {
                    setEditMode(true)
                  }
                }}
              >
                {editMode ? '입력 취소' : '정보 수정'}
              </Button>
            )}
          </div>
          {profile?.profile ? (
            editMode ? (
              <form onSubmit={handleProfileSubmit} className="mt-4 space-y-6 text-sm text-slate-600">
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <span className="text-slate-500">성별</span>
                    <select
                      value={form.gender}
                      onChange={(e) => handleFormChange('gender', e.target.value)}
                      className="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-3 text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                    >
                      <option value="" className="bg-slate-900">
                        선택 없음
                      </option>
                      <option value="M" className="bg-slate-900">
                        남성
                      </option>
                      <option value="F" className="bg-slate-900">
                        여성
                      </option>
                      <option value="O" className="bg-slate-900">
                        기타
                      </option>
                    </select>
                  </div>

                  <div className="space-y-2">
                    <span className="text-slate-500">나이</span>
                    <Input
                      type="number"
                      min="0"
                      value={form.age}
                      onChange={(e) => handleFormChange('age', e.target.value)}
                      placeholder="나이 (숫자)"
                      className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
                    />
                  </div>

                  <div className="space-y-2">
                    <span className="text-slate-500">키 (cm)</span>
                    <Input
                      type="number"
                      min="0"
                      step="0.1"
                      value={form.heightCm}
                      onChange={(e) => handleFormChange('heightCm', e.target.value)}
                      placeholder="예: 170.5"
                      className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
                    />
                  </div>

                  <div className="space-y-2">
                    <span className="text-slate-500">몸무게 (kg)</span>
                    <Input
                      type="number"
                      min="0"
                      step="0.1"
                      value={form.weightKg}
                      onChange={(e) => handleFormChange('weightKg', e.target.value)}
                      placeholder="예: 65.3"
                      className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
                    />
                  </div>

                  {profile.role === 'USER' && (
                    <div className="space-y-2">
                      <span className="text-slate-500">활동량</span>
                      <select
                        value={form.activityLevel}
                        onChange={(e) => handleFormChange('activityLevel', e.target.value)}
                        className="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 px-3 text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-200"
                      >
                        <option value="" className="bg-slate-900">
                          선택 없음
                        </option>
                        <option value="LOW" className="bg-slate-900">
                          낮음
                        </option>
                        <option value="MID" className="bg-slate-900">
                          보통
                        </option>
                        <option value="HIGH" className="bg-slate-900">
                          높음
                        </option>
                      </select>
                    </div>
                  )}

                  {profile.role === 'TRAINER' && (
                    <>
                      <div className="space-y-2">
                        <span className="text-slate-500">경력 (년)</span>
                        <Input
                          type="number"
                          min="0"
                          value={form.careerYears}
                          onChange={(e) => handleFormChange('careerYears', e.target.value)}
                          placeholder="예: 5"
                          className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
                        />
                      </div>
                      <div className="space-y-2 md:col-span-2">
                        <span className="text-slate-500">전문 분야</span>
                        <Input
                          value={form.specialty}
                          onChange={(e) => handleFormChange('specialty', e.target.value)}
                          placeholder="예: 다이어트, 재활, 기능성 트레이닝 등"
                          className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
                        />
                      </div>
                    </>
                  )}
                </div>

                <div className="space-y-2">
                  <span className="text-slate-500">자기소개</span>
                  <Textarea
                    value={form.intro}
                    onChange={(e) => handleFormChange('intro', e.target.value)}
                    rows={4}
                    placeholder="간단한 자기소개를 입력하세요."
                    className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-200"
                  />
                </div>

                {profile.role === 'USER' && (
                  <label className="flex items-center gap-3 text-xs text-slate-500">
                    <input
                      type="checkbox"
                      checked={form.isPublic}
                      onChange={(e) => handleFormChange('isPublic', e.target.checked)}
                      className="h-4 w-4 rounded border-slate-300 text-indigo-500 focus:ring-indigo-300"
                    />
                    프로필을 다른 사용자에게 공개합니다.
                  </label>
                )}

                <div className="flex justify-end gap-3">
                  <Button
                    type="button"
                    variant="outline"
                    className="rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                    onClick={handleCancelEdit}
                    disabled={saving}
                  >
                    취소
                  </Button>
                  <Button type="submit" className="rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 px-4 py-2 text-white shadow-md hover:from-blue-600 hover:to-indigo-600" disabled={saving}>
                    {saving ? '저장 중...' : '저장'}
                  </Button>
                </div>
              </form>
            ) : (
              <>
                <div className="mt-4 space-y-3 text-sm text-slate-600">
                  {profileInfoItems.map((item) => (
                    <div key={item.label} className="flex items-center justify-between gap-4">
                      <span className="text-slate-500">{item.label}</span>
                      <span className="text-right font-semibold text-slate-900">{item.value}</span>
                    </div>
                  ))}
                </div>
                <div className="mt-5">
                  <h4 className="text-sm font-semibold text-slate-600">자기소개</h4>
                  <p className="mt-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-700">
                    {profile.profile.intro && profile.profile.intro.trim()
                      ? profile.profile.intro
                      : '등록된 자기소개가 없습니다.'}
                  </p>
                </div>
              </>
            )
          ) : (
            <p className="mt-4 text-sm text-slate-200/70">회원가입 시 입력한 프로필 정보가 없습니다.</p>
          )}
        </div>

        <div className="rounded-[26px] border border-slate-200 bg-white/95 p-6 shadow-sm">
          <h3 className="text-xl font-semibold text-slate-900">내 매칭</h3>
          <div className="mt-4 space-y-3">
            {matches.length > 0 ? (
              matches.map((match) => {
                const partnerLabel = match.isAiTrainer
                  ? '🤖 AI 트레이너'
                  : (match.partnerName || match.partnerUsername || `트레이너 #${match.trainerId}`);
                const statusLabel = match.status === 'IN_PROGRESS' ? ' (진행중)'
                  : match.status === 'ACCEPTED' ? ' (승인됨)'
                    : match.status === 'REQUESTED' ? ' (요청됨)'
                      : match.status === 'ENDED' ? ' (종료됨)'
                        : '';
                return (
                  <div
                    key={match.id}
                    className="flex cursor-pointer items-center justify-between rounded-2xl border border-slate-200 bg-white/90 p-4 transition hover:-translate-y-1 hover:shadow-md"
                    onClick={() => navigate(`/matches`)}
                  >
                    <div>
                      <p className="font-semibold text-slate-900">
                        {partnerLabel} - 매칭 #{match.id}
                      </p>
                      <p className="text-xs text-slate-500">
                        상태: {match.status}{statusLabel}
                      </p>
                    </div>
                    <Button
                      onClick={(e) => {
                        e.stopPropagation()
                        navigate(`/chat/${match.id}`)
                      }}
                      className="rounded-2xl border border-indigo-200 bg-indigo-50 px-4 py-2 text-indigo-600 hover:bg-indigo-100"
                    >
                      채팅
                    </Button>
                  </div>
                );
              })
            ) : (
              <p className="text-center text-sm text-slate-500">매칭이 없습니다.</p>
            )}
          </div>
        </div>

        <div className="rounded-[26px] border border-slate-200 bg-white/95 p-6 shadow-sm">
          <h3 className="text-xl font-semibold text-slate-900">계정 정보</h3>
          <div className="mt-4 space-y-3 text-sm text-slate-600">
            <div className="flex items-center justify-between">
              <span className="text-slate-500">아이디</span>
              <span className="font-semibold text-slate-900">{profile?.username}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-500">표시 이름</span>
              <span className="font-semibold text-slate-900">{profile?.displayName || '설정되지 않음'}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-slate-500">역할</span>
              <span className="font-semibold text-slate-900">
                {profile?.role === 'USER' ? '일반 사용자' : profile?.role === 'TRAINER' ? '트레이너' : '관리자'}
              </span>
            </div>
            {profile?.profileId && (
              <div className="flex items-center justify-between">
                <span className="text-slate-500">프로필 ID</span>
                <span className="font-semibold text-slate-900">{profile.profileId}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default MyPage

