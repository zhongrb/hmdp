package com.hmdp.service.impl;

import com.hmdp.exception.BizException;
import com.hmdp.service.SignService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void sign() {
        Long userId = UserHolder.getUser().getId();
        LocalDate today = LocalDate.now();
        String key = buildKey(userId, today);
        int dayIndex = today.getDayOfMonth() - 1;
        // 每月一个 bitmap，按“日 - 1”定位签到位，既能快速判断当天是否已签到，也方便后续连续签到统计。
        Boolean signed = stringRedisTemplate.opsForValue().getBit(key, dayIndex);
        if (Boolean.TRUE.equals(signed)) {
            log.info("重复签到被拦截，userId={}, date={}", userId, today);
            throw new BizException("今天已经签到过了");
        }
        stringRedisTemplate.opsForValue().setBit(key, dayIndex, true);
        log.info("签到成功，userId={}, date={}", userId, today);
    }

    @Override
    public int countCurrentStreak() {
        Long userId = UserHolder.getUser().getId();
        LocalDate today = LocalDate.now();
        String key = buildKey(userId, today);
        int dayOfMonth = today.getDayOfMonth();
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth))
                        .valueAt(0)
        );
        if (result == null || result.isEmpty() || result.get(0) == null) {
            return 0;
        }
        long bits = result.get(0);
        int streak = 0;
        // 从今天开始向前统计连续的 1，遇到第一个 0 就停止，这样得到的是“截至今天”的连续签到天数。
        for (int i = 0; i < dayOfMonth; i++) {
            if ((bits & 1) == 0) {
                break;
            }
            streak++;
            bits >>>= 1;
        }
        return streak;
    }

    private String buildKey(Long userId, LocalDate date) {
        return RedisConstants.SIGN_KEY + userId + ":" + date.format(MONTH_FORMATTER);
    }
}
