package project.yourNews.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Slf4j
@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /* Queue를 생성하는 Bean을 정의 */
    @Bean
    public Queue queue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", exchangeName) // Dead Letter Exchange 설정
                .withArgument("x-dead-letter-routing-key", routingKey + ".dlq") // Dead Letter Routing Key 설정
                .build();
    }

    /* Dead Letter Queue를 생성 */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(queueName + ".dlq", true);
    }

    /* 지정된 Exchange 이름으로 Direct Exchange Bean 을 생성 */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    /* Queue와 Exchange를 라우팅 키로 바인딩 */
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    /* Dead Letter Queue와 Exchange를 라우팅 키로 바인딩 */
    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange exchange) {
        return BindingBuilder.bind(deadLetterQueue).to(exchange).with(routingKey + ".dlq");
    }

    /*  RabbitTemplate을 생성하는 Bean을 정의. 메시지 컨버터를 설정 */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(2);
        factory.setMaxConcurrentConsumers(2);
        factory.setPrefetchCount(5);
        factory.setAdviceChain(retryInterceptor());
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    /* 메시지 처리 재시도 횟수 */
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 8000)
                .recoverer((message, cause) -> {
                    log.error("Message failed after retries: {}. Cause: {}", message, cause.getMessage(), cause);
                    new RejectAndDontRequeueRecoverer().recover(message, cause);
                })
                .build();
    }

    /* 직렬화 */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
