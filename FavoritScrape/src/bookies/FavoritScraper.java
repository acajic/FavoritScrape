package bookies;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.http.client.fluent.Content;
import org.apache.http.concurrent.FutureCallback;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import server.HttpClient;


public class FavoritScraper {

	private HttpClient httpClient;
	private Map<String, String> parentCategoryNames;
	private Map<String, String> categoryNames;
	

	public FavoritScraper(HttpClient httpClient) {
		super();
		this.httpClient = httpClient;
		this.parentCategoryNames = new HashMap<String, String>();
		this.categoryNames = new HashMap<String, String>();
	}
	
	
	public Future<Content> scrapeBasicForToday() throws UnsupportedEncodingException, URISyntaxException {
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
				// #ponudaKategorijaID..., .ponudaNazivLige.text()
				// #ponudaParentKategorijaID..., .nazivParentKategorije.text()
				int[] partIndices = new int[]{0,3,4,5, 6, 9, 14};
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
