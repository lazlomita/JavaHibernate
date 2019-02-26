/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import com.kuvata.kmf.Constants;
import com.kuvata.kmf.SchemaDirectory;
/**
 * 
 * @author anaber
 *
 * 
 */
public class ReferencedFile 
{
	private static Logger logger = Logger.getLogger(ReferencedFile.class);
	
	/**
	 * 
	 */
    public String fileloc;	// Relative path
    private Long filesize;  // In bytes
    
    /**
     * 
     *
     */
    public ReferencedFile()
    {}
    /**
     * 
     * @param fileloc
     */
    public ReferencedFile(String fileloc)
    {
        this.fileloc = fileloc;
    }
	
    /**
     * 
     * @param fileloc
     * @throws IOException
     * @throws HibernateException
     */
    public void createFrom(String fileloc) throws IOException, HibernateException
    {
    	String fullpath = SchemaDirectory.getSchemaBaseDirectory() + "/"+ Constants.PRESENTATIONS +"/" + this.fileloc; 
        File origFile = new File( fileloc );
		File destFile = new File( fullpath );
		this.setFilesize( origFile != null && origFile.exists() ? Long.valueOf( origFile.length() ) : 0 );
        
		// If the destination file already exists, and it's the same size as the original file
		if( destFile.exists() && origFile.length() == destFile.length() )
        {			
            // don't have to copy it again! -- touch both the asset file and presentation file so we know it's not old
			origFile.setLastModified( System.currentTimeMillis() );
			destFile.setLastModified( System.currentTimeMillis() );					
            return;
        }

		try 
		{
			// Create a physical link from the assets directory to the presentations directory
			Process p = Runtime.getRuntime().exec("ln -f " + origFile.getAbsolutePath() + " "+ destFile.getAbsolutePath());
			p.waitFor();
			
			// If the exitVal of the process is not zero -- throw an exception
			if( p.exitValue() != 0 ){
				throw new IOException("An error occurred while creating presentation symbolic link: "+ origFile.getAbsolutePath() +" to "+ destFile.getAbsolutePath() +".");
			}
			p = null;
		} catch (Exception e) {
			logger.error("An error occurred while creating presentation symbolic link: "+ origFile.getAbsolutePath() +" to "+ destFile.getAbsolutePath() +".", e);
		}
    }

    /**
     * @return Returns the fileloc.
     */
    public String getFileloc() {
        return fileloc;
    }
    /**
     * @param fileloc The fileloc to set.
     */
    public void setFileloc(String fileloc) {
        this.fileloc = fileloc;
    }
	/**
	 * @return the filesize
	 */
	public Long getFilesize() {
		return filesize;
	}
	/**
	 * @param filesize the filesize to set
	 */
	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

    
}
