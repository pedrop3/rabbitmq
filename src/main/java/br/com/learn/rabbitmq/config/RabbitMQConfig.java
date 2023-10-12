package br.com.learn.rabbitmq.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queueItem() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "event.v1.created.dlx");
        // args.put("x-dead-letter-routing-key",
        // "event.v1.created.dlx.generate-item.dlq"); - envio direito para fila
        return new Queue("event.v1.created.generate-item", true, false, false, args);
    }

    @Bean
    public Binding binding() {
        Queue queue = queueItem();
        FanoutExchange exchange = new FanoutExchange("event.v1.created.ex");
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public Queue queueItemDLQ() {
        return new Queue("event.v1.created.dlx.generate-item.dlq");
    }

    @Bean
    public Queue queueItemDLQParkingLot() {
        return new Queue("event.v1.created.dlx.generate-item.dlq.parking-lot");
    }

    @Bean
    public Binding bindingDLQ() {
        Queue queue = queueItemDLQ();
        FanoutExchange exchange = new FanoutExchange("event.v1.created.dlx");
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        rabbitTemplate.setMessageConverter(messageConverter);

        return rabbitTemplate;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(
            RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }

}