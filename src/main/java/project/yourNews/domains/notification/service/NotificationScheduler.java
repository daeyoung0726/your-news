package project.yourNews.domains.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.yourNews.common.mail.mail.service.NewsEmailSender;
import project.yourNews.domains.member.service.MemberService;
import project.yourNews.domains.news.dto.NewsInfoDto;
import project.yourNews.domains.news.service.NewsService;
import project.yourNews.domains.notification.dto.NewsListDto;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NewsService newsService;
    private final NotificationService notificationService;
    private final MemberService memberService;
    private final NewsEmailSender newsEmailSender;

    @Scheduled(cron = "0 0 20 * * *")
    public void runDailyNewsNotificationJob() {

        List<NewsInfoDto> news = newsService.readAllNews();

        news.stream()
                .map(NewsInfoDto::getNewsName)
                .forEach(newsName -> {

                    List<NewsListDto> newsListDtos = notificationService.getAllNewsInfo(newsName);

                    if (!newsListDtos.isEmpty()) {
                        List<String> memberEmails = memberService.findEmailsByDailySubscribedNews(newsName);
                        newsEmailSender.sendDailyNewsToMember(memberEmails, newsName, newsListDtos);
                    }
                });
    }
}
