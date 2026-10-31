package in.gov.npncd.caresetu.repository;

import in.gov.npncd.caresetu.domain.entity.UserAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByMobileAndActiveTrue(String mobile);

    Optional<UserAccount> findByPublicIdAndActiveTrue(String publicId);

    boolean existsByMobile(String mobile);
}
