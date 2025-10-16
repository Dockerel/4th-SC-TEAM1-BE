package com.gdg.Todak.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class RabbitMQConfig {

    public static final String SAVE_NOTIFICATION_QUEUE = "saveNotificationQueue";
    public static final String SAVE_NOTIFICATION_EXCHANGE = "saveNotificationExchange";

    public static final String PUBLISH_NOTIFICATION_QUEUE = "publishNotificationQueue";
    public static final String PUBLISH_NOTIFICATION_EXCHANGE = "publishNotificationExchange";

    public static final String DEAD_LETTER_QUEUE = "deadLetterQueue";
    public static final String DEAD_LETTER_EXCHANGE = "deadLetterExchange";

    // 알림 저장 큐
    @Bean
    public Queue saveNotificationQueue() {
        return QueueBuilder.durable(SAVE_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
                .build();
    }

    @Bean
    public DirectExchange saveNotificationExchange() {
        return new DirectExchange(SAVE_NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding saveNotificationBinding() {
        return BindingBuilder.bind(saveNotificationQueue()).to(saveNotificationExchange()).with(SAVE_NOTIFICATION_QUEUE);
    }

    // 알림 저장 데드레터 큐
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DEAD_LETTER_QUEUE);
    }

    // 알림 발송 큐
    @Bean
    public String dynamicPublishNotificationQueueName() {
        String randomString = UUID.randomUUID().toString();
        return PUBLISH_NOTIFICATION_QUEUE + " : " + randomString;
    }

    @Bean
    public Queue publishNotificationQueue() {
        return new Queue(dynamicPublishNotificationQueueName(), false);
    }

    @Bean
    public FanoutExchange publishNotificationExchange() {
        return new FanoutExchange(PUBLISH_NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding publishNotificationBinding() {
        return BindingBuilder.bind(publishNotificationQueue()).to(publishNotificationExchange());
    }

    // 직렬화, 역직렬화 설정
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

        configurer.configure(factory, connectionFactory);

        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());

        return factory;
    }
}
