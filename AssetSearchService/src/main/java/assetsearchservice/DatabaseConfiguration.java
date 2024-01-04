package assetsearchservice;

import assetmetadataregistry.*;
import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class DatabaseConfiguration {
    @Bean
    public SessionFactory createSessionFactory(
            @Value("${assetsearch.connection.url}") String url,
            @Value("${assetsearch.connection.user}") String user,
            @Value("${assetsearch.connection.password}") String password,
            @Value("${assetsearch.connection.dialect}") String dialect) {
        final var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.user", user)
                .applySetting("hibernate.connection.password", password)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .applySetting("hibernate.connection.autocommit", true)
                .applySetting("hibernate.show_sql", true)
                .applySetting("hibernate.dialect", dialect)
                .build();
        final var metadata = new MetadataSources(registry)
                .addAnnotatedClass(AssetMetadataRecord.class)
                .buildMetadata();

        return metadata.buildSessionFactory();
    }
}
