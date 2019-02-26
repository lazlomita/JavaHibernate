package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import parkmedia.KMFLogger;
import parkmedia.KuvataConfig;
import parkmedia.usertype.AssetIngesterColumnType;

import com.Ostermiller.util.ExcelCSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.kuvata.kmf.AppUser;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.HibernateSession;
import com.kuvata.kmf.SchemaDirectory;

public class AutoAssetIngest extends Thread{
	
	private static KMFLogger logger = (KMFLogger)KMFLogger.getInstance( AutoAssetIngest.class );
	private static final int WATCH_FREQUENCY = 30000;
	private static final String DUPLICATE_MODE_DEFAULT = "ignore";
	private static final DateFormat OUTPUT_FORMAT = new SimpleDateFormat( Constants.OUTPUT_DATE_FORMAT );
	private static final String CONF_FILE = "autoAssetIngester.conf";
	
	private static File configFile;
	private static HashMap<AppUser, File> autoIngesterMap = new HashMap();
	
	public static final String AUTOMATICALLY_INGESTED_SUB_DIRECTORY = "/automatically_ingested";
	
	public void readConfigFile(){
		try {
			// Clear the map so that we don't have any old configs in case they get removed from the conf file
			autoIngesterMap.clear();
			
			configFile = new File(KuvataConfig.getKuvataHome() + "/" + CONF_FILE);
			if(configFile.exists()){
				FileInputStream fstream = new FileInputStream(configFile);
			    DataInputStream in = new DataInputStream(fstream);
			    BufferedReader br = new BufferedReader(new InputStreamReader(in));
			    String strLine;
			    
			    while ((strLine = br.readLine()) != null){
			    	String username = strLine.split(" ")[0];
			    	String directory = strLine.split(" ")[1];
			    	if(username != null && directory != null){
				    	AppUser au = AppUser.getAppUser(username);
				    	if(au != null){
				    		File dir = new File(directory);
				    		
				    		// Create the directory if needed
							if(dir.exists() == false){
								dir.mkdir();
								
								// Chmod the directory
								Files.chmod(dir.getAbsolutePath(), "775");
							}
							
							File subDir = new File(dir.getAbsolutePath() + AUTOMATICALLY_INGESTED_SUB_DIRECTORY );
							
							// Make the sub-directory
							if(subDir.exists() == false){
								if(subDir.mkdir() == false){
									logger.error("Can not create directory " + subDir.getAbsolutePath());
								}else{
									// Chmod the directory
									Files.chmod(subDir.getAbsolutePath(), "775");
								}
							}
							
							// Populate the map
							if(subDir.exists()){
								autoIngesterMap.put(au, dir);
							}
				    	}
			    	}
			    }
			}else{
				logger.error("Could not locate " + KuvataConfig.getKuvataHome() + "/" + CONF_FILE);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void run(){

		HashMap<String, Long> fileLengthMap = new HashMap();
		
		try
		{
			SchemaDirectory.initialize( "kuvata", "AutoAssetIngest", "Asset Ingester", false, false );
			
			while(true){
				try {
					
					// Read the config file
					readConfigFile();
					
					for(AppUser au : autoIngesterMap.keySet()){
						
						File dir = autoIngesterMap.get(au);
					
						File files[] = dir.listFiles();
						
						// For each file in the directory
						for(int i=0;i<files.length;i++){
							
							// Make sure that this is a CSV file
							if(files[i].isFile() && files[i].getName().endsWith(".csv")){
								
								// Make sure that this file is not being uploaded
								if(fileLengthMap.containsKey(files[i].getAbsolutePath()) && (Long)fileLengthMap.get(files[i].getAbsolutePath()) == files[i].length() ){
									
									// Read the csv file to make sure all underlying assets have been uploaded
									boolean filesUploaded = true;
									ArrayList<String> assetFileLocs = new ArrayList();
									InputStream is = new FileInputStream( files[i] );
									LabeledCSVParser lcsvp = new LabeledCSVParser( new ExcelCSVParser( is, ',' ) );
									
									// For all assets in this csv file
									while(lcsvp.getLine() != null){
										boolean fileUploaded = true;
										String assetFileLoc = lcsvp.getValueByLabel( AssetIngesterColumnType.ASSET.toString() );
										if(assetFileLoc != null && assetFileLoc.trim().length() > 0){
											
											assetFileLoc = assetFileLoc.trim();
											
											// If this is a local file
											if(assetFileLoc.toLowerCase().startsWith("file://") || !assetFileLoc.contains(":")){
												
												// Make file loc correction for files local to the csv file
												if(assetFileLoc.startsWith("/") || assetFileLoc.startsWith("\\")){
													assetFileLoc = assetFileLoc.substring(1);
												}
												
												// Strip the file:// or add the directory location
												assetFileLoc = assetFileLoc.contains(":") ? assetFileLoc = assetFileLoc.substring(7) : dir.getAbsolutePath() + "/" + assetFileLoc;
												
												File assetFile = new File(assetFileLoc);
												
												if(assetFile.exists()){
													// Make sure that this file is not being uploaded
													if(!fileLengthMap.containsKey(assetFile.getAbsolutePath()) || (Long)fileLengthMap.get(assetFile.getAbsolutePath()) != assetFile.length() ){
														if(fileLengthMap.containsKey(assetFile.getAbsolutePath())){
															fileUploaded = false;
														}
														filesUploaded = false;
													}
													assetFileLocs.add(assetFile.getAbsolutePath());
													fileLengthMap.put(assetFile.getAbsolutePath(), assetFile.length());
												}else{
													fileUploaded = false;
													filesUploaded = false;
												}
											}
											
											// If this is not a local file
											else{
												
												// If this is an ftp/http file
												if(assetFileLoc.toLowerCase().startsWith("ftp://") || assetFileLoc.toLowerCase().startsWith("http://")){
													String username = lcsvp.getValueByLabel( AssetIngesterColumnType.AUTH_USERNAME.toString() );
													String password = lcsvp.getValueByLabel( AssetIngesterColumnType.AUTH_PASSWORD.toString() );
													
													// If a valid username and password was passed in 
													if(username != null && username.trim().length() > 0 && password != null && password.trim().length() > 0){
														
														if(assetFileLoc.toLowerCase().startsWith("http://")){
															Authenticator.setDefault(new HttpAuthenticator(username, password));
														}else{
															// Add the username and the password to the URL
															assetFileLoc = "ftp://" + Reformat.urlEscape(username) + ":" + Reformat.urlEscape(password) + "@" + assetFileLoc.substring(6);
														}
													}
												}
												
												try {
													
													URL url = new URL(assetFileLoc);
													URLConnection conn = url.openConnection();
													long size = conn.getContentLength();
													
													// Make sure that this file is not being uploaded
													if(size < 0 || !fileLengthMap.containsKey(assetFileLoc) || (Long)fileLengthMap.get(assetFileLoc) != size ){
														if(fileLengthMap.containsKey(assetFileLoc)){
															fileUploaded = false;
														}
														filesUploaded = false;
													}
													
													assetFileLocs.add(assetFileLoc);
													fileLengthMap.put(assetFileLoc, size);
													
													conn.getInputStream().close();
													
												} catch (Exception e) {
													fileUploaded = false;
													filesUploaded = false;
													logger.error(e);
												}
											}
										}
										
										if(fileUploaded == false){
											// Log the fact that this file is not completely uploaded
											logger.info("Waiting for " + lcsvp.getValueByLabel( AssetIngesterColumnType.ASSET.toString() ) + " to be completely uploaded.");
										}
									}
									
									if(filesUploaded){
										
										logger.info("Ingesting " + files[i].getAbsolutePath());
										
										// Remove the asset files from the map
										fileLengthMap.keySet().removeAll(assetFileLocs);
										
										// Chmod the csv file
										Files.chmod(files[i].getAbsolutePath(), "775");
										
										String controlFilePath = au.getName() +"-"+ OUTPUT_FORMAT.format( new Date() ) + ".csv";
										controlFilePath = dir.getAbsolutePath() + AUTOMATICALLY_INGESTED_SUB_DIRECTORY +"/"+ controlFilePath;
										
										// Move the file to 
										Files.copyFile(files[i].getAbsolutePath(), controlFilePath, true);
										files[i].delete();
										
										// Encrypt the password
										TripleDES decrypter = new TripleDES();
										String unencryptedPassword = decrypter.decryptFromString( au.getPassword() );
										
										// Run the asset ingester
										String result = AssetIngester.runAssetIngester(au.getName(), unencryptedPassword, DUPLICATE_MODE_DEFAULT, controlFilePath);
										logger.info("Auto asset ingester result: " + result);
										
										// Remove the file from the collection
										fileLengthMap.remove(files[i].getAbsolutePath());
									}
								}else{
									// Log the fact that this file is not completely uploaded
									logger.info("Waiting for " + files[i].getAbsolutePath() + " to be completely uploaded.");
									fileLengthMap.put(files[i].getAbsolutePath(), files[i].length());
								}
							}
						}
					}
					Thread.sleep(WATCH_FREQUENCY);
				} catch (Exception e) {
					logger.error(e);
				} finally{
					HibernateSession.closeSession();
				}
			}
		}catch(Exception e){
			logger.error( e );
		}finally{
			HibernateSession.closeSession();	
		}
	}
	
	public static class HttpAuthenticator extends Authenticator {
		private String username, password;
		
		public HttpAuthenticator(String user, String pass) {
			username = user;
			password = pass;
		}
		
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}
}
