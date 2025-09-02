<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>로그인</title>
    <style>
@font-face {
    font-family: 'Paperozi';
    src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/2408-3@1.0/Paperlogy-9Black.woff2') format('woff2');
    font-weight: 900;
    font-display: swap;
}
        body {
            font-family: 'Paperozi';
            background: white;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .login-container {
            background-color: white;
            padding: 40px;
			border:solid 4px;
            width: 350px;
        }
        .login-container h2 {
            text-align: center;
            margin-bottom: 25px;
            color: #333;
        }
        .input-group {
            margin-bottom: 20px;
        }
        .input-group label {
            display: block;
            margin-bottom: 6px;
            color: #555;
            font-weight: bold;
        }
        .input-group input {
            width: 100%;
            padding: 12px;
            border:solid;
            outline: none;
            transition: 0.3s;
        }
        .input-group input:focus {
            border-color: gray;
            box-shadow: 0 0 8px rgba(79, 172, 254, 0.3);
        }
        .login-btn {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, black,gray);
            border: none;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.3s;
        }
        .login-btn:hover {
            background: linear-gradient(135deg, gray,black);
        }
        .links {
            margin-top: 15px;
            text-align: center;
        }
        .links a {
            text-decoration: none;
            color: black;
            margin: 0 5px;
            font-size: 14px;
        }
        .links a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <form action="login.do" method="post" class="login-container">
        <h2>로그인</h2>
        <div class="input-group">
            <label for="id">아이디</label>
            <input type="text" id="id" name="id" placeholder="아이디를 입력하세요" required>
        </div>
        <div class="input-group">
            <label for="pwd">비밀번호</label>
            <input type="password" id="pwd" name="pwd" placeholder="비밀번호를 입력하세요" required>
        </div>
        <button type="submit" class="login-btn">로그인</button>
        <div class="links">
            <a href="join.jsp">회원가입</a>
        </div>
    </form>
</body>
</html>
