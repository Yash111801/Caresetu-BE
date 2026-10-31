package in.gov.npncd.caresetu.dto.facility;

import java.time.LocalDate;

public record PoolDayDto(LocalDate date, int used, int capacity, int left, boolean open) {}
