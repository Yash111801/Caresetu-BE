package in.gov.npncd.caresetu.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.entity.Permission;
import in.gov.npncd.caresetu.domain.entity.Role;
import in.gov.npncd.caresetu.domain.entity.UserAccount;
import in.gov.npncd.caresetu.domain.enums.CancerType;
import in.gov.npncd.caresetu.domain.enums.PatientStage;
import in.gov.npncd.caresetu.domain.enums.PresentationType;
import in.gov.npncd.caresetu.domain.enums.RiskLevel;
import in.gov.npncd.caresetu.dto.patient.WorkActionDto;
import in.gov.npncd.caresetu.security.UserPrincipal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorklistServiceTest {

    @Mock ScopeService scopeService;
    @Mock MissedVisitService missedVisitService;

    WorklistService worklistService;

    @BeforeEach
    void setUp() {
        worklistService = new WorklistService(scopeService, missedVisitService);
    }

    @Test
    void ashaSeesMonitorActionOnMonitorStage() {
        Patient patient = basePatient();
        patient.setStage(PatientStage.MONITOR);
        UserPrincipal principal = principal("ASHA");
        when(scopeService.primaryRole(principal)).thenReturn("ASHA");
        when(missedVisitService.missStatus(anyLong())).thenReturn(Map.of());

        List<WorkActionDto> actions = worklistService.actionsFor(patient, principal);
        assertTrue(actions.stream().anyMatch(a -> "monitor".equals(a.act())));
    }

    @Test
    void hospSeesAttendAndCantServeOnBookedDueVisit() {
        Patient patient = basePatient();
        patient.setStage(PatientStage.BOOKED);
        patient.setVisitAt(java.time.LocalDate.now().minusDays(1));
        UserPrincipal principal = principal("HOSP");
        when(scopeService.primaryRole(principal)).thenReturn("HOSP");
        when(missedVisitService.missStatus(anyLong())).thenReturn(Map.of());

        List<WorkActionDto> actions = worklistService.actionsFor(patient, principal);
        assertTrue(actions.stream().anyMatch(a -> "attend".equals(a.act())));
        assertTrue(actions.stream().anyMatch(a -> "cantserve".equals(a.act())));
    }

    private static Patient basePatient() {
        Patient p = new Patient();
        p.setId(1L);
        p.setPublicId("p1");
        p.setUhid("CS-GNR-0001");
        p.setFullName("Test");
        p.setAge(40);
        p.setSex("F");
        p.setPhcCode("ADL");
        p.setCancerType(CancerType.oral);
        p.setPresentation(PresentationType.symptomatic);
        p.setRiskLevel(RiskLevel.MEDIUM);
        p.setActive(true);
        return p;
    }

    private static UserPrincipal principal(String roleCode) {
        Permission perm = new Permission();
        perm.setCode("PATIENT_READ");
        Role role = new Role();
        role.setCode(roleCode);
        role.setPermissions(Set.of(perm));
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setPublicId("u1");
        user.setFullName("Test");
        user.setMobile("9876543210");
        user.setActive(true);
        user.setRoles(new HashSet<>(Set.of(role)));
        return new UserPrincipal(user);
    }
}
