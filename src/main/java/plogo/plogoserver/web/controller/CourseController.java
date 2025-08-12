package plogo.plogoserver.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plogo.plogoserver.payload.ApiResponse;
import plogo.plogoserver.service.AreaService;
import plogo.plogoserver.service.CourseService;
import plogo.plogoserver.service.S3ImageService;
import plogo.plogoserver.service.SaveService;

@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;
    private final SaveService saveService;
    private final S3ImageService s3ImageService;
    private final AreaConverter areaConverter;
    private final AreaService areaService;

    @Operation(summary = "지역 선택 시 해당 코스 반환")
    @GetMapping("/area/{areaCode}")
    public ApiResponse<List<CourseResponseDTO>> coursesFromArea(
            @RequestHeader("Authorization") String token,
            @PathVariable Long areaCode
    ) {
        String tokenWithoutBearer = token.substring(7);
        //임시 유저값
        List<CourseResponseDTO> courses = courseService.getByAreaName(tokenWithoutBearer, areaCode);

        return ApiResponse.onSuccess(courses);
    }

//    @Operation(summary = "선호도 코스 분석 : 테스트 용")
//    @PostMapping("/analyze")
//    public ApiResponse<List<Long>> analyzePreference(
//            @RequestHeader String token,
//            @RequestBody PreferenceRequestBody requestBody
//            ) {
//        return ApiResponse.onSuccess(courseService.analyzePreference(token, requestBody));
//    }

    @Operation(summary = "선호도 코스 분석")
    @PostMapping("/analyze")
    public ApiResponse<List<CourseResponseDTO>> analyzePreference(
            @RequestHeader("Authorization") String token,
            @RequestBody PreferenceRequestBody requestBody
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(courseService.analyzePreference(tokenWithoutBearer, requestBody));
    }

    @Operation(summary = "추천 코스 조회")
    @GetMapping("/recommend")
    public ApiResponse<List<CourseResponseDTO>> recommendCourse(
            @RequestHeader("Authorization") String token
    ) {
        String tokenWithoutBearer = token.substring(7);
        List<CourseResponseDTO> recommendCourses = courseService.getRecommendCourse(tokenWithoutBearer);

        if (recommendCourses.isEmpty())
            return ApiResponse.onFailure(ErrorStatus._NOANALISYS, recommendCourses);
        else
            return ApiResponse.onSuccess(recommendCourses);
    }

    @Operation(summary = "코스 클릭 시 상세 정보 조회")
    @GetMapping("/detail/{course_id}")
    public ApiResponse<CourseDetailDTO> courseDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long course_id
    ) {
        String tokenWithoutBearer = token.substring(7);
        //임시 유저값
        CourseDetailDTO course = courseService.getCourseDetail(tokenWithoutBearer, course_id);

        return ApiResponse.onSuccess(course);
    }

    @Operation(summary = "코스 저장 버튼 클릭 시 저장/취소 토글 기능")
    @PostMapping("/save/{course_id}")
    public ApiResponse<SaveStatusDTO> saveCourse(
            @RequestHeader("Authorization") String token,
            @PathVariable Long course_id
    ) {
        String tokenWithoutBearer = token.substring(7);
        SaveStatusDTO dto = courseService.toggleSaveCourse(tokenWithoutBearer, course_id);
        return ApiResponse.onSuccess(dto);
    }

    @Operation(summary = "코스 네이버 포스트 조회")
    @GetMapping(value = "/post/{course_id}")
    public ApiResponse<List<NaverBlogDTO>> getNaverBlogPost(
            @RequestHeader("Authorization") String token,
            @PathVariable(value = "course_id")Long course_id
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(courseService.getNaverBlogs(course_id));
    }

    @Operation(summary = "코스 완주하기")
    @PostMapping(value = "/complete/{course_id}")
    public ApiResponse<LogIdDTO> completeCourse(
            @RequestHeader("Authorization") String token,
            @PathVariable(value = "course_id") Long course_id
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(LogIdDTO.builder()
                .logId(courseService.completeCourse(tokenWithoutBearer, course_id)).build());
    }

    @Operation(summary = "지역 코드 리스트 반환")
    @GetMapping(value = "/area_code")
    public ApiResponse<List<AreaCodeDTO>> getAreaCodes(
            @RequestHeader("Authorization") String token
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(areaService.getAreaCodes());
    }

    @Operation(summary = "저장한 코스 조회")
    @GetMapping(value = "/save_list")
    public ApiResponse<List<CourseResponseDTO>> getSaveCourse(
            @RequestHeader("Authorization") String token
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(courseService.getSaveCourse(tokenWithoutBearer));
    }

    @Operation(summary = "완주한 코스 조회")
    @GetMapping(value = "/complete_course")
    public ApiResponse<List<CourseResponseDTO>> getCompleteCourse(
            @RequestHeader("Authorization") String token
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(courseService.getCompleteCourse(tokenWithoutBearer));
    }

    @Operation(summary = "최근 핫한 코스 조회")
    @GetMapping(value = "/hot")
    public ApiResponse<List<CourseResponseDTO>> getHotCourses(
            @RequestHeader("Authorization") String token
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(courseService.getHotCourses(tokenWithoutBearer));
    }

//    @Operation(summary = "코스 검색 api")
//    @GetMapping(value = "/search/{keyword}")
//    public ApiResponse<List<CourseResponseDTO>> searchCourse(
//            @RequestHeader("Authorization") String token,
//            @PathVariable(value = "keyword") String keyword
//    ) {
//        String tokenWithoutBearer = token.substring(7);
//        return ApiResponse.onSuccess(courseService.getCourseByKeyowrd(tokenWithoutBearer, keyword));
//    }

    @Operation(summary = "최근 확인한 코스")
    @GetMapping(value = "/recent")
    public ApiResponse<List<CourseResponseDTO>> getRecentCourse(
            @RequestHeader("Authorization") String token
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(courseService.getRecentCourse(tokenWithoutBearer));
    }

    @Operation(summary = "시군구 별 코스 조회")
    @GetMapping(value = "/sigungu/{sigungu_id}")
    public ApiResponse<List<CourseResponseDTO>> getSigunguCourse(
            @RequestHeader("Authorization") String token,
            @PathVariable("sigungu_id") Long sigunguId
    ) {
        String tokenWithoutBearer = token.substring(7);
        return ApiResponse.onSuccess(courseService.getSigunguCourse(tokenWithoutBearer, sigunguId));
    }

//    @Operation( summary = "이미지 업로드 테스트")
//    @PostMapping(value = "/s3/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> s3Upload(@RequestParam(value = "image") MultipartFile image) {
//        String profileImage = s3ImageService.upload(image);
//        return ResponseEntity.ok(profileImage);
//    }


}
