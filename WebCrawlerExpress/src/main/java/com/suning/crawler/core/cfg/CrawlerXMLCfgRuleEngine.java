package com.suning.crawler.core.cfg;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

public class CrawlerXMLCfgRuleEngine extends CrawlerXMLCfgReader{
	public CrawlerXMLCfgRuleEngine(String xmlFileName, Logger logger) {
		super(xmlFileName, logger);
	}
	
	@Override
	public Set<String> readSeeds() {
		return super.readSeeds();
	}

	/**
	 * 
	 * @param url
	 * @return true: means fail to pass test
	 */
	public boolean isULRTraverseConstrainted(String url) {
		LinkedHashSet<String[]> set = readQuadTuple("ULRTraverseConstraints");

		logger.info("isULRTraverseConstrainted():" + url);
		for(String[] tuple: set) {
			
			String combine = new String(tuple[3] + tuple[2]);
			Pattern pattern;
			Matcher matcher;
			switch(combine) {
				case "InclusiveRegex":
					pattern = Pattern.compile(tuple[1]);
					matcher = pattern.matcher(url);
					if(!matcher.find()) { 
						logger.info("Url: \"" + url + "\" fails to pass Quad Tuple test: " + "\tName: " + tuple[0] + "\tExpression: " + 
													tuple[1] + "\tOperator: " + tuple[2] + "\tLogicType: " + tuple[3]);				    	
						return true;
					}
					break;
				case "ExclusiveRegex":
					pattern = Pattern.compile(tuple[1]);
					matcher = pattern.matcher(url);
					if(matcher.find()) { 
						logger.info("Url: \"" + url + "\" fails to pass Quad Tuple test: " + "\tName: " + tuple[0] + "\tExpression: " + 
													tuple[1] + "\tOperator: " + tuple[2] + "\tLogicType: " + tuple[3]);				    	
						return true;
					}					
					break;			
				case "InclusiveIndexOf":
					if(url.indexOf(tuple[1]) == -1) {
						logger.info("Url: \"" + url + "\" fails to pass Quad Tuple test: " + "\tName: " + tuple[0] + "\tExpression: " + 
													tuple[1] + "\tOperator: " + tuple[2] + "\tLogicType: " + tuple[3]);				    	
						return true;
					}
					break;
				case "ExclusiveIndexOf":
					if(url.indexOf(tuple[1]) != -1) {
						logger.info("Url: \"" + url + "\" fails to pass Quad Tuple test: " + "\tName: " + tuple[0] + "\tExpression: " + 
													tuple[1] + "\tOperator: " + tuple[2] + "\tLogicType: " + tuple[3]);				    	
						return true;
					}
					break;
				default:
					logger.error("Uknown ULRTraverseConstraint found");
					break;
			}
		}
		
		// pass all the rule test, there is no constraint to this url
		return false;
	}
}
