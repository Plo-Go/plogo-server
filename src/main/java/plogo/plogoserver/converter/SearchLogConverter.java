package plogo.plogoserver.converter;

import footlogger.footlog.web.dto.response.SearchLogDTO;
import plogo.plogoserver.web.dto.response.SearchLogDTO;

public class SearchLogConverter {

    public static SearchLogDTO toDTO(String keyword) {
        return SearchLogDTO.builder()
                .keyword(keyword)
                .build();
    }
}
