-- ========================================
-- 테이블 생성 스크립트
-- ========================================

-- 1. users 테이블 생성
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(150) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    state VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    gender VARCHAR(10) NOT NULL DEFAULT 'NONE',
    age INT NOT NULL DEFAULT 0,
    profile VARCHAR(500),
    last_login DATETIME,
    marketing VARCHAR(20) NOT NULL DEFAULT 'DISAGREE',
    terms_of_service VARCHAR(20) NOT NULL DEFAULT 'DISAGREE',
    personal_info_policy VARCHAR(20) NOT NULL DEFAULT 'DISAGREE',
    withdrawal_reason VARCHAR(500),
    reg_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    withdraw_date DATETIME,
    
    -- 제약조건
    CONSTRAINT uniq_email UNIQUE (email),
    
    -- 인덱스
    INDEX nick_idx (nickname)
);

-- 2. social_accounts 테이블 생성
CREATE TABLE social_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    provider VARCHAR(20) NOT NULL,
    social_id VARCHAR(200) NOT NULL,
    email VARCHAR(150),
    access_token TEXT,
    refresh_token TEXT,
    connected_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT uniq_social UNIQUE (provider, social_id),
    CONSTRAINT fk_social_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. access_tokens 테이블 생성
CREATE TABLE access_tokens (
    token_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash VARCHAR(200) NOT NULL,
    token TEXT,
    platform VARCHAR(20) NOT NULL,
    user_agent VARCHAR(300) NOT NULL,
    expire_date DATETIME NOT NULL,
    revoked_at DATETIME,
    reg_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT uniq_hash UNIQUE (token_hash),
    
    -- 인덱스
    INDEX idx_user_platform (user_id, platform)
);

-- ========================================
-- 초기 데이터 (선택사항)
-- ========================================

-- 테스트용 사용자 데이터 (필요시 주석 해제)
/*
INSERT INTO users (email, nickname, state, gender, age, marketing, terms_of_service, personal_info_policy) 
VALUES 
('test@example.com', 'testuser', 'ACTIVE', 'NONE', 25, 'AGREE', 'AGREE', 'AGREE'),
('admin@example.com', 'admin', 'ACTIVE', 'NONE', 30, 'AGREE', 'AGREE', 'AGREE');
*/ 