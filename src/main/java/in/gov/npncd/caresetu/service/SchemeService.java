package in.gov.npncd.caresetu.service;

import in.gov.npncd.caresetu.domain.entity.Patient;
import in.gov.npncd.caresetu.domain.entity.Scheme;
import in.gov.npncd.caresetu.dto.patient.EligibilityRequest;
import in.gov.npncd.caresetu.repository.SchemeRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchemeService {

    private final SchemeRepository schemeRepository;

    public SchemeService(SchemeRepository schemeRepository) {
        this.schemeRepository = schemeRepository;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listSchemes() {
        return schemeRepository.findByActiveTrueOrderByNameAsc().stream().map(this::toMap).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> evaluate(EligibilityRequest req) {
        List<Scheme> all = schemeRepository.findByActiveTrueOrderByNameAsc();
        List<Map<String, Object>> eligible = new ArrayList<>();
        List<Map<String, Object>> maybe = new ArrayList<>();

        int age = req.age() == null ? 40 : req.age();
        boolean bplLike = "bpl".equalsIgnoreCase(nullSafe(req.rationCard()))
                || "low".equalsIgnoreCase(nullSafe(req.incomeBand()));
        boolean pmjay = Boolean.TRUE.equals(req.hasPmjayCard()) || bplLike;

        for (Scheme s : all) {
            Map<String, Object> row = toMap(s);
            switch (s.getCode()) {
                case "free" -> eligible.add(row);
                case "pmjay" -> {
                    if (pmjay) eligible.add(row);
                    else maybe.add(row);
                }
                case "vay" -> {
                    if (age >= 70) eligible.add(row);
                    else maybe.add(row);
                }
                case "hmcpf" -> {
                    if (bplLike) eligible.add(row);
                    else maybe.add(row);
                }
                case "esic" -> {
                    if (Boolean.TRUE.equals(req.esic())) eligible.add(row);
                    else maybe.add(row);
                }
                case "govt" -> {
                    if (Boolean.TRUE.equals(req.govtEmployee())) eligible.add(row);
                    else maybe.add(row);
                }
                case "insurance" -> {
                    if (Boolean.TRUE.equals(req.privateInsurance())) eligible.add(row);
                    else maybe.add(row);
                }
                default -> maybe.add(row);
            }
        }

        Map<String, Object> out = new HashMap<>();
        out.put("eligible", eligible);
        out.put("possible", maybe);
        out.put("helplines", List.of(
                Map.of("name", "Ambulance", "number", "108"),
                Map.of("name", "Health helpline", "number", "104"),
                Map.of("name", "PM-JAY", "number", "14555")));
        return out;
    }

    public EligibilityRequest fromPatient(Patient p) {
        return new EligibilityRequest(
                p.getIncomeBand(),
                p.getRationCard(),
                p.getHasPmjayCard(),
                p.isEsic(),
                p.isGovtEmployee(),
                p.isPrivateInsurance(),
                p.getAge(),
                p.getCancerType() != null ? p.getCancerType().name() : null);
    }

    private Map<String, Object> toMap(Scheme s) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", s.getCode());
        m.put("name", s.getName());
        m.put("shortName", s.getShortName());
        m.put("level", s.getLevelLabel());
        m.put("cover", s.getCoverText());
        m.put("what", s.getWhatText());
        m.put("how", s.getHowText());
        return m;
    }

    private static String nullSafe(String v) {
        return v == null ? "" : v;
    }
}
