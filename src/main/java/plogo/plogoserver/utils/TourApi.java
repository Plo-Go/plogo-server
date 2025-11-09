package plogo.plogoserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TourApi {

    private final ObjectMapper jacksonObjectMapper;
    @Value("${tour_api.service_key}")
    private String serviceKey;

    public TourApi(ObjectMapper jacksonObjectMapper) {
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    // 기본 메서드 (기존 유지)
    public String getSigunguCourse(int areaCode, int sigunguCode) {
        return getSigunguCourse(areaCode, sigunguCode, 1); // 기본적으로 pageNo=1
    }

    // 새 메서드
    // ✅ 1) 코스 조회 API
    public String getSigunguCourse(int areaCode, int sigunguCode, int pageNo) {
        try {
            //String encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

            // ✅ 중요: /areaBasedList1 → /areaBasedList 로 수정
            String url = "https://apis.data.go.kr/B551011/GreenTourService1/areaBasedList1"
                    + "?serviceKey=" + serviceKey
                    + "&numOfRows=40"
                    + "&pageNo=" + pageNo
                    + "&areaCode=" + areaCode
                    + "&sigunguCode=" + sigunguCode
                    + "&MobileOS=ETC"
                    + "&MobileApp=AppTest"
                    + "&_type=json";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("accept", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // ✅ 응답 원문 출력
            String body = response.body();
            System.out.println("📡 API Response (" + areaCode + "," + sigunguCode + ", page=" + pageNo + "): "
                    + body.substring(0, Math.min(300, body.length())));

            return body;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ 시군구 목록 조회용 메서드 (BatchService에서 호출됨)
    public String getSiCode(int areaCode) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://apis.data.go.kr/B551011/GreenTourService1/areaCode1"
                        + "?serviceKey=" + serviceKey
                        + "&numOfRows=50"
                        + "&pageNo=1"
                        + "&MobileOS=ETC"
                        + "&MobileApp=AppTest"
                        + "&areaCode=" + areaCode
                        + "&_type=json"))
                .header("accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("📡 시군구 API Response (" + areaCode + "): "
                    + response.body().substring(0, Math.min(200, response.body().length())));
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }



}
