CREATE TABLE member_quiz (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             quiz_id BIGINT NOT NULL,
                             mission_id BIGINT NOT NULL,
                             selected_answer VARCHAR(255),
                             is_correct BOOLEAN NOT NULL,
                             created_at DATETIME(6),
                             updated_at DATETIME(6),
                             FOREIGN KEY (member_id) REFERENCES member(id),
                             FOREIGN KEY (quiz_id) REFERENCES quiz(id),
                             FOREIGN KEY (mission_id) REFERENCES mission(id)
);