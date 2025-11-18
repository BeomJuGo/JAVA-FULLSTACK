/** 앱 엔트리포인트 */
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import App from './App.jsx'
import Login from './pages/Login.jsx'
import Signup from './pages/Signup.jsx'
import PlanDetail from './pages/PlanDetail.jsx'
import Matches from './pages/Matches.jsx'
import Community from './pages/Community.jsx'
import CommunityPost from './pages/CommunityPost.jsx'
import CommunityNew from './pages/CommunityNew.jsx'
import Trainers from './pages/Trainers.jsx'
import TrainerDetail from './pages/TrainerDetail.jsx'
import Reviews from './pages/Reviews.jsx'
import Chat from './pages/Chat.jsx'
import Certification from './pages/Certification.jsx'
import TrainerPlan from './pages/TrainerPlan.jsx'
import MyPage from './pages/MyPage.jsx'
import UserDetail from './pages/UserDetail.jsx'
import EditDisplayName from './pages/EditDisplayName.jsx'
import AiRecommendation from './pages/AiRecommendation.jsx'
import AdminPage from './pages/AdminPage.jsx'

import './index.css' /**tailwind import */

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    <Route path="/login" element={<Login />} />
                    <Route path="/signup" element={<Signup />} />
                    <Route
                        path="/"
                        element={
                            <ProtectedRoute>
                                <App />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/plans/:matchId/:weekStart"
                        element={
                            <ProtectedRoute>
                                <PlanDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/matches"
                        element={
                            <ProtectedRoute>
                                <Matches />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/community"
                        element={
                            <ProtectedRoute>
                                <Community />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/community/new"
                        element={
                            <ProtectedRoute>
                                <CommunityNew />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/community/:postId"
                        element={
                            <ProtectedRoute>
                                <CommunityPost />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/profiles/trainers"
                        element={
                            <ProtectedRoute>
                                <Trainers />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/profiles/trainers/:id"
                        element={
                            <ProtectedRoute>
                                <TrainerDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/profiles/users/:id"
                        element={
                            <ProtectedRoute>
                                <UserDetail />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/reviews"
                        element={
                            <ProtectedRoute>
                                <Reviews />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/chat/:matchId"
                        element={
                            <ProtectedRoute>
                                <Chat />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/certification/:date"
                        element={
                            <ProtectedRoute>
                                <Certification />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/trainer-plan/:matchId/:weekStart"
                        element={
                            <ProtectedRoute>
                                <TrainerPlan />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/mypage"
                        element={
                            <ProtectedRoute>
                                <MyPage />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/mypage/edit-display-name"
                        element={
                            <ProtectedRoute>
                                <EditDisplayName />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/ai-recommendation"
                        element={
                            <ProtectedRoute>
                                <AiRecommendation />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/admin"
                        element={
                            <ProtectedRoute>
                                <AdminPage />
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    </React.StrictMode>
)