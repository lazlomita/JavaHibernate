/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.asset.Video;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * @author anaber
 *
 */
public class VideoPresentation extends Presentation {
	public ReferencedFile referencedFile;
	public Boolean displayEmbeddedSubtitles = Boolean.FALSE;
	public Boolean suppressAudio = Boolean.FALSE;
	public Boolean anamorphicWidescreen = Boolean.FALSE;
	public Boolean framesync = Boolean.FALSE;
	public Integer opacity;
	public String videoCodec;
	public Integer normalizationBeginAvg;
	
	/**
	 * 
	 *
	 */
	public VideoPresentation()
	{
	    SUBDIRECTORY = "video";	    
	}
	/**
	 * 
	 * @param v
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public VideoPresentation(Video v, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(v);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param v
	 * @throws HibernateException
	 * @throws IOException
	 */
	public VideoPresentation(Video v) throws HibernateException, IOException
	{
	    this();
	    
	    this.setPresenterType(PresenterType.VIDEO);
	    this.setAssetId( v.getVideoId() );
	    this.setDisplayEmbeddedSubtitles( v.getDisplayEmbeddedSubtitles() != null ? v.getDisplayEmbeddedSubtitles() : Boolean.FALSE );
	    this.setSuppressAudio( v.getSuppressAudio() != null ? v.getSuppressAudio() : Boolean.FALSE );
	    this.setAnamorphicWidescreen( v.getAnamorphicWidescreen() != null ? v.getAnamorphicWidescreen() : Boolean.FALSE);
	    this.setFramesync( v.getFramesync() != null ? v.getFramesync() : Boolean.FALSE);
	    this.setOpacity( v.getOverlayOpacity());
	    this.setVideoCodec(v.getVideoCodec());
	    this.setNormalizationBeginAvg(v.getNormalizationBeginAvg());
		
	    String extension = getFileExtension( v.getFileloc() );
	    String newFileloc = SUBDIRECTORY + "/" +v.getVideoId()+"-video-"+v.getAdler32() + extension;
	    ReferencedFile videoFile = new ReferencedFile( newFileloc );
	    videoFile.createFrom( v.getFileloc() );
	    this.setReferencedFile( videoFile );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "VideoPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId(),result,3);
		result = Reformat.getSafeHash(this.getReferencedFile().getFileloc(),result,5);
		result = Reformat.getSafeHash(this.getDisplayEmbeddedSubtitles(),result,11);
		result = Reformat.getSafeHash(this.getSuppressAudio(),result,13);
		result = Reformat.getSafeHash(this.getAnamorphicWidescreen(),result,17);
		result = Reformat.getSafeHash(this.getOpacity(),result,19);
		result = Reformat.getSafeHash(this.getVideoCodec(),result,23);
		result = Reformat.getSafeHash(this.getNormalizationBeginAvg(),result,29);
		result = Reformat.getSafeHash(this.getFramesync(),result,31);
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
	 * @return Returns the displayEmbeddedSubtitles.
	 */
	public synchronized Boolean getDisplayEmbeddedSubtitles() {
		return displayEmbeddedSubtitles;
	}
	
	/**
	 * @param displayEmbeddedSubtitles The displayEmbeddedSubtitles to set.
	 */
	public synchronized void setDisplayEmbeddedSubtitles(
			Boolean displayEmbeddedSubtitles) {
		this.displayEmbeddedSubtitles = displayEmbeddedSubtitles;
	}
	/**
	 * @return the suppressAudio
	 */
	public Boolean getSuppressAudio() {
		return suppressAudio;
	}
	/**
	 * @param suppressAudio the suppressAudio to set
	 */
	public void setSuppressAudio(Boolean suppressAudio) {
		this.suppressAudio = suppressAudio;
	}
	public Boolean getAnamorphicWidescreen() {
		return anamorphicWidescreen;
	}
	public void setAnamorphicWidescreen(Boolean anamorphicWidescreen) {
		this.anamorphicWidescreen = anamorphicWidescreen;
	}
	public Integer getOpacity() {
		return opacity;
	}
	public void setOpacity(Integer opacity) {
		this.opacity = opacity;
	}
	public String getVideoCodec() {
		return videoCodec;
	}
	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}
	public Integer getNormalizationBeginAvg() {
		return normalizationBeginAvg;
	}
	public void setNormalizationBeginAvg(Integer normalizationBeginAvg) {
		this.normalizationBeginAvg = normalizationBeginAvg;
	}
	public Boolean getFramesync() {
		return framesync;
	}
	public void setFramesync(Boolean framesync) {
		this.framesync = framesync;
	}
}
