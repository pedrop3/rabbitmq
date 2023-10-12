package br.com.learn.rabbitmq.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/queue")
@AllArgsConstructor
public class TestController {

    private static final int MAX_RETRIES = 5;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping
    public void send() {
        retry();
    }

    private void retry() {
        for (int i = 0; i <= MAX_RETRIES; i++) {
            rabbitTemplate.convertAndSend("event.v1.created.ex", "", i);
        }

    }

}
