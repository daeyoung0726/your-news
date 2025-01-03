package project.yourNews.crawling.strategy;

import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import project.yourNews.domains.member.service.MemberService;
import project.yourNews.domains.news.dto.NewsInfoDto;
import project.yourNews.domains.urlHistory.service.URLHistoryService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class YutopiaCrawlingStrategy implements CrawlingStrategy {

    private final MemberService memberService;
    private final URLHistoryService urlHistoryService;

    private static final String NEWS_NAME = "YuTopia(비교과)";

    @Override
    public String getScheduledTime() {
        return "0 10 10 * * *";  // 매일 오전 10시 10분
    }

    @Override
    public boolean canHandle(String newsName) {
        return newsName.equals(NEWS_NAME);
    }

    @Override
    public Elements getPostElements(Document doc) {
        Elements postElements = doc.select("div[data-module='eco'][data-role='item'].OPEN");
        postElements.addAll(doc.select("div[data-module='eco'][data-role='item'].APPROACH_CLOSING"));
        return postElements;
    }

    @Override
    public boolean shouldProcessElement(Element postElement) {
        return true;  // 모든 게시글을 처리하도록 항상 true 반환
    }

    @Override
    public String extractPostTitle(Element postElement) {
        return postElement.selectFirst("div.title_wrap > b.title").text();
    }

    @Override
    public String extractPostURL(Element postElement) {
        return postElement.selectFirst("a").absUrl("href");
    }

    @Override
    public List<String> getSubscribedMembers(String newsName) {
        return memberService.findEmailsBySubscribedNews(newsName);
    }

    @Override
    public void saveURL(String postURL) {
        urlHistoryService.saveYuTopiaURL(postURL);
    }

    @Override
    public boolean isExisted(String postURL) {
        return urlHistoryService.existsURLCheck(postURL);
    }

    /* YuTopia 뉴스에 대한 여러 URL 반환 */
    public List<String> getUrlsForYuTopiaNews(NewsInfoDto news) {
        String baseUrl = news.getNewsURL();

        List<String> urls = new ArrayList<>();
        urls.add(baseUrl + 1 + "/list/0/1?sort=date");
        urls.add(baseUrl + 3 + "/list/0/1?sort=date");
        urls.add(baseUrl + 5 + "/list/0/1?sort=date");
        urls.add(baseUrl + 7 + "/list/0/1?sort=date");

        return urls;
    }

}
