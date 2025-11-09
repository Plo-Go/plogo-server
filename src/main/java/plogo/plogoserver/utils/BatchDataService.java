package plogo.plogoserver.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import plogo.plogoserver.domain.Sigungu;
import plogo.plogoserver.repository.SigunguRepository;
import plogo.plogoserver.service.CourseService;
import plogo.plogoserver.utils.SigunguResponse.Item;

@Service
@RequiredArgsConstructor
public class BatchDataService {

    private final TourApi tourApi;
    private final SigunguRepository sigunguRepository;
    private final ObjectMapper objectMapper;
    private final CourseService courseService;

    // ✅ [1단계] 전국 시군구 저장
//    public void saveAllSigunguCodes() {
//        // 전국 지역 코드 (areaCode)
//        int[] areaCodes = {1, 31, 2, 32, 3, 8, 34, 33, 6, 7, 36, 35, 4, 5, 38, 37, 39};
//
//        for (int areaCode : areaCodes) {
//            try {
//                // API 호출
//                String sigunguJson = tourApi.getSiCode(areaCode);
//                SigunguResponse sigunguResponse = objectMapper.readValue(sigunguJson, SigunguResponse.class);
//
//                // 응답 파싱
//                SigunguResponse.Body body = sigunguResponse.getResponse().getBody();
//
//                // 데이터 없으면 스킵
//                if (body.getItems() == null || body.getItems().getItem() == null) {
//                    System.out.println("⚠️ No sigungu data for areaCode=" + areaCode);
//                    continue;
//                }
//
//                List<Item> sigunguItems = body.getItems().getItem();
//
//                for (SigunguResponse.Item item : sigunguItems) {
//                    int sigunguCode = Integer.parseInt(item.getCode());
//                    String sigunguName = item.getName();
//
//                    // 중복 저장 방지
//                    if (!sigunguRepository.existsByAreaCodeAndSigunguCode(areaCode, sigunguCode)) {
//                        Sigungu sigungu = Sigungu.builder()
//                                .areaCode(areaCode)
//                                .sigunguCode(sigunguCode)
//                                .sigunguName(sigunguName)
//                                .build();
//
//                        sigunguRepository.save(sigungu);
//                        System.out.println("✅ 시군구 저장 완료 → " + sigunguName + " (" + areaCode + ", " + sigunguCode + ")");
//                    } else {
//                        System.out.println("⚠️ 이미 존재 → " + sigunguName + " (" + areaCode + ", " + sigunguCode + ")");
//                    }
//                }
//
//            } catch (Exception e) {
//                System.err.println("❌ 시군구 저장 실패 → areaCode=" + areaCode);
//                e.printStackTrace();
//            }
//        }
//    }

    // ✅ [2단계] (현재 주석 처리) 시군구별 코스 저장
    public void saveAllCourses() {
        List<Sigungu> sigungus = sigunguRepository.findAll();
        System.out.println("📋 시군구 개수: " + sigungus.size());

        for (Sigungu sigungu : sigungus) {
            try {
                courseService.saveSigunguCourses(sigungu.getAreaCode(), sigungu.getSigunguCode());
                Thread.sleep(1000);
                System.out.println("✅ 코스 저장 완료 → " + sigungu.getSigunguName());
            } catch (Exception e) {
                System.err.println("❌ 코스 저장 실패 → " + sigungu.getSigunguName());
            }
        }
    }

    // ✅ 전체 실행
    public void runSaveAll() {
        System.out.println("🚀 Batch Job Started...");

        // 1단계: 시군구 코드 저장
        //saveAllSigunguCodes();

        // 2단계: 코스 저장
        try {
            Thread.sleep(3000);
            saveAllCourses();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("✅ Batch Job Completed!");
    }

}
