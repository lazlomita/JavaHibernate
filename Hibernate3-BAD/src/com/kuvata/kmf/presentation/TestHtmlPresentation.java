/*
 * Created on Nov 19, 2004
 *
 * Copyright 2004 Kuvata, Inc.
 */
package com.kuvata.kmf.presentation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import junit.framework.TestCase;

import org.hibernate.HibernateException;

import parkmedia.KuvataConfig;
import com.kuvata.kmf.usertype.PresenterType;

import com.kuvata.kmf.asset.Html;

import electric.xml.ParseException;
/**
 * 
 * @author anaber
 *
 * 
 */
public class TestHtmlPresentation extends TestCase {
    private static final boolean CLEANUP = false;
    /**
     * 
     * @return
     * @throws IOException
     */ 
    private Html createTestHtml() throws IOException
    {
        String filename = KuvataConfig.getKuvataHome() + "/tmp/testHtml.txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write("test html");
        out.close();
        
        Html h = new Html();
        h.setHtmlpageId( new Long(1) );
        h.setFileloc( filename );
        h.setAdler32( new Long("12345") );
        
        return h;
    }
    /**
     * 
     * @throws HibernateException
     * @throws IOException
     * @throws NoSuchFieldException
     */
    public void testImagePresentation() throws HibernateException, IOException, NoSuchFieldException, ParseException
    {
        Html h = createTestHtml();

        /*
         * Test the constructor, which should also copy the image file to the
         * /kuvata/presentations/image/ folder.
         */
        HtmlPresentation hp = new HtmlPresentation(h);
        assertEquals( hp.getPresenterType(), PresenterType.HTML );
        
        /*
         * xmlEncode the ImagePresentation to the same folder
         */
        hp.xmlEncode();

        /*
         * Decode it into memory
         */
        String filename = hp.getFilename();
        HtmlPresentation hp2 = (HtmlPresentation) HtmlPresentation.xmlDecode( filename );
        assertEquals( filename, hp2.getFilename() );
        
        /*
         * Delete the encoded file and the image file
         */
        if( CLEANUP )
        {
	        File f = new File(filename);
	        f.delete();
	        f = new File( hp2.getReferencedFile().getFileloc() );
	        f.delete();
        }
    }
}
