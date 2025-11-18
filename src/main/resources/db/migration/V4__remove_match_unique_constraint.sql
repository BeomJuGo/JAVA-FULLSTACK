-- V4__remove_match_unique_constraint.sql
-- match 테이블의 uc_match_unique 제약 조건 제거
-- 같은 사용자와 트레이너가 여러 번 매칭할 수 있도록 허용

ALTER TABLE `match` DROP INDEX uc_match_unique;

