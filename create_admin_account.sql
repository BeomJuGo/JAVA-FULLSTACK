-- 관리자 계정 생성 SQL 스크립트
-- 사용법: mysql -u root -p healthcare < create_admin_account.sql

USE healthcare;

-- 기존 관리자 계정이 있으면 삭제 (선택사항)
-- DELETE FROM account WHERE username = 'admin';

-- 관리자 계정 생성
-- 비밀번호: admin123
-- BCrypt 해시: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO account (username, password, role, status, display_name, created_at, updated_at)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'ACTIVE',
    '관리자',
    NOW(),
    NOW()
);

-- 생성 확인
SELECT id, username, role, status, display_name, created_at 
FROM account 
WHERE username = 'admin';

