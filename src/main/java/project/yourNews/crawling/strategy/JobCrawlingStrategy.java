package project.yourNews.crawling.strategy;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import project.yourNews.common.utils.datetime.DateTimeFormatterUtil;
import project.yourNews.domains.member.service.MemberService;
import project.yourNews.domains.urlHistory.service.URLHistoryService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JobCrawlingStrategy implements CrawlingStrategy {

    private final MemberService memberService;
    private final URLHistoryService urlHistoryService;

    private final Map<String, Long> deadlineCache = new HashMap<>();

    private static final String JOB_BOARD_NAME = "취업처";

    @Override
    public String getScheduledTime() {
        return "0 10 10 * * *";  // 매일 오전 10시 10분
    }

    @Override
    public boolean canHandle(String newsName) {
        return JOB_BOARD_NAME.equals(newsName);
    }

    @Override
    public Elements getPostElements(Document doc) {
        return doc.select("tr");
    }

    @Override
    public boolean shouldProcessElement(Element postElement) {
        Element newPostElement = postElement.selectFirst("td:nth-child(5) img[alt='모집중']");
        return newPostElement != null;  // 모집중인 게시글인지 확인
    }

    @Override
    public String extractPostTitle(Element postElement) {
        Element companyElement = postElement.selectFirst("td:nth-child(1) a");
        Element jobElement = postElement.selectFirst("td:nth-child(2) a");

        return "회사 : " + companyElement.text() + "<br>직종 : " + jobElement.text();
    }

    @Override
    public String extractPostURL(Element postElement) {
        Element titleElement = postElement.selectFirst("td:nth-child(2) a");

        String postURL = titleElement.absUrl("href");
        long ttl = calculateTTL(extractDeadline(postElement));
        deadlineCache.put(postURL, ttl);

        return postURL;
    }

    private String extractDeadline(Element postElement) {
        Element deadlineElement = postElement.selectFirst("td:nth-child(4)");
        return deadlineElement.text().split(" ")[1];
    }

    private long calculateTTL(String deadLine) {
        LocalDate deadlineDate = DateTimeFormatterUtil.parseToLocalDateTime(deadLine);
        LocalDate currentDate = LocalDate.now();

        long daysUntilDeadline = ChronoUnit.DAYS.between(currentDate, deadlineDate);

        return (daysUntilDeadline + 1) * 86400;
    }

    @Override
    public List<String> getSubscribedMembers(String newsName) {
        return memberService.findEmailsBySubscribedNews(newsName);
    }

    @Override
    public void saveURL(String postURL) {
        urlHistoryService.saveJobURL(postURL, deadlineCache.get(postURL));
        deadlineCache.remove(postURL);
    }

    @Override
    public boolean isExisted(String postURL) {
        return urlHistoryService.existsURLCheck(postURL);
    }
}
