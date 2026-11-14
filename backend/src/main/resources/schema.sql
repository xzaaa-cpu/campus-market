-- 校园二手交易平台 表结构（H2，MySQL 兼容模式）
-- 表名统一加 t_ 前缀，避免 user / order 等保留字冲突

CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(100) NOT NULL,
    nickname    VARCHAR(50),
    avatar      VARCHAR(255),
    phone       VARCHAR(30),
    intro       VARCHAR(255),
    role        VARCHAR(20) DEFAULT 'USER',
    created_at  DATETIME,
    CONSTRAINT uk_user_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS t_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30) NOT NULL,
    icon        VARCHAR(20),
    sort        INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS t_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(100) NOT NULL,
    description     CLOB,
    price           DECIMAL(10, 2) NOT NULL,
    original_price  DECIMAL(10, 2),
    category_id     BIGINT,
    seller_id       BIGINT NOT NULL,
    image_url       VARCHAR(500),
    item_condition  VARCHAR(20),
    location        VARCHAR(100),
    status          TINYINT DEFAULT 1,
    view_count      INT DEFAULT 0,
    created_at      DATETIME,
    updated_at      DATETIME
);

CREATE TABLE IF NOT EXISTS t_order (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no    VARCHAR(40) NOT NULL,
    item_id     BIGINT NOT NULL,
    buyer_id    BIGINT NOT NULL,
    seller_id   BIGINT NOT NULL,
    amount      DECIMAL(10, 2) NOT NULL,
    status      TINYINT DEFAULT 1,
    created_at  DATETIME,
    finished_at DATETIME,
    CONSTRAINT uk_order_no UNIQUE (order_no)
);

CREATE TABLE IF NOT EXISTS t_favorite (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    item_id    BIGINT NOT NULL,
    created_at DATETIME,
    CONSTRAINT uk_fav_user_item UNIQUE (user_id, item_id)
);

CREATE INDEX IF NOT EXISTS idx_item_status ON t_item (status);
CREATE INDEX IF NOT EXISTS idx_item_category ON t_item (category_id);
CREATE INDEX IF NOT EXISTS idx_order_buyer ON t_order (buyer_id);
CREATE INDEX IF NOT EXISTS idx_order_seller ON t_order (seller_id);
