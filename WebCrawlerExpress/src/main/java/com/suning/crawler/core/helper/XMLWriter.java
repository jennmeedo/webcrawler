package com.suning.crawler.core.helper;

import static java.nio.file.StandardOpenOption.APPEND;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.slf4j.Logger;

public class XMLWriter {
	Logger logger;
	private File file;
	private Charset charset;
	private BufferedWriter writer;
	
	String baseFilename;
	int CurFileSize;
	int CurSequenceNo;
	static final int MAXXMLFILESIZE = (1*1024*1024);
	
	public XMLWriter(String fileName, Logger logger) {
		this.logger = logger;
		baseFilename = new String(fileName);

		CurFileSize = 0;
		CurSequenceNo = 1;
		
		initNewFile();
	}
	
	private void initNewFile() {
		
		file = new File(getFileName());
		charset = Charset.forName("UTF-8");
		try {
			File filebak = new File(file.toString()+".bak");
			if(filebak.exists())
				filebak.delete();
			
			if(file.exists()) {
				file.renameTo(filebak);
				file.delete();
			}
			file.getParentFile().mkdirs();
			file.createNewFile();
			writer = Files.newBufferedWriter(file.toPath(), charset, APPEND);
		} catch (IOException e) {
			logger.error("SERVE: " + getFileName() + " Init fail");
			e.printStackTrace();
			writer = null;
		}
	}
	
	private String getFileName() {
		String seqnostr = String.format("%08d", CurSequenceNo);
		return new String("./var/xmldata/" + baseFilename +".data." + seqnostr + ".xml");
	}
	
	String xmlEscapeText(String t) {
		   StringBuilder sb = new StringBuilder();
		   for(int i = 0; i < t.length(); i++){
		      char c = t.charAt(i);
		      switch(c){
		      case '<': sb.append("&lt;"); break;
		      case '>': sb.append("&gt;"); break;
		      case '\"': sb.append("&quot;"); break;
		      case '&': sb.append("&amp;"); break;
		      case '\'': sb.append("&apos;"); break;
		      default:
		    	 /*
		         if(c>0x7e) {
		            sb.append("&#"+((int)c)+";");
		         }else
		         */
		            sb.append(c);
		      }
		   }
		   return sb.toString();
		}
	
	public void printNode(String name, String value){
		write("<" + name + ">" + value + "</" + name + ">");
	}
	
	public synchronized void write(String string) {
		this.write(string, false, "", "");
	}

	public synchronized void write(String string, boolean isSplitPoint, String closingTag, String newStartingTag) {
		if(writer != null) {
			logger.info("Write to file: " + string);
			try {
				writer.write(string, 0, string.length());
				writer.write("\n");
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("Write to console: " + string);
		}
		
		CurFileSize += string.length();
		
		if(( CurFileSize > MAXXMLFILESIZE) && (isSplitPoint) && (closingTag.length()>0)) {
			try {
				writer.write(closingTag, 0, closingTag.length());
				writer.write("\n");
				writer.flush();
				writer.close();
				writer = null;

				CurFileSize = 0;
				CurSequenceNo ++;
				
				initNewFile();
				writer.write(newStartingTag, 0, newStartingTag.length());
				writer.write("\n");
				writer.flush();			
			} catch (IOException e) {
				e.printStackTrace();
			}		
		}
	}
	
	public class BufferDocWriter {
		private StringBuilder strBuilder;
		public BufferDocWriter() {
			strBuilder = new StringBuilder();
		}
		public void write(String string) {
			logger.info("Write to buf: " + string);
			strBuilder.append(string);
			strBuilder.append("\n");
		}
		
		public void printNode(String name, String value){
			String escxmlstr = xmlEscapeText(value);
			write("<" + name + ">" + escxmlstr + "</" + name + ">");
		}
		
		public String getDoc() {
			return strBuilder.toString();
		}
	}
	
	public synchronized BufferDocWriter newBufferDocWriter() {
		return new BufferDocWriter();
	}
	
	@Override
	protected void finalize() throws Throwable {
		writer.close();
	}
}
