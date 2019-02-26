package com.kuvata.kmf.alerts;


import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import parkmedia.KMFLogger;


/**
 * Creates a text/plain message and sends it.
 * 
 * @author Jeff Randesi
 */
public class Emailer {
    
	protected static final KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Emailer.class );	
	
	/**
	 * Builds the column headers and row data of the email based on the given parameters
	 * @param colHeaders
	 * @param data
	 * @return
	 */
	public static String buildEmailSection(ArrayList<String> colHeaders, ArrayList<ArrayList<String>> data)
	{				
		StringBuffer msg = new StringBuffer();
		if( data.size() == 0 ){
			msg.append(" (none)\n");			
		}else{
			
			// Figure out the max width of each column
			ArrayList<Integer> colMaxWidths = new ArrayList<Integer>();
			for( int i=0; i<colHeaders.size(); i++ ){
				colMaxWidths.add( colHeaders.get(i).length() );			
			}	
			if( colMaxWidths.size() > 0 ){
				for( int i=0; i<data.size(); i++ ){
					ArrayList<String> currentRowData = data.get(i);
					for( int j=0; j<currentRowData.size(); j++ ){
						colMaxWidths.set( j, currentRowData.get(j).length() > colMaxWidths.get(j) ? currentRowData.get(j).length() : colMaxWidths.get(j) );
					}			
				}	
			}
			
			/*
			 * Build the column headers
			 */			
			for( int i=0; i<colHeaders.size(); i++ ){
				msg.append(" ");
				msg.append( colHeaders.get(i) );
				for( int j=0; j<colMaxWidths.get(i) - colHeaders.get(i).length(); j++ ){
					msg.append(" ");
				}
				msg.append("  ");
			}
			if( colHeaders.size() > 0 ){
				msg.append("\n");
			}
			
			for( int i=0; i<colHeaders.size(); i++ ){
				msg.append(" ");
				for( int j=0; j<colMaxWidths.get(i); j++ )
				{
					// Only add the underline dashes if a column header name was provided
					if( colHeaders.get(i).length() > 0 ){
						msg.append("-");
					}
				}
				msg.append("  ");
			}
			if( colHeaders.size() > 0 ){
				msg.append("\n");
			}		
			
			/*
			 * Build the data section
			 */
			for( int i=0; i<data.size(); i++ ){
				ArrayList<String> currentRowData = data.get(i);			
				for( int j=0; j<currentRowData.size(); j++)
				{
					msg.append(" ");
					msg.append( currentRowData.get(j) );
					if( colMaxWidths.size() > 0 ){
						for( int k=0; k<colMaxWidths.get(j) - currentRowData.get(j).length(); k++ ){
							msg.append(" ");
						}
					}
					msg.append("  ");					
				}	
				msg.append("\n");	
			}
		}
		msg.append("\n");	
		return msg.toString();
	}

	/**
	 * By default the from name is Sign Center, but this can be overridden with a direct call passing in the sender email
	 * 
	 * @param subject
	 * @param msgBody
	 * @param recipients
	 * @param senderEmail
	 * @param mailServer
	 * @param isHtml
	 */
	public static void sendMessage(String subject, String msgBody, String recipients, String senderEmail, String mailServer, boolean isHtml) 
	{
		sendMessage(subject, msgBody, recipients, senderEmail, "Sign Center", mailServer, isHtml);
	}
	
	/**
	 * Creates a text/plain message and sends it.
	 * 
	 * @param subject
	 * @param text
	 * @param to
	 * @param from
	 * @param host
	 */
    public static void sendMessage(String subject, String msgBody, String recipients, String senderEmail, String senderName, String mailServer, boolean isHtml) 
	{		
		// Get the default mail session
		Properties props = new Properties();
		props.put("mail.smtp.host", mailServer);			
		Session session = Session.getInstance(props, null);
		
		try 
		{
		    // Create a message
		    Message msg = new MimeMessage(session);
		    InternetAddress from = new InternetAddress(senderEmail);
		    try { from = new InternetAddress(senderEmail, senderName); } catch(Exception e) {}
		    msg.setFrom( from );
			
			// If the recipients is a comma delimited string
			InternetAddress[] address = null;
			if( recipients.indexOf(",") > 0 )
			{
				// Build the array of internet addresses
				String[] addresses = recipients.split("\\,");
				address = new InternetAddress[ addresses.length ];
				for( int i=0; i<addresses.length; i++ ) {
					address[i] = new InternetAddress( addresses[i].trim() );
				}					
			} else {
				address = new InternetAddress[1];
				address[0] = new InternetAddress( recipients.trim() );
			}			
		    msg.setRecipients( Message.RecipientType.TO, address );
		    msg.setSubject( subject );
		    msg.setSentDate( new Date() );
		    if(isHtml){
		    	msg.setContent( msgBody, "text/html" );
		    }else{
		    	msg.setText( msgBody );
		    }
		    Transport.send(msg);
		} 
		catch (MessagingException mex) 
		{
		    logger.error("MessagingException occurred in Emailer.java", mex);
		    Exception ex = mex;
		    do {
				if (ex instanceof SendFailedException) 
				{
				    SendFailedException sfex = (SendFailedException)ex;
				    Address[] invalid = sfex.getInvalidAddresses();
				    if (invalid != null) 
					{
						logger.error("    ** Invalid Addresses");
						for(int i=0; i < invalid.length; i++) {
							logger.error("         " + invalid[i]);
						}						
				    }
				    Address[] validUnsent = sfex.getValidUnsentAddresses();
				    if (validUnsent != null) 
					{
						logger.error("    ** ValidUnsent Addresses");					
					    for (int i = 0; i < validUnsent.length; i++) {
							logger.error("         "+validUnsent[i]);
					    }
					}
				
				    Address[] validSent = sfex.getValidSentAddresses();
				    if (validSent != null) 
					{
						logger.error("    ** ValidSent Addresses");						
					    for (int i = 0; i < validSent.length; i++) {
							logger.error("         "+validSent[i]);
						}
				    }
				}

				if (ex instanceof MessagingException)
				    ex = ((MessagingException)ex).getNextException();
				else
				    ex = null;
		    } 
			while (ex != null);
		}
    }
	
	public static void main(String[] args)
	{
		if( args.length == 5 ) {
			Emailer.sendMessage(args[0], args[1], args[2], args[3], args[4], false);
		} else {
			System.out.println("Usage. Emailer.sendMessage subject text to from host");
		}
	}
}
