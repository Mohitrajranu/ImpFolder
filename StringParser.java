package com.btsl.properties;
/*
@Author:MohitRaj 2017
*/



public class StringParser implements StringValidator  {
	static org.apache.log4j.Category loger = org.apache.log4j.Category.getInstance(StringParser.class.getName());
	
	@Override
	public boolean strValidation(String tmp,String value) {
		boolean flag =false;
		PropertyValidation pVal = new PropertyValidation();
		pVal.setListBehavior(new ListParser());
		if(("runningMode").equals(tmp)){
       	 
       	 //Running Mode Check condition
	   		 if(("").equals(value) || value.matches("^[OCE]") || value.matches("^[E][R]{1}")){
	   			value = (("").equals(value)) ? "O" : value.trim();
	   			loger.debug("The Key fetched from the property file is:"+tmp);
	   			loger.debug("The value retrieved from the property file is:"+value);
	    		flag=true;
	    		} 
	   		 else{
	   			flag =false;
	   		 }
        }
        else if(("serviceGroup").equals(tmp)){
       	 if((("").equals(value) || ("VAS").equals(value) || ("DIG").equals(value))){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
    		}
       	 else{
       		flag =false;
	   		 } 
        }
        
        else if(("renewalPattern").equals(tmp)){
       	 if (("").equals(value) || value.matches("^[TD]")){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
    		}
       	 else{
       		flag =false;
	   		 } 
        }
        
        else if(("subStatus").equals(tmp)){
       	 if (("").equals(value) || value.matches("^[AGSCDEI]")){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
       	 }
       	 else{
       		flag =false;
	   		 } 
        }
        
        else if(("serviceType").equals(tmp)){
       	 if (("").equals(value) || value.matches("'([^']*)'")){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
       	 }
       	 else{
       		flag =false;
	   		 } 
        }
        
        else if(("serviceId").equals(tmp) || ("fileName").equals(tmp) || ("tableName").equals(tmp) || ("filePath").equals(tmp)){
       	 if (("").equals(value) || value.matches("^[a-zA-Z/][a-zA-Z_/]+")){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
       	 }
       	 else{
       		flag =false;
	   		 } 
        }
        else if(("servicePriority").equals(tmp) || ("fileCreationFlag").equals(tmp)
       		){
       	 if (("").equals(value) || value.matches("^[NY]")){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
			 }
       	 else{
       		flag =false;
	   		 } 
        }
        else if(("regionCode").equals(tmp) || ("colName").equals(tmp)){
       	 if (("").equals(value) || value.matches("^[a-zA-Z][a-zA-Z,]+")){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
			 }
       	 else{
       		flag =false;
	   		 } 
        }
        else if(("recordDelim").equals(tmp)){
       	 if (("").equals(value) || value.matches("^[|,$#]")){
       		loger.debug("The Key fetched from the property file is:"+tmp);
       		loger.debug("The value retrieved from the property file is:"+value);
       		flag=true;
			 }
       	 else{
       		flag =false;
	   		 } 
       	 
        }
	return 	flag;
	}

}
