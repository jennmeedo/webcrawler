package com.suning.crawler.site;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;

import com.suning.crawler.core.CrawlerController;
import com.suning.crawler.core.CrawlerWorker;
import com.suning.crawler.core.helper.XMLWriter;
import com.suning.crawler.core.URLRetriver.URLResultResource;
import com.suning.crawler.core.helper.XMLWriter.BufferDocWriter;
import com.suning.crawler.helper.StringListener;

public class CrawlerJDMobileProductCommentsWithTag extends CrawlerWorker {
	
	public CrawlerJDMobileProductCommentsWithTag(String crawlerName) {
		super(crawlerName);
		cfgRuleEngine.setSeedListener(seedListener);
	}
	
	public void htmlInformationRetriver(String url, String html, Document htmlDoc, Logger logger, XMLWriter xmlWriter) {

		/*
		TBD: pageTypeDetect basing on XMLCfg
		*/
		
		//product list page detection		
		Elements results = htmlDoc.select("div.pic > a[href]");
		logger.info("Products links: " + results.size());
		loggerListener.textEmitted("Products links: " + results.size());
		statusListener.textEmitted(this.getCrawlerStatus().toString());
		speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
		if (results.size() == 0) {
			//non product list page, just traverse link topo
			//linkWidthTraverse(htmlDoc);
			return;
		}
		
		String category = crawlerHelper.purifyStringRegex(htmlDoc.title(), "- 京东手机版");
		
		logger.info("Processing Product List Page: " + url);
		loggerListener.textEmitted("Processing Product List Page: " + url);
		statusListener.textEmitted(this.getCrawlerStatus().toString());
		speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
		for (Element link: results) {
			
			if(CrawlerController.quitFlag)
				break;
			
			String linkHref = link.attr("abs:href");
						
			if((CrawlerController.pageFileManager.isVisited(linkHref) == false) && 
					(linkHref.indexOf("product") != -1))	{		
				logger.info("Processing Product Page: " + linkHref);
				statusListener.textEmitted(this.getCrawlerStatus().toString());
				speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
				parsingProductPage(linkHref, category, logger, xmlWriter);
			} else { 
				synchronized(this) {
					//seeds.add(linkHref);
					//totalPages++;
				}
			}
		}
		
		//linkWidthTraverse(htmlDoc);
	}
	
	public void parsingProductPage(String url, String category, Logger logger, XMLWriter xmlWriter) {
		//To support multiple thread, write all things to temporary buffer, and
		//write the whole doc to file in once when this url is fully processed
		BufferDocWriter bufWriter = xmlWriter.newBufferDocWriter();
		String productid_str = null;
		
		{
			URLResultResource urlresult = distributedURLRetriver.getUrlSource(url);
			if(urlresult == null) {
				logger.info("Product document get error: " + url);
				statusListener.textEmitted(this.getCrawlerStatus().toString());
				loggerListener.textEmitted("Product document get error: " + url);
				speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
				return;
			}
			String html = urlresult.html;
			
			bufWriter.write("<product>");
			bufWriter.printNode("product_url", url);
			
			org.jsoup.nodes.Document doc = Jsoup.parse(html, url);
	
			//product ID
			Element productid = doc.select("div[style]").first();
			if(productid != null) {
				if(productid.text().length() > 6) {
					productid_str = productid.text().substring(6, productid.text().length());
					bufWriter.printNode("product_id", productid_str);
				}
				else
				{
					logger.info("Wrong Product ID extracted" + productid.text());
					statusListener.textEmitted(this.getCrawlerStatus().toString());
					loggerListener.textEmitted("Wrong Product ID extracted" + productid.text());
					speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
				}
			}
	
			//product title
			Element element = doc.select("title").first();
			if(element != null) {
				String title = element.text();
				bufWriter.printNode("title", 
					crawlerHelper.purifyStringRegex(title, "- 京东手机版"));
			}
	
			//Product Description	
			Element elementdesc = doc.select("div.m3 > div.mc").first();
			if(element != null) {
				String desc = elementdesc.text();
				bufWriter.printNode("product_desc", desc);
			}
			
			//Product icon	
			Element elementIcon = doc.select("div.p-img > a > img").first();
			if(elementIcon != null)
				bufWriter.printNode("icon", elementIcon.attr("src"));
			else
			{
				logger.info("No Icon found: " + url);
				loggerListener.textEmitted("No Icon found: " + url);
				statusListener.textEmitted(this.getCrawlerStatus().toString());
				speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
			}
			
			//Get price information
			Element price = doc.select("div.p-price > font").first();
			if(productid != null)
				bufWriter.printNode("price", price.text());
			
			html = null;
		}

		//Image Information
		{
			String imageurl = "http://www.jd.com/bigimage.aspx?id=" + productid_str;
			URLResultResource urlresult = distributedURLRetriver.getUrlSource(imageurl);
			if(urlresult == null) {
				logger.info("Product document get error: " + imageurl);
				loggerListener.textEmitted("Product document get error: " + imageurl);
				statusListener.textEmitted(this.getCrawlerStatus().toString());
				speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
				return;
			}
			
			String imghtml = urlresult.html;
			org.jsoup.nodes.Document imgdoc = Jsoup.parse(imghtml, imageurl);		
	
			Element imgElement = imgdoc.select("div#biger img").first();
			if(imgElement != null)
				bufWriter.printNode("bigimg", imgElement.attr("src"));
			else
			{
				logger.info("No image found: " + imageurl);
				loggerListener.textEmitted("No image found: " + imageurl);
				statusListener.textEmitted(this.getCrawlerStatus().toString());
				speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
			}
			imghtml = null;
		}

				
		//To get all the comments
		{
			String reviewurl = "http://club.jd.com/review/" + productid_str + "-0-" + 1 + "-0.html";
			
			logger.info(reviewurl);
			statusListener.textEmitted(this.getCrawlerStatus().toString());
			
			URLResultResource urlresult = distributedURLRetriver.getUrlSource(reviewurl);
			if(urlresult == null) {
				logger.info("Comment first page get error: " + reviewurl);
				loggerListener.textEmitted("Comment first page get error: " + reviewurl);
				statusListener.textEmitted(this.getCrawlerStatus().toString());
				speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
				return;
			}
			
			String html = urlresult.html;
			Document doc = Jsoup.parse(html, reviewurl);
			
			//To find how many comments page available
			String comment_pages = null;
			//if There is no next shown, then no comments more than 1 page
			int comment_pages_num = 1;
			Elements nextPageElements = doc.select("div.pagin > a.next");
			if(nextPageElements != null) {	
				Element nextPageElement = nextPageElements.first();
				if(nextPageElement != null) {
					Element lastNumberPageElement = nextPageElement.previousElementSibling();
					if(lastNumberPageElement != null)
						comment_pages = lastNumberPageElement.text();
				}
	
				if(comment_pages != null ) {
					if(comment_pages.length() <= 3)
						comment_pages_num = new Integer(comment_pages).intValue();
					else
						comment_pages_num = new Integer(comment_pages.substring(3, comment_pages.length())).intValue();
				}
			} 
				
			logger.info("comment_pages: " + comment_pages + " " + comment_pages_num);
			loggerListener.textEmitted("comment_pages: " + comment_pages + " " + comment_pages_num);
			statusListener.textEmitted(this.getCrawlerStatus().toString());
			speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
			//To browse all the comment pages
			bufWriter.write("<comments>");
			for(int p=1; p<comment_pages_num && p<30; p++) {
	
				if(CrawlerController.quitFlag)
					break;
				
				reviewurl = "http://club.jd.com/review/" + productid_str + "-0-" + p + "-0.html";
				
				logger.info(reviewurl);
				statusListener.textEmitted(this.getCrawlerStatus().toString());
				loggerListener.textEmitted(reviewurl);
				speedListener.textEmitted("Time Passed: " + getTimer() + " sec");

				urlresult = distributedURLRetriver.getUrlSource(reviewurl);
				if(urlresult == null) {
					logger.info("Comments document get error: " + reviewurl);
					loggerListener.textEmitted("Comments document get error: " + reviewurl);
					speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
					continue;
				}
				
				html = urlresult.html;
				doc = Jsoup.parse(html, reviewurl);
				
				Elements comments = doc.select("div.item");
				if(comments != null) {
					for(Element c : comments) {
						bufWriter.write("<comment>");
						
						//user
						Element userElement = c.select("div.u-name > a").first();
						if(userElement != null)
							bufWriter.printNode("user_url", userElement.attr("href"));	
						else
						{
							logger.info("No comments user found @: " + reviewurl);
							loggerListener.textEmitted("No comments user found @: " + reviewurl);
							statusListener.textEmitted(this.getCrawlerStatus().toString());
							speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
						}
						
						//tags
						Elements c_tag= c.select("div.comment-content dd > span");
						if(c_tag != null) {
							bufWriter.write("<tags>");
							for(Element c_tag_e : c_tag) {
								bufWriter.printNode("tag", c_tag_e.text());
							}
							bufWriter.write("</tags>");
						}
						//comments
						Element usrCommentElement = c.select("div.comment-content dl > dd").first();
						if(usrCommentElement != null)
							bufWriter.printNode("learn", usrCommentElement.text());	
						else
						{
							logger.info("No comments found @: " + reviewurl);
							loggerListener.textEmitted("No comments found @: " + reviewurl);
							statusListener.textEmitted(this.getCrawlerStatus().toString());
							speedListener.textEmitted("Time Passed: " + getTimer() + " sec");
						}

						bufWriter.write("</comment>");
					}			
				}
			}
			
			bufWriter.write("</comments>");
		}
		
		bufWriter.write("</product>");
		
		//File can be rolled over to new one if the size is bigger than threshold at this point
		xmlWriter.write(bufWriter.getDoc(), true, "</docs>", "<docs>");
	}

	
}
