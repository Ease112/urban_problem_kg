package index;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

public class Index {
	public static File[] readFiles(File dir) {
		File[] files = null;
		try {
			files = dir.listFiles();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return files;
	}
	
	public static ArrayList<String> getExistances(String index, String type) {
		ArrayList<String> title_list = new ArrayList<String>();
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
					title_list.add((String)hit.getSource().get("title"));
				}
				response = client.prepareSearchScroll(response.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
			} while(response.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
			client.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return title_list;
	}
	
	public static void store(String title, String content, String index, String type) {
		String endpoint = "http://localhost:9200";
		try {
			TransportClient client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
			IndexResponse response = client.prepareIndex(index, type)
					.setSource(jsonBuilder()
							.startObject()
							.field("title", title)
							.field("postDate", new Date())
							.field("content", content)
							.endObject()
							)
					.get();
			
			RestStatus status = response.status();
			System.out.println(status.getStatus());
			client.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
