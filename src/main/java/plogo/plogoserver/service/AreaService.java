package plogo.plogoserver.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import plogo.plogoserver.domain.Sigungu;
import plogo.plogoserver.repository.SigunguRepository;
import plogo.plogoserver.web.dto.response.AreaCodeDTO;
import plogo.plogoserver.web.dto.response.SigunguCodeDTO;

@Service
@RequiredArgsConstructor
public class AreaService {
    private final AreaConverter areaConverter;
    private final SigunguRepository sigunguRepository;

    public List<AreaCodeDTO> getAreaCodes() {
        return areaConverter.getAreaCodeDTOByCodes();
    }

    public List<SigunguCodeDTO> getSigunguInfo() {
        List<Sigungu> sigungus = sigunguRepository.findAll();

        return sigungus.stream()
                .map(areaConverter::getSigunguCodeDTOByCodes)
                .toList();
    }
}
