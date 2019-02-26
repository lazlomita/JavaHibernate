
package com.kuvata.kmf.presentation;

import java.io.IOException;

import org.hibernate.HibernateException;

import com.kuvata.kmf.usertype.PresenterType;

import com.kuvata.kmf.asset.Flash;
import com.kuvata.kmf.util.Reformat;
/**
 * @author anaber
 *
 */
public class FlashPresentation extends Presentation {
	public ReferencedFile referencedFile;
	public Integer width;
	public Integer height;
	public Boolean fitToSize;
	public Boolean setTransparentBg;
	public Integer opacity;

	/**
	 * 
	 *
	 */
	public FlashPresentation()
	{
	    SUBDIRECTORY = "flash";	    
	}
	/**
	 * 
	 * @param f
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public FlashPresentation(Flash f, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(f);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param f
	 * @throws HibernateException
	 * @throws IOException
	 */
	public FlashPresentation(Flash f) throws HibernateException, IOException
	{
	    this();
	    	    
	    this.setPresenterType( PresenterType.FLASH );
	    this.setAssetId( f.getAssetId() );
	    this.setWidth( f.getWidth() );
	    this.setHeight( f.getHeight() );
		this.setFitToSize( f.getFitToSize() );
		this.setOpacity(f.getOverlayOpacity());
		this.setSetTransparentBg(f.getSetTransparentBg());
	    
	    String extension = getFileExtension(f.getFileloc());
	    String newFileloc = SUBDIRECTORY + "/" +f.getAssetId()+"-flash-"+f.getAdler32() + extension;
	    ReferencedFile flashFile = new ReferencedFile( newFileloc );
	    flashFile.createFrom( f.getFileloc() );
	    
	    this.setReferencedFile( flashFile );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "FlashPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getWidth(),result,5);
		result = Reformat.getSafeHash(this.getHeight(),result,7);
		result = Reformat.getSafeHash(this.getFitToSize(),result,11);
		result = Reformat.getSafeHash(this.getReferencedFile().getFileloc().hashCode(),result,13);
		result = Reformat.getSafeHash(this.getOpacity(),result,17);
		result = Reformat.getSafeHash(this.getSetTransparentBg(),result,19);
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
	public Boolean getSetTransparentBg() {
		return setTransparentBg;
	}
	public void setSetTransparentBg(Boolean setTransparentBg) {
		this.setTransparentBg = setTransparentBg;
	}
	
	
}
