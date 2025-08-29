package plogo.plogoserver.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import plogo.plogoserver.domain.Sigungu;

public interface SigunguRepository extends JpaRepository<Sigungu, Long> {
    List<Sigungu> findByAreaCodeAndSigunguCode(Long areaCode, Long sigunguCode);
    boolean existsByAreaCodeAndSigunguCode(int areaCode, int sigunguCode);
}
