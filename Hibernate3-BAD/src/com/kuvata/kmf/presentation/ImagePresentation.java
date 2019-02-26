/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.asset.Image;
import com.kuvata.kmf.util.Reformat;
/**
 * @author anaber
 *
 */
public class ImagePresentation extends Presentation {
	public ReferencedFile referencedFile;
	public Integer width;
	public Integer height;
	public Boolean fitToSize;
	public Integer opacity;
	/**
	 * 
	 *
	 */
	public ImagePresentation()
	{
	    SUBDIRECTORY = "image";	    
	}
	/**
	 * 
	 * @param i
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public ImagePresentation(Image i, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(i);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param i
	 * @throws HibernateException
	 * @throws IOException
	 */
	public ImagePresentation(Image i) throws HibernateException, IOException
	{
	    this();
	    	    
	    this.setPresenterType( PresenterType.IMAGE );
	    this.setAssetId( i.getImageId() );
	    this.setWidth( i.getWidth() );
	    this.setHeight( i.getHeight() );
		this.setFitToSize( i.getFitToSize() );
		this.setOpacity(i.getOverlayOpacity());
	    
	    String extension = getFileExtension(i.getFileloc());
	    String newFileloc = SUBDIRECTORY + "/" +i.getImageId()+"-image-"+i.getAdler32() + extension;
	    ReferencedFile imageFile = new ReferencedFile( newFileloc );
	    imageFile.createFrom( i.getFileloc() );
	    
	    this.setReferencedFile( imageFile );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "ImagePresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getWidth(),result,5);
		result = Reformat.getSafeHash(this.getHeight(),result,7);
		result = Reformat.getSafeHash(this.getFitToSize(),result,11);
		result = Reformat.getSafeHash(this.getReferencedFile().getFileloc().hashCode(),result,13);
		result = Reformat.getSafeHash(this.getOpacity(),result,17);
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
     * @return Returns the width.
     */
    public Integer getWidth() {
        return width;
    }
    /**
     * @param width The width to set.
     */
    public void setWidth(Integer width) {
        this.width = width;
    }
    /**
     * @return Returns the height.
     */
    public Integer getHeight() {
        return height;
    }
    /**
     * @param height The height to set.
     */
    public void setHeight(Integer height) {
        this.height = height;
    }
	/**
	 * @return Returns the fitToSize.
	 */
	public Boolean getFitToSize() {
		return fitToSize;
	}
	
	/**
	 * @param fitToSize The fitToSize to set.
	 */
	public void setFitToSize(Boolean fitToSize) {
		this.fitToSize = fitToSize;
	}
	public Integer getOpacity() {
		return opacity;
	}
	public void setOpacity(Integer opacity) {
		this.opacity = opacity;
	}
}