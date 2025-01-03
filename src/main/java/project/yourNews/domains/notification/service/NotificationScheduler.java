package project.yourNews.domains.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.yourNews.common.mail.mail.MailContentBuilder;
import project.yourNews.common.mail.mail.util.MailProperties;
import project.yourNews.crawling.dto.EmailRequest;
import project.yourNews.domains.member.service.MemberService;
import project.yourNews.domains.news.dto.NewsInfoDto;
import project.yourNews.domains.news.service.NewsService;
import project.yourNews.domains.notification.dto.NewsNotificationDto;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NewsService newsService;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationService notificationService;
    private final MemberService memberService;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Scheduled(cron = "0 0 20 * * *")
    public void runDailyNewsNotificationJob() {

        List<NewsInfoDto> news = newsService.readAllNews();

        news.stream()
                .map(NewsInfoDto::getNewsName)
                .forEach(newsName -> {

                    List<NewsNotificationDto> notificationDtos = notificationService.getAllNewsInfo(newsName);

                    if (!notificationDtos.isEmpty()) {
                        List<String> memberEmails = memberService.findEmailsByDailySubscribedNews(newsName);
                        sendNewsToMember(memberEmails, newsName, notificationDtos);
                    }
                });
    }

    /* 소식 구독자에게 메일 보내기 */
    private void sendNewsToMember(List<String> memberEmails, String newsName, List<NewsNotificationDto> notificationDtos) {
        String mailContent = MailContentBuilder.buildNewsMailContent(newsName, notificationDtos);


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
