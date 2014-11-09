package bookies;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.concurrent.FutureCallback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import server.HttpClient;

import com.google.gson.Gson;


public class FavoritScraper {

	private HttpClient httpClient;
	private Map<String, String> parentCategoryNames;
	private Map<String, String> categoryNames;
	
	private HashSet<String> utakmiceIds;
	

	public FavoritScraper(HttpClient httpClient) {
		super();
		this.httpClient = httpClient;
		this.parentCategoryNames = new HashMap<String, String>();
		this.categoryNames = new HashMap<String, String>();
	}
	
	public Future<ArrayList<Content>> scrapeAllForToday() throws UnsupportedEncodingException, URISyntaxException, InterruptedException, ExecutionException {
		this.utakmiceIds = new HashSet<String>();
		
		final Future<Content> futureBasic = this.scrapeBasicForToday();
		
		futureBasic.get();
			
		FutureTask<ArrayList<Content>> futureTask = new FutureTask<ArrayList<Content>>(new Callable<ArrayList<Content>>() {

			@Override
			public ArrayList<Content> call() throws Exception {
				ArrayList<Future<Content>> futures = new ArrayList<Future<Content>>();
				futures.add(futureBasic);
				
				
				for (String utakmicaId : utakmiceIds) {
					
					Future<Content> futureDetails = scrapeDodatnaPonuda(utakmicaId);
					futures.add(futureDetails);
					
				}
				
				ArrayList<Content> results = new ArrayList<Content>();
				for (Future<Content> future : futures) {				
					results.add(future.get());
				}
				return results;
			}
			
		});
		
		futureTask.run();
		return futureTask;
	}
	
	
	public Future<Content> scrapeBasicForToday() throws UnsupportedEncodingException, URISyntaxException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("categoryIDs", "");
		params.put("selectedDate", "10.11.");
		
		Callable<Content> callable  = new Callable<Content>() {

			@Override
			public Content call() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		final FutureTask<Content> futureTask = new FutureTask<Content>(callable);
		
		httpClient.post("https://www.favorit.hr/Controls/ControlServices/ContentLoaderService.asmx/GetPonudaByCategoryIDs", params, new FutureCallback<Content>() {
			
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
				
				
				Elements parentCategoryElements = doc.select("div[id^=ponudaParentKategorijaID]");
				for (Element parentCategoryElement : parentCategoryElements) {
					String parentCategoryId = parentCategoryElement.id().replaceFirst("ponudaParentKategorijaID_", "");
					String parentCategoryName = parentCategoryElement.select(".nazivParentKategorije").text();
					parentCategoryNames.put(parentCategoryId, parentCategoryName);
				}
				
				
				Elements categoryElements = doc.select("div[id^=ponudaKategorijaID]");
				for (Element categoryElement : categoryElements) {
					String categoryId = categoryElement.id().replaceFirst("ponudaKategorijaID_", "");
					String categoryName = categoryElement.select(".ponudaNazivLige").text().replaceFirst(" Prikaži filter razrade ponude", "");
					categoryNames.put(categoryId, categoryName);
				}
				
				
				// dodajNaListic(sifraDogadjaja, utakmicaId, susretId, kategorijaPonudeId, kategorijaPonudeParentId, par, koeficijent, tipOklade, idKoeficijenta, txtNazivKoeficijenta, noKombSusreti, ligaID, kategorijaID, nazivVrsteOklade, datumDogadjaja)
				int[] partIndices = new int[]{0,2,3,4,5, 6, 9, 14};
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
						
						
						
						if (koefPartindex == 3)
							koefPart = categoryNames.get(koefPart);
						if (koefPartindex == 4)
							koefPart = parentCategoryNames.get(koefPart);
						
						System.out.print(koefPart + " | ");
					}
					System.out.println();
					
					utakmiceIds.add(parts[1]);
					
					
				}
				
				futureTask.run();
			}
			
			@Override
			public void cancelled() {
				// TODO Auto-generated method stub
				System.out.println("Cancel");
			}
		});
	
		return futureTask;
	}
	
	

	public Future<Content> scrapeDodatnaPonuda(String utakmicaId) throws UnsupportedEncodingException, URISyntaxException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("dogadjajID", utakmicaId);
		
		
		Future<Content> future = httpClient.post("https://www.favorit.hr/Controls/ControlServices/ContentLoaderService.asmx/GetDodatnaPonudaByDogadjajID", params, new FutureCallback<Content>() {
			
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
				
				String innerJson = map.get("d");
				fromJson = (Map<String,String>) gson.fromJson(innerJson, map.getClass());
				map=fromJson;
				
				String html = map.get("dodatnaPonuda");
				html = StringEscapeUtils.unescapeHtml4(html);
				
				Document doc = Jsoup.parse(html);
				
				Elements categoryElements = doc.select("div[id^=dodatnaPonuda_]");
				for (Element categoryElement : categoryElements) {
					String categoryId = categoryElement.id().substring(categoryElement.id().lastIndexOf("_")+1);
					String categoryName = categoryElement.select("table > tbody > tr > th").first().text();
					
					categoryNames.put(categoryId, categoryName);
				}
				
				
				
				
				// dodajNaListic(sifraDogadjaja, utakmicaId, susretId, kategorijaPonudeId, kategorijaPonudeParentId, par, koeficijent, tipOklade, idKoeficijenta, txtNazivKoeficijenta, noKombSusreti, ligaID, kategorijaID, nazivVrsteOklade, datumDogadjaja)
				int[] partIndices = new int[]{0,2,3,4,5, 6, 9, 14};
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
						
						if (koefPartindex == 3)
							koefPart = categoryNames.get(koefPart);
						if (koefPartindex == 4)
							koefPart = parentCategoryNames.get(koefPart);
						
						System.out.print(koefPart + " | ");
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
	
		return future;
	}
	
	
}
