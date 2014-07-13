package com.suning.crawler.core;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.suning.crawler.core.cfg.CrawlerXMLCfgRuleEngine;
import com.suning.crawler.core.helper.CrawlerHelper;
import com.suning.crawler.core.URLRetriver.ResourceType;
import com.suning.crawler.core.URLRetriver.URLResultResource;
import com.suning.crawler.core.helper.XMLWriter;
import com.suning.crawler.helper.StringListener;

public abstract class CrawlerWorker implements ICrawlerWorker{
	final Logger logger;
			
	XMLWriter xmlWriter;
	protected URLRetriver distributedURLRetriver;

	protected Queue<String> seeds;
	public String crawlerName;
	
	protected CrawlerHelper crawlerHelper;
	protected CrawlerXMLCfgRuleEngine cfgRuleEngine;
	protected StringListener loggerListener;
	protected StringListener statusListener;
	protected StringListener seedListener;
	protected StringListener speedListener;
	protected long startDate;

	
	CrawlerStatus crawlerStatus;
	
	
	public CrawlerWorker(String _crawlerName) {
		if(_crawlerName == null)
			crawlerName = new String(this.getClass().getName());
		else
			crawlerName = new String(_crawlerName);
		
		logger = LoggerFactory.getLogger(crawlerName);
		startDate = Calendar.getInstance().getTimeInMillis();
		//Data from crawler controller
		//
		
		//only base name for the output file, it will roll basing on file size
		xmlWriter = new XMLWriter(crawlerName, logger);
		seeds = new LinkedList<String>();
		
		crawlerStatus = new CrawlerStatus(this);
		
		
		distributedURLRetriver = new URLRetriver(crawlerName, logger, crawlerStatus);

		cfgRuleEngine = new CrawlerXMLCfgRuleEngine(crawlerName+".xml", logger);
		//cfgRuleEngine.setSeedListener(this.seedListener);
		
		
		xmlWriter.write("<?xml version='1.0' encoding='UTF-8'?>");
		xmlWriter.write("<docs>");
		xmlWriter.write("<Version> 1.0.0 </Version>");
	}
	
	public String getCrawlerWorkerName() {
		return crawlerName;
	}
	
	public CrawlerStatus getCrawlerStatus() {
		crawlerStatus.seedLength = seeds.size();
		return crawlerStatus;
	}
	
	public void initCrawler() {
		Set<String> seedSet = cfgRuleEngine.readSeeds();
//		cfgRuleEngine.setSeedListener(new StringListener() {
//			
//			@Override
//			public void textEmitted(String text) {
//				seedListener.textEmitted(text);
//				
//			}
//		});
		for(String s: seedSet) {
			synchronized(this) {
				seeds.add(s);
				crawlerStatus.seededUrls ++;
			}
		}
	}	        		
		
	public void crawling() {
		
		String url;
		while(true) {			
			if(CrawlerController.quitFlag)
				break;
			
			url = seeds.poll();
			
			if (url == null) {
				try {
					logger.info("Crawler: <" + crawlerName + "> Seeds queue empty, there maybe new seeds from work thread, or 'x' to exit");
					loggerListener.textEmitted("Crawler: <" + crawlerName + "> Seeds queue empty, there maybe new seeds from work thread, or 'x' to exit");
					System.out.println("Crawler: <" + crawlerName + "> Seeds queue empty, there maybe new seeds from work thread, or 'x' to exit");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//seeds maybe populated by backend worker thread.
				continue;
			}
							
			//index page
	        //submit work to the thread pool
        	//Assign seed url to worker thread
			if(!CrawlerController.quitFlag) {
				logger.info("Crawler: <" + crawlerName + "> is crawling Link:" + url);
				loggerListener.textEmitted("Crawler: <" + crawlerName + "> is crawling Link:" + url);
				System.out.println("Crawler: <" + crawlerName + "> is crawling Link:" + url);			
				URLResultResource urlresult = distributedURLRetriver.getUrlSource(url);
				
				if(urlresult == null) {
					logger.info("Get URL fail:" + url);
					continue;
				}
					
				if(urlresult.ResultType == ResourceType.BINARY) {
					attachmentInformationRetriver(url, urlresult.fileName, logger, xmlWriter);
				}
				else {
	    			Document htmlDoc = Jsoup.parse(urlresult.html, url);
	    			if(htmlDoc == null) {
	    				logger.info("HTML parse error: " + url);
	    				loggerListener.textEmitted("HTML parse error: " + url);
	    				
	    				continue;
	    			}
	    			     			
	    			htmlInformationRetriver(url, urlresult.html, htmlDoc, logger, xmlWriter);
	    			linksRetriver(url, urlresult.html, htmlDoc, logger, xmlWriter);
				}
			} else
				break;	
		}
		
		logger.info("No more URL to crawler or key pressed, exit");
		loggerListener.textEmitted("No more URL to crawler or key pressed, exit");
	}
	
	public boolean addToSeed(String url) {
		if(url.length() <= 8) { //"http://?"
			logger.info("Wrong URL: \"" + url + "\"");
			return false;
		}	
		if(!cfgRuleEngine.isULRTraverseConstrainted(url) &&	
				(CrawlerController.pageFileManager.isVisited(url) == false)) {
			synchronized(this) {
				seeds.add(url);
				crawlerStatus.seededUrls ++;
				seedListener.textEmitted(url);
				return true;
			}
		} else 
			return false;
		
	}
	
	public void linkWidthTraverse(Document doc) {
		//get all links which in the bounded site and add to seeds queue
		Elements nextHopURLList = doc.select("a[href]");
		for(Element link: nextHopURLList){
			String absLink = link.attr("abs:href");
			if(absLink != null) {
				addToSeed(absLink);
				seedListener.textEmitted(absLink);
				crawlerStatus.seededUrls ++;
			}
		}
	}
	
	public void closeCrawler() {
		xmlWriter.write("</docs>");
		
	}
	
	
	//To be override by specific implementation
	// will be call by framework to analysis html
	public void htmlInformationRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter) {

		logger.info("Nothing to do with: " + url);
		loggerListener.textEmitted("Nothing to do with: " + url);

	}
	
	// will be call by framework to analysis attachment
	public void attachmentInformationRetriver(String url, String fileName, Logger logger, XMLWriter xmlWriter) {

		logger.info("Attachment downloaded: " + fileName);
		loggerListener.textEmitted("Attachment downloaded: " + fileName);
	
	}
	
	// will be call by framework to analysis links
	public void linksRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter) {

		linkWidthTraverse(htmlDoc);	

	}
	
	@Override
	public void addLoggerListener(StringListener listener) {
		// TODO Auto-generated method stub
		this.loggerListener = listener;
	}
	
	
	@Override
	public void addStatusListener(StringListener listener) {
		// TODO Auto-generated method stub
		this.statusListener = listener;
	}
	
	@Override
	public void addSpeedListener(StringListener listener) {
		// TODO Auto-generated method stub
		this.speedListener = listener;
	}
	
	@Override
	public void addSeedListener(StringListener listener) {
		// TODO Auto-generated method stub
		this.seedListener = listener;
	}

	public long getTimer() {
		
		return (Calendar.getInstance().getTimeInMillis() - startDate )/1000;
	}
	
}
