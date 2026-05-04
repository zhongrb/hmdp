package com.hmdp.utils;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RedisData {

    private Object data;
    private LocalDateTime expireTime;
}
