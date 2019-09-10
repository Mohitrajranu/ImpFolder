package com.btsl.process;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.btsl.properties.AutoRenCache;
import com.btsl.properties.IntegerParser;
import com.btsl.properties.ListParser;
import com.btsl.properties.PropertiesCache;
import com.btsl.properties.PropertyValidation;
import com.btsl.properties.StringParser;
import com.btsl.util.common.Constants;
import com.btsl.util.common.DBillProperties;
import com.btsl.util.common.DbConnectionUtil;
import com.btsl.util.common.DbillRequest;
import com.btsl.util.common.GenericConstants;
import com.btsl.util.common.SEConstantsI;
import com.btsl.util.common.SEUtil;

/**
* Copyright(c) 2017, Comviva Ltd.
* All Rights Reserved

*-------------------------------------------------------------------------------
* Author                Date            History
*-------------------------------------------------------------------------------
*Mohit Raj             16/03/2017       Autorenewal based on limitsize
*-------------------------------------------------------------------------------
*/
/**
 * This class is used to make renewal subscription after 30 days using configurable parameters
 */

    public class RenewalProcessDynamicUsers {
	static org.apache.log4j.Category _logger = org.apache.log4j.Category.getInstance(RenewalProcessDynamicUsers.class.getName());
	
	/**
	 * RenewalSubscription for dynamic users default constructor.
	 */
	public RenewalProcessDynamicUsers(){

	}
	private static int sleepCount = 1;
     
	 String limitRecords = null;
	 long startRecords = 1;
	 String delayForRecords = null;
	 static String fileName =null;
	 
	 public static void main(String[] args) {
	 String loadSqlString = null;
	 String tmp = null;
	 String value = null;
	 String key = null;
	 boolean strFlag = false;
	 boolean intFlag = false;
	 boolean chckFlag = false;
	 //Connection con = null;
	 RenewalProcessDynamicUsers mRenewalProcess=null;
	 RenewalProcessVo mRenewalProcessVo = null;
	 
	 try{
		 
	     fileName = args[0];
	     org.apache.log4j.PropertyConfigurator.configure("/home/mbas/BackEndProcess/config/log4j.properties");
	        AutoRenCache.load(args[0]);
			Set <String> hs = PropertiesCache.getInstance().getAllPropertyNames();
			Set <String> hs1 = AutoRenCache.getInstance().getAllPropertyNames();
			List<String> common = new ArrayList<String>(hs);
			common.retainAll(hs1);
			PropertyValidation pVal = new PropertyValidation();
			pVal.setStringBehavior(new StringParser());
			pVal.setNumberBehavior(new IntegerParser());
			pVal.setListBehavior(new ListParser());

			Iterator<String> iter = common.iterator();

              _logger.debug("The Value fetched from the property file is :");
        
			   while (iter.hasNext()) {
			   try
			   {	
				   tmp=iter.next();
				   
			        key=PropertiesCache.getInstance().getProperty(tmp);
			        if(key.equalsIgnoreCase("VarChar")){
			        	value = AutoRenCache.getInstance().getProperty(tmp);
			            strFlag=pVal.performStrValidation(tmp,value);
			            if(!strFlag){
			            	chckFlag = true;
			            	 pVal.getStringListdata().add(tmp+":"+value);
			   		 }
			       }
			        
			        else if(key.equalsIgnoreCase("Integer") || key.equalsIgnoreCase("Float") || key.equalsIgnoreCase("Double") 
			        		|| key.equalsIgnoreCase("Long")){
			        	value = AutoRenCache.getInstance().getProperty(tmp);
			        	intFlag=pVal.performNumValidation(tmp,value,key);
			        	if(!intFlag){
			        		chckFlag = true;
			        		 pVal.getStringListdata().add(tmp+":"+value);
			   		        }
			        }
			        
				}
				catch(Exception ex)
				{
					_logger.error(ex.getMessage());
				}
			}  
		if(chckFlag)
		{
			_logger.debug("The Value fetched from the property file "+pVal.getStringListdata()+" is Incorrect so set the correct value.");
		     System.exit(0);
		 }
	    // boolean isMysqlDb =false;
	     String isOracledb="";
	     
	      try {
           Constants.load(null);
           DBillProperties.load(null);
          // org.apache.log4j.PropertyConfigurator.configure("/home/mbas/BackEndProcess/config/log4j.properties");
           mRenewalProcess = new RenewalProcessDynamicUsers();
           mRenewalProcessVo = new RenewalProcessVo();
           mRenewalProcess.setRenParam(mRenewalProcessVo);
	       }
	       catch (Exception e) {
	        System.err.println("Exception during loding of constants Files: "+e.getMessage());
	        }
	        /*con = DbConnectionUtil.getConnection();
	        if(con == null){
	            _logger.error("main() : could not establish data base connection");
	            return;
	        }*/
	        isOracledb=Constants.getProperty("isOracledbforBackend");
	        if(!SEUtil.isNullString(isOracledb))
	        {
	        isOracledb=SEConstantsI.DISABLED;
	        }
	        System.out.println("get Constants values :isOracledbforBackend="+isOracledb);
	        if (SEConstantsI.ENABLED.equalsIgnoreCase(isOracledb))
	        {
	        	//isMysqlDb=false;
	        }
	        if(!SEUtil.isNullString(mRenewalProcessVo.getSrcTablename()))
	        {
	        	if(!("T_SUB_SERVICE_INFO".equalsIgnoreCase(mRenewalProcessVo.getSrcTablename()))){
	        	 loadSqlString =  "SELECT * FROM (SELECT DISTINCT SI.subs_type,  SI.MSISDN , SI.REGION_CODE, SI.OFFER_ID,  "
	     	      		+ "SI.PLAN_ID,  SI.AUTO_RENEW ,SI.REQ_PLAN_ID, SI.REQ_NEXT_PLAN_ID, SI.CUST_SERVICE_ID,  "
	     	      		+ "SI.SERVICE_TYPE,   SI.SERVICE_ID, SI.BEARER_ID,  SI.SERVICE_NODE, SI.CONTENT_ID,   "
	     	      		+ "SI.CATEGORY, SI.TRXN_ID, SI.PARAM1,   SI.PARAM2, SI.PARAM3, SI.PARAM4, SI.PARAM5,"
	     	      		+ " SI.CUST_STATUS, SI.LANGUAGE_ID, SI.CHARGING_AMOUNT, SI.LINKED_SERVICE_ID,   "
	     	      		+ "SI.LINKED_SERVICE_TYPE,   SI.OPERATION,  SI.CRM_ID,  SI.CALLED_PARTY, SI.SUBSCRIPTION_ID , "
	     	      		+ "SI.PRODUCT_ORDER_ID,  SI.MODE_OF_PAYMENT , SM.SERVICE_GROUP ,ROWNUM rn FROM "+mRenewalProcessVo.getSrcTablename()+" SI, "
	     	      		+ "T_D_CNF_SERVICE_MASTER SM WHERE SI.AUTO_RENEW='Y'  AND SI.CUST_STATUS = 'A' AND "
	     	      		+ "SI.RENEWAL_DATE < trunc(sysdate+1) AND SI.SERVICE_ID = SM.SERVICE_ID AND SI.SERVICE_TYPE = SM.SERVICE_TYPE ";
	     	       
	        	}
	        	else
	        	{
		        	 loadSqlString =  "SELECT * FROM (SELECT DISTINCT SI.subs_type,  SI.MSISDN , SI.REGION_CODE, SI.OFFER_ID,  "
			     	      		+ "SI.PLAN_ID,  SI.AUTO_RENEW ,SI.REQ_PLAN_ID, SI.REQ_NEXT_PLAN_ID, SI.CUST_SERVICE_ID,  "
			     	      		+ "SI.SERVICE_TYPE,   SI.SERVICE_ID, SI.BEARER_ID,  SI.SERVICE_NODE, SI.CONTENT_ID,   "
			     	      		+ "SI.CATEGORY, SI.TRXN_ID, SI.PARAM1,   SI.PARAM2, SI.PARAM3, SI.PARAM4, SI.PARAM5,"
			     	      		+ " SI.CUST_STATUS, SI.LANGUAGE_ID, SI.CHARGING_AMOUNT, SI.LINKED_SERVICE_ID,   "
			     	      		+ "SI.LINKED_SERVICE_TYPE,   SI.OPERATION,  SI.CRM_ID,  SI.CALLED_PARTY, SI.SUBSCRIPTION_ID , "
			     	      		+ "SI.PRODUCT_ORDER_ID,  SI.MODE_OF_PAYMENT , SM.SERVICE_GROUP ,ROWNUM rn FROM T_SUB_SERVICE_INFO SI, "
			     	      		+ "T_D_CNF_SERVICE_MASTER SM WHERE SI.AUTO_RENEW='Y'  AND SI.CUST_STATUS = 'A' AND "
			     	      		+ "SI.RENEWAL_DATE < trunc(sysdate+1) AND SI.SERVICE_ID = SM.SERVICE_ID AND SI.SERVICE_TYPE = SM.SERVICE_TYPE ";
			     	       
			        	}
	        }
	        else
        	{
	        	 loadSqlString =  "SELECT * FROM (SELECT DISTINCT SI.subs_type,  SI.MSISDN , SI.REGION_CODE, SI.OFFER_ID,  "
		     	      		+ "SI.PLAN_ID,  SI.AUTO_RENEW ,SI.REQ_PLAN_ID, SI.REQ_NEXT_PLAN_ID, SI.CUST_SERVICE_ID,  "
		     	      		+ "SI.SERVICE_TYPE,   SI.SERVICE_ID, SI.BEARER_ID,  SI.SERVICE_NODE, SI.CONTENT_ID,   "
		     	      		+ "SI.CATEGORY, SI.TRXN_ID, SI.PARAM1,   SI.PARAM2, SI.PARAM3, SI.PARAM4, SI.PARAM5,"
		     	      		+ " SI.CUST_STATUS, SI.LANGUAGE_ID, SI.CHARGING_AMOUNT, SI.LINKED_SERVICE_ID,   "
		     	      		+ "SI.LINKED_SERVICE_TYPE,   SI.OPERATION,  SI.CRM_ID,  SI.CALLED_PARTY, SI.SUBSCRIPTION_ID , "
		     	      		+ "SI.PRODUCT_ORDER_ID,  SI.MODE_OF_PAYMENT , SM.SERVICE_GROUP ,ROWNUM rn FROM T_SUB_SERVICE_INFO SI, "
		     	      		+ "T_D_CNF_SERVICE_MASTER SM WHERE SI.AUTO_RENEW='Y'  AND SI.CUST_STATUS = 'A' AND "
		     	      		+ "SI.RENEWAL_DATE < trunc(sysdate+1) AND SI.SERVICE_ID = SM.SERVICE_ID AND SI.SERVICE_TYPE = SM.SERVICE_TYPE ";
		     	       
		        	}
               // con.setAutoCommit(false);
	        
	        mRenewalProcess.loadDynamicUsers(loadSqlString,mRenewalProcessVo);
	       
	    }
	    catch(Exception ex){
	        _logger.error("main() : exception "+ex.getMessage());
	        ex.printStackTrace();
	    }
	    finally{/*
		    try{
		        if(con!=null) 
		        con.close();
		        con = null;
		    }
		    catch(Exception ex){
		            _logger.error("main() :  error while closing connection "+ex.getMessage());
		    }
		    _logger.info("main() Exiting Time="+new Date());
	    */
	    	_logger.info("main() Exiting Time="+new Date());	
	    }
	}
	  private boolean deleteRenewalSubs (DbillRequest p_req) {
	    	_logger.info("deleteRenewalSubs() : p_service = "+p_req.getServiceType()+", p_planId="+p_req.getPlanId()+"subscription_id="+p_req.getSubscriptionId());
	        
	    	Connection p_con = null;
	        PreparedStatement pstmt = null;
	        ResultSet rs = null;
	        boolean deleteFlag = false;
	        int delCount = 0;
	        
	        try{
	        	p_con = DbConnectionUtil.getConnection();
	            if(p_con == null){
	                 _logger.error("deleteRenewalSubs() : could not establish data base connection");
	                 return deleteFlag;
	            }
	           p_con.setAutoCommit(false);
	            
	            String queryCountUsers = "delete from T_SUB_SERVICE_INFO_DIG_REN where SUBSCRIPTION_ID = ?";

	            pstmt = p_con.prepareStatement(queryCountUsers);
	            pstmt.setString(1, p_req.getSubscriptionId());
	            delCount = pstmt.executeUpdate();


	            if(delCount > 0){
	                deleteFlag = true;
	                p_con.commit();
	                _logger.info("deleteRenewalSubs() : Record of the following Subscription Id deleted = "+p_req.getSubscriptionId());
	            }
	        }
	        catch(Exception ex){
	            _logger.error("deleteRenewalSubs() : error while loading user list, error ="+ex.getMessage());
	        }
	        finally{

    	    	try{
    	            if(p_con!=null) {
    	            	p_con.close();
    	            	p_con =null;
    	            }
    	        }catch(Exception ex){
    	            _logger.error("deleteRenewalSubs() : error while closing Connection "+ex.getMessage());
    	        }
	            try{
	                if(rs!=null){
	                	rs.close();
	                	rs = null;
	                }
	            }catch(Exception ex){
	                _logger.error("deleteRenewalSubs() : error while closing resultset, error="+ex.getMessage());
	            }
	            try{
	                if(pstmt!=null) {
	                	pstmt.close();
	                	pstmt = null;
	                }
	            }catch(Exception ex){
	                _logger.error("deleteRenewalSubs() : error while closing prepared statement, error="+ex.getMessage());
	            }
	            
	            _logger.info("deleteRenewalSubs() finally Exit, deleteFlag="+deleteFlag);
	        }
	        return deleteFlag;
	    }


	        private boolean insertUsers (DbillRequest p_req) {
	        _logger.info("insertUsers() : subscriptionId = "+p_req.getSubscriptionId());
	        
	        Connection p_con = null;
	        PreparedStatement pstmt = null;
	        ResultSet rs = null;
	        boolean deleteFlag = false;
	        int ins = 0;
	        try{
	        	p_con = DbConnectionUtil.getConnection();
	            if(p_con == null){
	                 _logger.error("insertUsers() : could not establish data base connection");
	                 return deleteFlag;
	            }
	            p_con.setAutoCommit(false);
	            
	            String queryCountUsers = "insert into T_SUB_RENEWAL_API_CALL_STATUS(SUBSCRIPTION_ID,CREATION_TIME,CREATED_ON)"
	            		+ "  values(?,?,?)";

	            pstmt = p_con.prepareStatement(queryCountUsers);
	            pstmt.setString(1, p_req.getSubscriptionId());
	            pstmt.setString(2, SEUtil.convertUtilDateToString(new Date(), "dd-MM-yyyy HH:mm:ss"));
	            pstmt.setString(3, SEUtil.convertUtilDateToString(new Date(), "dd-MM-yyyy"));
	            ins = pstmt.executeUpdate();
	            if(ins > 0){
	                deleteFlag = true;
	                p_con.commit();
	                _logger.info("insertUsers() : Record inserted in the database for subscriptionId = "+p_req.getSubscriptionId());
	            }
	            else{
	            	 _logger.info("insertUsers() : Record already existed in the database for subscriptionId = "+p_req.getSubscriptionId());	
	            }
	        }
	        catch(Exception ex){
	            _logger.error("insertUsers() : error while inserting records as Record is already present in the Table.");
	        }
	        finally{

    	    	try{
    	            if(p_con!=null) {
    	            	p_con.close();
    	            	p_con =null;
    	            }
    	        }catch(Exception ex){
    	            _logger.error("insertUsers() : error while closing Connection "+ex.getMessage());
    	        }
    	    	
	            try{
	                if(rs!=null) 
	                {
	                	rs.close();
	                	rs = null;
	                }
	            }catch(Exception ex){
	                _logger.error("insertUsers() : error while closing resultset "+ex.getMessage());
	            }
	            try{
	                if(pstmt!=null) 
	                {
	                	pstmt.close();
	                	pstmt = null;
	                }
	            }catch(Exception ex){
	                _logger.error("insertUsers() : error while closing prepared statement "+ex.getMessage());
	            }
	            
	            _logger.info("insertUsers() Exiting Time="+new Date());
	        }
	        return deleteFlag;
	    }

	 

	/**
	 * This method is used to make charging for users
	 * Creation date: (20-Jun-07 1:48:53 PM)
	 * @param p_users java.util.ArrayList
	 * @param p_con Connection
	 */
	
	  private int processRepeatCDR (DbillRequest p_req) {

          DBillRequestProcess mDBillRequestProcess = null;
	    int res = 0;
          
          try {
              mDBillRequestProcess = new DBillRequestProcess();
              
              if(GenericConstants.AUTO_RENEW.Y.equals(p_req.getRenFlag())){
  	    		res = mDBillRequestProcess.processSubscription(p_req);
              }
  	    	else if(GenericConstants.AUTO_RENEW.N.equals(p_req.getRenFlag())){
  	    		res = mDBillRequestProcess.processUnSubscription(p_req);
  	    	}
              return res;
          }
          catch(Exception ex){
              _logger.error("processRepeatCDR() : exception while making a entry into cdr "+ex.getMessage());
              ex.printStackTrace();
              return res;
          }
          finally{
              if(mDBillRequestProcess.getResponse() != null){
              	mDBillRequestProcess.setResponse(null);
              }
              if(mDBillRequestProcess != null){
              	mDBillRequestProcess = null;
              }
              _logger.info("processRepeatCDR() : finally Exit.");
          }
      }

	  public static String process(String s) {			
			s = (s == null || (SEUtil.isNullString(s))) ? "" : s; 
			if(!SEUtil.isNullString(s)){	
				s = s.replaceAll("\n|\r", " ");
			}
			if(!SEUtil.isNullString(s) && "null|".equals(s)){	
				s = "|";
			}
			return s;
		}
	/**
	 * This method is used to load users list for whom  repeat CDR entry has to be made
	 * @param serviceGroup 
	 * @param subsTypeForService 
	 * @param regionCodeForService 
	 * @param servicesForRenewal 
	 * @param p_users java.sql.Connection
	 * @return java.util.ArrayList
	 */
       	
	private StringBuffer prepareQuery(String _loadSqlString, RenewalProcessVo aRenewalProcessVo)
	{
		StringBuffer _queryStrBuff = null;
		_logger.info("RenewalProcessDynamicUsers : servicesForRenewal="+aRenewalProcessVo.getServiceType()+", SERVICE_ID="+aRenewalProcessVo.getServiceId()+", subsTypeForService="+aRenewalProcessVo.getSubsTypeForService()+", serviceGroup="+aRenewalProcessVo.getServiceGroup());  

        try {
        	_queryStrBuff= new StringBuffer(_loadSqlString);
  	        if((!SEUtil.isNullString(aRenewalProcessVo.getServiceGroup()) && 
  	        aRenewalProcessVo.getServiceGroup().length() > 0 && !SEUtil.isNullString(aRenewalProcessVo.getServiceType())
  	        && aRenewalProcessVo.getServiceType().length() > 0) || (SEUtil.isNullString(aRenewalProcessVo.getServiceGroup()) 
  	        && !SEUtil.isNullString(aRenewalProcessVo.getServiceType()) && aRenewalProcessVo.getServiceType().length() > 0))
  	        {
  	        	_queryStrBuff.append(" AND (SI.SERVICE_TYPE IN (" +aRenewalProcessVo.getServiceType()+ ")) ");
  	        	
  	        }
  	        if(SEUtil.isNullString(aRenewalProcessVo.getServiceType()) &&
  	        !SEUtil.isNullString(aRenewalProcessVo.getServiceGroup()) && aRenewalProcessVo.getServiceGroup().length() > 0
  	        )
  	        {
  	        	_queryStrBuff.append(" AND SM.SERVICE_GROUP = '"+aRenewalProcessVo.getServiceGroup()+"' ");
  	        }
  	      if(!SEUtil.isNullString(aRenewalProcessVo.getServiceId()) && aRenewalProcessVo.getServiceId().length() > 0
  	  	        )
  	  	        {
  	  	        	_queryStrBuff.append(" AND SI.SERVICE_ID = '"+aRenewalProcessVo.getServiceId()+"' ");
  	  	        }
  	        
		} catch (Exception e) {
			e.printStackTrace();
		}
       
		return _queryStrBuff;
		
	}
	
	public void sleep(RenewalProcessVo mRenewalProcessVo){
		_logger.info("sleep() : enter");
		
		try {
			int sleepTime = mRenewalProcessVo.getSleepTime();
			int sleepMultiplier = mRenewalProcessVo.getSleepTimeMultiplier();
			
			int finalSleepTime = 0;
			 if(sleepMultiplier == -1){
				 finalSleepTime = sleepTime;
			 }else{
				 finalSleepTime = sleepTime * sleepCount; 
			 }
			if(sleepCount == sleepMultiplier)
				{
				sleepCount = 0;
				}
			try {
				Thread.sleep(finalSleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sleepCount++;
			_logger.info("sleep() : exit, thread going to sleep, finalSleepTime="+finalSleepTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private boolean deleteUsers (Connection p_con, DbillRequest p_req) {
        _logger.info("deleteUsers() : subscriptionId = "+p_req.getSubscriptionId()+", userId="+p_req.getCallingParty());
        
       // Connection p_con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean deleteFlag = false;
        int del = 0;
        
        try{
        	//p_con = DbConnectionUtil.getConnection();
            if(p_con == null){
                 _logger.error("main() : could not establish data base connection");
                 return deleteFlag;
            }
            p_con.setAutoCommit(false);
            
            String queryCountUsers = "delete from T_SUB_RENEWAL_API_CALL_STATUS where SUBSCRIPTION_ID = ?";

            pstmt = p_con.prepareStatement(queryCountUsers);
            pstmt.setString(1, p_req.getSubscriptionId());
            del = pstmt.executeUpdate();
            if(del > 0){
                deleteFlag = true;
                p_con.commit();
                _logger.info("deleteUsers() : deleted = "+p_req.getSubscriptionId()+" ,delete Query="+queryCountUsers);
            }
        }
        catch(Exception ex){
            _logger.error("deleteUsers() : error while loading user list "+ex.getMessage());
        }
        finally{

	    	/*try{
	            if(p_con!=null) {
	            	p_con.close();
	            	p_con =null;
	            }
	        }catch(Exception ex){
	            _logger.error("deleteUsers() : error while closing Connection "+ex.getMessage());
	        }*/
            try{
                if(rs!=null) rs.close();
            }catch(Exception ex){
                _logger.error("deleteUsers() : error while closing resultset "+ex.getMessage());
            }
            try{
                if(pstmt!=null) pstmt.close();
            }catch(Exception ex){
                _logger.error("deleteUsers() : error while closing prepared statement "+ex.getMessage());
            }
            
            _logger.info("deleteUsers() Exiting Time="+new Date());
        }
        return deleteFlag;
    }
	//#fileCreationFlag
	public void setRenParam (RenewalProcessVo mRenewalProcessVo){
		
		int sleepTimeMultiplier = 0;
		int sleepTime = 0;
		int recLimit = 0;
		int delayRecords = 0;
		try {
                      
                      try {
                         AutoRenCache.load(fileName);
                        } catch (IOException e1) {
                        e1.printStackTrace();
                        }

			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("serviceType"))){				
				mRenewalProcessVo.setServiceType(AutoRenCache.getInstance().getProperty("serviceType"));
			}
			
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("colName"))){				
				mRenewalProcessVo.setOrderoflogging(AutoRenCache.getInstance().getProperty("colName"));
			}
			//tableName
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("tableName"))){				
				mRenewalProcessVo.setSrcTablename(AutoRenCache.getInstance().getProperty("tableName"));
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("fileName"))){				
				mRenewalProcessVo.setFileName(AutoRenCache.getInstance().getProperty("fileName"));
			}
			
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("filePath"))){				
				mRenewalProcessVo.setFileName(AutoRenCache.getInstance().getProperty("filePath"));
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("fileCreationFlag"))){				
				mRenewalProcessVo.setFileCreationFlag(AutoRenCache.getInstance().getProperty("fileCreationFlag"));
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("serviceId"))){				
				mRenewalProcessVo.setServiceId(AutoRenCache.getInstance().getProperty("serviceId"));
			}
			
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("regionCode"))){				
				mRenewalProcessVo.setRegionCodeForService(AutoRenCache.getInstance().getProperty("regionCode"));
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("subscriptionType"))){				
				mRenewalProcessVo.setSubsTypeForService(AutoRenCache.getInstance().getProperty("subscriptionType"));
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("serviceGroup"))){				
				mRenewalProcessVo.setServiceGroup(AutoRenCache.getInstance().getProperty("serviceGroup"));
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("runningMode"))){				
				mRenewalProcessVo.setModeofapplication(AutoRenCache.getInstance().getProperty("runningMode"));
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("recordLimit"))){	
				try {
					recLimit = Integer.parseInt(AutoRenCache.getInstance().getProperty("recordLimit"));
					} 
				    catch (NumberFormatException e) {
				    	_logger.error("Exception caught at setSdrParam() RecordLimit:"+recLimit+" and Exception is"+e.getMessage());
				    	recLimit = SEConstantsI.RENEWAL_RECORD_LIMIT;
				    }
				
				mRenewalProcessVo.setReclimit(recLimit);
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("delayRecords"))){	
				try {
					delayRecords = Integer.parseInt(AutoRenCache.getInstance().getProperty("delayRecords"));
					} 
				    catch (NumberFormatException e) {
				    	_logger.error("Exception caught at setSdrParam() delayRecords:"+delayRecords+" and Exception is"+e.getMessage());
				    	delayRecords = SEConstantsI.RECORD_DELAY;
				    }
				
				mRenewalProcessVo.setDelayReqRecords(delayRecords);
			}
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("sleepTime"))){	
				try {
					sleepTime = Integer.parseInt(AutoRenCache.getInstance().getProperty("sleepTime"));
					} 
				    catch (NumberFormatException e) {
				    	_logger.error("Exception caught at setSdrParam() sleepTime:"+sleepTime+" and Exception is"+e.getMessage());
				    	sleepTime = SEConstantsI.SLEEP_TIME;
				    }
					
				mRenewalProcessVo.setSleepTime(sleepTime);
			}
			
			if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("maxSleepCounter"))){	
				try {
				 sleepTimeMultiplier = Integer.parseInt(AutoRenCache.getInstance().getProperty("maxSleepCounter"));
				} 
			    catch (NumberFormatException e) {
			    	_logger.error("Exception caught at setSdrParam() sleepTimeMultiplier:"+sleepTimeMultiplier+" and Exception is"+e.getMessage());
					sleepTimeMultiplier = SEConstantsI.SLEEP_MULTIPLIER;
			    }
					
				mRenewalProcessVo.setSleepTimeMultiplier(sleepTimeMultiplier);
			}
		} catch (NumberFormatException e) {
			_logger.error("Exception caught at setSdrParam() Exit: and Exception is"+e.getMessage());
		}
	}

       	private List<DbillRequest> loadDynamicUsers (String p_loadSqlString,RenewalProcessVo autoRenVo) {
    	    PreparedStatement psmtLoadUsers = null;
    	    ResultSet rsLoadUsers = null;
    	    List<DbillRequest> users = null;
    	    long renewalFetchLimit = 0;
    	    long limitSize = 0;
    	    StringBuffer queryStrBuff = null;
    	    String circle =null;
    	    String subType =null;
    	    String[] subscriptionType =null;
    	    String[] regionCode=null;
    	    StringBuilder sb = null;
    		String orderLogging[] = null;
    		FileWriter fWriter = null;
    		BufferedWriter bWriter = null;
    		String FileDir = null;
    		String FileName = null;
    		String pathname = null;
    		String today = null;
    		FileInputStream fis = null;
    		Connection p_con = null;
    	    try {
    	    	
    	    	Date date = new Date();
                DateFormat df = new SimpleDateFormat("ddMMyyyyHH");
                
    	        users = new ArrayList<DbillRequest>();
    	        Properties props = new Properties();
    	        
    	        limitRecords = String.valueOf(autoRenVo.getReclimit());
    	        try {
					renewalFetchLimit = Long.parseLong(limitRecords);
					limitSize = Long.parseLong(limitRecords);
					if(!SEUtil.isNullString(autoRenVo.getRegionCodeForService())){		
	    	        	circle=autoRenVo.getRegionCodeForService();
	        			regionCode= circle.split("\\,", -1);
	        			
	        			
	    			}
					if(!SEUtil.isNullString(autoRenVo.getSubsTypeForService())){		
						subType=autoRenVo.getSubsTypeForService();
						subscriptionType= subType.split("\\,", -1);
	    			}
					
					}
    	        catch (Exception e) {
    	        	if(e instanceof NumberFormatException){
					renewalFetchLimit = GenericConstants.RENEWAL_FETCH_LIMIT;
					limitSize = 0;
				     }
    	        	e.printStackTrace();
    	        	_logger.error("loadDynamicUsers() : Connection is null"+e.getMessage());
    	        	}
    	        
    	        
    	        while(true)
    	        {
    	        	
    	        try {
    	        	queryStrBuff = this.prepareQuery(p_loadSqlString,autoRenVo);
    	        	if(limitRecords.trim().length() > 0 && !("0").equals(limitRecords))
    	            {
    	            	queryStrBuff.append(" AND ROWNUM <= "+renewalFetchLimit+")"+" Where rn >= "+startRecords);
    	            }
    	        	else  {
    	            	queryStrBuff.append(")");
    	            }
    	        	
    	        	p_con = DbConnectionUtil.getConnection();
        	        if(p_con == null){
        	    		_logger.error("loadDynamicUsers() : Connection is null");
        	    		return users;
        	    	}
                p_con.setAutoCommit(false);
    	        psmtLoadUsers = p_con.prepareStatement(queryStrBuff.toString(),ResultSet.TYPE_SCROLL_SENSITIVE, 
                ResultSet.CONCUR_UPDATABLE);
    	        rsLoadUsers = psmtLoadUsers.executeQuery();
    	        Iterator<DbillRequest> userItr = null;
    	        DbillRequest mDbillRequest = null;
    	    //    DbillRequest mDbillRequestTemp = null;
    	        long sequenceCount= 1000;
    	        long rowCount = 0; 
    	      //  int size =0;
    	        if (!rsLoadUsers.first())
    	        {
    	            _logger.info("RenewalProcessDynamicUsers : loadDynamicUsers() : final queryStr = "+queryStrBuff.toString());
        	        _logger.info("result set is empty");
        	        startRecords = 1;
        	        renewalFetchLimit = limitSize;
        	        FileInputStream fin  = null;
        	        try {
        	        	fin = new FileInputStream(fileName);
						props.load(fin);
						if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("runningMode"))){
						
						if("O".equals(props.getProperty("runningMode").trim())){
							break;
						}
						
			}
						this.sleep(autoRenVo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						_logger.info("RenewalProcessDynamicUsers : loadDynamicUsers() Error while closing property file:"+e.getMessage());
					}
        	        finally{
        	        	if(fin != null){
        	        		fin.close();
        	        		fin = null;
        	        	}
        	        }
        	        
        	        }
    	        else{
    	        	_logger.info("RenewalProcessDynamicUsers : loadDynamicUsers() : final queryStr = "+queryStrBuff.toString());
    	        	rsLoadUsers.beforeFirst();
    	        	/*rsLoadUsers.last();
				    size = rsLoadUsers.getRow();
				    rsLoadUsers.beforeFirst();
				    if(size <= (autoRenVo.getReclimit()/2)){
				    	this.sleep(autoRenVo);
				    }*/
				 //   else{
				    	while(rsLoadUsers.next()){
		    	        	try{
		    	        		if(!SEUtil.isNullString(autoRenVo.getRegionCodeForService())){
		    	        			autoRenVo.setCircle(rsLoadUsers.getString("region_code"));
				    	        if(!Arrays.asList(regionCode).contains(autoRenVo.getCircle())){
				    	        	continue;
				    	        }}
		    	        		if(!SEUtil.isNullString(autoRenVo.getSubsTypeForService())){
		    	        			autoRenVo.setSubType(rsLoadUsers.getInt("subs_type")+"");
		    	        			if(!Arrays.asList(subscriptionType).contains(autoRenVo.getSubType())){
		    	        				continue;
		    	        			}}
		    	        		if(!SEUtil.isNullString(autoRenVo.getFileCreationFlag())){
		    	        	        if("Y".equals(autoRenVo.getFileCreationFlag())){
		    	        	        	sb =  new StringBuilder("");
		    	    	        		if(SEUtil.isNullString(autoRenVo.getOrderoflogging()) || 
		    	    	        		SEUtil.isNullString(autoRenVo.getFileName()) || 
		    	    	        		SEUtil.isNullString(autoRenVo.getFilePath())){
		    	    	        			_logger.info("FilePath,FileName and ColumnName are not properly configured in the"
		    	    	        					+ " Property, reconfigure them correctly as exiting the main application");
		    	    	        		System.exit(0);
		    	    	        		}
		    							if(autoRenVo.getOrderoflogging() != null){
		    								orderLogging = autoRenVo.getOrderoflogging().split(",");

		    								for(int index = 0; index < orderLogging.length; index++ ){

		    									if (index == orderLogging.length - 1){
		    										sb.append(process(rsLoadUsers.getString(orderLogging[index])));
		    									}
		    									else{
		    										sb.append(process(rsLoadUsers.getString(orderLogging[index]) + "|"));
		    									}
		    								}
		    							}

		    	        	        today = df.format(date);
		    	                    
		    	        			FileDir = autoRenVo.getFilePath();
		    	        			FileName = FileDir+autoRenVo.getFileName();

		 				            pathname = FileName+"_"+rsLoadUsers.getString("SERVICE_TYPE").trim()+"_"+today+".csv";
		 				        	 
		 						   try{
		 				                fWriter = new FileWriter(pathname.trim(),true);
		 				                bWriter = new BufferedWriter(fWriter);
		 				                bWriter.write(sb.toString());
		 				                bWriter.newLine();
		 					        } 
		 					        catch(Exception e){
		 					             e.printStackTrace();
		 					        }
		 					        finally{
		 					             try {
		 					            	 if(bWriter != null) {
		 					            		 bWriter.flush();
		 					            		 bWriter.close();
		 					            	 }
		 								 } 
		 					             catch (IOException e) {
		 					            	 // TODO Auto-generated catch block
		 					            	 e.printStackTrace();
		 								 } 
		 					         }
		 				        
		    	        	        }
		    	        	        }
		    	        								
		                        mDbillRequest = new DbillRequest();
		                        mDbillRequest.setCallingParty(rsLoadUsers.getString("MSISDN"));
		                        mDbillRequest.setServiceId(rsLoadUsers.getString("SERVICE_ID"));
		                        mDbillRequest.setServiceType(rsLoadUsers.getString("SERVICE_TYPE"));
		                        mDbillRequest.setServiceNode(rsLoadUsers.getString("SERVICE_NODE"));
		                        mDbillRequest.setContentId(rsLoadUsers.getString("CONTENT_ID"));
		                        mDbillRequest.setCategory(rsLoadUsers.getString("CATEGORY"));
		                        mDbillRequest.setBearerId(rsLoadUsers.getString("bearer_id"));
		                        mDbillRequest.setCurrentStatus(rsLoadUsers.getString("cust_status"));
		                        mDbillRequest.setBundleType(GenericConstants.FLAG_NO);
		                        mDbillRequest.setTransId(rsLoadUsers.getString("trxn_id"));
		                        mDbillRequest.setLanguageId(rsLoadUsers.getString("language_id"));
		                        mDbillRequest.setPlanId(rsLoadUsers.getString("plan_id"));
		                        mDbillRequest.setOfferId(rsLoadUsers.getString("OFFER_ID"));
		                        mDbillRequest.setCustServiceId(rsLoadUsers.getString("CUST_SERVICE_ID"));
		                        mDbillRequest.setRenFlag(GenericConstants.enumValue(GenericConstants.AUTO_RENEW.class, rsLoadUsers.getString("auto_renew")));
		                        mDbillRequest.setOptionalParameter1(rsLoadUsers.getString("param1"));
		                        mDbillRequest.setOptionalParameter2(rsLoadUsers.getString("param2"));
		                        mDbillRequest.setOptionalParameter3(rsLoadUsers.getString("param3"));
		                        mDbillRequest.setOptionalParameter4(rsLoadUsers.getString("param4"));
		                        mDbillRequest.setOptionalParameter5(rsLoadUsers.getString("param5"));
		                        mDbillRequest.setRegionId(rsLoadUsers.getString("region_code"));
		                        mDbillRequest.setSubscrFlag(GenericConstants.SUBSCRIBER_FLAG.Y);
		                       if("1".equalsIgnoreCase(rsLoadUsers.getInt("subs_type")+""))
		                       mDbillRequest.setSubsType(GenericConstants.SUBSCRIBER_TYPE.PREPAID);
		                       else if("2".equalsIgnoreCase(rsLoadUsers.getInt("subs_type")+""))
		                       mDbillRequest.setSubsType(GenericConstants.SUBSCRIBER_TYPE.POSTPAID);
		                       else if("3".equalsIgnoreCase(rsLoadUsers.getInt("subs_type")+""))
		                       mDbillRequest.setSubsType(GenericConstants.SUBSCRIBER_TYPE.HYBRID);
		                       mDbillRequest.setCrmId(rsLoadUsers.getString("CRM_ID"));
		                       mDbillRequest.setSubscriptionId(rsLoadUsers.getString("SUBSCRIPTION_ID"));
		                       mDbillRequest.setCalledParty(rsLoadUsers.getString("CALLED_PARTY"));
		                       mDbillRequest.setProductOrderId(rsLoadUsers.getString("PRODUCT_ORDER_ID"));
		                       mDbillRequest.setModeOfPayment(GenericConstants.PAYMENT_MODE_CHARGE);
		                       mDbillRequest.setServiceGroup(rsLoadUsers.getString("SERVICE_GROUP"));
		                       mDbillRequest.setSequenceNo(String.valueOf(sequenceCount++));
		                   /* if(GenericConstants.FLAG_YES.equalsIgnoreCase(Constants.getProperty("singleRequestForAllContents").trim())){
		                    if(mDbillRequestTemp!=null){
		                            if(mDbillRequestTemp.getCallingParty().equalsIgnoreCase(mDbillRequest.getCallingParty()) &&  mDbillRequestTemp.getServiceId().equalsIgnoreCase(mDbillRequest.getServiceId()))
		                            {
		                                    DbillRequest usersDbillRequestTemp = (DbillRequest) users.get(users.size()-1);
		                                    users.remove(users.size()-1);
		                                    usersDbillRequestTemp.setContentId(usersDbillRequestTemp.getContentId()+"~"+mDbillRequest.getContentId());
		                                    users.add(usersDbillRequestTemp);
		                            }
		                            else
		                            {
		                            	users.add(mDbillRequest);
		                             }
		                        }
		                           else
		                           {
		                    	     users.add(mDbillRequest);
		                    	   }

		                      }
		                   else
		                   {
*/		                    users.add(mDbillRequest);
		                 //  }
		                       // mDbillRequestTemp = mDbillRequest;
		    	        		++rowCount;
		    	        		if(limitSize > 0){
		    	        		  if(rowCount % limitSize == 0){
		    	        			  startRecords = startRecords + limitSize;
		    	        			  renewalFetchLimit = renewalFetchLimit + limitSize;
		    	        		  }
		    	        		}
		    	        		  _logger.debug("Records picked:"+rowCount);
		    		        }
		    	        	
		    	        	catch(Exception ex){
		    	    	        _logger.error("loadUsers() : error while loading ACCOUNT_NUMBER : "+rsLoadUsers.getString("msisdn")+"PLAN_ID:"+rsLoadUsers.getString("PLAN_ID"));
		    	    	        ex.printStackTrace();
		    	    	    }
		    	        
				    	}//end of while loop
				    	
				    	 if(users != null && users.size() > 0){
			   	  	        	long totalSize = users.size();
			   	  	        	//boolean mActivationStatus = false;
			   	  	        	//List<DbillRequest> resp = new ArrayList<DbillRequest>();
			   	  	        	long unProcessedList = 1;
			   	  	        	int returnValue = 0;
			   	  	            boolean inst = false;
			   	  	            boolean del = false;
			   	  	        	userItr = users.iterator();
			   	              	//for(int i=0; i < totalSize; i++){
			   	  	        	while(userItr.hasNext()){
			   	              		try{
			   	              			DbillRequest pDbillRequest = userItr.next();
			   	  		            	//Send request through xml over http
			   	  		       //     _logger.info("main() : subscriptionId="+pDbillRequest.getSubscriptionId());
			   	  		       if(!("T_SUB_SERVICE_INFO".equalsIgnoreCase(autoRenVo.getSrcTablename()))){

		   	  	              returnValue = this.processRepeatCDR(pDbillRequest);
		   	  	              _logger.debug("main(): processed="+(unProcessedList)+", Out of "+totalSize);
		   	  	              unProcessedList++;
		   	  	              int delay = 0;
		   	  	              try {
		   	  	            	  delay = autoRenVo.getDelayReqRecords();
		   	  	              } catch (Exception e) {
		   	  	            	  if(e instanceof NumberFormatException){
		   	  	            		  delay =6000;
		   	  	            	  }
		   	  	              }
		   	  	              if(delay > 0){
		   	  	            	  TimeUnit.MILLISECONDS.sleep(delay); 
		   	  	              }
		   	  	               if(returnValue != 1){
  	                			del = this.deleteRenewalSubs(pDbillRequest);
  	                			 _logger.info("main() : returnValue="+returnValue+"  Record gets  deleted from Temp table="+del);
		   	  	               }
			   	  		       }
			   	  		       else{

		   	              		    inst = this.insertUsers(pDbillRequest);
		   	  	                	
		   	  	                	if(inst)
		   	  	                	{
		   	  	                	returnValue = this.processRepeatCDR(pDbillRequest);
		   	  	              _logger.info("main() : returnValue="+returnValue+" after  inst flage="+inst);   
		   	  	               if(returnValue == 1){
   	                			del = this.deleteUsers(p_con,pDbillRequest);
   	                			 _logger.info("main() : returnValue="+returnValue+" after  delete flage="+del);
   	                			// resp.add(pDbillRequest);
		   	  	               }
		   	  	      	_logger.debug("main(): processed="+(unProcessedList)+", Out of "+totalSize);
                        unProcessedList++;
                        int delay = 0;
                        try {
                        	delay = autoRenVo.getDelayReqRecords();
                        } catch (Exception e) {
                        	if(e instanceof NumberFormatException){
                        		delay =6000;
                        	}
                        }
                        if(delay > 0){
                        	TimeUnit.MILLISECONDS.sleep(delay); 
                        }  
		   	  	           }
		   	  	      	else{
		   	  	      		_logger.debug("Record has already been processed in the previous request");		
		   	  	                	}
		   	  		       
			   	  		       }
			   	              		}
			   	      	        	catch(Exception ex){
			   	      	    	        _logger.error("main() : exception while making renewal billing process-Subscriber Number="+ex.getMessage());
			   	      	    	        ex.printStackTrace();
			   	      	    	    }
			   	      	        	//remove processed subscriber from the list to free the memory.
			   	      	        	//userItr.remove();
			   	      	        //	long unProcessedList = users.size();
			   	      	       
			   	              	}
			   	  	          users.clear();
			   	        	} 
			    	        if(rowCount < limitSize){
			      			  startRecords = startRecords + limitSize;
			      			  renewalFetchLimit = renewalFetchLimit + limitSize;
			      		  }
			    	        if(limitSize == 0)
			    	        {
			    	        	queryStrBuff.setLength(0);
			    	        //	break;
			    	        }
    	    }
    	        queryStrBuff.setLength(0);
    	      
    	        } catch (Exception e) {
    	        	
					// TODO: handle exception
    	        	 e.printStackTrace();
    	        	 queryStrBuff.setLength(0);
				}
    	        finally{
    	        	queryStrBuff.setLength(0);
    	        	users.clear();
    	        }
    	       
    	        try {
    	        	fis = new FileInputStream(fileName);
					props.load(fis);
					if(!SEUtil.isNullString(AutoRenCache.getInstance().getProperty("runningMode"))){
						if("ER".equals(props.getProperty("runningMode").trim()) || SEConstantsI.MODE_OF_APPLICATION.equals(props.getProperty("runningMode").trim())){
							break;
						}
					}
				} catch (Exception e) {
					_logger.info("RenewalProcessDynamicUsers : loadDynamicUsers() Error while closing property file at recordset level:"+e.getMessage());
				}
    	        finally{
    	        	if(fis != null){
    	        		fis.close();
    	        		fis = null;
    	        	}
    	        	try{
        	            if(p_con!=null) {
        	            	p_con.close();
        	            	p_con =null;
        	            }
        	        }catch(Exception ex){
        	            _logger.error("loadUsers() : error while closing Connection "+ex.getMessage());
        	        }
        	        try{
        	            if(rsLoadUsers!=null) {
        	            	rsLoadUsers.close();
        	                rsLoadUsers =null;
        	            }
        	        }catch(Exception ex){
        	            _logger.error("loadUsers() : error while closing resultset "+ex.getMessage());
        	        }
        	        try{
        	            if(psmtLoadUsers!=null) {
        	            	psmtLoadUsers.close();
        	                psmtLoadUsers = null;}
        	        }catch(Exception ex){
        	            _logger.error("loadUsers() : error while closing prepared statement "+ex.getMessage());
        	        }
        	        
    	        }
    	    	
    	        }
    	        
    	        
    	    	return users;	
    	    }
    	    catch(Exception ex){
    	    	ex.printStackTrace();
    	        _logger.error("loadUsers() : error while loading user list "+ex.getMessage());
    	        return null;
    	    }
    	    finally{
    	    	
    	    	try{
    	            if(p_con!=null) {
    	            	p_con.close();
    	            	p_con =null;
    	            }
    	        }catch(Exception ex){
    	            _logger.error("loadUsers() : error while closing Connection "+ex.getMessage());
    	        }
    	        try{
    	            if(rsLoadUsers!=null) {
    	            	rsLoadUsers.close();
    	                rsLoadUsers =null;
    	            }
    	        }catch(Exception ex){
    	            _logger.error("loadUsers() : error while closing resultset "+ex.getMessage());
    	        }
    	        try{
    	            if(psmtLoadUsers!=null) {
    	            	psmtLoadUsers.close();
    	                psmtLoadUsers = null;}
    	        }catch(Exception ex){
    	            _logger.error("loadUsers() : error while closing prepared statement "+ex.getMessage());
    	        }
    	        
    	        
    	        _logger.info("loadUsers() : exiting");
    	    }
    	    
			
			}
}
