package com.kuvata.kmf.util;

import com.kuvata.kmf.Layout;
import com.kuvata.kmf.LayoutDisplayarea;
import com.kuvata.kmf.SchemaDirectory;

public class UpdateDisplayAreaRoles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SchemaDirectory.initialize( "kuvata", "UpdateDisplayAreaRoles", null, false, true );
		
		// For each layout
		for(Layout l : Layout.getLayouts()){
			System.out.println("Updating displayareas under layout: " + l.getLayoutName());
			// For each non-shared display area
			for(LayoutDisplayarea lda : l.getLayoutDisplayareas()){
				if(lda.getDisplayarea().getIsShared() == null || lda.getDisplayarea().getIsShared() == Boolean.FALSE){
					lda.getDisplayarea().copyPermissionEntries(l, true, true);
				}
			}
		}
	}

}
