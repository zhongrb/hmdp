package com.hmdp.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmdp.dto.UserDTO;
import com.hmdp.exception.BizException;
import com.hmdp.service.impl.SignServiceImpl;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class SignServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SignServiceImpl signService;

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldRejectWhenAlreadySignedToday() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        String key = RedisConstants.SIGN_KEY + "1:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getBit(key, LocalDate.now().getDayOfMonth() - 1)).thenReturn(true);

        assertThatThrownBy(() -> signService.sign())
                .isInstanceOf(BizException.class)
                .hasMessage("今天已经签到过了");

        verify(valueOperations, never()).setBit(key, LocalDate.now().getDayOfMonth() - 1, true);
    }

    @Test
    void shouldMarkTodaySignedWhenNotSignedYet() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        String key = RedisConstants.SIGN_KEY + "1:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.getBit(key, LocalDate.now().getDayOfMonth() - 1)).thenReturn(false);

        signService.sign();

        verify(valueOperations).setBit(key, LocalDate.now().getDayOfMonth() - 1, true);
    }

    @Test
    void shouldCountContinuousSignDaysFromBitmap() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        String key = RedisConstants.SIGN_KEY + "1:" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.bitField(
                org.mockito.ArgumentMatchers.eq(key),
                org.mockito.ArgumentMatchers.any(BitFieldSubCommands.class)
        )).thenReturn(java.util.List.of(7L));

        int streak = signService.countCurrentStreak();

        org.assertj.core.api.Assertions.assertThat(streak).isEqualTo(3);
    }
}
