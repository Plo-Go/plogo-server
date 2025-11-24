package plogo.plogoserver.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NaverBlog {
    @Value("${naverApi.clientId}")
    private String clientId;

    @Value("${naverApi.clientSecret}")
    private String clientSecret;

    public String search(String keyword) {
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", clientId);
        headers.add("X-Naver-Client-Secret", clientSecret);

        String url = "https://openapi.naver.com/v1/search/blog.json?"
                + "query=" + keyword;

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> responseEntity = rest.exchange(
                    url, HttpMethod.GET, requestEntity, String.class);

            HttpStatusCode httpStatusCode = responseEntity.getStatusCode();
            int status = httpStatusCode.value();
            String response = responseEntity.getBody();

            System.out.println("Response status: " + status);
            System.out.println("Response body: " + response);

            return response;
        } catch (Exception e) {
            System.out.println("Error during API call: " + e.getMessage());
            return null;
        }
    }
}
