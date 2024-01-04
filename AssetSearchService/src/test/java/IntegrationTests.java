import org.junit.jupiter.api.extension.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.context.junit.jupiter.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = IntegrationTests.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "assetsearch.connection.url=" + IntegrationTests.databaseUrl,
                "assetsearch.connection.user=SA",
                "assetsearch.connection.password=",
                "assetsearch.assetstore.rooturl=" + IntegrationTests.s3RootUrl,
                "assetsearch.assetstore.accesskey=" + IntegrationTests.s3AccessKey,
                "assetsearch.assetstore.secretkey=" + IntegrationTests.s3SecretKey,
        })
public class IntegrationTests {
    static final String databaseUrl = "jdbc:hsqldb:mem:search_test;DB_CLOSE_DELAY=-1";
    private static final int s3Port = 8081;
    static final String s3RootUrl = "http://localhost:" + s3Port;
    static final String s3AccessKey = "abcdefg";
    static final String s3SecretKey = "12345";

    // TO DO - build integration tests
}
