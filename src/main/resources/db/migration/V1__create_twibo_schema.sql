CREATE TABLE app_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(30) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    birthday DATE NOT NULL,
    security_question VARCHAR(120) NOT NULL,
    security_answer_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_app_user_username UNIQUE (username)
);

CREATE TABLE post (
    id BIGINT NOT NULL AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    content VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES app_user (id)
);

CREATE INDEX idx_post_author_created ON post (author_id, created_at);
CREATE INDEX idx_post_created ON post (created_at);

CREATE TABLE post_comment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (id),
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES app_user (id)
);

CREATE INDEX idx_comment_post_created ON post_comment (post_id, created_at);

CREATE TABLE user_follow (
    id BIGINT NOT NULL AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    followed_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_follow UNIQUE (follower_id, followed_id),
    CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES app_user (id),
    CONSTRAINT fk_follow_followed FOREIGN KEY (followed_id) REFERENCES app_user (id),
    CONSTRAINT ck_follow_not_self CHECK (follower_id <> followed_id)
);

CREATE INDEX idx_follow_followed ON user_follow (followed_id);

-- Shared sessions let multiple cloud replicas serve the same signed-in user.
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
);

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID)
        REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
);
