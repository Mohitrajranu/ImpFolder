package com.cache;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.SEUtil;

public class CacheParser {
	
	public static void main(String[] args){
		
		String p_responseStr = "<cacheResponse><resultCode>1</resultCode><resultDesc>Success</resultDesc><caches><cache><process>ALL</process><resultCode>1</resultCode><resultDesc>Success</resultDesc></cache></caches></cacheResponse>";
		CacheParser mCacheParser = null;
		
		try {
			mCacheParser = new CacheParser();
			CacheRequest mCacheRequest = mCacheParser.parseCacheResponse(p_responseStr);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public CacheRequest parseCacheResponse (String p_responseStr){
		
		java.io.ByteArrayInputStream Bis = null;
		List<Cache> mCacheList = new ArrayList<Cache>();
		CacheRequest mCacheRequest = null;
		
		try {
			
			if(!SEUtil.isNullString(p_responseStr)){
				mCacheRequest = new CacheRequest();
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				
				Bis = new java.io.ByteArrayInputStream(p_responseStr.getBytes("UTF-8"));
	            Document doc = builder.parse(Bis);
	            Element mElement = doc.getDocumentElement();
	            System.out.println("Root Node="+mElement.getNodeName());
	            NodeList nodeList = mElement.getChildNodes();
	            
				mCacheRequest.setRequestNo(getTextValue(mElement, "requestNo"));
				mCacheRequest.setAppInstance(getTextValue(mElement, "appInstance"));
				mCacheRequest.setResultCode(getTextValue(mElement, "resultCode"));
				mCacheRequest.setResultDesc(getTextValue(mElement, "resultDesc"));
	            
	            //get a node list of cache
	            NodeList mNodeList = mElement.getElementsByTagName("cache");
	           
	           	if(mNodeList != null && mNodeList.getLength() > 0) {
	        	   	Cache mCache = null;
	        	   	Element cacheElement = null;
		   			for(int i = 0 ; i < mNodeList.getLength();i++) {
		
		   				//get the cache element
		   				cacheElement = (Element)mNodeList.item(i);
		
		   				//get the Cache object
		   				mCache = getCache(cacheElement);
		
		   				//add it to list
		   				mCacheList.add(mCache);
		   			}
	           	}
	           	
	           	mCacheRequest.setCaches(mCacheList);
			}
		} 
		catch (ParserConfigurationException pe) {
			// TODO Auto-generated catch block
			pe.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mCacheRequest;
		
	}
	
	
	
	/**
	 * I take an cache element and read the values in, create
	 * an Cache object and return it
	 */
	private Cache getCache (Element cacheElement) {

		//Create a new Cache with the value read from the xml nodes
		Cache mCache = null;
		try {
			String process = getTextValue(cacheElement, "process");
			String resultCode = getTextValue(cacheElement, "resultCode");
			String resultDesc = getTextValue(cacheElement, "resultDesc");


			mCache = new Cache(process, resultCode, resultDesc);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mCache;
	}

	
	/**
	 * I take a xml element and the tag process, look for the tag and get
	 * the text content
	 * i.e for <cache><process>EPC</process></cache> xml snippet if
	 * the Element points to cache node and tagName is 'process' I will return EPC
	 */
	private String getTextValue(Element pElement, String tagName) {
		String textVal = null;
		NodeList mNodeList = null;
		
		try {
			mNodeList = pElement.getElementsByTagName(tagName);
			if(mNodeList != null && mNodeList.getLength() > 0) {
				Element el = (Element)mNodeList.item(0);
				textVal = el.getFirstChild().getNodeValue();
			}
		} 
		catch (DOMException de) {
			// TODO Auto-generated catch block
			de.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return textVal;
	}

	

}
