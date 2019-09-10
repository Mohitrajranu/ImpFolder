package com.btsl.process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import com.btsl.util.common.Constants;
import com.btsl.util.common.DbillErrorCodes;
import com.btsl.util.common.SEUtil;

public class EPCRequestProcess  {
	public final Logger logger =Logger.getLogger(EPCRequestProcess.class);
	
	public static final String SEPARATOR = "\\|qwerty786\\|";
	public static final String DATE_FORMAT = "ddMMyyyyHHmmssss";
	public static final String s = "|qwerty786|";
	
	public ArrayList<EPCRequestVO> readRequestFile (File pReqFile){
		String[] recStrArr = null;
		EPCRequestVO mEPCRequestVO = null;
		ArrayList<EPCRequestVO> requestList = new ArrayList<EPCRequestVO>();
		BufferedReader in = null;
		try{
		     in = new BufferedReader(new FileReader(pReqFile));
		    String s;
		    while((s = in.readLine()) != null){
		    	recStrArr = s.split(SEPARATOR);
		    	if(recStrArr != null && recStrArr.length < 2){
				logger.debug("readRequestFile() : Length less then 2");
		    		continue;
		    	}
		    	
		    	if(recStrArr[0] == null || recStrArr[1] == null){
				logger.debug("readRequestFile() : recStrArr[0]="+recStrArr[0]+", recStrArr[1]="+recStrArr[1]);
		    		continue;
		    	}
		    	
		    	
		    	mEPCRequestVO = new EPCRequestVO();
		    	
		    	mEPCRequestVO.setCreatedOn(recStrArr[0]);
		    	mEPCRequestVO.setRequestJson(recStrArr[1]);
		    	
		    	requestList.add(mEPCRequestVO);
		    	
		    }
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try {
				if(in != null){
					in.close();
					in = null;
				}
			} 
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return requestList;
	}
	
	
	public DbillErrorCodes sendHttpEpcJsonRequest (EPCRequestVO pEPCRequestVO) {
		logger.info("sendHttpEpcJsonRequest() : enter.");
	
		URL url = null;
		HttpURLConnection urlConn = null;
		PrintWriter _out = null;
		int httpStatus = 0;
		urlConn = null;
		BufferedReader _in = null;
		StringBuffer buffer = null;
		String responseStr = null;
		String wEPCRequestVO = null;
		String jwEPCRequestVO = null;
		DbillErrorCodes mDbillErrorCodes = DbillErrorCodes.SERVICE_DENY;
		
		String urlStr = null;
	
		try {
			urlStr = Constants.getProperty("epcURL");
		        logger.debug("sendHttpEpcJsonRequest() : urlStr="+urlStr+", jsonContent="+pEPCRequestVO.getRequestJson());
	
			if (!SEUtil.isNullString(urlStr)) {
				url = new URL(urlStr);
				urlConn = (HttpURLConnection) url.openConnection();
				urlConn.setConnectTimeout(10000);
				urlConn.setReadTimeout(10000);
				urlConn.setDoOutput(true);
				urlConn.setRequestProperty("Content-Type", "application/json");
				urlConn.setDoInput(true);
				urlConn.setRequestMethod("POST");
				urlConn.setUseCaches(false);
				urlConn.connect();
	
				try {
					_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream())),true);
					_out.flush();
					_out.println(pEPCRequestVO.getRequestJson());
					_out.flush();
				} 
				catch (Exception e) {
					logger.error("sendHttpEpcJsonRequest() : Exception at send request, Error="+ e.getMessage());
					e.printStackTrace();
				}
	
				try {
					httpStatus = urlConn.getResponseCode();
				}
				catch (Exception e) {
					logger.error("sendHttpEpcJsonRequest() : Exception at getResponseCode, Error="+ e.getMessage());
					e.printStackTrace();
				} 
				finally {
					logger.info("sendHttpEpcJsonRequest() : httpStatus=" + httpStatus);
				}
				if (httpStatus == 200) {
					try {
						_in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
					} catch (Exception e) {
						e.printStackTrace();
					}
	
					String response = null;
					buffer = new StringBuffer();
					while ((response = _in.readLine()) != null) {
						buffer.append(response);
					}
					responseStr = buffer.toString();
					logger.info("sendHttpEpcJsonRequest() : responseStr="	+ responseStr);
	
					mDbillErrorCodes = DbillErrorCodes.SERVICE_ALLOW;
				} 
				else {
					mDbillErrorCodes = DbillErrorCodes.SERVICE_DENY;
				}
			} 
			else {
				mDbillErrorCodes = DbillErrorCodes.BLANK_CALLBACL_URL;
			}
			
		} 
		catch (Exception e) {
			logger.error("sendHttpEpcJsonRequest() : Exception, Error="+ e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {
			try {
				if (_in != null)
					_in.close();
				    _in = null;
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (_out != null)
					_out.close();
				    _out = null;
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				if (url != null)
					url = null;
			} 
			catch (Exception ex) {
				ex.printStackTrace();

			}
			try {
				if (urlConn != null)
					urlConn.disconnect();
				    urlConn = null;
				} catch (Exception ex) {
					ex.printStackTrace();

			}
	
			logger.info("sendHttpEpcJsonRequest() : finally Exit, mDbillErrorCodes="+ mDbillErrorCodes);
			
		}
		wEPCRequestVO = pEPCRequestVO.getCreatedOn();
		jwEPCRequestVO = pEPCRequestVO.getRequestJson();
		this.writeToFile(wEPCRequestVO,jwEPCRequestVO,httpStatus,responseStr,mDbillErrorCodes);
		return mDbillErrorCodes;
	}
     
     public void writeToFile(String wEPCRequestVO, String jwEPCRequestVO, int httpStatus, String responseStr, DbillErrorCodes mDbillErrorCodes) {
    	 
    	 FileOutputStream is = null;
    	 OutputStreamWriter osw = null;
    	 BufferedWriter bfw = null;
    	 String uploadDirectory = null;
    	 String uploadPath = Constants.getProperty("epcJsonProcessedFilePath");
    	 File resLog = null;
    	 try {
    		 
    		 String newtime = SEUtil.convertUtilDateToString(new java.util.Date(), DATE_FORMAT);
    		 String t1=newtime.substring(8, 10);
    		 String t2 = newtime.substring(0, 8);
    		 int i=Integer.parseInt(t1)-1;
    		 uploadDirectory = uploadPath + "//" + "log_SubscriptionEngineJson_" + t2 + i +"00.txt";
    		 resLog = new File(uploadDirectory);
    		 if(resLog.exists() && !resLog.isDirectory()) { 
    			 logger.info("writeToFile() : filealready exist no need to create a new file="+resLog.getName());
    			}
    		 else{
    		 resLog.createNewFile();
    		 }
    		 is = new FileOutputStream(resLog,true);
    		 osw = new OutputStreamWriter(is);
    		 bfw = new BufferedWriter(osw);
    		 bfw.write(wEPCRequestVO);
    		 bfw.write(s);
    		 bfw.write(jwEPCRequestVO);
    		 bfw.write(s);
    		 bfw.write(String.valueOf(httpStatus));
    		 bfw.write(s);
    		 bfw.write(mDbillErrorCodes.getErrorDescription());
    		 bfw.write(s);
    		 bfw.write(responseStr);
    		 bfw.write(s);
    		 bfw.newLine();
			 bfw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	 finally{
    		 try {
 				if (is != null)
 					is.close();
 				    is = null;
 			} 
 			catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			try {
 				if (osw != null)
 					osw.close();
 				    osw = null;
 			} 
 			catch (Exception ex) {
 				ex.printStackTrace();
 			}
 			try {
 				if (bfw != null)
 					bfw.close();
 				    bfw = null;
 			} 
 			catch (Exception ex) {
 				ex.printStackTrace();
 			}
    	 }
    	 
     }
}

