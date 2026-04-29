package com.hmdp.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.impl.VoucherOrderServiceImpl;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@ExtendWith(MockitoExtension.class)
class VoucherSeckillServiceTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private VoucherMapper voucherMapper;
    @Mock
    private VoucherOrderMapper voucherOrderMapper;
    @Mock
    private DefaultRedisScript<Long> seckillScript;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock lock;

    @InjectMocks
    private VoucherOrderServiceImpl voucherOrderService;

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldRejectWhenVoucherAlreadyClaimed() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        when(stringRedisTemplate.execute(eq(seckillScript), anyList(), eq("1"), eq("1"))).thenReturn(2L);

        assertThatThrownBy(() -> voucherOrderService.claimSeckillVoucher(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("请勿重复领取同一张优惠券");

        verify(voucherOrderMapper, never()).insert(any(VoucherOrder.class));
    }

    @Test
    void shouldRejectWhenVoucherOutOfStock() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        when(stringRedisTemplate.execute(eq(seckillScript), anyList(), eq("1"), eq("1"))).thenReturn(1L);

        assertThatThrownBy(() -> voucherOrderService.claimSeckillVoucher(1L))
                .isInstanceOf(BizException.class)
                .hasMessage("优惠券已抢完，下次记得早点来");
    }

    @Test
    void shouldCreateOrderWhenLuaCheckPasses() {
        UserHolder.saveUser(UserDTO.builder().id(1L).build());
        Voucher voucher = new Voucher();
        voucher.setId(1L);
        voucher.setStock(10);
        voucher.setBeginTime(LocalDateTime.now().minusHours(1));
        voucher.setEndTime(LocalDateTime.now().plusHours(1));
        when(stringRedisTemplate.execute(eq(seckillScript), anyList(), eq("1"), eq("1"))).thenReturn(0L);
        when(redissonClient.getLock("lock:order:1")).thenReturn(lock);
        when(voucherOrderMapper.selectCount(any())).thenReturn(0L);
        when(voucherMapper.selectById(1L)).thenReturn(voucher);

        voucherOrderService.claimSeckillVoucher(1L);

        verify(stringRedisTemplate).execute(
                eq(seckillScript),
                eq(List.of(RedisConstants.SECKILL_STOCK_KEY + 1L, RedisConstants.SECKILL_ORDER_KEY + 1L)),
                eq("1"),
                eq("1")
        );
        verify(voucherOrderMapper).insert(any(VoucherOrder.class));
    }
}
