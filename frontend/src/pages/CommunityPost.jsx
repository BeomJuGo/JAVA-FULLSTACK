import React, { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getPost, toggleLike, addComment, getComments } from '../api/communityApi'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'

function CommunityPost() {
  const { postId } = useParams()
  const navigate = useNavigate()
  const { user } = useAuth()
  const [post, setPost] = useState(null)
  const [comments, setComments] = useState([])
  const [loading, setLoading] = useState(true)
  const [commentText, setCommentText] = useState('')
  const [liked, setLiked] = useState(false)

  const getProfileLink = (role, profileId) => {
    if (!profileId) return null
    if (role === 'USER') return `/profiles/users/${profileId}`
    if (role === 'TRAINER') return `/profiles/trainers/${profileId}`
    return null
  }

  const renderAuthorButton = (displayName, username, fallback, role, profileId, options = {}) => {
    const label = displayName || username || fallback
    const route = getProfileLink(role, profileId)
    if (!route) {
      return <span className="font-semibold text-slate-100">{label}</span>
    }
    return (
      <button
        type="button"
        onClick={(e) => {
          if (options.stopPropagation) {
            e.stopPropagation()
          }
          navigate(route)
        }}
        className="font-semibold text-indigo-200 underline-offset-2 hover:text-white hover:underline"
      >
        {label}
      </button>
    )
  }

  useEffect(() => {
    loadPost()
    loadComments()
  }, [postId])

  const loadPost = async () => {
    try {
      setLoading(true)
      const data = await getPost(postId)
      setPost(data)
    } catch (err) {
      console.error('게시글 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const loadComments = async () => {
    try {
      const data = await getComments(postId)
      setComments(data.content || [])
    } catch (err) {
      console.error('댓글 로드 실패:', err)
    }
  }

  const handleLike = async () => {
    try {
      await toggleLike(postId)
      await loadPost()
    } catch (err) {
      alert(err.response?.data?.message || '좋아요에 실패했습니다.')
    }
  }

  const handleCommentSubmit = async (e) => {
    e.preventDefault()
    if (!commentText.trim()) return

    try {
      await addComment(postId, commentText)
      setCommentText('')
      await loadComments()
    } catch (err) {
      alert(err.response?.data?.message || '댓글 작성에 실패했습니다.')
    }
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-600">
        <div className="rounded-3xl border border-white/80 bg-white px-6 py-4 shadow-lg">로딩 중...</div>
      </div>
    )
  }

  if (!post) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-50 via-white to-blue-50 text-slate-600">
        <div className="rounded-3xl border border-white/80 bg-white px-6 py-4 shadow-lg">게시글을 찾을 수 없습니다.</div>
      </div>
    )
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-10 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-32 left-[18%] h-72 w-72 rounded-full bg-indigo-200/45 blur-[120px]" />
        <div className="absolute right-[10%] top-[18%] h-64 w-64 rounded-full bg-rose-200/40 blur-[115px]" />
        <div className="absolute bottom-[-150px] left-1/2 h-[380px] w-[380px] -translate-x-1/2 rounded-full bg-purple-100/65 blur-[130px]" />
      </div>

      <div className="relative mx-auto flex w-full max-w-4xl flex-col gap-8">
        <Button
          variant="outline"
          onClick={() => navigate(-1)}
          className="w-fit rounded-2xl border-slate-200 bg-white/90 px-6 py-3 text-slate-700 shadow-sm hover:bg-slate-50"
        >
          ← 뒤로가기
        </Button>

        <div className="rounded-[28px] border border-slate-200 bg-white/95 p-8 shadow-[0_25px_80px_-50px_rgba(15,23,42,0.6)]">
          <div className="flex flex-col gap-6">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
              <div>
                <p className="text-xs font-semibold uppercase tracking-[0.35em] text-indigo-400">Community Post</p>
                <h1 className="mt-2 text-3xl font-bold text-slate-900">{post.title}</h1>
                <div className="mt-3 flex flex-wrap items-center gap-4 text-xs text-slate-400">
                  <span className="flex items-center gap-1">
                    <span>작성자:</span>
                    {renderAuthorButton(
                      post.authorDisplayName,
                      post.authorUsername,
                      `사용자 ${post.authorAcc}`,
                      post.authorRole,
                      post.authorProfileId
                    )}
                    {post.authorUsername && (
                      <span className="text-slate-300">@{post.authorUsername}</span>
                    )}
                  </span>
                  <span>조회수: {post.viewCount}</span>
                  <span>{formatDate(post.createdAt)}</span>
                </div>
              </div>
              <Button
                type="button"
                variant="outline"
                onClick={handleLike}
                className="inline-flex items-center gap-2 rounded-2xl border-slate-200 bg-white px-4 py-2 text-slate-700 hover:border-rose-200 hover:text-rose-500"
                aria-label="좋아요"
              >
                <span className={`text-2xl ${post.likeCount > 0 ? 'text-rose-500' : 'text-slate-300'}`}>
                  ❤️
                </span>
                <span className="text-sm font-medium text-slate-600">{post.likeCount}</span>
              </Button>
            </div>

            {post.hashtags && post.hashtags.length > 0 && (
              <div className="flex flex-wrap gap-2">
                {post.hashtags.map((tag, idx) => (
                  <span
                    key={idx}
                    className="rounded-full border border-indigo-100 bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-600"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            )}

            <div className="whitespace-pre-wrap text-base leading-relaxed text-slate-700">{post.content}</div>

            {post.media && post.media.length > 0 && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                {post.media.map((media) => (
                  <img
                    key={media.id}
                    src={media.url}
                    alt="게시글 이미지"
                    className="w-full rounded-2xl border border-slate-200 object-cover"
                  />
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="rounded-[28px] border border-slate-200 bg-white/95 p-6 shadow-[0_25px_80px_-50px_rgba(15,23,42,0.6)]">
          <h2 className="text-xl font-semibold text-slate-900">댓글 ({comments.length})</h2>

          <div className="mt-6 space-y-4">
            {comments.map((comment) => (
              <div key={comment.id} className="border-b border-slate-100 pb-4">
                <div className="flex items-center justify-between text-xs text-slate-400">
                  <div className="flex flex-col gap-1 text-left">
                    {renderAuthorButton(
                      comment.authorDisplayName,
                      comment.authorUsername,
                      `사용자 ${comment.authorAcc}`,
                      comment.authorRole,
                      comment.authorProfileId
                    )}
                    {comment.authorUsername && (
                      <span className="text-[11px] text-slate-300">@{comment.authorUsername}</span>
                    )}
                  </div>
                  <span>{formatDate(comment.createdAt)}</span>
                </div>
                <p className="mt-2 whitespace-pre-wrap text-sm text-slate-700">{comment.content}</p>
              </div>
            ))}
            {comments.length === 0 && (
              <p className="rounded-2xl border border-dashed border-slate-200 bg-slate-50 py-6 text-center text-sm text-slate-500">
                댓글이 없습니다.
              </p>
            )}
          </div>

          {user && (
            <form onSubmit={handleCommentSubmit} className="mt-6 space-y-4 border-t border-slate-100 pt-6">
              <div className="space-y-2 text-left">
                <Label htmlFor="comment-text" className="text-sm font-semibold text-slate-700">
                  댓글 작성
                </Label>
                <Textarea
                  id="comment-text"
                  value={commentText}
                  onChange={(e) => setCommentText(e.target.value)}
                  placeholder="댓글을 입력하세요..."
                  rows={3}
                  className="rounded-2xl border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-300"
                />
              </div>
              <div className="flex justify-end">
                <Button
                  type="submit"
                  className="rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 px-6 py-2 text-white shadow-lg hover:from-blue-600 hover:to-indigo-600"
                  disabled={!commentText.trim()}
                >
                  댓글 작성
                </Button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}

export default CommunityPost

