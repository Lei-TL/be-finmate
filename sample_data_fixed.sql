USE finmate;

SET @user_id = '8075ba80-6e1c-4717-b1a6-3e2c9a1b4fe8';

SET @now = NOW();

-- ============================================
-- 1. WALLETS (Ví tiền)
-- ============================================

-- Ví chính (default)
INSERT INTO wallets (id, user_id, name, type, currency, initial_balance, current_balance, is_default, archived, color, created_at, updated_at, deleted)
VALUES 
    (UUID(), @user_id, 'Ví chính', 'CASH', 'VND', 5000000.0000, 3500000.0000, 1, 0, '#4CAF50', @now, @now, 0);

-- Ví tiết kiệm
INSERT INTO wallets (id, user_id, name, type, currency, initial_balance, current_balance, is_default, archived, color, created_at, updated_at, deleted)
VALUES 
    (UUID(), @user_id, 'Ví tiết kiệm', 'SAVINGS', 'VND', 10000000.0000, 10000000.0000, 0, 0, '#2196F3', @now, @now, 0);

-- Ví chi tiêu hàng ngày
INSERT INTO wallets (id, user_id, name, type, currency, initial_balance, current_balance, is_default, archived, color, created_at, updated_at, deleted)
VALUES 
    (UUID(), @user_id, 'Ví chi tiêu', 'CASH', 'VND', 2000000.0000, 500000.0000, 0, 0, '#FF9800', @now, @now, 0);

-- Lưu wallet IDs để dùng cho transactions
SET @wallet_main = (SELECT id FROM wallets WHERE user_id = @user_id AND is_default = 1 LIMIT 1);
SET @wallet_savings = (SELECT id FROM wallets WHERE user_id = @user_id AND name = 'Ví tiết kiệm' LIMIT 1);
SET @wallet_daily = (SELECT id FROM wallets WHERE user_id = @user_id AND name = 'Ví chi tiêu' LIMIT 1);

-- ============================================
-- 2. CATEGORIES (Danh mục) - Global, không có user_id
-- ============================================
-- ✅ Categories giờ là global, chỉ insert nếu chưa có

-- INCOME categories
INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Lương', 'INCOME', 'ic_salary', 1, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Lương' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Thưởng', 'INCOME', 'ic_bonus', 2, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Thưởng' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Đầu tư', 'INCOME', 'ic_invest', 3, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Đầu tư' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Kinh doanh', 'INCOME', 'ic_business', 4, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kinh doanh' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Cho thuê', 'INCOME', 'ic_rent', 5, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Cho thuê' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Lãi tiết kiệm', 'INCOME', 'ic_interest', 6, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Lãi tiết kiệm' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Quà tặng nhận', 'INCOME', 'ic_gift_received', 7, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Quà tặng nhận' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Bán hàng', 'INCOME', 'ic_sell', 8, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Bán hàng' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Hoàn tiền', 'INCOME', 'ic_refund', 9, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hoàn tiền' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Thu nhập khác', 'INCOME', 'ic_other_income', 10, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Thu nhập khác' AND deleted = 0);

-- EXPENSE categories
INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Ăn uống', 'EXPENSE', 'ic_food', 1, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Ăn uống' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Mua sắm', 'EXPENSE', 'ic_shopping', 2, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mua sắm' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Hóa đơn điện', 'EXPENSE', 'ic_electricbill', 3, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hóa đơn điện' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Hóa đơn nước', 'EXPENSE', 'ic_waterbill', 4, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hóa đơn nước' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Xăng xe', 'EXPENSE', 'ic_car', 5, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Xăng xe' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Giải trí', 'EXPENSE', 'ic_entertain', 6, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Giải trí' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Sức khỏe', 'EXPENSE', 'ic_health', 7, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Sức khỏe' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Giáo dục', 'EXPENSE', 'ic_education', 8, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Giáo dục' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Du lịch', 'EXPENSE', 'ic_travel', 9, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Du lịch' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Quà tặng', 'EXPENSE', 'ic_gift', 10, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Quà tặng' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Thời trang', 'EXPENSE', 'ic_fashion', 11, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Thời trang' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Hóa đơn', 'EXPENSE', 'ic_bill', 12, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Hóa đơn' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Internet', 'EXPENSE', 'ic_internet', 13, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Internet' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Điện thoại', 'EXPENSE', 'ic_phone', 14, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Điện thoại' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Bảo hiểm', 'EXPENSE', 'ic_insurance', 15, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Bảo hiểm' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Nhà ở', 'EXPENSE', 'ic_home', 16, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Nhà ở' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Thú cưng', 'EXPENSE', 'ic_pet', 17, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Thú cưng' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Thể thao', 'EXPENSE', 'ic_sport', 18, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Thể thao' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Đọc sách', 'EXPENSE', 'ic_read', 19, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Đọc sách' AND deleted = 0);

INSERT INTO categories (id, name, type, icon, display_order, created_at, updated_at, deleted)
SELECT UUID(), 'Khác', 'EXPENSE', 'ic_default_category', 20, @now, @now, 0
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Khác' AND deleted = 0);

-- ✅ Lưu category IDs - Categories giờ là global, không có user_id
SET @cat_salary = (SELECT id FROM categories WHERE name = 'Lương' AND deleted = 0 LIMIT 1);
SET @cat_bonus = (SELECT id FROM categories WHERE name = 'Thưởng' AND deleted = 0 LIMIT 1);
SET @cat_food = (SELECT id FROM categories WHERE name = 'Ăn uống' AND deleted = 0 LIMIT 1);
SET @cat_shopping = (SELECT id FROM categories WHERE name = 'Mua sắm' AND deleted = 0 LIMIT 1);
SET @cat_electric = (SELECT id FROM categories WHERE name = 'Hóa đơn điện' AND deleted = 0 LIMIT 1);
SET @cat_water = (SELECT id FROM categories WHERE name = 'Hóa đơn nước' AND deleted = 0 LIMIT 1);
SET @cat_car = (SELECT id FROM categories WHERE name = 'Xăng xe' AND deleted = 0 LIMIT 1);
SET @cat_entertain = (SELECT id FROM categories WHERE name = 'Giải trí' AND deleted = 0 LIMIT 1);
SET @cat_health = (SELECT id FROM categories WHERE name = 'Sức khỏe' AND deleted = 0 LIMIT 1);
SET @cat_education = (SELECT id FROM categories WHERE name = 'Giáo dục' AND deleted = 0 LIMIT 1);
SET @cat_travel = (SELECT id FROM categories WHERE name = 'Du lịch' AND deleted = 0 LIMIT 1);
SET @cat_gift = (SELECT id FROM categories WHERE name = 'Quà tặng' AND deleted = 0 LIMIT 1);

-- ============================================
-- 3. TRANSACTIONS (Giao dịch)
-- ============================================

-- INCOME transactions (Thu nhập)
INSERT INTO transactions (id, user_id, wallet_id, category_id, type, amount, currency, occurred_at, note, created_at, updated_at, deleted)
VALUES 
    -- Lương tháng 11
    (UUID(), @user_id, @wallet_main, @cat_salary, 'INCOME', 15000000.0000, 'VND', DATE_SUB(@now, INTERVAL 15 DAY), 'Lương tháng 11/2024', DATE_SUB(@now, INTERVAL 15 DAY), DATE_SUB(@now, INTERVAL 15 DAY), 0),
    -- Thưởng
    (UUID(), @user_id, @wallet_main, @cat_bonus, 'INCOME', 2000000.0000, 'VND', DATE_SUB(@now, INTERVAL 10 DAY), 'Thưởng cuối năm', DATE_SUB(@now, INTERVAL 10 DAY), DATE_SUB(@now, INTERVAL 10 DAY), 0),
    -- Lương tháng 12
    (UUID(), @user_id, @wallet_main, @cat_salary, 'INCOME', 15000000.0000, 'VND', DATE_SUB(@now, INTERVAL 2 DAY), 'Lương tháng 12/2024', DATE_SUB(@now, INTERVAL 2 DAY), DATE_SUB(@now, INTERVAL 2 DAY), 0);

-- EXPENSE transactions (Chi tiêu)
INSERT INTO transactions (id, user_id, wallet_id, category_id, type, amount, currency, occurred_at, note, created_at, updated_at, deleted)
VALUES 
    -- Ăn uống
    (UUID(), @user_id, @wallet_main, @cat_food, 'EXPENSE', 50000.0000, 'VND', DATE_SUB(@now, INTERVAL 1 DAY), 'Ăn trưa', DATE_SUB(@now, INTERVAL 1 DAY), DATE_SUB(@now, INTERVAL 1 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_food, 'EXPENSE', 80000.0000, 'VND', DATE_SUB(@now, INTERVAL 2 DAY), 'Cà phê với bạn', DATE_SUB(@now, INTERVAL 2 DAY), DATE_SUB(@now, INTERVAL 2 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_food, 'EXPENSE', 120000.0000, 'VND', DATE_SUB(@now, INTERVAL 3 DAY), 'Đi chợ mua đồ ăn', DATE_SUB(@now, INTERVAL 3 DAY), DATE_SUB(@now, INTERVAL 3 DAY), 0),
    (UUID(), @user_id, @wallet_daily, @cat_food, 'EXPENSE', 60000.0000, 'VND', DATE_SUB(@now, INTERVAL 4 DAY), 'Bữa sáng', DATE_SUB(@now, INTERVAL 4 DAY), DATE_SUB(@now, INTERVAL 4 DAY), 0),
    
    -- Mua sắm
    (UUID(), @user_id, @wallet_main, @cat_shopping, 'EXPENSE', 500000.0000, 'VND', DATE_SUB(@now, INTERVAL 5 DAY), 'Mua quần áo', DATE_SUB(@now, INTERVAL 5 DAY), DATE_SUB(@now, INTERVAL 5 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_shopping, 'EXPENSE', 300000.0000, 'VND', DATE_SUB(@now, INTERVAL 7 DAY), 'Mua đồ dùng cá nhân', DATE_SUB(@now, INTERVAL 7 DAY), DATE_SUB(@now, INTERVAL 7 DAY), 0),
    
    -- Hóa đơn
    (UUID(), @user_id, @wallet_main, @cat_electric, 'EXPENSE', 350000.0000, 'VND', DATE_SUB(@now, INTERVAL 8 DAY), 'Hóa đơn điện tháng 11', DATE_SUB(@now, INTERVAL 8 DAY), DATE_SUB(@now, INTERVAL 8 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_water, 'EXPENSE', 150000.0000, 'VND', DATE_SUB(@now, INTERVAL 9 DAY), 'Hóa đơn nước tháng 11', DATE_SUB(@now, INTERVAL 9 DAY), DATE_SUB(@now, INTERVAL 9 DAY), 0),
    
    -- Xăng xe
    (UUID(), @user_id, @wallet_main, @cat_car, 'EXPENSE', 200000.0000, 'VND', DATE_SUB(@now, INTERVAL 6 DAY), 'Đổ xăng', DATE_SUB(@now, INTERVAL 6 DAY), DATE_SUB(@now, INTERVAL 6 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_car, 'EXPENSE', 250000.0000, 'VND', DATE_SUB(@now, INTERVAL 11 DAY), 'Đổ xăng', DATE_SUB(@now, INTERVAL 11 DAY), DATE_SUB(@now, INTERVAL 11 DAY), 0),
    
    -- Giải trí
    (UUID(), @user_id, @wallet_main, @cat_entertain, 'EXPENSE', 300000.0000, 'VND', DATE_SUB(@now, INTERVAL 12 DAY), 'Xem phim', DATE_SUB(@now, INTERVAL 12 DAY), DATE_SUB(@now, INTERVAL 12 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_entertain, 'EXPENSE', 150000.0000, 'VND', DATE_SUB(@now, INTERVAL 13 DAY), 'Chơi game', DATE_SUB(@now, INTERVAL 13 DAY), DATE_SUB(@now, INTERVAL 13 DAY), 0),
    
    -- Sức khỏe
    (UUID(), @user_id, @wallet_main, @cat_health, 'EXPENSE', 500000.0000, 'VND', DATE_SUB(@now, INTERVAL 14 DAY), 'Khám sức khỏe', DATE_SUB(@now, INTERVAL 14 DAY), DATE_SUB(@now, INTERVAL 14 DAY), 0),
    
    -- Giáo dục
    (UUID(), @user_id, @wallet_main, @cat_education, 'EXPENSE', 1000000.0000, 'VND', DATE_SUB(@now, INTERVAL 16 DAY), 'Mua sách học', DATE_SUB(@now, INTERVAL 16 DAY), DATE_SUB(@now, INTERVAL 16 DAY), 0),
    
    -- Du lịch
    (UUID(), @user_id, @wallet_main, @cat_travel, 'EXPENSE', 2000000.0000, 'VND', DATE_SUB(@now, INTERVAL 20 DAY), 'Du lịch Đà Lạt', DATE_SUB(@now, INTERVAL 20 DAY), DATE_SUB(@now, INTERVAL 20 DAY), 0),
    
    -- Quà tặng
    (UUID(), @user_id, @wallet_main, @cat_gift, 'EXPENSE', 400000.0000, 'VND', DATE_SUB(@now, INTERVAL 18 DAY), 'Quà sinh nhật bạn', DATE_SUB(@now, INTERVAL 18 DAY), DATE_SUB(@now, INTERVAL 18 DAY), 0);

-- Transactions tháng trước
INSERT INTO transactions (id, user_id, wallet_id, category_id, type, amount, currency, occurred_at, note, created_at, updated_at, deleted)
VALUES
    -- ===== Tháng -1 =====
    (UUID(), @user_id, @wallet_main, @cat_food, 'EXPENSE', 120000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 1 MONTH), INTERVAL 2 DAY), 'Ăn tối với bạn (tháng -1)', DATE_SUB(DATE_SUB(@now, INTERVAL 1 MONTH), INTERVAL 2 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 1 MONTH), INTERVAL 2 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_shopping, 'EXPENSE', 450000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 1 MONTH), INTERVAL 5 DAY), 'Mua áo khoác mới (tháng -1)', DATE_SUB(DATE_SUB(@now, INTERVAL 1 MONTH), INTERVAL 5 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 1 MONTH), INTERVAL 5 DAY), 0),
    
    -- ===== Tháng -2 =====
    (UUID(), @user_id, @wallet_main, @cat_electric, 'EXPENSE', 320000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 3 DAY), 'Hóa đơn điện (tháng -2)', DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 3 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 3 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_water, 'EXPENSE', 140000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 4 DAY), 'Hóa đơn nước (tháng -2)', DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 4 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 4 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_car, 'EXPENSE', 180000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 6 DAY), 'Đổ xăng (tháng -2)', DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 6 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 2 MONTH), INTERVAL 6 DAY), 0),
    
    -- ===== Tháng -3 =====
    (UUID(), @user_id, @wallet_main, @cat_entertain, 'EXPENSE', 250000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 2 DAY), 'Đi karaoke với bạn (tháng -3)', DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 2 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 2 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_education, 'EXPENSE', 800000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 7 DAY), 'Đóng học phí / khóa học online (tháng -3)', DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 7 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 7 DAY), 0),
    (UUID(), @user_id, @wallet_main, @cat_travel, 'EXPENSE', 1500000.0000, 'VND', DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 10 DAY), 'Đi chơi gần (tháng -3)', DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 10 DAY), DATE_SUB(DATE_SUB(@now, INTERVAL 3 MONTH), INTERVAL 10 DAY), 0);

-- ============================================
-- 4. BUDGETS (Ngân sách) - Optional
-- ============================================

-- Ngân sách chi tiêu tháng 12
INSERT INTO budgets (id, user_id, wallet_id, category_id, name, period_type, start_date, end_date, limit_amount, currency, spent_amount, created_at, updated_at, deleted)
VALUES 
    (UUID(), @user_id, @wallet_main, NULL, 'Ngân sách tháng 12', 'MONTHLY', DATE_FORMAT(@now, '%Y-12-01'), DATE_FORMAT(@now, '%Y-12-31'), 5000000.0000, 'VND', 2000000.0000, @now, @now, 0),
    (UUID(), @user_id, @wallet_main, @cat_food, 'Ngân sách ăn uống tháng 12', 'MONTHLY', DATE_FORMAT(@now, '%Y-12-01'), DATE_FORMAT(@now, '%Y-12-31'), 1000000.0000, 'VND', 310000.0000, @now, @now, 0);

-- ============================================
-- VERIFY DATA
-- ============================================

-- Kiểm tra số lượng records đã insert
SELECT 'Wallets' as table_name, COUNT(*) as count FROM wallets WHERE user_id = @user_id AND deleted = 0
UNION ALL
SELECT 'Categories', COUNT(*) FROM categories WHERE deleted = 0
UNION ALL
SELECT 'Transactions', COUNT(*) FROM transactions WHERE user_id = @user_id AND deleted = 0
UNION ALL
SELECT 'Budgets', COUNT(*) FROM budgets WHERE user_id = @user_id AND deleted = 0;



