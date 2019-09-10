package com.btsl.properties;


/*
@Author:MohitRaj 2017
*/


public class IntegerParser implements NumberValidator{
	static org.apache.log4j.Category loger = org.apache.log4j.Category.getInstance(IntegerParser.class.getName());
	
	@Override
	public boolean numValidation(String tmp,String value,String key) {
		int num=0;
		double dnum =0.0;
		float fnum = 0.0f;
		long lnum = 0l;
		boolean flag=false;
		
		 if(("subscriptionType").equals(tmp)){
        	 if (value.matches("^[012]") && !("").equals(value)){
        		num=Integer.parseInt(value);
        		loger.debug("The Key fetched from the property file is:"+tmp);
        		loger.debug("The value retrieved from the property file is:"+num);
        		flag = true;
			 }
        	 else if(("").equals(value)){
        		num = -999;
        		loger.debug("The value for the key "+tmp+" retrieved from the property file is blank and so a default identifier is set:"+num);
         		flag = true;
        	 }
        	 else{
        		 flag =false;
	   		 } 
         }//"^[0-9]*$"
		 else if(("preInterval").equals(tmp) || ("recordLimit").equals(tmp) || ("batchSize").equals(tmp) ||
    		 ("sleepTime").equals(tmp) || ("maxSleepCounter").equals(tmp) ||("delayRecords").equals(tmp)
    		  || ("retryInterval").equals(tmp) || ("retryDays").equals(tmp) || ("maxRetryCount").equals(tmp) 
    		  || ("bearerId").equals(tmp)){
    	 if (value.matches("^[0-9]\\d*(\\.\\d+)?$") && !("").equals(value)){
    		 loger.debug("The Key fetched from the property file is:"+tmp);
    		 if(key.equalsIgnoreCase("Integer")){
    			if(value.matches("^[0-9]*$")){
    			 num=Integer.parseInt(value);
         		loger.debug("The value retrieved from the property file is:"+num);
         		flag = true; 
    			}
    			else{
    			flag = false;
    			}
    		 }
              if(key.equalsIgnoreCase("Float")){
            	  fnum=Float.parseFloat(value);
          		loger.debug("The value retrieved from the property file is:"+fnum);
          		flag = true;
    		 }
              if(key.equalsIgnoreCase("Long")){
            	  lnum=Long.parseLong(value);
          		loger.debug("The value retrieved from the property file is:"+lnum);
          		flag = true;
     		 }
              if(key.equalsIgnoreCase("Double")){
            	  dnum=Double.parseDouble(value);
          		loger.debug("The value retrieved from the property file is:"+dnum);
          		flag = true;
     		 }
         	
			 }
        	 else if(("").equals(value)){
        		 num = -999;
        		 loger.debug("The value for the key "+tmp+" retrieved from the property file is blank and so a default identifier is set:"+num);
         		 flag = true;
        	 }
        	 else{
        		 flag =false;
	   		 } 
    	   
     }
		return flag;
	}

}
