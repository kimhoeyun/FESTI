package com.example.FESTI.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;
import java.time.Clock;

@Configuration
@EnableJpaAuditing
@EnableConfigurationProperties(AuthProperties.class)
public class AppConfig {

    @Bean
    public Clock appClock() {
        return Clock.systemUTC();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    @Bean(initMethod = "migrate")
    @ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flyway(DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .load();
    }
}
