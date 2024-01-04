package assetingest;

import org.springframework.context.annotation.*;

import javax.imageio.*;
import java.io.*;
import java.net.*;

@Configuration
public class ExternalDependenciesConfiguration {
    @Bean
    public AssetOrigins createAssetOrigins() {
        return uri -> {
            try {
                return ImageIO.read(new URL(uri));
            } catch (IOException e) {
                return null;
            }
        };
    }
}
