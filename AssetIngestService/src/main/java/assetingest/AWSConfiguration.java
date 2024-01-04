package assetingest;

import com.amazonaws.auth.*;
import com.amazonaws.client.builder.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class AWSConfiguration {
    @Bean
    public AwsClientBuilder.EndpointConfiguration getEndpointConfiguration(
            @Value("${assetingest.assetstore.rooturl:}") String root,
            @Value("${assetingest.assetstore.signingRegion:us-west-2}") String region) {
        return new AwsClientBuilder.EndpointConfiguration(root, region);
    }

    @Bean
    public AWSCredentialsProvider createS3Credentials(
            @Value("${assetingest.assetstore.accesskey:}") String accessKey,
            @Value("${assetingest.assetstore.secretkey:}") String secretKey) {
        return new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
    }
}
