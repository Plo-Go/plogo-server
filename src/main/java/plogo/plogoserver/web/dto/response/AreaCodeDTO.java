package plogo.plogoserver.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AreaCodeDTO {
    private Long areaCode;
    private String areaName;
}
