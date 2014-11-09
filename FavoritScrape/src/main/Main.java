package main;

import java.util.concurrent.Future;

import org.apache.http.client.fluent.Content;

import server.HttpClient;
import bookies.FavoritScraper;


public class Main {
	 
	public static void main(String[] args) throws Exception {
		
		HttpClient httpClient = new HttpClient();
		FavoritScraper favoritScraper = new FavoritScraper(httpClient);
		
		Future<Content> favoritBasicTodayFuture = favoritScraper.scrapeBasicForToday();
		
		favoritBasicTodayFuture.get();
		httpClient.executorService.shutdown();
	}
	
 
	
}
