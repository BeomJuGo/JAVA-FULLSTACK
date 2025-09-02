<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>로그인</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #4facfe, #00f2fe);
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .login-container {
            background-color: white;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0px 8px 20px rgba(0,0,0,0.15);
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
            border: 1px solid #ddd;
            border-radius: 8px;
            outline: none;
            transition: 0.3s;
        }
        .input-group input:focus {
            border-color: #4facfe;
            box-shadow: 0 0 8px rgba(79, 172, 254, 0.3);
        }
        .login-btn {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #4facfe, #00f2fe);
            border: none;
            border-radius: 8px;
            color: white;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background 0.3s;
        }
        .login-btn:hover {
            background: linear-gradient(135deg, #00c6ff, #0072ff);
        }
        .links {
            margin-top: 15px;
            text-align: center;
        }
        .links a {
            text-decoration: none;
            color: #4facfe;
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
