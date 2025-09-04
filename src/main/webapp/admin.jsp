<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.store.StoreDTO" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>관리자 페이지</title>
<script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-50">
    <div class="container mx-auto p-6">
        <h1 class="text-2xl font-bold mb-4">관리자 페이지</h1>
        <!-- 평균 평점 표시 -->
        <div class="mb-6">
            <strong>현재 전체 평균 평점: </strong>
            <span><%= (request.getAttribute("avgRating") != null) ? String.format("%.1f", (double)request.getAttribute("avgRating")) : "0.0" %></span>
        </div>
        <!-- 새 상점 추가 폼 -->
        <div class="mb-8 p-4 border border-gray-300 rounded bg-white">
            <h2 class="text-xl font-semibold mb-2">새 가게 등록</h2>
            <form action="<%= request.getContextPath() %>/admin.do" method="post" class="space-y-4">
                <input type="hidden" name="action" value="insert">
                <div>
                    <label class="block text-sm font-medium mb-1" for="name">가게 이름</label>
                    <input class="w-full border px-3 py-2" type="text" id="name" name="name" required>
                </div>
                <div>
                    <label class="block text-sm font-medium mb-1" for="adress">주소</label>
                    <input class="w-full border px-3 py-2" type="text" id="adress" name="adress" required>
                </div>
                <div>
                    <label class="block text-sm font-medium mb-1" for="rating">평점 (예: 4.5)</label>
                    <input class="w-full border px-3 py-2" type="number" id="rating" name="rating" step="0.1" min="0" max="5" required>
                </div>
                <div>
                    <label class="block text-sm font-medium mb-1" for="category">카테고리</label>
                    <select class="w-full border px-3 py-2" id="category" name="category" required>
                        <option value="일식">일식</option>
                        <option value="중식">중식</option>
                        <option value="양식">양식</option>
                        <option value="한식">한식</option>
                    </select>
                </div>
                <div>
                    <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">등록</button>
                </div>
            </form>
        </div>
        <!-- 상점 목록 표시 -->
        <div class="p-4 border border-gray-300 rounded bg-white">
            <h2 class="text-xl font-semibold mb-2">등록된 가게 목록</h2>
            <table class="min-w-full border text-left text-sm">
                <thead>
                    <tr class="bg-gray-100">
                        <th class="px-3 py-2 border">이름</th>
                        <th class="px-3 py-2 border">주소</th>
                        <th class="px-3 py-2 border">평점</th>
                        <th class="px-3 py-2 border">카테고리</th>
                        <th class="px-3 py-2 border">삭제</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        List<StoreDTO> storeList = (List<StoreDTO>) request.getAttribute("storeList");
                        if (storeList != null && !storeList.isEmpty()) {
                            for (StoreDTO store : storeList) {
                    %>
                    <tr>
                        <td class="px-3 py-2 border"><%= store.getName() %></td>
                        <td class="px-3 py-2 border"><%= store.getAdress() %></td>
                        <td class="px-3 py-2 border"><%= String.format("%.1f", store.getRating()) %></td>
                        <td class="px-3 py-2 border"><%= store.getCategory() %></td>
                        <td class="px-3 py-2 border">
                            <form action="<%= request.getContextPath() %>/admin.do" method="post" onsubmit="return confirm('정말 삭제하시겠습니까?');">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="name" value="<%= store.getName() %>">
                                <button type="submit" class="text-red-600 hover:underline">삭제</button>
                            </form>
                        </td>
                    </tr>
                    <%
                            }
                        } else {
                    %>
                    <tr>
                        <td colspan="5" class="px-3 py-4 text-center">등록된 가게가 없습니다.</td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
