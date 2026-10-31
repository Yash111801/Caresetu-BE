package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.OtpChallenge;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    Optional<OtpChallenge> findFirstByMobileAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String mobile, Instant now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OtpChallenge o set o.consumed = true where o.mobile = :mobile and o.consumed = false")
    int consumeAllActive(String mobile);
}
