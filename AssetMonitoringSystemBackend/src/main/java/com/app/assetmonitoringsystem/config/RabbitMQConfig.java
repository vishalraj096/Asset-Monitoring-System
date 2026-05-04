package com.app.assetmonitoringsystem.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for asynchronous alert notifications.
 */
@Configuration
public class RabbitMQConfig {

    public static final String ALERT_QUEUE = "eams.alert.queue";
    public static final String ALERT_EXCHANGE = "eams.alert.exchange";
    public static final String ALERT_ROUTING_KEY = "eams.alert.routing.key";

    public static final String SENSOR_QUEUE = "eams.sensor.queue";
    public static final String SENSOR_EXCHANGE = "eams.sensor.exchange";
    public static final String SENSOR_ROUTING_KEY = "eams.sensor.routing.key";

    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable(ALERT_QUEUE).build();
    }

    @Bean
    public DirectExchange alertExchange() {
        return new DirectExchange(ALERT_EXCHANGE);
    }

    @Bean
    public Binding alertBinding(Queue alertQueue, DirectExchange alertExchange) {
        return BindingBuilder.bind(alertQueue).to(alertExchange).with(ALERT_ROUTING_KEY);
    }

    @Bean
    public Queue sensorQueue() {
        return QueueBuilder.durable(SENSOR_QUEUE).build();
    }

    @Bean
    public DirectExchange sensorExchange() {
        return new DirectExchange(SENSOR_EXCHANGE);
    }

    @Bean
    public Binding sensorBinding(Queue sensorQueue, DirectExchange sensorExchange) {
        return BindingBuilder.bind(sensorQueue).to(sensorExchange).with(SENSOR_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
