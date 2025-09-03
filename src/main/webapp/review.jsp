<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="jakarta.tags.functions" prefix="fn"%>
<%
// 가능하면 컨트롤러에서 setAttribute 하세요.
// 여기서는 안전장치(없을 때 기본값)만 둡니다.
String ctx = request.getContextPath();
if (request.getAttribute("page") == null)
	request.setAttribute("page", 1);
if (request.getAttribute("totalPages") == null)
	request.setAttribute("totalPages", 1);
%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>가게 리뷰 관리</title>
<style>
@font-face {
	font-family: 'Paperozi';
	src:
		url('https://cdn.jsdelivr.net/gh/projectnoonnu/2408-3@1.0/Paperlogy-9Black.woff2')
		format('woff2');
	font-weight: 900;
	font-display: swap;
}
/* (사용자 CSS 그대로 유지) */
* {
	margin: 0;
	padding: 0;
	box-sizing: border-box
}

body {
	font-family: 'Paperozi';
	font-size: 14px;
	line-height: 1.5;
	background-color: #fff;
	color: #000
}

.container {
	min-height: 100vh;
	padding: 1rem
}

.main-wrapper {
	max-width: 56rem;
	margin: 0 auto
}

.main-container {
	border: 2px solid #000;
	padding: 1.5rem
}

.top-section {
	display: flex;
	gap: 1.5rem;
	margin-bottom: 2rem
}

.img-area {
	border: 2px solid #000;
	width: 12rem;
	height: 8rem;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #fff
}

.img-area img {
	max-width: 100%;
	max-height: 100%;
	object-fit: contain
}

.form-area {
	flex: 1
}

.form-fields {
	display: flex;
	flex-direction: column;
	gap: 1rem
}

.field-group {
	display: flex;
	flex-direction: column
}

.field-label {
	display: block;
	margin-bottom: .25rem;
	font-weight: 500
}

.field-input-wrapper {
	border-bottom: 2px solid #000;
	padding-bottom: .25rem
}

.field-input {
	width: 100%;
	background: transparent;
	outline: none;
	border: none;
	font-size: 14px;
	line-height: 1.5
}

.right-buttons {
	display: flex;
	flex-direction: column;
	gap: .5rem
}

.button {
	border: 1px solid #000;
	padding: .25rem 1rem;
	background: #fff;
	cursor: pointer;
	font-size: 14px;
	font-weight: 500;
	line-height: 1.5
}

.button:hover {
	background: #f9fafb
}

.review-section {
	margin-bottom: 1.5rem
}

.review-list {
	display: flex;
	flex-direction: column;
	gap: .5rem
}

.review-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	border-bottom: 1px solid #000;
	padding: .5rem 0
}

.review-item:last-child {
	border-bottom: 2px solid #000
}

.review-left {
	display: flex;
	gap: 2rem
}

.bottom-section {
	display: flex;
	align-items: center;
	justify-content: space-between
}

.pagination {
	display: flex;
	align-items: center;
	gap: 1rem
}

.pagination-button {
	border: 1px solid #000;
	padding: .25rem .75rem;
	background: #fff;
	cursor: pointer;
	font-size: 14px;
	font-weight: 500;
	line-height: 1.5;
	text-decoration: none;
	color: #000
}

.pagination-button:hover {
	background: #f9fafb
}

.review-write-button {
	border: 1px solid #000;
	padding: .25rem 1rem;
	background: #fff;
	cursor: pointer;
	font-size: 14px;
	font-weight: 500;
	line-height: 1.5
}

.review-write-button:hover {
	background: #f9fafb
}

.review-right {
	display: flex;
	align-items: center;
	gap: .5rem
}

.btn-link {
	border: 1px solid #000;
	padding: .2rem .6rem;
	background: #fff;
	cursor: pointer;
	font-size: 12px;
	text-decoration: none;
	color: #000
}

.btn-link:hover {
	background: #f9fafb
}
</style>
</head>
<body>
	<div class="container">
		<div class="main-wrapper">
			<div class="main-container">
				<!-- 상단 섹션 -->
				<div class="top-section">
					<!-- 폼/정보 영역 -->
					<div class="form-area">
						<div class="form-fields">
							<div class="field-group">
								<label class="field-label">가게이름</label>
								<div class="field-input-wrapper">
									<c:out value="${store.name}" />
								</div>
							</div>
							<div class="field-group">
								<label class="field-label">가게주소</label>
								<div class="field-input-wrapper">
									<c:out value="${store.address}" />
								</div>
							</div>
							<div class="field-group">
								<label class="field-label">평균 평점</label>
								<div class="field-input-wrapper">
									<c:out value="${store.avg}" />
								</div>
							</div>
						</div>
					</div>

					<!-- 우측 버튼 -->
					<div class="right-buttons">
						<button class="button" onclick="location.href='<%=ctx%>/main.jsp'">메인으로</button>
						<button class="button" onclick="location.href='<%=ctx%>/map.jsp'">지도보기</button>
					</div>
				</div>

				<!-- 리뷰 목록 섹션 -->
				<div class="review-section">
					<div class="review-list">
						<c:choose>
							<c:when test="${not empty reviews}">
								<c:forEach var="r" items="${reviews}">
									<div class="review-item">
										<div class="review-left">
											<span><strong>평점</strong> <c:out value="${r.rating}" /></span>
											<span><c:out value="${r.review}" /></span>
										</div>
										<div class="review-right">
											<!-- 작성자 표시 -->
											<span><c:out value="${r.userId}" /></span>

											<!-- 로그인 유저가 본인 글이거나, ADMIN 권한이면 수정 버튼 노출 -->
											<c:if
												test="${not empty sessionScope.loginUser 
               and (r.userId eq sessionScope.loginUser.id 
                 or sessionScope.loginUser.role eq 'ADMIN')}">
												<a class="btn-link"
													href="${pageContext.request.contextPath}/review_update.jsp
             ?reviewId=${r.id}
             &storeId=${store.id}
             &page=${curPage}">
													리뷰수정 </a>
											</c:if>
										</div>

									</div>

								</c:forEach>
							</c:when>
							<c:otherwise>
								<div class="review-item">
									<div class="review-left">
										<span>리뷰가 없습니다.</span>
									</div>
									<span>-</span>
								</div>
							</c:otherwise>
						</c:choose>
					</div>
				</div>

				<!-- 하단: 페이지네이션 & 리뷰쓰기 -->
				<div class="bottom-section">
					<div class="pagination">
						<c:set var="curPage" value="${page}" />
						<c:set var="tp" value="${totalPages}" />

						<c:choose>
							<c:when test="${curPage > 1}">
								<a class="pagination-button"
									href="${pageContext.request.contextPath}/storeDetail.jsp?id=${store.id}&page=${curPage - 1}">
									이전 </a>
							</c:when>
							<c:otherwise>
								<span class="pagination-button"
									style="opacity: .5; pointer-events: none;">이전</span>
							</c:otherwise>
						</c:choose>

						<span><c:out value="${curPage}" /> / <c:out value="${tp}" />
							페이지</span>

						<c:choose>
							<c:when test="${curPage < tp}">
								<a class="pagination-button"
									href="${pageContext.request.contextPath}/storeDetail.jsp?id=${store.id}&page=${curPage + 1}">
									다음 </a>
							</c:when>
							<c:otherwise>
								<span class="pagination-button"
									style="opacity: .5; pointer-events: none;">다음</span>
							</c:otherwise>
						</c:choose>
					</div>

					<button class="review-write-button"
						onclick="location.href='<%=ctx%>/review_add.jsp'">리뷰 쓰기</button>
				</div>

			</div>
		</div>
	</div>
</body>
</html>
