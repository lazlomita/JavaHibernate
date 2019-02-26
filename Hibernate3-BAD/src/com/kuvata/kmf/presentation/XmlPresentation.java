/*
 * Created on Jan 17, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.IOException;
import org.hibernate.HibernateException;
import com.kuvata.kmf.usertype.PresenterType;
import com.kuvata.kmf.asset.Xml;
import com.kuvata.kmf.util.Reformat;

/**
 * 
 * @author anaber
 *
 */
public class XmlPresentation extends Presentation {
	public ReferencedFile referencedFile;
	/**
	 * 
	 *
	 */
	public XmlPresentation()
	{
	    SUBDIRECTORY = "xml";	    
	}
	/**
	 * 
	 * @param h
	 * @param presenterType
	 * @throws HibernateException
	 * @throws IOException
	 */
	public XmlPresentation(Xml h, PresenterType presenterType) throws HibernateException, IOException
	{
	    this(h);
	    this.setPresenterType(presenterType);
	}
	/**
	 * 
	 * @param h
	 * @throws HibernateException
	 * @throws IOException
	 */
	public XmlPresentation(Xml h) throws HibernateException, IOException
	{
	    this();
	    
	    this.setPresenterType(PresenterType.XML);
	    this.setAssetId( h.getXmlpageId() );
	    
	    String extension = getFileExtension(h.getFileloc());
	    String newFileloc = SUBDIRECTORY + "/" +h.getXmlpageId()+"-xml-"+h.getAdler32() + extension;
	    ReferencedFile imageFile = new ReferencedFile( newFileloc );
	    imageFile.createFrom( h.getFileloc() );
	    
	    this.setReferencedFile( imageFile );
	}
	/**
	 * 
	 */
	public int hashCode()
	{
		int result = "XmlPresentation".hashCode();
		result = Reformat.getSafeHash(this.getPresenterType().toString(),result,2);
		result = Reformat.getSafeHash(this.getAssetId().hashCode(),result,3);
		result = Reformat.getSafeHash(this.getReferencedFile().getFileloc().hashCode(),result,5);
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
}
