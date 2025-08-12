package plogo.plogoserver.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plogo.plogoserver.payload.ApiResponse;
import plogo.plogoserver.payload.code.status.ErrorStatus;
import plogo.plogoserver.repository.CourseRepository;
import plogo.plogoserver.service.AreaService;
import plogo.plogoserver.service.CourseService;
import plogo.plogoserver.service.SearchService;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final AreaService areaService;

    @Operation(summary = "최근 검색어 조회")
    @GetMapping("/recent")
    public ApiResponse<List<SearchLogDTO>> findRecentSearchLog(
            @RequestHeader("Authorization") String token
    ) {
        String tokenWithoutBearer = token.substring(7);
        List<SearchLogDTO> recentSearchLogList = searchService.getRecentSearchLogs(tokenWithoutBearer);

        return ApiResponse.onSuccess(recentSearchLogList);
    }

    @Operation(summary = "검색어 삭제")
    @PatchMapping("/delete/{keyword}")
    public ApiResponse<CourseCountDTO> deleteSearchLog(
            @RequestHeader("Authorization") String token,
            @PathVariable(value = "keyword") String keyword
    ) {
        String tokenWithoutBearer = token.substring(7);

        try {
            String decodedKeyword = URLDecoder.decode(keyword, "UTF-8");

            return ApiResponse.onSuccess(CourseCountDTO.builder()
                    .Count(searchService.deleteRecentSearchLog(tokenWithoutBearer, decodedKeyword))
                    .build());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

            return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST, CourseCountDTO.builder().build());
        }
    }

    @Operation(summary = "코스 검색")
    @GetMapping("/course/{keyword}")
    public ApiResponse<List<CourseResponseDTO>> searchCourse(
            @RequestHeader("Authorization") String token,
            @PathVariable(value = "keyword") String keyword
    ) {
        String tokenWithoutBearer = token.substring(7);

        try {
            String decodedKeyword = URLDecoder.decode(keyword, "UTF-8");

            return ApiResponse.onSuccess(courseService.getCourseByKeyword(tokenWithoutBearer, decodedKeyword));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return ApiResponse.onFailure(ErrorStatus._BAD_REQUEST, Arrays.asList(CourseResponseDTO.builder().build()));
        }


    }

    @Operation(summary = "시군구 정보 전체 조회")
    @GetMapping(value = "sigungu_code")
    public ApiResponse<List<SigunguCodeDTO>> getSigunguInfo(
            @RequestHeader("Authorization") String token
    ) {
        return ApiResponse.onSuccess(areaService.getSigunguInfo());
    }
}
