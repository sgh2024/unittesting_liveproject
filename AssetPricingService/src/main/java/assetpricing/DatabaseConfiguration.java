package assetpricing;

import assetpricingregistry.*;
import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class DatabaseConfiguration {
    @Bean
    public SessionFactory createSessionFactory(
            @Value("${assetpricing.connection.url}") String url,
            @Value("${assetpricing.connection.user}") String user,
            @Value("${assetpricing.connection.password}") String password) {
        final var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.user", user)
                .applySetting("hibernate.connection.password", password)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .applySetting("hibernate.connection.autocommit", true)
                .applySetting("hibernate.show_sql", true)
                .build();
        final var metadata = new MetadataSources(registry)
                .addAnnotatedClass(AssetPricingRecord.class)
                .addAnnotatedClass(PriceScheduleRecord.class)
                .buildMetadata();

        return metadata.buildSessionFactory();
    }
}
