package com.suning.crawler.core.cfg;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;

import com.suning.crawler.core.CrawlerController;

/*
 *  dom4j's popular functions
 *  Node Type: Element, Attribute, Text, Comments
 *  
	Element root = xmlDoc.getRootElement();
	Element element = root.element(“ Element Name ");
	List elementList = root.elements();
	String elementName = root.getName();
	String  value = element.attributeValue(" Attribute Name");
	String text = element.getText();
	String text = element.elementText("Child Element Name");
*/

public class CrawlerXMLCfgReader {
	Logger logger;
	Document xmlDoc;
	
	public CrawlerXMLCfgReader(String xmlFileName, Logger logger) {
		
		this.logger = logger;
		
		InputStream input = CrawlerController.class.getClassLoader().getResourceAsStream(xmlFileName);
		SAXReader reader = new SAXReader();
		try {
			xmlDoc = reader.read(input);
			if(xmlDoc == null)
				logger.info("Open XML file: " + xmlFileName + "fail");
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Set<String> readSeeds() {

		Set<String> seedsSet = new LinkedHashSet<String>(); 
		
		if(xmlDoc == null)
			return seedsSet; //just return a empty set
		
		Element root = xmlDoc.getRootElement();
		Element seedsElement = root.element("URLSeeds");
		@SuppressWarnings("unchecked")
		List<Element> seedsList = seedsElement.elements();
		
		for(Element seedElement: seedsList) {
			String seedName = seedElement.getName();
			if(seedName.equals("ScopeSeed")) {
				String prefix = seedElement.attributeValue("Prefix");
				String postfix = seedElement.attributeValue("Suffix");
				int varStart = new Integer(seedElement.attributeValue("VarStart")).intValue();
				int varEnd = new Integer(seedElement.attributeValue("VarEnd")).intValue();
				for(int i=varStart; i<=varEnd; i++) {
					String seedStr = prefix + i + postfix;
					if(seedStr.length() > 0)
						seedsSet.add(seedStr);
					logger.info("Seed: " + seedStr );
				}		
			} else if(seedName.equals("SimpleSeed")) {
				String seedStr = seedElement.getText();
				if(seedStr.length() > 0)
					seedsSet.add(seedStr);
				logger.info("Seed: " + seedStr );				
			} else {
				logger.info("Wrong Element Name: " + seedName);
			}
		}
		
		return seedsSet;
	}

	public LinkedHashSet<String[]> readQuadTuple(String elementsName) {

		LinkedHashSet<String[]> set = new LinkedHashSet<String[]>(); 

		if(xmlDoc == null)
			return set; //just return a empty set
		
		Element root = xmlDoc.getRootElement();
		Element l2Element = root.element(elementsName);
		@SuppressWarnings("unchecked")
		List<Element> l3ElementList = l2Element.elements();
		
		for(Element constraintElement: l3ElementList) {
			String name = constraintElement.attributeValue("Name");
			String expression = constraintElement.attributeValue("Expression");
			String operator = constraintElement.attributeValue("Operator");
			String logicType = constraintElement.attributeValue("LogicType");
			String[] strArray = new String[4];
			if( (name.length() >0) && (expression.length()>0) && (operator.length()>0) && (logicType.length()>0)) {
				strArray[0] = new String(name);
				strArray[1] = new String(expression);
				strArray[2] = new String(operator);
				strArray[3] = new String(logicType);
				set.add(strArray);
			}
			/*
			logger.info("Quad Tuple: " + "\tName: " + strArray[0] + "\tExpression: " + 
				strArray[1] + "\tOperator: " + strArray[2] + "\tLogicType: " + strArray[3]);
			*/				
		}

		return set;
	}
}
