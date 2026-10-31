package in.gov.npncd.caresetu.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionTest {

    @Test
    void carriesStatusCodeAndMessage() {
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("PATIENT_NOT_FOUND", ex.getCode());
        assertEquals("Patient not found", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }
}
