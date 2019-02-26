/*
 * Created on Jan 17, 2005
 * Contains XML, String, Date/Time, and Miscellaneous output reformatting utilities
 * @quote What's the number for 911? --Homer Simpson
 */
package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import parkmedia.KMFLogger;

import com.kuvata.kmf.Constants;

/**
 * @author jrourke
 */
public class Reformat {
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Reformat.class );	
//********************************STRING REFORMATTERS******************************************

	/**
	 * Returns the original string or an empty string if null
	 * @param s The string to check and return
	 * @return replaces null with ""
	 */
	public static String getSafeString(String s){
		if (s == null){
			return "";
		} else {
			return s;
		}
	}
	/**
	 * Calls the ToString() method but returns an empty string if the object is null
	 * @param o The object to check for null
	 * @return s ToString() or null
	 */
	public static String getSafeToString(Object o){
		if (o == null){
			return "";
		} else {
			return o.toString();
		}
	}
	
    
//********************************XML REFORMATTERS******************************************

	/**
	 * Constructs an xml string with an element, value, and 1 optional attribute and value
	 * The option single attribute is provided by method overloading
	 * A tab indentation and newline character are also provided
	 * @param sTag		The xml element name
	 * @param sValue	The xml element value
	 * @param nTabs		The number of tabs to indent
	 * @param sAttName	The single attribute name
	 * @param sAttValue	The single attribute value
	 * @return 			XML string with the element followed by the value with the closing tag
	 * 					If no value is provided, automatically closes into one tag
	 */
	public static String getXmlLine(String sElement, String sValue, int nTabs, String sAttName, String sAttValue){
		sValue = getEscapeXml(sValue);
		sAttValue = getEscapeXml(sAttValue);
		
		String sPrePend = "";
		for(int x=0; x < nTabs; x++){
			sPrePend += "\t";
		}
		if (sValue == null || sValue.equals("")){
			return sPrePend + "<" + sElement + " " + sAttName + "=" + "\"" + sAttValue + "\"" + "/>\n";
		} else {
			return sPrePend + "<" + sElement + " " + sAttName + "=" + "\"" + sAttValue + "\"" + ">" + sValue + "</" + sElement + ">\n";
		}
	}
	
	/**
	 * Constructs an xml string with an element and value
	 * @see getXmlLine(String sTag, String sValue, int nTabs, String sAttName, String sAttValue)
	 * @param sTag		
	 * @param sValue
	 * @param nTabs
	 * @return 			XML string with the element followed by the value with the closing tag
	 * 					If no value is provided, automatically closes into one tag
	 */
	public static String getXmlLine(String sElement, String sValue, int nTabs){
		sValue = getEscapeXml(sValue);
		String sPrePend = "";
		for(int x=0; x < nTabs; x++){
			sPrePend += "\t";
		}
		if (sValue == null || sValue.equals("")){
			return sPrePend + "<" + sElement + " />\n";
		} else {
			return sPrePend + "<" + sElement + ">" + sValue + "</" + sElement + ">\n";
		}
	}
	/**
	 * @see getXmlLine(String sElement, String sValue, int nTabs)
	 * @param sTag
	 * @param sValue
	 * @return XML string with the element and value with no indent
	 */
	public static String getXmlLine(String sElement, String sValue){
		return getXmlLine(sElement, sValue,0);
	}
	
	/**
	 * Used for a single tag with no attribute or element closing tag
	 * Pass in '/xxxxx' to make a closing element tag
	 * Adds closing <> chars and a newline to a string passed in at a tab indentation
	 * @param sElement	The name of the element
	 * @return			Element tag with <> and tabs
	 */
	public static String getXmlTag(String sElement, int nTabs){
		String sPrePend = "";
		for(int x=0; x < nTabs; x++){
			sPrePend += "\t";
		}
		return sPrePend + "<" + sElement + ">\n";
	}
	/** 
	 * @see getXmlTag(String sElement, int nTabs)
	 * @param sTag
	 * @return getXmlTag with no indent
	 */
	public static String getXmlTag(String sElement){
		return getXmlTag(sElement, 0);
	}

	/**
	 * Escapes an element value per xml standards
	 * @param sValue	string to be formatted into a valid xml value
	 * @return			valid xml string value
	 */
	public static String getEscapeXml(String sValue){
		if (sValue == null || sValue.equalsIgnoreCase("")){
			return "";
		}
		sValue = sValue.replaceAll("&","&amp;");
		sValue = sValue.replaceAll("\"","&quot;");
		sValue = sValue.replaceAll("'","&apos;");
		sValue = sValue.replaceAll("<","&lt;");
		sValue = sValue.replaceAll(">","&gt;");

		return sValue;
	}
	

	/**
	 * Escapes an element value per xml standards
	 * @param sValue	string to be formatted into a valid xml value
	 * @return			valid xml string value
	 */
	public static String getUnescapeXml(String sValue){
		if (sValue == null || sValue.equalsIgnoreCase("")){
			return "";
		}
		sValue = sValue.replaceAll("&amp;","&");
		sValue = sValue.replaceAll("&quot;","\"");
		sValue = sValue.replaceAll("&apos;","'");
		sValue = sValue.replaceAll("&lt;","<");
		sValue = sValue.replaceAll("&gt;",">");
		return sValue;
	}	

//********************************DATE AND TIME REFORMATTERS******************************************

//	Common public date and time manipulation functions
  	
	/**
	* Converts a date string from one format to another
	* @param dateIn string of date to convert
	* @param formatIn format of incoming string
	* @param formatOut format of outgoing string
	* @return String of the date in formatOut format
	*/
	public static String DateConvert(
		String dateIn,    // string of date to convert
		String formatIn,  // format of incoming string
		String formatOut  // format of outgoing string 
	)
	{
		try
		{ 
			if( dateIn.equalsIgnoreCase("now") ) 
			{
				return (new SimpleDateFormat(formatOut).format(new Date()));
			} else {
				return (new SimpleDateFormat(formatOut)).format(
						(new SimpleDateFormat(formatIn)).parse(
								dateIn, new ParsePosition(0)));
			}
		} catch( Exception e ) {
			return "";
		}
	}
	
	/**
	* Converts time in seconds to a <code>String</code> in the format HH:mm:ss.
	* @param time the time in seconds.
	* @return a <code>String</code> representing the time in the format HH:mm:ss.
	*/
	public static String secondsToDateString(long time)
	{
	    int seconds = (int)(time % 60);
	    int minutes = (int)((time/60) % 60);
	    int hours = (int)(time/3600);
	    String secondsStr = (seconds<10 ? "0" : "")+seconds;
	    String minutesStr = (minutes<10 ? "0" : "")+minutes;
	    String hoursStr = (hours<10 ? "0" : "")+hours;
	    return new String(hoursStr+":"+minutesStr+":"+secondsStr);
	}
	
	/**
	* Converts time in milliseconds to a <code>String</code> in the format HH:mm:ss.SSS.
	* @param time the time in milliseconds.
	* @return a <code>String</code> representing the time in the format HH:mm:ss.SSS.
	*/
	public static String millisecondsToDateString(long time)
	{
	    int milliseconds = (int)(time % 1000);
	    int seconds = (int)((time/1000) % 60);
	    int minutes = (int)((time/60000) % 60);
	    int hours = (int)((time/3600000) % 24);
	    int days = (int)((time/3600000)/24);
	    hours += days*24;
	    String millisecondsStr = (milliseconds<10 ? "00" : (milliseconds<100 ? "0" : ""))+milliseconds;
	    String secondsStr = (seconds<10 ? "0" : "")+seconds;
	    String minutesStr = (minutes<10 ? "0" : "")+minutes;
	    String hoursStr = (hours<10 ? "0" : "")+hours;
	    return new String(hoursStr+":"+minutesStr+":"+secondsStr+"."+millisecondsStr);
	}
	
	/**
	 * Convert the given string (in seconds) to milliseconds
	 * @param seconds
	 * @return
	 */
	public static long secondsToMilliseconds(String seconds)
	{
		float msTime = 1000 * Float.valueOf( seconds ).floatValue();		
		return new Float(msTime).longValue();
	}

//********************************MISC REFORMATTERS******************************************

	/**
	 * Returns the hash algorithm used, but returns a safe value for null objects
	 * @param o			object to made into a hash--makes the container object of this object unique
	 * 					When this number changes, it can be assumed that a regeneration is necessary
	 * @param iResult	The hash code for the entire container object
	 * @param iPrime	The last unique prime number
	 * @return			New hash code for the container
	 */
	public static int getSafeHash(Object o, int iResult, int iPrime){
		if (o == null){
			return iResult;
		} else {
			// Multiple by an additional prime number to ensure uniqueness
			return 37 * iPrime * iResult + o.hashCode();
		}
	}
	
	/**
	 * Formats the given string to support values used in Oracle.
	 * 
	 * @param s		The string to format
	 * @return		The formatted string acceptable for use with Oracle db
	 */
	public static String oraesc(String s)
	{
		int x = 0;
		int y = 0;		
		if( s != null && s.length() > 0 ) 
		{
			String[] esc = { "'" };
			String[] escto = { "''" };
			for( int i=0; i<esc.length; i++ ) {
				while( (y = s.indexOf(esc[i],x)) != -1 ) {
					if( y == 0 ) {
						s = escto[i] + s.substring(1,s.length());
					} else if( y == s.length()-1 ) {
						s = s.substring(0,s.length()-1) + escto[i];
					} else {
						s = s.substring(0,y) + escto[i] + s.substring(y+1,s.length());
					}
					x = y+escto[i].length();
				}
			}
		}
		return s;
	}
	
	/**
	 * Formats the given string to be a valid Excel filename.
	 * (Escapes all the invalid filename characters for windows)
	 * Also escapes an apostrophe to avoid problems while using FTP etc.
	 * 
	 * @param s		The string to format
	 * @return		The formatted string acceptable for use with Windows/MsExcel filename
	 */
	public static String windowsEscape(String s){
		if( s != null && s.length() > 0 ){
			s = s.replace("\"", ".").replace(":", ".").replace("?", ".").replace("/", ".").replace("\\", ".")
				.replace("*", ".").replace("<", ".").replace(">", ".").replace("|", ".").replace("'", ".");
		}
		return s;
	}
	
	/**
	 * Escapes an element value per jsp standards
	 * @param sValue	string to be formatted into a valid value
	 * @return			valid string value
	 */
	public static String jspEscape(String sValue){
		if (sValue == null || sValue.equalsIgnoreCase("")){
			return "";
		};
		sValue = sValue.replaceAll("\\'","\\\\\'");		
		sValue = sValue.replaceAll("\\\"", "\\\\\'\\\\'");
		return sValue;
	}
	
	/**
	 * Escapes an element value per jsp standards
	 * @param sValue	string to be formatted into a valid value
	 * @return			valid string value
	 */
	public static String jspUnescape(String sValue){
		if (sValue == null || sValue.equalsIgnoreCase("")){
			return "";
		};
		sValue = sValue.replaceAll("\\\\\'\\\\'", "\\\"");
		sValue = sValue.replaceAll("\\\\\'", "\\'");				
		return sValue;
	}
	
	/**
	 * Escapes an element value per CSV standards
	 * @param sValue	string to be formatted into a valid value
	 * @return			valid string value
	 */
	public static String csvEscape(String sValue){
		if(sValue != null && sValue.contains(",")){
			// Surround by quotes
			sValue = "\"" + sValue + "\"";
		}
		return sValue;
	}
	
	/**
	 * Escapes special characters in the given params string.
	 * Necessary in the case where the parameter string contains either
	 * a comma or an ampersand, which would cause problems when parsing.
	 * 
	 * @param params
	 * @return
	 */
	public static String escape(String params)
	{
		if( params != null && params.length() > 0 ){
			params = params.replaceAll("\\&", "\\&amp;");
			params = params.replaceAll("\\,", "\\&comma;");
		}
		return params;
	}
	
	/**
	 * Escapes special characters in the given params string.
	 * Necessary in the case where the parameter string contains either
	 * a comma or an ampersand, which would cause problems when parsing.
	 * 
	 * @param params
	 * @return
	 */
	public static String unescape(String params)
	{
		params = params.replaceAll("\\&comma;", "\\,");
		params = params.replaceAll("\\&amp;", "\\&");				
		return params;
	}	
	
	/**
	 * This method converts all characters to their equivalent hex
	 * and appends a % at the beginning to make it a valid url
	 */
	public static String urlEscape(String param){
		String result = "";
		for ( int i = 0; i < param.length(); ++i ) {
			char c = param.charAt( i );
			int j = (int) c;
			result += "%" + Integer.toHexString(j);
		}
		return result;
	}
	
	/**
	 * Formats the give time in seconds to hh:mm:ss.tenths of seconds
	 * 
	 * @param fSeconds
	 * @return
	 */
	public static String formatTime(float fSeconds)
	{		
		BigDecimal time = new BigDecimal( fSeconds );
		
		// Subtract the int value of the seconds from the float value of the seconds and multiply by 10 to get tenths of a second		
		BigDecimal t = time.subtract( new BigDecimal(time.intValue()) );		
		int tenths = (t.multiply(new BigDecimal(10))).intValue();		
		int seconds = (int)(time.intValue() % 60);
		int minutes = (int)((time.intValue()/60) % 60);
		int hours = (int)((time.intValue()/3600) % 24);
		int days = (int)((time.intValue()/3600)/24);
		hours += days*24; 	   	   
		String secondsStr = (seconds<10 ? "0" : "")+seconds;
	    String minutesStr = (minutes<10 ? "0" : "")+minutes;
	    String hoursStr = (hours<10 ? "0" : "")+hours;
		
	    return new String(hoursStr+":"+minutesStr+":"+secondsStr+"."+tenths);
	}	

	private static Map htmlEntities;
	   private synchronized static Map getHtmlEntities()
	   {
	      if (htmlEntities==null)
	      {
	         htmlEntities=new Hashtable();
	         //Quotation mark
	         htmlEntities.put("quot","\"");
	         //Ampersand
	         htmlEntities.put("amp","\u0026");
	         //Less than
	         htmlEntities.put("lt","\u003C");
	         //Greater than
	         htmlEntities.put("gt","\u003E");
	         //Nonbreaking space
	         htmlEntities.put("nbsp","\u00A0");
	         //Inverted exclamation point
	         htmlEntities.put("iexcl","\u00A1");
	         //Cent sign
	         htmlEntities.put("cent","\u00A2");
	         //Pound sign
	         htmlEntities.put("pound","\u00A3");
	         //General currency sign
	         htmlEntities.put("curren","\u00A4");
	         //Yen sign
	         htmlEntities.put("yen","\u00A5");
	         //Broken vertical bar
	         htmlEntities.put("brvbar","\u00A6");
	         //Section sign
	         htmlEntities.put("sect","\u00A7");
	         //Umlaut
	         htmlEntities.put("uml","\u00A8");
	         //Copyright
	         htmlEntities.put("copy","\u00A9");
	         //Feminine ordinal
	         htmlEntities.put("ordf","\u00AA");
	         //Left angle quote
	         htmlEntities.put("laquo","\u00AB");
	         //Not sign
	         htmlEntities.put("not","\u00AC");
	         //Soft hyphen
	         htmlEntities.put("shy","\u00AD");
	         //Registered trademark
	         htmlEntities.put("reg","\u00AE");
	         //Macron accent
	         htmlEntities.put("macr","\u00AF");
	         //Degree sign
	         htmlEntities.put("deg","\u00B0");
	         //Plus or minus
	         htmlEntities.put("plusmn","\u00B1");
	         //Superscript 2
	         htmlEntities.put("sup2","\u00B2");
	         //Superscript 3
	         htmlEntities.put("sup3","\u00B3");
	         //Acute accent
	         htmlEntities.put("acute","\u00B4");
	         //Micro sign (Greek mu)
	         htmlEntities.put("micro","\u00B5");
	         //Paragraph sign
	         htmlEntities.put("para","\u00B6");
	         //Middle dot
	         htmlEntities.put("middot","\u00B7");
	         //Cedilla
	         htmlEntities.put("cedil","\u00B8");
	         //Superscript 1
	         htmlEntities.put("sup1","\u00B9");
	         //Masculine ordinal
	         htmlEntities.put("ordm","\u00BA");
	         //Right angle quote
	         htmlEntities.put("raquo","\u00BB");
	         //Fraction one-fourth
	         htmlEntities.put("frac14","\u00BC");
	         //Fraction one-half
	         htmlEntities.put("frac12","\u00BD");
	         //Fraction three-fourths
	         htmlEntities.put("frac34","\u00BE");
	         //Inverted question mark
	         htmlEntities.put("iquest","\u00BF");
	         //Capital A, grave accent
	         htmlEntities.put("Agrave","\u00C0");
	         //Capital A, acute accent
	         htmlEntities.put("Aacute","\u00C1");
	         //Capital A, circumflex accent
	         htmlEntities.put("Acirc","\u00C2");
	         //Capital A, tilde
	         htmlEntities.put("Atilde","\u00C3");
	         //Capital A, umlaut
	         htmlEntities.put("Auml","\u00C4");
	         //Capital A, ring
	         htmlEntities.put("Aring","\u00C5");
	         //Capital AE ligature
	         htmlEntities.put("AElig","\u00C6");
	         //Capital C, cedilla
	         htmlEntities.put("Ccedil","\u00C7");
	         //Capital E, grave accent
	         htmlEntities.put("Egrave","\u00C8");
	         //Capital E, acute accent
	         htmlEntities.put("Eacute","\u00C9");
	         //Capital E, circumflex accent
	         htmlEntities.put("Ecirc","\u00CA");
	         //Capital E, umlaut
	         htmlEntities.put("Euml","\u00CB");
	         //Capital I, grave accent
	         htmlEntities.put("Igrave","\u00CC");
	         //Capital I, acute accent
	         htmlEntities.put("Iacute","\u00CD");
	         //Capital I, circumflex accent
	         htmlEntities.put("Icirc","\u00CE");
	         //Capital I, umlaut
	         htmlEntities.put("Iuml","\u00CF");
	         //Capital eth, Icelandic
	         htmlEntities.put("ETH","\u00D0");
	         //Capital N, tilde
	         htmlEntities.put("Ntilde","\u00D1");
	         //Capital O, grave accent
	         htmlEntities.put("Ograve","\u00D2");
	         //Capital O, acute accent
	         htmlEntities.put("Oacute","\u00D3");
	         //Capital O, circumflex accent
	         htmlEntities.put("Ocirc","\u00D4");
	         //Capital O, tilde
	         htmlEntities.put("Otilde","\u00D5");
	         //Capital O, umlaut
	         htmlEntities.put("Ouml","\u00D6");
	         //Multiply sign
	         htmlEntities.put("times","\u00D7");
	         //Capital O, slash
	         htmlEntities.put("Oslash","\u00D8");
	         //Capital U, grave accent
	         htmlEntities.put("Ugrave","\u00D9");
	         //Capital U, acute accent
	         htmlEntities.put("Uacute","\u00DA");
	         //Capital U, circumflex accent
	         htmlEntities.put("Ucirc","\u00DB");
	         //Capital U, umlaut
	         htmlEntities.put("Uuml","\u00DC");
	         //Capital Y, acute accent
	         htmlEntities.put("Yacute","\u00DD");
	         //Capital thorn, Icelandic
	         htmlEntities.put("THORN","\u00DE");
	         //Small sz ligature, German
	         htmlEntities.put("szlig","\u00DF");
	         //Small a, grave accent
	         htmlEntities.put("agrave","\u00E0");
	         //Small a, acute accent
	         htmlEntities.put("aacute","\u00E1");
	         //Small a, circumflex accent
	         htmlEntities.put("acirc","\u00E2");
	         //Small a, tilde
	         htmlEntities.put("atilde","\u00E3");
	         //Small a, umlaut
	         htmlEntities.put("auml","\u00E4");
	         //Small a, ring
	         htmlEntities.put("aring","\u00E5");
	         //Small ae ligature
	         htmlEntities.put("aelig","\u00E6");
	         //Small c, cedilla
	         htmlEntities.put("ccedil","\u00E7");
	         //Small e, grave accent
	         htmlEntities.put("egrave","\u00E8");
	         //Small e, acute accent
	         htmlEntities.put("eacute","\u00E9");
	         //Small e, circumflex accent
	         htmlEntities.put("ecirc","\u00EA");
	         //Small e, umlaut
	         htmlEntities.put("euml","\u00EB");
	         //Small i, grave accent
	         htmlEntities.put("igrave","\u00EC");
	         //Small i, acute accent
	         htmlEntities.put("iacute","\u00ED");
	         //Small i, circumflex accent
	         htmlEntities.put("icirc","\u00EE");
	         //Small i, umlaut
	         htmlEntities.put("iuml","\u00EF");
	         //Small eth, Icelandic
	         htmlEntities.put("eth","\u00F0");
	         //Small n, tilde
	         htmlEntities.put("ntilde","\u00F1");
	         //Small o, grave accent
	         htmlEntities.put("ograve","\u00F2");
	         //Small o, acute accent
	         htmlEntities.put("oacute","\u00F3");
	         //Small o, circumflex accent
	         htmlEntities.put("ocirc","\u00F4");
	         //Small o, tilde
	         htmlEntities.put("otilde","\u00F5");
	         //Small o, umlaut
	         htmlEntities.put("ouml","\u00F6");
	         //Division sign
	         htmlEntities.put("divide","\u00F7");
	         //Small o, slash
	         htmlEntities.put("oslash","\u00F8");
	         //Small u, grave accent
	         htmlEntities.put("ugrave","\u00F9");
	         //Small u, acute accent
	         htmlEntities.put("uacute","\u00FA");
	         //Small u, circumflex accent
	         htmlEntities.put("ucirc","\u00FB");
	         //Small u, umlaut
	         htmlEntities.put("uuml","\u00FC");
	         //Small y, acute accent
	         htmlEntities.put("yacute","\u00FD");
	         //Small thorn, Icelandic
	         htmlEntities.put("thorn","\u00FE");
	         //Small y, umlaut
	         htmlEntities.put("yuml","\u00FF");
	      }
	      return htmlEntities;
	   }

	   /**
	    * Converts any HTML tags (i.e. &nbsp;) within the given string to ASCII characters
	    * @param str
	    * @return
	    */
	   public static String decodeHtml(String str)
	   {
	        StringBuffer ostr = new StringBuffer();
	        int i1=0;
	        int i2=0;

	        while(i2<str.length())
	        {
	           i1 = str.indexOf("&",i2);
	           if (i1 == -1 ) {
	                ostr.append(str.substring(i2, str.length()));
	                break ;
	           }
	           ostr.append(str.substring(i2, i1));
	           i2 = str.indexOf(";", i1);
	           if (i2 == -1 ) {
	                ostr.append(str.substring(i1, str.length()));
	                break ;
	           }

	           String tok = str.substring(i1+1, i2);
	           if (tok.charAt(0)=='#')
	           {
	              tok=tok.substring(1);
	              try {
	                   int radix = 10 ;
	                   if (tok.trim().charAt(0) == 'x') {
	                      radix = 16 ;
	                      tok = tok.substring(1,tok.length());
	                   }
	                   ostr.append((char) Integer.parseInt(tok, radix));
	              } catch (NumberFormatException exp) {
	                   ostr.append('?');
	              }
	           } else
	           {
	              tok=(String)getHtmlEntities().get(tok);
	              if (tok!=null)
	                 ostr.append(tok);
	              else
	                 ostr.append('?');
	           }
	           i2++ ;
	        }
	        return ostr.toString();
	   }
	   
	/**
	 * Parses the given params and returns a hashmap of name/value pairs
	 * @param params
	 * @return
	 */
	public static HashMap<String, String> parseParameters(String params) throws Exception
	{
		HashMap<String, String> result = new HashMap<String, String>();
		
		// Split on the & sign
		String[] parts = params.split("\\&");
		for( int i=0; i<parts.length; i++)
		{
			// Parse the name and the value out of each parameter
			String param = parts[i];
			int equalsIndex = param.indexOf('=');
			if( equalsIndex<=0 )
			{
				throw new Exception("couldn't find expected = character parsing parameters: "+params);
			}
			String name = param.substring( 0, param.indexOf("=") );
			String value;
			if( param.indexOf("=") == param.length()-1 )
			{
				value = "";
			}
			else
			{
				value = param.substring( param.indexOf("=") + 1 );
			}
			result.put( name, value );
		}
		return result;
	}	 

//***************************************** Parsers ********************************************
	public static List csvParser(String s){
		// A CSV parser to parse unselected items
		if(s!=null){
			ArrayList unselectedAssetIds = new ArrayList();
			for(int i=0; i<s.length();i++){
				String id = s.substring(i, s.indexOf(",", i));
				if(!id.equals("-1"))
					unselectedAssetIds.add( id ); 
				i = s.indexOf(",", i);
			}
			return unselectedAssetIds;
		}else
			return null;
	}
	
	/**
	 * Converts the given Clob to a String
	 * @param c
	 * @return
	 */
	public static String convertClobToString(Clob c, boolean appendEol)
	{
		StringBuffer result = new StringBuffer();
		if( c != null ){
			try {
				BufferedReader br = new BufferedReader( c.getCharacterStream() );
				String line;
				while ((line = br.readLine()) != null){
				    result.append( line );
				    if(appendEol){
				    	result.append("\n");
				    }
				}
				br = null;
			} catch (Exception e) {
				logger.error( e );
			}
		}
        return result.toString();
	}	
	
	/**
	 * Converts the given string (e.g 1GB, 1MB, etc) into bytes
	 * @return
	 */
	public static double convertStringToBytes(String s)
	{
		double result = 0;
		if( s != null && s.length() > 0 )
		{
			String stringToConvert = s.toUpperCase();

			// Convert terabytes to bytes
			if( stringToConvert.indexOf( "T" ) > 0 ){
				stringToConvert = stringToConvert.substring( 0, stringToConvert.indexOf("T") );
				result = Double.valueOf( stringToConvert ).doubleValue() * Constants.GIGABYTE * 1024;
			}
			// Convert the gigabytes to bytes
			else if( stringToConvert.indexOf( "G" ) > 0 ){
				stringToConvert = stringToConvert.substring( 0, stringToConvert.indexOf("G") );
				result = Double.valueOf( stringToConvert ).doubleValue() * Constants.GIGABYTE;
			}
			// Convert the megabytes to bytes
			else if( stringToConvert.indexOf( "M" ) > 0 ){
				stringToConvert = stringToConvert.substring( 0, stringToConvert.indexOf("M") );
				result = Double.valueOf( stringToConvert ).doubleValue() * Constants.MEGABYTE;
			}
			// Convert the kilobytes to bytes
			else if( stringToConvert.indexOf( "K" ) > 0 ){
				stringToConvert = stringToConvert.substring( 0, stringToConvert.indexOf("K") );
				result = Double.valueOf( stringToConvert ).doubleValue() * Constants.KILOBYTE;
			}	
		}
		return result;
	}
	
	// Returns a comma delimited string of selected ids or names
	public static String parseSelectedValue(String selectedValue, String allParameter, boolean returnNames){
		if(selectedValue.equals(allParameter) == false){
			String[] idNameStrings = selectedValue.split(Constants.UNIQUE_DELIMITER);
			String selectedIds = "";
			for(int i=0;i<idNameStrings.length;i++){
				int index = returnNames ? 1 : 0;
				// Parse out the ids or the names
				if(idNameStrings.length >= index + 1){
					selectedIds += idNameStrings[i].split("\\" + Constants.DELIMITER)[index] + ", ";
				}
			}
			if(selectedIds.length() > 2){
				return selectedIds.substring(0, selectedIds.length() - 2);
			}else{
				return "";
			}
		}else{
			return selectedValue;
		}
	}
	
	public static String convertListToCommaDelimitedString(List l){
		String s = null;
		if(l != null && l.size() > 0){
			s = l.toString();
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}
	
	public static int splitExcelCells(HSSFSheet sheet, HSSFCellStyle style, String title, String value, int rowCounter){
		HSSFRow row1 = sheet.createRow( rowCounter++ );
		HSSFCell row1Cell0 = row1.createCell( (short)0 );
		row1Cell0.setCellValue( title );
		row1Cell0.setCellStyle( style );
		if(value.length() > 255){
			int splitIndex = 0;
			while(value.length() - splitIndex > 255){
				String subValue = value.substring(splitIndex, splitIndex + 255);
				row1.createCell( (short)1 ).setCellValue( subValue );
				
				// Create a new row for the next subvalue
				row1 = sheet.createRow( rowCounter++ );
				splitIndex += 255;
			}
			
			String subValue = value.substring(splitIndex);
			row1.createCell( (short)1 ).setCellValue( subValue );
			
		}else{
			row1.createCell( (short)1 ).setCellValue( value );
		}
		return rowCounter;
	}
}