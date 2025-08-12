package plogo.plogoserver.web.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LogResponseDto {

//    @Builder
//    @Getter
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class LogListDto {
//        private List<LogNameDto> logList;
//    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogDto {
        private Long logId;
        private String address;
        private String name;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogDetailDto {
        private Long logId;
        private String address;
        private String logContent;
        private List<String> photos;
    }
}
