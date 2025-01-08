package project.yourNews.common.mail.mail.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.yourNews.common.exception.CustomException;
import project.yourNews.common.exception.error.ErrorCode;
import project.yourNews.common.mail.mail.MailContentBuilder;
import project.yourNews.common.mail.mail.dto.StibeeRequest;
import project.yourNews.crawling.dto.EmailRequest;

import java.time.Duration;

@Slf4j
@Service
public class NewsMailService {

    private final RestTemplate restTemplate;

    @Value("${stibee.url}")
    private String stibeeUrl;

    @Value("${mail.unsubscribe.link}")
    private String unsubscribeLink;

    private final Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofSeconds(1))))
            .build();

    public NewsMailService(@Qualifier("stibeeRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /* 메일 보내기 */
    @RabbitListener(queues = "${rabbitmq.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void sendMail(EmailRequest emailRequest) {
        String email = emailRequest.getSubscriber();

        String emailUsername = email.split("@")[0];
        String emailContent = MailContentBuilder.buildRegularMailContent(
                emailRequest.getContent(),
                unsubscribeLink,
                emailUsername
        );

        sendStibeeEmail(
                StibeeRequest.builder()
                        .subscriber(email)
                        .title(emailRequest.getTitle())
                        .content(emailContent)
                        .build()
        );
    }

    /* 스티비 API를 사용하여 이메일 보내기 */
    private void sendStibeeEmail(StibeeRequest request) {
        manageRequestRate();

        HttpEntity<StibeeRequest> entity = new HttpEntity<>(request);
        ResponseEntity<String> response = restTemplate.postForEntity(stibeeUrl, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Subscription failed. Status code: {}, Response body: {}", response.getStatusCode(), response.getBody());
            throw new CustomException(ErrorCode.EMAIL_SENDING_FAILED);
        }
    }

    private void manageRequestRate() {
        try {
            bucket.asBlocking().consume(1); // 토큰이 없으면 대기하여 요청 처리.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.THREAD_INTERRUPTED);
        }
    }
}
