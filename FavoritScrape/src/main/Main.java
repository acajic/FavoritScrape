package main;

import java.util.ArrayList;
import java.util.concurrent.Future;

import org.apache.http.client.fluent.Content;

import server.HttpClient;
import bookies.FavoritScraper;


public class Main {
	 
	public static void main(String[] args) throws Exception {
		
		HttpClient httpClient = new HttpClient();
		FavoritScraper favoritScraper = new FavoritScraper(httpClient);
		
		Future<ArrayList<Content>> favoritBasicTodayFuture = favoritScraper.scrapeAllForToday();
		
		
		favoritBasicTodayFuture.get();
		httpClient.executorService.shutdown();
	}
	
 
	
}
