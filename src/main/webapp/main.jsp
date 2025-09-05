<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    String ctx = request.getContextPath();
    com.member.MemberDTO loginUser = (com.member.MemberDTO) session.getAttribute("loginUser");
    boolean loggedIn = (loginUser != null);
    boolean isAdmin = loggedIn && loginUser.getUser_level() == 4; // 4 = 관리자
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>한국 음식점 검색</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        .menu-icon { width:24px; height:24px; display:flex; flex-direction:column; justify-content:space-between; }
        .menu-icon span { height:2px; background:black; display:block; }
        .menu-button { background:transparent; border:0; padding:4px; cursor:pointer; }
        .transition-colors { transition: background-color 0.3s, color 0.3s, border-color 0.3s; }
    </style>
</head>
<body class="bg-white">
    <div class="min-h-screen flex">
        <!-- Sidebar -->
        <div id="sidebar" class="w-64 bg-white border-r-2 border-black">
            <div class="p-4 border-b-2 border-black flex items-center">
                <button type="button" class="menu-button" onclick="toggleSidebar()" aria-label="사이드바 토글">
                    <div class="menu-icon"><span></span><span></span><span></span></div>
                </button>
                <span class="ml-2">메뉴</span>
            </div>
            <nav class="p-4">
                <ul class="space-y-4">
                    <li><button onclick="selectCategory('일식')" class="category-btn w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors">일식</button></li>
                    <li><button onclick="selectCategory('중식')" class="category-btn w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors">중식</button></li>
                    <li><button onclick="selectCategory('양식')" class="category-btn w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors">양식</button></li>
                    <li><button onclick="selectCategory('한식')" class="category-btn w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors">한식</button></li>
                </ul>
            </nav>
        </div>

        <div class="flex-1 flex flex-col">
            <!-- Header -->
            <header class="border-b-2 border-black bg-white p-4">
                <div class="flex items-center justify-between">
                    <button type="button" class="menu-button" onclick="toggleSidebar()" aria-label="사이드바 토글">
                        <div class="menu-icon"><span></span><span></span><span></span></div>
                    </button>

                    <div class="flex gap-2">
                        <% if (!loggedIn) { %>
                            <button class="px-4 py-2 border-2 border-black hover:bg-gray-100" onclick="location.href='<%=ctx%>/login.jsp'">로그인</button>
                            <button class="px-4 py-2 border-2 border-black hover:bg-gray-100" onclick="location.href='<%=ctx%>/join.jsp'">회원가입</button>
                        <% } else { %>
                            <span class="px-2 py-2">안녕하세요, <strong><%=loginUser.getUser_name()%></strong>님</span>
                            <button class="px-4 py-2 border-2 border-black hover:bg-gray-100" onclick="location.href='<%=ctx%>/logout.jsp'">로그아웃</button>
                            <button class="px-4 py-2 border-2 border-black hover:bg-gray-100" onclick="location.href='<%=ctx%>/mypage.jsp'">마이페이지</button>
                            <button class="px-4 py-2 border-2 border-black hover:bg-gray-100" onclick="location.href='<%=ctx%>/map.jsp'">지도보기</button>
                            <% if (isAdmin) { %>
                                <button class="px-4 py-2 border-2 border-black bg-yellow-300 hover:bg-yellow-400" onclick="location.href='<%=ctx%>/admin.do'">관리자 페이지</button>
                            <% } %>
                        <% } %>
                    </div>
                </div>
            </header>

            <!-- Main Content -->
			<main class="flex-1 p-6">
			    <div class="mb-6">
			        <h2 class="text-xl font-medium mb-4">Popular</h2>
			        <div id="restaurant-grid" class="grid grid-cols-2 gap-4">
			
			            <c:choose>
			                <c:when test="${not empty storeList}">
			                    <c:forEach var="store" items="${storeList}">
			                        <div class="border-4 border-black bg-white">
			                            <div class="p-4">
			                                <div class="aspect-video border-2 border-black mb-4 overflow-hidden flex flex-col justify-center items-center text-center space-y-1">
			                                    <p class="font-bold text-lg">${store.name}</p>
			                                    <p>${store.address}</p>
			                                    <p>Rating: ${store.rating}</p>
			                                    <p>Category: ${store.category}</p>
			                                </div>
			
			                                <button
			                                    class="px-4 py-2 border-2 border-black bg-white hover:bg-gray-100 transition-colors"
			                                    onclick="location.href='${pageContext.request.contextPath}/review.do?storeName=${fn:escapeXml(store.name)}'">
			                                    리뷰보기
			                                </button>
			                            </div>
			                        </div>
			                    </c:forEach>
			                </c:when>
			                <c:otherwise>
			                    <div class="col-span-2 text-center text-gray-500">No stores found.</div>
			                </c:otherwise>
			            </c:choose>
			
			        </div>
			    </div>
			</main>

        </div>
    </div>

    <script>
        let selectedCategory = '';
        let currentPage = 1;
        const totalPages = 5;
        let isSidebarOpen = true;

        function toggleSidebar() {
            const sidebar = document.getElementById('sidebar');
            isSidebarOpen = !isSidebarOpen;
            sidebar.classList.toggle('hidden', !isSidebarOpen);
        }

        function selectCategory(category) {
            selectedCategory = (selectedCategory === category) ? '' : category;
        }

        function previousPage() { if(currentPage > 1) currentPage--; }
        function nextPage() { if(currentPage < totalPages) currentPage++; }
    </script>
</body>
</html>



