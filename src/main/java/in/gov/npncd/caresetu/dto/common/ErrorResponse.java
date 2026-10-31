package in.gov.npncd.caresetu.dto.common;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        boolean success,
        String message,
        String code,
        List<FieldErrorDto> errors,
        Instant timestamp
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(false, message, code, null, Instant.now());
    }

    public static ErrorResponse of(String code, String message, List<FieldErrorDto> errors) {
        return new ErrorResponse(false, message, code, errors, Instant.now());
    }

    public record FieldErrorDto(String field, String message) {
    }
}
