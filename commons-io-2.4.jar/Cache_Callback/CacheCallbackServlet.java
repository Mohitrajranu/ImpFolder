package com.comviva.dbill.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.comviva.dbill.util.common.SEUtil;
import com.comviva.dbill.util.logging.CacheLog;

public class CacheCallbackServlet extends HttpServlet {
	
	static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CacheCallbackServlet.class.getName());
	
	
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    
    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    
	
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @SuppressWarnings("unchecked")
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("processRequest() : Enter.");
        
        BufferedReader requestReader = null;
        

        try {
        	
        	StringBuffer requestBuffer = new StringBuffer();
            try {
                 requestReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
                String temp = null;
                while ((temp = requestReader.readLine()) != null) {
                    requestBuffer.append(temp);
                }
            } 
            catch (Exception e) {
                logger.error("processRequest() : Exception while reading the request");
                e.printStackTrace();
                
                return;
            }
            
            logger.info("processRequest() : requestXml="+requestBuffer.toString());
            
            String requestXml = requestBuffer.toString();
            
            parseResponse(response, requestXml);
        	
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception Occured processRequest() : message :- "+e.getMessage());
        }
        finally{
        	logger.info("processRequest() : finally, exit");
        }
    }


    public void parseResponse (HttpServletResponse response, String requestXml) {
        logger.info("parseResponse() : Enter.");
        String finalResponse = null;
        
        ServletOutputStream out = null;
        byte[] data;
        CacheRequest mCacheRequest = null;
        
        try {
        	
        	if(!SEUtil.isNullString(requestXml)){
        		CacheParser mCacheParser = new CacheParser();
        		mCacheRequest = mCacheParser.parseCacheResponse(requestXml);
        		if(mCacheRequest != null){
        			finalResponse = "Valid Callback Request";
        		}
        		else{
        			finalResponse = "Invalid Callback Request, so unable to parse.";
        		}
        	}
        	else{
        		finalResponse = "Blank Callback Request";
        	}
        	
        	logger.debug("parseResponse() : finalResponse ="+finalResponse);
        	
        	if(mCacheRequest == null){
        		mCacheRequest = new CacheRequest();
        	}
        	
            response.setContentLength(finalResponse.length());
            response.setContentType("text/xml");
            data = finalResponse.getBytes();
            out = response.getOutputStream();
            out.write(data);
        }
        catch (Exception e) {
            e.printStackTrace();
            finalResponse = "General Exception";
            logger.error("Exception Occured parseResponse() : message :- "+e.getMessage());
        }
        finally {
        	mCacheRequest.setResponse(finalResponse);
        	try {
				CacheLog.println(mCacheRequest, requestXml);
			} 
        	catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	logger.debug("parseResponse() : finally exit.");
            
        }
    }

}
