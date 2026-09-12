-- テストユーザー（パスワード: password123）
INSERT INTO users (user_name, email, password_hash, created_at, updated_at)
SELECT 'テストユーザー', 'test@example.com',
       '$2b$10$ewQ3Y1ICXW5u/J4BKlcHQupTeNYiNuMzMTvaEjqZBuFzj5xLVpb.S',
       now(), now()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'test@example.com'
);

-- テストカテゴリー
INSERT INTO categories (user_id, category_name, is_active, created_at, updated_at)
SELECT u.user_id, '食費', true, now(), now()
FROM users u
WHERE u.email = 'test@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM categories c
      WHERE c.user_id = u.user_id AND c.category_name = '食費'
  );

-- テスト資産
INSERT INTO assets (user_id, asset_name, asset_type, initial_balance, is_active, created_at, updated_at)
SELECT u.user_id, '財布', '現金', 10000, true, now(), now()
FROM users u
WHERE u.email = 'test@example.com'
  AND NOT EXISTS (
      SELECT 1 FROM assets a
      WHERE a.user_id = u.user_id AND a.asset_name = '財布'
  );