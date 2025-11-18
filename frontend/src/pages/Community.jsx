import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getPosts, toggleLike, registerView } from '../api/communityApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

function Community() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [posts, setPosts] = useState([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const [sort, setSort] = useState('latest')
  const [hashtagFilter, setHashtagFilter] = useState('')
  const [hashtagInput, setHashtagInput] = useState('')

  const getProfileLink = (role, profileId) => {
    if (!profileId) return null
    if (role === 'USER') return `/profiles/users/${profileId}`
    if (role === 'TRAINER') return `/profiles/trainers/${profileId}`
    return null
  }

  const renderAuthorLabel = (post, options = {}) => {
    const name = post.authorDisplayName || post.authorUsername || `사용자 ${post.authorAcc}`
    const route = getProfileLink(post.authorRole, post.authorProfileId)
    if (!route) {
      return <span className="font-semibold text-slate-100">{name}</span>
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
        {name}
      </button>
    )
  }

  useEffect(() => {
    loadPosts()
  }, [page, sort, hashtagFilter])

  const loadPosts = async () => {
    try {
      setLoading(true)
      const normalizedHashtag = hashtagFilter ? hashtagFilter.replace(/^#/, '') : null
      const data = await getPosts(page, 10, normalizedHashtag, null, sort)
      if (page === 0) {
        setPosts(data.content || [])
      } else {
        setPosts(prev => [...prev, ...(data.content || [])])
      }
      setHasMore(!data.last)
    } catch (err) {
      console.error('게시글 로드 실패:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleLike = async (postId, e) => {
    e.stopPropagation()
    try {
      await toggleLike(postId)
      await loadPosts()
    } catch (err) {
      alert(err.response?.data?.message || '좋아요에 실패했습니다.')
    }
  }

  const handlePostClick = async (postId) => {
    try {
      await registerView(postId)
      navigate(`/community/${postId}`)
    } catch (err) {
      console.error('조회수 등록 실패:', err)
      navigate(`/community/${postId}`)
    }
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-10 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-28 left-[10%] h-72 w-72 rounded-full bg-indigo-200/45 blur-[110px]" />
        <div className="absolute right-[12%] top-[15%] h-64 w-64 rounded-full bg-emerald-200/40 blur-[120px]" />
        <div className="absolute bottom-[-140px] left-1/2 h-[380px] w-[380px] -translate-x-1/2 rounded-full bg-purple-100/65 blur-[140px]" />
      </div>

      <div className="relative mx-auto flex w-full max-w-6xl flex-col gap-8">
        <div className="rounded-[28px] border border-white/80 bg-white/95 p-6 shadow-[0_25px_80px_-50px_rgba(15,23,42,0.65)] backdrop-blur">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <span className="inline-flex items-center rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-xs font-semibold uppercase tracking-[0.35em] text-indigo-500">
                Community
              </span>
              <div className="mt-4 flex items-center gap-3">
                <h1 className="text-4xl font-bold tracking-tight text-slate-900">커뮤니티</h1>
                {hashtagFilter && (
                  <div className="flex items-center gap-2 rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-sm text-indigo-600">
                    <span>#{hashtagFilter.replace(/^#/, '')}</span>
                    <button
                      type="button"
                      onClick={() => {
                        setHashtagFilter('')
                        setHashtagInput('')
                        setPage(0)
                      }}
                      className="text-indigo-400 hover:text-indigo-600"
                    >
                      ×
                    </button>
                  </div>
                )}
              </div>
              <p className="mt-2 text-sm text-slate-500">사용자들과 경험을 공유하고 새로운 정보를 받아보세요.</p>
            </div>
            <div className="flex w-full flex-col gap-3 md:w-auto md:flex-row md:items-center">
              <form
                onSubmit={(e) => {
                  e.preventDefault()
                  setHashtagFilter(hashtagInput.trim())
                  setPage(0)
                }}
                className="flex w-full gap-2 md:w-auto"
              >
                <Input
                  type="text"
                  value={hashtagInput}
                  onChange={(e) => setHashtagInput(e.target.value)}
                  placeholder="해시태그 검색 (예: diet)"
                  className="flex-1 rounded-2xl border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus-visible:ring-indigo-300 md:w-60"
                />
                <Button type="submit" className="rounded-2xl bg-gradient-to-r from-blue-500 to-indigo-500 shadow-md hover:from-blue-600 hover:to-indigo-600">
                  검색
                </Button>
              </form>
              <select
                value={sort}
                onChange={(e) => {
                  setSort(e.target.value)
                  setPage(0)
                }}
                className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm text-slate-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-200"
              >
                <option value="latest">최신순</option>
                <option value="likes">좋아요순</option>
                <option value="views">조회수순</option>
              </select>
              <div className="flex gap-2">
                <Button className="rounded-2xl bg-gradient-to-r from-sky-500 to-blue-500 shadow-md hover:from-sky-600 hover:to-blue-600" onClick={() => navigate('/community/new')}>
                  글 작성
                </Button>
                <Button
                  variant="outline"
                  className="rounded-2xl border-slate-200 bg-white text-slate-700 hover:bg-slate-50"
                  onClick={() => navigate(-1)}
                >
                  뒤로가기
                </Button>
              </div>
            </div>
          </div>
        </div>

        <div className="space-y-4">
          {posts.map((post) => (
            <div
              key={post.id}
              onClick={() => handlePostClick(post.id)}
              className="cursor-pointer rounded-[26px] border border-slate-200 bg-white/95 p-6 shadow-[0_20px_70px_-40px_rgba(15,23,42,0.55)] transition hover:-translate-y-1 hover:shadow-xl"
            >
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1">
                  <h2 className="text-xl font-semibold text-slate-900">{post.title}</h2>
                  <p className="mt-3 line-clamp-2 text-sm text-slate-500">{post.content}</p>
                  <div className="mt-4 flex flex-wrap items-center gap-4 text-xs text-slate-400">
                    <span className="flex items-center gap-1">
                      <span>작성자:</span> {renderAuthorLabel(post, { stopPropagation: true })}
                      {post.authorUsername && (
                        <span className="text-slate-300">@{post.authorUsername}</span>
                      )}
                    </span>
                    <span>조회수: {post.viewCount}</span>
                    <span>{formatDate(post.createdAt)}</span>
                  </div>
                  {post.hashtags && post.hashtags.length > 0 && (
                    <div className="mt-3 flex flex-wrap gap-2">
                      {post.hashtags.map((tag, idx) => (
                        <button
                          key={idx}
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation()
                            setHashtagFilter(tag)
                            setHashtagInput(tag)
                            setPage(0)
                          }}
                          className={`rounded-full border px-3 py-1 text-[11px] uppercase tracking-wide transition ${
                            hashtagFilter.replace(/^#/, '') === tag
                              ? 'border-indigo-300 bg-indigo-500 text-white'
                              : 'border-indigo-200 bg-indigo-50 text-indigo-600 hover:bg-indigo-100'
                          }`}
                        >
                          #{tag}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
                <Button
                  type="button"
                  variant="outline"
                  onClick={(e) => handleLike(post.id, e)}
                  className="ml-4 flex flex-col items-center gap-1 rounded-2xl border-slate-200 bg-white px-4 py-3 text-slate-700 hover:border-indigo-200 hover:text-indigo-600"
                  aria-label="좋아요"
                >
                  <span className={`text-2xl ${post.likeCount > 0 ? 'text-rose-500' : 'text-slate-300'}`}>❤️</span>
                  <span className="text-xs text-slate-500">{post.likeCount}</span>
                </Button>
              </div>
              {post.media && post.media.length > 0 && (
                <div className="mt-4 flex gap-2">
                  {post.media.slice(0, 3).map((media) => (
                    <img
                      key={media.id}
                      src={media.url}
                      alt="게시글 이미지"
                      className="h-24 w-24 rounded-xl border border-slate-200 object-cover"
                    />
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>

        {loading && (
          <div className="py-8 text-center text-slate-500">로딩 중...</div>
        )}

        {!loading && posts.length === 0 && (
          <div className="rounded-[26px] border border-dashed border-slate-200 bg-white/85 py-12 text-center text-sm text-slate-500">
            게시글이 없습니다.
          </div>
        )}

        {hasMore && !loading && (
          <div className="text-center">
            <Button
              type="button"
              className="mt-4 rounded-2xl border-slate-200 bg-white px-6 py-3 text-slate-700 hover:bg-slate-50"
              onClick={() => setPage((prev) => prev + 1)}
            >
              더보기
            </Button>
          </div>
        )}
      </div>
    </div>
  )
}

export default Community

