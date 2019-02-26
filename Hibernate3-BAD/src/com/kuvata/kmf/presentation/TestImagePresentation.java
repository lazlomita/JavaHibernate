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

import com.kuvata.kmf.asset.Image;

import electric.xml.ParseException;
/**
 * 
 * @author anaber
 *
 */
public class TestImagePresentation extends TestCase {
    private static final boolean CLEANUP = false;
    /**
     * 
     * @return
     * @throws HibernateException
     * @throws IOException
     */
    private Image createTestImage() throws HibernateException, IOException
    {
        String filename = KuvataConfig.getKuvataHome() + "/tmp/testImage.txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write("test image");
        out.close();
        
        Image i = new Image();
        i.setImageId( new Long(1) );
        i.setHeight( new Integer(1024) );
        i.setWidth( new Integer(768) );
        i.setFileloc( filename );
        i.setAdler32( new Long("12345") );
        
        return i;
    }
    /**
     * 
     * @throws HibernateException
     * @throws IOException
     * @throws NoSuchFieldException
     */
    public void testImagePresentation() throws HibernateException, IOException, NoSuchFieldException, ParseException
    {
        Image i = createTestImage();

        /*
         * Test the constructor, which should also copy the image file to the
         * /kuvata/presentations/image/ folder.
         */
        ImagePresentation ip = new ImagePresentation(i);
        assertEquals( ip.getPresenterType(), PresenterType.IMAGE );
        
        /*
         * xmlEncode the ImagePresentation to the same folder
         */
        ip.xmlEncode();

        /*
         * Decode it into memory
         */
        String filename = ip.getFilename();
        ImagePresentation ip2 = (ImagePresentation) ImagePresentation.xmlDecode( filename );
        assertEquals( ip.getPresenterType(), PresenterType.IMAGE );
        assertEquals( filename, ip2.getFilename() );
        
        /*
         * Delete the encoded file and the image file
         */
        if( CLEANUP )
        {
	        File f = new File(filename);
	        f.delete();
	        f = new File( ip2.getReferencedFile().getFileloc() );
	        f.delete();
        }

    }
}
