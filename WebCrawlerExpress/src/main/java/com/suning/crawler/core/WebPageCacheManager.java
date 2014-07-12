package com.suning.crawler.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.slf4j.Logger;

class IndexBlock implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public String url;
	public String filename;
	public Date date;
	
	IndexBlock(String url, String filename, Date date) {
		this.url = url;
		this.filename = filename;
		this.date = date;
	}
	
	public String toString() {
		return "{IndexBlock{url: " + url +
			"\tfilename: " + filename + 
			"\tdate: " + date.toString()+"}";
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getUrl() {
		return url;
	}
	public String getFilename() {
		return filename;
	}
	public Date getDate() {
		return date;
	}
}


public class WebPageCacheManager {
	final Logger logger;
	
	HashMap<String, Date> rumtimeVistedHashMap = new HashMap<String, Date>();
	HashMap<Integer, LinkedList<IndexBlock>> fileHasmap = new HashMap<Integer, LinkedList<IndexBlock>>();
	
	WebPageCacheManager(Logger logger) {
		this.logger = logger;
	}
	
	synchronized public boolean logFileEntry(String url, String filename, Date date) {
		IndexBlock idxblk = new IndexBlock(url, filename, date);
		LinkedList<IndexBlock> idxBlkList = fileHasmap.get(new Integer(url.hashCode()));
		if(idxBlkList == null) {
			idxBlkList = new LinkedList<IndexBlock>();
			idxBlkList.add(idxblk);
			fileHasmap.put(new Integer(url.hashCode()), idxBlkList);
			
			return true;
		} else {
			if(idxBlkList.contains(idxblk))
				return true;
			else {
				idxBlkList.add(idxblk);
				return true;
			}
		}
	}
	
	synchronized public String getFilename(String url) {
		LinkedList<IndexBlock> idxBlkList = fileHasmap.get(new Integer(url.hashCode()));
		if(idxBlkList == null) {
			return null;
		} else {
			java.util.Iterator<IndexBlock> itr = idxBlkList.iterator();
			while(itr.hasNext()) {
				IndexBlock idxblk = (IndexBlock)itr.next();
				if(idxblk.url.equals(url))
					return idxblk.filename;
			}
		}
		return null;
	}
	
	synchronized public boolean isVisited(String url) {
		if(rumtimeVistedHashMap.get(url) == null) {
			rumtimeVistedHashMap.put(url, new Date());
			return false;
		}
		else 
			return true;
	}
	
	@SuppressWarnings("unchecked")
	public void deserialize(){

		logger.info("Deseralizing Page Index File");
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream("./var/PageFileIndex.ser");
			ois = new ObjectInputStream(fis);
			LinkedList<IndexBlock> listobj;
			while(( listobj = (LinkedList<IndexBlock>) ois.readObject()) != null) {
				for(IndexBlock idxblk: listobj) {
					logFileEntry(idxblk.url, idxblk.filename, idxblk.date);
					System.out.println(idxblk.toString());
				}
			}
			ois.close();
		} catch (FileNotFoundException e1) {
			logger.info("./var/PageFileIndex.ser doesn't exist!");
		} catch (Exception e) {
			if(ois != null) {
				try {
					ois.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}

	// serialize the given object and save it to file
	public void serialize(){
		
		logger.info("Seralizing Page Index File");
		
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream("./var/PageFileIndex.ser");
			oos = new ObjectOutputStream(fos);
			for(Integer i: fileHasmap.keySet()) {
				oos.writeObject(fileHasmap.get(i));				
			}

			oos.close();
		} catch (Exception e) {
			if(oos != null) {
				try {
					oos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}
	}	
}
