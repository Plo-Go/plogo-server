package plogo.plogoserver.web.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NaverBlogDTO {
    private String title;
    private String link;
    private String summary;
    private String blog_name;
    private String post_date;
}
