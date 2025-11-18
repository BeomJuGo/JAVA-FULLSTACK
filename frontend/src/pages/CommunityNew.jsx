import React, { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createPost } from '../api/communityApi'
import { getUploadSign } from '../api/uploadApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import FormLayout from '@/components/layout/FormLayout'

function CommunityNew() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [hashtags, setHashtags] = useState('')
  const [loading, setLoading] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [mediaList, setMediaList] = useState([])
  const fileInputRef = useRef(null)

  const MAX_MEDIA = 5
  const MAX_SIZE_MB = 5

  const handleOpenFileDialog = () => {
    if (uploading) {
      alert('이미지 업로드가 완료될 때까지 기다려주세요.')
      return
    }
    fileInputRef.current?.click()
  }

  const handleImageSelect = async (event) => {
    const files = Array.from(event.target.files || [])
    if (files.length === 0) return

    const availableSlots = MAX_MEDIA - mediaList.length
    if (availableSlots <= 0) {
      alert(`이미지는 최대 ${MAX_MEDIA}장까지 첨부할 수 있습니다.`)
      event.target.value = ''
      return
    }

    const selectedFiles = files.slice(0, availableSlots)

    try {
      setUploading(true)

      for (const file of selectedFiles) {
        if (!file.type.startsWith('image/')) {
          alert('이미지 파일만 업로드할 수 있습니다.')
          continue
        }

        if (file.size > MAX_SIZE_MB * 1024 * 1024) {
          alert(`"${file.name}" 파일이 너무 큽니다. ${MAX_SIZE_MB}MB 이하로 업로드해주세요.`)
          continue
        }

        const signData = await getUploadSign()

        const formData = new FormData()
        formData.append('file', file)
        formData.append('api_key', signData.apiKey)
        formData.append('timestamp', signData.timestamp)
        formData.append('signature', signData.signature)
        formData.append('folder', signData.folder)

        const uploadResponse = await fetch(
          `https://api.cloudinary.com/v1_1/${signData.cloudName}/image/upload`,
          {
            method: 'POST',
            body: formData,
          }
        )

        if (!uploadResponse.ok) {
          throw new Error('이미지 업로드에 실패했습니다.')
        }

        const result = await uploadResponse.json()

        setMediaList((prev) => [
          ...prev,
          {
            mediaType: 'IMAGE',
            publicId: result.public_id,
            url: result.secure_url,
            width: result.width ?? null,
            height: result.height ?? null,
            bytes: result.bytes ?? null,
            preview: result.secure_url,
          },
        ])
      }
    } catch (err) {
      console.error('이미지 업로드 실패:', err)
      alert(err.message || '이미지 업로드 중 오류가 발생했습니다.')
    } finally {
      setUploading(false)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleRemoveImage = (publicId) => {
    setMediaList((prev) => prev.filter((media) => media.publicId !== publicId))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!title.trim() || !content.trim()) {
      alert('제목과 내용을 입력해주세요.')
      return
    }

    if (uploading) {
      alert('이미지 업로드가 완료될 때까지 기다려주세요.')
      return
    }

    try {
      setLoading(true)
      const tagList = hashtags.split(',').map(tag => tag.trim()).filter(tag => tag)
      const payloadMedia = mediaList.map(({ preview, ...rest }) => rest)
      // authorAcc는 백엔드에서 자동으로 설정되므로 전달하지 않음
      await createPost(title, content, tagList, payloadMedia)
      navigate('/community')
    } catch (err) {
      console.error('게시글 작성 실패:', err)
      const errorMessage = err.response?.data?.message || err.message || '게시글 작성에 실패했습니다.'
      alert(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  return (
    <FormLayout
      eyebrow="COMMUNITY"
      title="새로운 이야기를 공유해 주세요"
      description="루틴, 식단, 인증 후기 등 여러분의 건강 스토리를 커뮤니티와 나눠보세요. 해시태그를 활용하면 더 많은 사람들이 글을 발견할 수 있어요."
      side={
        <div className="space-y-5 text-sm text-slate-500">
          <div className="rounded-2xl border border-slate-200 bg-white/90 p-5 shadow-sm">
            <p className="text-xs uppercase tracking-[0.35em] text-indigo-400">Guidelines</p>
            <ul className="mt-3 space-y-2 text-slate-600">
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-emerald-400" />
                경험과 노하우를 구체적으로 작성할수록 반응이 좋아요.
              </li>
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-sky-400" />
                해시태그는 쉼표로 구분해 입력하면 자동으로 분리됩니다.
              </li>
              <li className="flex items-start gap-3">
                <span className="mt-1 size-2 rounded-full bg-rose-400" />
                커뮤니티 가이드라인을 준수하며 서로를 존중해 주세요.
              </li>
            </ul>
          </div>
          <Button variant="ghost" className="text-sm text-indigo-500 hover:text-indigo-700" onClick={() => navigate(-1)}>
            목록으로 돌아가기
          </Button>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-6 text-left">
        <div className="space-y-2">
          <Label htmlFor="post-title" className="text-slate-600">
            제목
          </Label>
          <Input
            id="post-title"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="제목을 입력하세요"
            className="rounded-2xl border-slate-200 bg-slate-50 text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-300"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="post-content" className="text-slate-600">
            내용
          </Label>
          <Textarea
            id="post-content"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={10}
            placeholder="내용을 입력하세요"
            className="rounded-2xl border-slate-200 bg-slate-50 text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-300"
            required
          />
        </div>

        <div className="space-y-2">
          <Label htmlFor="post-hashtags" className="text-slate-600">
            해시태그 (쉼표로 구분)
          </Label>
          <Input
            id="post-hashtags"
            value={hashtags}
            onChange={(e) => setHashtags(e.target.value)}
            placeholder="예: 운동, 다이어트, 건강"
            className="rounded-2xl border-slate-200 bg-slate-50 text-slate-900 placeholder:text-slate-400 focus-visible:ring-indigo-300"
          />
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <Label className="text-slate-600">이미지 업로드</Label>
            <span className="text-xs text-slate-400">
              {mediaList.length}/{MAX_MEDIA}장
            </span>
          </div>

          <div className="rounded-2xl border border-dashed border-slate-200 bg-white/90 p-4">
            <div className="flex flex-wrap gap-4">
              {mediaList.map((media) => (
                <div key={media.publicId} className="relative">
                  <img
                    src={media.preview}
                    alt="업로드 미리보기"
                    className="h-32 w-32 rounded-xl border border-slate-200 object-cover shadow-inner"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemoveImage(media.publicId)}
                    className="absolute -right-2 -top-2 flex h-6 w-6 items-center justify-center rounded-full bg-rose-500 text-xs font-bold text-white shadow-lg transition hover:bg-rose-400"
                  >
                    ×
                  </button>
                </div>
              ))}

              {mediaList.length < MAX_MEDIA && (
                <button
                  type="button"
                  onClick={handleOpenFileDialog}
                  className="flex h-32 w-32 flex-col items-center justify-center gap-2 rounded-xl border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-500 transition hover:border-indigo-200 hover:bg-indigo-50"
                >
                  <span className="text-2xl text-indigo-400">＋</span>
                  <span>이미지 추가</span>
                </button>
              )}
            </div>
            <p className="mt-3 text-xs text-slate-400">
              JPEG, PNG 등 이미지 파일을 업로드할 수 있으며, 파일당 최대 {MAX_SIZE_MB}MB까지 가능합니다.
            </p>
            {uploading && (
              <p className="mt-2 text-sm text-indigo-500">이미지를 업로드하는 중입니다...</p>
            )}
          </div>

          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            multiple
            onChange={handleImageSelect}
            className="hidden"
          />
        </div>

        <Button
          type="submit"
          disabled={loading || uploading}
          className="w-full rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 py-3 text-white shadow-lg hover:from-blue-600 hover:to-indigo-600 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {loading ? '작성 중...' : uploading ? '이미지를 업로드 중입니다' : '작성하기'}
        </Button>
      </form>
    </FormLayout>
  )
}

export default CommunityNew