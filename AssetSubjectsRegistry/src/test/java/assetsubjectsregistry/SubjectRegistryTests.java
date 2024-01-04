package assetsubjectsregistry;

import assetdatabase.*;
import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.*;
import java.util.stream.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class SubjectRegistryTests {
    private Connection connection;
    private SessionFactory sessionFactory;
    private AssetSubjectsRepositoryImplementation repository;

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
                .addAnnotatedClass(AssetSubjectsRecord.class)
                .addAnnotatedClass(SubjectRecord.class)
                .buildMetadata();

        sessionFactory = metadata.buildSessionFactory();
        repository = new AssetSubjectsRepositoryImplementation();
    }

    @AfterEach
    public void closeConnection() throws Exception {
        connection.close();
        sessionFactory.close();
    }

    @Test
    public void setSubjectsCreatesNewSubjects() {
        final String assetId = preExistingAssetId();
        final var subject1 = getSubjectRecord("some-id", "Some Title");
        final var subject2 = getSubjectRecord("some-other-id", "Some Other Title");

        associateSubjects(assetId, subject1, subject2);

        assetShouldHaveSubjects(assetId, subject1, subject2);
    }

    @Test
    public void createsAssetIfNeedBe() {
        final String assetId = UUID.randomUUID().toString();
        final var subject1 = getSubjectRecord("some-id", "Some Title");
        final var subject2 = getSubjectRecord("some-other-id", "Some Other Title");

        associateSubjects(assetId, subject1, subject2);

        assetShouldHaveSubjects(assetId, subject1, subject2);
    }

    @Test
    public void setSubjectsCreatesUpdatesExistingSubjects() {
        final String assetId = preExistingAssetId();
        final var subjectId = "some-id";
        final var oldSubject = getSubjectRecord(subjectId, "old title");
        final var newSubject = getSubjectRecord(subjectId, "some other title");
        saveSubject(oldSubject);

        associateSubjects(assetId, newSubject);

        assetShouldHaveSubjects(assetId, newSubject);
    }

    private void saveSubject(SubjectRecord oldSubject) {
        try (final var session = sessionFactory.openSession()) {
            final var transaction = session.beginTransaction();
            session.save(oldSubject);
            transaction.commit();
        }
    }

    private void assetShouldHaveSubjects(String assetId, SubjectRecord... expected) {
        try (final var session = sessionFactory.openSession()) {
            final var asset = session.find(AssetSubjectsRecord.class, assetId);

            assertThat(getComparableSubjects(asset.getSubjects()), equalTo(getComparableSubjects(Arrays.asList(expected))));
        }
    }

    private void associateSubjects(String assetId, SubjectRecord... subjectRecords) {
        try (final var session = sessionFactory.openSession()) {
            repository.setSubjects(session, assetId, subjectRecords);
        }
    }

    private String preExistingAssetId() {
        final var assetId = UUID.randomUUID().toString();

        try (final var session = sessionFactory.openSession()) {
            final var transaction = session.beginTransaction();
            final var asset = new AssetSubjectsRecord();
            asset.setId(assetId);
            session.save(asset);

            transaction.commit();
        }
        return assetId;
    }

    private Set<String> getComparableSubjects(Collection<SubjectRecord> subjects) {
        return subjects.stream().map(s -> s.getId() + ":" + s.getTitle()).collect(Collectors.toSet());
    }

    private SubjectRecord getSubjectRecord(String subjectId, String subjectTitle) {
        final var result = new SubjectRecord();
        result.setId(subjectId);
        result.setTitle(subjectTitle);
        return result;
    }
}
