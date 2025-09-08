<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%
    String ctx = request.getContextPath();
    com.member.MemberDTO loginUser = (com.member.MemberDTO) session.getAttribute("loginUser");
    boolean loggedIn = (loginUser != null);
    boolean isAdmin = loggedIn && loginUser.getUser_level() == 4;
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>한국 음식점 검색</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        .menu-icon { width: 24px; height: 24px; display: flex; flex-direction: column; justify-content: space-between; }
        .menu-icon span { height: 2px; background: black; display: block; }
        .menu-button { background: transparent; border: 0; padding: 4px; cursor: pointer; }
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

    <!-- Main Content -->
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
                        <% if (loginUser.getUser_level() != 4) { %>
                            <button class="px-4 py-2 border-2 border-black hover:bg-gray-100" onclick="location.href='<%=ctx%>/mypage.jsp'">마이페이지</button>
                        <% } %>
                        <button class="px-4 py-2 border-2 border-black hover:bg-gray-100" onclick="location.href='<%=ctx%>/map.jsp'">지도보기</button>
                        <% if (isAdmin) { %>
                            <button class="px-4 py-2 border-2 border-black bg-yellow-300 hover:bg-yellow-400" onclick="location.href='<%=ctx%>/admin.do'">관리자 페이지</button>
                        <% } %>
                    <% } %>
                </div>
            </div>
        </header>

        <!-- Main Section -->
        <main class="flex-1 p-6">
            <h2 class="text-xl font-medium mb-4">Popular</h2>
            <div id="restaurant-grid" class="grid grid-cols-2 gap-4">
                <!-- 음식점 목록 동적 생성 영역 -->
            </div>

            <!-- Pagination -->
            <div class="flex items-center justify-center gap-2 mt-8">
                <button onclick="previousPage()" id="prev-btn" class="px-4 py-2 border-2 border-black hover:bg-gray-100">이전</button>
                <span id="page-info" class="px-3 py-2 border-2 border-black select-none">1 / 1</span>
                <button onclick="nextPage()" id="next-btn" class="px-4 py-2 border-2 border-black hover:bg-gray-100">다음</button>
            </div>
        </main>
    </div>
</div>

<script>
  // ✅ JSP에서 컨텍스트 경로를 JS 변수로 주입
  const ctx = '<%=request.getContextPath()%>';

  // ✅ API 엔드포인트
  const LIST_API = ctx + '/store/list.do';

  let selectedCategory = '';
  let currentPage = 1;
  const pageSize = 8;
  let isSidebarOpen = true;
  let cachedItems = [];

  document.addEventListener('DOMContentLoaded', function() {
    fetchAndRender();
    highlightCategoryButtons();
  });

  function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    isSidebarOpen = !isSidebarOpen;
    sidebar.classList.toggle('hidden', !isSidebarOpen);
  }

  function selectCategory(category) {
    selectedCategory = (selectedCategory === category) ? '' : category;
    fetchAndRender();
    highlightCategoryButtons();
  }

  async function fetchAndRender() {
    const params = new URLSearchParams();
    if (selectedCategory) params.set('category', selectedCategory);

    try {
      const res = await fetch(LIST_API + '?' + params.toString(), { headers: { 'Accept': 'application/json' } });
      const data = await res.json();
      cachedItems = data.items || [];
      currentPage = 1;
      render();
    } catch (e) {
      const grid = document.getElementById('restaurant-grid');
      grid.innerHTML =
        '<div class="col-span-2 text-center text-red-600 py-8 border-2 border-red-300 rounded-xl">목록을 불러오지 못했습니다.</div>';
      document.getElementById('page-info').textContent = '0 / 0';
    }
  }

  function render() {
    const grid = document.getElementById('restaurant-grid');
    const pageInfo = document.getElementById('page-info');
    const prevBtn = document.getElementById('prev-btn');
    const nextBtn = document.getElementById('next-btn');

    const totalPages = getTotalPages();
    const start = (currentPage - 1) * pageSize;
    const pageItems = cachedItems.slice(start, start + pageSize);

    if (!pageItems.length) {
      grid.innerHTML =
        '<div class="col-span-2 text-center text-gray-500 py-8 border-2 border-dashed border-gray-300 rounded-xl">등록된 가게가 없습니다.</div>';
    } else {
      grid.innerHTML = pageItems.map(function(item) {
        return '' +
          '<div class="border-2 border-black p-4 rounded-xl">' +
            '<div class="text-xs mb-1">' + escapeHtml(item.category) + '</div>' +
            '<div class="text-lg font-semibold mb-1">' + escapeHtml(item.name) + '</div>' +
            '<div class="text-sm text-gray-600 mb-2">' + escapeHtml(item.address) + '</div>' +
            '<div class="text-sm mb-3">평점: ' + Number((item.rating ?? 0)).toFixed(1) + '</div>' +
            '<button class="px-3 py-2 border-2 border-black hover:bg-gray-100" ' +
              'onclick="location.href=\'' + ctx + '/review.jsp?storeName=' + encodeURIComponent(item.name) +
              '&address=' + encodeURIComponent(item.address) + '\'">' +
              '리뷰 보기' +
            '</button>' +
          '</div>';
      }).join('');
    }

    pageInfo.textContent = currentPage + ' / ' + totalPages;
    prevBtn.disabled = currentPage <= 1;
    nextBtn.disabled = currentPage >= totalPages;

    [prevBtn, nextBtn].forEach(function(btn) {
      btn.classList.toggle('opacity-40', btn.disabled);
      btn.classList.toggle('cursor-not-allowed', btn.disabled);
    });
  }

  function previousPage() { if (currentPage > 1) { currentPage--; render(); } }
  function nextPage() { const totalPages = getTotalPages(); if (currentPage < totalPages) { currentPage++; render(); } }
  function getTotalPages() { return Math.max(1, Math.ceil(cachedItems.length / pageSize)); }
  function highlightCategoryButtons() {
    document.querySelectorAll('.category-btn').forEach(function(btn) {
      const text = btn.textContent.trim();
      const active = text === selectedCategory;
      btn.classList.toggle('bg-black', active);
      btn.classList.toggle('text-white', active);
      btn.classList.toggle('border-black', active);
    });
  }
  function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"'`=\/]/g, function(c) {
      return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;','/':'&#x2F;','`':'&#x60;','=':'&#x3D;'}[c];
    });
  }
</script>
</body>
</html>
