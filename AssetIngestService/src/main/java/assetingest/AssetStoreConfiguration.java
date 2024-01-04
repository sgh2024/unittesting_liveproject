package assetingest;

import com.amazonaws.auth.*;
import com.amazonaws.client.builder.*;
import com.amazonaws.services.s3.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

import java.awt.*;

@Configuration
public class AssetStoreConfiguration {
    @Bean
    public AssetStore createAssetStore(
            AWSCredentialsProvider credentialsProvider,
            AwsClientBuilder.EndpointConfiguration endpointConfiguration,
            @Value("${assetingest.assetstore.bucketname:default-bucket}") String bucketName) {

        final var builder = AmazonS3Client.builder()
                .withCredentials(credentialsProvider)
                .withPathStyleAccessEnabled(true)
                .withEndpointConfiguration(endpointConfiguration);

        try {
            return new S3AssetStore(builder.build(), bucketName);
        } catch (Exception ex) {
            System.err.println("Could not connect to AWS:" + ex);
            return () -> new AssetStoreEntry() {
                @Override
                public String getUri() {
                    return "constant-uri";
                }

                @Override
                public void saveImage(Image image) {
                }
            };
        }
    }
}
