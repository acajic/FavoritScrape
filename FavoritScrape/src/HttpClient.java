import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;



public class HttpClient {

	public ExecutorService executorService;

	public HttpClient() {
		// java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINE);
		java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINE);

		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "error");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
		
		this.executorService = Executors.newFixedThreadPool(2);
	}

	
	
	public Future<Content> get(String url, Map<String, Object> params, FutureCallback<Content> callback) throws URISyntaxException 
	{
		
		URIBuilder uriBuilder = new URIBuilder(url);
		for (String paramKey : params.keySet()) {
			Object paramValue = params.get(paramKey);
			uriBuilder.addParameter(paramKey, paramValue.toString());
		}
		
		URI uri = uriBuilder.build();
		
		final Request request = Request.Get(uri);
		
		
		
		System.out.println(request.toString());
		System.out.println();
		
		Async async = Async.newInstance().use(this.executorService);
		Future<Content> future = async.execute(request, callback);
		
		return future;
	}
	
	public Future<Content> post(String url, Map<String, Object> params, FutureCallback<Content> callback) throws URISyntaxException, UnsupportedEncodingException 
	{
		final Request request = Request.Post(url);
		
		
		/*
		ArrayList<NameValuePair> parameters = new ArrayList<NameValuePair>();
		for (Map.Entry<String, Object> entry : params.entrySet()) {
		    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
		}
		*/
		Gson gson = new Gson();
		String json = gson.toJson(params);
		StringEntity paramsEntity = new StringEntity(json);
		request.body(paramsEntity);
		
		request.setHeader("Content-Type", "application/json");
		
		
		System.out.println(request);
		
		Async async = Async.newInstance().use(this.executorService);
		Future<Content> future = async.execute(request, callback);
		
		return future;
	}

	
	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException {
		HttpClient httpClient = new HttpClient();
		
		Future<Content> future = httpClient.get("http://index.hr", new HashMap<String, Object>(), new FutureCallback<Content>() {
		    
			public void failed (final Exception e) {
		        
				System.out.println(e.getMessage());
				System.exit(0);
		    }
		    
		    public void completed (final Content content) {
		        System.out.println("Response:\n"+ content.asString());
		        System.exit(0);
		    }
		    
		    public void cancelled () {
		    	System.exit(0);
		    }
		});
		
		int a = 5;
		System.out.println(a);
	}
}
