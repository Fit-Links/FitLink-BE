CREATE
    DATABASE IF NOT EXISTS fit_link DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;;
ALTER
    DATABASE fit_link CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

USE fit_link;

-- 회원 정보 테이블
CREATE TABLE member
(
    member_id           BIGINT NOT NULL AUTO_INCREMENT,
    trainer_id          BIGINT,
    name                VARCHAR(255),
    birth_date          VARCHAR(10),
    profile_picture_url VARCHAR(255),
    phone_number        VARCHAR(15),
    is_request          BOOLEAN,
    is_connected        BOOLEAN,
    created_at          DATETIME(6),
    updated_at          DATETIME(6),
    PRIMARY KEY (member_id)
);

-- 트레이너 정보 테이블
CREATE TABLE trainer
(
    trainer_id   BIGINT NOT NULL AUTO_INCREMENT,
    name         VARCHAR(255),
    trainer_code VARCHAR(15),
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    PRIMARY KEY (trainer_id)
);

-- 트레이너 연차 정보 테이블
CREATE TABLE day_off
(
    day_off_id   BIGINT NOT NULL AUTO_INCREMENT,
    trainer_id   BIGINT,
    day_off_date DATETIME(6),
    PRIMARY KEY (day_off_id)
);

-- 연동 정보 테이블
CREATE TABLE connecting_info
(
    connecting_info_id BIGINT NOT NULL AUTO_INCREMENT,
    member_id          BIGINT,
    trainer_id         BIGINT,
    status             ENUM ('CONNECT_PROCESSING', 'CONNECT_COMPLETED', 'CONNECT_REFUSED'),
    created_at         DATETIME(6),
    updated_at         DATETIME(6),
    PRIMARY KEY (connecting_info_id)
);

-- 개인 상세 정보 테이블
CREATE TABLE personal_detail
(
    personal_detail_id  BIGINT NOT NULL AUTO_INCREMENT,
    name                VARCHAR(255),
    trainer_id          BIGINT,
    member_id           BIGINT,
    provider_id         VARCHAR(255),
    profile_picture_url VARCHAR(255),
    gender              ENUM ('MALE', 'FEMALE'),
    birth_date          DATETIME(6),
    phone_number        VARCHAR(15),
    email               VARCHAR(100),
    oauth_provider      ENUM ('GOOGLE', 'KAKAO', 'NAVER', 'APPLE'),
    status              ENUM ('NORMAL', 'REQUIRED_SMS','SLEEP', 'SUSPEND', 'DELETE'),
    PRIMARY KEY (personal_detail_id)
);

-- 토큰 정보 테이블
CREATE TABLE token
(
    token_id           BIGINT NOT NULL AUTO_INCREMENT,
    personal_detail_id BIGINT,
    refresh_token      VARCHAR(255),
    created_at         DATETIME(6),
    updated_at         DATETIME(6),
    PRIMARY KEY (token_id)
);

-- 회원 운동 일정 테이블
CREATE TABLE workout_schedule
(
    workout_schedule_id BIGINT NOT NULL AUTO_INCREMENT,
    day_of_week         ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'),
    member_id           BIGINT,
    preference_times    JSON,
    created_at          DATETIME(6),
    updated_at          DATETIME(6),
    PRIMARY KEY (workout_schedule_id)
);

-- 트레이너 일정 정보 테이블
CREATE TABLE available_time
(
    available_time_id BIGINT NOT NULL AUTO_INCREMENT,
    trainer_id        BIGINT,
    is_holiday        BOOLEAN,
    apply_at          DATETIME(6),
    day_of_week       ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'),
    start_time        TIME,
    end_time          TIME,
    created_at        DATETIME(6),
    updated_at        DATETIME(6),
    PRIMARY KEY (available_time_id)
);

-- 세션 정보 테이블
CREATE TABLE session_info
(
    session_info_id BIGINT NOT NULL AUTO_INCREMENT,
    trainer_id      BIGINT,
    member_id       BIGINT,
    total_count     INT,
    remaining_count INT,
    PRIMARY KEY (session_info_id)
);

-- 세션 사용 정보 테이블
CREATE TABLE session
(
    session_id     BIGINT NOT NULL AUTO_INCREMENT,
    reservation_id BIGINT,
    status         ENUM ('SESSION_CANCELLED', 'SESSION_WAITING', 'SESSION_COMPLETED'),
    cancel_reason  VARCHAR(255),
    is_completed   BOOLEAN,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    PRIMARY KEY (session_id)
);

-- 예약 정보 테이블
CREATE TABLE reservation
(
    reservation_id    BIGINT NOT NULL AUTO_INCREMENT,
    member_id         BIGINT,
    trainer_id        BIGINT,
    session_info_id   BIGINT,
    name              VARCHAR(255),
    reservation_dates JSON   NOT NULL,
    change_date       DATETIME(6),
    status            ENUM ('FIXED_RESERVATION','DISABLED_TIME_RESERVATION', 'RESERVATION_WAITING','RESERVATION_APPROVED',
        'RESERVATION_CANCELLED', 'RESERVATION_REFUSED', 'RESERVATION_CHANGE_REQUEST', 'RESERVATION_COMPLETED'),
    cancel_reason     VARCHAR(255),
    is_day_off        BOOLEAN,
    created_at        DATETIME(6),
    updated_at        DATETIME(6),
    PRIMARY KEY (reservation_id)
);

-- 캘린더 테이블
CREATE TABLE calendar
(
    calendar_date DATETIME(6) NOT NULL,
    is_weekend    BOOLEAN,
    is_holiday    BOOLEAN,
    PRIMARY KEY (calendar_date)
);

-- 통합 이력 정보 테이블
CREATE TABLE history
(
    history_id         BIGINT NOT NULL AUTO_INCREMENT,
    ref_id             BIGINT,
    ref_type           ENUM ('RESERVATION', 'SESSION', 'CONNECTING'),
    personal_detail_id BIGINT,
    content            VARCHAR(255),
    created_at         DATETIME(6),
    PRIMARY KEY (history_id)
);

-- 알림 정보 테이블
CREATE TABLE notification
(
    notification_id    BIGINT NOT NULL AUTO_INCREMENT,
    ref_id             BIGINT,
    ref_type           ENUM ('RESERVATION', 'SESSION', 'CONNECTING'),
    personal_detail_id BIGINT,
    name               VARCHAR(255),
    content            VARCHAR(255),
    notification_type  ENUM ('RESERVATION_REQUESTED','RESERVATION_CANCEL_REQUEST','RESERVATION_CHANGE_REQUEST',
        'SESSION_COMPLETED','CONNECT','DISCONNECT','RESERVATION_CHANGE_REQUEST_REFUSED','RESERVATION_APPROVE',
        'RESERVATION_CANCEL','SESSION_DEDUCTED','SESSION_REMINDER','SESSION_CANCEL_REQUEST_APPROVED',
        'SESSION_CANCEL_REQUEST_REFUSED','SESSION_REMAIN_5','SESSION_EDITED'),
    is_sent            BOOLEAN,
    is_processed       BOOLEAN,
    send_date          DATETIME(6),
    PRIMARY KEY (notification_id)
);