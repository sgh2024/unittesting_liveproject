package assetingest;

import ext.subjects.detection.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;

@Configuration
public class SubjectDetectionAPIConfiguration {
    @Bean
    public SubjectDetectionAPI createSubjectDetectionAPI(
            @Value("${assetingest.dependencies.subjects.url:http://localhost:3100/}")
            String url
    ) {
        if ("<<<default>>>".equals(url))
            return new SubjectDetectionAPI();

        return new SubjectDetectionAPI(url);
    }
}
