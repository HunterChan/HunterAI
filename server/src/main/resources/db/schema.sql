-- 创建用户表(如果不存在)
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- 创建照片表
CREATE TABLE IF NOT EXISTS photos (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    upload_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 创建场景表
CREATE TABLE IF NOT EXISTS scenes (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    preview_image VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建提示词表
CREATE TABLE IF NOT EXISTS prompts (
    id SERIAL PRIMARY KEY,
    user_id INT,
    content TEXT NOT NULL,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 创建合照记录表
CREATE TABLE IF NOT EXISTS photo_merges (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    photo1_id INT NOT NULL,
    photo2_id INT NOT NULL,
    scene_id INT NOT NULL,
    prompt_text TEXT,
    result_path VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (photo1_id) REFERENCES photos(id) ON DELETE CASCADE,
    FOREIGN KEY (photo2_id) REFERENCES photos(id) ON DELETE CASCADE,
    FOREIGN KEY (scene_id) REFERENCES scenes(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_photos_user_id ON photos(user_id);
CREATE INDEX IF NOT EXISTS idx_photos_file_name ON photos(file_name);
CREATE INDEX IF NOT EXISTS idx_photos_upload_time ON photos(upload_time);

CREATE INDEX IF NOT EXISTS idx_scenes_is_active ON scenes(is_active);
CREATE INDEX IF NOT EXISTS idx_prompts_user_id ON prompts(user_id);
CREATE INDEX IF NOT EXISTS idx_prompts_is_public ON prompts(is_public);
CREATE INDEX IF NOT EXISTS idx_photo_merges_user_id ON photo_merges(user_id);
CREATE INDEX IF NOT EXISTS idx_photo_merges_status ON photo_merges(status); 