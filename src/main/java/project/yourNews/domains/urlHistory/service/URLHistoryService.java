package project.yourNews.domains.urlHistory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.yourNews.common.utils.redis.RedisUtil;

import static project.yourNews.common.utils.redis.RedisProperties.URL_EXPIRATION_TIME;
import static project.yourNews.common.utils.redis.RedisProperties.URL_HISTORY_KEY_PREFIX;
import static project.yourNews.common.utils.redis.RedisProperties.YU_URL_EXPIRATION_TIME;

@Slf4j
@RequiredArgsConstructor
@Service
public class URLHistoryService {

    private final RedisUtil redisUtil;

    /* 기본 소식 url 저장하기 */
    public void saveDefaultURL(String url) {

        String key = URL_HISTORY_KEY_PREFIX + url;
        redisUtil.set(key, url);
        redisUtil.expire(key, URL_EXPIRATION_TIME);
    }

    /* yutopia 소식 url 저장하기 */
    public void saveYuTopiaURL(String url) {

        String key = URL_HISTORY_KEY_PREFIX + url;
        redisUtil.set(key, url);
        redisUtil.expire(key, YU_URL_EXPIRATION_TIME);
    }

    /* 취업처 url 저장하기 */
    public void saveJobURL(String url, long ttl) {

        String key = URL_HISTORY_KEY_PREFIX + url;
        redisUtil.set(key, url);
        redisUtil.expire(key, ttl);
    }

    /* 이미 보낸 소식인지 확인 */
    public boolean existsURLCheck(String url) {

        return redisUtil.existed(URL_HISTORY_KEY_PREFIX + url);
    }
}
