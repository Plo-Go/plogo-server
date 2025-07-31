package plogo.plogoserver.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

public class UserResponseDto {

    @Data
    @AllArgsConstructor
    public static class LoginResultDto {
        private String accessToken;
        private String refreshToken;
    }

    @Data
    @AllArgsConstructor
    public static class UserInfoDto {
        private Long id;
        private String email;
        private String nickname;
    }
}
