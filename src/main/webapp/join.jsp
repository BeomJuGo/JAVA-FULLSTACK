<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>회원가입</title>
<style>
@font-face {
	font-family: 'Paperozi';
	src:
		url('https://cdn.jsdelivr.net/gh/projectnoonnu/2408-3@1.0/Paperlogy-9Black.woff2')
		format('woff2');
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
	margin: 0
}

.card {
	background: #fff;
	padding: 40px;
	border:solid 4px;
	width: 380px;
	max-width: 92%
}

h2 {
	margin: 0 0 22px;
	text-align: center;
	color: #333
}

.group {
	margin-bottom: 16px
}

.group label {
	display: block;
	margin-bottom: 6px;
	color: #555;
	font-weight: 600
}

.group input {
	width: 100%;
	padding: 12px;
	border: solid ;
	outline: 0;
	transition: .25s
}

.group input:focus {
	border-color: gray;
	box-shadow: 0 0 8px rgba(79, 172, 254, .3)
}

.row {
	display: flex;
	gap: 10px
}

.row .group {
	flex: 1;
	margin-bottom: 0
}

.btn {
	width: 100%;
	padding: 12px;
	border: 0;
	background: linear-gradient(135deg, black,gray);
	color: #fff;
	font-size: 16px;
	font-weight: 700;
	cursor: pointer
}

.btn:hover {
	background: linear-gradient(135deg, gray,black);
}

.links {
	text-align: center;
	margin-top: 12px
}

.links a {
	color: black;
	text-decoration: none
}

.links a:hover {
	text-decoration: underline
}

.msg {
	color: #e54848;
	font-size: 13px;
	margin-top: 6px;
	display: none
}
</style>
<script>
	function validateJoin(e) {
		const id = document.getElementById('id').value.trim();
		const name = document.getElementById('name').value.trim();
		const email = document.getElementById('email').value.trim();
		const pwd = document.getElementById('pwd').value;
		const pwd2 = document.getElementById('pwd2').value;
		const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
		let ok = true, msg = "";

		if (id.length < 4) {
			ok = false;
			msg = "아이디는 4자 이상이어야 합니다.";
		} else if (name.length < 2) {
			ok = false;
			msg = "이름을 2자 이상 입력해 주세요.";
		} else if (!emailOk) {
			ok = false;
			msg = "올바른 이메일 형식이 아닙니다.";
		} else if (pwd.length < 6) {
			ok = false;
			msg = "비밀번호는 6자 이상이어야 합니다.";
		} else if (pwd !== pwd2) {
			ok = false;
			msg = "비밀번호가 서로 일치하지 않습니다.";
		}

		const m = document.getElementById('msg');
		if (!ok) {
			e.preventDefault();
			m.textContent = msg;
			m.style.display = 'block';
		} else {
			m.style.display = 'none';
		}
	}
</script>
</head>
<body>
	<form action="join.do" method="post" class="card"
		onsubmit="validateJoin(event)">
		<h2>회원가입</h2>

		<div class="group">
			<label for="id">아이디</label> <input type="text" id="id" name="id"
				placeholder="영문/숫자 4자 이상" required>
		</div>

		<div class="group">
			<label for="name">이름</label> <input type="text" id="name" name="name"
				placeholder="이름" required>
		</div>

		<div class="group">
			<label for="email">이메일</label> <input type="email" id="email"
				name="email" placeholder="example@mail.com" required>
		</div>

		<div class="row">
			<div class="group">
				<label for="pwd">비밀번호</label> <input type="password" id="pwd"
					name="pwd" placeholder="6자 이상" required>
			</div>
			<div class="group">
				<label for="pwd2">비밀번호 확인</label> <input type="password" id="pwd2"
					name="pwd2" placeholder="비밀번호 재입력" required>
			</div>
		</div>

		<div class="group">
			<label for="phone">휴대폰 (선택)</label> <input type="text" id="phone"
				name="phone" placeholder="010-0000-0000">
		</div>

		<div id="msg" class="msg"></div>

		<button type="submit" class="btn">가입하기</button>
		<div class="links">
			<a href="login.jsp">로그인으로 돌아가기</a>
		</div>
	</form>
</body>
</html>
