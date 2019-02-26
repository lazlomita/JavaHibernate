/*
 * Created on May 26, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.kuvata.kmf.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author Jeff Mattson
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TripleDES {
	
	private Cipher ecipher;
	private Cipher dcipher;
	
	private final static byte[] KEY_BYTES = {103, 101, 97, 89, 83, 79, 73, 71, 67, 61, 59, 53, 47, 43, 41, 37, 31, 29, 23, 19, 17, 13, 11, 7};
	private final static String CIPHER_TRANS = "DESede/CBC/PKCS5Padding";
	private final static String KEY_GEN_TRANS = "DESede";	
	private final static int BUFSIZE = 2048;
	
	private byte[] keybytes;
	
	private DESedeKeySpec keyspec;
	private SecretKeyFactory keyfactory;
	private SecretKey key;	
		
	private byte[] iv = {65, 110, 68, 26, 69, -78, -56, -37};
	
	public TripleDES() throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
	{	
		this( KEY_BYTES );
	}
	
	public TripleDES(byte[] keyBytes) throws InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException
	{		
		try
		{
			this.keybytes = keyBytes;	
			keyspec = new DESedeKeySpec( keybytes );
			keyfactory = SecretKeyFactory.getInstance( KEY_GEN_TRANS );
			key = keyfactory.generateSecret( keyspec );	
					
			IvParameterSpec ivp = new IvParameterSpec(iv);
			ecipher = Cipher.getInstance(CIPHER_TRANS);
			dcipher = Cipher.getInstance(CIPHER_TRANS);
			ecipher.init(Cipher.ENCRYPT_MODE, key, ivp);
			dcipher.init(Cipher.DECRYPT_MODE, key, ivp);
		}
		catch (NoSuchPaddingException e)   { e.printStackTrace(); }
		catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
		catch (InvalidKeyException e)      { e.printStackTrace(); }
	}	
	
	public String decrypt(byte[] encrypted)
	{			
		try
		{							
			// Decrypt
			byte[] dec = dcipher.doFinal(encrypted);
			// Decode using utf-8
			return new String(dec, "UTF8");
		}
		catch (BadPaddingException e)          { e.printStackTrace(); }
		catch (IllegalBlockSizeException e)    { e.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }		
		return null;
	}
	
	public String decryptFromString(String encrypted)
	{			
		try
		{			
			// Base64 Decode
			byte[] decoded = new BASE64Decoder().decodeBuffer(encrypted);
			// Decrypt
			byte[] dec = dcipher.doFinal(decoded);
			// Decode using utf-8
			return new String(dec, "UTF8");
		}
		catch (BadPaddingException e)          { e.printStackTrace(); }
		catch (IllegalBlockSizeException e)    { e.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		catch (IOException e) 				   { e.printStackTrace(); }	
		return null;
	}
	
	public byte[] encrypt(String decrypted)
	{		
		try
		{			
		   // Encode the string into bytes using utf-8
		   byte[] utf8 = decrypted.getBytes("UTF8");
		   // Encrypt
		   byte[] enc = ecipher.doFinal(utf8);			   
		   return enc;
		}
		catch (BadPaddingException e)          { e.printStackTrace(); }
		catch (IllegalBlockSizeException e)    { e.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }        
		return null;
	}
	
	public String encryptToString(String decrypted)
	{		
		try
		{
		   // Encode the string into bytes using utf-8
		   byte[] utf8 = decrypted.getBytes("UTF8");
		   // Encrypt
		   byte[] enc = ecipher.doFinal(utf8);	
		   // Base64 Encode
		   return new BASE64Encoder().encode(enc);						   
		}
		catch (BadPaddingException e)          { e.printStackTrace(); }
		catch (IllegalBlockSizeException e)    { e.printStackTrace(); }
		catch (UnsupportedEncodingException e) { e.printStackTrace(); }        
		return null;
	}
	
	public static String bytesToString(byte[] bytes)
	{
		return new BASE64Encoder().encode(bytes);		
	}
	
	public static byte[] stringToBytes(String string)
	{		
		try {
			return new BASE64Decoder().decodeBuffer(string);
		}
		catch (IOException e) { e.printStackTrace(); }		
		return null;
	}
	
	public static void main(String[] args) {
		
		try
		{
			TripleDES tdes = new TripleDES();
			String decrypted = tdes.decryptFromString("wVc4CeMNL5bladNCCesLVw==");
			System.out.println(decrypted);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
