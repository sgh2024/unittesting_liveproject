package assetmetadataregistry;


import org.hibernate.*;
import org.hibernate.boot.*;
import org.hibernate.boot.registry.*;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.*;
import java.util.function.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class AssetMetadataRepositoryTests {
    private static final String newEnglishTitle = "new english title";
    private static final String newGermanTitle = "new german title";
    private static final String newSpanishTitle = "new spanish title";
    private static final String newFrenchTitle = "new french title";
    private static final String newEnglishDescription = "new english description";
    private static final String newGermanDescription = "new german description";
    private static final String newSpanishDescription = "new spanish description";
    private static final String newFrenchDescription = "new french description";
    private static final String newThumb64 = "new-thumb-64";
    private static final String newThumb128 = "new-thumb-128";
    private static final String newThumb256 = "new-thumb-256";
    private static final String newThumb512 = "new-thumb-512";

    private AssetMetadataRepository repository;
    private String assetId;
    private String oldEnglishTitle;
    private String oldGermanTitle;
    private String oldSpanishTitle;
    private String oldFrenchTitle;
    private String oldEnglishDescription;
    private String oldGermanDescription;
    private String oldSpanishDescription;
    private String oldFrenchDescription;
    private List<AssetMetadataRecord> found;

    private Connection connection;
    private SessionFactory sessionFactory;

    @BeforeEach
    public void setUp() throws Exception {
        assetId = anyString();
        final var database = "jdbc:hsqldb:mem:test-" + anyString() + ";DB_CLOSE_DELAY=-1";
        final var user = "SA";
        final var password = "";
        connection = DriverManager.getConnection(database, user, password);

        final var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", database)
                .applySetting("hibernate.connection.user", user)
                .applySetting("hibernate.connection.password", password)
                .applySetting("hibernate.hbm2ddl.auto", "create")
                .build();
        final var metadata = new MetadataSources(registry)
                .addAnnotatedClass(AssetMetadataRecord.class)
                .buildMetadata();

        sessionFactory = metadata.buildSessionFactory();
        repository = new AssetMetadataRepositoryImplementation();
    }

    @AfterEach
    public void tearDown() throws Exception {
        connection.close();
    }

    @Test
    public void modifyThumbnails() {
        setUpAssetRecord();

        setThumbnails(newThumb64, newThumb128, newThumb256, newThumb512);

        verifyThumbnails(newThumb64, newThumb128, newThumb256, newThumb512);
    }

    @Test
    public void createThumbs() {
        setThumbnails(newThumb64, newThumb128, newThumb256, newThumb512);

        verifyThumbnails(newThumb64, newThumb128, newThumb256, newThumb512);
    }

    @Test
    public void createRecordFromTitles() {
        setTitles(newEnglishTitle, newGermanTitle, newSpanishTitle, newFrenchTitle);

        verifyAssetTitles(newEnglishTitle, newGermanTitle, newSpanishTitle, newFrenchTitle);
    }

    @Test
    public void setAllTitles() {
        setUpAssetRecord();

        setTitles(newEnglishTitle, newGermanTitle, newSpanishTitle, newFrenchTitle);

        verifyAssetTitles(newEnglishTitle, newGermanTitle, newSpanishTitle, newFrenchTitle);
    }

    @Test
    public void retainUnchangedFrenchTitle() {
        setUpAssetRecord();

        setTitles(newEnglishTitle, newGermanTitle, newSpanishTitle, null);

        verifyAssetTitles(newEnglishTitle, newGermanTitle, newSpanishTitle, oldFrenchTitle);
    }

    @Test
    public void retainUnchangedSpanishTitle() {
        setUpAssetRecord();

        setTitles(newEnglishTitle, newGermanTitle, null, newFrenchTitle);

        verifyAssetTitles(newEnglishTitle, newGermanTitle, oldSpanishTitle, newFrenchTitle);
    }

    @Test
    public void retainUnchangedGermanTitle() {
        setUpAssetRecord();

        setTitles(newEnglishTitle, null, newSpanishTitle, newFrenchTitle);

        verifyAssetTitles(newEnglishTitle, oldGermanTitle, newSpanishTitle, newFrenchTitle);
    }

    @Test
    public void retainUnchangedEnglishTitle() {
        setUpAssetRecord();

        setTitles(null, newGermanTitle, newSpanishTitle, newFrenchTitle);

        verifyAssetTitles(oldEnglishTitle, newGermanTitle, newSpanishTitle, newFrenchTitle);
    }

    @Test
    public void createRecordFromDescriptions() {
        setDescriptions(newEnglishDescription, newGermanDescription, newSpanishDescription, newFrenchDescription);

        verifyAssetDescriptions(newEnglishDescription, newGermanDescription, newSpanishDescription, newFrenchDescription);
    }

    @Test
    public void setAllDescriptions() {
        setUpAssetRecord();

        setDescriptions(newEnglishDescription, newGermanDescription, newSpanishDescription, newFrenchDescription);

        verifyAssetDescriptions(newEnglishDescription, newGermanDescription, newSpanishDescription, newFrenchDescription);
    }

    @Test
    public void retainUnchangedFrenchDescription() {
        setUpAssetRecord();

        setDescriptions(newEnglishDescription, newGermanDescription, newSpanishDescription, null);

        verifyAssetDescriptions(newEnglishDescription, newGermanDescription, newSpanishDescription, oldFrenchDescription);
    }

    @Test
    public void retainUnchangedSpanishDescription() {
        setUpAssetRecord();

        setDescriptions(newEnglishDescription, newGermanDescription, null, newFrenchDescription);

        verifyAssetDescriptions(newEnglishDescription, newGermanDescription, oldSpanishDescription, newFrenchDescription);
    }

    @Test
    public void retainUnchangedGermanDescription() {
        setUpAssetRecord();

        setDescriptions(newEnglishDescription, null, newSpanishDescription, newFrenchDescription);

        verifyAssetDescriptions(newEnglishDescription, oldGermanDescription, newSpanishDescription, newFrenchDescription);
    }

    @Test
    public void retainUnchangedEnglishDescription() {
        setUpAssetRecord();

        setDescriptions(null, newGermanDescription, newSpanishDescription, newFrenchDescription);

        verifyAssetDescriptions(oldEnglishDescription, newGermanDescription, newSpanishDescription, newFrenchDescription);
    }

    @Test
    public void findByEnglishTitle() {
        setUpAssetRecord();
        final var searchTerm = anySubstringOf(oldEnglishTitle);

        whenSearch(searchTerm);

        thenAssetWasFound();
    }

    @Test
    public void cannotFindWithoutTerm() {
        setUpAssetRecord();

        whenSearch(UUID.randomUUID().toString());

        thenAssetWasNotFound();
    }

    private void thenAssetWasNotFound() {
        Assertions.assertEquals(0, timesAssetAppearsInResultsSet());
    }

    private void thenAssetWasFound() {
        Assertions.assertEquals(1, timesAssetAppearsInResultsSet());
    }

    private long timesAssetAppearsInResultsSet() {
        return found.stream().filter(asset -> Objects.equals(asset.getId(), assetId)).count();
    }

    private void whenSearch(String searchTerm) {
        inSession(session ->
                found = repository.search(session, searchTerm)
        );
    }

    private String anySubstringOf(String text) {
        final var startIndex = anyIntegerLessThan(text.length() - 1);
        final var minimumEndIndex = startIndex + 1;
        final var endIndex = minimumEndIndex + anyIntegerLessThan(text.length() - minimumEndIndex);
        return text.substring(startIndex, endIndex);
    }

    private Random random = new Random();

    private int anyIntegerLessThan(int upperExclusive) {
        return random.nextInt(upperExclusive);
    }

    private String anyString() {
        return UUID.randomUUID().toString();
    }

    private void verifyAssetDescriptions(String newEnglishDescription, String newGermanDescription, String newSpanishDescription, String newFrenchDescription) {
        withAssetRecord(assetId, record -> {
            assertThat(record.getDescriptionEnglish(), is(equalTo(newEnglishDescription)));
            assertThat(record.getDescriptionGerman(), is(equalTo(newGermanDescription)));
            assertThat(record.getDescriptionFrench(), is(equalTo(newFrenchDescription)));
            assertThat(record.getDescriptionSpanish(), is(equalTo(newSpanishDescription)));
        });
    }

    private void verifyAssetTitles(String newEnglishTitle, String newGermanTitle, String newSpanishTitle, String newFrenchTitle) {
        withAssetRecord(assetId, record -> {
            assertThat(record.getTitleEnglish(), is(equalTo(newEnglishTitle)));
            assertThat(record.getTitleGerman(), is(equalTo(newGermanTitle)));
            assertThat(record.getTitleFrench(), is(equalTo(newFrenchTitle)));
            assertThat(record.getTitleSpanish(), is(equalTo(newSpanishTitle)));
        });
    }

    private void setTitles(String newEnglishTitle, String newGermanTitle, String newSpanishTitle, String newFrenchTitle) {
        inTransactionalSession(session -> repository.setTitles(session, assetId, newEnglishTitle, newGermanTitle, newSpanishTitle, newFrenchTitle));
    }

    private void setDescriptions(String newEnglishDescription, String newGermanDescription, String newSpanishDescription, String newFrenchDescription) {
        inTransactionalSession(session -> repository.setDescriptions(session, assetId, newEnglishDescription, newGermanDescription, newSpanishDescription, newFrenchDescription));
    }

    private void setUpAssetRecord() {
        inTransactionalSession(session -> {
            repository.setTitles(
                    session, assetId,
                    oldEnglishTitle = "old english title",
                    oldGermanTitle = "old german title",
                    oldSpanishTitle = "old spanish title",
                    oldFrenchTitle = "old french title"
            );

            repository.setDescriptions(
                    session, assetId,
                    oldEnglishDescription = "old english description",
                    oldGermanDescription = "old german description",
                    oldSpanishDescription = "old spanish description",
                    oldFrenchDescription = "old french description"
            );

        });
    }

    private void inTransactionalSession(Consumer<Session> toDo) {
        inSession(session -> {
            final var transaction = session.beginTransaction();
            toDo.accept(session);
            transaction.commit();
        });
    }

    private void inSession(Consumer<Session> toDo) {
        try (final var session = sessionFactory.openSession()) {
            toDo.accept(session);
        }
    }

    private void setThumbnails(String thumb64, String thumb128, String thumb256, String thumb512) {
        inTransactionalSession(session -> repository.setThumbs(session, assetId, thumb64, thumb128, thumb256, thumb512));
    }

    private void verifyThumbnails(String thumb64, String thumb128, String thumb256, String thumb512) {
        withAssetRecord(assetId, record1 -> {
            assertThat(record1.getThumb64(), is(equalTo(thumb64)));
            assertThat(record1.getThumb128(), is(equalTo(thumb128)));
            assertThat(record1.getThumb256(), is(equalTo(thumb256)));
            assertThat(record1.getThumb512(), is(equalTo(thumb512)));
        });
    }

    private void withAssetRecord(String id, Consumer<AssetMetadataRecord> toDo) {
        inSession(session -> {
            final var record = session.get(AssetMetadataRecord.class, id);

            toDo.accept(record);
        });
    }
}
