
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="false"%>
<%
// 컨텍스트 경로 및 로그인 여부(세션)
String ctx = request.getContextPath();
Object loginUser = session.getAttribute("loginUser"); // 로그인 성공 시 setAttribute("loginUser", user)
boolean loggedIn = (loginUser != null);
%>
<%@ page import="java.util.List, com.store.StoreDTO"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>한국 음식점 검색</title>
<script src="https://cdn.tailwindcss.com"></script>
<style>
.menu-icon {
	width: 24px;
	height: 24px;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.menu-icon span {
	height: 2px;
	background: black;
	display: block;
}

.menu-button {
	background: transparent;
	border: 0;
	padding: 4px;
	cursor: pointer;
}

.aspect-video {
	aspect-ratio: 16/9;
}

.transition-colors {
	transition: background-color 0.3s, color 0.3s, border-color 0.3s;
}
</style>
</head>
<body class="bg-white">
	<div class="min-h-screen bg-white flex">
		<!-- Sidebar -->
		<div id="sidebar" class="w-64 bg-white border-r-2 border-black">
			<div class="p-4 border-b-2 border-black flex items-center">
				<button type="button" class="menu-button" onclick="toggleSidebar()"
					aria-label="사이드바 토글">
					<div class="menu-icon">
						<span></span><span></span><span></span>
					</div>
				</button>
				<span class="ml-2">메뉴</span>
			</div>
			<nav class="p-4">
				<ul class="space-y-4">
                    <li><button onclick="location.href='<%=ctx%>/main.do?category=일식'"
							class="category-btn block w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors text-black hover:bg-gray-100"
							data-category="일식">일식</button></li>
                    <li><button onclick="location.href='<%=ctx%>/main.do?category=중식'"
							class="category-btn block w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors text-black hover:bg-gray-100"
							data-category="중식">중식</button></li>
                    <li><button onclick="location.href='<%=ctx%>/main.do?category=양식'"
							class="category-btn block w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors text-black hover:bg-gray-100"
							data-category="양식">양식</button></li>
                    <li><button onclick="location.href='<%=ctx%>/main.do?category=한식'"
							class="category-btn block w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors text-black hover:bg-gray-100"
							data-category="한식">한식</button></li>
				</ul>
			</nav>
		</div>

		<div class="flex-1 flex flex-col">
			<!-- Header -->
			<header class="border-b-2 border-black bg-white p-4">
				<div class="flex items-center justify-between">
					<!-- 헤더 햄버거(사이드바가 닫혀도 다시 열 수 있게) -->
					<button type="button" class="menu-button" onclick="toggleSidebar()"
						aria-label="사이드바 토글">
						<div class="menu-icon">
							<span></span><span></span><span></span>
						</div>
					</button>

					<div class="flex justify-end gap-2">
						<%
						if (!loggedIn) {
						%>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/main.do'">메인으로</button>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/login.jsp'">로그인</button>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/join.jsp'">회원가입</button>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/map.jsp'">지도보기</button>
						<%
						} else {
						%>
						<span class="px-2 py-2">안녕하세요, <strong><%=loginUser%></strong>님
						</span>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/main.do'">메인으로</button>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/logout.jsp'">로그아웃</button>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/mypage.jsp'">마이페이지</button>
						<button type="button"
							class="px-4 py-2 border-2 border-black transition-colors bg-white text-black hover:bg-gray-100"
							onclick="location.href='<%=ctx%>/map.jsp'">지도보기</button>
						<%
						}
						%>
					</div>
				</div>
			</header>

			<!-- Main Content -->
			<main class="flex-1 p-6">
				<div class="mb-6">
					<h2 class="text-xl font-medium mb-4">Popular</h2>
					<div id="restaurant-grid" class="grid grid-cols-2 gap-4">
					<%
									List<StoreDTO> storeList = (List<StoreDTO>) request.getAttribute("storeList");
									if (storeList != null && !storeList.isEmpty()) {
										for (StoreDTO store : storeList) {
									%>
						<div class="border-4 border-black bg-white">
						
							<div class="p-4">
								
								<div class="aspect-video border-2 border-black mb-4 overflow-hidden">
									
									<tr>
										<td><%=store.getName()%></td>
										<td><%=store.getAdress()%></td>
										<td><%=String.format("%.1f", store.getRating())%></td>
										<td><%=store.getCategory()%></td>
									</tr>
									

								</div>
								
								<button
									class="px-4 py-2 border-2 border-black bg-white hover:bg-gray-100 transition-colors"
									onclick="location.href='<%=ctx%>/review.do'">리뷰보기</button>
							</div>
							
						</div>
						<%
									}
									} else {
									%>
									<tr>
										<td colspan="4">No stores found.</td>
									</tr>
									<%
									}
									%>
					</div>
				</div>

				<!-- Pagination -->
				<div class="flex items-center justify-center gap-2 mt-8">
					<button onclick="previousPage()" id="prev-btn"
						class="px-4 py-2 border-2 border-black bg-white hover:bg-gray-100 transition-colors">
						이전</button>

					<!-- 현재/전체 표시 -->
					<span id="page-info"
						class="px-3 py-2 border-2 border-black bg-white select-none">1
						/ 5</span>

					<!-- 숫자 버튼 5개 영역(이전과 다음 사이) -->
					<div id="page-numbers" class="flex gap-2">
						<!-- 숫자 버튼들이 동적 생성 -->
					</div>

					<button onclick="nextPage()" id="next-btn"
						class="px-4 py-2 border-2 border-black bg-white hover:bg-gray-100 transition-colors">
						다음</button>
				</div>
			</main>
		</div>
	</div>

	<script>


        // ----- State -----
        let selectedCategory = '';
        let currentPage = 1;
        const totalPages = 5; // 서버 페이징 붙이면 실제 값으로 교체
        let isSidebarOpen = true;

        // ----- Sidebar -----
        function toggleSidebar() {
            const sidebar = document.getElementById('sidebar');
            isSidebarOpen = !isSidebarOpen;
            sidebar.classList.toggle('hidden', !isSidebarOpen);
        }

        function updateCategoryButtons() {
            document.querySelectorAll('.category-btn').forEach(btn => {
                const category = btn.getAttribute('data-category');
                const active = (category === selectedCategory);
                btn.className =
                    'category-btn block w-full text-left py-2 px-4 border-2 border-transparent hover:border-black transition-colors ' +
                    (active ? 'bg-black text-white' : 'text-black hover:bg-gray-100');
            });
        }

        // ----- Pagination -----
        function renderPagination() {
            // 이전/다음 버튼 상태 + 이동될 페이지 번호 표시
            prevBtn.disabled = (currentPage === 1);
            nextBtn.disabled = (currentPage === totalPages);
            prevBtn.classList.toggle('opacity-50', currentPage === 1);
            nextBtn.classList.toggle('opacity-50', currentPage === totalPages);
            prevBtn.textContent = (currentPage > 1) ? `이전 ${currentPage - 1}` : '이전';
            nextBtn.textContent = (currentPage < totalPages) ? `다음 ${currentPage + 1}` : '다음';

            // 현재/전체 표시
            if (pageInfo) pageInfo.textContent = `${currentPage} / ${totalPages}`;
        }

        // ----- Events -----
        function selectCategory(category) {
            selectedCategory = (selectedCategory === category) ? '' : category;
            updateCategoryButtons();
            renderRestaurantGrid();
        }

        function setCurrentPage(page) { currentPage = page; renderPagination(); }
        function previousPage() { if (currentPage > 1) { currentPage--; renderPagination(); } }
        function nextPage() { if (currentPage < totalPages) { currentPage++; renderPagination(); } }

        // ----- Init -----
        function init() {
            renderPagination();
            updateCategoryButtons();
        }
        document.addEventListener('DOMContentLoaded', init);
    </script>
</body>
</html>
