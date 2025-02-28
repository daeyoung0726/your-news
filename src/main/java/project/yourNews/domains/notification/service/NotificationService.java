package project.yourNews.domains.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.yourNews.common.utils.redis.RedisUtil;
import project.yourNews.domains.notification.dto.NewsListDto;

import java.util.List;
import java.util.stream.Collectors;

import static project.yourNews.common.utils.redis.RedisProperties.NEWS_INFO_KEY_PREFIX;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RedisUtil redisUtil;

    /* 소식 종류 별 수신 정보 저장 */
    public void saveNewsInfo(String newsName, String postTitle, String postURL) {

        String key = NEWS_INFO_KEY_PREFIX + newsName;
        NewsListDto newsListDto = new NewsListDto(postTitle, postURL);

        redisUtil.setList(key, newsListDto);
    }

    /* 소식 종류 별 수신 정보 불러오기 */
    public List<NewsListDto> getAllNewsInfo(String newsName) {

        String key = NEWS_INFO_KEY_PREFIX + newsName;

        List<NewsListDto> newsInfoList = redisUtil.getList(key).stream()
                .map(obj -> (NewsListDto) obj)
                .collect(Collectors.toList());

        redisUtil.del(key);

        return newsInfoList;
    }
}
