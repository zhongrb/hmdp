package com.hmdp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMqCallbackConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    public RabbitMqCallbackConfig(RabbitTemplate rabbitTemplate) {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("RabbitMQ confirm 成功，correlationId={}",
                    correlationData == null ? null : correlationData.getId());
            return;
        }
        log.error("RabbitMQ confirm 失败，correlationId={}, cause={}",
                correlationData == null ? null : correlationData.getId(), cause);
    }

    @Override
    public void returnedMessage(org.springframework.amqp.core.ReturnedMessage returned) {
        log.error("RabbitMQ 路由失败，exchange={}, routingKey={}, replyCode={}, replyText={}",
                returned.getExchange(),
                returned.getRoutingKey(),
                returned.getReplyCode(),
                returned.getReplyText());
    }
}
