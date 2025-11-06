package plogo.plogoserver.utils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import plogo.plogoserver.web.dto.response.PreferenceRequestBody;

@Component
public class RecommendSystem {
    //@Value("${FLASK_BaseURL}")
    private final String baseUrl = "http://3.39.43.241:5050/api";

    public List<Long> getRecommendations(PreferenceRequestBody request) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        // ✅ Flask에 맞게 keyword 리스트들을 하나로 합쳐서 문자열로 변환
        String joinedPreference = String.join(" ",
                Stream.of(
                                request.getFirstKeyword(),
                                request.getSecondKeyword(),
                                request.getThirdKeyword()
                        )
                        .filter(Objects::nonNull) // null 리스트는 제외
                        .flatMap(List::stream)    // 리스트 내부 원소 펼치기
                        .collect(Collectors.toList())
        );

        // ✅ Flask가 요구하는 구조에 맞게 Map으로 감싸기
        Map<String, String> body = Map.of("preference", joinedPreference);

        // ✅ WebClient 요청 (bodyValue 변경됨)
        Map<String, Object> response = webClient.post()
                .uri("/recommend/course")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body) // 👈 여기! 원래는 request였는데 body로 교체
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        // ✅ 응답에서 course_id 리스트 꺼내기
        if (response != null && response.containsKey("course_id")) {
            List<?> rawList = (List<?>) response.get("course_id"); // 🔹 우선 Object로 받기

            List<Long> courseIds = rawList.stream()
                    .map(Object::toString)   // 🔹 어떤 타입이든 문자열로 변환
                    .map(Long::parseLong)    // 🔹 문자열을 Long으로 변환
                    .collect(Collectors.toList());

            return courseIds;
        } else {
            throw new RuntimeException("course_id를 응답에서 찾을 수 없습니다.");
        }
    }
}
