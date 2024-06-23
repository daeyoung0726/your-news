package project.yourNews.stibee.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.yourNews.handler.exceptionHandler.error.ErrorCode;
import project.yourNews.handler.exceptionHandler.exception.CustomException;
import project.yourNews.stibee.dto.StibeeSubscribeRequest;
import project.yourNews.stibee.dto.Subscriber;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StibeeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stibee.api.url}")
    private String apiUrl;

    /* 메일 구독 */
    public void subscribe(String email) {

        try {
            Subscriber subscriber = Subscriber.builder()
                    .email(email)
                    .build();

            StibeeSubscribeRequest stibeeRequest = StibeeSubscribeRequest.builder()
                    .eventOccurredBy("MANUAL")
                    .confirmEmailYN("N")
                    .subscribers(Collections.singletonList(subscriber))
                    .build();

            String requestJson = objectMapper.writeValueAsString(stibeeRequest);
            HttpEntity<String> request = new HttpEntity<>(requestJson);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new CustomException(ErrorCode.SUBSCRIPTION_FAILED);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    /* 메일 구독 취소 */
    public void deleteSubscriber(String email) {

        try {
            List<String> emails = List.of(email);
            String requestJson = objectMapper.writeValueAsString(emails);

            HttpEntity<String> request = new HttpEntity<>(requestJson);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.DELETE, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new CustomException(ErrorCode.UNSUBSCRIBE_FAILED);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
