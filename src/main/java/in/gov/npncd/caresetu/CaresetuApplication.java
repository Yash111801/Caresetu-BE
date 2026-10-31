package in.gov.npncd.caresetu;

import in.gov.npncd.caresetu.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
public class CaresetuApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaresetuApplication.class, args);
    }
}
