package project.yourNews.crawling.strategy;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import project.yourNews.domains.member.service.MemberService;
import project.yourNews.domains.urlHistory.service.URLHistoryService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultV2CrawlingStrategy implements CrawlingStrategy {

    private final MemberService memberService;
    private final URLHistoryService urlHistoryService;

    private static final List<String> NEWS_NAME = List.of("반도체특성화대학", "AI/SW트랙");

    @Override
    public String getScheduledTime() {
        return "0 0 8-19 * * MON-FRI";  // 주말 제외, 평일에 1시간마다 크롤링
    }

    @Override
    public boolean canHandle(String newsName) {
        return NEWS_NAME.contains(newsName);
    }

    @Override
    public Elements getPostElements(Document doc) {
        return doc.select("tr");
    }

    @Override
    public boolean shouldProcessElement(Element postElement) {
        Element newPostElement = postElement.selectFirst("span.new");
        return newPostElement != null;  // 새로운 게시글인지 확인
    }

    @Override
    public String extractPostTitle(Element postElement) {
        Element titleElement = postElement.selectFirst("td.subject.tal > a");
        return titleElement.text();
    }

    @Override
    public String extractPostURL(Element postElement) {
        Element titleElement = postElement.selectFirst("td.subject.tal > a");
        return titleElement.absUrl("href");
    }

    @Override
    public List<String> getSubscribedMembers(String newsName) {
        return memberService.findEmailsBySubscribedNews(newsName);
    }

    @Override
    public void saveURL(String postURL) {
        urlHistoryService.saveDefaultURL(postURL);
    }

    @Override
    public boolean isExisted(String postURL) {
        return urlHistoryService.existsURLCheck(postURL);
    }
}
