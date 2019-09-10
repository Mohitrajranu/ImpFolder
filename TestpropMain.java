package org.mohit.mbas;

import org.mohit.properties.PropertiesCache;
import org.mohit.properties.PropertiesCache_VAS;

public class TestpropMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//Get individual properties
		String serviceId=PropertiesCache.getInstance().getProperty("serviceId");
		  System.out.println(serviceId);
		  
		  
		  System.out.println(PropertiesCache.getInstance().getProperty("ppRetryDays"));
		   
		  //All property names
		  System.out.println(PropertiesCache.getInstance().getAllPropertyNames());
		  System.out.println(PropertiesCache_VAS.getInstance().getAllPropertyNames());

		}

}
