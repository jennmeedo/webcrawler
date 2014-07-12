package com.suning.crawler.core.helper;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlerHelper {
	/**
	 * 
	 * @param text
	 * @param regexStr: The pattern will be removed from text
	 * @return
	 */
	static public String purifyStringRegex(String text, String regexStr) {

		Pattern pattern = Pattern.compile(regexStr);
		Matcher matcher = pattern.matcher(text);
    	StringBuilder sb = new StringBuilder("");
    	int textloc =0;
		while(matcher.find()) { 
	    	String tmpstr = matcher.group();
	    	if(tmpstr.length() == 0)
	    		break;
	    	
	    	int starti = text.indexOf(tmpstr);
	    	int endi = starti + tmpstr.length();
	    	
	    	sb.append(text.substring(textloc, starti));
	    	textloc = endi;
	    	//logger.info(sb.toString());
	    }
		sb.append(text.substring(textloc, text.length()));
		//logger.info(sb.toString());
		
		return purifySpace(sb.toString());
	}

	//return the string which meet regex expression
	static public String getStringRegex(String text, String regexStr) {

		Pattern pattern = Pattern.compile(regexStr);
		Matcher matcher = pattern.matcher(text);
		if(matcher.find()) 
	    	return matcher.group();
		
		return null;
	}
	
	static public String purifySpace(String blanktext) {

		String[] items = blanktext.split(" ");
		StringBuilder finalStr = new StringBuilder("");
	
		for(String s: items) {
			if((!s.equals(" ")) && (s.length() > 0)) {
				//logger.info("^" + s + "^");
				s.trim();
				finalStr.append(s);
				finalStr.append(" ");
			}
		}
		
		return finalStr.toString().trim();
	}
	
	static public String convertURLtoFileString(String url) {
		String s;
		try {
			s = new String(java.net.URLDecoder.decode(url, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			s = url;
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
		char ch;
		for(int i=0; i<s.length(); i++)
			switch((ch = s.charAt(i))) {
				case '/':
				case '\\':
				case ':':
				case '*':
				case '?':
				case '"':
				case '<':
				case '>':
				case '|':
					sb.append('_');
					break;
				default:
					sb.append(ch);
					break;
			}
		
		String s1 = sb.toString();
		return (s1.length()> 300)? s1.substring(0, 300): s1;
	}
}
