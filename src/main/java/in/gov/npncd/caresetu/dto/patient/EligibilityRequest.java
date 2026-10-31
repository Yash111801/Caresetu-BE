package in.gov.npncd.caresetu.dto.patient;

public record EligibilityRequest(
        String incomeBand,
        String rationCard,
        Boolean hasPmjayCard,
        Boolean esic,
        Boolean govtEmployee,
        Boolean privateInsurance,
        Integer age,
        String cancerType
) {
}
