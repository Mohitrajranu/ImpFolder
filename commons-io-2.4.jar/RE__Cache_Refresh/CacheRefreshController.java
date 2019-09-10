package com.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.Constants;
import com.SEConstantsI;
import com.SEUtil;

public class CacheRefreshController {
	
	private static Logger logger = Logger.getLogger(CacheRefreshController.class.getName());
	
	private static enum CACHE_APPROACH {SYNC, ASYNC};
	private static enum RETRY_DELAY_TYPE{FIX, INC};
	
	private static String ASYNC_ACKNOWLDGEMENT_RESPONSE = "70015";
	private static String SUCCESS_RESPONSE = "1";
	private static String FAIL_RESPONSE = "0";
	
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
	
	
	public String refreshCache (String p_cacheParam){
		logger.info("refreshCache() : enter, p_cacheParam="+p_cacheParam);
		
		String result = "Fail";
		String noOfInstanceStr = null;
		String appInstanceStr = null;
		String cacheApproach = null;
		String cacheContent = null;
		String appInstanceKey = null;
		
		int noOfInstance = 0;
		List<String> appInstanceList = new ArrayList<String>();
		
		TreeMap appInstanceTM = new TreeMap();
		TreeMap responseTM = new TreeMap();
		
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
					
					appInstanceTM.put(appInstanceKey, appInstanceStr.trim());
					responseTM.put(appInstanceKey, FAIL_RESPONSE);
				}
			}
			
			cacheApproach = Constants.getProperty("cacheApproach");
			
			cacheContent = "cacheParam="+p_cacheParam+"&cacheApproach="+cacheApproach;
			
			result = refreshInstance(appInstanceTM, responseTM, cacheContent, cacheApproach, "NEW");
			
			if(responseTM != null){
				Iterator respItr = responseTM.keySet().iterator();
				
				String instKey = null;
				String resp = null;
				TreeMap failInstTM = new TreeMap();
				TreeMap failRespTM = new TreeMap();
				
				while(respItr.hasNext()){
					instKey = (String)respItr.next();
					resp = (String)responseTM.get(instKey);
					
					logger.info("refreshCache() : instKey="+instKey+", resp="+resp);
					
					if(!SUCCESS_RESPONSE.equals(resp)){
						failInstTM.put(instKey, (String)appInstanceTM.get(instKey));
						failRespTM.put(instKey, FAIL_RESPONSE);
					}
				}
				
				logger.info("refreshCache() : failInstTM="+failInstTM);
				
				if(failInstTM != null && failInstTM.size() > 0){
					String cacheRetry = Constants.getProperty("cacheRetry");
					logger.info("refreshCache() : cacheRetry="+cacheRetry);
					if(SEConstantsI.ENABLED.equals(cacheRetry)){
						result = processCacheRetry(failInstTM, failRespTM, cacheContent, cacheApproach);
					}
				}
			}
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
	
	
	
	public String processCacheRetry (TreeMap p_instanceTM, TreeMap p_responseTM, String p_cacheContent, String p_cacheApproach){
		logger.info("processCacheRetry() : Enter, p_cacheContent="+p_cacheContent+", p_cacheApproach="+p_cacheApproach+", p_instanceTM="+p_instanceTM);
		String result = null;
		String cacheRetryCountStr = null;
		String cacheRetryDelayTimeStr = null;
		String cacheRetryDelayType = null;
		
		RETRY_DELAY_TYPE cacheRetryDelayTypeEnum = RETRY_DELAY_TYPE.FIX;
		
				
		int cacheRetryCount = 0;
		int cacheRetryDelayTime = 0;
		
		
		cacheRetryCountStr = Constants.getProperty("cacheRetryCount");
		cacheRetryDelayTimeStr = Constants.getProperty("cacheRetryDelayTime");
		cacheRetryDelayType = Constants.getProperty("cacheRetryDelayType");
		
		if(!SEUtil.isNullString(cacheRetryDelayType)){
			cacheRetryDelayTypeEnum = enumValue(RETRY_DELAY_TYPE.class, cacheRetryDelayType);
		}
		
		if(cacheRetryDelayTypeEnum == null){
			cacheRetryDelayTypeEnum = RETRY_DELAY_TYPE.FIX;
		}
		
		try {
			cacheRetryCount = Integer.parseInt(cacheRetryCountStr);
		} 
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			cacheRetryDelayTime = Integer.parseInt(cacheRetryDelayTimeStr);
		} 
		catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		logger.debug("processCacheRetry() : cacheRetryCountStr="+cacheRetryCountStr+", cacheRetryCount="+cacheRetryCount+", cacheRetryDelayTimeStr="+cacheRetryDelayTimeStr+", cacheRetryDelayTime="+cacheRetryDelayTime
				+", cacheRetryDelayType="+cacheRetryDelayType+", cacheRetryDelayTypeEnum="+cacheRetryDelayTypeEnum);
		
		try {
			if(cacheRetryCount > 0){
				int sleepTime = 0;
				Iterator respItr = null;
				
				for(int i = 0; i < cacheRetryCount; i++){
					
					if(p_responseTM != null && p_responseTM.size() > 0){
						if(cacheRetryDelayTime > 0){
							if(RETRY_DELAY_TYPE.INC.equals(cacheRetryDelayTypeEnum)){
								sleepTime = cacheRetryDelayTime * (i+1);
							}
							else{
								sleepTime = cacheRetryDelayTime;
							}
							logger.debug("processCacheRetry() : before sleep with sleepTime="+sleepTime);
									
							Thread.sleep(sleepTime);
						}
						
						result = refreshInstance(p_instanceTM, p_responseTM, p_cacheContent, p_cacheApproach, "RET"+(i+1));
						
						logger.debug("processCacheRetry() : after retry ="+(i+1)+", p_responseTM="+p_responseTM+", result="+result);
						
						if(p_responseTM != null){
							respItr = p_responseTM.keySet().iterator();
							
							String instKey = null;
							String resp = null;
							
							while(respItr.hasNext()){
								instKey = (String)respItr.next();
								resp = (String)p_responseTM.get(instKey);
								
								logger.info("refreshCache() : instKey="+instKey+", resp="+resp);
								
								if(SUCCESS_RESPONSE.equals(resp)){
									p_instanceTM.remove(instKey);
									p_responseTM.remove(instKey);
								}
							}
							
							logger.info("refreshCache() : after retry ="+(i+1)+", final p_instanceTM="+p_instanceTM+", p_responseTM="+p_responseTM);
						}
					}
				}
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	
	public String refreshInstance (TreeMap p_instanceTM, TreeMap p_responseTM, String p_cacheContent, String p_cacheApproach, String p_operation){
		logger.info("refreshInstance() : Enter, p_cacheContent="+p_cacheContent+", p_cacheApproach="+p_cacheApproach+", p_instanceTM="+p_instanceTM+", p_operation="+p_operation);
		
		String result = "Fail";
		String resultCode = null;
		String responseXml = null;
		CacheParser mCacheParser = null;
		CacheRequest mCacheRequest = null;
		
		Iterator instItr = null;
		String instKey = null;
		String url = null;
		
		try {
			
			if(!SEUtil.isNullString(p_operation)){
				p_cacheContent = p_cacheContent+"&operation="+p_operation;
			}
			
			if(p_instanceTM != null){
				instItr = p_instanceTM.keySet().iterator();
				while(instItr.hasNext()){
					instKey = (String)instItr.next();
					url = (String)p_instanceTM.get(instKey);
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
						else{
							p_responseTM.put(instKey, SUCCESS_RESPONSE);
						}
					}
					else{
						if(!SUCCESS_RESPONSE.equals(resultCode)){
							result = "Fail";
							break;
						}
						else{
							p_responseTM.put(instKey, SUCCESS_RESPONSE);
						}
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
	
	
	public static <E extends Enum<E>> E enumValue (Class<E> e, String s) {
        if(s == null){
        	return null;
        }
    	try {
            return Enum.valueOf(e, s);
        }
        catch(IllegalArgumentException ex) {
            return null;
        }
    }
	
	
}
