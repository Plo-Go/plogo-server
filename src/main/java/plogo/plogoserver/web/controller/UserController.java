package plogo.plogoserver.web.controller;

import static plogo.plogoserver.payload.code.status.ErrorStatus._INTERNAL_SERVER_ERROR;

import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.net.URLDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import plogo.plogoserver.payload.ApiResponse;
import plogo.plogoserver.service.KakaoService;
import plogo.plogoserver.service.UserService;
import plogo.plogoserver.web.dto.response.KakaoTokenResponseDto;
import plogo.plogoserver.web.dto.response.KakaoUserInfoResponseDto;
import plogo.plogoserver.web.dto.response.UserResponseDto;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final KakaoService kakaoService;
    private final UserService userService;

    @CrossOrigin("*")
    @Operation(summary = "카카오 로그인 및 회원 가입")
    @GetMapping("/kakao/callback")
    public ApiResponse<UserResponseDto.LoginResultDto> callback(@RequestParam("code") String code, @RequestParam("redirect_uri") String redirectUri) throws IOException {
        try {

            String decodedUri = URLDecoder.decode(redirectUri, "UTF-8");

            KakaoTokenResponseDto kakaoTokenResponseDto = kakaoService.getAccessTokenFromKakao(code, decodedUri);
            KakaoUserInfoResponseDto userInfo = kakaoService.getUserInfo(kakaoTokenResponseDto.getAccessToken());
//
            UserResponseDto.LoginResultDto loginResultDto = kakaoService.handleUserLogin(userInfo);

//            kakaoService.handleUserRegistration(userInfo, kakaoTokenResponseDto);
//
//            UserResponseDto.LoginResultDto loginResultDto = UserResponseDto.LoginResultDto.builder()
//                    .accessToken(kakaoTokenResponseDto.getAccessToken())
//                    .refreshToken(kakaoTokenResponseDto.getAccessToken())
//                    .build();
            log.info("Login result: {}", loginResultDto);
            return ApiResponse.onSuccess(loginResultDto);
        } catch (Exception e) {
            return ApiResponse.onFailure(_INTERNAL_SERVER_ERROR, null);
        }
    }

    @Operation(summary = "유저 정보 조회")
    @GetMapping("/info")
    public ApiResponse<UserResponseDto.UserInfoDto> getUserInfo() {
        return ApiResponse.onSuccess(userService.getUserInfo());
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@RequestHeader("Authorization") String token) {
        try {
            userService.deleteUser();
            return new ResponseEntity<>(ApiResponse.onSuccess(null), HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(ApiResponse.onFailure(_INTERNAL_SERVER_ERROR, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
