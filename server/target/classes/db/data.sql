-- 如果没有用户，添加测试用户
INSERT INTO users (username, password, email, created_at)
SELECT 'testuser', '$2a$10$eDIJO7UpNy0X.UO50MjQ3ufkvF03QBLye1sFu7KNtZY7.T5fPA.eW', 'test@example.com', CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

-- 添加测试照片数据（仅当用户存在且照片表为空时）
DO $$
DECLARE
    test_user_id INT;
BEGIN
    -- 获取测试用户ID
    SELECT id INTO test_user_id FROM users WHERE username = 'testuser';
    
    -- 如果找到用户ID且照片表为空
    IF test_user_id IS NOT NULL AND (SELECT COUNT(*) FROM photos) = 0 THEN
        -- 插入测试照片数据
        INSERT INTO photos (user_id, file_name, file_path, file_size, file_type, upload_time, description)
        VALUES 
            (test_user_id, 'test_photo1.jpg', './uploads/test_photos/test_photo1.jpg', 1024, 'image/jpeg', CURRENT_TIMESTAMP - INTERVAL '1 day', '测试照片1'),
            (test_user_id, 'test_photo2.jpg', './uploads/test_photos/test_photo2.jpg', 2048, 'image/jpeg', CURRENT_TIMESTAMP - INTERVAL '12 hours', '测试照片2'),
            (test_user_id, 'test_photo3.png', './uploads/test_photos/test_photo3.png', 3072, 'image/png', CURRENT_TIMESTAMP - INTERVAL '6 hours', '测试照片3');
    END IF;
END $$;

-- 添加测试场景数据（如果场景表为空）
INSERT INTO scenes (name, description, preview_image, is_active, created_at)
SELECT '浪漫海滩', '两个人在美丽的海滩上，夕阳西下的浪漫场景', './uploads/scenes/beach.jpg', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM scenes);

INSERT INTO scenes (name, description, preview_image, is_active, created_at)
SELECT '城市夜景', '在繁华都市的霓虹灯下，两人并肩漫步', './uploads/scenes/city_night.jpg', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM scenes WHERE name = '城市夜景');

INSERT INTO scenes (name, description, preview_image, is_active, created_at)
SELECT '童话森林', '如同童话故事里的神秘森林，充满魔幻色彩', './uploads/scenes/fairy_forest.jpg', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM scenes WHERE name = '童话森林');

INSERT INTO scenes (name, description, preview_image, is_active, created_at)
SELECT '复古风格', '怀旧复古风格的背景，仿佛回到过去的年代', './uploads/scenes/vintage.jpg', TRUE, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM scenes WHERE name = '复古风格');

-- 添加测试提示词数据
DO $$
DECLARE
    test_user_id INT;
BEGIN
    -- 获取测试用户ID
    SELECT id INTO test_user_id FROM users WHERE username = 'testuser';
    
    -- 如果找到用户ID且提示词表为空
    IF test_user_id IS NOT NULL AND (SELECT COUNT(*) FROM prompts) = 0 THEN
        -- 插入公共提示词
        INSERT INTO prompts (content, is_public, created_at)
        VALUES 
            ('微笑着看向对方，阳光明媚', TRUE, CURRENT_TIMESTAMP),
            ('一起大笑，欢乐时刻', TRUE, CURRENT_TIMESTAMP),
            ('温馨拥抱，背景虚化', TRUE, CURRENT_TIMESTAMP),
            ('专业摄影棚效果，完美光线', TRUE, CURRENT_TIMESTAMP);
            
        -- 插入用户私人提示词
        INSERT INTO prompts (user_id, content, is_public, created_at)
        VALUES 
            (test_user_id, '我们的第一次旅行纪念', FALSE, CURRENT_TIMESTAMP),
            (test_user_id, '浪漫约会的完美合影', FALSE, CURRENT_TIMESTAMP);
    END IF;
END $$;

-- 添加测试合照记录（如果用户和照片都存在且合照表为空）
DO $$
DECLARE
    test_user_id INT;
    photo1_id INT;
    photo2_id INT;
    scene_id INT;
BEGIN
    -- 获取测试数据的IDs
    SELECT id INTO test_user_id FROM users WHERE username = 'testuser';
    SELECT id INTO photo1_id FROM photos WHERE user_id = test_user_id ORDER BY id LIMIT 1;
    SELECT id INTO photo2_id FROM photos WHERE user_id = test_user_id AND id != photo1_id ORDER BY id LIMIT 1;
    SELECT id INTO scene_id FROM scenes ORDER BY id LIMIT 1;
    
    -- 如果找到所需ID且合照表为空
    IF test_user_id IS NOT NULL AND photo1_id IS NOT NULL AND photo2_id IS NOT NULL 
       AND scene_id IS NOT NULL AND (SELECT COUNT(*) FROM photo_merges) = 0 THEN
        -- 插入已完成的合照记录
        INSERT INTO photo_merges (user_id, photo1_id, photo2_id, scene_id, prompt_text, 
                                result_path, status, created_at, completed_at)
        VALUES 
            (test_user_id, photo1_id, photo2_id, scene_id, '微笑着看向对方，阳光明媚', 
             './uploads/results/user_' || test_user_id || '/merges/test_merge_1.jpg', 
             'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour');
             
        -- 插入处理中的合照记录
        INSERT INTO photo_merges (user_id, photo1_id, photo2_id, scene_id, prompt_text, 
                                status, created_at)
        VALUES 
            (test_user_id, photo2_id, photo1_id, scene_id, '一起大笑，欢乐时刻', 
             'PROCESSING', CURRENT_TIMESTAMP - INTERVAL '30 minutes');
    END IF;
END $$; 