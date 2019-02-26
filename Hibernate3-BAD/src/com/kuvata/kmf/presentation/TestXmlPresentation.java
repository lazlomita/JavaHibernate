/*
 * Created on Jan 17, 2004
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

import com.kuvata.kmf.asset.Xml;

import electric.xml.ParseException;

public class TestXmlPresentation extends TestCase {
    private static final boolean CLEANUP = false;
    
    private Xml createTestXml() throws IOException
    {
        String filename = KuvataConfig.getKuvataHome() + "/tmp/testXml.txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write("test xml");
        out.close();
        
        Xml h = new Xml();
        h.setXmlpageId( new Long(1) );
        h.setFileloc( filename );
        h.setAdler32( new Long("12345") );
        
        return h;
    }

    public void testImagePresentation() throws HibernateException, IOException, NoSuchFieldException, ParseException
    {
        Xml h = createTestXml();

        /*
         * Test the constructor, which should also copy the image file to the
         * /kuvata/presentations/image/ folder.
         */
        XmlPresentation hp = new XmlPresentation(h);
        assertEquals( hp.getPresenterType(), PresenterType.XML );
        
        /*
         * xmlEncode the ImagePresentation to the same folder
         */
        hp.xmlEncode();

        /*
         * Decode it into memory
         */
        String filename = hp.getFilename();
        XmlPresentation hp2 = (XmlPresentation) XmlPresentation.xmlDecode( filename );
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
