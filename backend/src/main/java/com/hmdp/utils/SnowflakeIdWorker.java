package com.hmdp.utils;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SnowflakeIdWorker {

    private static final long START_TIMESTAMP = 1735660800000L;
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    private final long workerId;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public SnowflakeIdWorker(@Value("${app.id-worker.worker-id:1}") long workerId) {
        if (workerId < 0 || workerId > MAX_WORKER_ID) {
            throw new IllegalArgumentException("workerId 超出 Snowflake 允许范围");
        }
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long currentTimestamp = currentTimestamp();
        if (currentTimestamp < lastTimestamp) {
            log.error("检测到系统时钟回拨，lastTimestamp={}, currentTimestamp={}", lastTimestamp, currentTimestamp);
            throw new IllegalStateException("系统时钟回拨，无法生成订单号");
        }
        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = currentTimestamp;
        return ((currentTimestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimestamp();
        }
        return timestamp;
    }

    private long currentTimestamp() {
        return Instant.now().toEpochMilli();
    }
}
