package com.readGmail;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.ResourceBundle;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.search.SearchTerm;

import org.apache.sling.commons.json.JSONObject;


public class ReadEmailFromGmail {
	
	static ResourceBundle bundle = ResourceBundle.getBundle("config");
	private static Folder folder;
	
	
	public static void main(String[] args) {

//        new ReadEmailFromGmail();
	}
	
	
	public ReadEmailFromGmail(PrintWriter out) {
		Gmail_Pojo gm = null;
		try {
			gm = new Gmail_Pojo();
			gm.setProtocol(bundle.getString("protocol"));
			gm.setHost(bundle.getString("host"));
			gm.setPort(bundle.getString("port"));
			gm.setUserName(bundle.getString("userName"));
			gm.setPassWord(bundle.getString("password"));
			
			processEmail(gm, out);
			
		} catch (Exception e) {
			e.printStackTrace();
			out.println(e.getMessage());
		}
	}
	
	public static void processEmail(Gmail_Pojo gmail_pj, PrintWriter out) {
		  
		try {
			
			GmailMethods.getServerConnect(gmail_pj);
			folder=GmailMethods.openFolder(bundle.getString("GmailFolderName"));
			SearchTerm term=GmailMethods.searchMailterm(out);
            
			if(term!=null){
         // fetches new messages from server
//            Message[] arrayMessages = folderInbox.getMessages();  // without search we can use this 
            Message[] arrayMessages = folder.search(term);     // search messages
            out.println("length :: " + arrayMessages.length);
            
//          for (int i = arrayMessages.length - 1; i >= 0; i--) {  // this used for print last message only
            for (int i = 0; i < arrayMessages.length; i++) {       // this will print all message
            	Message message = arrayMessages[i];
            	
            	String seen=GmailMethods.seen_UnseenEmails(out, message);
            	
            	Address[] fromAddress = message.getFrom();
            	String from="";
            	if(fromAddress.length>0){
            	 from = fromAddress[0].toString();
            	}
            	
            	String subject = message.getSubject();
            	
            	String toList=GmailMethods.parseAddresses(message.getRecipients(RecipientType.TO));
            	String ccList=GmailMethods.parseAddresses(message.getRecipients(RecipientType.CC));
            	
            	String sentDate = message.getSentDate().toString();
            	sentDate=(GmailMethods.isNullString(sentDate)) ? "" : sentDate;
            	String mailSentDate="";
            	if(!GmailMethods.isNullString(sentDate)){
            		Date date=GmailMethods.parseDate(sentDate);
                	mailSentDate=GmailMethods.formatDate(date);
            	}
            	
            	String recDate = message.getReceivedDate().toString();
            	recDate=(GmailMethods.isNullString(recDate)) ? "" : recDate;
            	String mailReceivedDate="";
            	if(!GmailMethods.isNullString(recDate)){
            	   Date date1=GmailMethods.parseDate(recDate);
            	   mailReceivedDate=GmailMethods.formatDate(date1);
            	}
                
            	String contentType = message.getContentType();
            	String bodyText="";
            	       bodyText=GmailMethods.getTextFromMessage(message);
            	       bodyText=bodyText.toString().replaceAll("\\<.*?>","").replaceAll("[image:\\s]*.*]","");
            	       bodyText=(GmailMethods.isNullString(bodyText)) ? "" : bodyText;
            	       
            	String messageContent="";
            	String Signature="";
            	JSONObject body_plus_messegecontent=null;
            	
            	if(!GmailMethods.isNullString(contentType)){
            	      body_plus_messegecontent=GmailMethods.GmailBodyText(contentType, message, out);
            	   if(body_plus_messegecontent.has("messageContent")){
            	      messageContent=body_plus_messegecontent.getString("messageContent");
            	      if(!GmailMethods.isNullString(messageContent) && (!GmailMethods.isNullString(bodyText))){
            	    	  Signature=GmailMethods.signature(bodyText, messageContent, out);
            	      }
            	      
            	   }
            	}
            	
            	if(!GmailMethods.isNullString(subject)){
            		String subject_replace = subject.replace("RE: CXL     RE: ", "").replace("CXL     RE: ", "")
    		    			.replace("REF: ", "").replace("RE: ", "").replace("FW: ", "").replace("#", "_").replace(":", "_").replace(",", "_").replace("ç­”å¤�: ", "")
    		    			.replace("[", "").replace("]", "").replaceAll("[\\//|:]+","").trim().replaceAll("\\s{1,}", "_");
    		    	       
                	       subject_replace=subject.replaceAll("[^\\p{ASCII}]", "").replaceAll("[^a-zA-Z0-9.]", "_").replaceAll("\\_+", "_").replaceFirst("\\_+", "").replaceAll("答复", "");
    			 
                	       /*String file_path="D:\\Scorpio\\Email\\"+subject_replace;
              			   String file_root_path="D:\\Scorpio\\Email";*/
                	       String file_path=bundle.getString("file_path")+subject_replace;
              			   String file_root_path=bundle.getString("file_path");
                	       
              			   File linuxdirectory=new File(bundle.getString("file_path"));
              			   if(!linuxdirectory.exists()){
              				   
              				 linuxdirectory.mkdir();
              				 out.println("folder created in linux");
              			   }
              			   
              			 File file = new File(file_path);
              			 gmail_pj.setFile_root_path(file_root_path);
              			 gmail_pj.setSubject_replace(subject_replace);
              			 gmail_pj.setBody(bodyText);
              			 gmail_pj.setTo(toList);
              			 gmail_pj.setFrom(from);
              			 gmail_pj.setCc(ccList);
              			 gmail_pj.setDatetime(sentDate);
              			 gmail_pj.setSubject(subject);
              			 gmail_pj.setMailSentDate(mailSentDate);
              			 gmail_pj.setMailReceivedDate(mailReceivedDate);
              			 gmail_pj.setSignature(Signature);
              			   
                        if (!file.exists()) {
   	   				 
    	   				if (file.mkdir()) {
    	   					out.println("Directory is created!");
    	   				    GmailMethods.create_text_Xml_attachment(contentType, message, bundle, gmail_pj, out);
    	   				}else{
    	   					out.println("Directory is exist!");
    	   					GmailMethods.create_text_Xml_attachment(contentType, message, bundle, gmail_pj, out);
    	   				}
    	   				 
    	   			 }else{
    	   				   out.println("Directory is exist!");
    	   				   GmailMethods.create_text_Xml_attachment(contentType, message, bundle, gmail_pj, out);
    	   			 }
              			    
                	       
            	}
            	
            	
                 GmailMethods.printAllMailsDetails(gmail_pj, out);
            	
            	/*if(i==2){
            		break;
            	}*/
            	
            }  // for close
            
            GmailMethods.closeFolder();
            GmailMethods.disconnect();
			}// term check null close
			
		} catch (Exception e) {
			e.printStackTrace();
			out.print(e.getMessage());
		}
		
	}
	
	

}
