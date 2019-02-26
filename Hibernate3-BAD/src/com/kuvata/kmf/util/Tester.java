package com.kuvata.kmf.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class Tester {
	private static String sourcePath = "d:\\data\\kuvata\\presentations\\video\\";
	private static String destPath = "d:\\3dtest\\";

	private static void copyFile(File source, File dest) throws IOException {
	    FileChannel inputChannel = null;
	    FileChannel outputChannel = null;
	    try {
	        inputChannel = new FileInputStream(source).getChannel();
	        outputChannel = new FileOutputStream(dest).getChannel();
	        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
	    } finally {
	        inputChannel.close();
	        outputChannel.close();
	    }
	}

	private static String getReferencedFilePath(String assetId) {
		File folder = new File( sourcePath );
		File[] listOfFiles = folder.listFiles();

		String result = null;
		long maxMtime = -1;
	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	  String name = listOfFiles[i].getName();
	    	  if( name.startsWith( new String(assetId + "-" ) ) && name.endsWith(".ultrad") ) {
	    		  File f = new File( sourcePath + name );
	    		  if( result == null || f.lastModified() > maxMtime ) {
	    			  result = name;
	    			  maxMtime = f.lastModified();
	    		  }
	    	  }
	      }
	    }
	    return result;
	}
	
	private static void processScheduleFile(String filename) {
		 try {
			  HashMap<String, String> assets = new HashMap<String, String>();
			  
			  File file = new File(filename);
			  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			  DocumentBuilder db = dbf.newDocumentBuilder();
			  Document doc = db.parse(file);
			  doc.getDocumentElement().normalize();
			  NodeList nodeList = doc.getElementsByTagName("Asset");

		      for(int x=0,size= nodeList.getLength(); x<size; x++) {
		    	  String xmlfile = nodeList.item(x).getAttributes().getNamedItem("presentationSerialization").getNodeValue();
		    	  String assetId = xmlfile.substring( xmlfile.indexOf("/")+1 , xmlfile.indexOf("-"));
		    	  String name = getReferencedFilePath( assetId );
		    	  if( name != null ) {
		    		  File sourceFile = new File( sourcePath + name );
		    		  File destFile = new File( destPath + name );
		    		  if( destFile.exists() == false || destFile.length() != sourceFile.length() ) {
		    			  copyFile( sourceFile, destFile );
		    		  }
		    		  assets.put(assetId, name);
		    	  }
		      }
		      
		      // get PlaylistAsset Elements and construct playlist
		      String playlist = "";
		      nodeList = doc.getElementsByTagName("PlaylistAsset");
		      for(int x=0,size=nodeList.getLength(); x<size; x++) {
		    	  String assetId = nodeList.item(x).getAttributes().getNamedItem("assetId").getNodeValue();
		    	  if( assetId != null && assets.get(assetId) != null ) {
		    		  playlist = playlist + assets.get(assetId) + "\r\n";
		    	  }
		      }
		      playlist = playlist + "/loop\r\n";

		      // dismount mass storage
		      
		      Writer writer = null;
		      try {
		          writer = new BufferedWriter(new OutputStreamWriter(
		                new FileOutputStream(destPath + "UltraD.udpl"), "utf-8"));
		          writer.write( playlist );
		      } catch (IOException ex) {
		    	  ex.printStackTrace();
		      } finally {
		         try {writer.close();} catch (Exception ex) {}
		      }
		      
		      // mount mass storage
		      
		  } catch (Exception e) {
		    e.printStackTrace();
		  }		
	}
	
	public static void main(String argv[]) {
		  File folder = new File( "d:\\testing" );
		  for( File f : folder.listFiles() ) {
			  if (f.isFile() && f.getName().toLowerCase().endsWith(".ultrad") ) {
				  String n = f.getName();
				  try {
					  int x = n.indexOf( '-' );
					  if( x == 2 ) {
						  String n2 = n.substring(3);
						  System.out.println( n2 );
						  f.renameTo( new File( "d:\\testing\\" + n2) );
					  }
				  } catch(Exception e) {
					  e.printStackTrace();
				  }
			  }
		  }
	}
}
