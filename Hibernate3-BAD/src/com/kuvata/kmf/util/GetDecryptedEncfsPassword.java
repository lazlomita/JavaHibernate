package com.kuvata.kmf.util;

import parkmedia.device.entities.ConnectionInfo;

import com.kuvata.kmf.SchemaDirectory;

/**
 * This class retrieves and prints to System.out the decrypted password
 * as defined by the root hash and encryptedEncfsPassword
 * @author jrandesi
 *
 */
public class GetDecryptedEncfsPassword {

	private static void getDecryptedPassword()
	{
		// TODO: do not run if not run from PM kernel
		try
		{
			SchemaDirectory.initialize("kuvata", "GetDecryptedPassword", null, false, true);
			
			// If there is a value for the encryptedPresentationsPath
			ConnectionInfo connectionInfo = ConnectionInfo.getConnectionInfo();
			if( connectionInfo != null )
			{
				String encryptedPresentationsPath = connectionInfo.getEncryptedPresentationsPath();
				String encryptedEncfsPassword = connectionInfo.getEncryptedEncfsPassword();
				if( encryptedPresentationsPath != null && encryptedPresentationsPath.length() > 0 
						&& encryptedEncfsPassword != null && encryptedEncfsPassword.length() > 0 )
				{
					// Use the root hash to decrypt the encrypted encfs password using TripleDES
					String rootHash = SetEncryptedParameters.getRootHash();
					TripleDES decrypter = new TripleDES( rootHash.getBytes() );
					String decryptedEncfsPassword = decrypter.decryptFromString( encryptedEncfsPassword );
					
					// Print out the decrypted password to system.out
					System.out.println( decryptedEncfsPassword );
					decrypter = null;									
				}
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		getDecryptedPassword();
	}

}
