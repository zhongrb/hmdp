package com.hmdp.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hmdp.dto.SeckillVoucherMessage;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.impl.VoucherOrderConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class VoucherOrderConsumerTest {

    @Mock
    private VoucherMapper voucherMapper;
    @Mock
    private VoucherOrderMapper voucherOrderMapper;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock lock;

    @InjectMocks
    private VoucherOrderConsumer voucherOrderConsumer;

    @Test
    void shouldCreateOrderWhenStockDeductionSucceeds() {
        SeckillVoucherMessage message = new SeckillVoucherMessage(100L, 1L, 10L);
        when(redissonClient.getLock("lock:order:1")).thenReturn(lock);
        when(voucherOrderMapper.selectCount(any())).thenReturn(0L);
        when(voucherMapper.deductStock(10L)).thenReturn(1);

        voucherOrderConsumer.handleMessage(message);

        verify(lock).lock();
        verify(voucherMapper).deductStock(10L);
        verify(voucherOrderMapper).insert(any(VoucherOrder.class));
        verify(lock).unlock();
    }

    @Test
    void shouldSkipWhenDuplicateOrderAlreadyExists() {
        SeckillVoucherMessage message = new SeckillVoucherMessage(100L, 1L, 10L);
        when(redissonClient.getLock("lock:order:1")).thenReturn(lock);
        when(voucherOrderMapper.selectCount(any())).thenReturn(1L);

        voucherOrderConsumer.handleMessage(message);

        verify(voucherMapper, never()).deductStock(any());
        verify(voucherOrderMapper, never()).insert(any(VoucherOrder.class));
        verify(lock).unlock();
    }

    @Test
    void shouldFailWhenDatabaseStockDeductionFails() {
        SeckillVoucherMessage message = new SeckillVoucherMessage(100L, 1L, 10L);
        when(redissonClient.getLock("lock:order:1")).thenReturn(lock);
        when(voucherOrderMapper.selectCount(any())).thenReturn(0L);
        when(voucherMapper.deductStock(10L)).thenReturn(0);

        assertThatThrownBy(() -> voucherOrderConsumer.handleMessage(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("数据库库存扣减失败");

        verify(voucherOrderMapper, never()).insert(any(VoucherOrder.class));
        verify(lock).unlock();
    }
}
