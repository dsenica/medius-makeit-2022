package si.medius.makeit.recommendation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedSignificantStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedSignificantTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;


@ApplicationScoped
public class RecommendationService
{
    @Inject
    RestHighLevelClient restClient;

    public List<String> getRecommendedItemsForUser(String userId) throws IOException
    {
        String[] items = userItems(userId);

        SearchRequest searchRequest = new SearchRequest("invoices");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0)
                .query(QueryBuilders.termsQuery("items.description", items))
                .aggregation(AggregationBuilders.significantTerms("recommendations")
                        .field("items.description")
                        .minDocCount(100)
                        .includeExclude(new IncludeExclude(null, items)));
        searchRequest.source(searchSourceBuilder);
        List<String> result = new ArrayList<>();
        SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);
        ParsedSignificantStringTerms recommendations = searchResponse.getAggregations().get("recommendations");
        for(var p : (List<ParsedSignificantTerms.ParsedBucket>) recommendations.getBuckets()) {
            result.add((String) p.getKey());
        }
        return result;
    }

    private String[] userItems(String userId) throws IOException
    {
        SearchRequest searchRequest = new SearchRequest("invoices");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0)
                .query(QueryBuilders.matchQuery("customerID", userId))
                .aggregation(AggregationBuilders.terms("uniq_items")
                        .field("items.description")
                        .size(10000));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = restClient.search(searchRequest, RequestOptions.DEFAULT);

        List<String> items = new ArrayList<>();
        ParsedStringTerms terms = searchResponse.getAggregations().get("uniq_items");
        for(var p : (List<ParsedStringTerms.ParsedBucket>)terms.getBuckets()) {
            items.add(p.getKeyAsString());
        }

        return items.toArray(String[]::new);
    }
}
