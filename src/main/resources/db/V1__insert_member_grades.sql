-- Member Grade initial data (L1-L5)
INSERT INTO member_grades (id, code, name, min_amount, discount_percent, point_multiplier) VALUES
(gen_random_uuid(), 'L1', '普通会员', 0, 0, 1.0),
(gen_random_uuid(), 'L2', '白银会员', 500, 5, 1.2),
(gen_random_uuid(), 'L3', '黄金会员', 2000, 10, 1.5),
(gen_random_uuid(), 'L4', '铂金会员', 5000, 15, 2.0),
(gen_random_uuid(), 'L5', '钻石会员', 10000, 20, 3.0);