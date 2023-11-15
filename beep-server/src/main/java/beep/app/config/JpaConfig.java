package beep.app.config;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "beep.app.data")
public class JpaConfig {
    // Configuration
}

