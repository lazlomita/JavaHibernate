/*
 * Created on Jan 28, 2005
 *
 * Kuvata, Inc.
 */
package com.kuvata.kmf.util;

import com.kuvata.kmf.Asset;

/**
 * @author Jeff Mattson
 *
 * Revisions:
 */
public class CalcAdler32 
{
	public static void main(String[] args) 
	{
		if (args.length != 1)
		{
			System.out.println("usage: java com.kuvata.kmf.util.CalcAdler32 filename(full path)");
		}	
		else
		{
			Long adler32 = Asset.calculateAdler32(args[0]);
			//System.out.println("Adler32: " + adler32.toString());
			System.out.println(adler32.toString());
		}
				
	}
}
