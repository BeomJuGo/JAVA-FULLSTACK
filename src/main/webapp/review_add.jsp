<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>리뷰 입력 폼</title>
  <style>
    @font-face {
      font-family: 'Paperozi';
      src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/2408-3@1.0/Paperlogy-3Light.woff2') format('woff2');
      font-weight: 300;
      font-display: swap;
    }

    body {
      font-family: 'Paperozi', sans-serif;
      background: #f8f8f8;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      margin: 0;
    }

    .form-container {
      background: white;
      border: 2px solid #ccc;
      padding: 30px 40px;
      max-width: 500px;
      width: 100%;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    }

    h2 {
      text-align: center;
      margin-bottom: 20px;
    }

    form {
      text-align: center;
    }

    form label {
      display: flex;
      justify-content: space-between;
      align-items: center;
      text-align: left;
      margin: 10px 0 5px;
      font-weight: bold;
    }

    label span {
      font-size: 0.8em;
      color: gray;
    }

    .form-container input[type="text"],
    .form-container input[type="button"],
    .form-container textarea,
    p {
      width: 100%;
      box-sizing: border-box;
      padding: 8px;
      margin-top: 5px;
      margin-bottom: 15px;
      font-size: 1rem;
    }

    .star-rating {
      unicode-bidi: bidi-override;
      font-size: 2.5rem;
      display: inline-flex;
      flex-direction: row-reverse;
      justify-content: center;
      margin-top: 10px;
    }

    .star-rating input[type="radio"] {
      display: none;
    }

    .star-rating label {
      color: #ccc;
      cursor: pointer;
      transition: color 0.2s;
    }

    .star-rating input:checked ~ label,
    .star-rating label:hover,
    .star-rating label:hover ~ label {
      color: gold;
    }

    .btn-container {
      margin-top: 20px;
    }

    .btn-submit {
      background-color: #44444E;
      color: white;
      padding: 10px 20px;
      border: none;
      border-radius: 4px;
      font-size: 14px;
      cursor: pointer;
    }

    .btn-submit:hover {
      background-color: #D3DAD9;
    }

    .btn-reset {
      background-color: #7A7A73;
      color: white;
      padding: 10px 20px;
      border: none;
      border-radius: 4px;
      font-size: 14px;
      margin-left: 10px;
      cursor: pointer;
    }

    .btn-reset:hover {
      background-color: #44444E;
    }
  </style>
</head>
<body>
  <div class="form-container">
    <h2>리뷰 입력 폼</h2>
    <form name="reviewInput" action="<%=request.getContextPath()%>/Review" method="post" enctype="multipart/form-data">
      <%
      	String storeName = request.getParameter("storeName");
      	String address = request.getParameter("address");
      	// 불러온 내용이 없을 경우 임의로 지정된 내용이 들어감
      	if (storeName == null || storeName.trim().length() == 0) {
      		storeName = "테스트 식당";
      	}
      	if (address == null || address.trim().length() == 0) {
      		address = "서울시 테스트구 123";
      	}

      %>
      <label>식당명</label>
      <p><%= storeName%></p>
      <input type="hidden" name="storeName" value="<%= storeName %>">

      <label>주소</label>
      <p><%= address%></p>
      <input type="hidden" name="address" value="<%= address %>">

      <label>평점</label>
      <div class="star-rating">
        <input type="radio" id="star5" name="rating" value="5"><label for="star5">★</label>
        <input type="radio" id="star4" name="rating" value="4"><label for="star4">★</label>
        <input type="radio" id="star3" name="rating" value="3"><label for="star3">★</label>
        <input type="radio" id="star2" name="rating" value="2"><label for="star2">★</label>
        <input type="radio" id="star1" name="rating" value="1" required><label for="star1">★</label>
      </div>

      <hr>

      <label>사진 <span>(선택사항)</span></label>
      <table>
      	<tr>
		 <td colspan=2 align=center><input type="file" name="pic"></td>
		</tr>
      </table>

      <label>리뷰 내용 <span>(선택사항)</span></label>
      <textarea name="review" rows="5" required></textarea>

      <div class="btn-container">
        <input type="submit" class="btn-submit" value="리뷰 제출">
        <input type="reset" class="btn-reset" value="다시 작성">
      </div>
    </form>
  </div>
</body>
</html>
