package com.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.Constants;
import com.SEUtil;

public class CacheRefreshController {
	
	private static Logger logger = Logger.getLogger(CacheRefreshController.class.getName());
	
	private static enum CACHE_APPROACH {SYNC, ASYNC};
	private static String ASYNC_ACKNOWLDGEMENT_RESPONSE = "70015";
	private static String SUCCESS_RESPONSE = "1";
	
	public static void main(String[] args){
		
		String fileName = "/home/mbas/mbasApp/config/Constants.properties";
		String result = "Fail";
		String cacheAutoRefresh = "N";
		String cacheParam = null;
		
		String userhome = System.getProperty("user.home");
		
		
		try {
			Constants.load(fileName);
			String logPath = userhome + File.separator + "BackEndProcess/src/log4j.properties";
			System.out.println("logPath ..................................."+logPath);
			org.apache.log4j.PropertyConfigurator.configure(logPath);
			
			cacheAutoRefresh = Constants.getProperty("cacheAutoRefresh");
			cacheParam = Constants.getProperty("cacheParam");
			System.out.println("main() : cacheAutoRefresh="+cacheAutoRefresh+", cacheParam="+cacheParam);
			
			if("Y".equals(cacheAutoRefresh)){
				CacheRefreshController mCacheRefreshController = new CacheRefreshController();
				
				result = mCacheRefreshController.refreshCache(cacheParam);
			}
			else{
				result = "Cache Auto Refresh off, so please refresh manually.";
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			System.out.println("main() : result="+result);
		}
		
	}
	
	
	public String refreshCache(String p_cacheParam){
		logger.info("refreshCache() : enter, p_cacheParam="+p_cacheParam);
		
		String result = "Fail";
		String noOfInstanceStr = null;
		String appInstanceStr = null;
		String cacheApproach = null;
		String cacheContent = null;
		String appInstanceKey = null;
		
		int noOfInstance = 0;
		List<String> appInstanceList = new ArrayList<String>();
		
		try {
			noOfInstanceStr = Constants.getProperty("noOfInstance");
			noOfInstance = Integer.parseInt(noOfInstanceStr);
			
			logger.info("refreshCache() : enter, noOfInstanceStr="+noOfInstanceStr+", noOfInstance="+noOfInstance);
			
			for(int i = 0; i < noOfInstance; i++){
				appInstanceKey = "appInstance_"+(i+1);
				appInstanceStr = Constants.getProperty(appInstanceKey);
				logger.info("refreshCache() : appInstanceKey="+appInstanceKey+", appInstanceStr="+appInstanceStr);
				
				if(!SEUtil.isNullString(appInstanceStr)){
					appInstanceList.add(appInstanceStr.trim());
				}
			}
			
			cacheApproach = Constants.getProperty("cacheApproach");
			
			cacheContent = "cacheParam="+p_cacheParam+"&cacheApproach="+cacheApproach;
			
			result = refreshInstance(appInstanceList, cacheContent, cacheApproach);
		} 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			logger.info("refreshCache() : finally exit, result="+result);
		}
		
		return result;
	}
	
	
	public String refreshInstance (List<String> p_instanceList, String p_cacheContent, String p_cacheApproach){
		logger.info("refreshInstance() : Enter, p_cacheContent="+p_cacheContent+", p_cacheApproach="+p_cacheApproach+", p_instanceList="+p_instanceList);
		
		String result = "Fail";
		String resultCode = null;
		String responseXml = null;
		CacheParser mCacheParser = null;
		CacheRequest mCacheRequest = null;
		
		try {
			for(String url : p_instanceList){
				resultCode = null;
				responseXml = sendHttpRequest(url, p_cacheContent);
				if(!SEUtil.isNullString(responseXml)){
					mCacheParser = new CacheParser();
					mCacheRequest = mCacheParser.parseCacheResponse(responseXml);
					if(mCacheRequest != null){
						resultCode = mCacheRequest.getResultCode();
						result = mCacheRequest.getResultDesc();
					}
				}
				
				if(CACHE_APPROACH.ASYNC.toString().equals(p_cacheApproach)){
					if(!ASYNC_ACKNOWLDGEMENT_RESPONSE.equals(resultCode)){
						result = "Fail";
						break;
					}
				}
				else{
					if(!SUCCESS_RESPONSE.equals(resultCode)){
						result = "Fail";
						break;
					}
				}
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			logger.info("refreshInstance() : Exit, result="+result);
		}
		
		return result;
	}
	

	public String sendHttpRequest (String p_url, String p_cacheContent){
		logger.info("sendHttpRequest() : Enter, p_cacheContent="+p_cacheContent+", p_url="+p_url);
		
		URL url = null;
	    HttpURLConnection urlConn = null;
	    
	    int httpStatus = -1;
	    int readTimeout = 10000;
        int connectionTimeout = 10000;
        
        String readTimeoutStr = null;
        String connectionTimeoutStr = null;
	    
        urlConn = null;
	    BufferedReader  _in = null;
	    StringBuffer buffer = null;
	    String responseStr = null;
	    String urlStr = null;
	    
	    try {
    		if(!SEUtil.isNullString(p_url)){
    			connectionTimeoutStr = Constants.getProperty("cacheRefreshConnectTimeout");
    			readTimeoutStr = Constants.getProperty("cacheRefreshReadTimeout");
    			
    			if(!SEUtil.isNullString(readTimeoutStr)){
	    			try {
						readTimeout = Integer.parseInt(readTimeoutStr);
					} 
	    			catch (Exception e) {
						// TODO Auto-generated catch block
						readTimeout = 10000;
					}
	    		}
    	    	
    	    	if(!SEUtil.isNullString(connectionTimeoutStr)){
	    			try {
	    				connectionTimeout = Integer.parseInt(connectionTimeoutStr);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						connectionTimeout = 10000;
					}
	    		}
    			
    	    	urlStr = p_url+p_cacheContent;
    	    	
    	    	logger.debug("sendHttpRequest() : connectionTimeoutStr="+connectionTimeoutStr+", readTimeoutStr="+readTimeoutStr+", connectionTimeout="+ connectionTimeout+", readTimeout="+readTimeout+", urlStr="+urlStr);
    	    	
	            try {
					url = new URL(urlStr);
					urlConn = (HttpURLConnection) url.openConnection();
					urlConn.setConnectTimeout(connectionTimeout);
					urlConn.setReadTimeout(readTimeout);
					urlConn.setDoOutput (true);
					urlConn.setRequestProperty("Content-Type", "text/xml");
					urlConn.setDoInput (true);
					urlConn.setRequestMethod("GET");
					urlConn.setUseCaches (false);
					urlConn.setRequestProperty("Connection", "close");
					urlConn.connect();
					
					httpStatus = urlConn.getResponseCode();
				} 
	            catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					httpStatus = -1;
				}
	            
	
	            logger.debug("sendHttpRequest() : httpStatus="+httpStatus);
	            
	            if(httpStatus == HttpURLConnection.HTTP_OK){
		            try {
		            	_in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
		            }
			     	catch(Exception e) {
		                e.printStackTrace();
			        }
		            
			        String response = null;
			        buffer = new StringBuffer();
			        while ((response = _in.readLine()) != null) {
		                buffer.append(response);
			        }
			        responseStr = buffer.toString();
			        logger.debug("sendHttpRequest() : responseStr ="+ responseStr);
	            }
    		}
    		else{
    			logger.info("sendHttpRequest() : Blank url.");
    		}
		} 
    	catch (SocketTimeoutException ste){
    		  //Remote host didn’t respond in time
    		logger.info("sendHttpRequest() : More than " + connectionTimeoutStr + " elapsed.");
    		responseStr="-1";
            ste.printStackTrace();
    	}
    	catch (Exception e) {
    		logger.error("sendHttpRequest() : Exception, Error="+e.getMessage());
    		responseStr="-1";
	        e.printStackTrace();
		}
		finally{
			try{
				if(_in != null)
					_in.close();
			}
			catch(Exception ex){}
			try{
				if(url != null)
					url = null;
			}
			catch(Exception ex){}
			try{
				if(urlConn != null);
					urlConn.disconnect();
			}
			catch(Exception ex){}
			
			logger.info("sendHttpRequest() : finally Exit, responseStr="+ responseStr);
		}
    	return responseStr;
	}
	
	
}
