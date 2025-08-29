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
    public String getSigunguCourse(int areaCode, int sigunguCode, int pageNo) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://apis.data.go.kr/B551011/GreenTourService1/areaBasedList1"
                        + "?serviceKey=" + serviceKey
                        + "&numOfRows=40"
                        + "&pageNo=" + pageNo
                        + "&areaCode=" + areaCode
                        + "&sigunguCode=" + sigunguCode
                        + "&MobileOS=ETC"
                        + "&MobileApp=AppTest"
                        + "&_type=json"))
                .header("accept", "application/json")
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("📡 API Response (" + areaCode + "," + sigunguCode + ", page=" + pageNo + "): "
                    + response.body().substring(0, Math.min(200, response.body().length()))); // 앞부분만 찍기
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }


}
