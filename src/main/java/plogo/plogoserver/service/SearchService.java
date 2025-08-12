package plogo.plogoserver.service;

import footlogger.footlog.converter.SearchLogConverter;
import footlogger.footlog.domain.User;
import footlogger.footlog.repository.UserRepository;
import footlogger.footlog.web.dto.response.SearchLogDTO;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import plogo.plogoserver.domain.User;
import plogo.plogoserver.repository.UserRepository;
import plogo.plogoserver.web.dto.response.SearchLogDTO;


@Service
@RequiredArgsConstructor
public class SearchService {
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    //검색어 저장 기능
    public void saveRecentSearchLog(User user, String keyword) {

        //key값 : SearchLog + 유저 id 값
        String key = "SearchLog" + user.getId();

        redisTemplate.opsForList().remove(key, 1, keyword);

        Long size = redisTemplate.opsForList().size(key);

        //10개를 넘을 경우 가장 오래된 데이터 삭제
        if(size == 10) {
            redisTemplate.opsForList().rightPop(key);
        }

        redisTemplate.opsForList().leftPush(key, keyword);
    }

    //검색 기록 조회
    public List<SearchLogDTO> getRecentSearchLogs(String token) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String key = "SearchLog" + user.getId();

        List<String> logs = redisTemplate.opsForList().range(key, 0, 10);

        return logs.stream()
                .map(SearchLogConverter::toDTO)
                .collect(Collectors.toList());
    }

    public Long deleteRecentSearchLog(String token, String keyword) {
        User user = userRepository.findByAccessToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String key = "SearchLog" + user.getId();

        long count = redisTemplate.opsForList().remove(key, 1, keyword);

        if(count == 0) {
            throw new IllegalArgumentException("검색어가 존재하지 않습니다.");
        }

        return count;
    }
}
