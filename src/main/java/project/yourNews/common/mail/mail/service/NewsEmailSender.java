package project.yourNews.common.mail.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import project.yourNews.common.mail.mail.MailContentBuilder;
import project.yourNews.common.mail.mail.util.MailProperties;
import project.yourNews.crawling.dto.EmailRequest;
import project.yourNews.domains.notification.dto.NewsListDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsEmailSender {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public void sendNewsToMember(List<String> memberEmails, String newsName, String postTitle, String postURL) {
        String mailContent = MailContentBuilder.buildNewsMailContent(newsName, postTitle, postURL);
        sendEmail(memberEmails, newsName, mailContent);
    }

    public void sendNewsToMember(List<String> memberEmails, String newsName, List<NewsListDto> newsListDtos) {
        String mailContent = MailContentBuilder.buildJobNewsMailContent(newsName, newsListDtos);
        sendEmail(memberEmails, newsName, mailContent);
    }

    public void sendDailyNewsToMember(List<String> memberEmails, String newsName, List<NewsListDto> newsListDtos) {
        String mailContent = MailContentBuilder.buildNewsMailContent(newsName, newsListDtos);
        sendEmail(memberEmails, newsName, mailContent);
    }

    private void sendEmail(List<String> memberEmails, String newsName, String mailContent) {
        for (String email : memberEmails) {
            EmailRequest emailRequest = new EmailRequest(email, MailProperties.NEWS_SUBJECT, mailContent);
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, emailRequest);
            } catch (Exception e) {
                log.error("Failed to send email request to RabbitMQ : {}", newsName, e);
            }
        }
    }
}
