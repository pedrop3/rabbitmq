package br.com.learn.rabbitmq.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class QueueConsumer {

    private static final int NTHREDS = 10;
    private static final String QUEUE_ITEM = "event.v1.created.generate-item";
    private static final String DLQ = "event.v1.created.dlx.generate-item.dlq";
    private static final String DLQ_PARKING_LOT = "event.v1.created.dlx.generate-item.dlq.parking-lot";
    private static ExecutorService threadExecutor = Executors.newFixedThreadPool(NTHREDS);

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = { QUEUE_ITEM }, concurrency = "1")
    public void receive(@Payload String fileBody) {

        if (Integer.parseInt(fileBody) == 2) {
            throw new RuntimeException("Errro");
        }

        threadExecutor.submit(new Thread(() -> {
            System.out.println("Message " + fileBody);
        }));

    }

    @RabbitListener(queues = { DLQ }, concurrency = "1")
    public void dlQueue(@Payload Object event, @Headers Map<String, Object> headers) {

        Integer retryHeader = (Integer) headers.get("x-dlq-retry");

        if (retryHeader == null) {
            retryHeader = 0;
        }

        if (retryHeader < 3) {
            Map<String, Object> newHeaders = new HashMap<>(headers);

            int retryCount = retryHeader + 1;
            newHeaders.put("x-dlq-retry", retryCount);

            // Lógica Reprocessamento ....

            final MessagePostProcessor messagePostProcessor = message -> {
                MessageProperties messageProperties = message.getMessageProperties();
                newHeaders.forEach(messageProperties::setHeader);
                return message;
            };

            System.out.println("Resend messagem " + event);
            this.rabbitTemplate.convertAndSend(DLQ, event, messagePostProcessor);
        } else {
            System.out.println("Resend messagem to parking in lot " + event);
            this.rabbitTemplate.convertAndSend(DLQ_PARKING_LOT, event);

        }

    }

}