package com.suning.crawler.core.helper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

public class DetectHTMLCharset {
	Logger logger;
	
	public DetectHTMLCharset(Logger logger) {
		this.logger = logger;
	}
    /**
     * To detect Charset basing on Page Information
     */
    private String getEncode(URL url){
        String charset = null;

        URLConnection con;
		try {
			con = url.openConnection();
	        if(con == null)
	        	return null;
	        
	        con.setConnectTimeout(5000);
			con.setReadTimeout(10000);
			con.setRequestProperty( "User-Agent", "Mozilla/5.0" );			
	        
	        String contentType = con.getContentType(); //To get charset from Header     
	        if(contentType != null)
	        	charset = doGetEncode(contentType);
	        if(charset == null) {  //To get charset from page content
	            InputStream is = url.openStream();
	            BufferedInputStream bis = new BufferedInputStream(is);
	            ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            int count = 0;
	            byte[] bytes = new byte[1024];
	            while((count = bis.read(bytes)) != -1) {  //read page segment
	                bos.write(bytes, 0, count);
	                bos.flush();
	                charset = doGetEncode(bos.toString());
	                if(charset != null) {  //Found charset
	                    break;
	                }
	                bos.reset();
	            }
	            is.close();
	        }
			} catch (Exception e) {
				e.printStackTrace();
		}
        
        return charset;
    }

    /**
     * To find charset by page meta data
     */
    private String doGetEncode(String str) {
        String charset = null;

        //Header: Content-Type		Value: text/html; charset=UTF-8
        { //To localize code block
	        //charset=UTF-8, from html header
	    	Pattern pattern = Pattern.compile("charset=([0-9a-zA-Z-]){4,16}", Pattern.CASE_INSENSITIVE);
	    	Matcher matcher = pattern.matcher(str);
	        if(matcher.find()) {  
	        	String matchStr = matcher.group();
	        	//logger.info("matchStr: " + matchStr);
	            charset = matchStr.substring(matchStr.indexOf("=") + 1, matchStr.length() );            
	        }
	        if(charset != null)
	        	return charset;
        }

       
        { //To localize code block
	        //charset=gb2312", from html file
	    	Pattern pattern = Pattern.compile("charset=([0-9a-zA-Z-]){4,6}\"", Pattern.CASE_INSENSITIVE);
	    	Matcher matcher = pattern.matcher(str);
	        if(matcher.find()) {  
	        	String matchStr = matcher.group();
	        	//logger.info("matchStr: " + matchStr);
	            charset = matchStr.substring(matchStr.indexOf("=") + 1, matchStr.length()-1 );            
	        }
	        if(charset != null)
	        	return charset;
        }

        {
	        //charset="GB2312", from html file
	    	Pattern pattern1 = Pattern.compile("charset=\"([0-9a-zA-Z-]){4,6}\"", Pattern.CASE_INSENSITIVE);
	    	Matcher matcher1 = pattern1.matcher(str);
	        if(matcher1.find()) { 
	        	String matchStr1 = matcher1.group();
	        	//logger.info("matchStr1: " + matchStr1);
	            charset = matchStr1.substring(matchStr1.indexOf("\"") + 1, matchStr1.length()-1 );            
	        }
        }
        return charset;
    }

    public String findCharset(String url) {
        String charset;
		try {
			charset = getEncode(new URL(url));
			
	        if (charset != null) {
	            logger.info("Page Charset should be:" + charset);
	            return charset;
	        } else {
	            charset = Charset.defaultCharset().toString(); 
	            logger.info("No Charset found, Platform Default：" + charset);
	            
	            return charset;
	        }
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
    }
}
