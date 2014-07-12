package com.suning.crawler.core;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suning.crawler.site.CrawlerJDMobileCommentsNoTag;
import com.suning.crawler.site.CrawlerJDMobileProductCommentsWithTag;
import com.suning.crawler.site.SimpleSiteWidePageDownloader;
 
public class CrawlerController implements Runnable {
	static final Logger logger = LoggerFactory.getLogger("CrawlerController");

	//Singleton instance
	public static boolean quitFlag = false;
	public static WebPageCacheManager pageFileManager= new WebPageCacheManager(logger);
	
	static String crawlerName;
	static ICrawlerWorker crawlerWorker;
	
	public static void main(String[] args) {
		
//		if(args.length <2) {
//			System.out.println("Uasge: java -jar crawler.jar <CrawlerName> PageTemplateId");
//			System.out.println("\tPageTemplate 1: JD Product Comments with Tags");
//			System.out.println("\tPageTemplate 2: JD Product Comments without Tags");
//			System.out.println("\tPageTemplate 3: General Page Downloader");
//			return;
//		}
		
		//Change to the new crawler class name as necessary 
		crawlerName = args[0];
		switch(Integer.valueOf(args[1])) {
			case 1:
				crawlerWorker = new CrawlerJDMobileProductCommentsWithTag(crawlerName);
				break;
			case 2:
				crawlerWorker = new CrawlerJDMobileCommentsNoTag(crawlerName);
				break;
			case 3:
				crawlerWorker = new SimpleSiteWidePageDownloader(crawlerName);
				break;
			default:
				System.out.println("Uasge: java -jar crawler.jar <CrawlerName> PageTemplateId");
				System.out.println("\tPageTemplate 1: JD Product Comments with Tags");
				System.out.println("\tPageTemplate 2: JD Product Comments without Tags");
				System.out.println("\tPageTemplate 3: General Page Downloader");
				return;
		}
		
		
		pageFileManager.deserialize();
		
		crawlerWorker.initCrawler();
	    logger.info("Any key to exit");

	    Thread worker = new Thread( new Runnable() {
    		public void run() {
    			 Thread.currentThread().setName("Site Thread: " + crawlerWorker.getCrawlerWorkerName());
    			 
    			 crawlerWorker.crawling();
    		}			        		
    	} );
    	worker.start();
		
		while (true) {
			try {
				int ch = System.in.read();
				switch(ch) {
				case 's':
					System.out.println(crawlerWorker.getCrawlerStatus().toString());
					break;
				case 'x':
					quitFlag = true;
					break;
				}			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(quitFlag)
				break;
		}
		
		logger.info("Waiting for end of crawling thread!");
		
		crawlerWorker.closeCrawler();
		
		pageFileManager.serialize();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
