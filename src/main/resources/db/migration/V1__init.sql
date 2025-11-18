USE healthcare;

-- 타임존 Asia/Seoul 고정 (세션/글로벌은 운영 환경에서 설정 권장)
SET time_zone = '+09:00';

-- 1) 공통: 계정/프로필
CREATE TABLE account (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  username     VARCHAR(100) NOT NULL UNIQUE,
  password     VARCHAR(255) NOT NULL,
  role         ENUM('USER','TRAINER','ADMIN') NOT NULL,
  status       ENUM('ACTIVE','SUSPENDED','WITHDRAWN') NOT NULL DEFAULT 'ACTIVE',
  display_name VARCHAR(100) NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE user_profile (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id    BIGINT NOT NULL UNIQUE,
  gender        ENUM('M','F','O') NULL,
  height_cm     DECIMAL(5,2) NULL,
  weight_kg     DECIMAL(5,2) NULL,
  age           INT NULL,
  activity_level ENUM('LOW','MID','HIGH') NULL, -- 운동강도
  intro         VARCHAR(500),
  image_url     VARCHAR(500),          -- Cloudinary 공개 URL(옵션)
  image_public_id VARCHAR(255),        -- Cloudinary public_id(권장)
  is_public     TINYINT(1) NOT NULL DEFAULT 1, -- 공개/비공개
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_profile_account FOREIGN KEY (account_id) REFERENCES account(id)
) ENGINE=InnoDB;

CREATE TABLE trainer_profile (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  account_id      BIGINT NOT NULL UNIQUE,
  gender          ENUM('M','F','O') NULL,
  height_cm       DECIMAL(5,2) NULL,
  weight_kg       DECIMAL(5,2) NULL,
  age             INT NULL,
  career_years    INT DEFAULT 0,
  specialty       VARCHAR(200),
  intro           VARCHAR(500),
  image_url       VARCHAR(500),
  image_public_id VARCHAR(255),
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_trainer_profile_account FOREIGN KEY (account_id) REFERENCES account(id)
) ENGINE=InnoDB;

CREATE INDEX idx_user_public ON user_profile(is_public);
CREATE INDEX idx_trainer_specialty ON trainer_profile(specialty);

-- 2) 매칭 (요청→동의→진행→종료) + 차단/신고/강제종료
CREATE TABLE `match` (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT NOT NULL,     -- user_profile.id
  trainer_id    BIGINT NOT NULL,     -- trainer_profile.id
  status        ENUM('REQUESTED','ACCEPTED','IN_PROGRESS','ENDED','REJECTED','FORCE_ENDED') NOT NULL DEFAULT 'REQUESTED',
  requested_by  ENUM('USER','TRAINER') NOT NULL,
  requested_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  accepted_at   TIMESTAMP NULL,
  ended_at      TIMESTAMP NULL,
  end_reason    VARCHAR(500) NULL,   -- 종료/강제종료 사유
  is_blocked    TINYINT(1) NOT NULL DEFAULT 0,
  block_reason  VARCHAR(500) NULL,
  is_reported   TINYINT(1) NOT NULL DEFAULT 0,
  report_reason VARCHAR(500) NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_match_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
  CONSTRAINT fk_match_trainer FOREIGN KEY (trainer_id) REFERENCES trainer_profile(id),
  CONSTRAINT uc_match_unique UNIQUE (user_id, trainer_id, status)
) ENGINE=InnoDB;

CREATE INDEX idx_match_user ON `match`(user_id, status);
CREATE INDEX idx_match_trainer ON `match`(trainer_id, status);

-- 3) 채팅 (매칭별 1 스레드, 메시지 분리)
CREATE TABLE chat_thread (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  match_id   BIGINT NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_chat_thread_match FOREIGN KEY (match_id) REFERENCES `match`(id)
) ENGINE=InnoDB;

CREATE TABLE chat_message (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  thread_id    BIGINT NOT NULL,
  sender_acc   BIGINT NOT NULL,  -- account.id
  content      TEXT NOT NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_hidden    TINYINT(1) NOT NULL DEFAULT 0,   -- 모더레이션 숨김
  CONSTRAINT fk_chat_msg_thread FOREIGN KEY (thread_id) REFERENCES chat_thread(id),
  CONSTRAINT fk_chat_msg_sender FOREIGN KEY (sender_acc) REFERENCES account(id)
) ENGINE=InnoDB;

CREATE INDEX idx_chat_message_thread_time ON chat_message(thread_id, created_at);

-- 4) 플랜 (주 단위 → 일 → 여러 개 항목)
CREATE TABLE plan_week (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  match_id     BIGINT NOT NULL,
  week_start   DATE NOT NULL,   -- 월요일 등 프론트 규칙
  title        VARCHAR(200) NOT NULL,
  note         VARCHAR(1000),
  created_by   BIGINT NOT NULL, -- account.id (주로 트레이너)
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_plan_week_match FOREIGN KEY (match_id) REFERENCES `match`(id),
  CONSTRAINT fk_plan_week_creator FOREIGN KEY (created_by) REFERENCES account(id),
  CONSTRAINT uc_plan_week UNIQUE (match_id, week_start)
) ENGINE=InnoDB;

CREATE TABLE plan_day (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  week_id    BIGINT NOT NULL,
  day_index  TINYINT NOT NULL,  -- 0~6
  note       VARCHAR(1000),
  CONSTRAINT fk_plan_day_week FOREIGN KEY (week_id) REFERENCES plan_week(id),
  CONSTRAINT uc_plan_day UNIQUE (week_id, day_index)
) ENGINE=InnoDB;

CREATE TABLE plan_item (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  day_id         BIGINT NOT NULL,
  item_type      ENUM('DIET','WORKOUT') NOT NULL,
  title          VARCHAR(200) NOT NULL,
  description    VARCHAR(1000),
  target_kcal    INT NULL,       -- 식단일 때 사용 가능
  target_min     INT NULL,       -- 운동일 때 사용 가능
  status_mark    ENUM('O','D','X') NOT NULL DEFAULT 'X', -- O/△/X (△는 DB에서 D로 표기)
  completed_at   TIMESTAMP NULL,
  locked         TINYINT(1) NOT NULL DEFAULT 0, -- 완료 후 수정 제한 시 플래그
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_plan_item_day FOREIGN KEY (day_id) REFERENCES plan_day(id)
) ENGINE=InnoDB;

CREATE INDEX idx_plan_item_day ON plan_item(day_id);
CREATE INDEX idx_plan_item_status ON plan_item(status_mark);

-- 5) 커뮤니티 (게시글/미디어/해시태그/좋아요/조회/댓글 + 모더레이션)
CREATE TABLE post (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_acc   BIGINT NOT NULL,
  title        VARCHAR(200) NOT NULL,
  content      TEXT NOT NULL,
  is_hidden    TINYINT(1) NOT NULL DEFAULT 0,       -- 숨김(모더레이션)
  hidden_reason VARCHAR(500) NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_author FOREIGN KEY (author_acc) REFERENCES account(id)
) ENGINE=InnoDB;

CREATE TABLE post_media (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id        BIGINT NOT NULL,
  media_type     ENUM('IMAGE','VIDEO') NOT NULL,
  url            VARCHAR(500),         -- 접근 URL(옵션)
  public_id      VARCHAR(255) NOT NULL, -- Cloudinary public_id (필수)
  width          INT NULL,
  height         INT NULL,
  bytes          BIGINT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_post_media_post FOREIGN KEY (post_id) REFERENCES post(id)
) ENGINE=InnoDB;

CREATE INDEX idx_post_media_post ON post_media(post_id);

CREATE TABLE hashtag (
  id        BIGINT PRIMARY KEY AUTO_INCREMENT,
  tag       VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE post_hashtag (
  post_id   BIGINT NOT NULL,
  hashtag_id BIGINT NOT NULL,
  PRIMARY KEY (post_id, hashtag_id),
  CONSTRAINT fk_post_hashtag_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_post_hashtag_tag FOREIGN KEY (hashtag_id) REFERENCES hashtag(id)
) ENGINE=InnoDB;

CREATE TABLE post_like (
  post_id    BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (post_id, account_id),
  CONSTRAINT fk_post_like_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_post_like_acc FOREIGN KEY (account_id) REFERENCES account(id)
) ENGINE=InnoDB;

-- 유니크 조회수(계정/아이피/날짜 단위 중 택1) 여기선 계정+일 단위 예시
CREATE TABLE post_view (
  post_id    BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  view_date  DATE NOT NULL,
  cnt        INT NOT NULL DEFAULT 1,
  PRIMARY KEY (post_id, account_id, view_date),
  CONSTRAINT fk_post_view_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_post_view_acc FOREIGN KEY (account_id) REFERENCES account(id)
) ENGINE=InnoDB;

CREATE TABLE comment (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id      BIGINT NOT NULL,
  author_acc   BIGINT NOT NULL,
  content      VARCHAR(1000) NOT NULL,
  is_hidden    TINYINT(1) NOT NULL DEFAULT 0,
  hidden_reason VARCHAR(500) NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post(id),
  CONSTRAINT fk_comment_author FOREIGN KEY (author_acc) REFERENCES account(id)
) ENGINE=InnoDB;

-- 신고(포스트/댓글/채팅메시지 공통 처리용)
CREATE TABLE report (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  target_type   ENUM('POST','COMMENT','CHAT_MESSAGE') NOT NULL,
  target_id     BIGINT NOT NULL,
  reporter_acc  BIGINT NOT NULL,
  reason        VARCHAR(500) NOT NULL,
  status        ENUM('PENDING','REVIEWING','RESOLVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  resolution    VARCHAR(500) NULL,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_report_reporter FOREIGN KEY (reporter_acc) REFERENCES account(id),
  INDEX idx_report_target (target_type, target_id)
) ENGINE=InnoDB;

-- 6) 리뷰 (매칭 1건당 1개, 평점 소수 허용, 익명/수정기간)
CREATE TABLE review (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  match_id       BIGINT NOT NULL UNIQUE,  -- 매칭 당 1개
  user_id        BIGINT NOT NULL,         -- 작성자: user_profile.id
  trainer_id     BIGINT NOT NULL,         -- 대상: trainer_profile.id
  rating         DECIMAL(2,1) NOT NULL,   -- 1.0 ~ 5.0
  content        VARCHAR(2000) NOT NULL,
  is_anonymous   TINYINT(1) NOT NULL DEFAULT 0,
  editable_until TIMESTAMP NULL,          -- 생성 시 +30일 저장
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_review_match FOREIGN KEY (match_id) REFERENCES `match`(id),
  CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
  CONSTRAINT fk_review_trainer FOREIGN KEY (trainer_id) REFERENCES trainer_profile(id),
  CONSTRAINT chk_review_rating CHECK (rating >= 1.0 AND rating <= 5.0)
) ENGINE=InnoDB;

CREATE INDEX idx_review_trainer ON review(trainer_id, rating);

-- 7) 관리자 로깅
CREATE TABLE admin_action_log (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_acc    BIGINT NOT NULL,               -- account(ADMIN)
  action_type  ENUM('HIDE_POST','UNHIDE_POST','HIDE_COMMENT','UNHIDE_COMMENT','SUSPEND_ACCOUNT','RESTORE_ACCOUNT','FORCE_END_MATCH') NOT NULL,
  target_type  ENUM('ACCOUNT','POST','COMMENT','MATCH') NOT NULL,
  target_id    BIGINT NOT NULL,
  reason       VARCHAR(500) NULL,
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_admin_action_admin FOREIGN KEY (admin_acc) REFERENCES account(id),
  INDEX idx_admin_action_target (target_type, target_id)
) ENGINE=InnoDB;

-- 8) 편의 인덱스
CREATE INDEX idx_post_created ON post(created_at);
CREATE INDEX idx_comment_post_created ON comment(post_id, created_at);
CREATE INDEX idx_chat_message_sender ON chat_message(sender_acc, created_at);
