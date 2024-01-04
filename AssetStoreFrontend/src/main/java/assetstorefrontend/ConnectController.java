package assetstorefrontend;

import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConnectController {
    @Value("${assetstorefrontend.dependencies.searchservice.url}")
    private String searchServiceUrl;

    @GetMapping(value = "/connect.js", produces = "text/javascript")
    public String generateConnectionScripts() {
        return "const searchServiceUrl = '" + searchServiceUrl + "';";
    }
}
