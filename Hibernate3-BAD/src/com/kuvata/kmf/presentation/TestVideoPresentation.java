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

import com.kuvata.kmf.asset.Video;

import electric.xml.ParseException;
/**
 * 
 * @author anaber
 *
 */
public class TestVideoPresentation extends TestCase {
    private static final boolean CLEANUP = false;
    /**
     * 
     * @return
     * @throws IOException
     */
    private Video createTestVideo() throws IOException
    {
        String filename = KuvataConfig.getKuvataHome() + "/tmp/testVideo.txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(filename));
        out.write("test video");
        out.close();
        
        Video v = new Video();
        v.setVideoId( new Long(1) );
        v.setFileloc( filename );
        v.setAdler32( new Long("12345") );
        
        return v;
    }
    /**
     * 
     * @throws HibernateException
     * @throws IOException
     * @throws NoSuchFieldException
     */
    public void testVideoPresentation() throws HibernateException, IOException, NoSuchFieldException, ParseException
    {
        Video v = createTestVideo();

        /*
         * Test the constructor, which should also copy the image file to the
         * /kuvata/presentations/image/ folder.
         */
        VideoPresentation vp = new VideoPresentation(v);
        assertEquals( vp.getPresenterType(), PresenterType.VIDEO );
        
        /*
         * xmlEncode the ImagePresentation to the same folder
         */
        vp.xmlEncode();

        /*
         * Decode it into memory
         */
        String filename = vp.getFilename();
        VideoPresentation vp2 = (VideoPresentation) VideoPresentation.xmlDecode( filename );
        assertEquals( filename, vp2.getFilename() );
        
        /*
         * Delete the encoded file and the image file
         */
        if( CLEANUP )
        {
	        File f = new File(filename);
	        f.delete();
	        f = new File( vp2.getReferencedFile().getFileloc() );
	        f.delete();
        }
    }
}
