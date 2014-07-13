package com.suning.crawler.core;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suning.crawler.helper.StringListener;
import com.suning.crawler.site.CrawlerJDMobileCommentsNoTag;
import com.suning.crawler.site.CrawlerJDMobileProductCommentsWithTag;
import com.suning.crawler.site.SimpleSiteWidePageDownloader;

public class CrawlerController implements Runnable {
	static final Logger logger = LoggerFactory.getLogger("CrawlerController");

	// Singleton instance
	public static boolean quitFlag = false;
	public static WebPageCacheManager pageFileManager = new WebPageCacheManager(
			logger);

	static String crawlerName;
	static ICrawlerWorker crawlerWorker;
	private StringListener loggerListener;
	private StringListener statusListener;
	private StringListener speedListener;

	/**
	 * 
	 * @param name
	 * @param id
	 */
	public CrawlerController(String name, int id) {

		crawlerName = name;
		switch (id) {
		case 1:
			crawlerWorker = new CrawlerJDMobileProductCommentsWithTag(
					crawlerName);
			break;
		case 2:
			crawlerWorker = new CrawlerJDMobileCommentsNoTag(crawlerName);
			break;
		case 3:
			crawlerWorker = new SimpleSiteWidePageDownloader(crawlerName);
			break;
		default:
			System.out
					.println("Uasge: java -jar crawler.jar <CrawlerName> PageTemplateId");
			System.out
					.println("\tPageTemplate 1: JD Product Comments with Tags");
			System.out
					.println("\tPageTemplate 2: JD Product Comments without Tags");
			System.out.println("\tPageTemplate 3: General Page Downloader");
			return;
		}

		// Logger Listener
		crawlerWorker.addLoggerListener(new StringListener() {
			public void textEmitted(String text) {
				loggerListener.textEmitted(text);
			}
		});

		

		// Status Listener
		crawlerWorker.addStatusListener(new StringListener() {
			public void textEmitted(String text) {
				statusListener.textEmitted(text);
			}
		});

		// Speed Listener
		crawlerWorker.addSpeedListener(new StringListener() {
			public void textEmitted(String text) {
				speedListener.textEmitted(text);
			}
		});
		//pageFileManager.deserialize();
		crawlerWorker.initCrawler();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Thread.currentThread().setName(
				"Site Thread: " + crawlerWorker.getCrawlerWorkerName());
		loggerListener.textEmitted("Site Thread: "
				+ crawlerWorker.getCrawlerWorkerName());

		crawlerWorker.crawling();

	}

	public String getCrawlerStatus() {
		return crawlerWorker.getCrawlerStatus().toString();
	}

	public void addLoggerListener(StringListener stringListener) {
		// TODO Auto-generated method stub
		this.loggerListener = stringListener;

	}

	
	public void addSpeedListener(StringListener stringListener) {
		// TODO Auto-generated method stub
		this.speedListener = stringListener;

	}

	public void addStatusListener(StringListener stringListener) {
		// TODO Auto-generated method stub
		this.statusListener = stringListener;

	}

	public void stopProcessing() {
		// TODO Auto-generated method stub
		crawlerWorker.closeCrawler();
		pageFileManager.serialize();
		
		synchronized (this) {
			quitFlag = true;
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			loggerListener.textEmitted(e.toString());
		}
		loggerListener.textEmitted("Stoping job has been triggred");
		
	}
}
