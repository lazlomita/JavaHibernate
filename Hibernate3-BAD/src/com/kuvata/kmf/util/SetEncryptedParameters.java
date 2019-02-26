package com.kuvata.kmf.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import parkmedia.device.entities.ConnectionInfo;

import com.kuvata.kmf.SchemaDirectory;

public class SetEncryptedParameters {

	private static final String PASSWORD_HASH_FILEPATH = "/etc/shadow";
	private static final String HISTORY_FILEPATH = "/root/.bash_history";
	private static final String CLEAR_HISTORY_COMMAND = "history -c";
	
	public static void setEncryptedParameters() throws Exception
	{
		// TODO: Exit out of program if it was not launched from the PM kernel
		
		// Prompt for the path to the encrypted presentations folder
		System.out.println("Path to encrypted presentations folder:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
		String encryptedPresentationsPath = br.readLine().trim(); 	
		
		// Prompt for the encfs password
		System.out.println("EncFS Password:");
		br = new BufferedReader(new InputStreamReader(System.in)); 
		String encfsPassword1 = br.readLine().trim();
		
		// Prompt for the encfs password a second time
		System.out.println("Confirm EncFS Password:");
		br = new BufferedReader(new InputStreamReader(System.in)); 
		String encfsPassword2 = br.readLine().trim();		
		
		// If the passwords do not match -- break out of the program
		if( encfsPassword1.equals( encfsPassword2 ) == false ){
			System.out.println("Passwords do not match! Exiting.");
			return;
		}
		
		// Get the root hash by parsing /etc/shadow
		String rootHash = getRootHash();
		
		// Use the root hash to encrypt the encfs password using TripleDES
		TripleDES decrypter = new TripleDES( rootHash.getBytes() );
		String encryptedEncfsPassword = decrypter.encryptToString( encfsPassword1 );							 
		decrypter = null;
		
		SchemaDirectory.initialize("kuvata", "SetEncryptedParameters", null, false, true);
		ConnectionInfo connectionInfo = ConnectionInfo.getConnectionInfo();
		if( connectionInfo != null ){
			connectionInfo.setEncryptedPresentationsPath( encryptedPresentationsPath );
			connectionInfo.setEncryptedEncfsPassword( encryptedEncfsPassword );
			connectionInfo.update();
		}else{
			connectionInfo = new ConnectionInfo();
			connectionInfo.setEncryptedPresentationsPath( encryptedPresentationsPath );
			connectionInfo.setEncryptedEncfsPassword( encfsPassword1 );
			connectionInfo.save();
		}
		
		// Delete history
		File historyFile = new File( HISTORY_FILEPATH );
		if( historyFile.exists() ){
			historyFile.delete();
		}
	}
	
	/**
	 * Attempts to locate the hash value of the password associated with the root user
	 * @return
	 */
	public static String getRootHash()
	{
		String result = "";
		try
		{
			File f = new File( PASSWORD_HASH_FILEPATH );
			if( f.exists() )
			{
				// Look for the hash associated with the root user in PASSWORD_HASH_FILE
				BufferedReader in = new BufferedReader( new FileReader( PASSWORD_HASH_FILEPATH ) );
				String line = "";
				while( (line = in.readLine()) != null )
				{
					// Identify the user associated with this line
					if( line.indexOf(":") > 0 )
					{
						String username = line.substring(0, line.indexOf(":") );
						line = line.substring( line.indexOf(":") + 1 );
						
						// If this is the root user
						if( username.equalsIgnoreCase("root") )
						{
							// Locate the hashed password, which should be everything between the first and second colon (:)
							if( line.indexOf(":") >= 0 )
							{
								result = line.substring(0, line.indexOf(":") );
								break;
							}						
						}				
					}		
				}	
				in.close();
				in = null;				
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		return result;		
	}
	
	/**
	 * Replaces the root hash with random characters
	 * @return
	 */
	public static void replaceRootHash()
	{		
		Random random = new Random();
		try
		{
			StringBuffer fileContents = new StringBuffer();
			File f = new File( PASSWORD_HASH_FILEPATH );			
			if( f.exists() )
			{
				// Look for the hash associated with the root user in PASSWORD_HASH_FILE
				BufferedReader in = new BufferedReader( new FileReader( PASSWORD_HASH_FILEPATH ) );
				String line = "";
				while( (line = in.readLine()) != null )
				{
					// Identify the user associated with this line
					if( line.indexOf(":") > 0 )
					{
						String username = line.substring(0, line.indexOf(":") );
						String linePart = line.substring( line.indexOf(":") + 1 );
						
						// If this is the root user
						if( username.equalsIgnoreCase("root") )
						{
							// Locate the hashed password, which should be everything between the first and second colon (:)
							if( linePart.indexOf(":") >= 0 )
							{
								// Replace the hashed password with random characters of the same length
								String currentHashedPassword = linePart.substring(0, linePart.indexOf(":") );								
								byte[] randomBytes = new byte[ currentHashedPassword.length() ];
					            random.nextBytes( randomBytes );
								String randomHashedPassword = new String( randomBytes );
								if( randomHashedPassword.length() > currentHashedPassword.length() ){
									randomHashedPassword = randomHashedPassword.substring( 0, currentHashedPassword.length() );
								}
								line = username +":"+ randomHashedPassword + linePart.substring(line.indexOf(":") );
							}						
						}				
					}
					
					// Write out the original line
					fileContents.append( line + "\n" );	
				}	
				in.close();
				in = null;
								
				/*
				 * Rewrite the properties file
				 */
				BufferedWriter out = new BufferedWriter( new FileWriter( f.getAbsolutePath() ) );			
				out.write( fileContents.toString() );
				out.close();
				out = null;	
			}
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			setEncryptedParameters();
		} catch (Exception e) {
			System.err.println("An unexpected error occurred in SetEncryptedParameters: "+ e.getMessage());
			e.printStackTrace();
		}
	}

}
