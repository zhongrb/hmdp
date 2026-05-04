package com.hmdp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String SECKILL_ORDER_EXCHANGE = "seckill.order.exchange";
    public static final String SECKILL_ORDER_QUEUE = "seckill.order.queue";
    public static final String SECKILL_ORDER_ROUTING_KEY = "seckill.order";
    public static final String SECKILL_ORDER_DLX = "seckill.order.dlx";
    public static final String SECKILL_ORDER_DLQ = "seckill.order.dlq";

    @Bean
    public DirectExchange seckillOrderExchange() {
        return new DirectExchange(SECKILL_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange seckillOrderDeadLetterExchange() {
        return new DirectExchange(SECKILL_ORDER_DLX, true, false);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(SECKILL_ORDER_QUEUE, true, false, false, Map.of(
                "x-dead-letter-exchange", SECKILL_ORDER_DLX,
                "x-dead-letter-routing-key", SECKILL_ORDER_ROUTING_KEY
        ));
    }

    @Bean
    public Queue seckillOrderDeadLetterQueue() {
        return new Queue(SECKILL_ORDER_DLQ, true);
    }

    @Bean
    public Binding seckillOrderBinding() {
        return BindingBuilder.bind(seckillOrderQueue())
                .to(seckillOrderExchange())
                .with(SECKILL_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding seckillOrderDeadLetterBinding() {
        return BindingBuilder.bind(seckillOrderDeadLetterQueue())
                .to(seckillOrderDeadLetterExchange())
                .with(SECKILL_ORDER_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter jackson2JsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter);
        return factory;
    }
}
