package plogo.plogoserver.payload.code.status;

import plogo.plogoserver.payload.code.BaseCode;
import plogo.plogoserver.payload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    _OK("200", "Operation successful");

    private final String code;
    private final String message;

    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .message(message)
                .code(code)
                .build();
    }

}
