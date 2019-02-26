package com.kuvata.kmf.util;

import java.text.ParseException;

public class PopulateDynamicQueryParts {
	
	public static void main(String[] args) throws ParseException{
		// Due to the removal of attr columns on the playlist and content_rotation tables, this utility can no longer be used
		/*SimpleDateFormat inputFormat = new SimpleDateFormat(Constants.DATE_TIME_FORMAT_DISPLAYABLE);
		SimpleDateFormat outputFormat = new SimpleDateFormat("MM/dd/yyyy");
		SchemaDirectory.initialize( "kuvata", "PopulateDynamicQueryParts", null, false, true );
		
		String hql = "SELECT p FROM Playlist p WHERE p.attrDefinition IS NOT NULL";
		
		List<Playlist> playlists = HibernateSession.currentSession().createQuery(hql).list();
		
		for(Playlist p : playlists){
			System.out.println("Updating playlist " + p.getPlaylistName() + " (" + p.getPlaylistId() + ")");
			if(p.getAttrDefinition().getType().equals(AttrType.DATE)){
				String[] parts = p.getAttrValue().split("~");
				DynamicQueryPart.create(p, null, p.getAttrDefinition(), ">=", DateType.SELECTED_DATE.getPersistentValue(), outputFormat.parse(outputFormat.format(inputFormat.parse(parts[0]))), null, null, 0);
				DynamicQueryPart.create(p, null, p.getAttrDefinition(), "<", DateType.SELECTED_DATE.getPersistentValue(), outputFormat.parse(outputFormat.format(inputFormat.parse(parts[1]))), null, null, 1);
			}else if(p.getAttrDefinition().getType().equals(AttrType.NUMBER)){
				String[] parts = p.getAttrValue().split("~");
				DynamicQueryPart.create(p, null, p.getAttrDefinition(), ">=", parts[0], null, null, null, 0);
				DynamicQueryPart.create(p, null, p.getAttrDefinition(), "<=", parts[1], null, null, null, 1);
			}else{
				if(p.getAttrDefinition().getSearchInterface().equals(SearchInterfaceType.INPUT_BOX) || p.getAttrDefinition().getSearchInterface().equals(SearchInterfaceType.MEMO)){
					if(p.getAttrValue() == null || p.getAttrValue().length() == 0){
						// Wildcard
						DynamicQueryPart.create(p, null, p.getAttrDefinition(), "=", "*", null, null, null, 0);
					}else{
						DynamicQueryPart.create(p, null, p.getAttrDefinition(), "=", p.getAttrValue(), null, null, null, 0);
					}
				}else{
					DynamicQueryPart.create(p, null, p.getAttrDefinition(), "=", p.getAttrValue(), null, null, null, 0);
				}
			}
			
			p.setDynamicContentType("metadata");
			p.update();
		}
		
		hql = "SELECT cr FROM ContentRotation cr WHERE cr.attrDefinition IS NOT NULL";
		
		List<ContentRotation> crs = HibernateSession.currentSession().createQuery(hql).list();
		
		for(ContentRotation cr : crs){
			System.out.println("Updating content rotation " + cr.getContentRotationName() + " (" + cr.getContentRotationId() + ")");
			if(cr.getAttrDefinition().getType().equals(AttrType.DATE)){
				String[] parts = cr.getAttrValue().split("~");
				DynamicQueryPart.create(null, cr, cr.getAttrDefinition(), ">=", DateType.SELECTED_DATE.getPersistentValue(), outputFormat.parse(outputFormat.format(inputFormat.parse(parts[0]))), null, null, 0);
				DynamicQueryPart.create(null, cr, cr.getAttrDefinition(), "<", DateType.SELECTED_DATE.getPersistentValue(), outputFormat.parse(outputFormat.format(inputFormat.parse(parts[1]))), null, null, 1);
			}else if(cr.getAttrDefinition().getType().equals(AttrType.NUMBER)){
				String[] parts = cr.getAttrValue().split("~");
				DynamicQueryPart.create(null, cr, cr.getAttrDefinition(), ">=", parts[0], null, null, null, 0);
				DynamicQueryPart.create(null, cr, cr.getAttrDefinition(), "<=", parts[1], null, null, null, 1);
			}else{
				if(cr.getAttrDefinition().getSearchInterface().equals(SearchInterfaceType.INPUT_BOX) || cr.getAttrDefinition().getSearchInterface().equals(SearchInterfaceType.MEMO)){
					if(cr.getAttrValue() == null || cr.getAttrValue().length() == 0){
						// Wildcard
						DynamicQueryPart.create(null, cr, cr.getAttrDefinition(), "=", "*", null, null, null, 0);
					}else{
						DynamicQueryPart.create(null, cr, cr.getAttrDefinition(), "=", cr.getAttrValue(), null, null, null, 0);
					}
				}else{
					DynamicQueryPart.create(null, cr, cr.getAttrDefinition(), "=", cr.getAttrValue(), null, null, null, 0);
				}
				
			}
			
			cr.setDynamicContentType("metadata");
			cr.update();
		}*/
	}

}
