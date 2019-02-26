package com.kuvata.kmf.util;

import java.util.Iterator;
import java.util.List;

import com.kuvata.kmf.Layout;
import com.kuvata.kmf.LayoutDisplayarea;
import com.kuvata.kmf.SchemaDirectory;

/**
 * This program is to be run when converting a < 2.12 server to 2.12.
 * It calculates the height and with of each layout based on its displayareas
 * and saves them in the new height and width columns of the layout table.
 * 
 * @author jrandesi
 */
public class ConvertLayoutResolutions {

	private static void convertLayoutResolutions(String schema)
	{
		SchemaDirectory.initialize(schema, "ConvertLayoutResolutions", null, false, true);
		try
		{			
			// For each layout
			List layouts = Layout.getLayouts();
			for( Iterator i=layouts.iterator(); i.hasNext(); )
			{
				Layout layout = (Layout)i.next();
				System.out.println("Calculating resolution for layout: "+ layout.getLayoutName());
				
				// If there is not already a value for the width of this layout
				if( layout.getWidth() == null ){					
					Integer width = new Integer( getWidth( layout ) );
					System.out.println("Setting width: "+ width);
					layout.setWidth( width );
				}
				
				// If there is not already a value for the height of this layout
				if( layout.getHeight() == null ){
					Integer height = new Integer( getHeight( layout ) );
					System.out.println("Setting height: "+ height);
					layout.setHeight( height );
				}
				layout.update();
			}			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Calculates the maximum x position of the given layout.
	 * @param layout
	 */
	public static int getWidth(Layout layout)
	{
		int result = 0;
		for( Iterator i = layout.getLayoutDisplayareas().iterator(); i.hasNext(); )
		{			
			// Ignore displayareas with a height and width of zero
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			if( !(lda.getDisplayarea().getHeight().equals( new Integer(0) ) && lda.getDisplayarea().getWidth().equals( new Integer(0) )) )
			{
				// Get the ending x position of this displayarea by adding x position and width
				int xpos = lda.getXpos().intValue() + lda.getDisplayarea().getWidth().intValue();
				if( xpos > result ){
					result = xpos;
				}
			}
		}
		return result;
	}
	
	/**
	 * Calculates the maximum y position of this layout.
	 * @param layout
	 */
	public static int getHeight(Layout layout)
	{
		int result = 0;
		for( Iterator i = layout.getLayoutDisplayareas().iterator(); i.hasNext(); )
		{			
			// Ignore displayareas with a height and width of zero
			LayoutDisplayarea lda = (LayoutDisplayarea)i.next();
			if( !(lda.getDisplayarea().getHeight().equals( new Integer(0) ) && lda.getDisplayarea().getWidth().equals( new Integer(0) )) )
			{
				// Get the ending y position of this displayarea by adding y position and height
				int ypos = lda.getYpos().intValue() + lda.getDisplayarea().getHeight().intValue();
				if( ypos > result ){
					result = ypos;
				}
			}
		}
		return result;
	}	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if( args.length == 1 ){
			convertLayoutResolutions(args[0]);	
		}else{
			System.out.println("Usage: ConvertLayoutResolutions schema");
		}		
	}

}
