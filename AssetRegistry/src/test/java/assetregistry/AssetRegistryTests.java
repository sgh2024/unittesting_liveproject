package assetregistry;

import assetdatabase.*;
import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AssetRegistryTests {
    private static Connection connection;
    private static SessionFactory sessionFactory;
    private AssetRepositoryImplementation repository;
    private Session session;

    @BeforeAll
    public static void connect() throws Exception {
        final var database = "jdbc:hsqldb:mem:test";
        final var user = "SA";
        final var password = "";
        connection = DriverManager.getConnection(database, user, password);
        final var databaseDefinition = new AssetDatabase(connection);
        databaseDefinition.latest();

        final var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", database)
                .applySetting("hibernate.connection.user", user)
                .applySetting("hibernate.connection.password", password)
                .applySetting("hibernate.hbm2ddl.auto", "validate")
                .build();
        final var metadata = new MetadataSources(registry)
                .addAnnotatedClass(AssetRecord.class)
                .buildMetadata();

        sessionFactory = metadata.buildSessionFactory();
    }

    @AfterAll
    public static void disconnect() throws Exception {
        connection.close();
    }

    @BeforeEach
    void setUp() {
        repository = new AssetRepositoryImplementation();
        session = sessionFactory.openSession();
    }

    @Test
    public void providesAGuid() {
        final var result = provisionAsset("https://some/big.jpg");

        assertNotNull(UUID.fromString(result));
    }

    @Test
    public void providesUniqueIdentities() {
        final var firstId = provisionAsset("https://some/big.jpg");

        final var secondId = provisionAsset("https://some.other/big.jpg");

        assertNotEquals(firstId, secondId);
    }

    @Test
    public void recordContainsUrl() {
        final var fullResolutionUrl = "something.jpg";
        final var id = provisionAsset(fullResolutionUrl);

        final var record = session.get(AssetRecord.class, id);

        assertEquals(fullResolutionUrl, record.getFullUrl());
    }

    @Test
    public void addsDefaultTitles() {
        final var id = provisionAsset("something.jpg");

        final var record = session.get(AssetRecord.class, id);

        assertEquals("Coming soon...", record.getTitleEnglish());
        assertEquals("À venir...", record.getTitleFrench());
        assertEquals("Demnächst...", record.getTitleGerman());
        assertEquals("Próximamente...", record.getTitleSpanish());
    }

    private String provisionAsset(String s) {
        final var transaction = session.beginTransaction();
        final var result = repository.provisionAsset(session, s);
        transaction.commit();
        session.clear();

        return result;
    }
}
