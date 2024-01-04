package assetpricingregistry;

import assetdatabase.*;
import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class AssetPricingRepositoryTests {
    private Random random = new Random();
    private Connection connection;
    private SessionFactory sessionFactory;
    private AssetPricingRepositoryImplementation repository;

    @BeforeEach
    public void connect() throws Exception {
        final var database = "jdbc:hsqldb:mem:test-" + UUID.randomUUID();
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
                .addAnnotatedClass(AssetPricingRecord.class)
                .addAnnotatedClass(PriceScheduleRecord.class)
                .buildMetadata();

        sessionFactory = metadata.buildSessionFactory();
        repository = new AssetPricingRepositoryImplementation();
    }

    @AfterEach
    public void close() throws Throwable {
        sessionFactory.close();
        connection.close();
    }

    @Test
    public void initialSaveForNewAsset() {
        final var id = UUID.randomUUID().toString();
        final var savedTable = new ArrayList<PriceScheduleRecord>();
        addScheduleToList(savedTable);
        addScheduleToList(savedTable);

        inSession(session -> {
            repository.setPriceSchedules(session, id, savedTable);
        });

        final var retrievedTable = inSession(session -> {
            return repository.getPriceTable(session, id);
        });

        assertPricingSchedulesAreEquivalent(retrievedTable, savedTable);
    }

    @Test
    public void preservesIdOfExistingSchedules() {
        final var id = UUID.randomUUID().toString();
        final var savedTable = new ArrayList<PriceScheduleRecord>();
        final var pricingCode = UUID.randomUUID().toString();
        final var scheduleId = UUID.randomUUID().toString();
        addScheduleToList(savedTable, pricingCode, scheduleId);

        inAutoCommitSession(session -> {
            final var priceScheduleRecord = getPriceScheduleRecord(scheduleId, pricingCode);
            session.saveOrUpdate(priceScheduleRecord);
            final var asset = new AssetPricingRecord();
            asset.setId(id);
            asset.setPricingRecords(Set.of(priceScheduleRecord));
            session.saveOrUpdate(asset);
        });

        inSession(session -> {
            repository.setPriceSchedules(session, id, savedTable);
        });

        final var retrievedTable = inSession(session -> {
            return repository.getPriceTable(session, id);
        });

        assertPricingSchedulesAreEquivalent(retrievedTable, savedTable);
        assertThat(savedTable.get(0).getId(), equalTo(scheduleId));
    }

    @Test
    public void destroysRemovedAssets() {
        final var id = UUID.randomUUID().toString();
        final var savedTable = new ArrayList<PriceScheduleRecord>();
        final var pricingCode = UUID.randomUUID().toString();
        final var scheduleId = UUID.randomUUID().toString();
        addScheduleToList(savedTable);

        inAutoCommitSession(session -> {
            final var priceScheduleRecord = getPriceScheduleRecord(scheduleId, pricingCode);
            session.saveOrUpdate(priceScheduleRecord);
            final var asset = new AssetPricingRecord();
            asset.setId(id);
            asset.setPricingRecords(Set.of(priceScheduleRecord));
            session.saveOrUpdate(asset);
        });

        inSession(session -> {
            repository.setPriceSchedules(session, id, savedTable);
        });

        final var retrievedTable = inSession(session -> {
            return repository.getPriceTable(session, id);
        });

        assertPricingSchedulesAreEquivalent(retrievedTable, savedTable);
    }

    private PriceScheduleRecord getPriceScheduleRecord(String scheduleId, String pricingCode) {
        final var schedule = new PriceScheduleRecord();
        schedule.setId(scheduleId);
        schedule.setPricingCode(pricingCode);
        schedule.setStartDate(random.nextLong());
        schedule.setEndDate(random.nextLong());
        schedule.setIsLocked(random.nextInt(2) == 0);
        return schedule;
    }

    private void addScheduleToList(ArrayList<PriceScheduleRecord> savedTable) {
        final var id = UUID.randomUUID().toString();
        addScheduleToList(savedTable, id);
    }

    private void addScheduleToList(ArrayList<PriceScheduleRecord> savedTable, String pricingCode) {
        addScheduleToList(savedTable, pricingCode, null);
    }

    private void addScheduleToList(ArrayList<PriceScheduleRecord> savedTable, String pricingCode, String id) {
        savedTable.add(getPriceScheduleRecord(id, pricingCode));
    }

    private void assertPricingSchedulesAreEquivalent(Collection<PriceScheduleRecord> actual, Collection<PriceScheduleRecord> expected) {
        assertThat(actual.size(), equalTo(expected.size()));

        for (var expectedSchedule : expected)
            assertThat(
                    actual.stream().filter(actualSchedule -> pricingSchedulesAreEqual(expectedSchedule, actualSchedule)).count(),
                    equalTo(1L));
    }

    private boolean pricingSchedulesAreEqual(PriceScheduleRecord expectedSchedule, PriceScheduleRecord actualSchedule) {
        return Objects.equals(actualSchedule.getPricingCode(), expectedSchedule.getPricingCode()) &&
                Objects.equals(actualSchedule.getId(), expectedSchedule.getId()) &&
                Objects.equals(actualSchedule.getStartDate(), expectedSchedule.getStartDate()) &&
                Objects.equals(actualSchedule.getEndDate(), expectedSchedule.getEndDate()) &&
                Objects.equals(actualSchedule.getIsLocked(), expectedSchedule.getIsLocked());
    }

    private interface SessionConsumerWithReturn<T> {
        T run(Session session);
    }

    private interface SessionConsumer {
        void run(Session session);
    }

    private void inAutoCommitSession(SessionConsumer action) {
        inSession(session -> {
            final var transaction = session.beginTransaction();

            action.run(session);

            transaction.commit();
        });
    }

    private <T> T inSession(SessionConsumerWithReturn<T> decorated) {
        try (final var session = sessionFactory.openSession()) {
            return decorated.run(session);
        }
    }

    private void inSession(SessionConsumer decorated) {
        inSession(session -> {
            decorated.run(session);
            return (Void) null;
        });
    }
}
