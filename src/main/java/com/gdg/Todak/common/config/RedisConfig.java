package com.gdg.Todak.common.config;

import com.gdg.Todak.notification.service.RedisSubscriberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Value("${REDIS_HOST}")
    private String host;
    @Value("${REDIS_PORT}")
    private int port;
    @Value("${REDIS_PASSWORD}")
    private String password;

    @Bean
    public LettuceConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(
                redisStandaloneConfiguration);
        return lettuceConnectionFactory;
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisSubscriberService redisSubscriberService) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory());
        container.addMessageListener((message, pattern) ->
                        redisSubscriberService.onMessage(
                                new String(message.getChannel()),
                                new String(message.getBody())
                        ),
                new PatternTopic("notification")
        );
        return container;
    }
}
