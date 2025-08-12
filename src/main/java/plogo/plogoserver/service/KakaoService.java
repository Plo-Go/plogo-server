package plogo.plogoserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import plogo.plogoserver.domain.User;
import plogo.plogoserver.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${kakao.client_id}")
    private String clientId;

    //개발 할때까지만 잠시 주석처리!
//    @Value("${kakao.redirect_uri}")
//    private String redirectUri;

    @Value("${kakao.token_uri}")
    private String tokenUri;

    @Value("${kakao.user_info_uri}")
    private String userInfoUri;


    //    public KakaoTokenResponseDto getAccessTokenFromKakao(String code) {
//        try {
//            log.info(code);
//            KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create(tokenUri).post()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/oauth/token")
//                            .queryParam("grant_type", "authorization_code")
//                            .queryParam("client_id", clientId)
//                            .queryParam("redirect_uri", redirectUri)
//                            .queryParam("code", code)
//                            .build())
//                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
//                        log.error("4xx error when requesting access token: {}", clientResponse.statusCode());
//                        log.error("Response body: {}", clientResponse.bodyToMono(String.class).block());
//
//                        return Mono.error(new RuntimeException("Invalid Parameter - " + clientResponse.statusCode()));
//                    })
//                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
//                        log.error("5xx error when requesting access token: {}", clientResponse.statusCode());
//                        return Mono.error(new RuntimeException("Internal Server Error - " + clientResponse.statusCode()));
//                    })
//                    .bodyToMono(KakaoTokenResponseDto.class)
//                    .block();
//            return kakaoTokenResponseDto;
//        } catch (Exception e) {
//            log.error("Error occurred while getting access token from Kakao: ", e);
//            throw new RuntimeException("Failed to retrieve access token from Kakao", e);
//        }
//    }
    public KakaoTokenResponseDto getAccessTokenFromKakao(String code, String redirectUri) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String uri = UriComponentsBuilder.fromUriString(tokenUri)
                    .path("/oauth/token")
                    .queryParam("grant_type", "authorization_code")
                    .queryParam("client_id", clientId)
                    //현재 개발 상황으로 인해 환경변수가 아닌 프론트에게 직접 uri를 받음 개발 완료되면 변경 필요
                    .queryParam("redirect_uri", redirectUri)
                    .queryParam("code", code)
                    .toUriString();

            ResponseEntity<KakaoTokenResponseDto> responseEntity = restTemplate.postForEntity(uri, null, KakaoTokenResponseDto.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Error occurred while getting access token from Kakao: ", e);
            throw new RuntimeException("Failed to retrieve access token from Kakao", e);
        }}

    //    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
//
//        KakaoUserInfoResponseDto userInfo = WebClient.create(userInfoUri)
//                .get()
//                .uri(uriBuilder -> uriBuilder
//                        .scheme("https")
//                        .path("/v2/user/me")
//                        .build(true))
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
//                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
//                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
//                .bodyToMono(KakaoUserInfoResponseDto.class)
//                .block();
//
//        return userInfo;
//    }
    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String uri = UriComponentsBuilder.fromUriString(userInfoUri)
                    .path("/v2/user/me")
                    .toUriString();

            ResponseEntity<KakaoUserInfoResponseDto> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, KakaoUserInfoResponseDto.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error("Error occurred while getting user info from Kakao: ", e);
            throw new RuntimeException("Failed to retrieve user info from Kakao", e);
        }
    }


    // 사용자 정보로 회원가입 처리
    public void handleUserRegistration(KakaoUserInfoResponseDto userInfo, KakaoTokenResponseDto kakaoTokenResponseDto) {


        Long kakaoId = userInfo.getId();
        User existingUser = userService.findByKakaoId(kakaoId);

        if (existingUser != null) { // 유저가 존재하지 않으면 회원가입 처리
            User newUser = User.builder()
                    .kakaoId(kakaoId)
                    .nickname(userInfo.getKakaoAccount().getProfile().getNickName())
                    .profileImg(userInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                    .email(userInfo.getKakaoAccount().getEmail())
                    .level("새싹")
                    .stampCount(0L)
                    .accessToken(kakaoTokenResponseDto.getAccessToken())  // 액세스 토큰 저장
                    .refreshToken(kakaoTokenResponseDto.getRefreshToken())
                    .build();

            userService.save(newUser);
        } else {
            // 이미 존재하는 경우 토큰을 업데이트
            User user = existingUser;
            user.setAccessToken(kakaoTokenResponseDto.getAccessToken());
            user.setRefreshToken(kakaoTokenResponseDto.getRefreshToken());
            userService.save(user); // 갱신된 정보 저장
        }
    }

    public UserResponseDto.LoginResultDto handleUserLogin(KakaoUserInfoResponseDto userInfo) {
        User existingUser = userService.findByKakaoId(userInfo.getId());

        if (existingUser != null) {
            // 이미 유저가 존재하면 엑세스 토큰과 리프레쉬 토큰을 발급
            //return jwtTokenProvider.generateTokens(existingUser.getId());
            // 이미 유저가 존재하면 엑세스 토큰과 리프레쉬 토큰을 발급
            UserResponseDto.LoginResultDto tokens = jwtTokenProvider.generateTokens(existingUser.getId());

            // 발급된 토큰을 기존 유저 객체에 업데이트
            existingUser.setAccessToken(tokens.getAccessToken());
            existingUser.setRefreshToken(tokens.getRefreshToken());

            // DB에 업데이트
            userRepository.save(existingUser);

            return tokens;

        } else {
            // 유저가 없으면 회원가입 처리 후 토큰 발급
            User newUser = User.builder()
                    .kakaoId(userInfo.getId())
                    .nickname(userInfo.getKakaoAccount().getProfile().getNickName())
                    .profileImg(userInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                    .email(userInfo.getKakaoAccount().getEmail())
                    .level("새싹 플로거")
                    .stampCount(0L)
                    .build(); // 회원가입 처리 로직 추가

            newUser = userRepository.save(newUser);

            UserResponseDto.LoginResultDto tokens = jwtTokenProvider.generateTokens(newUser.getId());
            newUser.setAccessToken(tokens.getAccessToken());
            newUser.setRefreshToken(tokens.getRefreshToken());

            userRepository.save(newUser);

            return tokens;
        }
    }
}