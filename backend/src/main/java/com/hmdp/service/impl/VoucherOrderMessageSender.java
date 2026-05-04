package com.hmdp.service.impl;

import com.hmdp.config.RabbitMqConfig;
import com.hmdp.dto.SeckillVoucherMessage;
import com.hmdp.exception.BizException;
import com.hmdp.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherOrderMessageSender {

    private final RabbitTemplate rabbitTemplate;

    public void send(SeckillVoucherMessage message) {
        CorrelationData correlationData = new CorrelationData(RedisConstants.MQ_CORRELATION_KEY + message.orderId());
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMqConfig.SECKILL_ORDER_EXCHANGE,
                    RabbitMqConfig.SECKILL_ORDER_ROUTING_KEY,
                    message,
                    correlationData
            );
            log.info("发送秒杀下单消息成功，orderId={}, voucherId={}, userId={}",
                    message.orderId(), message.voucherId(), message.userId());
        } catch (Exception exception) {
            log.error("发送秒杀下单消息失败，orderId={}, voucherId={}, userId={}",
                    message.orderId(), message.voucherId(), message.userId(), exception);
            throw new BizException("系统繁忙，请稍后重试");
        }
    }
}
