import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getPosts } from '../api/communityApi'
import { getComments } from '../api/communityApi'
import { getAllUsers, getAllTrainers } from '../api/profileApi'
import { getMyMatches } from '../api/matchApi'
import { hidePost, hideComment, suspendAccount, restoreAccount, forceEndMatch, getAdminLogs } from '../api/adminApi'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card'

function AdminPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState('posts')
  const [loading, setLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(null)

  // 게시글 관리
  const [posts, setPosts] = useState([])
  const [postsPage, setPostsPage] = useState(0)
  const [postsHasMore, setPostsHasMore] = useState(true)

  // 댓글 관리
  const [selectedPostId, setSelectedPostId] = useState(null)
  const [comments, setComments] = useState([])
  const [commentsPage, setCommentsPage] = useState(0)

  // 계정 관리
  const [users, setUsers] = useState([])
  const [trainers, setTrainers] = useState([])
  const [allAccounts, setAllAccounts] = useState([])

  // 매칭 관리
  const [matches, setMatches] = useState([])

  // 로그
  const [logs, setLogs] = useState([])
  const [logsPage, setLogsPage] = useState(0)
  const [logsHasMore, setLogsHasMore] = useState(true)

  // 액션 모달
  const [actionModal, setActionModal] = useState(null)
  const [actionReason, setActionReason] = useState('')

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      navigate('/')
      return
    }
    loadInitialData()
  }, [user, navigate])

  const loadInitialData = async () => {
    setLoading(true)
    try {
      await Promise.all([
        loadPosts(),
        loadAccounts(),
        loadMatches(),
        loadLogs(),
      ])
    } catch (err) {
      console.error('초기 데이터 로드 실패:', err)
      alert('데이터를 불러오는데 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const loadPosts = async (page = 0) => {
    try {
      const data = await getPosts(page, 20, null, null, 'latest')
      if (page === 0) {
        setPosts(data.content || [])
      } else {
        setPosts(prev => [...prev, ...(data.content || [])])
      }
      setPostsHasMore(!data.last)
      setPostsPage(page)
    } catch (err) {
      console.error('게시글 로드 실패:', err)
    }
  }

  const loadComments = async (postId, page = 0) => {
    try {
      const data = await getComments(postId, page, 50)
      if (page === 0) {
        setComments(data.content || [])
      } else {
        setComments(prev => [...prev, ...(data.content || [])])
      }
      setCommentsPage(page)
    } catch (err) {
      console.error('댓글 로드 실패:', err)
    }
  }

  const loadAccounts = async () => {
    try {
      const [usersData, trainersData] = await Promise.all([
        getAllUsers().catch(() => []),
        getAllTrainers().catch(() => []),
      ])
      setUsers(usersData || [])
      setTrainers(trainersData || [])
      
      // 계정 정보를 합치기 (accountId 기준)
      const accountsMap = new Map()
      usersData?.forEach(u => {
        if (u.accountId) {
          accountsMap.set(u.accountId, {
            accountId: u.accountId,
            username: u.username,
            displayName: u.displayName,
            role: 'USER',
            status: u.status || 'ACTIVE',
            profileId: u.id,
          })
        }
      })
      trainersData?.forEach(t => {
        if (t.accountId) {
          accountsMap.set(t.accountId, {
            accountId: t.accountId,
            username: t.username,
            displayName: t.displayName,
            role: 'TRAINER',
            status: t.status || 'ACTIVE',
            profileId: t.id,
          })
        }
      })
      setAllAccounts(Array.from(accountsMap.values()))
    } catch (err) {
      console.error('계정 로드 실패:', err)
    }
  }

  const loadMatches = async () => {
    try {
      // 모든 매칭을 보려면 백엔드에 관리자용 API가 필요하지만, 일단 현재 사용자의 매칭만 조회
      const data = await getMyMatches()
      setMatches(data || [])
    } catch (err) {
      console.error('매칭 로드 실패:', err)
    }
  }

  const loadLogs = async (page = 0) => {
    try {
      const data = await getAdminLogs(page, 20)
      if (page === 0) {
        setLogs(data.content || [])
      } else {
        setLogs(prev => [...prev, ...(data.content || [])])
      }
      setLogsHasMore(!data.last)
      setLogsPage(page)
    } catch (err) {
      console.error('로그 로드 실패:', err)
    }
  }

  const handlePostAction = async (postId, hidden) => {
    if (!actionReason.trim()) {
      alert('사유를 입력해주세요.')
      return
    }
    setActionLoading(`post-${postId}`)
    try {
      await hidePost(postId, hidden, actionReason)
      alert(hidden ? '게시글이 숨겨졌습니다.' : '게시글이 다시 보이도록 설정되었습니다.')
      setActionModal(null)
      setActionReason('')
      await loadPosts(0)
    } catch (err) {
      alert(err.response?.data?.message || '작업에 실패했습니다.')
    } finally {
      setActionLoading(null)
    }
  }

  const handleCommentAction = async (commentId, hidden) => {
    if (!actionReason.trim()) {
      alert('사유를 입력해주세요.')
      return
    }
    setActionLoading(`comment-${commentId}`)
    try {
      await hideComment(commentId, hidden, actionReason)
      alert(hidden ? '댓글이 숨겨졌습니다.' : '댓글이 다시 보이도록 설정되었습니다.')
      setActionModal(null)
      setActionReason('')
      if (selectedPostId) {
        await loadComments(selectedPostId, 0)
      }
    } catch (err) {
      alert(err.response?.data?.message || '작업에 실패했습니다.')
    } finally {
      setActionLoading(null)
    }
  }

  const handleAccountSuspend = async (accountId) => {
    if (!actionReason.trim()) {
      alert('사유를 입력해주세요.')
      return
    }
    setActionLoading(`suspend-${accountId}`)
    try {
      await suspendAccount(accountId, actionReason)
      alert('계정이 정지되었습니다.')
      setActionModal(null)
      setActionReason('')
      await loadAccounts()
    } catch (err) {
      alert(err.response?.data?.message || '작업에 실패했습니다.')
    } finally {
      setActionLoading(null)
    }
  }

  const handleAccountRestore = async (accountId) => {
    if (!actionReason.trim()) {
      alert('사유를 입력해주세요.')
      return
    }
    setActionLoading(`restore-${accountId}`)
    try {
      await restoreAccount(accountId, actionReason)
      alert('계정이 복구되었습니다.')
      setActionModal(null)
      setActionReason('')
      await loadAccounts()
    } catch (err) {
      alert(err.response?.data?.message || '작업에 실패했습니다.')
    } finally {
      setActionLoading(null)
    }
  }

  const handleMatchForceEnd = async (matchId) => {
    if (!actionReason.trim()) {
      alert('사유를 입력해주세요.')
      return
    }
    setActionLoading(`match-${matchId}`)
    try {
      await forceEndMatch(matchId, actionReason)
      alert('매칭이 강제 종료되었습니다.')
      setActionModal(null)
      setActionReason('')
      await loadMatches()
    } catch (err) {
      alert(err.response?.data?.message || '작업에 실패했습니다.')
    } finally {
      setActionLoading(null)
    }
  }

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  if (user?.role !== 'ADMIN') {
    return null
  }

  return (
    <div className="relative isolate min-h-screen overflow-hidden bg-gradient-to-br from-slate-50 via-white to-blue-50 px-4 py-12 text-slate-800 sm:px-6 lg:px-10">
      <div className="pointer-events-none absolute inset-0 -z-10">
        <div className="absolute -top-36 left-[12%] h-72 w-72 rounded-full bg-red-200/40 blur-[120px]" />
        <div className="absolute right-[10%] top-[18%] h-64 w-64 rounded-full bg-orange-200/35 blur-[120px]" />
      </div>

      <div className="relative mx-auto w-full max-w-7xl">
        <div className="mb-8 rounded-[28px] border border-white/80 bg-white/95 px-6 py-5 shadow-[0_18px_60px_-40px_rgba(15,23,42,0.45)]">
          <p className="text-xs font-semibold uppercase tracking-[0.35em] text-rose-400">Admin</p>
          <h1 className="mt-2 text-3xl font-bold text-slate-900">관리자 페이지</h1>
          <p className="text-slate-500">시스템 관리 및 모니터링</p>
        </div>

        {/* 탭 메뉴 */}
        <div className="mb-6 flex flex-wrap gap-2 border-b border-slate-200">
          {[
            { id: 'posts', label: '게시글 관리' },
            { id: 'comments', label: '댓글 관리' },
            { id: 'accounts', label: '계정 관리' },
            { id: 'matches', label: '매칭 관리' },
            { id: 'logs', label: '관리자 로그' },
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => {
                setActiveTab(tab.id)
                if (tab.id === 'comments' && !selectedPostId) {
                  // 댓글 관리 탭에서는 게시글 선택 필요
                }
              }}
              className={`px-4 py-2 font-medium transition ${
                activeTab === tab.id
                  ? 'border-b-2 border-indigo-500 text-indigo-600'
                  : 'text-slate-400 hover:text-slate-600'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* 게시글 관리 */}
        {activeTab === 'posts' && (
          <Card className="border border-slate-200 bg-white/95 shadow-sm">
            <CardHeader>
              <CardTitle className="text-slate-900">게시글 관리</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="py-8 text-center text-slate-500">로딩 중...</p>
              ) : posts.length === 0 ? (
                <p className="py-8 text-center text-slate-500">게시글이 없습니다.</p>
              ) : (
                <div className="space-y-4">
                  {posts.map(post => (
                    <div
                      key={post.id}
                      className="rounded-2xl border border-slate-200 bg-white/90 p-4"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <h3 className="font-semibold text-slate-900">{post.title}</h3>
                            {post.hidden && (
                              <span className="rounded px-2 py-1 text-xs text-red-600 bg-red-50 border border-red-100">
                                숨김
                              </span>
                            )}
                          </div>
                          <p className="mb-2 line-clamp-2 text-sm text-slate-600">
                            {post.content}
                          </p>
                          <div className="flex items-center gap-4 text-xs text-slate-500">
                            <span>작성자: {post.authorDisplayName || post.authorUsername}</span>
                            <span>작성일: {formatDate(post.createdAt)}</span>
                            <span>조회수: {post.viewCount || 0}</span>
                            <span>좋아요: {post.likeCount || 0}</span>
                          </div>
                        </div>
                        <div className="ml-4 flex gap-2">
                          <Button
                            size="sm"
                            variant={post.hidden ? "default" : "destructive"}
                            onClick={() => setActionModal({ type: 'post', id: post.id, hidden: !post.hidden })}
                            disabled={actionLoading === `post-${post.id}`}
                          >
                            {post.hidden ? '보이기' : '숨기기'}
                          </Button>
                        </div>
                      </div>
                    </div>
                  ))}
                  {postsHasMore && (
                    <Button
                      onClick={() => loadPosts(postsPage + 1)}
                      className="w-full mt-4"
                      variant="outline"
                    >
                      더 보기
                    </Button>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* 댓글 관리 */}
        {activeTab === 'comments' && (
          <Card className="border border-slate-200 bg-white/95 shadow-sm">
            <CardHeader>
              <CardTitle className="text-slate-900">댓글 관리</CardTitle>
            </CardHeader>
            <CardContent>
              {!selectedPostId ? (
                <div>
                  <p className="mb-4 text-slate-500">게시글을 선택하여 댓글을 관리하세요.</p>
                  <div className="max-h-96 space-y-2 overflow-y-auto">
                    {posts.slice(0, 20).map(post => (
                      <button
                        key={post.id}
                        onClick={() => {
                          setSelectedPostId(post.id)
                          loadComments(post.id, 0)
                        }}
                        className="w-full rounded-lg border border-slate-200 bg-white/90 p-3 text-left transition hover:bg-white"
                      >
                        <div className="font-medium text-slate-900">{post.title}</div>
                        <div className="mt-1 text-xs text-slate-500">
                          작성자: {post.authorDisplayName || post.authorUsername} | 
                          댓글 수: {post.commentCount || 0}
                        </div>
                      </button>
                    ))}
                  </div>
                </div>
              ) : (
                <div>
                  <div className="flex items-center justify-between mb-4">
                    <div>
                      <button
                        onClick={() => {
                          setSelectedPostId(null)
                          setComments([])
                        }}
                        className="mb-2 text-indigo-500 hover:text-indigo-600"
                      >
                        ← 게시글 목록으로
                      </button>
                      <p className="text-slate-500">게시글 ID: {selectedPostId}</p>
                    </div>
                    <Button
                      size="sm"
                      onClick={() => loadComments(selectedPostId, 0)}
                    >
                      새로고침
                    </Button>
                  </div>
                  {comments.length === 0 ? (
                    <p className="py-8 text-center text-slate-500">댓글이 없습니다.</p>
                  ) : (
                    <div className="space-y-3">
                      {comments.map(comment => (
                        <div
                          key={comment.id}
                          className="rounded-xl border border-slate-200 bg-white/90 p-3"
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-1">
                                <span className="font-medium text-slate-900">
                                  {comment.authorDisplayName || comment.authorUsername}
                                </span>
                                {comment.hidden && (
                                  <span className="rounded px-2 py-0.5 text-xs text-red-600 bg-red-50 border border-red-100">
                                    숨김
                                  </span>
                                )}
                              </div>
                              <p className="text-sm text-slate-600">{comment.content}</p>
                              <p className="mt-1 text-xs text-slate-500">
                                {formatDate(comment.createdAt)}
                              </p>
                            </div>
                            <div className="ml-4 flex gap-2">
                              <Button
                                size="sm"
                                variant={comment.hidden ? "default" : "destructive"}
                                onClick={() => setActionModal({ type: 'comment', id: comment.id, hidden: !comment.hidden })}
                                disabled={actionLoading === `comment-${comment.id}`}
                              >
                                {comment.hidden ? '보이기' : '숨기기'}
                              </Button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* 계정 관리 */}
        {activeTab === 'accounts' && (
          <Card className="border border-slate-200 bg-white/95 shadow-sm">
            <CardHeader>
              <CardTitle className="text-slate-900">계정 관리</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="py-8 text-center text-slate-500">로딩 중...</p>
              ) : allAccounts.length === 0 ? (
                <p className="py-8 text-center text-slate-500">계정이 없습니다.</p>
              ) : (
                <div className="space-y-3">
                  {allAccounts.map(account => (
                    <div
                      key={account.accountId}
                      className="rounded-xl border border-slate-200 bg-white/90 p-4"
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-semibold text-slate-900">{account.displayName}</span>
                            <span className="rounded px-2 py-0.5 text-xs text-indigo-600 bg-indigo-50 border border-indigo-100">
                              {account.role}
                            </span>
                            {account.status === 'SUSPENDED' && (
                              <span className="rounded px-2 py-0.5 text-xs text-red-600 bg-red-50 border border-red-100">
                                정지됨
                              </span>
                            )}
                          </div>
                          <p className="text-sm text-slate-500">@{account.username}</p>
                        </div>
                        <div className="flex gap-2">
                          {account.status === 'SUSPENDED' ? (
                            <Button
                              size="sm"
                              variant="default"
                              onClick={() => setActionModal({ type: 'restore', id: account.accountId })}
                              disabled={actionLoading === `restore-${account.accountId}`}
                            >
                              복구
                            </Button>
                          ) : (
                            <Button
                              size="sm"
                              variant="destructive"
                              onClick={() => setActionModal({ type: 'suspend', id: account.accountId })}
                              disabled={actionLoading === `suspend-${account.accountId}`}
                            >
                              정지
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* 매칭 관리 */}
        {activeTab === 'matches' && (
          <Card className="border border-slate-200 bg-white/95 shadow-sm">
            <CardHeader>
              <CardTitle className="text-slate-900">매칭 관리</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="py-8 text-center text-slate-500">로딩 중...</p>
              ) : matches.length === 0 ? (
                <p className="py-8 text-center text-slate-500">매칭이 없습니다.</p>
              ) : (
                <div className="space-y-3">
                  {matches.map(match => (
                    <div
                      key={match.id}
                      className="rounded-xl border border-slate-200 bg-white/90 p-4"
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-semibold text-slate-900">매칭 #{match.id}</span>
                            <span className="rounded px-2 py-0.5 text-xs text-indigo-600 bg-indigo-50 border border-indigo-100">
                              {match.status}
                            </span>
                          </div>
                          <p className="text-sm text-slate-500">
                            사용자: {match.partnerName || `ID ${match.userId}`} | 
                            트레이너: {match.partnerName || `ID ${match.trainerId}`}
                          </p>
                        </div>
                        {match.status !== 'ENDED' && match.status !== 'FORCE_ENDED' && (
                          <Button
                            size="sm"
                            variant="destructive"
                            onClick={() => setActionModal({ type: 'match', id: match.id })}
                            disabled={actionLoading === `match-${match.id}`}
                          >
                            강제 종료
                          </Button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* 관리자 로그 */}
        {activeTab === 'logs' && (
          <Card className="border border-slate-200 bg-white/95 shadow-sm">
            <CardHeader>
              <CardTitle className="text-slate-900">관리자 로그</CardTitle>
            </CardHeader>
            <CardContent>
              {loading ? (
                <p className="py-8 text-center text-slate-500">로딩 중...</p>
              ) : logs.length === 0 ? (
                <p className="py-8 text-center text-slate-500">로그가 없습니다.</p>
              ) : (
                <div className="space-y-3">
                  {logs.map(log => (
                    <div
                      key={log.id}
                      className="rounded-xl border border-slate-200 bg-white/90 p-4"
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="font-medium text-slate-900">{log.actionType}</span>
                            <span className="rounded px-2 py-0.5 text-xs text-slate-600 bg-slate-100 border border-slate-200">
                              {log.targetType}
                            </span>
                          </div>
                          <p className="text-sm text-slate-600">
                            대상 ID: {log.targetId} | 관리자 ID: {log.adminAcc}
                          </p>
                          {log.reason && (
                            <p className="mt-1 text-sm text-slate-500">사유: {log.reason}</p>
                          )}
                          <p className="mt-1 text-xs text-slate-400">
                            {formatDate(log.createdAt)}
                          </p>
                        </div>
                      </div>
                    </div>
                  ))}
                  {logsHasMore && (
                    <Button
                      onClick={() => loadLogs(logsPage + 1)}
                      className="w-full mt-4"
                      variant="outline"
                    >
                      더 보기
                    </Button>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* 액션 모달 */}
        {actionModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm">
            <Card className="w-full max-w-md border border-slate-200 bg-white/95 shadow-2xl">
              <CardHeader>
                <CardTitle className="text-slate-900">
                  {actionModal.type === 'post' && (actionModal.hidden ? '게시글 숨기기' : '게시글 보이기')}
                  {actionModal.type === 'comment' && (actionModal.hidden ? '댓글 숨기기' : '댓글 보이기')}
                  {actionModal.type === 'suspend' && '계정 정지'}
                  {actionModal.type === 'restore' && '계정 복구'}
                  {actionModal.type === 'match' && '매칭 강제 종료'}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <label className="mb-2 block text-sm font-medium text-slate-600">
                    사유 (필수)
                  </label>
                  <Textarea
                    value={actionReason}
                    onChange={(e) => setActionReason(e.target.value)}
                    placeholder="사유를 입력하세요..."
                    className="rounded-2xl border border-slate-200 bg-slate-50 text-slate-800 focus-visible:ring-indigo-200"
                    rows={4}
                  />
                </div>
                <div className="flex justify-end gap-2">
                  <Button
                    variant="outline"
                    onClick={() => {
                      setActionModal(null)
                      setActionReason('')
                    }}
                  > 
                    취소
                  </Button>
                  <Button
                    onClick={() => {
                      if (actionModal.type === 'post') {
                        handlePostAction(actionModal.id, actionModal.hidden)
                      } else if (actionModal.type === 'comment') {
                        handleCommentAction(actionModal.id, actionModal.hidden)
                      } else if (actionModal.type === 'suspend') {
                        handleAccountSuspend(actionModal.id)
                      } else if (actionModal.type === 'restore') {
                        handleAccountRestore(actionModal.id)
                      } else if (actionModal.type === 'match') {
                        handleMatchForceEnd(actionModal.id)
                      }
                    }}
                    disabled={!actionReason.trim() || actionLoading}
                  >
                    확인
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </div>
  )
}

export default AdminPage

