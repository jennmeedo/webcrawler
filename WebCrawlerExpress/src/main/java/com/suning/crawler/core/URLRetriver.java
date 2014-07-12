package com.suning.crawler.core;

import static java.nio.file.StandardOpenOption.APPEND;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Date;

import org.slf4j.Logger;

import com.suning.crawler.core.helper.CrawlerHelper;
import com.suning.crawler.core.helper.DetectHTMLCharset;

public class URLRetriver {
	Logger logger;
	String crawlerName;
	CrawlerStatus crawlerStatus;
	public URLRetriver(String crawlerName, Logger logger, CrawlerStatus crawlerStatus) {
		this.crawlerName = crawlerName;
		this.logger = logger;
		this.crawlerStatus = crawlerStatus;
	}
	
	public boolean isNetworkConneted() {
		
		try {
			Socket socket = new Socket();
			InetSocketAddress addr = new InetSocketAddress("www.google.com", 80);
			socket.connect(addr, 3000);
			boolean netAccess = socket.isConnected();
			socket.close();
			return netAccess;
		} catch (Exception e1) {
			//e1.printStackTrace();
		}

		return false;
	}
	
	public enum ResourceType {
	    HTML, BINARY
	}
	
	public class URLResultResource {
		public ResourceType ResultType;   //0: text, 1: binary
		public String fileName;
		public String html;
		
		public URLResultResource(ResourceType ResultType, String fileName, String html) {
			this.ResultType = ResultType;
			this.fileName = fileName;
			this.html = html;
		}
	}
	
	private URLResultResource getCachedFile(String url, String cachedFile) {
		
		if(cachedFile.indexOf("attachment") != -1) {
			// binary file	
			return new URLResultResource(ResourceType.BINARY, cachedFile, "");
		} else {
			//html page		
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(new FileInputStream(cachedFile), "UTF-16"));
				String inputLine;
				StringBuilder a = new StringBuilder();
				while ((inputLine = in.readLine()) != null)
					a.append(inputLine);
				in.close();
								
				String html = a.toString();

				return new URLResultResource(ResourceType.HTML, "", html);
			} catch (Exception e) {
				logger.error("Cached file not found, url: " + url + "Cached Filename: " + cachedFile);
				try {
					if(in != null)
						in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				return null;
			}		
		}		
	}
	
	private URLResultResource getHtml(String _url, URLConnection urlc ) {
		
    	DetectHTMLCharset dcs = new DetectHTMLCharset(logger);
	    String detectedCharset = dcs.findCharset(_url);
	    
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(urlc.getInputStream(), detectedCharset));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.toString());
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error(e.toString());
			return null;
		}
		
		//To support binary format for image download ?
		
		String inputLine;
		StringBuilder a = new StringBuilder();
		try {
			while ((inputLine = in.readLine()) != null)
				a.append(inputLine);
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error(e.toString());
			return null;
		}
		try {
			in.close();
		} catch (IOException e) {
			//e.printStackTrace();
			logger.error(e.toString());
			return null;
		}
						
		String html = a.toString();
		
		//HTML page type detection, only html file can be passed to down stream for analysis
		if((html.indexOf("html") == -1) && (html.indexOf("HTML") == -1)) {
			logger.info("Non HTML Page found: " + _url);
			HtmlLog(_url, html);
			return null;
		}
	
		if(html.length()>200) 
			logger.info("Page Content:" + html.substring(0, 199));
		else
			logger.info("Page Content:" + html);
	
		String filename = HtmlLog(_url, html);
		
		if(filename != null) {
			CrawlerController.pageFileManager.logFileEntry(_url, filename, new Date());
		}
		
		crawlerStatus.htmlUrls ++;
		crawlerStatus.bytes += html.length();
		
		return new URLResultResource(ResourceType.HTML, "", html);
		
	}
	
	private URLResultResource getBinaryFile(String _url, URLConnection urlc, int contentLength) {
    	//Binary file downloading
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
	    try {
	    	String fileName = new String("./var/attachment/" + crawlerName + "/BINARY." + _url.hashCode() + "." + 
	    								CrawlerHelper.convertURLtoFileString(_url));					
			File file = new File(fileName);
			if(file.exists())
				file.delete();
			file.getParentFile().mkdirs();
			file.createNewFile();
			out = new BufferedOutputStream(new FileOutputStream(file));

			InputStream raw = urlc.getInputStream();
		    in = new BufferedInputStream(raw);
		    
		    byte[] data = new byte[(contentLength > 10*1024) ? (10*1024) : contentLength];
		    int bytesRead = 0;
		    int downloadedLength = 0;
		    while (downloadedLength < contentLength) {
		      bytesRead = in.read(data, 0, data.length);
		      if (bytesRead == -1)
		        break;
		      
		      out.write(data, 0, bytesRead);
		      out.flush();
		      
		      downloadedLength += bytesRead;
		    }
		    in.close();
		    out.close();

		    if (downloadedLength != contentLength) {
		    	logger.error("Only read " + downloadedLength + " bytes; Expected " + contentLength + " bytes");
		    	return null;
		    }
		    
		    CrawlerController.pageFileManager.logFileEntry(_url, fileName, new Date());
			
			crawlerStatus.binaryUrls ++;
			crawlerStatus.bytes += downloadedLength;
			
			return new URLResultResource(ResourceType.BINARY, fileName, "");
			
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if(in != null)
					in.close();
				if(out != null)
					out.close();
			} catch (Exception e1) {
				e1.printStackTrace();				
			}
			return null;
		}
	}

	private URLResultResource getUrlViaLocalNetwork(String _url) {
		URLConnection urlc;
	    int contentLength = -1;
		try {
			URL url = new URL(_url);
			urlc = url.openConnection();
			urlc.setConnectTimeout(5000);
			urlc.setReadTimeout(20000);
			urlc.setRequestProperty( "User-Agent", "Mozilla/5.0" );
			//urlc.setRequestProperty( "cookie", "token" );

		    contentLength = urlc.getContentLength();
		    if (contentLength == -1) {
				logger.info("Content Length Error, -1!");
				//return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		//header processing
	    for (int i = 0; ; i++) {
	        String headerName = urlc.getHeaderFieldKey(i);
	        String headerValue = urlc.getHeaderField(i);

	        if (headerName == null && headerValue == null) {
	        	break;
	        }
	        //logger.info("Header: " + headerName + "\t\tValue: " + headerValue);
	    }
	    
		String contentType = urlc.getContentType();
		logger.info("contentType: " + contentType);
		
		//For html page, it may return -1 from some site like jd.com
		if(contentType == null)
			return getHtml(_url, urlc);
		
	    if ( ((contentType.indexOf("text") != -1)) || (contentLength == -1) ||
	    		(_url.indexOf(".htm") != -1))	    	
	    	return getHtml(_url, urlc);
	    else 
	    	return getBinaryFile(_url, urlc, contentLength);
	}
	
	/**
	 * 
	 * @param _url
	 * @return fileName if success, otherwise, return null
	 */

	public URLResultResource getUrlSource(String _url) {
		
		logger.info("To downloading page: " + _url);
		
		String cachedFile = CrawlerController.pageFileManager.getFilename(_url);
		if(cachedFile != null) {
			System.out.println("cached file found for :" + _url);
			return getCachedFile(_url, cachedFile);
		}
		
		//Network status check
		while(!isNetworkConneted()) {
			logger.info("Waiting for network!");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(CrawlerController.quitFlag)
				return null;			
		}
	    
		//fetch data from local network
		return getUrlViaLocalNetwork(_url);
	    
	}	
	

	private String HtmlLog(String url, String html) {
		String filename = "./var/rawhtml/"+ crawlerName + "/HTML." + url.hashCode() + "." +
							CrawlerHelper.convertURLtoFileString(url);
		
		File file = new File(filename);
		if(file.exists())
			file.delete();
		try {
			file.getParentFile().mkdirs();
			file.createNewFile();
			BufferedWriter writer = Files.newBufferedWriter(file.toPath(), Charset.forName("UTF-16"), APPEND);
			writer.write(html, 0, html.length());
			writer.flush();
			writer.close();
			return filename;
		} catch (IOException e) {
			logger.info("url: " + url + "save html file fail");
			e.printStackTrace();
			return null;
		}
	}

}
