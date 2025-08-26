package plogo.plogoserver.web.controller;

import static plogo.plogoserver.payload.code.status.ErrorStatus._INTERNAL_SERVER_ERROR;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import plogo.plogoserver.payload.ApiResponse;
import plogo.plogoserver.service.LogService;
import plogo.plogoserver.web.dto.response.LogResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/log")
public class LogController {
    private final LogService logService;

    @Operation(summary = "완주한 코스 리스트")
    @GetMapping("/completedList")
    public ApiResponse<List<LogResponseDto.LogDto>> getCompletedList(){
        List<LogResponseDto.LogDto> response = logService.getCompletedList();
        return ApiResponse.onSuccess(response);
    }
    @Operation(summary = "로그 기록보기")
    @GetMapping("/detail/{logId}")
    public ApiResponse<LogResponseDto.LogDetailDto> getLogDetail(@PathVariable("logId") Long logId){
        LogResponseDto.LogDetailDto response = logService.getLogDetail(logId);
        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "로그 기록 업데이트")
    @PatchMapping(value = "update/{logId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<LogResponseDto.LogDetailDto>> updateLog(
            @PathVariable("logId") Long logId,
            @RequestParam(required = false) String logContent,
            @RequestParam(required = false) List<String> existingUrls,
            @RequestPart(required = false) List<MultipartFile> newImages
    ) {
        try{
            LogResponseDto.LogDetailDto response = logService.updateLog(logId, logContent, existingUrls, newImages);
            return new ResponseEntity<>(ApiResponse.onSuccess(response), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(ApiResponse.onFailure(_INTERNAL_SERVER_ERROR, null), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
