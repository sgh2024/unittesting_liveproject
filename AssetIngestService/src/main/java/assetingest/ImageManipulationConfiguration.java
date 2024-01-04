package assetingest;

import imagescaler.*;
import org.springframework.context.annotation.*;

@Configuration
public class ImageManipulationConfiguration {
    @Bean
    public ImageScaler createImageScaler() {
        return new ImageScalerImplementation();
    }
}
