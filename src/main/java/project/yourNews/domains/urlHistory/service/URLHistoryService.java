package project.yourNews.domains.urlHistory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.yourNews.domains.urlHistory.domain.URLHistory;
import project.yourNews.domains.urlHistory.dto.URLResponseDto;
import project.yourNews.domains.urlHistory.repository.URLHistoryRepository;
import project.yourNews.handler.exceptionHandler.error.ErrorCode;
import project.yourNews.handler.exceptionHandler.exception.CustomException;
import project.yourNews.utils.redis.RedisUtil;

import java.time.LocalDateTime;
import java.util.List;

import static project.yourNews.utils.redis.RedisProperties.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class URLHistoryService {

    private final RedisUtil redisUtil;

    /* 보낸 url 저장하기 */
    @Transactional
    public void saveURL(String url) {

        String key = URL_HISTORY_KEY_PREFIX + url;
        redisUtil.set(key, url);
        redisUtil.expire(key, URL_EXPIRATION_TIME);
    }

    /* 저장된 url 삭제하기 */
    @Transactional
    public void deleteURL(String dispatchedURL) {

        String key = URL_HISTORY_KEY_PREFIX + dispatchedURL;

        redisUtil.del(key);
    }

    /* 이미 보낸 소식인지 확인 */
    @Transactional
    public boolean existsURLCheck(String url) {

        return redisUtil.existed(URL_HISTORY_KEY_PREFIX + url);
    }
}
