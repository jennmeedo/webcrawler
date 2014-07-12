package com.suning.crawler.core;

public class CrawlerStatus {

	CrawlerWorker cw;	
	volatile int seedLength;
	volatile int seededUrls;
	volatile int bytes;
	volatile int htmlUrls;
	volatile int binaryUrls;

	public CrawlerStatus(CrawlerWorker cw) {
		this.cw = cw;
		seedLength = 0;
		seededUrls = 0;
		bytes = 0;
		htmlUrls = 0;
		binaryUrls = 0;
	}
	
	public String toString() {
		return "{Crawler=" + cw.crawlerName +
				", seedLength=" + seedLength +
				", seededUrls=" + seededUrls +
				", bytes=" + bytes +
				", htmlUrls=" + htmlUrls +
				", binaryUrls=" + binaryUrls +
				"}";
	}
}
