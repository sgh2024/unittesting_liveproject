package assetsearchservice;

import org.hibernate.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin
@RestController
public class SearchController {
    private final SessionFactory sessionFactory;

    public SearchController(@Autowired SessionFactory sessionFactory) {

        this.sessionFactory = sessionFactory;
    }

    @PostMapping(value = "/search", produces = "application/json")
    public SearchResultsMessage simpleTextSearch(@RequestParam("q")String query) {
        final var searchResultsMessage = new SearchResultsMessage();
        final var results = new ArrayList<SearchResultsFoundItem>();
//        try (final var session = sessionFactory.openSession()) {
//            final var criteriaBuilder = session.getCriteriaBuilder();
//            session.getCriteriaBuilder().createQuery(AssetMetadataRecord.class).from(AssetMetadataRecord.class).get(AssetMetadataRecord)
//            final var query = criteriaBuilder.createQuery(AssetMetadataRecord.class);
//
//            session.find(AssetMetadataRecord.class, );
//        }
//        searchResultsMessage.setFound(results);
        if ("balls".equals(query)) {
            addResult(results, new SearchResultsFoundItem(), "balls1", "https://www.gophersport.com/cmsstatic/img/265/g-72046-RainbowUltraPlayRecessReadySportBalls-web-001.jpg");
            addResult(results, new SearchResultsFoundItem(), "balls2", "https://www.worldsbestgolfdestinations.com/wp-content/uploads/2016/05/VICE_PRO_FLAMINGO_Cut.jpg");
            addResult(results, new SearchResultsFoundItem(), "balls3", "https://cdn.sweatband.com/Longridge_Foam_Practice_Balls_longridge_practice_foam_balls_single_2000x2000.jpg");
        }
        return searchResultsMessage;
    }

    private void addResult(ArrayList<SearchResultsFoundItem> results, SearchResultsFoundItem result1, String id, String thumb256) {
        result1.setThumb256(thumb256);
        result1.setId(id);
        results.add(result1);
    }
}

//@StaticMetamodel( AssetMetadataRecord.class )
//class AssetMetadataRecord_ {
//
//}
