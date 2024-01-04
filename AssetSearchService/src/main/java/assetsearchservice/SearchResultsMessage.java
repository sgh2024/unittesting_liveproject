package assetsearchservice;

import java.util.*;

public class SearchResultsMessage {
    private List<SearchResultsFoundItem> found;

    public List<SearchResultsFoundItem> getFound() {
        return found;
    }

    public void setFound(List<SearchResultsFoundItem> found) {
        this.found = found;
    }
}
