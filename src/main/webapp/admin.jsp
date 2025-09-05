<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.member.MemberDTO" %>
<%@ page import="com.store.StoreDTO" %>

<%
    String ctx = request.getContextPath();
    MemberDTO loginUser = (MemberDTO) session.getAttribute("loginUser");
    boolean isAdmin = (loginUser != null && loginUser.getUser_level() == 4);

    // ✅ 관리자만 접근 허용
    if (!isAdmin) {
        response.sendRedirect(ctx + "/main.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <title>관리자 페이지</title>
  <style>
    body{font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;background:#f4f6f8;margin:0;color:#333}
    .wrap{max-width:1200px;margin:30px auto;padding:20px;background:#fff;box-shadow:0 4px 12px rgba(0,0,0,.1);border-radius:12px;position:relative}
    h1{font-size:2rem;margin:0 0 20px;color:#2c3e50;text-align:center}
    h2{font-size:1.25rem;margin:20px 0 10px;border-bottom:2px solid #3498db;padding-bottom:6px;color:#3498db}
    .top-left-toolbar{position:absolute;top:20px;left:20px}
    .btn{background:#3498db;color:#fff;padding:6px 14px;border:none;border-radius:6px;cursor:pointer;text-decoration:none;font-size:.9rem;transition:all .2s}
    .btn:hover{background:#2980b9}
    .btn-delete{background:#e74c3c}.btn-delete:hover{background:#c0392b}
    .btn-edit{background:#f39c12}.btn-edit:hover{background:#d35400}
    .btn-save{background:#2ecc71}.btn-save:hover{background:#27ae60}
    .btn-cancel{background:#7f8c8d}.btn-cancel:hover{background:#636e72}
    table{width:100%;border-collapse:collapse;margin-bottom:24px;font-size:.95rem}
    th,td{border:1px solid #ddd;padding:10px;text-align:center}
    th{background:#3498db;color:#fff;font-weight:600}
    tr:nth-child(even){background:#f9f9f9}
    tr:hover{background:#f1f1f1}
    form{display:inline}
    .hidden{display:none}
    .mt-2{margin-top:8px}
    .mb-2{margin-bottom:8px}
  </style>
  <script>
    function enableEdit(rowId){
      const row = document.getElementById(rowId);
      row.querySelectorAll('.view').forEach(el=>el.classList.add('hidden'));
      row.querySelectorAll('.edit').forEach(el=>el.classList.remove('hidden'));
      const be = row.querySelector('.btn-edit'); if(be) be.classList.add('hidden');
      const bs = row.querySelector('.btn-save'); if(bs) bs.classList.remove('hidden');
      const bc = row.querySelector('.btn-cancel'); if(bc) bc.classList.remove('hidden');
      const bd = row.querySelector('.btn-delete'); if(bd) bd.classList.add('hidden');
    }
    function cancelEdit(rowId){
      const row = document.getElementById(rowId);
      row.querySelectorAll('.view').forEach(el=>el.classList.remove('hidden'));
      row.querySelectorAll('.edit').forEach(el=>el.classList.add('hidden'));
      const be = row.querySelector('.btn-edit'); if(be) be.classList.remove('hidden');
      const bs = row.querySelector('.btn-save'); if(bs) bs.classList.add('hidden');
      const bc = row.querySelector('.btn-cancel'); if(bc) bc.classList.add('hidden');
      const bd = row.querySelector('.btn-delete'); if(bd) bd.classList.remove('hidden');
      // 비밀번호 입력칸 초기화
      const pwd = row.querySelector('input[name="userPwd"]');
      if(pwd) pwd.value = '';
    }
    function confirmDelete(msg){
      return confirm(msg || '정말 삭제하시겠습니까?');
    }
  </script>
</head>
<body>
<div class="wrap">
  <div class="top-left-toolbar">
    <a href="<%=ctx%>/main.jsp" class="btn">메인으로</a>
  </div>

  <h1>관리자 페이지</h1>

  <!-- ========== 회원 등록 ========= -->
  <section>
    <h2>회원 등록</h2>
    <form method="post" action="<%=ctx%>/admin.do" class="mb-2">
      <input type="hidden" name="action" value="createUser"/>
      ID: <input type="text" name="userId" required />
      이름: <input type="text" name="userName" required />
      비밀번호: <input type="password" name="userPwd" required />
      권한: <input type="number" name="userLevel" value="1" min="1" max="4" required />
      <button type="submit" class="btn">등록</button>
    </form>
  </section>

  <!-- ========== 회원 목록 ========= -->
  <section>
    <h2>회원 목록</h2>
    <table>
      <thead>
      <tr>
        <th>ID</th><th>이름</th><th>비밀번호</th><th>권한</th><th>관리</th>
      </tr>
      </thead>
      <tbody>
      <%
        List<MemberDTO> users = (List<MemberDTO>) request.getAttribute("users");
        if (users != null) {
          for (MemberDTO u : users) {
            String uid = u.getId();
      %>
      <tr id="user-<%=uid%>">
        <!-- 수정 폼 -->
        <form method="post" action="<%=ctx%>/admin.do">
          <input type="hidden" name="action" value="editUser"/>
          <input type="hidden" name="userId" value="<%=uid%>"/>

          <td><span class="view"><%=uid%></span></td>
          <td>
            <span class="view"><%=u.getUser_name()%></span>
            <input class="edit hidden" type="text" name="userName" value="<%=u.getUser_name()%>"/>
          </td>
          <td>
            <span class="view">******</span>
            <!-- 값 비워두면 서버에서 비번 미변경 처리 권장 -->
            <input class="edit hidden" type="password" name="userPwd" placeholder="변경 시에만 입력"/>
          </td>
          <td>
            <span class="view"><%=u.getUser_level()%></span>
            <input class="edit hidden" type="number" name="userLevel" value="<%=u.getUser_level()%>" min="1" max="4"/>
          </td>
          <td>
            <button type="button" class="btn btn-edit" onclick="enableEdit('user-<%=uid%>')">수정</button>
            <button type="submit" class="btn btn-save hidden">저장</button>
            <button type="button" class="btn btn-cancel hidden" onclick="cancelEdit('user-<%=uid%>')">취소</button>
        </form>
        <form method="post" action="<%=ctx%>/admin.do" style="display:inline" onsubmit="return confirmDelete('해당 사용자를 삭제하시겠습니까?');">
          <input type="hidden" name="action" value="deleteUser"/>
          <input type="hidden" name="userId" value="<%=uid%>"/>
          <button type="submit" class="btn btn-delete">삭제</button>
        </form>
          </td>
      </tr>
      <%
          } // for
        } // if
      %>
      </tbody>
    </table>
  </section>

  <!-- ========== 가게 등록 ========= -->
  <section>
    <h2>가게 등록</h2>
    <form method="post" action="<%=ctx%>/admin.do" class="mb-2">
      <input type="hidden" name="action" value="createStore"/>
      이름: <input type="text" name="name" required />
      주소: <input type="text" name="address" required />
      카테고리:
      <select name="category" required>
        <option value="일식">일식</option>
        <option value="중식">중식</option>
        <option value="양식">양식</option>
        <option value="한식">한식</option>
      </select>
      <button type="submit" class="btn">가게 등록</button>
    </form>
  </section>

  <!-- ========== 가게 목록 ========= -->
  <section>
    <h2>가게 목록</h2>
    <table>
      <thead>
      <tr>
        <th>이름</th><th>주소</th><th>평점</th><th>카테고리</th><th>관리</th>
      </tr>
      </thead>
      <tbody>
      <%
        List<StoreDTO> stores = (List<StoreDTO>) request.getAttribute("stores");
        if (stores != null) {
          for (StoreDTO s : stores) {
            String storeKey = s.getName();
      %>
      <tr id="store-<%=storeKey%>">
        <!-- 수정 폼 -->
        <form method="post" action="<%=ctx%>/admin.do">
          <input type="hidden" name="action" value="editStore"/>
          <input type="hidden" name="originalName" value="<%=storeKey%>"/>

          <td>
            <span class="view"><%=s.getName()%></span>
            <input class="edit hidden" type="text" name="name" value="<%=s.getName()%>" required />
          </td>
          <td>
            <span class="view"><%=s.getAddress()%></span>
            <input class="edit hidden" type="text" name="address" value="<%=s.getAddress()%>" required />
          </td>
          <td>
            <!-- ✅ RATING은 읽기 전용 -->
            <span class="view"><%=s.getRating()%></span>
            <span class="edit hidden" style="opacity:.6;">(수정 불가)</span>
          </td>
          <td>
            <span class="view"><%= s.getCategory() %></span>
            <select class="edit hidden" name="category" required>
              <option value="일식" <%= "일식".equals(s.getCategory()) ? "selected" : "" %>>일식</option>
              <option value="중식" <%= "중식".equals(s.getCategory()) ? "selected" : "" %>>중식</option>
              <option value="양식" <%= "양식".equals(s.getCategory()) ? "selected" : "" %>>양식</option>
              <option value="한식" <%= "한식".equals(s.getCategory()) ? "selected" : "" %>>한식</option>
            </select>
          </td>
          <td>
            <button type="button" class="btn btn-edit" onclick="enableEdit('store-<%=storeKey%>')">수정</button>
            <button type="submit" class="btn btn-save hidden">저장</button>
            <button type="button" class="btn btn-cancel hidden" onclick="cancelEdit('store-<%=storeKey%>')">취소</button>
        </form>
        <form method="post" action="<%=ctx%>/admin.do" style="display:inline" onsubmit="return confirmDelete('해당 가게를 삭제하시겠습니까?');">
          <input type="hidden" name="action" value="deleteStore"/>
          <input type="hidden" name="storeId" value="<%=storeKey%>"/>
          <button type="submit" class="btn btn-delete">삭제</button>
        </form>
          </td>
      </tr>
      <%
          } // for
        } // if
      %>
      </tbody>
    </table>
  </section>

</div>
</body>
</html>
