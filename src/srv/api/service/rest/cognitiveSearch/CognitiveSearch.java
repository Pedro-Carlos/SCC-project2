package srv.api.service.rest.cognitiveSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.azure.core.credential.*;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.util.SearchPagedResponse;
import utils.AzureKeys;

public class CognitiveSearch {

    private static CognitiveSearch instance;

    private SearchClient searchClient;
    public static final String URL = AzureKeys.getInstance().getSearchServiceUrl();
    public static final String QUERY_KEY = AzureKeys.getInstance().getSearchServiceQueryKey();
    public static final String INDEX_NAME = AzureKeys.getInstance().getIndexName();

    public static synchronized CognitiveSearch getInstance() {
        if( instance != null)
            return instance;

        SearchClient searchClient = new SearchClientBuilder()
                .credential(new AzureKeyCredential(QUERY_KEY))
                .endpoint(URL)
                .indexName(INDEX_NAME)
                .buildClient();

        instance = new CognitiveSearch(searchClient);
        return instance;
    }

    public CognitiveSearch(SearchClient searchClient) {
        this.searchClient = searchClient;
    }

    public List<List<Map.Entry<String, Object>>> search(String searchFor, String ownerId) {

        SearchOptions options = new SearchOptions()
                .setIncludeTotalCount(true)
                .setFilter("ownerId eq '" + ownerId + "'")
                .setSelect("id", "ownerId", "description", "status")
                .setSearchFields("description")
                .setTop(5);

        SearchPagedIterable searchPagedIterable = searchClient.search(searchFor, options, null);

        List<List<Map.Entry<String, Object>>> results = new ArrayList<List<Map.Entry<String, Object>>>();

        for(SearchPagedResponse resultResponse : searchPagedIterable.iterableByPage()) {
            resultResponse.getValue().forEach(searchResult -> {
                List<Map.Entry<String, Object>> auction = new ArrayList<Map.Entry<String, Object>>();
                for (Map.Entry<String, Object> res : searchResult.getDocument(SearchDocument.class).entrySet()) {
                    auction.add(res);
                }
                results.add(auction);
            });
        }

        return results;
    }


}