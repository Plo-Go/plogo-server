package plogo.plogoserver.converter;

import plogo.plogoserver.web.dto.response.SearchLogDTO;

public class SearchLogConverter {

    public static SearchLogDTO toDTO(String keyword) {
        return SearchLogDTO.builder()
                .keyword(keyword)
                .build();
    }
}
