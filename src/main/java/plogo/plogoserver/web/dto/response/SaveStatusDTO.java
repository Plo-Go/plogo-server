package plogo.plogoserver.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SaveStatusDTO {
    private Boolean isSave;
}
