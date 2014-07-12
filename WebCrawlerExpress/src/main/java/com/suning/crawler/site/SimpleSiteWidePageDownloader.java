package com.suning.crawler.site;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import com.suning.crawler.core.CrawlerWorker;
import com.suning.crawler.core.helper.XMLWriter;

public class SimpleSiteWidePageDownloader extends CrawlerWorker {
	public SimpleSiteWidePageDownloader(String crawlerName) {
		super(crawlerName);
	}
	
	public void htmlInformationRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter) {

		logger.info("Nothing to do with: " + url);

	}
	
	public void linksRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter) {
		
		logger.info("Traversing page: " + url);
		linkWidthTraverse(htmlDoc);
	
	}
}
