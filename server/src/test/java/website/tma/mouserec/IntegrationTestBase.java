package website.tma.mouserec;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Profile("test")
@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class IntegrationTestBase {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:12-alpine");

    @Container
    private static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:5.4.3"));

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.username", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);

        registry.add("spring.r2dbc.url", () -> postgresContainer.getJdbcUrl()
                .replace("jdbc", "r2dbc")
                .concat("&currentSchema=mouserec")
        );
        registry.add("spring.r2dbc.username", postgresContainer::getUsername);
        registry.add("spring.r2dbc.password", postgresContainer::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @Configuration
    public static class DataSourceConfig {
        @Bean
        public DataSource dataSource() {
            DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.url(postgresContainer.getJdbcUrl());
            dataSourceBuilder.username(postgresContainer.getUsername());
            dataSourceBuilder.password(postgresContainer.getPassword());

            return dataSourceBuilder.build();
        }
    }

}
