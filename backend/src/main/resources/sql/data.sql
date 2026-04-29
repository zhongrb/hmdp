INSERT INTO tb_user (id, phone, password, nick_name, icon)
VALUES
    (1, '13800138000', NULL, '阿星', 'https://example.com/avatar-1.png'),
    (2, '13900139000', NULL, '小满', 'https://example.com/avatar-2.png'),
    (3, '13700137000', NULL, '团团', 'https://example.com/avatar-3.png')
ON DUPLICATE KEY UPDATE nick_name = VALUES(nick_name), icon = VALUES(icon);

INSERT INTO tb_shop_type (id, name, sort, icon)
VALUES
    (1, '美食', 1, 'food'),
    (2, '丽人', 2, 'beauty'),
    (3, '酒店', 3, 'hotel')
ON DUPLICATE KEY UPDATE name = VALUES(name), sort = VALUES(sort), icon = VALUES(icon);

INSERT INTO tb_shop (id, name, type_id, address, x, y, images, avg_price, comments, score, open_hours)
VALUES
    (1, '城南小馆', 1, '上海市黄浦区云南南路 88 号', 121.490317, 31.222771, 'shop-1.jpg', 88.00, 126, 4.70, '10:00-22:00'),
    (2, '晴空造型', 2, '上海市徐汇区漕溪北路 166 号', 121.436525, 31.193446, 'shop-2.jpg', 168.00, 45, 4.60, '11:00-21:00'),
    (3, '滨江悦宿', 3, '上海市浦东新区滨江大道 2688 号', 121.506377, 31.235929, 'shop-3.jpg', 688.00, 82, 4.80, '全天营业')
ON DUPLICATE KEY UPDATE name = VALUES(name), address = VALUES(address), avg_price = VALUES(avg_price), comments = VALUES(comments), score = VALUES(score), open_hours = VALUES(open_hours);

INSERT INTO tb_voucher (id, shop_id, title, sub_title, pay_value, actual_value, stock, begin_time, end_time, status)
VALUES
    (1, 1, '双人晚餐套餐', '限时秒杀，先到先得', 9900, 19900, 50, '2026-04-01 10:00:00', '2026-12-31 23:59:59', 1),
    (2, 2, '洗剪吹体验券', '工作日可用', 5900, 9900, 80, '2026-04-01 10:00:00', '2026-12-31 23:59:59', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), sub_title = VALUES(sub_title), stock = VALUES(stock), status = VALUES(status);

INSERT INTO tb_blog (id, user_id, shop_id, title, content, images, liked, status)
VALUES
    (1, 1, 1, '晚餐值得再来', '环境舒服，菜品稳定，适合朋友小聚。', 'blog-1.jpg', 12, 1),
    (2, 2, 2, '周末换个新发型', '服务细致，沟通顺畅，整体体验不错。', 'blog-2.jpg', 9, 1)
ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), liked = VALUES(liked), status = VALUES(status);

INSERT INTO tb_follow (id, user_id, follow_user_id)
VALUES
    (1, 1, 2),
    (2, 2, 1)
ON DUPLICATE KEY UPDATE follow_user_id = VALUES(follow_user_id);
