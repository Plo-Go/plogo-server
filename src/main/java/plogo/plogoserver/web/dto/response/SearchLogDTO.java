package plogo.plogoserver.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchLogDTO {
    private String keyword;
}
