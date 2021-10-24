package search;

import java.net.InetAddress;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class ElasticSearchTest {

	public static void main(String[] args) {
		String index = "social";
		String type = "scraping";
		String word="";
		try {
			TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			SearchResponse response = client.prepareSearch(index)
					.setTypes(type) 
					.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setScroll(new TimeValue(60000))
					.setQuery(QueryBuilders.matchAllQuery())                 // Query
					//.setPostFilter(QueryBuilders.rangeQuery("age").from(12).to(18))     // Filter
					.setFrom(0).setSize(60).setExplain(true)
					.get();
			
			do {
				for (SearchHit hit : response.getHits().getHits()) {
					System.out.println(hit.getSource().get("title"));
//					result_map = hit.sourceAsMap();
//					result_map.put("_id", hit.getId());
//					result_map_list.add(result_map);
				}
				response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
			} while(response.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
