/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.PresenterType;

import com.kuvata.kmf.asset.Audio;
import com.kuvata.kmf.util.Reformat;
/**
 * @author anaber
 *
 */
public class AudioPresentation extends Presentation {
	public ReferencedFile referencedFile;
	public Float length;
	public Integer normalizationBeginAvg;
	/**
	 * 
	 *
	 */
	public AudioPresentation()
	{
	    SUBDIRECTORY = "audio";	    
	}
	/**
	 * 
	 * @param a
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public AudioPresentation(Audio a, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(a);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param a
	 * @throws HibernateException
	 * @throws IOException
	 */
	public AudioPresentation(Audio a) throws HibernateException, IOException
	{
	    this();
	    	    
	    this.setPresenterType( PresenterType.AUDIO );
	    this.setAssetId( a.getAudioId() );
	    this.setLength(a.getLength());
	    this.setNormalizationBeginAvg(a.getNormalizationBeginAvg());
	    
	    String extension = getFileExtension(a.getFileloc());
	    String newFileloc = SUBDIRECTORY + "/" +a.getAudioId()+"-audio-"+a.getAdler32() + extension;
	    ReferencedFile audioFile = new ReferencedFile( newFileloc );
	    audioFile.createFrom( a.getFileloc() );
	    
	    this.setReferencedFile( audioFile );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "AudioPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId(),result,3);
		result = Reformat.getSafeHash(length,result,5);
		result = Reformat.getSafeHash(this.getReferencedFile().fileloc,result,7);
		result = Reformat.getSafeHash(this.getNormalizationBeginAvg(),result,11);
		return result < 0 ? -result : result;
	}
	/**
	 * @return Returns the referencedFile.
	 */
	public ReferencedFile getReferencedFile() {
		return referencedFile;
	}
	/**
	 * @param referencedFile The referencedFile to set.
	 */
	public void setReferencedFile(ReferencedFile referencedFile) {
		this.referencedFile = referencedFile;
	}
    /**
     * @return Returns the length.
     */
    public Float getLength() {
        return length;
    }
    /**
     * @param length The length to set.
     */
    public void setLength(Float length) {
        this.length = length;
    }
	public Integer getNormalizationBeginAvg() {
		return normalizationBeginAvg;
	}
	public void setNormalizationBeginAvg(Integer normalizationBeginAvg) {
		this.normalizationBeginAvg = normalizationBeginAvg;
	}
}
