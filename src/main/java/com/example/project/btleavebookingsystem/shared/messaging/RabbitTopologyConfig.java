package com.example.project.btleavebookingsystem.shared.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    public static final String STAFF_EVENTS_EXCHANGE = "staff.events.exchange";
    public static final String STAFF_DIRECTORY_QUEUE = "staff.directory.queue";
    public static final String STAFF_MEMBER_ADDED_ROUTING_KEY = "staff.member.added";
    public static final String STAFF_MEMBER_UPDATED_ROUTING_KEY = "staff.member.updated";

    @Bean
    public TopicExchange staffEventsExchange() {
        return new TopicExchange(STAFF_EVENTS_EXCHANGE, true, false);
    }

    @Bean
    public Queue staffDirectoryQueue() {
        return new Queue(STAFF_DIRECTORY_QUEUE, true);
    }

    @Bean
    public Binding staffDirectoryBinding(Queue staffDirectoryQueue, TopicExchange staffEventsExchange) {
        return BindingBuilder.bind(staffDirectoryQueue).to(staffEventsExchange).with("staff.member.*");
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}