package com;

/* Application.java
 * Name             	Date            History
 *------------------------------------------------------------------------
 * Soms             	29-Jan-2004    	Initial Creation
 *------------------------------------------------------------------------
 * Copyright (c) 2004 Bharti Telesoft International Pvt. Ltd.
 */

import java.io.*;
import java.util.*;

public class Application
{
    public static Properties properties = new Properties();

    /**
    * This method returns the value of the property from the properties.
    * @return String, property value
    * @param String propertyName, name of the property which value is to be fetched.
    */
    public static String getProperty(String propertyName)
    {
    	return properties.getProperty(propertyName);
    }//end getProperty

    /**
    * This method is used to load the Application.props properties file.
    * into properties.
    * @return none;
    * @param String fileName, file to be loaded.
    */
    public static void load(String fileName) throws IOException
    {
    	File file = new File(fileName);
    	properties.load(new FileInputStream(file));
    }//load
}//end of class Constants
