CREATE TABLE answer_image
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    inquiry_id BIGINT                NOT NULL,
    image_url  VARCHAR(1000)         NOT NULL,
    sort_order INT                   NOT NULL,
    CONSTRAINT pk_answerimage PRIMARY KEY (id)
);

CREATE TABLE attendance
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime              NOT NULL,
    updated_at      datetime              NOT NULL,
    member_id       BIGINT                NOT NULL,
    attendance_date date                  NOT NULL,
    CONSTRAINT pk_attendance PRIMARY KEY (id)
);

CREATE TABLE attendance_streak
(
    id                  BIGINT AUTO_INCREMENT NOT NULL,
    created_at          datetime              NOT NULL,
    updated_at          datetime              NOT NULL,
    member_id           BIGINT                NOT NULL,
    current_streak      INT                   NOT NULL,
    last_attended_date  date                  NULL,
    last_completed_date date                  NULL,
    CONSTRAINT pk_attendance_streak PRIMARY KEY (id)
);

CREATE TABLE goal_result
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime              NOT NULL,
    updated_at     datetime              NOT NULL,
    member_id      BIGINT                NOT NULL,
    goal_type      VARCHAR(20)           NOT NULL,
    goal_date      date                  NOT NULL,
    target_count   INT                   NOT NULL,
    achieved_count INT                   NOT NULL,
    goal_status    VARCHAR(20)           NOT NULL,
    popup_shown_at datetime              NULL,
    CONSTRAINT pk_goal_result PRIMARY KEY (id)
);

CREATE TABLE inquiry
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime              NOT NULL,
    updated_at  datetime              NOT NULL,
    member_id   BIGINT                NOT NULL,
    title       VARCHAR(255)          NOT NULL,
    content     TEXT                  NOT NULL,
    status      VARCHAR(255)          NOT NULL,
    answer      TEXT                  NULL,
    answered_at datetime              NULL,
    is_secret   BIT(1)                NOT NULL,
    category    VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_inquiry PRIMARY KEY (id)
);

CREATE TABLE inquiry_image
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    inquiry_id BIGINT                NOT NULL,
    image_url  VARCHAR(1000)         NOT NULL,
    sort_order INT                   NOT NULL,
    CONSTRAINT pk_inquiryimage PRIMARY KEY (id)
);

CREATE TABLE interest
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    name       VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_interest PRIMARY KEY (id)
);

CREATE TABLE item
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    price      INT                   NOT NULL,
    type       VARCHAR(255)          NOT NULL,
    name       VARCHAR(255)          NOT NULL,
    image_url  VARCHAR(1000)         NOT NULL,
    is_on_sale BIT(1)                NOT NULL,
    CONSTRAINT pk_item PRIMARY KEY (id)
);

CREATE TABLE keyword
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime              NOT NULL,
    updated_at   datetime              NOT NULL,
    name         VARCHAR(255)          NOT NULL,
    keyword_type VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_keyword PRIMARY KEY (id)
);

CREATE TABLE member
(
    id                     BIGINT AUTO_INCREMENT NOT NULL,
    created_at             datetime              NOT NULL,
    updated_at             datetime              NOT NULL,
    email                  VARCHAR(100)          NULL,
    password               VARCHAR(255)          NULL,
    name                   VARCHAR(50)           NOT NULL,
    provider               VARCHAR(255)          NOT NULL,
    provider_id            VARCHAR(255)          NULL,
    `role`                 VARCHAR(255)          NOT NULL,
    status                 VARCHAR(255)          NOT NULL,
    deleted_at             datetime              NULL,
    daily_goal             INT                   NULL,
    weekly_goal            INT                   NULL,
    goal_retention         VARCHAR(255)          NULL,
    goal_end_date          date                  NULL,
    pending_daily_goal     INT                   NULL,
    pending_goal_retention VARCHAR(255)          NULL,
    pending_apply_date     date                  NULL,
    phone_number           VARCHAR(255)          NULL,
    gender                 VARCHAR(255)          NULL,
    birthday               date                  NULL,
    profile_image          INT                   NOT NULL,
    onboarding_completed   BIT(1)                NOT NULL,
    policy_agreed          BIT(1)                NOT NULL,
    CONSTRAINT pk_member PRIMARY KEY (id)
);

CREATE TABLE member_item
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime              NOT NULL,
    updated_at  datetime              NOT NULL,
    member_id   BIGINT                NOT NULL,
    item_id     BIGINT                NOT NULL,
    is_equipped BIT(1)                NOT NULL,
    CONSTRAINT pk_memberitem PRIMARY KEY (id)
);

CREATE TABLE member_mission
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_at         datetime              NOT NULL,
    updated_at         datetime              NOT NULL,
    member_id          BIGINT                NULL,
    mission_id         BIGINT                NULL,
    mission_status     VARCHAR(255)          NOT NULL,
    attempt_count      INT                   NOT NULL,
    reward_at          datetime              NULL,
    is_content_watched BIT(1)                NOT NULL,
    CONSTRAINT pk_member_mission PRIMARY KEY (id)
);

CREATE TABLE member_policy
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    member_id  BIGINT                NOT NULL,
    policy_id  BIGINT                NOT NULL,
    is_agreed  BIT(1)                NOT NULL,
    agreed_at  datetime              NULL,
    CONSTRAINT pk_memberpolicy PRIMARY KEY (id)
);

CREATE TABLE member_setting
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    member_id     BIGINT                NOT NULL,
    setting_key   VARCHAR(50)           NOT NULL,
    setting_value VARCHAR(20)           NOT NULL,
    created_at    datetime              NOT NULL,
    updated_at    datetime              NOT NULL,
    CONSTRAINT pk_member_setting PRIMARY KEY (id)
);

CREATE TABLE member_sub_interest
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime              NOT NULL,
    updated_at      datetime              NOT NULL,
    member_id       BIGINT                NOT NULL,
    sub_interest_id BIGINT                NOT NULL,
    CONSTRAINT pk_membersubinterest PRIMARY KEY (id)
);

CREATE TABLE mission
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime              NOT NULL,
    updated_at       datetime              NOT NULL,
    title            VARCHAR(255)          NOT NULL,
    `description`    TEXT                  NULL,
    video_url        VARCHAR(255)          NOT NULL,
    reward           INT                   NOT NULL,
    video_length     INT                   NOT NULL,
    duration_minutes INT                   NOT NULL,
    CONSTRAINT pk_mission PRIMARY KEY (id)
);

CREATE TABLE mission_keyword
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    mission_id BIGINT                NULL,
    keyword_id BIGINT                NULL,
    CONSTRAINT pk_missionkeyword PRIMARY KEY (id)
);

CREATE TABLE mission_sub_interest
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime              NOT NULL,
    updated_at      datetime              NOT NULL,
    sub_interest_id BIGINT                NOT NULL,
    mission_id      BIGINT                NOT NULL,
    CONSTRAINT pk_mission_sub_interest PRIMARY KEY (id)
);

CREATE TABLE policy
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime              NOT NULL,
    updated_at   datetime              NOT NULL,
    policy_type  VARCHAR(255)          NOT NULL,
    is_mandatory BIT(1)                NOT NULL,
    version      VARCHAR(255)          NOT NULL,
    title        VARCHAR(255)          NOT NULL,
    content      LONGTEXT              NOT NULL,
    effective_at datetime              NOT NULL,
    is_active    BIT(1)                NOT NULL,
    CONSTRAINT pk_policy PRIMARY KEY (id)
);

CREATE TABLE quiz
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_at         datetime              NOT NULL,
    updated_at         datetime              NOT NULL,
    mission_id         BIGINT                NULL,
    question           VARCHAR(255)          NOT NULL,
    type               VARCHAR(255)          NOT NULL,
    answer             VARCHAR(255)          NOT NULL,
    detail_information JSON                  NULL,
    CONSTRAINT pk_quiz PRIMARY KEY (id)
);

CREATE TABLE sub_interest
(
    id          BIGINT AUTO_INCREMENT NOT NULL,
    created_at  datetime              NOT NULL,
    updated_at  datetime              NOT NULL,
    interest_id BIGINT                NOT NULL,
    name        VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_subinterest PRIMARY KEY (id)
);

CREATE TABLE wallet
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NOT NULL,
    updated_at datetime              NOT NULL,
    member_id  BIGINT                NOT NULL,
    point      INT                   NOT NULL,
    CONSTRAINT pk_wallet PRIMARY KEY (id)
);

CREATE TABLE wallet_history
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime              NOT NULL,
    updated_at       datetime              NOT NULL,
    wallet_id        BIGINT                NOT NULL,
    `description`    VARCHAR(255)          NOT NULL,
    amount           INT                   NOT NULL,
    balance_snapshot INT                   NOT NULL,
    type             VARCHAR(255)          NOT NULL,
    mission_id       BIGINT                NULL,
    item_id          BIGINT                NULL,
    CONSTRAINT pk_wallethistory PRIMARY KEY (id)
);

ALTER TABLE attendance_streak
    ADD CONSTRAINT uc_attendance_streak_member UNIQUE (member_id);

ALTER TABLE member_setting
    ADD CONSTRAINT uc_d42ba464d4b135efd754f985c UNIQUE (member_id, setting_key);

ALTER TABLE keyword
    ADD CONSTRAINT uc_keyword_name UNIQUE (name);

ALTER TABLE wallet
    ADD CONSTRAINT uc_wallet_member UNIQUE (member_id);

ALTER TABLE attendance
    ADD CONSTRAINT uk_attendance_member_date UNIQUE (member_id, attendance_date);

ALTER TABLE goal_result
    ADD CONSTRAINT uk_goal_result_member_type_date UNIQUE (member_id, goal_type, goal_date);

ALTER TABLE member_sub_interest
    ADD CONSTRAINT uk_member_interest UNIQUE (member_id, sub_interest_id);

ALTER TABLE member_item
    ADD CONSTRAINT uk_member_item UNIQUE (member_id, item_id);

ALTER TABLE member_mission
    ADD CONSTRAINT uk_member_mission_member_mission UNIQUE (member_id, mission_id);

ALTER TABLE member_policy
    ADD CONSTRAINT uk_member_policy UNIQUE (member_id, policy_id);

ALTER TABLE member
    ADD CONSTRAINT uk_member_provider_id UNIQUE (provider, provider_id);

ALTER TABLE mission_sub_interest
    ADD CONSTRAINT uk_mission_sub_interest_mission_sub UNIQUE (mission_id, sub_interest_id);

ALTER TABLE sub_interest
    ADD CONSTRAINT uk_sub_interest_name UNIQUE (interest_id, name);

ALTER TABLE answer_image
    ADD CONSTRAINT FK_ANSWERIMAGE_ON_INQUIRY FOREIGN KEY (inquiry_id) REFERENCES inquiry (id);

ALTER TABLE attendance
    ADD CONSTRAINT FK_ATTENDANCE_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE attendance_streak
    ADD CONSTRAINT FK_ATTENDANCE_STREAK_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE goal_result
    ADD CONSTRAINT FK_GOAL_RESULT_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE inquiry_image
    ADD CONSTRAINT FK_INQUIRYIMAGE_ON_INQUIRY FOREIGN KEY (inquiry_id) REFERENCES inquiry (id);

ALTER TABLE inquiry
    ADD CONSTRAINT FK_INQUIRY_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_item
    ADD CONSTRAINT FK_MEMBERITEM_ON_ITEM FOREIGN KEY (item_id) REFERENCES item (id);

ALTER TABLE member_item
    ADD CONSTRAINT FK_MEMBERITEM_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_policy
    ADD CONSTRAINT FK_MEMBERPOLICY_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_policy
    ADD CONSTRAINT FK_MEMBERPOLICY_ON_POLICY FOREIGN KEY (policy_id) REFERENCES policy (id);

ALTER TABLE member_sub_interest
    ADD CONSTRAINT FK_MEMBERSUBINTEREST_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_sub_interest
    ADD CONSTRAINT FK_MEMBERSUBINTEREST_ON_SUB_INTEREST FOREIGN KEY (sub_interest_id) REFERENCES sub_interest (id);

ALTER TABLE member_mission
    ADD CONSTRAINT FK_MEMBER_MISSION_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE member_mission
    ADD CONSTRAINT FK_MEMBER_MISSION_ON_MISSION FOREIGN KEY (mission_id) REFERENCES mission (id);

ALTER TABLE member_setting
    ADD CONSTRAINT FK_MEMBER_SETTING_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE mission_keyword
    ADD CONSTRAINT FK_MISSIONKEYWORD_ON_KEYWORD FOREIGN KEY (keyword_id) REFERENCES keyword (id);

ALTER TABLE mission_keyword
    ADD CONSTRAINT FK_MISSIONKEYWORD_ON_MISSION FOREIGN KEY (mission_id) REFERENCES mission (id);

ALTER TABLE mission_sub_interest
    ADD CONSTRAINT FK_MISSION_SUB_INTEREST_ON_MISSION FOREIGN KEY (mission_id) REFERENCES mission (id);

ALTER TABLE mission_sub_interest
    ADD CONSTRAINT FK_MISSION_SUB_INTEREST_ON_SUB_INTEREST FOREIGN KEY (sub_interest_id) REFERENCES sub_interest (id);

ALTER TABLE quiz
    ADD CONSTRAINT FK_QUIZ_ON_MISSION FOREIGN KEY (mission_id) REFERENCES mission (id);

ALTER TABLE sub_interest
    ADD CONSTRAINT FK_SUBINTEREST_ON_INTEREST FOREIGN KEY (interest_id) REFERENCES interest (id);

ALTER TABLE wallet_history
    ADD CONSTRAINT FK_WALLETHISTORY_ON_ITEM FOREIGN KEY (item_id) REFERENCES item (id);

ALTER TABLE wallet_history
    ADD CONSTRAINT FK_WALLETHISTORY_ON_MISSION FOREIGN KEY (mission_id) REFERENCES mission (id);

ALTER TABLE wallet_history
    ADD CONSTRAINT FK_WALLETHISTORY_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallet (id);

ALTER TABLE wallet
    ADD CONSTRAINT FK_WALLET_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);