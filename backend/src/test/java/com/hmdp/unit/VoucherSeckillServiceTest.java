package com.hmdp.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmdp.dto.SeckillVoucherMessage;
import com.hmdp.dto.UserDTO;
import com.hmdp.exception.BizException;
import com.hmdp.service.impl.VoucherOrderMessageSender;
import com.hmdp.service.impl.VoucherOrderServiceImpl;
import com.hmdp.service.impl.VoucherRedisPreheatService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SnowflakeIdWorker;
import com.hmdp.utils.UserHolder;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@ExtendWith(MockitoExtension.class)
class VoucherSeckillServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private DefaultRedisScript<Long> seckillScript;
    @Mock
    private SnowflakeIdWorker snowflakeIdWorker;
    @Mock
    private VoucherOrderMessageSender voucherOrderMessageSender;
    @Mock
    private VoucherRedisPreheatService voucherRedisPreheatService;

    @InjectMocks
    private VoucherOrderServiceImpl voucherOrderService;

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldRejectWhenVoucherAlreadyClaimed() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        when(voucherRedisPreheatService.ensureVoucherStock(1L)).thenReturn(true);
        when(snowflakeIdWorker.nextId()).thenReturn(99L);
        when(stringRedisTemplate.execute(eq(seckillScript), anyList(), eq("1"))).thenReturn(2L);

        assertThatThrownBy(() -> voucherOrderService.claimSeckillVoucher(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("请勿重复领取同一张优惠券");

        verify(voucherOrderMessageSender, never()).send(any(SeckillVoucherMessage.class));
    }

    @Test
    void shouldRejectWhenVoucherOutOfStock() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        when(voucherRedisPreheatService.ensureVoucherStock(1L)).thenReturn(true);
        when(snowflakeIdWorker.nextId()).thenReturn(99L);
        when(stringRedisTemplate.execute(eq(seckillScript), anyList(), eq("1"))).thenReturn(1L);

        assertThatThrownBy(() -> voucherOrderService.claimSeckillVoucher(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("优惠券已抢完，下次记得早点来");
    }

    @Test
    void shouldSendMessageWhenLuaCheckPasses() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        when(voucherRedisPreheatService.ensureVoucherStock(1L)).thenReturn(true);
        when(snowflakeIdWorker.nextId()).thenReturn(123456L);
        when(stringRedisTemplate.execute(eq(seckillScript), anyList(), eq("1"))).thenReturn(0L);

        Long orderId = voucherOrderService.claimSeckillVoucher(1L);

        assertThat(orderId).isEqualTo(123456L);
        verify(stringRedisTemplate).execute(
                eq(seckillScript),
                eq(List.of(RedisConstants.SECKILL_STOCK_KEY + 1L, RedisConstants.SECKILL_ORDER_KEY + 1L)),
                eq("1")
        );
        verify(voucherOrderMessageSender).send(new SeckillVoucherMessage(123456L, 1L, 1L));
    }

    @Test
    void shouldRejectWhenVoucherNotPreheated() {
        UserHolder.saveUser(UserDTO.builder().id(2L).build());
        when(voucherRedisPreheatService.ensureVoucherStock(2L)).thenReturn(false);

        assertThatThrownBy(() -> voucherOrderService.claimSeckillVoucher(2L))
                .isInstanceOf(BizException.class)
                .hasMessage("活动尚未开始或已结束");

        verify(stringRedisTemplate, never()).execute(any(), anyList(), any());
    }
}
