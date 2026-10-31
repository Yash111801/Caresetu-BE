package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.entity.UserAccount;
import in.gov.npncd.caresetu.repository.PatientRepository;
import in.gov.npncd.caresetu.repository.UserAccountRepository;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.util.List;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import in.gov.npncd.caresetu.exception.ApiException;

@Service
public class ScopeService {

    private final UserAccountRepository userAccountRepository;
    private final PatientRepository patientRepository;

    public ScopeService(UserAccountRepository userAccountRepository, PatientRepository patientRepository) {
        this.userAccountRepository = userAccountRepository;
        this.patientRepository = patientRepository;
    }

    public UserAccount requireUser(UserPrincipal principal) {
        return userAccountRepository.findByPublicIdAndActiveTrue(principal.getPublicId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "User not found"));
    }

    public String primaryRole(UserPrincipal principal) {
        Set<String> roles = principal.getRoles();
        for (String preferred : List.of("ADMIN", "NAT", "SNO", "DIST", "BLK", "ONCO", "PATH", "HOSP", "MO", "CHO", "ASHA")) {
            if (roles.contains(preferred)) {
                return preferred;
            }
        }
        return roles.stream().findFirst().orElse("ASHA");
    }

    public List<Patient> patientsInScope(UserPrincipal principal) {
        UserAccount user = requireUser(principal);
        String role = primaryRole(principal);
        return switch (role) {
            case "ASHA", "CHO", "MO" -> patientRepository.findByPhcCodeAndActiveTrueOrderByUpdatedAtDesc(user.getPhcCode());
            case "HOSP", "PATH" -> patientRepository.findByActiveTrueOrderByUpdatedAtDesc().stream()
                    .filter(p -> user.getFacilityCode() != null && (
                            user.getFacilityCode().equals(p.getAssignedFacCode())
                                    || user.getFacilityCode().equals(p.getTestFacCode())))
                    .toList();
            case "ONCO" -> patientRepository.findByAssignedFacCodeAndActiveTrueOrderByUpdatedAtDesc("RCC");
            case "BLK" -> patientRepository.findByDistrictAndActiveTrueOrderByUpdatedAtDesc(user.getDistrict()).stream()
                    .filter(p -> user.getTaluka() == null || user.getTaluka().equalsIgnoreCase(p.getTaluka()))
                    .toList();
            case "DIST" -> patientRepository.findByDistrictAndActiveTrueOrderByUpdatedAtDesc(
                    user.getDistrict() != null ? user.getDistrict() : "Gandhinagar");
            case "SNO" -> patientRepository.findByStateCodeAndActiveTrueOrderByUpdatedAtDesc(
                    user.getStateCode() != null ? user.getStateCode() : "GJ");
            case "NAT", "ADMIN" -> patientRepository.findByActiveTrueOrderByUpdatedAtDesc();
            default -> patientRepository.findByActiveTrueOrderByUpdatedAtDesc();
        };
    }

    public Patient requirePatientInScope(UserPrincipal principal, String publicId) {
        Patient patient = patientRepository.findByPublicIdAndActiveTrue(publicId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found"));
        boolean allowed = patientsInScope(principal).stream().anyMatch(p -> p.getId().equals(patient.getId()));
        // Hospital/path can also see referrals newly assigned to their facility even if list filter lags
        UserAccount user = requireUser(principal);
        String role = primaryRole(principal);
        if (!allowed && ("HOSP".equals(role) || "PATH".equals(role))
                && user.getFacilityCode() != null
                && user.getFacilityCode().equals(patient.getAssignedFacCode())) {
            allowed = true;
        }
        if (!allowed && Set.of("NAT", "ADMIN", "SNO", "DIST").contains(role)) {
            allowed = true;
        }
        if (!allowed) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Patient outside your scope");
        }
        return patient;
    }
}
