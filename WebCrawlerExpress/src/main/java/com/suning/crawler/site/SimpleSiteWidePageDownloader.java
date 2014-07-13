package com.suning.crawler.site;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import com.suning.crawler.core.CrawlerWorker;
import com.suning.crawler.core.helper.XMLWriter;
import com.suning.crawler.helper.StringListener;

public class SimpleSiteWidePageDownloader extends CrawlerWorker {
	public SimpleSiteWidePageDownloader(String crawlerName) {
		super(crawlerName);
		cfgRuleEngine.setSeedListener(seedListener);
	}
	
	public void htmlInformationRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter) {

		logger.info("Nothing to do with: " + url);
		loggerListener.textEmitted("Nothing to do with: " + url);
		statusListener.textEmitted(this.getCrawlerStatus().toString());
		speedListener.textEmitted("Time Passed: " + getTimer() + " sec");

	}
	
	public void linksRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter) {
		
		logger.info("Traversing page: " + url);
		loggerListener.textEmitted("Traversing page: " + url);
		statusListener.textEmitted(this.getCrawlerStatus().toString());
		speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
		linkWidthTraverse(htmlDoc);
	
	}

	@Override
	public void addLoggerListener(StringListener listener) {
		// TODO Auto-generated method stub
		this.loggerListener = listener;
	}
}
