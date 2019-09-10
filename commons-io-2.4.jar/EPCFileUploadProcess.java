package com.btsl.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.btsl.util.common.Constants;
import com.btsl.util.common.SEUtil;


	public class EPCFileUploadProcess {
		public static final Logger loger = Logger.getLogger(EPCFileUploadProcess.class);
		public static final String DATE_FORMAT = "ddMMyyyyHHmmssss";
		
		
		String epcJsonProcessedFilePath = null;
		
		
		public void parseEpcfile(){
			
			ArrayList<EPCRequestVO> requestList = null;
			String epcJsonFilePath = null;
			
			try {
				epcJsonFilePath = Constants.getProperty("epcJsonFilePath");
				epcJsonProcessedFilePath = Constants.getProperty("epcJsonProcessedFilePath");
				
				File fileList = new File(epcJsonFilePath);
								
				File [] filesArr = fileList.listFiles();
					
				if(filesArr != null && filesArr.length > 0) {
					for(File file1:filesArr) {
							String time = SEUtil.convertUtilDateToString(new java.util.Date(), DATE_FORMAT);
							String t1=time.substring(8, 10);
							loger.info("parseEpcfile() : current hour is"+t1);
							
							int i=Integer.parseInt(t1);
							
				         String filename=file1.getAbsolutePath().substring(file1.getAbsolutePath().lastIndexOf("/") +1);
				         String filedate_time=filename.substring(31, 33);
				         int j=Integer.parseInt(filedate_time)+1;
				         
				         if(i==j) {
							EPCRequestProcess mEPCRequestProcess = new EPCRequestProcess();
							
							requestList = mEPCRequestProcess.readRequestFile(file1);
							
							processRequest(requestList);

							File newFile = new File(epcJsonProcessedFilePath);
							try {
								FileUtils.moveToDirectory(file1,newFile,true);
							} 
							catch (IOException e) {
								// TODO: handle exception
								loger.debug("Error while moving from Upload to moveDirectory="+e.getMessage());
								e.printStackTrace();
							}
					
					 }
				         else{
							System.out.println("File hour and current hour don't match"+ "");	
							break;
				         }
						
					}
				}
				else {
					loger.debug("No more files are present in UploadDirectory wait for file to complete");
				
				}
			}
			catch(Exception e){
				
			}
		}
		
		
		public void processRequest (ArrayList<EPCRequestVO> requestList){
			loger.info("processRequest() : Enter.");
			
			if(requestList == null){
				loger.debug("processRequest() : empty list.");
				return;
			}
			
			Iterator<EPCRequestVO> reqItr = requestList.iterator();
			EPCRequestVO mEPCRequestVO = null;
			EPCRequestProcess mEPCRequestProcess = new EPCRequestProcess();
			
			int totalSize = requestList.size();
			
			while(reqItr.hasNext()){
				mEPCRequestVO = (EPCRequestVO)reqItr.next();
				
				mEPCRequestProcess.sendHttpEpcJsonRequest(mEPCRequestVO);
				
				reqItr.remove();
				
				loger.debug("processRequest() : processed="+(totalSize - requestList.size())+", Out of "+totalSize);
			}
			
		}
		
		
		
	public static void main(String[] args) {
		EPCFileUploadProcess mEPCFileUploadProcess = null;
		
		try {
			mEPCFileUploadProcess = new EPCFileUploadProcess();
			org.apache.log4j.PropertyConfigurator.configure("/home/mbas/BackEndProcess/config/log4j.properties");
				
			Constants.load(null);
			mEPCFileUploadProcess.parseEpcfile();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
