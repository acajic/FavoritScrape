
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
 
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.fluent.Content;
import org.apache.http.concurrent.FutureCallback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


public class Scraper {

	private final String USER_AGENT = "Mozilla/5.0";
	 
	public static void main(String[] args) throws Exception {
		
		
		Scraper scraper = new Scraper();
		
		HttpClient httpClient = new HttpClient();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("categoryIDs", "");
		params.put("selectedDate", "10.11.");
		
		Future<Content> future = httpClient.post("https://www.favorit.hr/Controls/ControlServices/ContentLoaderService.asmx/GetPonudaByCategoryIDs", params, new FutureCallback<Content>() {
			
			@Override
			public void failed(Exception arg0) {
				// TODO Auto-generated method stub
				System.out.println("Fail");
			}
			
			@Override
			public void completed(Content arg0) {
				// TODO Auto-generated method stub
				String json = arg0.asString();
				
				Gson gson = new Gson();
				Map<String,String> map=new HashMap<String,String>();
				@SuppressWarnings("unchecked")
				Map<String,String> fromJson = (Map<String,String>) gson.fromJson(json, map.getClass());
				map=fromJson;
				
				String html = map.get("d");
				Document doc = Jsoup.parse(html);
				
				/*Elements rows = doc.select("tr[title^=Šifra]");
				for (Element row : rows) {
					Element dateElement = row.select("td").first();
					String dateString = dateElement.text();
					
					Elements teamElements = row.select(".teams");
					String teamsString = "";
					for (Element teamElement : teamElements) {
						teamsString += teamElement.text();
						teamsString += " - ";
					}
					teamsString = teamsString.substring(0, teamsString.length() - 3);
					
					System.out.println(dateString + ": " + teamsString);
				}*/
				
				int[] partIndices = new int[]{0,5, 6, 9, 14};
				Elements koefs = doc.select("a[onclick^=dodajNaListic]");
				System.out.println();
				for (Element koefElement : koefs) {
					String onclickString = koefElement.attr("onclick");
					onclickString = onclickString.replaceFirst(Pattern.quote("dodajNaListic("), "");
					onclickString = onclickString.replaceFirst(Pattern.quote("); return false;"), "");
					String[] parts = onclickString.split(", ");
					for (int koefPartindex : partIndices) {
						String koefPart = parts[koefPartindex];
						koefPart = koefPart.trim();
						if (koefPart.charAt(0) == '\'') {
							koefPart = koefPart.substring(1, koefPart.length()-1);
						}
						
						System.out.print(koefPart + " ");
					}
					System.out.println();
					
				}
				
				
			}
			
			@Override
			public void cancelled() {
				// TODO Auto-generated method stub
				System.out.println("Cancel");
			}
		});
	
		
		
		future.get();
		httpClient.executorService.shutdown();
	}
	
 
	
}
