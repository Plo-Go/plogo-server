package plogo.plogoserver.web.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PreferenceRequestBody {
    private List<String> firstKeyword;
    private List<String> secondKeyword;
    private List<String> thirdKeyword;
}
