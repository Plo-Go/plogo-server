package plogo.plogoserver.web.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import plogo.plogoserver.web.dto.response.UserResponseDto;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @CrossOrigin("*")
    @Operation(summary = "카카오 로그인 및 회원 가입")
    @GetMapping("/kakao/callback")
    public ApiResponse<UserResponseDto.LoginResultDto> callback(
            @RequestParam("code") String code,
            @RequestParam("redirect_uri") String redirectUri
    ) {
        // 로직 없이 Swagger용 더미 응답
        return ApiResponse.onSuccess(new UserResponseDto.LoginResultDto("access_token_example", "refresh_token_example"));
    }

    @Operation(summary = "유저 정보 조회")
    @GetMapping("/info")
    public ApiResponse<UserResponseDto.UserInfoDto> getUserInfo(
            @RequestHeader("Authorization") String token
    ) {
        return ApiResponse.onSuccess(new UserResponseDto.UserInfoDto(1L, "example@footlog.com", "닉네임"));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @RequestHeader("Authorization") String token
    ) {
        return new ResponseEntity<>(ApiResponse.onSuccess(null), HttpStatus.OK);
    }
}
