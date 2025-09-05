<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.member.MemberDTO" %>
<%@ page import="com.store.StoreDTO" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>관리자 페이지</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8; margin:0; padding:0; color:#333; }
        .wrap { max-width:1200px; margin:30px auto; padding:20px; background:#fff; box-shadow:0 4px 12px rgba(0,0,0,0.1); border-radius:12px; position:relative; }
        h1 { font-size:2rem; margin-bottom:20px; color:#2c3e50; text-align:center; }
        h2 { font-size:1.4rem; margin:20px 0 10px; border-bottom:2px solid #3498db; padding-bottom:6px; color:#3498db; }
        .top-left-toolbar { position:absolute; top:20px; left:20px; }
        .btn { background-color:#3498db; color:#fff; padding:6px 14px; border:none; border-radius:6px; cursor:pointer; text-decoration:none; font-size:0.9rem; transition:all 0.2s; }
        .btn:hover { background-color:#2980b9; }
        .btn-delete { background-color:#e74c3c; } .btn-delete:hover { background-color:#c0392b; }
        .btn-edit { background-color:#f39c12; } .btn-edit:hover { background-color:#d35400; }
        .btn-save { background-color:#2ecc71; } .btn-save:hover { background-color:#27ae60; }
        .btn-cancel { background-color:#7f8c8d; } .btn-cancel:hover { background-color:#636e72; }
        table { width:100%; border-collapse:collapse; margin-bottom:25px; font-size:0.95rem; }
        th, td { border:1px solid #ddd; padding:12px 10px; text-align:center; }
        th { background-color:#3498db; color:#fff; font-weight:600; }
        tr:nth-child(even) { background-color:#f9f9f9; }
        tr:hover { background-color:#f1f1f1; }
        form { display:inline; }
    </style>
    <script>
        function enableEdit(userId) {
            const row = document.getElementById("row-" + userId);
            row.querySelectorAll(".view").forEach(el => el.style.display = "none");
            row.querySelectorAll(".edit").forEach(el => el.style.display = "inline");

            row.querySelector(".btn-edit").style.display = "none";
            row.querySelector(".btn-delete").style.display = "none";
            row.querySelector(".btn-save").style.display = "inline";
            row.querySelector(".btn-cancel").style.display = "inline";
        }

        function cancelEdit(userId) {
            const row = document.getElementById("row-" + userId);
            row.querySelectorAll(".view").forEach(el => el.style.display = "inline");
            row.querySelectorAll(".edit").forEach(el => el.style.display = "none");

            row.querySelector(".btn-edit").style.display = "inline";
            row.querySelector(".btn-delete").style.display = "inline";
            row.querySelector(".btn-save").style.display = "none";
            row.querySelector(".btn-cancel").style.display = "none";
        }
    </script>
</head>
<body>
<div class="wrap">
    <div class="top-left-toolbar">
        <a href="main.jsp" class="btn">메인으로</a>
    </div>

    <h1>관리자 페이지</h1>

    <!-- ====== 회원 등록 ====== -->
    <section>
        <h2>회원 등록</h2>
        <form method="post" action="admin.do">
            <input type="hidden" name="action" value="createUser"/>
            ID: <input type="text" name="userId" required />
            이름: <input type="text" name="userName" required />
            비밀번호: <input type="password" name="userPwd" required />
            권한: <input type="number" name="userLevel" value="1" min="1" max="4" required />
            <button type="submit" class="btn">등록</button>
        </form>
    </section>

    <!-- ====== 회원 목록 ====== -->
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
            %>
                <tr id="row-<%=u.getId()%>">
                    <form method="post" action="admin.do">
                        <input type="hidden" name="userId" value="<%=u.getId()%>">
                        <input type="hidden" name="action" value="editUser">
                        <td>
                            <span class="view"><%=u.getId()%></span>
                        </td>
                        <td>
                            <span class="view"><%=u.getUser_name()%></span>
                            <input class="edit" type="text" name="userName" value="<%=u.getUser_name()%>" style="display:none;">
                        </td>
                        <td>
                            <span class="view">******</span>
                            <input class="edit" type="password" name="userPwd" value="<%=u.getPwd()%>" style="display:none;">
                        </td>
                        <td>
                            <span class="view"><%=u.getUser_level()%></span>
                            <input class="edit" type="number" name="userLevel" value="<%=u.getUser_level()%>" min="1" max="4" style="display:none;">
                        </td>
                        <td>
                            <button type="button" class="btn btn-edit" onclick="enableEdit('<%=u.getId()%>')">수정</button>
                            <button type="submit" class="btn btn-save" style="display:none;">저장</button>
                            <button type="button" class="btn btn-cancel" style="display:none;" onclick="cancelEdit('<%=u.getId()%>')">취소</button>
                    </form>
                    <form method="post" action="admin.do" style="display:inline;">
                        <input type="hidden" name="action" value="deleteUser"/>
                        <input type="hidden" name="userId" value="<%=u.getId()%>"/>
                        <button type="submit" class="btn btn-delete">삭제</button>
                    </form>
                        </td>
                </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </section>

    <!-- ====== 가게 목록 ====== -->
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
            %>
                <tr>
                    <td><%=s.getName()%></td>
                    <td><%=s.getAdress()%></td>
                    <td><%=s.getRating()%></td>
                    <td><%=s.getCategory()%></td>
                    <td>
                        <form method="post" action="admin.do">
                            <input type="hidden" name="action" value="deleteStore"/>
                            <input type="hidden" name="storeId" value="<%=s.getName()%>"/>
                            <button type="submit" class="btn btn-delete">삭제</button>
                        </form>
                    </td>
                </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </section>

</div>
</body>
</html>
