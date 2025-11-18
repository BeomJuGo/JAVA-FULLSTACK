import React, { createContext, useContext, useState, useEffect, useRef } from 'react'
import { getCurrentUser, isAuthenticated, logout as authLogout } from '../api/authApi'

const AuthContext = createContext(null)

// 로그인 시간을 저장하는 키
const LOGIN_TIME_KEY = 'loginTime'
// 자동 로그아웃 시간 (1시간 = 60분 = 3600000ms)
const AUTO_LOGOUT_TIME = 60 * 60 * 1000 // 1시간

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const logoutTimerRef = useRef(null)

  // 자동 로그아웃 체크 함수
  const checkAutoLogout = () => {
    if (!isAuthenticated()) {
      return
    }

    const loginTimeStr = localStorage.getItem(LOGIN_TIME_KEY)
    if (!loginTimeStr) {
      // 로그인 시간이 없으면 현재 시간으로 설정 (이전 세션 호환성)
      localStorage.setItem(LOGIN_TIME_KEY, Date.now().toString())
      return
    }

    const loginTime = parseInt(loginTimeStr, 10)
    const now = Date.now()
    const elapsed = now - loginTime

    if (elapsed >= AUTO_LOGOUT_TIME) {
      // 1시간이 지났으면 자동 로그아웃
      handleAutoLogout()
    }
  }

  // 자동 로그아웃 처리
  const handleAutoLogout = () => {
    authLogout()
    localStorage.removeItem(LOGIN_TIME_KEY)
    setUser(null)
    if (logoutTimerRef.current) {
      clearInterval(logoutTimerRef.current)
      logoutTimerRef.current = null
    }
    // 로그아웃 알림 (선택사항)
    alert('1시간이 지나 자동으로 로그아웃되었습니다.')
    // 페이지 새로고침하여 로그인 페이지로 이동
    window.location.href = '/login'
  }

  useEffect(() => {
    // 초기 로드 시 사용자 정보 확인 및 자동 로그아웃 체크
    if (isAuthenticated()) {
      const loginTimeStr = localStorage.getItem(LOGIN_TIME_KEY)
      if (!loginTimeStr) {
        // 로그인 시간이 없으면 현재 시간으로 설정
        localStorage.setItem(LOGIN_TIME_KEY, Date.now().toString())
      } else {
        // 로그인 시간이 있으면 자동 로그아웃 체크
        checkAutoLogout()
      }
      setUser(getCurrentUser())
    } else {
      // 인증되지 않았으면 로그인 시간 제거
      localStorage.removeItem(LOGIN_TIME_KEY)
    }
    setLoading(false)

    // 1분마다 자동 로그아웃 체크
    logoutTimerRef.current = setInterval(() => {
      checkAutoLogout()
    }, 60 * 1000) // 1분마다 체크

    // 컴포넌트 언마운트 시 타이머 정리
    return () => {
      if (logoutTimerRef.current) {
        clearInterval(logoutTimerRef.current)
        logoutTimerRef.current = null
      }
    }
  }, [])

  const login = (userData) => {
    setUser(userData)
    // 로그인 시간 저장
    localStorage.setItem(LOGIN_TIME_KEY, Date.now().toString())
    // 기존 타이머가 있으면 정리
    if (logoutTimerRef.current) {
      clearInterval(logoutTimerRef.current)
    }
    // 새로운 타이머 시작
    logoutTimerRef.current = setInterval(() => {
      checkAutoLogout()
    }, 60 * 1000) // 1분마다 체크
  }

  const logout = () => {
    authLogout()
    localStorage.removeItem(LOGIN_TIME_KEY)
    setUser(null)
    // 타이머 정리
    if (logoutTimerRef.current) {
      clearInterval(logoutTimerRef.current)
      logoutTimerRef.current = null
    }
  }

  const value = {
    user,
    isAuthenticated: isAuthenticated(),
    login,
    logout,
    loading,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}


