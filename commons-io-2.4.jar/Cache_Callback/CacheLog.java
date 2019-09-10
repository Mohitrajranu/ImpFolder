package com.comviva.dbill.util.logging;

/** RequestLog.java
 * Copyright(c) 2004, Bharti Telesoft International Pvt.Ltd.
 * All Rights Reserved
 *------------------------------------------------------------------------
 * Name                         Date            History
 *------------------------------------------------------------------------
 * Rama Nayak             27/09/2011 			Initial Creation
 *------------------------------------------------------------------------
 */
/** Overview of Application        :
 * Contains static method println() which has to be called by
 * RequestServlet to log every request message in the log file.
 */



import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

import com.comviva.dbill.engine.Cache;
import com.comviva.dbill.engine.CacheRequest;
import com.comviva.dbill.util.common.Application;
import com.comviva.dbill.util.common.SEUtil;

/** Overview of Application        :
 * Contains static method println() which has to be called by
 * CacheCallbackServlet to log every cache request in the log file.
 */
public class CacheLog implements Serializable {
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CacheLog.class.getName());
	
	static SimpleDateFormat sdf = new SimpleDateFormat ("ddMMyyyy");
	static SimpleDateFormat logSdf = new SimpleDateFormat ("yyyyMMddHHmmss");
	static String HEADER = "requestTime|requestNo|callbackResponse|appInstance|finalResultCode|finalResultDesc|operation|process[resultCode::resultDesc::remarks];|";
	
	
	static final String LOG_SEPARATOR = "|";
	static final String ARRAY_SEPARATOR = ";";
	
	/**
	 * This method creates a dir(f not exists) and creates cache Log
	 * file for all activity on daily basis with date stamp.
	 * get file path, file name, file extn from Constants.props
	 * @ param p_cacheRequest String
	*/
	public static void println (String p_cacheRequest) throws Exception {
            logger.debug("println() : Entered. ");
            
            PrintWriter log = null;

            String currentDateTime = "";
            
            
            boolean newFile = false;
            //System.out.println("RequestLog : println() : currentDateTime 4 = "+currentDateTime)
            try {
            	currentDateTime = sdf.format(new java.util.Date());
                File dir = null;

                String filePath = Application.getConstantProperty("cacheLogFilePath");
                String fileName = Application.getConstantProperty("cacheLogFileName");
                String fileExtn = Application.getConstantProperty("cacheLogFileExtn");

                dir = new File(filePath);
                if(!dir.isDirectory()) dir.mkdirs();
                //Creating log file name with current date and time
                String fName = fileName+currentDateTime+"."+fileExtn;
                String CFName = dir + File.separator + fName;
                logger.debug("println() : Log File Name = " + CFName);
                File logfile = new File(CFName);
                //if log file does not exists and logging is enabled then creates the log file.
                if(!logfile.exists()){
                	logfile.createNewFile();
                	newFile = true;
                }
                log = new PrintWriter(new FileOutputStream(CFName, true), true);
                if(newFile){
                	log.println(HEADER);
                }
                
                log.println(p_cacheRequest);
            } //end of try
            catch(Exception ex) {
                logger.error("println() : Exception : "+ ex.getMessage());
                throw ex;
            }//end catch
            finally {
                try{
                	if(log != null){
                		log.close();
                	}
                }
                catch(Exception e){
                	logger.error("println() : Exception while closing log file:");
                }
                logger.debug("println() : finally block Exited. ");
            }//end of finally
	}//end of
	
	
	
	public static void println (CacheRequest pCacheRequest, String pRequestXml) throws Exception {
		
		StringBuffer cacheBuff = new StringBuffer();
		List<Cache> cacheList = null;
		
		try {
			cacheBuff.append(logSdf.format(new java.util.Date())).append(LOG_SEPARATOR);
			
			if(pCacheRequest != null){
				cacheBuff.append(SEUtil.nullToString(pCacheRequest.getRequestNo())).append(LOG_SEPARATOR);
				cacheBuff.append(SEUtil.nullToString(pCacheRequest.getResponse())).append(LOG_SEPARATOR);
				cacheBuff.append(SEUtil.nullToString(pCacheRequest.getAppInstance())).append(LOG_SEPARATOR);
				cacheBuff.append(SEUtil.nullToString(pCacheRequest.getResultCode())).append(LOG_SEPARATOR);
				cacheBuff.append(SEUtil.nullToString(pCacheRequest.getResultDesc())).append(LOG_SEPARATOR);
				cacheBuff.append(SEUtil.nullToString(pCacheRequest.getOperation())).append(LOG_SEPARATOR);
				
				cacheList = pCacheRequest.getCaches();
				if(cacheList != null){
					for(Cache cache : cacheList){
						cacheBuff.append(cache.getProcess()+"["+SEUtil.nullToString(cache.getResultCode())+"::"+SEUtil.nullToString(cache.getResultDesc())+"::"+SEUtil.nullToString(cache.getRemarks())+"]").append(ARRAY_SEPARATOR);
					}
				}
				else{
					cacheBuff.append(LOG_SEPARATOR);
				}
			}
			else{
				cacheBuff.append(LOG_SEPARATOR);
				cacheBuff.append(LOG_SEPARATOR);
				cacheBuff.append(LOG_SEPARATOR);
				cacheBuff.append(LOG_SEPARATOR);
				cacheBuff.append(LOG_SEPARATOR);
				cacheBuff.append(LOG_SEPARATOR);
				cacheBuff.append(LOG_SEPARATOR);
			}
			
			//cacheBuff.append(pRequestXml).append(LOG_SEPARATOR);
			
			String logResp = cacheBuff.toString();
			
			println(logResp);
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
} //end of the class
