/**
 * Methods that perform work at a file level
 * This includes a xml-DTD check and a File Copy method
 * @author jrourke
 */

package com.kuvata.kmf.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import parkmedia.KMFLogger;
import parkmedia.device.configurator.DeviceProperty;

import com.kuvata.ErrorLogger;
import com.kuvata.StreamGobbler;

import electric.xml.Document;

public class Files {
	
//************************************CLASS VARIABLES******************************************
	
    private boolean errorFound;						//Were any errors found?  Default is false.
    private static final int BUFFER_SIZE = 4096;		//Buffer per read size for copyFile
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( Files.class );
    
//************************************DTD VALIDATION******************************************
    /**
     * @see isValidDTD(String sFile)
     * @param args first argument (args[0]): the xml file to validate with its included DTD
     * Currently, there is no need to add command-line functionality for the other methods
     */
    public static void main(String[] args) {

    	isValidDTD(args[0]);
    	
    }//static void main
   
    /**
     * From a single file containing the DTD in the header, 
     * 		writes errors where the xml data does not conform to the DTD
     * @param sFile the xml file to validate with its included DTD
     */
    public static boolean isValidDTD(String sFile) {
    	Files files = new Files();
    	try {
    		File x = new File(sFile);
    		SAXParserFactory f = SAXParserFactory.newInstance();
    		f.setValidating(true); // Default is false
    		SAXParser p = f.newSAXParser();
    		DefaultHandler h = files.getDefaultHandlerInstance();
    		//Parse and print out any errors from the MyErrorHandler handler
    		p.parse(x,h);

    	} catch (ParserConfigurationException e) {
    		System.out.println(e.toString());
    		return false;
    	} catch (SAXException e) {
    		System.out.println(e.toString());
    		return false;
    	} catch (IOException e) {
    		System.out.println(e.toString());
    		return false;
    	}
    	return !files.getErrorFound();
    }//static void main
    
    /**
     * Overrides of the SAXParser handler
     * Writes output using System.out
     * SAXValidator.java
     * Copyright (c) 2002 by Dr. Herong Yang
     * @modified jrourke
     */
    private class MyErrorHandler extends DefaultHandler {
    	public void warning(SAXParseException e) throws SAXException {
    		System.out.println("Warning: "); 
    		printInfo(e);
    		setErrorFound(true);
    	}
    	public void error(SAXParseException e) throws SAXException {
    		System.out.println("Error: "); 
    		printInfo(e);
    		setErrorFound(true);
    	}
    	public void fatalError(SAXParseException e) throws SAXException {
    		System.out.println("Fatal error: "); 
    		printInfo(e);
    		setErrorFound(true);
    	}
    	private void printInfo(SAXParseException e) {
    		System.out.println("   Public ID: "+e.getPublicId());
    		System.out.println("   System ID: "+e.getSystemId());
    		System.out.println("   Line number: "+e.getLineNumber());
    		System.out.println("   Column number: "+e.getColumnNumber());
    		System.out.println("   Message: "+e.getMessage());
    	}
    	final public void endDocument() throws org.xml.sax.SAXException {
    		//Display some helpful console information
    		String sMessage;
    		if (!getErrorFound()){
    			sMessage = "No errors found with DTD";
    		} else {
    			sMessage = "Errors found with DTD";
    		}
    		System.out.println("--------------------\nFinished Validation (" + sMessage + ")");
    	}
    }//class MyErrorHandler
   
//************************************OTHER FILE UTILITIES******************************************
   /**
	 * Copies an input file to an output file.
	 * @param sInputFile	The input file name
	 * @param sOutputFile	The output file name
	 * @param bOverwrite	Do we overwrite existing files?
	 */
	public static boolean copyFile(String sInputFile, String sOutputFile, boolean bOverwrite){
		
		//check values		
		if (sInputFile == null || sInputFile.equals("")) {
			logger.info("Input File Not Found:" + sInputFile);
			return false;
		}
	
		if (sOutputFile == null || sOutputFile.equals("")){
			logger.info("Output File Not Found:" + sOutputFile);
			return false;
		}	
		
		//Create files to check existence
		File fInputFile = new File(sInputFile);
		File fOutputFile = new File(sOutputFile);
			
		if (!fInputFile.exists()){
			logger.info("Input file does not exist:" + sInputFile);
			return false;
		}
		//Do not overwrite output file
		if (!fOutputFile.exists() || bOverwrite){
		
			FileInputStream fis = null;
			FileOutputStream fos = null;

			try {
				//initialize variables
			  	fis = new FileInputStream (fInputFile);
				fos = new FileOutputStream (fOutputFile);
				int bytes_read;
				byte[] buffer = new byte[BUFFER_SIZE];
					
				//perform copy
                while(true) { 
                	bytes_read = fis.read(buffer); 
                    if (bytes_read == -1) break; 
                    fos.write(buffer, 0, bytes_read); 
                }
                //catch errors   
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			//close files
			} finally {
				//close input file
				if (fis != null){
					try {
				       	fis.close ();
					} catch (IOException e) {
				       	e.printStackTrace();
					}
				}
				//close output file
				if (fos != null){
					try {
						fos.close ();
					} catch (IOException e) {
						e.printStackTrace();
				    }
				}
					
			}//finally
		}//if outputfile does not exist
		return true;
	}//static void copyFile
	/**
	 * Default copy with no overwrite
	 * @see copyFile(String sInputFile, String sOutputFile, boolean bOverWrite)
	 * @param sInputFile
	 * @param sOutputFile
	 */
	public static void copyFile(String sInputFile, String sOutputFile){
		copyFile(sInputFile, sOutputFile, false);
	}
	
	/**
	 * Copies the input stream into the output stream.
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
	    int len;
	    while((len = in.read(buffer)) >= 0) {
	      out.write(buffer, 0, len);
	    }
	    in.close();
	    out.close();
	  }	
	
	/**
	 * Checks to see if a directory exists.  If it does not, then it creates it.
	 * @param sDirectory		The directory to check
	 * @param bMakeAncestors	Should all ancestors be created with mkdirs()?
	 */
	public static void makeCheckDirectory(String sDirectory, boolean bMakeAncestors){
		File fFile = new File(sDirectory);
		if (!fFile.exists()){
			if (bMakeAncestors){
				fFile.mkdirs();
			} else {
				fFile.mkdir();
			}
		}
	}
	/**
	 * Overload that does not automatically create ancestors
	 * @see makeCheckDirectory(String sDirectory, boolean bMakeAncestors)
	 * @param sDirectory
	 */
	public static void makeCheckDirectory(String sDirectory){
		makeCheckDirectory(sDirectory, false);
	}
	
    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }
	
	/**
	 * Converts the given xml document to a byte array which is then written
	 * to the given output stream.
	 * 
	 * @param doc
	 * @param outputStream
	 * @throws IOException
	 */
	public synchronized static void serializeXml(Document doc, OutputStream outputStream) throws IOException
	{
		StringWriter writer = new StringWriter();			
		XMLSerializer output = new XMLSerializer(writer, new OutputFormat());
		output.serialize(doc);	        
		outputStream.write( writer.toString().getBytes() );      
	}
	
	
	/**
	 * RootFilter
	 * @author jrourke
	 * Compares equality of two files from the root without the extension
	 * This is used in the File listFiles() method
	 * One obtains this filter via getRootFilter()
	 */
	private class RootFilter implements FileFilter{
		private String mWildName;
		
		private RootFilter(String sWildName){
			this.mWildName = sWildName;
		}
		
		public boolean accept(File f) {
			//get name and not full path
			String sFile = f.getName();
			if (sFile.lastIndexOf(".") < 0){
				return false;
			}
			String sFileToCheck = sFile.substring(0,sFile.lastIndexOf("."));
			if (sFileToCheck.equals(mWildName)){
				return true;
			}  else {
				return false;
			}
		}
	}
	/**
	 * @return Returns a new instance of RootFilter
	 */
	public FileFilter getRootFilter(String sWildName){
   		return new RootFilter(sWildName);
    }
	
//	************************************GETTERS AND SETTERS******************************************
	/**
	 * @return Returns a new instance of MyErrorHandler
	 */
	private DefaultHandler getDefaultHandlerInstance(){
   		return new MyErrorHandler();
    }
	/**
	 * @return Returns the errorFound.
	 */
	private boolean getErrorFound() {
		return this.errorFound;
	}
	/**
	 * @param errorFound The errorFound to set.
	 */
	private void setErrorFound(boolean errorFound) {
		this.errorFound = errorFound;
	}
	
	/**
	 * Performs chmod on the given file 
	 * @param fullpath
	 * @param permissions
	 */
	public static void chmod(String fullpath, String permissions)
	{
		// Do this only on a linux device
		if(DeviceProperty.getCurrentPlatform().equals(DeviceProperty.PLATFORM_LINUX)){
			logger.info("Executing chmod: "+ fullpath);
			Runtime rt = Runtime.getRuntime();
			Process proc = null;
			try
			{		
				String cmd = "chmod "+ permissions + " "+ fullpath;
				proc = rt.exec( cmd );
				proc.getInputStream().close(); 
				proc.getOutputStream().close(); 
				proc.getErrorStream().close();									
				proc.waitFor();	
			}
			catch(Exception e)
			{			
				e.printStackTrace();
			}
			finally
			{
				if(proc != null){
					proc.destroy();
					proc = null;	
				}			
				rt = null;	
			}	
		}
	}
	
	/**
	 * Performs dos2unix on the given file 
	 * @param fullpath
	 * @param permissions
	 */
	public static void dos2unix(String fullpath)
	{
		// Do this only on a linux device
		if(DeviceProperty.getCurrentPlatform().equals(DeviceProperty.PLATFORM_LINUX)){
			logger.info("Executing dos2unix: "+ fullpath);
			Runtime rt = Runtime.getRuntime();
			Process proc = null;
			try
			{		
				String cmd = "/usr/bin/dos2unix "+ fullpath;
				proc = rt.exec( cmd );
	            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERR");            		            
	            StreamGobbler outputGobbler = new  StreamGobbler(proc.getInputStream(), "OUT");
	            errorGobbler.start();
	            outputGobbler.start();					
				int exitVal = proc.waitFor();
			}
			catch(Exception e){			
				e.printStackTrace();
			}finally {
				if(proc != null){
					proc.destroy();
					proc = null;	
				}			
				rt = null;	
			}
		}
	}	
	
	/**
	 * Zips the each file in filenames into the given outputPath
	 * @param filenames
	 * @param outputPath
	 */
	public static void zip(String[] filenames, String outputPath)
	{
	    // Create a buffer for reading the files
	    byte[] buf = new byte[1024];
	    
	    try {
	        // Create the ZIP file
	        ZipOutputStream out = new ZipOutputStream(new FileOutputStream( outputPath ) );
	    
	        // Compress the files
	        for (int i=0; i<filenames.length; i++) {
	            FileInputStream in = new FileInputStream(filenames[i]);
	    
	            // Add ZIP entry to output stream.
	            filenames[i] = filenames[i].replace("\\", "/");
	            out.putNextEntry(new ZipEntry(filenames[i].substring(filenames[i].lastIndexOf("/")+1)));
	    
	            // Transfer bytes from the file to the ZIP file
	            int len;
	            while ((len = in.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	    
	            // Complete the entry
	            out.closeEntry();
	            in.close();
	        }
	    
	        // Complete the ZIP file
	        out.close();
	    } catch (IOException e) {
			logger.error( e );
	    }
	}
	
	/**
	 * Unzips the given zipFileFullPath into the given destinationDirectory.
	 * Returns the name of the last file extracted (useful when we know we're 
	 * unzipping a zip file containing only one file).
	 * @param zipFileFullPath
	 * @param destinationDirectory
	 */
	public static String unzip(String zipFileFullPath, String destinationDirectory, boolean buildDirectoryStructure)
	{				
		String lastExtractedFile = "";
	    try 
		{
			ZipFile zipFile = new ZipFile( zipFileFullPath );
			Enumeration entries = zipFile.entries();
			while(entries.hasMoreElements()) 
			{
				ZipEntry entry = (ZipEntry)entries.nextElement();			  
				File file = new File( destinationDirectory +"/"+ entry.getName());
				if( file.getParentFile() != null ) 
				{
					// If the flag to build the directory structure was passed in
					if( buildDirectoryStructure ){
						file.getParentFile().mkdirs();
					}
				}
			  
				if( !entry.isDirectory() ) 
				{
					// If we're not building the the directory structure
					if( buildDirectoryStructure == false )
					{
						// Get the name of the file without directory paths
						if( entry.getName().indexOf("/") >= 0 ){ 
							lastExtractedFile = entry.getName().substring( entry.getName().lastIndexOf("/") + 1 );
						}else{
							lastExtractedFile = entry.getName();
						}
					}else{
						lastExtractedFile = entry.getName();
					}			
					logger.info("Extracting file: " + lastExtractedFile);
					Files.copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream( destinationDirectory +"/"+ lastExtractedFile)));				  
				}			  
	      }
	      zipFile.close();
	    } 
		catch(Exception ioe) {
			ErrorLogger.logError( "Error extracting zip file: " + zipFileFullPath );
			return null;
	    }		
		return lastExtractedFile;
	}	
	
	/**
	 * Compares the version of the first file to the version of the second file.
	 * Returns -1 if v1 is less than v2.
	 * Returns 0 if v1 is equal to v2.
	 * Returns 1 if v1 is greater than v2.
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static int compareVersions(String v1, String v2)
	{
		int result = 0; 
		while( v1.length() > 0 || v2.length() > 0 )
		{			
			// Get the next section of the version
			String v1Part = v1;
			String v2Part = v2; 
			if( v1.indexOf(".") > 0 ){
				v1Part = v1Part.substring( 0, v1Part.indexOf(".") );
			}
			if( v2Part.indexOf(".") > 0 ){
				v2Part = v2Part.substring( 0, v2Part.indexOf(".") );
			}
			
			// Compare this section of each the file version and the device version
			result = Files.compareVersionSection( v1Part, v2Part );
			
			// If the version of the first file is not equal to the version of the second file -- we do not need to continue comparing
			if( result != 0 ){
				break;
			}
			
			// If we've gotten this far, go to the next section of the version
			if( v1.indexOf(".") > 0 ){			
				v1 = v1.substring( v1.indexOf(".") + 1 );
			}else{
				v1 = "";
			}
			if( v2.indexOf(".") > 0 ){			
				v2 = v2.substring( v2.indexOf(".") + 1 );
			}else{
				v2 = "";
			}
		}		
		return result;
	}	
	
	/**
	 * Compare the given FRAGMENT of the file version against the device version.
	 * @param strFileVersion
	 * @param strDeviceVersion
	 * @return
	 */
	public static int compareVersionSection(String strFileVersion, String strDeviceVersion)
	{
		int result = 0;			

		// If there is a beta number for the file version, but not one on the device
		if( strFileVersion.indexOf("b") >= 0 && strDeviceVersion.indexOf("b") < 0 )
		{
			// Ignore the beta version
			strFileVersion = strFileVersion.substring( 0, strFileVersion.indexOf("b") );
			result = doVersionComparison( strFileVersion, strDeviceVersion );
		}
		// If there is a beta number for both the file version and the device version
		else if( strFileVersion.indexOf("b") >= 0 && strDeviceVersion.indexOf("b") >= 0 )
		{
			// Compare pre-beta and post-beta
			String preBetaFileVersion = strFileVersion.substring( 0, strFileVersion.indexOf("b") );
			String preBetaDeviceVersion = strDeviceVersion.substring( 0, strDeviceVersion.indexOf("b") );				
			result = doVersionComparison( preBetaFileVersion, preBetaDeviceVersion );			
			if( result == 0 )
			{
				// Compare post-beta
				String postBetaFileVersion = strFileVersion.substring( strFileVersion.indexOf("b") + 1 );
				String postBetaDeviceVersion = strDeviceVersion.substring( strDeviceVersion.indexOf("b") + 1 );
				result = doVersionComparison( postBetaFileVersion, postBetaDeviceVersion );			
			}
		}
		// If there is a beta number for the device version, but not the file version
		else if( strFileVersion.indexOf("b") < 0 && strDeviceVersion.indexOf("b") >= 0 )
		{
			// Compare pre-beta			
			strDeviceVersion = strDeviceVersion.substring( 0, strDeviceVersion.indexOf("b") );				
			result = doVersionComparison( strFileVersion, strDeviceVersion );			
			
			// If equal, the file version wins
			if( result == 0 ) {
				result = 1;
			}						
		}
		else
		{
			result = doVersionComparison( strFileVersion, strDeviceVersion );
		}		
		return result;
	}
	
	/**
	 * Returns -1 if the file version is less than the device version.
	 * Returns 0 if the file version is equal to the device version.
	 * Returns 1 if the file version is greater than the device version.
	 * @param strFileVersion
	 * @param strDeviceVersion
	 * @return
	 */
	private static int doVersionComparison(String strFileVersion, String strDeviceVersion)
	{
		int result = 0;			
		
		// If there is a value for the file version, but not one for the device version
		if( strFileVersion.length() > 0 && strDeviceVersion.length() == 0 )
		{
			// This file version must be higher -- return true
			result = 1;	
		}
		// If there is a value for the device version, but not one for the file version
		else if( strFileVersion.length() == 0 && strDeviceVersion.length() > 0 )
		{
			// This file version must not be higher -- return false
			result = -1;
		}		
		// If there is a value for both the file version and the device version
		else if( strFileVersion.length() > 0 && strDeviceVersion.length() > 0 )
		{
			int fileVersion = Integer.valueOf( strFileVersion ).intValue();
			int deviceVersion = Integer.valueOf( strDeviceVersion ).intValue();
			if( fileVersion > deviceVersion ){
				result = 1;
			}else if( fileVersion < deviceVersion ){
				result = -1;
			}		
		}	
		return result;
	}	
	
	/**
	 * First attempts a java File.renameTo. If unsuccessful, attempts
	 * a file copy. The is necessary because java.renameTo is not supported
	 * when moving across a partitioned drive in RedHat ES4.
	 * @param f
	 * @param newFile
	 * @return
	 */
	public static boolean renameTo(File f, File newFile)
	{
		boolean success = false;
		
		// First attempt the renameTo
		success = f.renameTo( newFile );
		
		// If we did not rename sucessfully
		if( success == false )
		{
			// Attempt a file copy
			logger.info("File rename unsuccessful. Attempting copy: "+ f.getAbsolutePath() +" to "+ newFile.getAbsolutePath());
			success = Files.copyFile( f.getAbsolutePath(), newFile.getAbsolutePath(), true );
			
			// Remove the original file
			f.delete();
		}		
		return success;		
	}
	
	public static String read(File f)
	{
		String result = "";
		try {			
			// Read in the contents of the properties file
			BufferedReader in = new BufferedReader( new FileReader( f ) );
			StringBuffer fileContents = new StringBuffer();
			String line = "";
			while((line = in.readLine()) != null) {
				fileContents.append( line );
				fileContents.append( "\n" );
			}
			in.close();
			in = null;
			result =  fileContents.toString();
		} catch (FileNotFoundException e) {
			logger.error( e );
		} catch (IOException e) {
			logger.error( e );
		}
		return result;
	}
	
	public static void write(File f, String fileContents)
	{
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter( f.getAbsolutePath() ));
			out.write( fileContents );
			out.close();
		} catch (IOException e) {
			logger.error( e );
		}
	}
	
	/**
	 * Overwrites the contents of the given fullPath
	 * with random characters.
	 *  
	 * @param fullPath
	 */
	public static void shred(String fullPath)
	{
		Random random = new Random();
		try{
			File f = new File( fullPath );
			if( f.exists() )
			{
				for( int i=0; i<50; i++)
				{
					FileChannel rwChannel = new RandomAccessFile(f.getAbsolutePath(), "rw").getChannel();
		            int numBytes = (int)rwChannel.size();
		            ByteBuffer buffer = rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, numBytes);
		            buffer.clear();
		            byte[] randomBytes = new byte[numBytes];
		            random.nextBytes(randomBytes); 
		            buffer.put(randomBytes);
		            rwChannel.write(buffer);
		            rwChannel.close();					
				}
			}else{
				logger.info("Unable to shred file. File does not exist: "+ f.getAbsolutePath());
			}
		}catch(Exception e){
			logger.error( e );
		}
	}
	
}//class Files
