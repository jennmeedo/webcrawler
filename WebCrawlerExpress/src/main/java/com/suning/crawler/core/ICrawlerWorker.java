package com.suning.crawler.core;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;

import com.suning.crawler.core.helper.XMLWriter;
import com.suning.crawler.helper.StringListener;

public interface ICrawlerWorker {

	//Parsing URL from seed queue, to be implemented by specific crawler
	//Some valuable functions/variable can be called by parsingPage()
	//		boolean addToSeed(String url)
	//		void linkWidthTraverse(Document doc)
	//		CrawlerXMLCfgRuleEngine cfgRuleEngine
	public void htmlInformationRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter);
	public void attachmentInformationRetriver(String url, String fileName, Logger logger, XMLWriter xmlWriter);
	public void linksRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter);
	
	
	//Implemented by BASE class CrawlerWorker, specific crawler does not be necessary to override it
	public void initCrawler();
	public void closeCrawler();
	public void crawling();
	
	public String getCrawlerWorkerName();
	public CrawlerStatus getCrawlerStatus();
	public void addLoggerListener(StringListener listener);
	public void addStatusListener(StringListener listener);
	public void addSpeedListener(StringListener listener);
	public void addSeedListener(StringListener listener);
}
