package website.tma.mouserec.configs;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    @Value("${spring.flyway.url}")
    private String url;
    @Value("${spring.flyway.username}")
    private String username;
    @Value("${spring.flyway.password}")
    private String password;
    @Value("${spring.flyway.schemas}")
    private String schema;
    @Value("${spring.flyway.locations}")
    private String location;

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        return new Flyway(Flyway
                .configure()
                .dataSource(url, username, password)
                .schemas(schema)
                .locations(location)
        );
    }

}
